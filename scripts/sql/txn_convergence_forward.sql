BEGIN;

CREATE TABLE IF NOT EXISTS migration_control (
  migration_name      VARCHAR(120) PRIMARY KEY,
  phase               VARCHAR(40) NOT NULL,
  started_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at        TIMESTAMP,
  status              VARCHAR(20) NOT NULL,
  notes               VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS legacy_txn_map (
  legacy_txn_id       BIGINT PRIMARY KEY,
  canonical_txn_id    BIGINT NOT NULL,
  migrated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  checksum            VARCHAR(128),
  CONSTRAINT fk_legacy_map_txn
    FOREIGN KEY (canonical_txn_id) REFERENCES txn(id) ON DELETE CASCADE,
  CONSTRAINT uq_legacy_map_canonical UNIQUE (canonical_txn_id)
);

ALTER TABLE txn_split
  ADD CONSTRAINT IF NOT EXISTS chk_txn_split_amount_nonzero
  CHECK (amount_signed <> 0);

ALTER TABLE txn_split
  ADD CONSTRAINT IF NOT EXISTS chk_txn_split_amount_scale
  CHECK (amount_signed = ROUND(amount_signed, 2));

ALTER TABLE fund_transfer
  ADD CONSTRAINT IF NOT EXISTS chk_fund_transfer_amount_positive
  CHECK (amount > 0);

ALTER TABLE fund_transfer
  ADD CONSTRAINT IF NOT EXISTS chk_fund_transfer_distinct_funds
  CHECK (from_fund_id <> to_fund_id);

ALTER TABLE fund_transfer
  ADD CONSTRAINT IF NOT EXISTS fk_fund_transfer_from_fund
  FOREIGN KEY (from_fund_id) REFERENCES fund(id);

ALTER TABLE fund_transfer
  ADD CONSTRAINT IF NOT EXISTS fk_fund_transfer_to_fund
  FOREIGN KEY (to_fund_id) REFERENCES fund(id);

ALTER TABLE fund_transfer
  ADD CONSTRAINT IF NOT EXISTS fk_fund_transfer_posted_txn
  FOREIGN KEY (posted_txn_id) REFERENCES txn(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS ix_txn_date_id ON txn(txn_date, id);
CREATE INDEX IF NOT EXISTS ix_txn_bank_date ON txn(bank_account_id, txn_date);
CREATE INDEX IF NOT EXISTS ix_split_txn_amount ON txn_split(txn_id, amount_signed);
CREATE INDEX IF NOT EXISTS ix_split_account_fund ON txn_split(account_id, fund_id);
CREATE INDEX IF NOT EXISTS ix_split_fund_txn ON txn_split(fund_id, txn_id);
CREATE INDEX IF NOT EXISTS ix_fund_transfer_posted_txn ON fund_transfer(posted_txn_id);

INSERT INTO legacy_txn_map(legacy_txn_id, canonical_txn_id, checksum)
SELECT jt.id, t.id, NULL
FROM journal_transaction jt
JOIN txn t ON t.id = jt.id
LEFT JOIN legacy_txn_map m ON m.legacy_txn_id = jt.id
WHERE m.legacy_txn_id IS NULL;

CREATE OR REPLACE VIEW v_journal_transaction AS
SELECT
  COALESCE(m.legacy_txn_id, t.id) AS id,
  EXTRACT(EPOCH FROM t.created_at) * 1000 AS booking_ts,
  CAST(t.txn_date AS VARCHAR(32)) AS date_text,
  t.memo AS memo,
  cp.display_name AS to_from,
  CAST(NULL AS VARCHAR(64)) AS check_number,
  CAST(NULL AS VARCHAR(64)) AS clear_bank,
  CAST(NULL AS VARCHAR(128)) AS bank_name,
  FALSE AS reconciled,
  CAST(NULL AS VARCHAR(512)) AS budget_tracking,
  f.name AS associated_fund_name
FROM txn t
LEFT JOIN legacy_txn_map m ON m.canonical_txn_id = t.id
LEFT JOIN counterparty cp ON cp.id = t.payee_id
LEFT JOIN (
  SELECT ts.txn_id, MIN(ts.fund_id) AS fund_id
  FROM txn_split ts
  GROUP BY ts.txn_id
) tf ON tf.txn_id = t.id
LEFT JOIN fund f ON f.id = tf.fund_id;

CREATE OR REPLACE VIEW v_journal_entry AS
SELECT
  ts.id AS id,
  COALESCE(m.legacy_txn_id, t.id) AS txn_id,
  ABS(ts.amount_signed) AS amount,
  a.account_number AS account_number,
  CASE WHEN ts.amount_signed < 0 THEN 'CREDIT' ELSE 'DEBIT' END AS account_side,
  a.name AS account_name,
  f.code AS fund_number
FROM txn_split ts
JOIN txn t ON t.id = ts.txn_id
JOIN account a ON a.id = ts.account_id
JOIN fund f ON f.id = ts.fund_id
LEFT JOIN legacy_txn_map m ON m.canonical_txn_id = t.id;

COMMIT;
