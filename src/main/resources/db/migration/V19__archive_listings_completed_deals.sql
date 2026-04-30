-- Объявления с завершённой сделкой не должны оставаться в каталоге как активные
UPDATE listings l
SET active = false
WHERE l.active = true
  AND EXISTS (
    SELECT 1 FROM deals d
    WHERE d.listing_id = l.id
      AND d.status = 'COMPLETED'
  );
