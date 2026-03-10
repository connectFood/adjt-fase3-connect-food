CREATE TABLE IF NOT EXISTS "restaurant".restaurants (
  id          BIGSERIAL PRIMARY KEY,
  uuid        UUID NOT NULL DEFAULT gen_random_uuid(),
  name        VARCHAR(120) NOT NULL,
  description VARCHAR(512),
  active      BOOLEAN NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version     BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_restaurants_uuid UNIQUE (uuid)
);

CREATE TABLE IF NOT EXISTS "restaurant".menu_items (
  id              BIGSERIAL PRIMARY KEY,
  uuid            UUID NOT NULL DEFAULT gen_random_uuid(),
  restaurant_uuid UUID NOT NULL,
  item_code       VARCHAR(64) NOT NULL,
  name            VARCHAR(120) NOT NULL,
  description     VARCHAR(512),
  price           NUMERIC(12,2) NOT NULL,
  available       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version         BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_menu_items_uuid UNIQUE (uuid),
  CONSTRAINT uk_menu_items_restaurant_code UNIQUE (restaurant_uuid, item_code),
  CONSTRAINT fk_menu_items_restaurant_uuid FOREIGN KEY (restaurant_uuid)
  REFERENCES "restaurant".restaurants(uuid) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_menu_items_restaurant_uuid ON "restaurant".menu_items(restaurant_uuid);
