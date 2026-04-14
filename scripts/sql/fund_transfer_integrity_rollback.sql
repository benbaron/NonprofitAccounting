BEGIN;

DROP INDEX IF EXISTS ix_ft_repair_open;
DROP TABLE IF EXISTS fund_transfer_repair_queue;
DROP TABLE IF EXISTS fund_transfer_integrity_event;
DROP TABLE IF EXISTS fund_transfer_status_transition;

DROP INDEX IF EXISTS ix_fund_transfer_from_to_date;
DROP INDEX IF EXISTS ix_fund_transfer_status_date;

ALTER TABLE fund_transfer DROP CONSTRAINT IF EXISTS uq_fund_transfer_posted_txn;
ALTER TABLE fund_transfer DROP CONSTRAINT IF EXISTS chk_fund_transfer_posted_link_by_status;
ALTER TABLE fund_transfer DROP CONSTRAINT IF EXISTS chk_fund_transfer_status_domain;

COMMIT;
