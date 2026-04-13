BEGIN;

DROP INDEX IF EXISTS ix_fund_alias_fund_active;
DROP INDEX IF EXISTS uq_fund_alias_norm_active;
ALTER TABLE fund_alias DROP CONSTRAINT IF EXISTS fk_fund_alias_fund;
ALTER TABLE fund_alias DROP CONSTRAINT IF EXISTS chk_fund_alias_text_not_blank;

DROP INDEX IF EXISTS ix_account_alias_account_active;
DROP INDEX IF EXISTS uq_account_alias_norm_active;
ALTER TABLE account_alias DROP CONSTRAINT IF EXISTS fk_account_alias_account;
ALTER TABLE account_alias DROP CONSTRAINT IF EXISTS chk_account_alias_text_not_blank;

DROP INDEX IF EXISTS uq_fund_name_norm_active;
DROP INDEX IF EXISTS uq_fund_code_norm_active;
ALTER TABLE fund DROP CONSTRAINT IF EXISTS chk_fund_active_date_range;
ALTER TABLE fund DROP CONSTRAINT IF EXISTS chk_fund_name_not_blank;
ALTER TABLE fund DROP CONSTRAINT IF EXISTS chk_fund_code_not_blank;

DROP INDEX IF EXISTS uq_account_code_norm_active;
DROP INDEX IF EXISTS uq_account_number_norm_active;
ALTER TABLE account DROP CONSTRAINT IF EXISTS chk_account_active_date_range;
ALTER TABLE account DROP CONSTRAINT IF EXISTS chk_account_code_not_blank;
ALTER TABLE account DROP CONSTRAINT IF EXISTS chk_account_name_not_blank;
ALTER TABLE account DROP CONSTRAINT IF EXISTS chk_account_number_not_blank;

DROP TABLE IF EXISTS alias_review_queue;

COMMIT;
