-- Add a parallel canonical transaction reference for reconciliation adjustments.
--
-- journal_txn_id remains the legacy journal write-ledger reference.
-- canonical_txn_id stores the synchronized canonical txn.id when available.

ALTER TABLE banking_transaction_record
ADD COLUMN IF NOT EXISTS canonical_txn_id BIGINT;
