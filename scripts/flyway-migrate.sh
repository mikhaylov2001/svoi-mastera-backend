#!/usr/bin/env bash
# Ручной запуск Flyway к вашей Postgres (Neon / Render).
#
# Подставьте те же реквизиты, что в SPRING_DATASOURCE_* на Render:
#   export FLYWAY_URL='jdbc:postgresql://....neon.tech/neondb?sslmode=require'
#   export FLYWAY_USER='...'
#   export FLYWAY_PASSWORD='...'
#
# Дальше из каталога backend:
#   ./scripts/flyway-migrate.sh migrate   # применить миграции
#   ./scripts/flyway-migrate.sh info      # что уже применено
#
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
CMD="${1:-migrate}"
./mvnw -q compile "flyway:${CMD}" \
  "-Dflyway.url=${FLYWAY_URL:?Задайте FLYWAY_URL (jdbc:postgresql://...)}" \
  "-Dflyway.user=${FLYWAY_USER:?Задайте FLYWAY_USER}" \
  "-Dflyway.password=${FLYWAY_PASSWORD:?Задайте FLYWAY_PASSWORD}"
