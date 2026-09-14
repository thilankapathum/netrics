#!/bin/sh
set -e

exists=$(psql -U "$POSTGRES_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='${POSTGRES_DB_BEAM}'")
if [ "$exists" != "1" ]; then
  echo "Creating database ${POSTGRES_DB_BEAM}"
  psql -U "$POSTGRES_USER" -d postgres -c "CREATE DATABASE \"${POSTGRES_DB_BEAM}\""
else
  echo "Database ${POSTGRES_DB_BEAM} already exists, skipping"
fi
