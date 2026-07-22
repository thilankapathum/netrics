#!/usr/bin/env python3
"""
SFTP report puller.

Continuously polls one or more remote directories on an SFTP server and
pulls newly-arrived files down to local directories, skipping files that
have already been downloaded and files that still look like they're being
written by the remote side (still growing / too recently modified).

Config lives in config.yaml (path set via CONFIG_PATH env var).
"""

import datetime
import fnmatch
import json
import logging
import os
import re
import signal
import stat
import sys
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Optional

import paramiko
import yaml

# --------------------------------------------------------------------------
# Config loading
# --------------------------------------------------------------------------

ENV_VAR_PATTERN = re.compile(r"\$\{([A-Za-z_][A-Za-z0-9_]*)\}")


def _expand_env(value):
    """Recursively replace ${VAR_NAME} in strings with os.environ values."""
    if isinstance(value, str):
        return ENV_VAR_PATTERN.sub(lambda m: os.environ.get(m.group(1), ""), value)
    if isinstance(value, dict):
        return {k: _expand_env(v) for k, v in value.items()}
    if isinstance(value, list):
        return [_expand_env(v) for v in value]
    return value


@dataclass
class Source:
    name: str
    remote_dir: str
    local_dir: str
    pattern: str = "*"
    min_file_age_seconds: Optional[int] = None
    # If the remote files live under dated sub-directories, e.g.
    # remote_dir/20260722/report.csv, set use_date_subdirs: true.
    use_date_subdirs: bool = False
    date_dir_format: str = "%Y%m%d"
    lookback_days: int = 2
    # When use_date_subdirs is true, mirror the dated structure locally by
    # default (local_dir/20260722/file.csv). Set flatten_local_dirs: true to
    # instead drop every file straight into local_dir with no date subfolder
    # - useful when the downstream consumer only scans local_dir itself.
    flatten_local_dirs: bool = False


@dataclass
class Config:
    host: str
    port: int
    username: str
    password: Optional[str]
    private_key_path: Optional[str]
    private_key_passphrase: Optional[str]
    poll_interval_seconds: int
    min_file_age_seconds: int
    state_file: str
    sources: list
    connect_timeout: int = 15
    banner_timeout: int = 15
    auth_timeout: int = 20
    max_connect_retries: int = 5


def load_config(path: str) -> Config:
    with open(path, "r") as f:
        raw = yaml.safe_load(f)
    raw = _expand_env(raw)

    sftp = raw["sftp"]
    sources = [
        Source(
            name=s["name"],
            remote_dir=s["remote_dir"],
            local_dir=s["local_dir"],
            pattern=s.get("pattern", "*"),
            min_file_age_seconds=s.get("min_file_age_seconds"),
            use_date_subdirs=bool(s.get("use_date_subdirs", False)),
            date_dir_format=s.get("date_dir_format", "%Y%m%d"),
            lookback_days=int(s.get("lookback_days", 2)),
            flatten_local_dirs=bool(s.get("flatten_local_dirs", False)),
        )
        for s in raw["sources"]
    ]

    return Config(
        host=sftp["host"],
        port=int(sftp.get("port", 22)),
        username=sftp["username"],
        password=sftp.get("password") or None,
        private_key_path=sftp.get("private_key_path") or None,
        private_key_passphrase=sftp.get("private_key_passphrase") or None,
        poll_interval_seconds=int(raw.get("poll_interval_seconds", 60)),
        min_file_age_seconds=int(raw.get("min_file_age_seconds", 30)),
        state_file=raw.get("state_file", "/data/state/downloaded_files.json"),
        sources=sources,
        connect_timeout=int(raw.get("connect_timeout_seconds", 15)),
        banner_timeout=int(raw.get("banner_timeout_seconds", 15)),
        auth_timeout=int(raw.get("auth_timeout_seconds", 20)),
        max_connect_retries=int(raw.get("max_connect_retries", 5)),
    )


# --------------------------------------------------------------------------
# State tracking (what's already been downloaded)
# --------------------------------------------------------------------------

class State:
    """Tracks which remote files have already been pulled, per source."""

    def __init__(self, path: str):
        self.path = Path(path)
        self.path.parent.mkdir(parents=True, exist_ok=True)
        self._data = self._load()

    def _load(self) -> dict:
        if self.path.exists():
            try:
                with open(self.path, "r") as f:
                    return json.load(f)
            except (json.JSONDecodeError, OSError) as e:
                logging.warning("Could not read state file (%s), starting fresh: %s", self.path, e)
        return {}

    def seen(self, namespace: str, filename: str) -> bool:
        return filename in self._data.get(namespace, {})

    def mark_downloaded(self, namespace: str, filename: str, remote_mtime: int, size: int):
        self._data.setdefault(namespace, {})[filename] = {
            "remote_mtime": remote_mtime,
            "size": size,
            "downloaded_at": int(time.time()),
        }
        self._save()

    def _save(self):
        tmp_path = self.path.with_suffix(self.path.suffix + ".tmp")
        with open(tmp_path, "w") as f:
            json.dump(self._data, f, indent=2)
        os.replace(tmp_path, self.path)


# --------------------------------------------------------------------------
# SFTP connection
# --------------------------------------------------------------------------

def _load_private_key(cfg: Config):
    if not cfg.private_key_path:
        return None
    for key_cls in (paramiko.Ed25519Key, paramiko.RSAKey, paramiko.ECDSAKey):
        try:
            return key_cls.from_private_key_file(cfg.private_key_path, password=cfg.private_key_passphrase)
        except paramiko.SSHException:
            continue
    raise ValueError(f"Could not load private key from {cfg.private_key_path} (unsupported type)")


def connect(cfg: Config) -> paramiko.SFTPClient:
    """Returns an SFTPClient. The underlying SSHClient is stashed on
    sftp._ssh_client so close_connection() can tear both down cleanly."""
    attempt = 0
    while True:
        attempt += 1
        client = paramiko.SSHClient()
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        try:
            client.connect(
                hostname=cfg.host,
                port=cfg.port,
                username=cfg.username,
                password=cfg.password,
                pkey=_load_private_key(cfg),
                timeout=cfg.connect_timeout,        # TCP connect timeout
                banner_timeout=cfg.banner_timeout,  # SSH banner exchange timeout
                auth_timeout=cfg.auth_timeout,      # fail fast if auth stalls, instead
                                                     # of hanging until the OS/network
                                                     # eventually drops the socket
                allow_agent=False,
                look_for_keys=False,
            )
            sftp = client.open_sftp()
            sftp.get_channel().settimeout(cfg.connect_timeout)
            sftp._ssh_client = client  # keep alive; closed together in close_connection()
            logging.info("Connected to %s:%s as %s", cfg.host, cfg.port, cfg.username)
            return sftp
        except Exception as e:
            try:
                client.close()
            except Exception:
                pass
            if attempt >= cfg.max_connect_retries:
                raise
            backoff = min(60, 2 ** attempt)
            logging.warning(
                "SFTP connect failed (attempt %d/%d): %s - retrying in %ds",
                attempt, cfg.max_connect_retries, e, backoff,
            )
            time.sleep(backoff)


def close_connection(sftp: Optional[paramiko.SFTPClient]):
    if not sftp:
        return
    client = getattr(sftp, "_ssh_client", None)
    try:
        sftp.close()
    except Exception:
        pass
    if client:
        try:
            client.close()
        except Exception:
            pass


# --------------------------------------------------------------------------
# Polling logic
# --------------------------------------------------------------------------

def poll_source(sftp: paramiko.SFTPClient, cfg: Config, source: Source, state: State):
    """Entry point for one source. Fans out to one or more date sub-directories
    if configured, otherwise polls remote_dir directly."""
    if not source.use_date_subdirs:
        _poll_directory(
            sftp, cfg, source, state,
            remote_dir=source.remote_dir,
            local_dir=source.local_dir,
            state_namespace=source.name,
        )
        return

    today = datetime.date.today()
    for offset in range(source.lookback_days):
        day = today - datetime.timedelta(days=offset)
        day_name = day.strftime(source.date_dir_format)
        remote_dir = f"{source.remote_dir.rstrip('/')}/{day_name}"
        local_dir = (
            source.local_dir
            if source.flatten_local_dirs
            else f"{source.local_dir.rstrip('/')}/{day_name}"
        )
        _poll_directory(
            sftp, cfg, source, state,
            remote_dir=remote_dir,
            local_dir=local_dir,
            state_namespace=f"{source.name}/{day_name}",
            missing_dir_is_error=False,  # today's/older folders may not exist yet
        )


def _poll_directory(
    sftp: paramiko.SFTPClient,
    cfg: Config,
    source: Source,
    state: State,
    remote_dir: str,
    local_dir: str,
    state_namespace: str,
    missing_dir_is_error: bool = True,
):
    try:
        entries = sftp.listdir_attr(remote_dir)
    except FileNotFoundError:
        if missing_dir_is_error:
            logging.error("[%s] remote dir not found: %s", source.name, remote_dir)
        else:
            logging.debug("[%s] remote dir not present yet: %s", source.name, remote_dir)
        return
    except IOError as e:
        logging.error("[%s] error listing %s: %s", source.name, remote_dir, e)
        return

    min_age = (
        source.min_file_age_seconds
        if source.min_file_age_seconds is not None
        else cfg.min_file_age_seconds
    )
    now = time.time()
    Path(local_dir).mkdir(parents=True, exist_ok=True)

    new_count = 0
    for entry in entries:
        if stat.S_ISDIR(entry.st_mode):
            continue
        filename = entry.filename
        if not fnmatch.fnmatch(filename, source.pattern):
            continue
        if state.seen(state_namespace, filename):
            continue

        age = now - entry.st_mtime
        if age < min_age:
            logging.debug(
                "[%s] skipping %s, too new (age=%.0fs < %ds)",
                source.name, filename, age, min_age,
            )
            continue

        remote_path = f"{remote_dir.rstrip('/')}/{filename}"
        local_final = Path(local_dir) / filename
        local_tmp = Path(local_dir) / f".{filename}.part"

        try:
            logging.info("[%s] downloading %s/%s (%d bytes)", source.name, remote_dir, filename, entry.st_size)
            sftp.get(remote_path, str(local_tmp))
            os.replace(local_tmp, local_final)  # atomic rename once fully written
            state.mark_downloaded(state_namespace, filename, int(entry.st_mtime), entry.st_size)
            new_count += 1
        except Exception as e:
            logging.error("[%s] failed to download %s/%s: %s", source.name, remote_dir, filename, e)
            if local_tmp.exists():
                try:
                    local_tmp.unlink()
                except OSError:
                    pass

    if new_count:
        logging.info("[%s] pulled %d new file(s) from %s", source.name, new_count, remote_dir)


# --------------------------------------------------------------------------
# Main loop
# --------------------------------------------------------------------------

_shutdown = False


def _handle_signal(signum, frame):
    global _shutdown
    logging.info("Received signal %s, shutting down after current cycle...", signum)
    _shutdown = True


def main():
    logging.basicConfig(
        level=os.environ.get("LOG_LEVEL", "INFO").upper(),
        format="%(asctime)s %(levelname)s %(message)s",
        stream=sys.stdout,
    )
    config_path = os.environ.get("CONFIG_PATH", "/app/config.yaml")
    cfg = load_config(config_path)
    state = State(cfg.state_file)

    signal.signal(signal.SIGTERM, _handle_signal)
    signal.signal(signal.SIGINT, _handle_signal)

    logging.info(
        "Starting: %d source(s), polling every %ds", len(cfg.sources), cfg.poll_interval_seconds
    )

    sftp = None
    while not _shutdown:
        cycle_start = time.time()
        try:
            if sftp is None:
                sftp = connect(cfg)
            for source in cfg.sources:
                if _shutdown:
                    break
                poll_source(sftp, cfg, source, state)
        except Exception as e:
            logging.error("Cycle failed, will reconnect next cycle: %s", e)
            close_connection(sftp)
            sftp = None

        elapsed = time.time() - cycle_start
        sleep_for = max(0, cfg.poll_interval_seconds - elapsed)
        if not _shutdown and sleep_for:
            time.sleep(sleep_for)

    close_connection(sftp)
    logging.info("Shutdown complete.")


if __name__ == "__main__":
    main()