-- Prompt H: Logical Relationship Hardening
-- PostgreSQL-oriented migration. Uses NOT VALID to enforce new writes while deferring full-table validation.

BEGIN;

-- ============================================================================
-- Phase 1 (P0/P1): add supporting indexes for FK checks
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_fund_transfer_from_fund_id ON fund_transfer (from_fund_id);
CREATE INDEX IF NOT EXISTS idx_fund_transfer_to_fund_id ON fund_transfer (to_fund_id);
CREATE INDEX IF NOT EXISTS idx_fund_transfer_posted_txn_id ON fund_transfer (posted_txn_id);

CREATE INDEX IF NOT EXISTS idx_account_alias_account_id ON account_alias (account_id);
CREATE INDEX IF NOT EXISTS idx_fund_alias_fund_id ON fund_alias (fund_id);

CREATE INDEX IF NOT EXISTS idx_account_report_section_account_id ON account_report_section (account_id);
CREATE INDEX IF NOT EXISTS idx_account_report_section_report_section_id ON account_report_section (report_section_id);

CREATE INDEX IF NOT EXISTS idx_account_schedule_requirement_account_id ON account_schedule_requirement (account_id);
CREATE INDEX IF NOT EXISTS idx_account_schedule_requirement_schedule_kind_id ON account_schedule_requirement (schedule_kind_id);

CREATE INDEX IF NOT EXISTS idx_account_subtype_schedule_default_schedule_kind_id
  ON account_subtype_schedule_default (schedule_kind_id);

-- ============================================================================
-- Phase 1 (P0/P1): add NOT VALID constraints idempotently
-- ============================================================================
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_fund_transfer_from_fund'
  ) THEN
    ALTER TABLE fund_transfer
      ADD CONSTRAINT fk_fund_transfer_from_fund
      FOREIGN KEY (from_fund_id) REFERENCES fund(id)
      NOT VALID;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_fund_transfer_to_fund'
  ) THEN
    ALTER TABLE fund_transfer
      ADD CONSTRAINT fk_fund_transfer_to_fund
      FOREIGN KEY (to_fund_id) REFERENCES fund(id)
      NOT VALID;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_fund_transfer_posted_txn'
  ) THEN
    ALTER TABLE fund_transfer
      ADD CONSTRAINT fk_fund_transfer_posted_txn
      FOREIGN KEY (posted_txn_id) REFERENCES txn(id)
      NOT VALID;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_account_alias_account'
  ) THEN
    ALTER TABLE account_alias
      ADD CONSTRAINT fk_account_alias_account
      FOREIGN KEY (account_id) REFERENCES account(id)
      NOT VALID;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_fund_alias_fund'
  ) THEN
    ALTER TABLE fund_alias
      ADD CONSTRAINT fk_fund_alias_fund
      FOREIGN KEY (fund_id) REFERENCES fund(id)
      NOT VALID;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_account_report_section_account'
  ) THEN
    ALTER TABLE account_report_section
      ADD CONSTRAINT fk_account_report_section_account
      FOREIGN KEY (account_id) REFERENCES account(id)
      NOT VALID;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_account_report_section_report_section'
  ) THEN
    ALTER TABLE account_report_section
      ADD CONSTRAINT fk_account_report_section_report_section
      FOREIGN KEY (report_section_id) REFERENCES report_section(id)
      NOT VALID;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_account_schedule_requirement_account'
  ) THEN
    ALTER TABLE account_schedule_requirement
      ADD CONSTRAINT fk_account_schedule_requirement_account
      FOREIGN KEY (account_id) REFERENCES account(id)
      NOT VALID;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_account_schedule_requirement_schedule_kind'
  ) THEN
    ALTER TABLE account_schedule_requirement
      ADD CONSTRAINT fk_account_schedule_requirement_schedule_kind
      FOREIGN KEY (schedule_kind_id) REFERENCES schedule_kind(id)
      NOT VALID;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_account_subtype_schedule_default_schedule_kind'
  ) THEN
    ALTER TABLE account_subtype_schedule_default
      ADD CONSTRAINT fk_account_subtype_schedule_default_schedule_kind
      FOREIGN KEY (schedule_kind_id) REFERENCES schedule_kind(id)
      NOT VALID;
  END IF;
END $$;

COMMIT;
