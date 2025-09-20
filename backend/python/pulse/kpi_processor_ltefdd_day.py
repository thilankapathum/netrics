import os
import zipfile
import pandas as pd
import mysql.connector
from mysql.connector import Error
import logging
import time
import shutil
from datetime import datetime
import re
from typing import Dict, List, Optional, Tuple
import json
import sys
from pathlib import Path

# Force UTF-8 encoding for stdout
sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('kpi_processor.log', encoding='utf-8'),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)


class KPIProcessor:
    def __init__(self, config_file='config-ltefdd-day.json'):
        """Initialize KPI Processor with configuration"""
        # load_dotenv()

        self.config = self.load_config(config_file)

        self.db_config = {
            "host": os.getenv("MYSQL_HOST", self.config["database"].get("host")),
            "port": int(os.getenv("MYSQL_PORT", self.config["database"].get("port", 3306))),
            "database": os.getenv("MYSQL_DATABASE", self.config["database"].get("database")),
            "user": os.getenv("MYSQL_USER", self.config["database"].get("user")),
            "password": os.getenv("MYSQL_PASSWORD", self.config["database"].get("password")),
        }

        # self.db_config = self.config['database']
        self.ftp_folder = self.config['ftp_folder']
        self.processed_folder = self.config['processed_folder']
        self.temp_folder = self.config['temp_folder']

        # Database-driven configuration (loaded from DB)
        self.standard_kpis = {}  # Will hold standard KPI info
        self.kpi_mappings = {}  # Will hold OSS to standard KPI mappings
        self.oss_configs = {}  # Will hold OSS configuration info
        self.standard_kpi_mappings = {}  # Will hold standard KPI to numerator/denominator mappings
        self.district_codes = {}  # Will hold district code mappings

        # Load configuration from database
        self.load_database_configuration()

        # Create necessary directories
        os.makedirs(self.processed_folder, exist_ok=True)
        os.makedirs(self.temp_folder, exist_ok=True)

    def load_config(self, config_file: str) -> Dict:
        """Load basic configuration from JSON file (only database connection and paths)"""
        try:
            with open(config_file, 'r') as f:
                return json.load(f)
        except FileNotFoundError:
            logger.error(f"Config file {config_file} not found. Creating default config.")
            return self.create_default_config(config_file)

    def create_default_config(self, config_file: str) -> Dict:
        """Create a default configuration file with minimal settings"""
        default_config = {
            "database": {
                "host": "mysql",
                "port": 3306,
                "database": "netrics_pulse_db",
                "user": "root",
                "password": "root"
            },
            "ftp_folder": "/app/day-average/ltefdd/ftp",
            "processed_folder": "/app/day-average/ltefdd/processed",
            "temp_folder": "/app/day-average/ltefdd/temp"
        }

        with open(config_file, 'w') as f:
            json.dump(default_config, f, indent=4)

        logger.info(f"Created default config file: {config_file}")
        return default_config

    def load_database_configuration(self):
        """Load configuration from database tables"""
        connection = None
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor(dictionary=True)

            # Load standard KPIs
            logger.info("Loading standard KPIs from database...")
            cursor.execute("""
                SELECT id, kpi_name, unit, type, worst_order, threshold 
                FROM lte_fdd_standard_kpi
            """)
            standard_kpis_data = cursor.fetchall()

            for kpi in standard_kpis_data:
                self.standard_kpis[kpi['kpi_name']] = {
                    'id': kpi['id'],
                    'unit': kpi['unit'],
                    'type': kpi['type'],
                    'worst_order': kpi['worst_order'],
                    'threshold': kpi['threshold']
                }

            logger.info(f"Loaded {len(self.standard_kpis)} standard KPIs")

            # Load standard KPI numerator/denominator mappings
            logger.info("Loading standard KPI numerator/denominator mappings...")
            cursor.execute("""
                SELECT 
                    m.id,
                    m.standard_kpi_id,
                    m.numerator_id,
                    m.denominator_id,
                    s.kpi_name as standard_kpi_name,
                    n.kpi_name as numerator_kpi_name,
                    d.kpi_name as denominator_kpi_name
                FROM lte_fdd_standard_raw_kpi_mapping m
                JOIN lte_fdd_standard_kpi s ON m.standard_kpi_id = s.id
                LEFT JOIN lte_fdd_standard_kpi n ON m.numerator_id = n.id
                LEFT JOIN lte_fdd_standard_kpi d ON m.denominator_id = d.id
            """)
            standard_mapping_data = cursor.fetchall()

            for mapping in standard_mapping_data:
                standard_kpi_name = mapping['standard_kpi_name']
                self.standard_kpi_mappings[standard_kpi_name] = {
                    'standard_kpi_id': mapping['standard_kpi_id'],
                    'numerator_id': mapping['numerator_id'],
                    'denominator_id': mapping['denominator_id'],
                    'numerator_kpi_name': mapping['numerator_kpi_name'],
                    'denominator_kpi_name': mapping['denominator_kpi_name']
                }

            logger.info(f"Loaded {len(self.standard_kpi_mappings)} standard KPI mappings")

            # Load OSS configurations
            logger.info("Loading OSS configurations from database...")
            cursor.execute("""
                SELECT id, oss_name, identifier, vendor 
                FROM oss
            """)
            oss_data = cursor.fetchall()

            for oss in oss_data:
                self.oss_configs[oss['identifier']] = {
                    'id': oss['id'],
                    'oss_name': oss['oss_name'],
                    'vendor': oss['vendor'],
                    'filename_pattern': rf"{oss['identifier']}.*\.(zip|xlsx|csv)$",
                    'sheet_name': 0,
                    'skip_rows': 0,
                    'encoding': 'utf-8',
                    'delimiter': ","
                }

            logger.info(f"Loaded {len(self.oss_configs)} OSS configurations")

            # Load KPI mappings
            logger.info("Loading KPI mappings from database...")
            cursor.execute("""
                SELECT 
                    m.id,
                    m.lte_fdd_standard_kpi_id,
                    m.oss_kpi_name,
                    m.oss_id,
                    m.multiplication_factor,
                    s.kpi_name as standard_kpi_name,
                    o.identifier as oss_identifier
                FROM lte_fdd_kpi_mapping m
                JOIN lte_fdd_standard_kpi s ON m.lte_fdd_standard_kpi_id = s.id
                JOIN oss o ON m.oss_id = o.id
            """)
            mapping_data = cursor.fetchall()

            for mapping in mapping_data:
                # Create a composite key: oss_identifier + oss_kpi_name
                mapping_key = f"{mapping['oss_identifier']}:{mapping['oss_kpi_name']}"
                self.kpi_mappings[mapping_key] = {
                    'standard_kpi_name': mapping['standard_kpi_name'],
                    'multiplication_factor': mapping['multiplication_factor'],
                    'oss_id': mapping['oss_id'],
                    'lte_fdd_standard_kpi_id': mapping['lte_fdd_standard_kpi_id']
                }

                # Also create direct mapping for backward compatibility
                self.kpi_mappings[mapping['oss_kpi_name']] = mapping['standard_kpi_name']

            logger.info(f"Loaded {len(mapping_data)} KPI mappings")

            # Load district codes
            logger.info("Loading district codes from database...")
            cursor.execute("""
                SELECT id, category, code, district_id 
                FROM district_codes
                ORDER BY LENGTH(code) DESC
            """)
            district_data = cursor.fetchall()

            for district in district_data:
                self.district_codes[district['code']] = {
                    'id': district['id'],
                    'category': district['category'],
                    'district_id': district['district_id']
                }

            logger.info(f"Loaded {len(self.district_codes)} district codes")

        except Error as e:
            logger.error(f"Error loading database configuration: {e}")
            raise
        finally:
            if connection and connection.is_connected():
                cursor.close()
                connection.close()

    def get_district_code_id(self, cell_name: str) -> Optional[int]:
        """Get district code ID based on cell name prefix"""
        if not cell_name or pd.isna(cell_name):
            return None

        cell_name = str(cell_name).strip()

        # Try to match prefixes (ordered by length descending to match longer prefixes first)
        for code, district_info in self.district_codes.items():
            if cell_name.startswith(code):
                logger.debug(f"Matched cell '{cell_name}' with district code '{code}' (ID: {district_info['id']})")
                return district_info['id']

        logger.debug(f"No district code found for cell: {cell_name}")
        return None

    def create_database_tables(self):
        """Create necessary database tables"""
        connection = None
        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor()

            # Create unpivoted KPI table (updated schema with numerator/denominator and district_code_id columns)
            create_table_query = """
            CREATE TABLE IF NOT EXISTS lte_fdd_kpi_day (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                timestamp DATETIME NOT NULL,
                cell_name VARCHAR(31),
                site_name VARCHAR(63),
                lte_fdd_standard_kpi_id BIGINT,
                kpi_value DECIMAL(15,3),
                data_type ENUM('percentage', 'integer', 'decimal') DEFAULT 'decimal',
                oss_id BIGINT,
                file_name VARCHAR(255),
                numerator_kpi_id BIGINT NULL,
                numerator_kpi_value DECIMAL(15,3) NULL,
                denominator_kpi_id BIGINT NULL,
                denominator_kpi_value DECIMAL(15,3) NULL,
                district_code_id BIGINT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_timestamp (timestamp),
                INDEX idx_cell_name (cell_name),
                INDEX idx_oss_id (oss_id),
                INDEX idx_district_code_id (district_code_id),
                FOREIGN KEY (lte_fdd_standard_kpi_id) REFERENCES lte_fdd_standard_kpi(id),
                FOREIGN KEY (numerator_kpi_id) REFERENCES lte_fdd_standard_kpi(id),
                FOREIGN KEY (denominator_kpi_id) REFERENCES lte_fdd_standard_kpi(id),
                FOREIGN KEY (oss_id) REFERENCES oss(id),
                FOREIGN KEY (district_code_id) REFERENCES district_codes(id)
            )
            """

            cursor.execute(create_table_query)
            connection.commit()
            logger.info("Database tables created successfully")

        except Error as e:
            logger.error(f"Error creating database tables: {e}")
        finally:
            if connection and connection.is_connected():
                cursor.close()
                connection.close()

    def get_new_files(self) -> List[str]:
        """Get list of new files (ZIP, XLSX, CSV) in FTP folder"""
        try:
            new_files = []
            for file in os.listdir(self.ftp_folder):
                if file.endswith(('.zip', '.xlsx', '.csv')):
                    file_path = os.path.join(self.ftp_folder, file)
                    processed_path = os.path.join(self.processed_folder, file)

                    # Check if file has been processed already
                    if not os.path.exists(processed_path):
                        new_files.append(file_path)
            return new_files

        except Exception as e:
            logger.error(f"Error reading files in FTP folder: {e}")
            return []

    def get_file_info(self, file_path: str) -> tuple:
        """Determine file type and if extraction is needed"""
        filename = os.path.basename(file_path)

        if filename.endswith('.zip'):
            return 'zip', True  # needs extraction
        elif filename.endswith('.xlsx'):
            return 'xlsx', False  # direct processing
        elif filename.endswith('.csv'):
            return 'csv', False  # direct processing
        else:
            return 'unknown', False

    def extract_data_file_from_zip(self, zip_path: str) -> Optional[tuple]:
        """Extract XLSX or CSV file from ZIP archive"""
        try:
            with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                # Find XLSX or CSV files in the archive
                data_files = [f for f in zip_ref.namelist() if f.endswith(('.xlsx', '.csv'))]

                if not data_files:
                    logger.warning(f"No XLSX or CSV file found in {zip_path}")
                    return None

                # Extract the first data file (prioritize XLSX over CSV)
                xlsx_files = [f for f in data_files if f.endswith('.xlsx')]
                csv_files = [f for f in data_files if f.endswith('.csv')]

                if xlsx_files:
                    data_file = xlsx_files[0]
                    file_type = 'xlsx'
                elif csv_files:
                    data_file = csv_files[0]
                    file_type = 'csv'
                else:
                    return None

                extract_path = os.path.join(self.temp_folder, data_file)

                with zip_ref.open(data_file) as source, open(extract_path, 'wb') as target:
                    shutil.copyfileobj(source, target)

                logger.info(f"Extracted {data_file} ({file_type}) from {zip_path}")
                return extract_path, file_type

        except Exception as e:
            logger.error(f"Error extracting data file from {zip_path}: {e}")
            return None

    def identify_oss_source(self, filename: str) -> Optional[Tuple[str, dict]]:
        """Identify which OSS generated the file based on filename pattern"""
        for oss_identifier, config in self.oss_configs.items():
            pattern = config['filename_pattern']
            if re.search(pattern, filename, re.IGNORECASE):
                return oss_identifier, config

        logger.warning(f"Could not identify OSS source for file: {filename}")
        return None, None

    def normalize_column_names(self, df: pd.DataFrame) -> pd.DataFrame:
        """Normalize column names"""

        # Strip whitespace from column names
        df.columns = df.columns.str.strip()
        df.columns = df.columns.str.replace('\xa0', ' ')

        return df

    def map_oss_kpi_to_standard(self, oss_kpi_name: str, oss_identifier: str) -> Optional[dict]:
        """Map OSS KPI name to standard KPI using database mapping"""
        # Try with OSS-specific mapping first
        mapping_key = f"{oss_identifier}:{oss_kpi_name}"

        if mapping_key in self.kpi_mappings:
            mapping_info = self.kpi_mappings[mapping_key]
            if isinstance(mapping_info, dict):
                return mapping_info

        # Fallback to direct mapping
        if oss_kpi_name in self.kpi_mappings:
            standard_kpi_name = self.kpi_mappings[oss_kpi_name]
            if isinstance(standard_kpi_name, str):
                # Find the standard KPI info
                if standard_kpi_name in self.standard_kpis:
                    return {
                        'standard_kpi_name': standard_kpi_name,
                        'multiplication_factor': 1.0,
                        'lte_fdd_standard_kpi_id': self.standard_kpis[standard_kpi_name]['id']
                    }

        return None

    def standardize_timestamp_column(self, df: pd.DataFrame) -> pd.DataFrame:
        """Standardize timestamp column name"""
        timestamp_columns = ['Begin Time', 'Start time', 'Start Time', 'Timestamp', 'start_time']

        for col in timestamp_columns:
            if col in df.columns:
                logger.info(f"Found timestamp column: {col}")
                df = df.rename(columns={col: 'timestamp'})
                break
        else:
            logger.warning(f"No timestamp column found. Looking for columns containing 'time'...")
            # Try to find any column containing 'time'
            time_cols = [col for col in df.columns if 'time' in col.lower()]
            if time_cols:
                logger.info(f"Using column: {time_cols[0]} as timestamp")
                df = df.rename(columns={time_cols[0]: 'timestamp'})

        return df

    def standardize_identifier_columns(self, df: pd.DataFrame) -> pd.DataFrame:
        """Standardize cell and eNodeB identifier columns"""
        cell_columns = ['Cell Name', 'Cell_Name', 'CellName', 'cell_name', 'E-UTRAN FDD Cell Name',
                        'E-UTRAN FDD Cell Name', 'E-UTRAN\xa0FDD\xa0Cell Name']
        enodeb_columns = ['eNodeB name', 'eNodeB_name', 'eNodeBName', 'enodeb_name', 'Managed Element',
                          'ManagedElement Name', 'Managed Element', 'Managed\xa0Element']

        for col in cell_columns:
            if col in df.columns:
                df = df.rename(columns={col: 'cell_name'})
                break

        for col in enodeb_columns:
            if col in df.columns:
                df = df.rename(columns={col: 'site_name'})
                break

        return df

    def determine_data_type(self, value, standard_kpi_name: str = None) -> str:
        """Determine the data type of KPI value based on standard KPI configuration"""
        if pd.isna(value):
            return 'decimal'

        # Check standard KPI configuration first
        if standard_kpi_name and standard_kpi_name in self.standard_kpis:
            kpi_info = self.standard_kpis[standard_kpi_name]
            unit = kpi_info.get('unit', '')
            if unit == '%':
                return 'percentage'
            elif unit in ['s', 'ms', 'kByte', 'MB', 'GB']:
                try:
                    clean_val = float(str(value).replace('%', '').replace(',', '').strip())
                    return 'integer' if clean_val.is_integer() else 'decimal'
                except:
                    return 'decimal'

        # Fallback to original logic
        value_str = str(value).strip()
        if '%' in value_str:
            return 'percentage'
        elif '.' in value_str:
            return 'decimal'
        else:
            return 'integer'

    def clean_kpi_value(self, value, multiplication_factor: float = 1.0) -> Optional[float]:
        """Clean and convert KPI value to numeric with multiplication factor"""
        if pd.isna(value):
            return None

        value_str = str(value).strip()

        # Remove percentage sign and commas
        if '%' in value_str:
            value_str = value_str.replace('%', '')
        if ',' in value_str:
            value_str = value_str.replace(',', '')

        try:
            numeric_value = float(value_str)
            # Apply multiplication factor
            numeric_value *= multiplication_factor
            return numeric_value

        except ValueError:
            logger.warning(f"Could not convert value to numeric: {value}")
            return None

    def get_kpi_values_for_standard_kpi(self, df: pd.DataFrame, standard_kpi_name: str, oss_identifier: str) -> Dict:
        """Get the main, numerator, and denominator values for a standard KPI"""
        result = {
            'main_value': None,
            'numerator_value': None,
            'denominator_value': None,
            'numerator_kpi_id': None,
            'denominator_kpi_id': None
        }

        # Check if this standard KPI has numerator/denominator mapping
        if standard_kpi_name in self.standard_kpi_mappings:
            mapping = self.standard_kpi_mappings[standard_kpi_name]
            numerator_kpi_name = mapping.get('numerator_kpi_name')
            denominator_kpi_name = mapping.get('denominator_kpi_name')

            result['numerator_kpi_id'] = mapping.get('numerator_id')
            result['denominator_kpi_id'] = mapping.get('denominator_id')

            # Look for numerator column in dataframe
            if numerator_kpi_name:
                for col in df.columns:
                    # Check if this column maps to the numerator KPI
                    mapped_info = self.map_oss_kpi_to_standard(col, oss_identifier)
                    if mapped_info and mapped_info.get('standard_kpi_name') == numerator_kpi_name:
                        result['numerator_value'] = col
                        break

            # Look for denominator column in dataframe
            if denominator_kpi_name:
                for col in df.columns:
                    # Check if this column maps to the denominator KPI
                    mapped_info = self.map_oss_kpi_to_standard(col, oss_identifier)
                    if mapped_info and mapped_info.get('standard_kpi_name') == denominator_kpi_name:
                        result['denominator_value'] = col
                        break

        return result

    def unpivot_dataframe(self, df: pd.DataFrame, oss_identifier: str, oss_config: dict,
                          file_name: str) -> pd.DataFrame:
        """Convert wide format to unpivoted (long) format using database mappings with numerator/denominator support"""
        # Standardize column names
        df = self.standardize_timestamp_column(df)
        df = self.standardize_identifier_columns(df)

        # Identify identifier columns and KPI columns
        id_columns = ['timestamp', 'cell_name', 'site_name']
        id_columns = [col for col in id_columns if col in df.columns]

        # KPI columns are all remaining columns except identifiers
        kpi_columns = [col for col in df.columns if col not in id_columns]

        if not kpi_columns:
            logger.warning("No KPI columns found in dataframe")
            return pd.DataFrame()

        # Process only standard KPIs (type = 'standard')
        processed_rows = []

        # Group KPI columns by their standard KPI mapping
        standard_kpi_groups = {}

        for col in kpi_columns:
            mapping_info = self.map_oss_kpi_to_standard(col, oss_identifier)
            if mapping_info:
                standard_kpi_name = mapping_info['standard_kpi_name']
                if standard_kpi_name in self.standard_kpis:
                    kpi_info = self.standard_kpis[standard_kpi_name]
                    # Only process standard KPIs (not numerator/denominator)
                    if kpi_info.get('type') == 'standard':
                        if standard_kpi_name not in standard_kpi_groups:
                            standard_kpi_groups[standard_kpi_name] = []
                        standard_kpi_groups[standard_kpi_name].append({
                            'column_name': col,
                            'mapping_info': mapping_info
                        })

        # Process each standard KPI group
        for standard_kpi_name, kpi_group in standard_kpi_groups.items():
            # For each standard KPI, process each occurrence (there should typically be only one)
            for kpi_info in kpi_group:
                col = kpi_info['column_name']
                mapping_info = kpi_info['mapping_info']

                # Get numerator and denominator information
                kpi_values_info = self.get_kpi_values_for_standard_kpi(df, standard_kpi_name, oss_identifier)

                # Process each row in the dataframe
                for _, row in df.iterrows():
                    # Get main KPI value
                    multiplication_factor = mapping_info.get('multiplication_factor', 1.0)
                    cleaned_value = self.clean_kpi_value(row[col], multiplication_factor)

                    if cleaned_value is None:
                        continue

                    # Get district code ID based on cell name
                    district_code_id = self.get_district_code_id(row.get('cell_name'))

                    # Get numerator and denominator values
                    numerator_value = None
                    denominator_value = None

                    if kpi_values_info['numerator_value']:
                        numerator_col = kpi_values_info['numerator_value']
                        if numerator_col in row:
                            # Get multiplication factor for numerator
                            num_mapping_info = self.map_oss_kpi_to_standard(numerator_col, oss_identifier)
                            num_mult_factor = num_mapping_info.get('multiplication_factor', 1.0) if num_mapping_info else 1.0
                            numerator_value = self.clean_kpi_value(row[numerator_col], num_mult_factor)

                    if kpi_values_info['denominator_value']:
                        denominator_col = kpi_values_info['denominator_value']
                        if denominator_col in row:
                            # Get multiplication factor for denominator
                            denom_mapping_info = self.map_oss_kpi_to_standard(denominator_col, oss_identifier)
                            denom_mult_factor = denom_mapping_info.get('multiplication_factor', 1.0) if denom_mapping_info else 1.0
                            denominator_value = self.clean_kpi_value(row[denominator_col], denom_mult_factor)

                    # Determine data type
                    data_type = self.determine_data_type(row[col], standard_kpi_name)

                    processed_row = {
                        'timestamp': row.get('timestamp'),
                        'cell_name': row.get('cell_name'),
                        'site_name': row.get('site_name'),
                        'lte_fdd_standard_kpi_id': mapping_info.get('lte_fdd_standard_kpi_id'),
                        'standard_kpi_name': standard_kpi_name,
                        'kpi_value': cleaned_value,
                        'data_type': data_type,
                        'oss_id': oss_config['id'],
                        'oss_kpi_name': col,
                        'file_name': file_name,
                        'numerator_kpi_id': kpi_values_info['numerator_kpi_id'] if kpi_values_info['numerator_kpi_id'] else None,
                        'numerator_kpi_value': numerator_value,
                        'denominator_kpi_id': kpi_values_info['denominator_kpi_id'] if kpi_values_info['denominator_kpi_id'] else None,
                        'denominator_kpi_value': denominator_value,
                        'district_code_id': district_code_id  # Add district code ID
                    }
                    processed_rows.append(processed_row)

        if not processed_rows:
            logger.warning("No valid KPI mappings found after processing")
            return pd.DataFrame()

        # Create final dataframe
        final_df = pd.DataFrame(processed_rows)

        # Ensure timestamp is datetime
        if 'timestamp' in final_df.columns:
            final_df['timestamp'] = pd.to_datetime(final_df['timestamp'], errors='coerce')

        # Log statistics
        unique_kpis = final_df['standard_kpi_name'].unique()
        unique_districts = final_df['district_code_id'].value_counts()
        logger.info(f"Processed {len(unique_kpis)} unique standard KPIs: {list(unique_kpis)}")
        logger.info(f"District code distribution: {dict(unique_districts)}")

        return final_df

    def convert_nan_to_none(self, value):
        """Convert pandas NaN values to None for proper MySQL NULL insertion"""
        if pd.isna(value) or value == '' or str(value).strip().lower() == 'nan':
            return None
        return value

    def insert_data_to_mysql(self, df: pd.DataFrame):
        """Insert unpivoted data into MySQL database with numerator/denominator and district code support"""
        if df.empty:
            logger.warning("No data to insert")
            return

        try:
            connection = mysql.connector.connect(**self.db_config)
            cursor = connection.cursor()

            # Prepare insert query with numerator/denominator and district_code_id columns
            insert_query = """
            INSERT INTO lte_fdd_kpi_day 
            (timestamp, cell_name, site_name, lte_fdd_standard_kpi_id, 
             kpi_value, data_type, oss_id, file_name, 
             numerator_kpi_id, numerator_kpi_value, 
             denominator_kpi_id, denominator_kpi_value, district_code_id)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """

            # Convert dataframe to list of tuples with proper None handling
            data_tuples = []
            for _, row in df.iterrows():
                data_tuples.append((
                    row.get('timestamp'),
                    row.get('cell_name'),
                    row.get('site_name'),
                    row.get('lte_fdd_standard_kpi_id'),
                    row.get('kpi_value'),
                    row.get('data_type'),
                    row.get('oss_id'),
                    row.get('file_name'),
                    self.convert_nan_to_none(row.get('numerator_kpi_id')),
                    self.convert_nan_to_none(row.get('numerator_kpi_value')),
                    self.convert_nan_to_none(row.get('denominator_kpi_id')),
                    self.convert_nan_to_none(row.get('denominator_kpi_value')),
                    self.convert_nan_to_none(row.get('district_code_id'))  # Add district code ID
                ))

            # Execute batch insert
            cursor.executemany(insert_query, data_tuples)
            connection.commit()

            logger.info(f"Inserted {len(data_tuples)} records into database")

        except Error as e:
            logger.error(f"Error inserting data to MySQL: {e}")
            # Log the problematic data for debugging
            if data_tuples:
                logger.error(f"Sample data tuple: {data_tuples[0]}")
        finally:
            if connection and connection.is_connected():
                cursor.close()
                connection.close()

    def read_data_file(self, file_path: str, file_type: str, oss_config: dict) -> pd.DataFrame:
        """Read data from XLSX or CSV file"""
        try:
            if file_type == 'xlsx':
                sheet_name = oss_config.get('sheet_name', 0)
                skip_rows = oss_config.get('skip_rows', 0)
                df = pd.read_excel(file_path, sheet_name=sheet_name, skiprows=skip_rows)

            elif file_type == 'csv':
                encoding = oss_config.get('encoding', 'utf-8-sig')
                delimiter = oss_config.get('delimiter', ',')
                skip_rows = oss_config.get('skip_rows', 0)

                df = pd.read_csv(
                    file_path,
                    encoding=encoding,
                    delimiter=delimiter,
                    skiprows=skip_rows
                )
            else:
                logger.error(f"Unsupported file type: {file_type}")
                return pd.DataFrame()

            logger.info(f"Successfully read {len(df)} rows and {len(df.columns)} columns from {file_type} file")
            return df

        except Exception as e:
            logger.error(f"Error reading {file_type} file {file_path}: {e}")
            return pd.DataFrame()

    def process_data_file(self, file_path: str, file_type: str, oss_identifier: str, oss_config: dict,
                          original_filename: str):
        """Process a single data file (XLSX or CSV)"""
        try:
            logger.info(f"Reading {file_type.upper()} file: {file_path}")
            logger.info(f"OSS identifier: {oss_identifier}")

            # Read the data file
            df = self.read_data_file(file_path, file_type, oss_config)

            if df.empty:
                logger.warning(f"No data read from {file_path}")
                return

            logger.info(f"Read {len(df)} rows and {len(df.columns)} columns from {file_path}")
            logger.info(f"Column names: {list(df.columns)}")

            # Normalize column names
            df = self.normalize_column_names(df)

            # Convert to unpivoted format using database mappings with numerator/denominator support
            unpivoted_df = self.unpivot_dataframe(df, oss_identifier, oss_config, original_filename)

            if unpivoted_df.empty:
                logger.warning("No data to insert after unpivoting and mapping")
                return

            # Insert into database
            self.insert_data_to_mysql(unpivoted_df)

            logger.info(f"Successfully processed {file_path}")

        except Exception as e:
            logger.error(f"Error processing data file {file_path}: {e}")
            import traceback
            logger.error(f"Traceback: {traceback.format_exc()}")

    def mark_file_as_processed(self, file_path: str):
        """Move processed file to processed folder"""
        try:
            filename = os.path.basename(file_path)
            processed_path = os.path.join(self.processed_folder, filename)
            shutil.move(file_path, processed_path)
            logger.info(f"Moved {filename} to processed folder")
        except Exception as e:
            logger.error(f"Error moving file to processed folder: {e}")

    def cleanup_temp_files(self):
        """Clean up temporary files"""
        try:
            for file in os.listdir(self.temp_folder):
                file_path = os.path.join(self.temp_folder, file)
                os.remove(file_path)
        except Exception as e:
            logger.error(f"Error cleaning up temp files: {e}")

    def process_new_files(self):
        """Process new files using database configuration"""
        new_files = self.get_new_files()

        for file_path in new_files:
            try:
                filename = os.path.basename(file_path)
                logger.info(f"Processing {filename}")

                # Identify OSS source using database configuration
                oss_identifier, oss_config = self.identify_oss_source(filename)

                if not oss_identifier:
                    logger.warning(f"Could not identify OSS for file: {filename}")
                    continue

                # Determine file type and processing method
                file_format, needs_extraction = self.get_file_info(file_path)

                if needs_extraction and file_format == 'zip':
                    logger.info("File needs extraction")
                    # Extract data file from ZIP
                    extraction_result = self.extract_data_file_from_zip(file_path)

                    if extraction_result:
                        data_file_path, file_type = extraction_result
                        # Process the extracted data file
                        self.process_data_file(data_file_path, file_type, oss_identifier, oss_config, filename)

                elif not needs_extraction and file_format in ['xlsx', 'csv']:
                    # Process file directly
                    logger.info("File does not need extraction")
                    self.process_data_file(file_path, file_format, oss_identifier, oss_config, filename)

                else:
                    logger.warning(f"Unsupported file format: {file_format}")
                    continue

                # Mark file as processed
                self.mark_file_as_processed(file_path)

                # Clean up temp files
                self.cleanup_temp_files()

            except Exception as e:
                logger.error(f"Error processing file {filename}: {e}")
                import traceback
                logger.error(f"Traceback: {traceback.format_exc()}")

    def run_continuously(self, interval_minutes: int = 60):
        """Run the processor continuously with specified interval"""
        logger.info(f"Starting continuous processing with {interval_minutes} minute intervals")

        # Create database tables on startup
        self.create_database_tables()

        while True:
            try:
                self.process_new_files()
                logger.info(f"Sleeping for {interval_minutes} minutes...")
                time.sleep(interval_minutes * 60)
            except KeyboardInterrupt:
                logger.info("Processing stopped by user")
                break
            except Exception as e:
                logger.error(f"Unexpected error in continuous processing: {e}")
                time.sleep(60)  # Wait 1 minute before retrying


if __name__ == "__main__":
    # Initialize and run the processor
    processor = KPIProcessor()

    # Run continuously (every 60 minutes)
    processor.run_continuously(interval_minutes=60)

    # Or run once
    # processor.process_new_files()