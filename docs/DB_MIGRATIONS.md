# Миграции БД

## Рекомендуется: вручную (надёжнее)

### Вариант A — только дозаплатка (БД уже прошла V1–V25)

1. Откройте **Neon** → ваш проект → **SQL Editor**.
2. Скопируйте и выполните целиком файл  
   [`MANUAL_NEON_FIX.sql`](./MANUAL_NEON_FIX.sql)
3. В конце запроса проверки должны быть **4 строки** (`phone` ×2, `target_customer_id`, `system`).
4. **Render** → redeploy бэкенда. Flyway при старте применит V26/V27 ещё раз (это нормально, скрипты идемпотентные) и запишет их в историю.

### Вариант B — все миграции с локальной машины (Flyway CLI)

Из каталога `svoi-mastera-backend`:

```bash
export FLYWAY_URL='jdbc:postgresql://ВАШ-HOST/neondb?sslmode=require'
export FLYWAY_USER='neondb_owner'
export FLYWAY_PASSWORD='ваш_пароль'

./scripts/flyway-migrate.sh info    # что уже применено
./scripts/flyway-migrate.sh migrate # применить все недостающие V*.sql
```

Пароль и URL — только из Neon Dashboard, **не коммитить** в git.

### Render (после ручного SQL)

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://....neon.tech/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
```

URL для Spring — с префиксом `jdbc:postgresql://`, без `channel_binding=require`.

---

## Версии Flyway

| Версия | Содержание |
|--------|------------|
| V1–V25 | Полная база (в `src/main/resources/db/migration/`) |
| V26 | `phone` в профилях |
| V27 | `reviews.target_customer_id`, nullable `target_worker_id`, `notification_settings.system` |

Ручной фикс для уже развёрнутой БД: **только V26+V27** → [`MANUAL_NEON_FIX.sql`](./MANUAL_NEON_FIX.sql).

## Пустая новая БД

Либо `./scripts/flyway-migrate.sh migrate` (все V1–V27), либо один deploy бэкенда с `SPRING_PROFILES_ACTIVE=prod` — Flyway создаст всё сам.
