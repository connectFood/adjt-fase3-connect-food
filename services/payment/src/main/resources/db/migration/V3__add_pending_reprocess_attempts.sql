ALTER TABLE payment.payment_transaction
  ADD COLUMN IF NOT EXISTS pending_reprocess_attempts INTEGER NOT NULL DEFAULT 0;
