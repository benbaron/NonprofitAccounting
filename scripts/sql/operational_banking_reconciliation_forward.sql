-- Prompt C: Operational Banking Reconciliation
-- Forward migration (initial implementation scaffold)

-- 1) bank_statement controls
ALTER TABLE bank_statement
  ADD COLUMN IF NOT EXISTS bank_id_record_id VARCHAR(255);
ALTER TABLE bank_statement
  ADD COLUMN IF NOT EXISTS period_start DATE;
ALTER TABLE bank_statement
  ADD COLUMN IF NOT EXISTS period_end DATE;
ALTER TABLE bank_statement
  ADD COLUMN IF NOT EXISTS status VARCHAR(24) DEFAULT 'OPEN' NOT NULL;
ALTER TABLE bank_statement
  ADD COLUMN IF NOT EXISTS imported_at TIMESTAMP;
ALTER TABLE bank_statement
  ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP;
ALTER TABLE bank_statement
  ADD COLUMN IF NOT EXISTS retention_until DATE;

ALTER TABLE bank_statement
  ADD CONSTRAINT IF NOT EXISTS ck_bank_statement_status
  CHECK (status IN ('OPEN','IN_REVIEW','CLOSED','LOCKED'));

CREATE INDEX IF NOT EXISTS idx_bank_statement_bank_period
  ON bank_statement(bank_id_record_id, period_start, period_end);

-- 2) banking_transaction_record matching/idempotency/anomaly fields
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS import_batch_id VARCHAR(128);
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS source_system VARCHAR(64);
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS source_fingerprint VARCHAR(128);
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS normalized_description VARCHAR(512);
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS external_transaction_id VARCHAR(255);
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS match_status VARCHAR(24) DEFAULT 'NEW' NOT NULL;
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS matched_at TIMESTAMP;
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS duplicate_seen_count INT DEFAULT 0 NOT NULL;
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS last_seen_batch_id VARCHAR(128);
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS anomaly_duplicate BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS anomaly_amount_outlier BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS anomaly_date_outlier BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS anomaly_reason VARCHAR(512);
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS supersedes_banking_record_id VARCHAR(255);

ALTER TABLE banking_transaction_record
  ADD CONSTRAINT IF NOT EXISTS ck_banking_match_status
  CHECK (match_status IN ('NEW','UNMATCHED','AUTO_MATCHED','MATCH_CONFIRMED','RECONCILED','DUPLICATE','ADJUSTED','STALE_UNMATCHED'));

CREATE UNIQUE INDEX IF NOT EXISTS uq_banking_idempotent_fingerprint
  ON banking_transaction_record(bank_id_record_id, source_fingerprint);

CREATE INDEX IF NOT EXISTS idx_banking_open_items
  ON banking_transaction_record(bank_id_record_id, match_status, transaction_date);

CREATE INDEX IF NOT EXISTS idx_banking_anomaly_queue
  ON banking_transaction_record(bank_id_record_id, anomaly_duplicate, anomaly_amount_outlier, anomaly_date_outlier);

-- 3) ledger_record match metadata
ALTER TABLE ledger_record
  ADD COLUMN IF NOT EXISTS match_group_id VARCHAR(64);
ALTER TABLE ledger_record
  ADD COLUMN IF NOT EXISTS match_method VARCHAR(24);
ALTER TABLE ledger_record
  ADD COLUMN IF NOT EXISTS reviewer_user VARCHAR(128);
ALTER TABLE ledger_record
  ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;
ALTER TABLE ledger_record
  ADD COLUMN IF NOT EXISTS link_status VARCHAR(24) DEFAULT 'ACTIVE' NOT NULL;

ALTER TABLE ledger_record
  ADD CONSTRAINT IF NOT EXISTS ck_ledger_match_method
  CHECK (match_method IN ('AUTO','MANUAL','RULE','IMPORT_REPLAY') OR match_method IS NULL);

ALTER TABLE ledger_record
  ADD CONSTRAINT IF NOT EXISTS ck_ledger_link_status
  CHECK (link_status IN ('ACTIVE','VOIDED','SUPERSEDED'));
