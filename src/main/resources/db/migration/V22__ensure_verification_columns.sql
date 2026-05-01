-- Идемпотентно добавляет колонки верификации, если их нет (частичная схема после Hibernate
-- или сбой до завершения V20). Дубликат DDL из V20 — безопасен при повторном запуске.

ALTER TABLE worker_profiles
    ADD COLUMN IF NOT EXISTS verification_status VARCHAR(32) NOT NULL DEFAULT 'NONE';

ALTER TABLE worker_profiles
    ADD COLUMN IF NOT EXISTS verification_submitted_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE worker_profiles
    ADD COLUMN IF NOT EXISTS verification_documents_json TEXT;

ALTER TABLE worker_profiles
    ADD COLUMN IF NOT EXISTS verification_signature_json TEXT;

ALTER TABLE worker_profiles
    ADD COLUMN IF NOT EXISTS verification_rejection_reason VARCHAR(500);

ALTER TABLE customer_profiles
    ADD COLUMN IF NOT EXISTS verified BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE customer_profiles
    ADD COLUMN IF NOT EXISTS verification_status VARCHAR(32) NOT NULL DEFAULT 'NONE';

ALTER TABLE customer_profiles
    ADD COLUMN IF NOT EXISTS verification_submitted_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE customer_profiles
    ADD COLUMN IF NOT EXISTS verification_documents_json TEXT;

ALTER TABLE customer_profiles
    ADD COLUMN IF NOT EXISTS verification_signature_json TEXT;

ALTER TABLE customer_profiles
    ADD COLUMN IF NOT EXISTS verification_rejection_reason VARCHAR(500);
