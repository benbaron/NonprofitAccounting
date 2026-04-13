-- 1) Blank or null account keys (must return 0 rows)
SELECT id, account_number, name, code
FROM account
WHERE account_number IS NULL
   OR LENGTH(TRIM(account_number)) = 0
   OR (name IS NOT NULL AND LENGTH(TRIM(name)) = 0)
   OR (code IS NOT NULL AND LENGTH(TRIM(code)) = 0);

-- 2) Duplicate normalized active account numbers (must return 0 rows)
SELECT REPLACE(REPLACE(UPPER(TRIM(account_number)), '-', ''), ' ', '') AS account_number_norm,
       COUNT(*) AS ct
FROM account
WHERE is_active = TRUE
GROUP BY REPLACE(REPLACE(UPPER(TRIM(account_number)), '-', ''), ' ', '')
HAVING COUNT(*) > 1;

-- 3) Duplicate normalized active account codes (must return 0 rows)
SELECT REPLACE(REPLACE(UPPER(TRIM(code)), '-', ''), ' ', '') AS account_code_norm,
       COUNT(*) AS ct
FROM account
WHERE is_active = TRUE AND code IS NOT NULL
GROUP BY REPLACE(REPLACE(UPPER(TRIM(code)), '-', ''), ' ', '')
HAVING COUNT(*) > 1;

-- 4) Blank or null fund keys (must return 0 rows)
SELECT id, code, name
FROM fund
WHERE code IS NULL
   OR LENGTH(TRIM(code)) = 0
   OR name IS NULL
   OR LENGTH(TRIM(name)) = 0;

-- 5) Duplicate normalized active fund codes (must return 0 rows)
SELECT REPLACE(REPLACE(UPPER(TRIM(code)), '-', ''), ' ', '') AS fund_code_norm,
       COUNT(*) AS ct
FROM fund
WHERE is_active = TRUE
GROUP BY REPLACE(REPLACE(UPPER(TRIM(code)), '-', ''), ' ', '')
HAVING COUNT(*) > 1;

-- 6) Duplicate normalized active fund names (must return 0 rows)
SELECT REPLACE(REPLACE(UPPER(TRIM(name)), '-', ''), ' ', '') AS fund_name_norm,
       COUNT(*) AS ct
FROM fund
WHERE is_active = TRUE
GROUP BY REPLACE(REPLACE(UPPER(TRIM(name)), '-', ''), ' ', '')
HAVING COUNT(*) > 1;

-- 7) Account alias collisions (must return 0 rows)
SELECT REPLACE(REPLACE(UPPER(TRIM(alias_text)), '-', ''), ' ', '') AS alias_norm,
       COUNT(DISTINCT account_id) AS account_count,
       LISTAGG(CAST(account_id AS VARCHAR(32)), ',') WITHIN GROUP (ORDER BY account_id) AS account_ids
FROM account_alias
WHERE is_active = TRUE
GROUP BY REPLACE(REPLACE(UPPER(TRIM(alias_text)), '-', ''), ' ', '')
HAVING COUNT(DISTINCT account_id) > 1;

-- 8) Fund alias collisions (must return 0 rows)
SELECT REPLACE(REPLACE(UPPER(TRIM(alias_text)), '-', ''), ' ', '') AS alias_norm,
       COUNT(DISTINCT fund_id) AS fund_count,
       LISTAGG(CAST(fund_id AS VARCHAR(32)), ',') WITHIN GROUP (ORDER BY fund_id) AS fund_ids
FROM fund_alias
WHERE is_active = TRUE
GROUP BY REPLACE(REPLACE(UPPER(TRIM(alias_text)), '-', ''), ' ', '')
HAVING COUNT(DISTINCT fund_id) > 1;

-- 9) Open queue items requiring governance action (track trend to 0)
SELECT alias_domain, status, COUNT(*) AS ct
FROM alias_review_queue
GROUP BY alias_domain, status
ORDER BY alias_domain, status;
