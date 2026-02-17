CREATE TABLE IF NOT EXISTS payment.payment_transaction (
  id              BIGSERIAL PRIMARY KEY,
  uuid            UUID NOT NULL DEFAULT gen_random_uuid(),
  order_uuid      UUID NOT NULL,
  customer_uuid   UUID NOT NULL,
  status          VARCHAR(40) NOT NULL, -- APPROVED | PENDING | FAILED
  amount          NUMERIC(12,2) NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version         BIGINT NOT NULL DEFAULT 0,

  CONSTRAINT uk_payment_transaction_uuid UNIQUE (uuid),
  CONSTRAINT uk_payment_transaction_order_uuid UNIQUE (order_uuid)
);

CREATE INDEX IF NOT EXISTS idx_payment_transaction_customer_uuid
  ON payment.payment_transaction(customer_uuid);

CREATE INDEX IF NOT EXISTS idx_payment_transaction_status
  ON payment.payment_transaction(status);
