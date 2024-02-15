#!/usr/bin/env bash

script_file="$(dirname "$0")/create-one-plan-user-and-role.sql"

docker run -it --network hmpps-one-plan-api_hmpps \
  -e PGPASSWORD='admin_password' \
  -e PGUSER='admin' \
  -v "$script_file:/run.sql" \
  postgres:16.1 psql -f /run.sql postgresql://auth-db:5432/auth-db
