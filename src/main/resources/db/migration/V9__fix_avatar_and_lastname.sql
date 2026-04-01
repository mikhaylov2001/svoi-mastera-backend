-- V9: Fix avatar_url to TEXT and add last_name to profiles
-- Дата: 2026-04-01

-- 1. Изменяем avatar_url на TEXT (для base64 данных)
ALTER TABLE users
ALTER COLUMN avatar_url TYPE TEXT;

-- 2. Добавляем last_name к worker_profiles
ALTER TABLE worker_profiles
    ADD COLUMN IF NOT EXISTS last_name VARCHAR(150);

-- 3. Добавляем last_name к customer_profiles
ALTER TABLE customer_profiles
    ADD COLUMN IF NOT EXISTS last_name VARCHAR(150);

-- Проверка
SELECT
    'users' as table_name,
    column_name,
    data_type
FROM information_schema.columns
WHERE table_name = 'users' AND column_name = 'avatar_url'

UNION ALL

SELECT
    'worker_profiles' as table_name,
    column_name,
    data_type
FROM information_schema.columns
WHERE table_name = 'worker_profiles' AND column_name = 'last_name'

UNION ALL

SELECT
    'customer_profiles' as table_name,
    column_name,
    data_type
FROM information_schema.columns
WHERE table_name = 'customer_profiles' AND column_name = 'last_name';