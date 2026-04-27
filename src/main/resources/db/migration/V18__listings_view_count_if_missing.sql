-- На части окружений V14 не применился; колонка нужна сущности Listing.
ALTER TABLE listings ADD COLUMN IF NOT EXISTS view_count INTEGER NOT NULL DEFAULT 0;
