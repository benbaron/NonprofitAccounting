-- Prompt H rollback: drops constraints created by logical_relationship_hardening_forward.sql

BEGIN;

ALTER TABLE IF EXISTS account_subtype_schedule_default
  DROP CONSTRAINT IF EXISTS fk_account_subtype_schedule_default_schedule_kind;

ALTER TABLE IF EXISTS account_schedule_requirement
  DROP CONSTRAINT IF EXISTS fk_account_schedule_requirement_schedule_kind,
  DROP CONSTRAINT IF EXISTS fk_account_schedule_requirement_account;

ALTER TABLE IF EXISTS account_report_section
  DROP CONSTRAINT IF EXISTS fk_account_report_section_report_section,
  DROP CONSTRAINT IF EXISTS fk_account_report_section_account;

ALTER TABLE IF EXISTS fund_alias
  DROP CONSTRAINT IF EXISTS fk_fund_alias_fund;

ALTER TABLE IF EXISTS account_alias
  DROP CONSTRAINT IF EXISTS fk_account_alias_account;

ALTER TABLE IF EXISTS fund_transfer
  DROP CONSTRAINT IF EXISTS fk_fund_transfer_posted_txn,
  DROP CONSTRAINT IF EXISTS fk_fund_transfer_to_fund,
  DROP CONSTRAINT IF EXISTS fk_fund_transfer_from_fund;

-- Intentionally keep indexes for performance and safe re-rollout.
-- If full rollback is required, drop indexes explicitly.

COMMIT;
