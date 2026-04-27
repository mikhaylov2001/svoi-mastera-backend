-- text[] → TEXT (JSON), чтобы фото объявлений сохранялись через JPA @Convert без JDBC ARRAY
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
