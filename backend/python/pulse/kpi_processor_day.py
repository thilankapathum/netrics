import os
import sys
from collections import defaultdict

import zipfile
import pandas as pd
import psycopg2
from psycopg2 import Error
from psycopg2.extras import RealDictCursor
from psycopg2.extras import execute_values
import logging
import time
import shutil
from datetime import datetime
import re
from typing import Dict, List, Optional, Tuple
import json
import requests
from datetime import timedelta

sys.stdout.reconfigure(encoding='utf-8', line_buffering=True)
sys.stderr.reconfigure(encoding='utf-8', line_buffering=True)

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('kpi_processor.log', encoding='utf-8'),
    ]
)

console_handler = logging.StreamHandler(sys.stderr)
console_handler.setLevel(logging.INFO)
console_handler.setFormatter(logging.Formatter('%(asctime)s - %(levelname)s - %(message)s'))
logging.getLogger().addHandler(console_handler)

logger = logging.getLogger(__name__)


class KPIProcessor:
    def __init__(self, config_file='config.json'):
        """Initialize KPI Processor with configuration"""
        self.config = self.load_config(config_file)

        self.db_config = {
            "host": os.getenv("POSTGRES_HOST", self.config["database"].get("host")),
            "port": int(os.getenv("POSTGRES_PORT", self.config["database"].get("port", 5432))),
            "database": os.getenv("POSTGRES_DB_PULSE", self.config["database"].get("database")),
            "user": os.getenv("POSTGRES_USER", self.config["database"].get("user")),
            "password": os.getenv("POSTGRES_PASSWORD", self.config["database"].get("password")),
        }

        self.clear_redis_cache_url = self.config['clear_redis_cache_url']

        # Support both old and new config format
        if 'base_folders' in self.config:
            self.base_folders = self.config['base_folders']
        else:
            # Backward compatibility: convert old base_folder to new format
            self.base_folders = {
                'day-average': self.config.get('base_folder', '/app/day-average'),
                'busy-hour': '/app/busy-hour'
            }

        # Database-driven configuration
        self.rats = {}
        self.standard_kpis = {}
        self.kpi_mappings = {}
        self.oss_configs = {}
        self.standard_kpi_mappings = {}
        self.district_codes = {}
        self.granularities = {}

        # Load configuration from database
        self.load_database_configuration()

        # Create necessary directories for all RATs and granularities
        self.create_rat_directories()

    def load_config(self, config_file: str) -> Dict:
        """Load basic configuration from JSON file"""
        try:
            with open(config_file, 'r') as f:
                config = json.load(f)
                return config
        except FileNotFoundError:
            logger.error(f"Config file {config_file} not found. Creating default config.")
            return self.create_default_config(config_file)

    def create_default_config(self, config_file: str) -> Dict:
        """Create a default configuration file"""
        default_config = {
            "database": {
                "host": "postgres",
                "port": 5432,
                "database": "pulse_db",
                "user": "pguser",
                "password": "password"
            },
            "base_folders": {
                "day-average": "/app/day-average",
                "busy-hour": "/app/busy-hour"
            },
            "clear_redis_cache_url": "http://localhost:8012/api/v1/pulse/cache/evict-and-warmup"
        }

        with open(config_file, 'w') as f:
            json.dump(default_config, f, indent=4)

        logger.info(f"Created default config file: {config_file}")
        return default_config

    def create_rat_directories(self):
        """Create necessary directories for all RATs and granularities"""
        logger.info(f"Creating directories for RATs...")
        for rat_id, rat_info in self.rats.items():
            for granularity_name, folders in rat_info['folders'].items():
                for folder_type in ['ftp', 'processed', 'temp', 'log']:
                    folder_path = folders[folder_type]
                    os.makedirs(folder_path, exist_ok=True)
                    # logger.info(f"Created directory: {folder_path}")
        logger.info(f"Created directories for RATs")

    def clear_redis_cache(self, rat_name: str, granularity_name:str):
        """Clear redis cache for specific RAT"""
        # logger.info(f"Clearing redis cache for RAT: {rat_name}...")
        try:
            url = f"{self.clear_redis_cache_url}?ratName={rat_name}&granularityName={granularity_name}"
            logger.info(f"Calling cache eviction URL: {url}")
            response = requests.post(url, timeout=10)
            if response.status_code == 200:
                logger.info(f"Redis cache cleared successfully for RAT: {rat_name}\n")
            else:
                logger.error(
                    f"Failed to clear redis cache for RAT {rat_name}: {response.status_code}, body: {response.text}\n")
        except Exception as e:
            logger.error(f"Failed to clear redis cache for RAT {rat_name}: {e}\n")

    def load_database_configuration(self):
        """Load configuration from database tables"""
        connection = None
        try:
            connection = psycopg2.connect(**self.db_config)
            cursor = connection.cursor(cursor_factory=RealDictCursor)

            # Load granularities
            logger.info("Loading granularities from database...")
            cursor.execute("SELECT id, name, label FROM granularity ORDER BY id")
            granularity_data = cursor.fetchall()

            for granularity in granularity_data:
                self.granularities[granularity['id']] = {
                    'name': granularity['name'],
                    'label': granularity['label']
                }

            logger.info(
                f"Loaded {len(self.granularities)} granularities: {[g['name'] for g in self.granularities.values()]}")

            # Load RATs
            logger.info("Loading RATs from database...")
            cursor.execute("SELECT id, name, label FROM rat ORDER BY id")
            rats_data = cursor.fetchall()

            for rat in rats_data:
                rat_id = rat['id']
                rat_name = rat['name']

                # Create folder structure for each granularity
                folders_by_granularity = {}
                for granularity_id, granularity_info in self.granularities.items():
                    granularity_name = granularity_info['name']
                    base_folder = self.base_folders.get(granularity_name, f'/app/{granularity_name}')

                    folders_by_granularity[granularity_name] = {
                        'ftp': os.path.join(base_folder, rat_name, 'ftp'),
                        'processed': os.path.join(base_folder, rat_name, 'processed'),
                        'temp': os.path.join(base_folder, rat_name, 'temp'),
                        'log': os.path.join(base_folder, rat_name, 'log')
                    }

                self.rats[rat_id] = {
                    'name': rat_name,
                    'label': rat['label'],
                    'folders': folders_by_granularity
                }

            logger.info(f"Loaded {len(self.rats)} RATs: {[r['name'] for r in self.rats.values()]}")

            # Load standard KPIs
            cursor.execute("SELECT id, kpi_name, unit, type, worst_order, threshold, rat_id FROM standard_kpi")
            for kpi in cursor.fetchall():
                rat_id = kpi['rat_id']
                if rat_id not in self.standard_kpis:
                    self.standard_kpis[rat_id] = {}
                self.standard_kpis[rat_id][kpi['kpi_name']] = {
                    'id': kpi['id'], 'unit': kpi['unit'], 'type': kpi['type'],
                    'worst_order': kpi['worst_order'], 'threshold': kpi['threshold']
                }

            # Load standard KPI mappings
            cursor.execute("""
                SELECT m.id, m.standard_kpi_id, m.numerator_id, m.denominator_id, m.rat_id,
                       s.kpi_name as standard_kpi_name, n.kpi_name as numerator_kpi_name,
                       d.kpi_name as denominator_kpi_name
                FROM standard_raw_kpi_mapping m
                JOIN standard_kpi s ON m.standard_kpi_id = s.id
                LEFT JOIN standard_kpi n ON m.numerator_id = n.id
                LEFT JOIN standard_kpi d ON m.denominator_id = d.id
            """)
            for mapping in cursor.fetchall():
                rat_id = mapping['rat_id']
                if rat_id not in self.standard_kpi_mappings:
                    self.standard_kpi_mappings[rat_id] = {}
                self.standard_kpi_mappings[rat_id][mapping['standard_kpi_name']] = {
                    'standard_kpi_id': mapping['standard_kpi_id'],
                    'numerator_id': mapping['numerator_id'],
                    'denominator_id': mapping['denominator_id'],
                    'numerator_kpi_name': mapping['numerator_kpi_name'],
                    'denominator_kpi_name': mapping['denominator_kpi_name']
                }

            # Load OSS configs
            cursor.execute("SELECT id, oss_name, identifier, vendor FROM oss")
            for oss in cursor.fetchall():
                self.oss_configs[oss['identifier']] = {
                    'id': oss['id'], 'oss_name': oss['oss_name'], 'vendor': oss['vendor'],
                    'filename_pattern': rf"{oss['identifier']}.*\.(zip|xlsx|csv)$",
                    'sheet_name': 0, 'skip_rows': 0, 'encoding': 'utf-8', 'delimiter': ","
                }

            # Load KPI mappings
            cursor.execute("""
                SELECT m.id, m.standard_kpi_id, m.oss_kpi_name, m.oss_id,
                       m.multiplication_factor, m.rat_id, s.kpi_name as standard_kpi_name,
                       o.identifier as oss_identifier
                FROM kpi_mapping m
                JOIN standard_kpi s ON m.standard_kpi_id = s.id
                JOIN oss o ON m.oss_id = o.id
            """)
            for mapping in cursor.fetchall():
                rat_id = mapping['rat_id']
                if rat_id not in self.kpi_mappings:
                    self.kpi_mappings[rat_id] = {}
                mapping_key = f"{mapping['oss_identifier']}:{mapping['oss_kpi_name']}"
                self.kpi_mappings[rat_id][mapping_key] = {
                    'standard_kpi_name': mapping['standard_kpi_name'],
                    'multiplication_factor': mapping['multiplication_factor'],
                    'oss_id': mapping['oss_id'],
                    'standard_kpi_id': mapping['standard_kpi_id']
                }
                self.kpi_mappings[rat_id][mapping['oss_kpi_name']] = mapping['standard_kpi_name']

            # Load district codes
            cursor.execute("SELECT id, category, code, district_id FROM district_codes ORDER BY LENGTH(code) DESC")
            for district in cursor.fetchall():
                self.district_codes[district['code']] = {
                    'id': district['id'], 'category': district['category'],
                    'district_id': district['district_id']
                }

        except Error as e:
            logger.error(f"Error loading database configuration: {e}")
            raise
        finally:
            if connection:
                cursor.close()
                connection.close()

    def get_granularity_id_by_name(self, granularity_name: str) -> Optional[int]:
        """Get granularity ID by name"""
        for granularity_id, granularity_info in self.granularities.items():
            if granularity_info['name'] == granularity_name:
                return granularity_id
        return None

    def get_district_code_id(self, cell_name: str) -> Optional[int]:
        """Get district code ID based on cell name prefix"""
        if not cell_name or pd.isna(cell_name):
            return None
        cell_name = str(cell_name).strip()
        for code, district_info in self.district_codes.items():
            if cell_name.startswith(code):
                return district_info['id']
        return None

    def create_database_tables(self):
        """Create necessary database tables"""
        connection = None
        try:
            connection = psycopg2.connect(**self.db_config)
            cursor = connection.cursor()

            create_table_query = """
            CREATE TABLE IF NOT EXISTS kpi_values (
                id BIGSERIAL PRIMARY KEY,
                timestamp TIMESTAMP NOT NULL,
                cell_name VARCHAR(31),
                site_name VARCHAR(63),
                standard_kpi_id BIGINT,
                kpi_value DECIMAL(15,3),
                data_type VARCHAR(20) CHECK (data_type IN ('percentage', 'integer', 'decimal')) DEFAULT 'decimal',
                oss_id BIGINT,
                file_name VARCHAR(255),
                numerator_kpi_id BIGINT NULL,
                numerator_kpi_value DECIMAL(15,3) NULL,
                denominator_kpi_id BIGINT NULL,
                denominator_kpi_value DECIMAL(15,3) NULL,
                district_code_id BIGINT NULL,
                rat_id BIGINT,
                granularity_id BIGINT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """
            cursor.execute(create_table_query)

            indexes = [
                "CREATE INDEX IF NOT EXISTS idx_kpi_values_timestamp ON kpi_values(timestamp)",
                "CREATE INDEX IF NOT EXISTS idx_kpi_values_cell_name ON kpi_values(cell_name)",
                "CREATE INDEX IF NOT EXISTS idx_kpi_values_oss_id ON kpi_values(oss_id)",
                "CREATE INDEX IF NOT EXISTS idx_kpi_values_district_code_id ON kpi_values(district_code_id)",
                "CREATE INDEX IF NOT EXISTS idx_kpi_values_rat_id ON kpi_values(rat_id)",
                "CREATE INDEX IF NOT EXISTS idx_kpi_values_granularity_id ON kpi_values(granularity_id)"
            ]

            for index_query in indexes:
                cursor.execute(index_query)

            connection.commit()
            logger.info("Database tables created successfully \n")

        except Error as e:
            logger.error(f"Error creating database tables: {e}")
        finally:
            if connection:
                cursor.close()
                connection.close()

    def get_new_files(self, rat_id: int, granularity_name: str) -> List[str]:
        """Get list of new files in RAT's FTP folder for specific granularity"""
        try:
            rat_info = self.rats[rat_id]
            ftp_folder = rat_info['folders'][granularity_name]['ftp']
            processed_folder = rat_info['folders'][granularity_name]['processed']

            new_files = []
            if not os.path.exists(ftp_folder):
                return new_files

            for file in os.listdir(ftp_folder):
                if file.endswith(('.zip', '.xlsx', '.csv')):
                    file_path = os.path.join(ftp_folder, file)
                    processed_path = os.path.join(processed_folder, file)
                    if not os.path.exists(processed_path):
                        new_files.append(file_path)
            return new_files

        except Exception as e:
            logger.error(f"Error reading files for RAT {rat_id}, granularity {granularity_name}: {e}")
            return []

    def get_file_info(self, file_path: str) -> tuple:
        """Determine file type and if extraction is needed"""
        filename = os.path.basename(file_path)
        if filename.endswith('.zip'):
            return 'zip', True
        elif filename.endswith('.xlsx'):
            return 'xlsx', False
        elif filename.endswith('.csv'):
            return 'csv', False
        else:
            return 'unknown', False

    def extract_data_file_from_zip(self, zip_path: str, rat_id: int, granularity_name: str) -> Optional[tuple]:
        """Extract XLSX or CSV file from ZIP archive"""
        try:
            rat_info = self.rats[rat_id]
            temp_folder = rat_info['folders'][granularity_name]['temp']

            with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                data_files = [f for f in zip_ref.namelist() if f.endswith(('.xlsx', '.csv'))]
                if not data_files:
                    return None

                xlsx_files = [f for f in data_files if f.endswith('.xlsx')]
                csv_files = [f for f in data_files if f.endswith('.csv')]

                if xlsx_files:
                    data_file, file_type = xlsx_files[0], 'xlsx'
                elif csv_files:
                    data_file, file_type = csv_files[0], 'csv'
                else:
                    return None

                extract_path = os.path.join(temp_folder, data_file)
                with zip_ref.open(data_file) as source, open(extract_path, 'wb') as target:
                    shutil.copyfileobj(source, target)

                logger.info(f"Extracted {data_file} ({file_type}) from {zip_path}")
                return extract_path, file_type

        except Exception as e:
            logger.error(f"Error extracting from {zip_path}: {e}")
            return None

    def identify_oss_source(self, filename: str) -> Optional[Tuple[str, dict]]:
        """Identify OSS source from filename"""
        for oss_identifier, config in self.oss_configs.items():
            if re.search(config['filename_pattern'], filename, re.IGNORECASE):
                return oss_identifier, config
        return None, None

    def normalize_column_names(self, df: pd.DataFrame) -> pd.DataFrame:
        """Normalize column names"""
        df.columns = df.columns.str.strip().str.replace('\xa0', ' ')
        return df

    def map_oss_kpi_to_standard(self, oss_kpi_name: str, oss_identifier: str, rat_id: int) -> Optional[dict]:
        """Map OSS KPI to standard KPI"""
        if rat_id not in self.kpi_mappings:
            return None

        mapping_key = f"{oss_identifier}:{oss_kpi_name}"
        if mapping_key in self.kpi_mappings[rat_id]:
            mapping_info = self.kpi_mappings[rat_id][mapping_key]
            if isinstance(mapping_info, dict):
                return mapping_info

        if oss_kpi_name in self.kpi_mappings[rat_id]:
            standard_kpi_name = self.kpi_mappings[rat_id][oss_kpi_name]
            if isinstance(standard_kpi_name, str) and rat_id in self.standard_kpis:
                if standard_kpi_name in self.standard_kpis[rat_id]:
                    return {
                        'standard_kpi_name': standard_kpi_name,
                        'multiplication_factor': 1.0,
                        'standard_kpi_id': self.standard_kpis[rat_id][standard_kpi_name]['id']
                    }
        return None

    def standardize_timestamp_column(self, df: pd.DataFrame) -> pd.DataFrame:
        """Standardize timestamp column"""
        timestamp_columns = ['Begin Time', 'Start time', 'Start Time', 'Timestamp', 'start_time', 'Date']
        for col in timestamp_columns:
            if col in df.columns:
                return df.rename(columns={col: 'timestamp'})

        time_cols = [col for col in df.columns if 'time' in col.lower()]
        if time_cols:
            return df.rename(columns={time_cols[0]: 'timestamp'})
        return df

    def standardize_identifier_columns(self, df: pd.DataFrame) -> pd.DataFrame:
        """Standardize cell and site identifier columns"""
        cell_columns = ['Cell Name', 'Cell_Name', 'CellName', 'cell_name', 'E-UTRAN FDD Cell Name',
                        'E-UTRAN\xa0FDD\xa0Cell Name', 'BTS NAME', 'CU cell configuration Name']
        enodeb_columns = ['eNodeB name', 'eNodeB_name', 'eNodeBName', 'enodeb_name', 'Managed Element',
                          'ManagedElement Name', 'Managed\xa0Element', 'SITE Name', 'Site Name', 'eNodeB Name']

        for col in cell_columns:
            if col in df.columns:
                df = df.rename(columns={col: 'cell_name'})
                break

        for col in enodeb_columns:
            if col in df.columns:
                df = df.rename(columns={col: 'site_name'})
                break

        return df

    def determine_data_type(self, value, standard_kpi_name: str = None, rat_id: int = None) -> str:
        """Determine KPI data type"""
        if pd.isna(value):
            return 'decimal'

        if standard_kpi_name and rat_id and rat_id in self.standard_kpis:
            if standard_kpi_name in self.standard_kpis[rat_id]:
                unit = self.standard_kpis[rat_id][standard_kpi_name].get('unit', '')
                if unit == '%':
                    return 'percentage'

        value_str = str(value).strip()
        if '%' in value_str:
            return 'percentage'
        elif '.' in value_str:
            return 'decimal'
        else:
            return 'integer'

    def clean_kpi_value(self, value, multiplication_factor: float = 1.0,
                        show_progress: bool = False, error_counter: dict = None) -> Optional[float]:
        """Clean and convert KPI value"""
        if pd.isna(value):
            return None

        value_str = str(value).strip().replace('%', '').replace(',', '')

        try:
            return float(value_str) * multiplication_factor
        except ValueError:
            if error_counter is not None:
                error_counter[str(value)] += 1
            return None

    def get_kpi_values_for_standard_kpi(self, df: pd.DataFrame, standard_kpi_name: str,
                                        oss_identifier: str, rat_id: int) -> Dict:
        """Get numerator and denominator values for standard KPI"""
        result = {
            'numerator_value': None, 'denominator_value': None,
            'numerator_kpi_id': None, 'denominator_kpi_id': None
        }

        if rat_id in self.standard_kpi_mappings and standard_kpi_name in self.standard_kpi_mappings[rat_id]:
            mapping = self.standard_kpi_mappings[rat_id][standard_kpi_name]
            result['numerator_kpi_id'] = mapping.get('numerator_id')
            result['denominator_kpi_id'] = mapping.get('denominator_id')

            for col in df.columns:
                mapped_info = self.map_oss_kpi_to_standard(col, oss_identifier, rat_id)
                if mapped_info:
                    if mapped_info.get('standard_kpi_name') == mapping.get('numerator_kpi_name'):
                        result['numerator_value'] = col
                    elif mapped_info.get('standard_kpi_name') == mapping.get('denominator_kpi_name'):
                        result['denominator_value'] = col

        return result

    def unpivot_dataframe(self, df: pd.DataFrame, oss_identifier: str, oss_config: dict,
                          file_name: str, rat_id: int, granularity_id: int) -> pd.DataFrame:
        """Convert to unpivoted format with granularity"""
        df = self.standardize_timestamp_column(df)
        df = self.standardize_identifier_columns(df)

        id_columns = [col for col in ['timestamp', 'cell_name', 'site_name'] if col in df.columns]
        kpi_columns = [col for col in df.columns if col not in id_columns]

        if not kpi_columns:
            return pd.DataFrame()

        conversion_errors = defaultdict(int)
        processed_rows = []
        standard_kpi_groups = {}

        for col in kpi_columns:
            mapping_info = self.map_oss_kpi_to_standard(col, oss_identifier, rat_id)
            if mapping_info:
                standard_kpi_name = mapping_info['standard_kpi_name']
                if rat_id in self.standard_kpis and standard_kpi_name in self.standard_kpis[rat_id]:
                    if self.standard_kpis[rat_id][standard_kpi_name].get('type') == 'standard':
                        if standard_kpi_name not in standard_kpi_groups:
                            standard_kpi_groups[standard_kpi_name] = []
                        standard_kpi_groups[standard_kpi_name].append(
                            {'column_name': col, 'mapping_info': mapping_info})

        for standard_kpi_name, kpi_group in standard_kpi_groups.items():
            for kpi_info in kpi_group:
                col = kpi_info['column_name']
                mapping_info = kpi_info['mapping_info']
                kpi_values_info = self.get_kpi_values_for_standard_kpi(df, standard_kpi_name, oss_identifier, rat_id)

                for _, row in df.iterrows():
                    multiplication_factor = mapping_info.get('multiplication_factor', 1.0)
                    cleaned_value = self.clean_kpi_value(row[col], multiplication_factor, True, conversion_errors)

                    if cleaned_value is None:
                        continue

                    district_code_id = self.get_district_code_id(row.get('cell_name'))

                    numerator_value = None
                    denominator_value = None

                    if kpi_values_info['numerator_value'] and kpi_values_info['numerator_value'] in row:
                        num_mapping = self.map_oss_kpi_to_standard(kpi_values_info['numerator_value'], oss_identifier,
                                                                   rat_id)
                        num_mult = num_mapping.get('multiplication_factor', 1.0) if num_mapping else 1.0
                        numerator_value = self.clean_kpi_value(row[kpi_values_info['numerator_value']], num_mult, True,
                                                               conversion_errors)

                    if kpi_values_info['denominator_value'] and kpi_values_info['denominator_value'] in row:
                        denom_mapping = self.map_oss_kpi_to_standard(kpi_values_info['denominator_value'],
                                                                     oss_identifier, rat_id)
                        denom_mult = denom_mapping.get('multiplication_factor', 1.0) if denom_mapping else 1.0
                        denominator_value = self.clean_kpi_value(row[kpi_values_info['denominator_value']], denom_mult,
                                                                 True, conversion_errors)

                    processed_rows.append({
                        'timestamp': row.get('timestamp'),
                        'cell_name': row.get('cell_name'),
                        'site_name': row.get('site_name'),
                        'standard_kpi_id': mapping_info.get('standard_kpi_id'),
                        'standard_kpi_name': standard_kpi_name,
                        'kpi_value': cleaned_value,
                        'data_type': self.determine_data_type(row[col], standard_kpi_name, rat_id),
                        'oss_id': oss_config['id'],
                        'oss_kpi_name': col,
                        'file_name': file_name,
                        'numerator_kpi_id': kpi_values_info['numerator_kpi_id'],
                        'numerator_kpi_value': numerator_value,
                        'denominator_kpi_id': kpi_values_info['denominator_kpi_id'],
                        'denominator_kpi_value': denominator_value,
                        'district_code_id': district_code_id,
                        'rat_id': rat_id,
                        'granularity_id': granularity_id
                    })

        if conversion_errors:
            rat_name = self.rats[rat_id]['name']
            for value, count in sorted(conversion_errors.items(), key=lambda x: x[1], reverse=True):
                logger.warning(f"[{rat_name}] Could not convert value: '{value}' - {count} occurrences")

        if not processed_rows:
            return pd.DataFrame()

        final_df = pd.DataFrame(processed_rows)
        if 'timestamp' in final_df.columns:
            final_df['timestamp'] = pd.to_datetime(final_df['timestamp'], errors='coerce')

        return final_df

    def convert_nan_to_none(self, value):
        """Convert NaN to None for PostgreSQL"""
        if pd.isna(value) or value == '' or str(value).strip().lower() == 'nan':
            return None
        return value

    def create_error_log_file(self, rat_id: int, granularity_name: str, original_filename: str, error_records: List[Dict]) -> str:
        """Create an error log file for failed record insertions"""
        rat_info = self.rats[rat_id]
        # log_folder = rat_info['folders']['log']
        log_folder = rat_info['folders'][granularity_name]['log']

        # Create log filename based on original file
        base_filename = os.path.splitext(os.path.basename(original_filename))[0]
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        error_log_filename = f"{base_filename}_errors_{timestamp}.log"
        error_log_path = os.path.join(log_folder, error_log_filename)

        # Write error log
        with open(error_log_path, 'w', encoding='utf-8') as f:
            f.write(f"Error Log for File: {original_filename}\n")
            f.write(f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write(f"Total Errors: {len(error_records)}\n")
            f.write("=" * 80 + "\n\n")

            for idx, error in enumerate(error_records, 1):
                f.write(f"Error #{idx}\n")
                f.write(f"Row Index: {error['row_index']}\n")
                f.write(f"Error Type: {error['error_type']}\n")
                f.write(f"Error Message: {error['error_message']}\n")
                f.write(f"Row Data:\n")
                for key, value in error['row_data'].items():
                    f.write(f"  {key}: {value}\n")
                f.write("-" * 80 + "\n\n")

        return error_log_path


    def insert_data_to_postgresql(
            self,
            df: pd.DataFrame,
            rat_id: int,
            original_filename: str,
            granularity_name: str
    ) -> bool:
        """
        High-performance batch insert into TimescaleDB using execute_values.
        Tracks skipped rows (duplicates) using ON CONFLICT DO NOTHING.

        - Batch inserts (fast)
        - Skipped rows counted
        - Per-row error isolation on actual errors
        - Progress + ETA logging
        """

        if df.empty:
            logger.warning("No data to insert")
            return True

        connection = None
        cursor = None
        error_records = []
        successful_inserts = 0
        skipped_records = 0
        total_records = len(df)

        start_time = time.time()
        granularity_id = self.get_granularity_id_by_name(granularity_name)
        rat_name = self.rats[rat_id]["name"]

        # 🔧 Tuning parameters
        BATCH_SIZE = 5000
        PROGRESS_INTERVAL = 5000

        insert_query = """
            INSERT INTO kpi_values (
                timestamp,
                cell_name,
                site_name,
                standard_kpi_id,
                kpi_value,
                data_type,
                oss_id,
                file_name,
                numerator_kpi_id,
                numerator_kpi_value,
                denominator_kpi_id,
                denominator_kpi_value,
                district_code_id,
                rat_id,
                granularity_id
            )
            VALUES %s
            ON CONFLICT (
                timestamp,
                cell_name,
                standard_kpi_id,
                oss_id,
                rat_id,
                granularity_id
            )
            DO NOTHING;
        """

        try:
            logger.info(f"[{granularity_name} - {rat_name}] Preparing data insert...")
            connection = psycopg2.connect(**self.db_config)
            cursor = connection.cursor()

            # ⚡ Faster WAL behavior
            cursor.execute("SET synchronous_commit = OFF")
            cursor.execute("SET jit = OFF")

            logger.info(f"[{granularity_name} - {rat_name}] Inserting {total_records} records...")

            batch = []
            batch_start_index = 0

            for idx, row in enumerate(df.itertuples(index=False), start=1):
                batch.append(
                    (
                        row.timestamp,
                        row.cell_name,
                        row.site_name,
                        row.standard_kpi_id,
                        row.kpi_value,
                        row.data_type,
                        row.oss_id,
                        row.file_name,
                        self.convert_nan_to_none(row.numerator_kpi_id),
                        self.convert_nan_to_none(row.numerator_kpi_value),
                        self.convert_nan_to_none(row.denominator_kpi_id),
                        self.convert_nan_to_none(row.denominator_kpi_value),
                        self.convert_nan_to_none(row.district_code_id),
                        rat_id,
                        granularity_id,
                    )
                )

                if len(batch) >= BATCH_SIZE or idx == total_records:
                    try:
                        # ⚡ Bulk insert
                        execute_values(cursor, insert_query, batch, page_size=BATCH_SIZE)
                        connection.commit()

                        # Track inserted vs skipped rows
                        inserted = cursor.rowcount
                        skipped = len(batch) - inserted
                        successful_inserts += inserted
                        skipped_records += skipped

                    except Error as batch_error:
                        # Batch failed → fallback to per-row inserts
                        connection.rollback()
                        for offset, record in enumerate(batch):
                            try:
                                cursor.execute(
                                    insert_query.replace(
                                        "VALUES %s",
                                        "VALUES (" + ",".join(["%s"] * 15) + ")"
                                    ),
                                    record
                                )
                                connection.commit()
                                successful_inserts += 1
                            except Error as row_error:
                                connection.rollback()
                                error_records.append(
                                    {
                                        "row_index": batch_start_index + offset,
                                        "error_type": type(row_error).__name__,
                                        "error_message": str(row_error),
                                        "row_data": df.iloc[batch_start_index + offset].to_dict(),
                                    }
                                )

                    # 📊 Progress reporting
                    if (successful_inserts + skipped_records) % PROGRESS_INTERVAL == 0 or idx == total_records:
                        elapsed = time.time() - start_time
                        rate = (successful_inserts + skipped_records) / elapsed if elapsed > 0 else 0
                        remaining = total_records - (successful_inserts + skipped_records)
                        eta = timedelta(seconds=int(remaining / rate)) if rate > 0 else "N/A"

                        msg = (
                            f"[{granularity_name} - {rat_name}] "
                            f"{successful_inserts}/{total_records} inserted | "
                            f"{skipped_records} skipped | "
                            f"errors: {len(error_records)} | "
                            f"rate: {rate:.0f} rec/s | ETA: {eta}"
                        )

                        sys.stdout.write("\r" + msg)
                        sys.stdout.flush()

                    batch.clear()
                    batch_start_index = idx

            sys.stdout.write("\n")
            sys.stdout.flush()

            logger.info(f"[{granularity_name} - {rat_name}] Successfully inserted {successful_inserts} records")
            logger.info(f"[{granularity_name} - {rat_name}] Skipped (duplicates) {skipped_records} records")

            if error_records:
                logger.warning(f"[{granularity_name} - {rat_name}] Failed to insert {len(error_records)} records")
                error_log_path = self.create_error_log_file(rat_id, granularity_name, original_filename, error_records)
                logger.info(f"[{granularity_name} - {rat_name}] Error log created: {error_log_path}")
                return False

            logger.info(f"[{granularity_name} - {rat_name}] All remaining records inserted successfully")
            return True

        except Error as e:
            sys.stdout.write("\n")
            sys.stdout.flush()
            logger.exception("Database error during insert")
            return False

        finally:
            if cursor:
                cursor.close()
            if connection:
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
                          original_filename: str, rat_id: int, granularity_name: str) -> bool:
        """Process a single data file (XLSX or CSV) for a specific RAT

        Returns:
            bool: True if processing was successful, False otherwise
        """
        granularity_id = self.get_granularity_id_by_name(granularity_name)

        try:
            rat_name = self.rats[rat_id]['name']
            logger.info(f"[{granularity_name} - {rat_name}] Reading {file_type.upper()} file: {file_path}")
            logger.info(f"[{granularity_name} - {rat_name}] OSS identifier: {oss_identifier}")

            # Read the data file
            df = self.read_data_file(file_path, file_type, oss_config)

            if df.empty:
                logger.warning(f"[{granularity_name} - {rat_name}] No data read from {file_path}")
                return False

            logger.info(f"[{granularity_name} - {rat_name}] Read {len(df)} rows and {len(df.columns)} columns from {file_path}")
            # logger.info(f"[{granularity_name} - {rat_name}] Column names: {list(df.columns)}")
            logger.info(f"[{granularity_name} - {rat_name}] Normalizing column names...")

            # Normalize column names
            df = self.normalize_column_names(df)

            logger.info(f"[{granularity_name} - {rat_name}] Unpivoting dataframe...")
            # Convert to unpivoted format using database mappings with numerator/denominator support
            unpivoted_df = self.unpivot_dataframe(df, oss_identifier, oss_config, original_filename, rat_id, granularity_id)

            if unpivoted_df.empty:
                logger.warning(f"[{granularity_name} - {rat_name}] No data to insert after unpivoting and mapping")
                return False

            # Insert into database
            success = self.insert_data_to_postgresql(unpivoted_df, rat_id, original_filename, granularity_name)

            if success:
                logger.info(f"[{granularity_name} - {rat_name}] Successfully processed {file_path}")
                return True
            else:
                logger.warning(f"[{granularity_name} - {rat_name}] Processing completed with errors for {file_path}")
                return True  # Still return True as partial data was inserted

        except Exception as e:
            logger.error(f"Error processing data file {file_path}: {e}")
            import traceback
            logger.error(f"Traceback: {traceback.format_exc()}")
            return False

    def mark_file_as_processed(self, file_path: str, rat_id: int, granularity_name:str):
        """Move processed file to processed folder"""
        try:
            rat_info = self.rats[rat_id]
            # processed_folder = rat_info['folders']['processed']
            processed_folder = rat_info['folders'][granularity_name]['processed']
            rat_name = rat_info['name']

            filename = os.path.basename(file_path)
            processed_path = os.path.join(processed_folder, filename)
            shutil.move(file_path, processed_path)
            logger.info(f"[{granularity_name} - {rat_name}] Moved {filename} to processed folder")
            logger.info(f"[{granularity_name} - {rat_name}] ========== END FILE ========== \n")
        except Exception as e:
            logger.error(f"Error moving file to processed folder: {e}")
            logger.info(f"========== END FILE ==========")

    def cleanup_temp_files(self, rat_id: int, granularity_name:str):
        """Clean up temporary files for specific RAT"""
        try:
            rat_info = self.rats[rat_id]
            # temp_folder = rat_info['folders']['temp']
            temp_folder = rat_info['folders'][granularity_name]['temp']

            for file in os.listdir(temp_folder):
                file_path = os.path.join(temp_folder, file)
                os.remove(file_path)
        except Exception as e:
            logger.error(f"Error cleaning up temp files: {e}")

    def process_new_files_for_rat(self, rat_id: int, granularity_name: str):
        """Process new files for a specific RAT"""
        rat_info = self.rats[rat_id]
        rat_name = rat_info['name']

        # logger.info(f"{'=' * 60}")
        # logger.info(f"{'-' * 10} Processing RAT: {rat_name} (ID: {rat_id}) {'-' * 10}")
        # logger.info(f"{'-' * 60}")

        new_files = self.get_new_files(rat_id, granularity_name)

        if not new_files:
            logger.info(f"[{granularity_name} - {rat_name}] No new files to process")
            return

        logger.info(f"[{granularity_name} - {rat_name}] Found {len(new_files)} new files to process")

        # Track if any files were successfully processed
        files_processed_successfully = 0
        files_with_errors = 0

        for file_path in new_files:
            processing_success = False

            try:
                filename = os.path.basename(file_path)
                logger.info(f"[{granularity_name} - {rat_name}] Processing {filename}")

                # Identify OSS source using database configuration
                oss_identifier, oss_config = self.identify_oss_source(filename)

                if not oss_identifier:
                    logger.warning(f"[{granularity_name} - {rat_name}] Could not identify OSS for file: {filename}")
                    continue

                # Determine file type and processing method
                file_format, needs_extraction = self.get_file_info(file_path)

                if needs_extraction and file_format == 'zip':
                    # logger.info(f"[{granularity_name} - {rat_name}] File needs extraction")
                    # Extract data file from ZIP
                    extraction_result = self.extract_data_file_from_zip(file_path, rat_id, granularity_name)

                    if extraction_result:
                        data_file_path, file_type = extraction_result
                        # Process the extracted data file
                        processing_success = self.process_data_file(data_file_path, file_type, oss_identifier, oss_config, filename, rat_id, granularity_name)

                elif not needs_extraction and file_format in ['xlsx', 'csv']:
                    # Process file directly
                    # logger.info(f"[{granularity_name} - {rat_name}] File does not need extraction")
                    processing_success = self.process_data_file(file_path, file_format, oss_identifier, oss_config, filename, rat_id, granularity_name)

                else:
                    logger.warning(f"[{granularity_name} - {rat_name}] Unsupported file format: {file_format}")
                    continue

                # Only mark file as processed if processing was successful
                if processing_success:
                    self.mark_file_as_processed(file_path, rat_id, granularity_name)
                    files_processed_successfully += 1
                else:
                    logger.error(f"[{granularity_name} - {rat_name}] File processing failed. File will not be moved to processed folder.")
                    logger.error(f"[{granularity_name} - {rat_name}] {'-' * 10} END FILE (FAILED!) {'-' * 10}")
                    files_with_errors += 1

                # Clean up temp files regardless of success
                self.cleanup_temp_files(rat_id, granularity_name)

            except Exception as e:
                logger.error(f"[{granularity_name} - {rat_name}] Error processing file {filename}: {e}")
                import traceback
                logger.error(f"Traceback: {traceback.format_exc()}")
                logger.error(f"[{granularity_name} - {rat_name}] File will not be moved to processed folder due to error.")
                logger.error(f"[{granularity_name} - {rat_name}] {'-' * 10} END FILE (ERROR!) {'-' * 10}")
                files_with_errors += 1

                # Clean up temp files even on error
                try:
                    self.cleanup_temp_files(rat_id, granularity_name)
                except:
                    pass

        # Clear Redis cache once after processing all files for this RAT
        if files_processed_successfully > 0:
            logger.info(f"[{granularity_name} - {rat_name}] Processed {files_processed_successfully} file(s) successfully")
            # logger.info(f"[{granularity_name} - {rat_name}] Triggering cache eviction and warmup for RAT...")
            # logger.info(f"skipping clearing cache...")
            self.clear_redis_cache(rat_name, granularity_name)
        else:
            logger.info(f"[{granularity_name} - {rat_name}] No files were processed successfully. Skipping cache clearing.")

        if files_with_errors > 0:
            logger.warning(f"[{granularity_name} - {rat_name}] {files_with_errors} file(s) encountered errors during processing")

    def process_new_files(self):
        """Process new files for all RATs using database configuration"""
        # logger.info(f"{'#' * 80}")
        logger.info(f"{'=' * 3} STARTING KPI PROCESSING CYCLE AT {datetime.now().strftime('%Y-%m-%d %H:%M:%S')} {'=' * 3}")
        # logger.info(f"{'#' * 80}\n")

        # logger.info(f"Available granularities: {self.granularities}")

        for granularity_id, gran in self.granularities.items():
            granularity_name = gran['name']
            # logger.info(f"Processing granularity: {granularity_name}")

            # Process files for each RAT
            for rat_id in self.rats.keys():
                try:
                    self.process_new_files_for_rat(rat_id, granularity_name)
                except Exception as e:
                    rat_name = self.rats[rat_id]['name']
                    logger.error(f"Error processing RAT {rat_name}: {e}")
                    import traceback
                    logger.error(f"Traceback: {traceback.format_exc()}")

        # logger.info(f"{'#' * 80}")
        logger.info(f"{'=' * 3} COMPLETED KPI PROCESSING CYCLE AT {datetime.now().strftime('%Y-%m-%d %H:%M:%S')} {'=' * 3}\n")
        # logger.info(f"{'#' * 80}\n")

    def run_continuously(self, interval_minutes: int = 60):
        """Run the processor continuously with specified interval"""
        logger.info(f"Starting continuous processing with {interval_minutes} minute intervals")
        rats_str = ", ".join(f"{r['name']} ({r['label']})" for r in self.rats.values())
        # logger.info("Configured RATs: %s", rats_str)

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