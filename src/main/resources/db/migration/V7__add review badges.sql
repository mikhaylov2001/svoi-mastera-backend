-- Миграция: Добавление смайликов/бейджей к отзывам
-- Дата: 2026-03-17

-- 1. Добавляем колонку для хранения бейджей (JSON array)
ALTER TABLE reviews
    ADD COLUMN IF NOT EXISTS badges TEXT;

-- Примеры значений:
-- '["polite", "fast", "quality"]'
-- '["polite", "quality", "price"]'

-- Доступные бейджи:
-- polite - Вежливый
-- fast - Быстро
-- quality - Качественно
-- price - Цена/качество

-- Проверка
SELECT id, rating, badges FROM reviews LIMIT 5;