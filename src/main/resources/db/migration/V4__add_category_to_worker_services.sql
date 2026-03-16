-- Миграция: Добавление категории к услугам мастеров
-- Дата: 2026-03-16

-- 1. Добавляем колонку category_id (nullable пока)
ALTER TABLE worker_services
    ADD COLUMN IF NOT EXISTS category_id UUID;

-- 2. Создаём внешний ключ на таблицу categories
ALTER TABLE worker_services
    ADD CONSTRAINT fk_worker_services_category
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;

-- 3. Создаём индекс для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_worker_services_category_id
    ON worker_services(category_id);

-- 4. Обновляем существующего мастера - устанавливаем категорию "Компьютерная помощь"
UPDATE worker_services
SET category_id = (
    SELECT id FROM categories
    WHERE slug = 'kompyuternaya-pomosh'
    LIMIT 1
    )
WHERE title = 'Дома'
  AND category_id IS NULL;

-- 5. Проверка
SELECT
    ws.id,
    ws.title,
    ws.active,
    c.name as category_name
FROM worker_services ws
         LEFT JOIN categories c ON ws.category_id = c.id;