-- Синхронизация схемы с JPA: колонки, которые были в отдельных SQL-файлах вне Flyway
-- или расходились с entity (reviews, notification_settings).

-- ── reviews: отзыв мастера заказчику ──
ALTER TABLE reviews
    ADD COLUMN IF NOT EXISTS target_customer_id UUID;

-- В JPA target_worker_id и target_customer_id nullable (один из двух)
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

-- ── notification_settings: поле system в NotificationSettings.java ──
ALTER TABLE notification_settings
    ADD COLUMN IF NOT EXISTS system BOOLEAN NOT NULL DEFAULT true;
