-- Добавить колонку target_customer_id в таблицу reviews
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS target_customer_id UUID;

-- Добавить внешний ключ
ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_target_customer
        FOREIGN KEY (target_customer_id)
            REFERENCES customer_profiles(id);