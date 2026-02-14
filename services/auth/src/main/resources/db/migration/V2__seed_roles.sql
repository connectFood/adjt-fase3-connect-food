INSERT INTO auth.roles (name, description)
VALUES
  ('ADMIN', 'Administrator'),
  ('CUSTOMER', 'Customer'),
  ('RESTAURANT_OWNER', 'Restaurant owner')
  ON CONFLICT (name) DO NOTHING;
