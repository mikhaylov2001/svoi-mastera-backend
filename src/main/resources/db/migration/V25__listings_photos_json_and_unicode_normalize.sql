-- V25: Нормализация listings.photos в JSON-массив (TEXT) + NFKC для текстов профилей/карточек.
-- Исправляет пустые фото на карточках, если в БД остался формат PostgreSQL text[] как строка "{...}"
-- или одно значение URL без JSON-обёртки.

-- ── 1. listings.photos: тип TEXT (если ещё ARRAY после частичного деплоя) ──
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns c
    WHERE c.table_schema = 'public' AND c.table_name = 'listings' AND c.column_name = 'photos'
      AND c.data_type = 'ARRAY'
  ) THEN
    ALTER TABLE listings ALTER COLUMN photos TYPE TEXT USING (
      CASE WHEN photos IS NULL THEN '[]' ELSE to_json(photos)::text END
    );
  END IF;
END $$;

-- ── 2. Строка вида PostgreSQL array literal "{a,b}" → JSON ["a","b"] ──
DO $$
DECLARE
  r RECORD;
  new_p TEXT;
BEGIN
  FOR r IN
    SELECT id, photos AS p
    FROM listings
    WHERE photos IS NOT NULL
      AND btrim(photos) <> ''
      AND btrim(photos) <> '[]'
      AND photos ~ '^\s*\{'
      AND photos !~ '^\s*\['
  LOOP
    BEGIN
      new_p := (
        SELECT COALESCE(json_agg(btrim(elem) ORDER BY ord)::text, '[]')
        FROM unnest(r.p::text[]) WITH ORDINALITY AS t(elem, ord)
        WHERE elem IS NOT NULL AND btrim(elem) <> ''
      );
      UPDATE listings SET photos = new_p WHERE id = r.id;
    EXCEPTION
      WHEN OTHERS THEN
        RAISE NOTICE 'V25 listings photos skip id=%: %', r.id, SQLERRM;
    END;
  END LOOP;
END $$;

-- ── 3. Одно значение URL / data:image без скобок и без JSON ──
UPDATE listings
SET photos = json_build_array(btrim(photos))::text
WHERE photos IS NOT NULL
  AND btrim(photos) <> ''
  AND btrim(photos) NOT LIKE '[%'
  AND btrim(photos) NOT LIKE '{%'
  AND (
    btrim(photos) LIKE 'http://%'
    OR btrim(photos) LIKE 'https://%'
    OR btrim(photos) LIKE 'data:image/%'
  );

-- Пустые / мусор после ошибок парсинга на фронте — не трогаем произвольный текст

-- ── 4. Unicode NFKC: «старые» совместимые символы и эмодзи-представления ──
UPDATE worker_profiles
SET display_name = normalize(display_name, NFKC),
    about = CASE WHEN about IS NULL THEN NULL ELSE normalize(about, NFKC) END,
    city = CASE WHEN city IS NULL THEN NULL ELSE normalize(city, NFKC) END,
    last_name = CASE WHEN last_name IS NULL THEN NULL ELSE normalize(last_name, NFKC) END
WHERE display_name IS NOT NULL OR about IS NOT NULL OR city IS NOT NULL OR last_name IS NOT NULL;

UPDATE customer_profiles
SET display_name = normalize(display_name, NFKC),
    city = CASE WHEN city IS NULL THEN NULL ELSE normalize(city, NFKC) END,
    last_name = CASE WHEN last_name IS NULL THEN NULL ELSE normalize(last_name, NFKC) END
WHERE display_name IS NOT NULL OR city IS NOT NULL OR last_name IS NOT NULL;

UPDATE listings
SET title = normalize(title, NFKC),
    description = CASE WHEN description IS NULL OR description = '' THEN description ELSE normalize(description, NFKC) END
WHERE title IS NOT NULL;

UPDATE job_requests
SET title = normalize(title, NFKC),
    description = normalize(description, NFKC),
    address_text = CASE WHEN address_text IS NULL THEN NULL ELSE normalize(address_text, NFKC) END,
    city = CASE WHEN city IS NULL THEN NULL ELSE normalize(city, NFKC) END
WHERE title IS NOT NULL OR description IS NOT NULL;

UPDATE reviews
SET text = normalize(text, NFKC)
WHERE text IS NOT NULL;

UPDATE messages
SET text = normalize(text, NFKC)
WHERE text IS NOT NULL;

UPDATE notifications
SET title = normalize(title, NFKC),
    body = CASE WHEN body IS NULL THEN NULL ELSE normalize(body, NFKC) END
WHERE title IS NOT NULL OR body IS NOT NULL;

UPDATE worker_services
SET title = normalize(title, NFKC),
    description = CASE WHEN description IS NULL THEN NULL ELSE normalize(description, NFKC) END
WHERE title IS NOT NULL OR description IS NOT NULL;
