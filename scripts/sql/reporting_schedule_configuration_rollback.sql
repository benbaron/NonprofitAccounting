BEGIN;

DROP TABLE IF EXISTS config_change_event;

DROP INDEX IF EXISTS ix_srr_release_subtype_schedule_validity;
DROP INDEX IF EXISTS ix_srr_release_account_schedule_validity;
DROP INDEX IF EXISTS ix_srr_subject_lookup;
DROP TABLE IF EXISTS schedule_requirement_rule;

DROP INDEX IF EXISTS ix_asm_release_account_validity;
DROP INDEX IF EXISTS ix_asm_account_open;
DROP TABLE IF EXISTS account_statement_mapping;

DROP INDEX IF EXISTS ix_statement_section_release_type;
DROP TABLE IF EXISTS statement_section;

DROP TABLE IF EXISTS config_release;

COMMIT;
