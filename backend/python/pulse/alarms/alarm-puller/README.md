# Alarm Report SFTP Puller

Continuously polls the Alarm report server over SFTP and pulls new report
files down to the KPI server, for one or more vendor source directories
(e.g. UME, U31, U2020).

## Setup

1. Copy the example config and edit it:
   ```
   cp config.yaml.example config.yaml
   ```
   Fill in the real `host`, `username`, and each vendor's `remote_dir` /
   `pattern`. Use `password` or `private_key_path`, not both.

2. Set the SFTP password as an environment variable (don't put it in
   config.yaml directly):
   ```
   export SFTP_PASSWORD='...'
   ```
   Or put it in a `.env` file next to docker-compose.yml:
   ```
   SFTP_PASSWORD=...
   ```

3. Build and run:
   ```
   docker compose up -d --build
   ```

4. Watch logs:
   ```
   docker compose logs -f
   ```

## How it decides what's "new"

- Each cycle it lists the remote directory and compares filenames against
  a manifest (`state_file`, on a persistent volume) of what's already been
  pulled — so restarts don't re-download everything.
- A file is skipped until its remote modified time is older than
  `min_file_age_seconds` (default 30s, overridable per source), to avoid
  grabbing a report while the vendor system is still writing it.
- Downloads land in a `.filename.part` temp file first, then get atomically
  renamed to the final name — so anything watching `local_dir` (like your
  ingestion script) never sees a half-downloaded file.

## Things to adjust before production use

- **Auth**: password auth is simplest to start with; switch to a key
  (`private_key_path`) when you can — mount it read-only and don't commit
  it anywhere.
- **Host key checking**: the script currently doesn't pin the remote host
  key. If that matters for your environment, tell me and I'll add
  known_hosts verification.
- **Remote directory layout / filename patterns**: the `remote_dir` and
  `pattern` values in config.yaml.example are placeholders — set them once
  you've inspected real UME/U31/U2020 export paths.
- **Retention**: this script only pulls files down; it doesn't clean up
  `local_dir` or delete anything from the remote side. Add a retention
  policy (e.g. delete local files older than N days) once your ingestion
  pipeline has consumed them, so `/data/incoming` doesn't grow forever.
- **Multiple containers vs one**: right now one container polls all
  sources sequentially each cycle over a single SFTP connection. That's
  fine unless one vendor's directory is huge/slow and starves the others —
  in that case split into separate containers/config files per vendor.

## Wiring into your ingestion pipeline

Point your existing alarm ingestion script's watch directories at
`/data/incoming/<source-name>` (or mount the same `alarm_incoming` Docker
volume into that service). Since files only appear there once fully
downloaded and atomically renamed, you can safely treat "file exists in
that directory" as "file is ready to ingest."
