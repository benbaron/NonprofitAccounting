-- Prompt H preflight checks (PostgreSQL)
-- Purpose: verify schema shape and identify migration drift before applying forward migration.

-- 1) Required table/column presence
WITH required AS (
  SELECT * FROM (VALUES
    ('fund_transfer','from_fund_id'),
    ('fund_transfer','to_fund_id'),
    ('fund_transfer','posted_txn_id'),
    ('fund','id'),
    ('txn','id'),
    ('account_alias','account_id'),
    ('account','id'),
    ('fund_alias','fund_id'),
    ('account_report_section','account_id'),
    ('account_report_section','report_section_id'),
    ('report_section','id'),
    ('account_schedule_requirement','account_id'),
    ('account_schedule_requirement','schedule_kind_id'),
    ('schedule_kind','id'),
    ('account_subtype_schedule_default','schedule_kind_id')
  ) AS t(table_name, column_name)
)
SELECT r.table_name, r.column_name,
       CASE WHEN c.column_name IS NULL THEN 'MISSING' ELSE 'OK' END AS status
FROM required r
LEFT JOIN information_schema.columns c
  ON c.table_schema = current_schema()
 AND c.table_name = r.table_name
 AND c.column_name = r.column_name
ORDER BY r.table_name, r.column_name;

-- 2) Type compatibility checks (child vs parent)
SELECT 'fund_transfer.from_fund_id -> fund.id' AS relationship,
       format_type(c_child.atttypid, c_child.atttypmod) AS child_type,
       format_type(c_parent.atttypid, c_parent.atttypmod) AS parent_type,
       CASE WHEN c_child.atttypid = c_parent.atttypid THEN 'COMPATIBLE' ELSE 'MISMATCH' END AS status
FROM pg_attribute c_child
JOIN pg_class t_child ON t_child.oid = c_child.attrelid
JOIN pg_namespace n_child ON n_child.oid = t_child.relnamespace
JOIN pg_attribute c_parent ON TRUE
JOIN pg_class t_parent ON t_parent.oid = c_parent.attrelid
JOIN pg_namespace n_parent ON n_parent.oid = t_parent.relnamespace
WHERE n_child.nspname = current_schema()
  AND t_child.relname = 'fund_transfer'
  AND c_child.attname = 'from_fund_id'
  AND n_parent.nspname = current_schema()
  AND t_parent.relname = 'fund'
  AND c_parent.attname = 'id'
UNION ALL
SELECT 'fund_transfer.to_fund_id -> fund.id',
       format_type(c_child.atttypid, c_child.atttypmod),
       format_type(c_parent.atttypid, c_parent.atttypmod),
       CASE WHEN c_child.atttypid = c_parent.atttypid THEN 'COMPATIBLE' ELSE 'MISMATCH' END
FROM pg_attribute c_child
JOIN pg_class t_child ON t_child.oid = c_child.attrelid
JOIN pg_namespace n_child ON n_child.oid = t_child.relnamespace
JOIN pg_attribute c_parent ON TRUE
JOIN pg_class t_parent ON t_parent.oid = c_parent.attrelid
JOIN pg_namespace n_parent ON n_parent.oid = t_parent.relnamespace
WHERE n_child.nspname = current_schema()
  AND t_child.relname = 'fund_transfer'
  AND c_child.attname = 'to_fund_id'
  AND n_parent.nspname = current_schema()
  AND t_parent.relname = 'fund'
  AND c_parent.attname = 'id'
UNION ALL
SELECT 'fund_transfer.posted_txn_id -> txn.id',
       format_type(c_child.atttypid, c_child.atttypmod),
       format_type(c_parent.atttypid, c_parent.atttypmod),
       CASE WHEN c_child.atttypid = c_parent.atttypid THEN 'COMPATIBLE' ELSE 'MISMATCH' END
FROM pg_attribute c_child
JOIN pg_class t_child ON t_child.oid = c_child.attrelid
JOIN pg_namespace n_child ON n_child.oid = t_child.relnamespace
JOIN pg_attribute c_parent ON TRUE
JOIN pg_class t_parent ON t_parent.oid = c_parent.attrelid
JOIN pg_namespace n_parent ON n_parent.oid = t_parent.relnamespace
WHERE n_child.nspname = current_schema()
  AND t_child.relname = 'fund_transfer'
  AND c_child.attname = 'posted_txn_id'
  AND n_parent.nspname = current_schema()
  AND t_parent.relname = 'txn'
  AND c_parent.attname = 'id';

-- 3) Existing constraint drift report (same names, different behavior)
SELECT conname,
       convalidated,
       pg_get_constraintdef(oid) AS definition
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
