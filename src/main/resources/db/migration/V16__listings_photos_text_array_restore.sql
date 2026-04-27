-- Вернуть колонку photos (text[]) после V15 (photos_json), чтобы Hibernate @JdbcTypeCode(ARRAY) работал.

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'listings' AND column_name = 'photos'
  ) THEN
    ALTER TABLE listings ADD COLUMN photos TEXT[];
  END IF;
END $$;

UPDATE listings l
SET photos = ARRAY(
  SELECT json_array_elements_text(trim(l.photos_json)::json)
)
WHERE EXISTS (
  SELECT 1 FROM information_schema.columns c
  WHERE c.table_schema = 'public' AND c.table_name = 'listings' AND c.column_name = 'photos_json'
)
  AND l.photos_json IS NOT NULL
  AND trim(l.photos_json) <> ''
  AND l.photos_json <> '[]'
  AND (l.photos IS NULL OR cardinality(l.photos) IS NULL OR cardinality(l.photos) = 0);

UPDATE listings l
SET photos = ARRAY[]::text[]
WHERE EXISTS (
  SELECT 1 FROM information_schema.columns c
  WHERE c.table_schema = 'public' AND c.table_name = 'listings' AND c.column_name = 'photos_json'
)
  AND (l.photos IS NULL OR cardinality(l.photos) IS NULL)
  AND (l.photos_json IS NULL OR trim(l.photos_json) = '' OR l.photos_json = '[]');

ALTER TABLE listings DROP COLUMN IF EXISTS photos_json;
