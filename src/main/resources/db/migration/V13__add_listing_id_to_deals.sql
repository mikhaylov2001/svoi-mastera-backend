-- Привязка сделки к объявлению (для двустороннего подтверждения)
ALTER TABLE deals ADD COLUMN IF NOT EXISTS listing_id UUID;
