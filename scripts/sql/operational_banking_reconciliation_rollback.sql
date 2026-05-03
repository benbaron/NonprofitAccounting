-- Prompt C: Operational Banking Reconciliation
-- Rollback migration (scaffold)

DROP INDEX IF EXISTS idx_banking_anomaly_queue;
DROP INDEX IF EXISTS idx_banking_open_items;
DROP INDEX IF EXISTS uq_banking_idempotent_fingerprint;
DROP INDEX IF EXISTS idx_bank_statement_bank_period;

ALTER TABLE ledger_record DROP CONSTRAINT IF EXISTS ck_ledger_link_status;
ALTER TABLE ledger_record DROP CONSTRAINT IF EXISTS ck_ledger_match_method;
ALTER TABLE banking_transaction_record DROP CONSTRAINT IF EXISTS ck_banking_match_status;
ALTER TABLE bank_statement DROP CONSTRAINT IF EXISTS ck_bank_statement_status;

-- Column removals are intentionally omitted in this first rollback scaffold
-- to reduce destructive risk on environments where data backfill has started.
