ALTER TABLE auth.users
ADD COLUMN IF NOT EXISTS name VARCHAR(150);

UPDATE auth.users
SET name = split_part(email, '@', 1)
WHERE name IS NULL;

ALTER TABLE auth.users
ALTER COLUMN name SET NOT NULL;
