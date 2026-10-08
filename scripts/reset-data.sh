#!/usr/bin/env bash
# Puts the local database back to its starting state: no tickets, only the seed agents.
# Empties every table (including any you add), restarts IDs at 1, then reloads src/main/resources/data.sql.
# Run from the project root while Postgres is running. Windows: run it from Git Bash.
set -euo pipefail
cd "$(dirname "$0")/.."

if docker compose version >/dev/null 2>&1; then
  compose=(docker compose)
else
  compose=(docker-compose)
fi

psql() {
  "${compose[@]}" exec -T postgres psql -U helpdesk -d helpdesk -v ON_ERROR_STOP=1 -q "$@"
}

psql <<'SQL'
DO $$
DECLARE
    table_record record;
BEGIN
    FOR table_record IN SELECT tablename FROM pg_tables WHERE schemaname = 'public' LOOP
        EXECUTE 'TRUNCATE TABLE ' || quote_ident(table_record.tablename) || ' RESTART IDENTITY CASCADE';
    END LOOP;
END $$;
SQL

psql < src/main/resources/data.sql
echo "Database reset: no tickets, seed agents reloaded."
