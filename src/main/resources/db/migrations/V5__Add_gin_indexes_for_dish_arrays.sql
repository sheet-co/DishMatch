-- Speeds up array containment/overlap lookups (@>, &&) on dishes.tags and dishes.ingredients,
-- e.g. matching dishes by a set of requested tags or ingredients on hand. Without these, such
-- queries fall back to a sequential scan over every row's array.
CREATE INDEX idx_dishes_tags_gin ON dishes USING GIN (tags);
CREATE INDEX idx_dishes_ingredients_gin ON dishes USING GIN (ingredients);
