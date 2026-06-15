-- Move banking/reconciliation ledger column ownership from Database.ensureSchema() to Flyway.

ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS banking_record_id VARCHAR(255);
ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS match_group_id VARCHAR(64);
ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS match_method VARCHAR(24);
ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS reviewer_user VARCHAR(128);
ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;
ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS link_status VARCHAR(24) DEFAULT 'ACTIVE' NOT NULL;

ALTER TABLE ledger_record ADD CONSTRAINT IF NOT EXISTS ck_ledger_match_method
  CHECK (match_method IN ('AUTO','MANUAL','RULE','IMPORT_REPLAY') OR match_method IS NULL);
ALTER TABLE ledger_record ADD CONSTRAINT IF NOT EXISTS ck_ledger_link_status
  CHECK (link_status IN ('ACTIVE','VOIDED','SUPERSEDED'));
ALTER TABLE ledger_record ADD CONSTRAINT IF NOT EXISTS fk_ledger_banking_record
  FOREIGN KEY (banking_record_id) REFERENCES banking_transaction_record(banking_record_id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_banking_anomaly_queue
  ON banking_transaction_record(bank_id_record_id, anomaly_duplicate, anomaly_amount_outlier, anomaly_date_outlier);
CREATE INDEX IF NOT EXISTS idx_ledger_match_group ON ledger_record(match_group_id);
CREATE INDEX IF NOT EXISTS idx_ledger_banking_active ON ledger_record(banking_record_id, link_status);
