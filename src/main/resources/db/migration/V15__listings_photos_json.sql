-- Перенос photos TEXT[] -> photos_json TEXT (промежуточный шаг; V16 возвращает native text[] для Hibernate ARRAY)
ALTER TABLE listings ADD COLUMN IF NOT EXISTS photos_json TEXT;

UPDATE listings l
SET photos_json = COALESCE(
  (SELECT json_agg(p)::text FROM unnest(l.photos) AS p),
  '[]'
)
WHERE EXISTS (
  SELECT 1 FROM information_schema.columns c
  WHERE c.table_schema = 'public' AND c.table_name = 'listings' AND c.column_name = 'photos'
);

UPDATE listings SET photos_json = '[]' WHERE photos_json IS NULL;

ALTER TABLE listings DROP COLUMN IF EXISTS photos;
