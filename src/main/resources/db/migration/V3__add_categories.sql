-- V3: Add all 9 service categories

INSERT INTO categories (id, created_at, updated_at, name, slug, active)
SELECT gen_random_uuid(), now(), now(), n, s, true
FROM (VALUES
          ('Ремонт квартир', 'remont-kvartir'),
          ('Уборка', 'uborka'),
          ('Парикмахер', 'parikhmaher'),
          ('Маникюр и педикюр', 'manikur'),
          ('Красота и здоровье', 'krasota-i-zdorovie'),
          ('Репетиторство', 'repetitorstvo'),
          ('Компьютерная помощь', 'kompyuternaya-pomosh')
     ) AS t(n, s)
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = s);