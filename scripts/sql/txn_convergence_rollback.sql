BEGIN;

DROP VIEW IF EXISTS v_journal_entry;
DROP VIEW IF EXISTS v_journal_transaction;

DROP INDEX IF EXISTS ix_fund_transfer_posted_txn;
DROP INDEX IF EXISTS ix_split_fund_txn;
DROP INDEX IF EXISTS ix_split_account_fund;
DROP INDEX IF EXISTS ix_split_txn_amount;
DROP INDEX IF EXISTS ix_txn_bank_date;
DROP INDEX IF EXISTS ix_txn_date_id;

ALTER TABLE fund_transfer DROP CONSTRAINT IF EXISTS fk_fund_transfer_posted_txn;
ALTER TABLE fund_transfer DROP CONSTRAINT IF EXISTS fk_fund_transfer_to_fund;
ALTER TABLE fund_transfer DROP CONSTRAINT IF EXISTS fk_fund_transfer_from_fund;
ALTER TABLE fund_transfer DROP CONSTRAINT IF EXISTS chk_fund_transfer_distinct_funds;
ALTER TABLE fund_transfer DROP CONSTRAINT IF EXISTS chk_fund_transfer_amount_positive;
ALTER TABLE txn_split DROP CONSTRAINT IF EXISTS chk_txn_split_amount_scale;
ALTER TABLE txn_split DROP CONSTRAINT IF EXISTS chk_txn_split_amount_nonzero;

DROP TABLE IF EXISTS legacy_txn_map;
DROP TABLE IF EXISTS migration_control;

COMMIT;
