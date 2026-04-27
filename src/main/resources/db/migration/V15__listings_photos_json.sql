-- Migrate photos from TEXT[] to TEXT (JSON array string)
ALTER TABLE listings ADD COLUMN IF NOT EXISTS photos_json TEXT;

UPDATE listings
SET photos_json = CASE
    WHEN photos IS NULL OR array_length(photos, 1) IS NULL THEN '[]'
    ELSE (
        SELECT '[' || string_agg('"' || replace(replace(p, '\', '\\'), '"', '\"') || '"', ',') || ']'
        FROM unnest(photos) AS p
    )
END;

ALTER TABLE listings DROP COLUMN IF EXISTS photos;
