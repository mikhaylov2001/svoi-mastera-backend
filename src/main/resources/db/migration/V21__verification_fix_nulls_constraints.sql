-- Hibernate ddl-auto=update мог добавить колонки верификации как nullable без DEFAULT,
-- затем попытка NOT NULL падает на существующих строках. Заполняем и фиксируем ограничения.

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'worker_profiles' AND column_name = 'verification_status'
  ) THEN
    UPDATE worker_profiles SET verification_status = 'NONE' WHERE verification_status IS NULL;
    ALTER TABLE worker_profiles ALTER COLUMN verification_status SET DEFAULT 'NONE';
    ALTER TABLE worker_profiles ALTER COLUMN verification_status SET NOT NULL;
  END IF;
END $$;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'customer_profiles' AND column_name = 'verified'
  ) THEN
    UPDATE customer_profiles SET verified = FALSE WHERE verified IS NULL;
    ALTER TABLE customer_profiles ALTER COLUMN verified SET DEFAULT FALSE;
    ALTER TABLE customer_profiles ALTER COLUMN verified SET NOT NULL;
  END IF;
END $$;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'customer_profiles' AND column_name = 'verification_status'
  ) THEN
    UPDATE customer_profiles SET verification_status = 'NONE' WHERE verification_status IS NULL;
    ALTER TABLE customer_profiles ALTER COLUMN verification_status SET DEFAULT 'NONE';
    ALTER TABLE customer_profiles ALTER COLUMN verification_status SET NOT NULL;
  END IF;
END $$;
