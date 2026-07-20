"""
Netrics — Multi-OSS Alarm Ingestion Script
============================================
Continuously polls ./files/oss/<source>/ for alarm reports (.csv, .xls, .xlsx, .zip),
normalizes them against DB-driven field/value mapping tables, and inserts into the
`alarms` hypertable. Designed to run as the single process in a Docker container.

Folder structure (auto-created on startup, per alarm_source in the DB):
  ./alarms/oss/<source>/        - drop new report files here
  ./alarms/processed/<source>/  - successfully ingested files are moved here
  ./alarms/failed/<source>/     - files that errored out entirely are moved here
  ./alarms/logs/<source>/       - per-source, per-day log files
"""

import os
import re
import sys
import signal
import time
import shutil
import logging
import zipfile
from datetime import datetime, timedelta
from contextlib import contextmanager
from dateutil import parser as dateparser
from dateutil import tz

import pandas as pd
import psycopg2
import psycopg2.extras
# from dateutil import parser as dateparser


# ============================================================
# Configuration
# ============================================================

DB_CONFIG = {
    "host": os.environ.get("POSTGRES_HOST", "localhost"),
    "port": os.environ.get("POSTGRES_PORT", "8032"),
    "dbname": os.environ.get("POSTGRES_DB_PULSE", "pulse_db"),
    "user": os.environ.get("POSTGRES_USER", "pguser"),
    "password": os.environ.get("POSTGRES_PASSWORD", "password"),
}

BASE_DIR = os.environ.get("ALARM_FILES_BASE_DIR", "./files/alarms")
OSS_DIR = os.path.join(BASE_DIR, "oss")
PROCESSED_DIR = os.path.join(BASE_DIR, "processed")
FAILED_DIR = os.path.join(BASE_DIR, "failed")
LOGS_DIR = os.path.join(BASE_DIR, "logs")

POLL_INTERVAL_SECONDS = int(os.environ.get("POLL_INTERVAL_SECONDS", "30"))

# Local UTC offset assumed for naive timestamps (U31 reports carry no offset).
# Confirm this matches the OSS servers' configured timezone.
LOCAL_UTC_OFFSET_HOURS = 5.5

SEVERITY_CHOICES = {"CRITICAL", "MAJOR", "MINOR", "WARNING", "INDETERMINATE", "CLEARED"}
ACK_STATE_CHOICES = {"ACKNOWLEDGED", "UNACKNOWLEDGED", "UNKNOWN"}
CLEAR_STATE_CHOICES = {"CLEARED", "UNCLEARED", "UNKNOWN"}

# Per-source FILE STRUCTURE quirks (encoding, header row, footer junk).
# These are format-level concerns, distinct from the DB-driven field/value
# mapping tables, which handle business-level column meaning instead.
PROFILES = {
    "ume": {
        "reader": "excel",
        "sheet_name": 0,
        "header_row": 0,
    },
    "u31": {
        "reader": "csv",
        "encoding": "latin1",
        "header_row": 1,  # row 0 is a title line "Active Alarm"
        "footer_filter_column": "Severity",
        "footer_filter_valid": {"CRITICAL", "MAJOR", "MINOR", "WARNING", "INDETERMINATE"},
    },
    "u2020": {
        "reader": "csv",
        "encoding": "utf-8-sig",
        "header_row": 0,
    },
}

_SCI_NOTATION_RE = re.compile(r'^\d(\.\d+)?E\+?\d+$', re.IGNORECASE)

_shutdown = False


# ============================================================
# Signal handling (graceful shutdown for Docker)
# ============================================================

def _handle_signal(signum, frame):
    global _shutdown
    logging.info(f"Received signal {signum}, will exit after current cycle")
    _shutdown = True


# ============================================================
# Logging
# ============================================================

def setup_logger(source_name):
    log_path = os.path.join(LOGS_DIR, source_name, f"{datetime.now():%Y%m%d}.log")
    logger = logging.getLogger(source_name)
    logger.setLevel(logging.INFO)
    logger.handlers.clear()
    fh = logging.FileHandler(log_path)
    fh.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
    logger.addHandler(fh)
    logger.addHandler(logging.StreamHandler(sys.stdout))
    return logger


# ============================================================
# Database access
# ============================================================

@contextmanager
def get_conn():
    conn = psycopg2.connect(**DB_CONFIG)
    try:
        yield conn
    finally:
        conn.close()


def fetch_alarm_sources(conn):
    with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
        cur.execute("SELECT id, name, label FROM alarm_sources")
        return cur.fetchall()


def fetch_field_mappings(conn, source_id):
    with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
        cur.execute("""
            SELECT canonical_field, source_column, extraction_regex, default_value, is_required
            FROM alarm_field_mappings WHERE alarm_source_id = %s
        """, (source_id,))
        return {row["canonical_field"]: row for row in cur.fetchall()}


def fetch_value_mappings(conn, source_id, mapping_type):
    with conn.cursor() as cur:
        cur.execute("""
            SELECT raw_value, canonical_value FROM alarm_value_mappings
            WHERE alarm_source_id = %s AND mapping_type = %s
        """, (source_id, mapping_type))
        return {raw.strip().upper(): canon for raw, canon in cur.fetchall()}


def record_unmapped_value(conn, source_id, mapping_type, raw_value):
    with conn.cursor() as cur:
        cur.execute("""
            INSERT INTO unmapped_alarm_values (alarm_source_id, mapping_type, raw_value)
            VALUES (%s, %s, %s)
            ON CONFLICT (alarm_source_id, mapping_type, raw_value)
            DO UPDATE SET occurrence_count = unmapped_alarm_values.occurrence_count + 1,
                          last_seen = now()
        """, (source_id, mapping_type, raw_value))
    conn.commit()


def normalize_type_name(raw):
    return re.sub(r"[\s_-]+", "_", raw.strip().upper())


def get_or_create_alarm_type(conn, raw_type_name):
    """Find-or-create an AlarmType, matching on a normalized (whitespace/underscore/
    hyphen-insensitive, case-insensitive) comparison so 'Platform Alarm' and
    'PLATFORM_ALARM' resolve to the same row.

    IMPORTANT: the lookup query must apply the *same* normalization SQL-side as
    normalize_type_name() does in Python — comparing upper(name) (which keeps
    spaces) against a Python-normalized value (which replaces spaces with
    underscores) would never match, causing every lookup to fall through to
    INSERT, hit the real unique constraint on an existing row, and return
    nothing from RETURNING — which previously caused a bare fetchone()[0] to
    crash with 'NoneType is not subscriptable'.
    """
    normalized = normalize_type_name(raw_type_name)
    lookup_sql = """
        SELECT id FROM alarm_types
        WHERE upper(regexp_replace(name, '[\\s_-]+', '_', 'g')) = %s
    """
    with conn.cursor() as cur:
        cur.execute(lookup_sql, (normalized,))
        row = cur.fetchone()
        if row:
            return row[0]

        cur.execute("""
            INSERT INTO alarm_types (name) VALUES (%s)
            ON CONFLICT (name) DO NOTHING
            RETURNING id
        """, (raw_type_name.strip(),))
        row = cur.fetchone()
        if row:
            return row[0]

        cur.execute(lookup_sql, (normalized,))
        row = cur.fetchone()
        if row:
            return row[0]

        raise RuntimeError(f"Could not resolve or create alarm_type for '{raw_type_name}'")


def get_or_create_alarm_definition(conn, source_id, alarm_code, alarm_name):
    with conn.cursor() as cur:
        cur.execute("""
            INSERT INTO alarm_definitions (alarm_source_id, alarm_code, alarm_name)
            VALUES (%s, %s, %s)
            ON CONFLICT (alarm_source_id, alarm_code) DO NOTHING
            RETURNING id
        """, (source_id, alarm_code, alarm_name))
        row = cur.fetchone()
        if row:
            return row[0]

        cur.execute("""
            SELECT id FROM alarm_definitions WHERE alarm_source_id = %s AND alarm_code = %s
        """, (source_id, alarm_code))
        row = cur.fetchone()
        if row:
            return row[0]

        raise RuntimeError(f"Could not resolve or create alarm_definition for source={source_id}, code={alarm_code}")


def insert_alarms(conn, rows):
    """Returns the true count of rows actually inserted (excludes rows skipped
    by ON CONFLICT DO NOTHING).

    Uses RETURNING id + fetch=True rather than cur.rowcount: execute_values
    batches internally in pages of 100 rows by default, and cur.rowcount only
    reflects the most recently executed page — for files with >100 rows this
    silently undercounts (e.g. 132 rows reported as 32 inserted). fetch=True
    aggregates the RETURNING results across all pages, giving an accurate total.
    """
    if not rows:
        return 0
    with conn.cursor() as cur:
        result = psycopg2.extras.execute_values(cur, """
            INSERT INTO alarms (
                node_name, alarm_definition_id, occurrence_time, specific_problem,
                severity, raw_severity, ack_state, alarm_id, alarm_type_id,
                location, additional_info, description, clear_state, clear_time,
                alarm_source_id
            ) VALUES %s
            ON CONFLICT (occurrence_time, node_name, alarm_definition_id, severity,
                         ack_state, clear_state, alarm_id, COALESCE(location, ''), alarm_source_id)
            DO NOTHING
            RETURNING id
        """, rows, fetch=True)
        return len(result)


# ============================================================
# Filesystem helpers
# ============================================================

def ensure_dirs(source_names):
    for name in source_names:
        for root in (OSS_DIR, PROCESSED_DIR, FAILED_DIR, LOGS_DIR):
            os.makedirs(os.path.join(root, name), exist_ok=True)


def list_unprocessed_files(source_dir):
    supported = (".csv", ".xls", ".xlsx", ".zip")
    return sorted(
        f for f in os.listdir(source_dir)
        if f.lower().endswith(supported) and os.path.isfile(os.path.join(source_dir, f))
    )


def extract_zip(zip_path, extract_to):
    extracted = []
    with zipfile.ZipFile(zip_path, "r") as zf:
        for member in zf.namelist():
            if member.lower().endswith((".csv", ".xls", ".xlsx")):
                zf.extract(member, extract_to)
                extracted.append(os.path.join(extract_to, member))
    return extracted


def move_file(src, dest_dir):
    os.makedirs(dest_dir, exist_ok=True)
    shutil.move(src, os.path.join(dest_dir, os.path.basename(src)))


# ============================================================
# Mapping / parsing helpers
# ============================================================

def normalize_columns(df):
    df.columns = [str(c).strip() for c in df.columns]
    return df


def extract_value(raw_value, regex):
    if raw_value is None or (isinstance(raw_value, float) and pd.isna(raw_value)):
        return None
    raw_value = str(raw_value).strip()
    if raw_value == "" or raw_value.lower() == "nan":
        return None
    if not regex:
        return raw_value
    match = re.search(regex, raw_value)
    return match.group(1).strip() if match else None


def map_row(row, field_mappings):
    result = {}
    for field, cfg in field_mappings.items():
        source_col = cfg["source_column"]
        if source_col and source_col in row.index:
            result[field] = extract_value(row[source_col], cfg["extraction_regex"])
        else:
            result[field] = cfg["default_value"]
    return result


def parse_datetime(value):
    """Returns a naive UTC datetime, or None if unparseable/absent.

    Strips 'GMT' before parsing offsets like 'GMT+05:30' — dateutil
    interprets 'GMT+N' using the POSIX sign convention (west-positive),
    which silently produces a UTC-N result instead of UTC+N. Stripping
    'GMT' leaves a plain '+05:30' offset, which parses correctly.
    """
    if not value:
        return None
    cleaned = re.sub(r"\s*GMT", "", value.strip())
    try:
        dt = dateparser.parse(cleaned)
    except (ValueError, OverflowError):
        return None
    if dt.tzinfo is not None:
        # return dt.astimezone(dateparser.tz.UTC).replace(tzinfo=None)
        return dt.astimezone(tz.UTC).replace(tzinfo=None)
    # Naive timestamp (e.g. U31) — assume local OSS server time, convert to UTC.
    return dt - timedelta(hours=LOCAL_UTC_OFFSET_HOURS)


def resolve_enum(raw_value, value_map, canonical_choices, fallback):
    """Resolve a raw vendor string to a canonical enum name.

    Order: (1) raw value already matches a canonical enum name directly
    (case-insensitive) — covers most sources without needing seed data;
    (2) explicit vendor mapping table entry — covers real mismatches like
    UME's 'Unack'; (3) fallback, with the miss recorded for review.
    Returns (canonical_value, matched: bool).
    """
    if raw_value is None:
        return fallback, False
    key = raw_value.strip().upper()
    if key in canonical_choices:
        return key, True
    if key in value_map:
        return value_map[key], True
    return fallback, False


def is_suspect_scientific_notation(raw_value):
    """Flags values like '1.78054E+12' — a sign the source export truncated
    a large integer's precision (seen intermittently in U31 exports that
    were opened and re-saved in Excel before delivery)."""
    return bool(raw_value) and bool(_SCI_NOTATION_RE.match(raw_value.strip()))


def to_bigint(raw):
    """Handles values exported in scientific notation (e.g. a truncated Alarm ID)."""
    return int(float(raw))


# ============================================================
# File reading
# ============================================================

def read_dataframe(path, profile):
    if profile["reader"] == "excel":
        return pd.read_excel(path, dtype=str, sheet_name=profile.get("sheet_name", 0),
                             header=profile.get("header_row", 0))
    return pd.read_csv(path, dtype=str, encoding=profile.get("encoding", "utf-8"),
                       header=profile.get("header_row", 0))


def apply_footer_filter(df, profile):
    col = profile.get("footer_filter_column")
    if not col or col not in df.columns:
        return df
    valid = profile["footer_filter_valid"]
    return df[df[col].fillna("").str.strip().str.upper().isin(valid)]


# ============================================================
# Row-level processing
# ============================================================

def process_dataframe(df, source, field_mappings, value_maps, logger):
    rows_to_insert = []
    error_count = 0

    for idx, row in df.iterrows():
        try:
            mapped = map_row(row, field_mappings)

            node_name = mapped.get("NODE_NAME")
            alarm_code_raw = mapped.get("ALARM_CODE")
            alarm_name = mapped.get("ALARM_NAME")
            occurrence_time = parse_datetime(mapped.get("OCCURRENCE_TIME"))
            alarm_id_raw = mapped.get("ALARM_ID")
            alarm_type_raw = mapped.get("ALARM_TYPE")

            if not all([node_name, alarm_code_raw, occurrence_time, alarm_id_raw, alarm_type_raw]):
                raise ValueError(f"Missing required field(s) in row {idx}")

            if is_suspect_scientific_notation(alarm_id_raw):
                logger.warning(
                    f"Row {idx}: alarm_id '{alarm_id_raw}' looks like truncated scientific "
                    f"notation — precision may be lost. Relying on 'location' in the dedup "
                    f"key as a safety net for this row."
                )

            severity, ok = resolve_enum(mapped.get("SEVERITY"), value_maps["SEVERITY"], SEVERITY_CHOICES,
                                        "INDETERMINATE")
            if not ok and mapped.get("SEVERITY"):
                record_unmapped_value(source["conn"], source["id"], "SEVERITY", mapped["SEVERITY"])
                logger.warning(f"Unmapped SEVERITY: '{mapped['SEVERITY']}'")

            ack_state, ok = resolve_enum(mapped.get("ACK_STATE"), value_maps["ACK_STATE"], ACK_STATE_CHOICES, "UNKNOWN")
            if not ok and mapped.get("ACK_STATE"):
                record_unmapped_value(source["conn"], source["id"], "ACK_STATE", mapped["ACK_STATE"])
                logger.warning(f"Unmapped ACK_STATE: '{mapped['ACK_STATE']}'")

            clear_state, ok = resolve_enum(mapped.get("CLEAR_STATE"), value_maps["CLEAR_STATE"], CLEAR_STATE_CHOICES,
                                           "UNKNOWN")
            if not ok and mapped.get("CLEAR_STATE"):
                record_unmapped_value(source["conn"], source["id"], "CLEAR_STATE", mapped["CLEAR_STATE"])
                logger.warning(f"Unmapped CLEAR_STATE: '{mapped['CLEAR_STATE']}'")

            clear_time = parse_datetime(mapped.get("CLEAR_TIME"))

            alarm_type_id = get_or_create_alarm_type(source["conn"], alarm_type_raw)
            alarm_definition_id = get_or_create_alarm_definition(
                source["conn"], source["id"], to_bigint(alarm_code_raw),
                alarm_name or f"UNKNOWN_{alarm_code_raw}")

            rows_to_insert.append((
                node_name, alarm_definition_id, occurrence_time, mapped.get("SPECIFIC_PROBLEM"),
                severity, mapped.get("SEVERITY"), ack_state, to_bigint(alarm_id_raw), alarm_type_id,
                mapped.get("LOCATION"), mapped.get("ADDITIONAL_INFO"), mapped.get("DESCRIPTION"),
                clear_state, clear_time, source["id"]
            ))
        except Exception as e:
            error_count += 1
            logger.error(f"Row {idx} failed: {e}")

    return rows_to_insert, error_count


def process_file(path, source, field_mappings, value_maps, logger, profile):
    df = read_dataframe(path, profile)
    df = normalize_columns(df)
    df = apply_footer_filter(df, profile)

    rows, errors = process_dataframe(df, source, field_mappings, value_maps, logger)
    inserted = insert_alarms(source["conn"], rows)
    source["conn"].commit()
    logger.info(f"{os.path.basename(path)}: read={len(df)}, inserted={inserted}, row_errors={errors}")
    return errors


# ============================================================
# Cycle / main loop
# ============================================================

def run_cycle(conn):
    sources = fetch_alarm_sources(conn)
    ensure_dirs([s["name"] for s in sources])

    for src in sources:
        name = src["name"]
        profile = PROFILES.get(name)
        if not profile:
            continue  # no known file-structure profile for this source yet

        logger = setup_logger(name)
        source_dir = os.path.join(OSS_DIR, name)
        field_mappings = fetch_field_mappings(conn, src["id"])
        value_maps = {
            "SEVERITY": fetch_value_mappings(conn, src["id"], "SEVERITY"),
            "ACK_STATE": fetch_value_mappings(conn, src["id"], "ACK_STATE"),
            "CLEAR_STATE": fetch_value_mappings(conn, src["id"], "CLEAR_STATE"),
        }
        source_ctx = {"id": src["id"], "name": name, "conn": conn}

        for filename in list_unprocessed_files(source_dir):
            file_path = os.path.join(source_dir, filename)
            logger.info(f"Processing {filename}")
            try:
                if filename.lower().endswith(".zip"):
                    extract_dir = os.path.join(source_dir, f".{filename}_extracted")
                    extracted_files = extract_zip(file_path, extract_dir)
                    for ef in extracted_files:
                        process_file(ef, source_ctx, field_mappings, value_maps, logger, profile)
                    shutil.rmtree(extract_dir, ignore_errors=True)
                else:
                    process_file(file_path, source_ctx, field_mappings, value_maps, logger, profile)
                move_file(file_path, os.path.join(PROCESSED_DIR, name))
            except Exception as e:
                logger.error(f"FATAL - could not process {filename}: {e}")
                conn.rollback()
                move_file(file_path, os.path.join(FAILED_DIR, name))


def main():
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
    signal.signal(signal.SIGTERM, _handle_signal)
    signal.signal(signal.SIGINT, _handle_signal)

    logging.info(f"Alarm ingestion starting, polling every {POLL_INTERVAL_SECONDS}s")

    while not _shutdown:
        cycle_start = time.monotonic()
        try:
            with get_conn() as conn:
                run_cycle(conn)
        except Exception as e:
            logging.error(f"Cycle failed: {e}")

        elapsed = time.monotonic() - cycle_start
        if elapsed > POLL_INTERVAL_SECONDS:
            logging.warning(f"Cycle took {elapsed:.1f}s, longer than the {POLL_INTERVAL_SECONDS}s interval")
        sleep_for = max(0, POLL_INTERVAL_SECONDS - elapsed)
        for _ in range(int(sleep_for)):
            if _shutdown:
                break
            time.sleep(1)

    logging.info("Shutdown complete")


if __name__ == "__main__":
    main()