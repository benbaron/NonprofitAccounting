-- Prompt G: Grant/Donor/Program Traceability
-- Rollback migration (H2-compatible)

DROP VIEW IF EXISTS v_grant_restriction_reporting;

DROP INDEX IF EXISTS ix_grant_posting_entry;
DROP INDEX IF EXISTS ix_grant_posting_split;
DROP INDEX IF EXISTS ix_grant_posting_grant_role;

DROP TABLE IF EXISTS grant_posting_link;

DROP INDEX IF EXISTS uq_grant_record_reference_number;
DROP INDEX IF EXISTS ix_grant_record_counterparty;
DROP INDEX IF EXISTS ix_grant_record_activity;
DROP INDEX IF EXISTS ix_grant_record_restriction_fund;
DROP INDEX IF EXISTS ix_grant_record_reporting_due;

ALTER TABLE grant_record DROP CONSTRAINT IF EXISTS fk_grant_record_canonical_txn;
ALTER TABLE grant_record DROP CONSTRAINT IF EXISTS fk_grant_record_legacy_txn;
ALTER TABLE grant_record DROP CONSTRAINT IF EXISTS fk_grant_record_activity;
ALTER TABLE grant_record DROP CONSTRAINT IF EXISTS fk_grant_record_fund;
ALTER TABLE grant_record DROP CONSTRAINT IF EXISTS fk_grant_record_counterparty;
ALTER TABLE grant_record DROP CONSTRAINT IF EXISTS fk_grant_record_contact_person;
ALTER TABLE grant_record DROP CONSTRAINT IF EXISTS fk_grant_record_person;
ALTER TABLE grant_record DROP CONSTRAINT IF EXISTS fk_grant_record_donor;
ALTER TABLE grant_record DROP CONSTRAINT IF EXISTS ck_grant_record_contact_presence;
ALTER TABLE grant_record DROP CONSTRAINT IF EXISTS ck_grant_record_award_amount_nonnegative;
ALTER TABLE grant_record DROP CONSTRAINT IF EXISTS ck_grant_record_period_order;
ALTER TABLE grant_record DROP CONSTRAINT IF EXISTS ck_grant_record_compliance_status;
ALTER TABLE grant_record DROP CONSTRAINT IF EXISTS ck_grant_record_restriction_class;

ALTER TABLE grant_record DROP COLUMN IF EXISTS compliance_notes;
ALTER TABLE grant_record DROP COLUMN IF EXISTS grant_reference_number;
ALTER TABLE grant_record DROP COLUMN IF EXISTS contact_person_id;
ALTER TABLE grant_record DROP COLUMN IF EXISTS counterparty_id;
ALTER TABLE grant_record DROP COLUMN IF EXISTS activity_id;
ALTER TABLE grant_record DROP COLUMN IF EXISTS canonical_txn_id;
ALTER TABLE grant_record DROP COLUMN IF EXISTS closeout_date;
ALTER TABLE grant_record DROP COLUMN IF EXISTS next_report_due;
ALTER TABLE grant_record DROP COLUMN IF EXISTS reporting_frequency;
ALTER TABLE grant_record DROP COLUMN IF EXISTS compliance_status;
ALTER TABLE grant_record DROP COLUMN IF EXISTS restriction_release_rule;
ALTER TABLE grant_record DROP COLUMN IF EXISTS restriction_class;
ALTER TABLE grant_record DROP COLUMN IF EXISTS period_end;
ALTER TABLE grant_record DROP COLUMN IF EXISTS period_start;
ALTER TABLE grant_record DROP COLUMN IF EXISTS award_date;
