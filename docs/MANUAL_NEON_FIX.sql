-- ═══════════════════════════════════════════════════════════════════
-- Ручной фикс схемы Neon (после V1–V25, если бэкенд падает на validate)
-- Neon → SQL Editor → вставить целиком → Run
-- Безопасно повторять: IF NOT EXISTS / идемпотентные ALTER
-- ═══════════════════════════════════════════════════════════════════

-- V26: телефон в профилях
ALTER TABLE worker_profiles
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

ALTER TABLE customer_profiles
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

-- V27: отзывы + настройки уведомлений
ALTER TABLE reviews
    ADD COLUMN IF NOT EXISTS target_customer_id UUID;

ALTER TABLE reviews
    ALTER COLUMN target_worker_id DROP NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_reviews_target_customer'
    ) THEN
        ALTER TABLE reviews
            ADD CONSTRAINT fk_reviews_target_customer
                FOREIGN KEY (target_customer_id) REFERENCES customer_profiles(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS ix_reviews_target_customer_id
    ON reviews(target_customer_id);

ALTER TABLE notification_settings
    ADD COLUMN IF NOT EXISTS system BOOLEAN NOT NULL DEFAULT true;

-- ── Проверка (должны быть 3 строки phone, target_customer_id, system) ──
SELECT table_name, column_name, data_type
FROM information_schema.columns
WHERE (table_name = 'customer_profiles' AND column_name = 'phone')
   OR (table_name = 'worker_profiles' AND column_name = 'phone')
   OR (table_name = 'reviews' AND column_name = 'target_customer_id')
   OR (table_name = 'notification_settings' AND column_name = 'system')
ORDER BY table_name, column_name;
