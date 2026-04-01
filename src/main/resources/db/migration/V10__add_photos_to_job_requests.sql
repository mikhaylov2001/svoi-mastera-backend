-- V10: Add photos to job_requests
-- Дата: 2026-04-01

-- 1. Добавляем колонку для хранения массива URL фотографий
ALTER TABLE job_requests
    ADD COLUMN IF NOT EXISTS photos TEXT[];

-- Примеры значений:
-- photos = {'/uploads/job-photos/uuid1.jpg', '/uploads/job-photos/uuid2.jpg'}
-- или base64 массив: {'data:image/jpeg;base64,...', 'data:image/png;base64,...'}

-- 2. Добавляем индекс для оптимизации поиска заявок с фото
CREATE INDEX IF NOT EXISTS idx_job_requests_has_photos
    ON job_requests ((photos IS NOT NULL AND array_length(photos, 1) > 0));

-- Проверка
SELECT
    id,
    title,
    array_length(photos, 1) as photo_count,
    photos
FROM job_requests
WHERE photos IS NOT NULL
    LIMIT 5;

COMMENT ON COLUMN job_requests.photos IS 'Массив URL или base64 фотографий к заявке (макс 5 шт)';