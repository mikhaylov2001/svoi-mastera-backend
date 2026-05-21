# Миграции БД (Flyway)

Все изменения схемы — только через `src/main/resources/db/migration/V*.sql`.

При старте с профилем `prod` Flyway применяет миграции автоматически (`application-prod.yml`).

## Ручной запуск (Neon / локально)

```bash
export FLYWAY_URL='jdbc:postgresql://HOST/neondb?sslmode=require'
export FLYWAY_USER='...'
export FLYWAY_PASSWORD='...'
./scripts/flyway-migrate.sh migrate
./scripts/flyway-migrate.sh info
```

## Переменные на Render

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL` — `jdbc:postgresql://...`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## Важные версии

| Версия | Содержание |
|--------|------------|
| V1–V25 | Базовая схема, объявления, верификация, гарантия |
| V26 | `phone` в `worker_profiles` / `customer_profiles` |
| V27 | `reviews.target_customer_id`, nullable `target_worker_id`, `notification_settings.system` |

Файлы в корне `migration_*.sql` — **устарели**, не используются Flyway; логика перенесена в V26/V27.
