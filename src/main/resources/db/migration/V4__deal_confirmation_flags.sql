-- V4: Add dual confirmation flags to deals
ALTER TABLE deals ADD COLUMN IF NOT EXISTS customer_confirmed boolean NOT NULL DEFAULT false;
ALTER TABLE deals ADD COLUMN IF NOT EXISTS worker_confirmed boolean NOT NULL DEFAULT false;