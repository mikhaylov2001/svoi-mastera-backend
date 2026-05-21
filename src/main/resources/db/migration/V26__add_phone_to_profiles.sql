-- Телефон в профилях (есть в JPA-сущностях, не было в Flyway до V26).

ALTER TABLE worker_profiles
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

ALTER TABLE customer_profiles
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
