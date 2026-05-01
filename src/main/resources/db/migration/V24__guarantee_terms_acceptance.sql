-- Согласие с условиями программы гарантии сделок (после верификации профиля)

ALTER TABLE worker_profiles
    ADD COLUMN IF NOT EXISTS guarantee_terms_accepted_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE worker_profiles
    ADD COLUMN IF NOT EXISTS guarantee_terms_consent_json TEXT;

ALTER TABLE customer_profiles
    ADD COLUMN IF NOT EXISTS guarantee_terms_accepted_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE customer_profiles
    ADD COLUMN IF NOT EXISTS guarantee_terms_consent_json TEXT;
