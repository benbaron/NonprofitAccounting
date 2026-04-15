-- Prompt H validation script
-- 1) orphan inventory
-- 2) validate constraints
-- 3) assert validated status

-- 1) Orphan inventory
SELECT 'fund_transfer.from_fund_id' AS relationship, COUNT(*) AS orphan_count
FROM fund_transfer ft
LEFT JOIN fund f ON f.id = ft.from_fund_id
WHERE ft.from_fund_id IS NOT NULL AND f.id IS NULL
UNION ALL
SELECT 'fund_transfer.to_fund_id', COUNT(*)
FROM fund_transfer ft
LEFT JOIN fund f ON f.id = ft.to_fund_id
WHERE ft.to_fund_id IS NOT NULL AND f.id IS NULL
UNION ALL
SELECT 'fund_transfer.posted_txn_id', COUNT(*)
FROM fund_transfer ft
LEFT JOIN txn t ON t.id = ft.posted_txn_id
WHERE ft.posted_txn_id IS NOT NULL AND t.id IS NULL
UNION ALL
SELECT 'account_alias.account_id', COUNT(*)
FROM account_alias aa
LEFT JOIN account a ON a.id = aa.account_id
WHERE aa.account_id IS NOT NULL AND a.id IS NULL
UNION ALL
SELECT 'fund_alias.fund_id', COUNT(*)
FROM fund_alias fa
LEFT JOIN fund f ON f.id = fa.fund_id
WHERE fa.fund_id IS NOT NULL AND f.id IS NULL
UNION ALL
SELECT 'account_report_section.account_id', COUNT(*)
FROM account_report_section ars
LEFT JOIN account a ON a.id = ars.account_id
WHERE ars.account_id IS NOT NULL AND a.id IS NULL
UNION ALL
SELECT 'account_report_section.report_section_id', COUNT(*)
FROM account_report_section ars
LEFT JOIN report_section rs ON rs.id = ars.report_section_id
WHERE ars.report_section_id IS NOT NULL AND rs.id IS NULL
UNION ALL
SELECT 'account_schedule_requirement.account_id', COUNT(*)
FROM account_schedule_requirement asr
LEFT JOIN account a ON a.id = asr.account_id
WHERE asr.account_id IS NOT NULL AND a.id IS NULL
UNION ALL
SELECT 'account_schedule_requirement.schedule_kind_id', COUNT(*)
FROM account_schedule_requirement asr
LEFT JOIN schedule_kind sk ON sk.id = asr.schedule_kind_id
WHERE asr.schedule_kind_id IS NOT NULL AND sk.id IS NULL
UNION ALL
SELECT 'account_subtype_schedule_default.schedule_kind_id', COUNT(*)
FROM account_subtype_schedule_default assd
LEFT JOIN schedule_kind sk ON sk.id = assd.schedule_kind_id
WHERE assd.schedule_kind_id IS NOT NULL AND sk.id IS NULL;

-- 2) Constraint validation
ALTER TABLE fund_transfer VALIDATE CONSTRAINT fk_fund_transfer_from_fund;
ALTER TABLE fund_transfer VALIDATE CONSTRAINT fk_fund_transfer_to_fund;
ALTER TABLE fund_transfer VALIDATE CONSTRAINT fk_fund_transfer_posted_txn;

ALTER TABLE account_alias VALIDATE CONSTRAINT fk_account_alias_account;
ALTER TABLE fund_alias VALIDATE CONSTRAINT fk_fund_alias_fund;

ALTER TABLE account_report_section VALIDATE CONSTRAINT fk_account_report_section_account;
ALTER TABLE account_report_section VALIDATE CONSTRAINT fk_account_report_section_report_section;

ALTER TABLE account_schedule_requirement VALIDATE CONSTRAINT fk_account_schedule_requirement_account;
ALTER TABLE account_schedule_requirement VALIDATE CONSTRAINT fk_account_schedule_requirement_schedule_kind;

ALTER TABLE account_subtype_schedule_default
  VALIDATE CONSTRAINT fk_account_subtype_schedule_default_schedule_kind;

-- 3) Validation status
SELECT conname, convalidated
FROM pg_constraint
WHERE conname IN (
  'fk_fund_transfer_from_fund',
  'fk_fund_transfer_to_fund',
  'fk_fund_transfer_posted_txn',
  'fk_account_alias_account',
  'fk_fund_alias_fund',
  'fk_account_report_section_account',
  'fk_account_report_section_report_section',
  'fk_account_schedule_requirement_account',
  'fk_account_schedule_requirement_schedule_kind',
  'fk_account_subtype_schedule_default_schedule_kind'
)
ORDER BY conname;
