CREATE TABLE IF NOT EXISTS "order".orders (
  id            BIGSERIAL PRIMARY KEY,
  uuid          UUID NOT NULL DEFAULT gen_random_uuid(),
  customer_uuid UUID NOT NULL,
  restaurant_id VARCHAR(64) NOT NULL,
  status        VARCHAR(40) NOT NULL,
  total_amount  NUMERIC(12,2) NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version       BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_orders_uuid UNIQUE (uuid)
);

CREATE TABLE IF NOT EXISTS "order".order_items (
  id          BIGSERIAL PRIMARY KEY,
  order_id    BIGINT NOT NULL,
  item_id     VARCHAR(64) NOT NULL,
  item_name   VARCHAR(255) NOT NULL,
  quantity    INT NOT NULL,
  unit_price  NUMERIC(12,2) NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
  REFERENCES "order".orders(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_orders_customer_uuid ON "order".orders(customer_uuid);
CREATE INDEX IF NOT EXISTS idx_orders_status ON "order".orders(status);
