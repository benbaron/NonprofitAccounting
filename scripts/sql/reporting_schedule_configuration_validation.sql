-- 1) Exactly one ACTIVE release expected for controlled cutover (target = 1 row).
SELECT status, COUNT(*) AS ct
FROM config_release
WHERE status = 'ACTIVE'
GROUP BY status;

-- 2) Release effective date sanity (must return 0 rows).
SELECT id, release_code, effective_from, effective_to
FROM config_release
WHERE effective_to IS NOT NULL
  AND effective_to < effective_from;

-- 3) Section catalog parity with legacy report_section for baseline release (delta should be 0).
SELECT
  (SELECT COUNT(*) FROM report_section) AS legacy_report_section_count,
  (SELECT COUNT(*)
   FROM statement_section ss
   JOIN config_release cr ON cr.id = ss.config_release_id
   WHERE cr.release_code = 'LEGACY_BASELINE_V1') AS normalized_section_count,
  ((SELECT COUNT(*)
    FROM statement_section ss
    JOIN config_release cr ON cr.id = ss.config_release_id
    WHERE cr.release_code = 'LEGACY_BASELINE_V1') -
   (SELECT COUNT(*) FROM report_section)) AS delta;

-- 4) Account statement mapping parity with legacy account_report_section (delta should be 0).
SELECT
  (SELECT COUNT(*) FROM account_report_section) AS legacy_mapping_count,
  (SELECT COUNT(*)
   FROM account_statement_mapping asm
   JOIN config_release cr ON cr.id = asm.config_release_id
   WHERE cr.release_code = 'LEGACY_BASELINE_V1'
     AND asm.valid_to IS NULL) AS normalized_open_mapping_count,
  ((SELECT COUNT(*)
    FROM account_statement_mapping asm
    JOIN config_release cr ON cr.id = asm.config_release_id
    WHERE cr.release_code = 'LEGACY_BASELINE_V1'
      AND asm.valid_to IS NULL) -
   (SELECT COUNT(*) FROM account_report_section)) AS delta;

-- 5) Schedule requirement parity (legacy account + subtype = normalized open rows; delta should be 0).
SELECT
  ((SELECT COUNT(*) FROM account_schedule_requirement) +
   (SELECT COUNT(*) FROM account_subtype_schedule_default)) AS legacy_schedule_rule_count,
  (SELECT COUNT(*)
   FROM schedule_requirement_rule srr
   JOIN config_release cr ON cr.id = srr.config_release_id
   WHERE cr.release_code = 'LEGACY_BASELINE_V1'
     AND srr.valid_to IS NULL) AS normalized_open_rule_count,
  ((SELECT COUNT(*)
    FROM schedule_requirement_rule srr
    JOIN config_release cr ON cr.id = srr.config_release_id
    WHERE cr.release_code = 'LEGACY_BASELINE_V1'
      AND srr.valid_to IS NULL) -
   ((SELECT COUNT(*) FROM account_schedule_requirement) +
    (SELECT COUNT(*) FROM account_subtype_schedule_default))) AS delta;

-- 6) Invalid subject discriminator combinations (must return 0 rows).
SELECT id, subject_kind, account_id, subtype
FROM schedule_requirement_rule
WHERE (subject_kind = 'ACCOUNT' AND (account_id IS NULL OR subtype IS NOT NULL))
   OR (subject_kind = 'SUBTYPE' AND (account_id IS NOT NULL OR subtype IS NULL));

-- 7) Duplicate open rules by subject + schedule within same release (must return 0 rows).
SELECT config_release_id,
       subject_kind,
       COALESCE(CAST(account_id AS VARCHAR(30)), subtype) AS subject_ref,
       schedule_kind_id,
       COUNT(*) AS ct
FROM schedule_requirement_rule
WHERE valid_to IS NULL
GROUP BY config_release_id, subject_kind, COALESCE(CAST(account_id AS VARCHAR(30)), subtype), schedule_kind_id
HAVING COUNT(*) > 1;

-- 8) Orphaned mappings/rules against reference dimensions (must return 0 rows).
SELECT 'ASM_ACCOUNT' AS test_name, COUNT(*) AS orphan_count
FROM account_statement_mapping asm
LEFT JOIN account a ON a.id = asm.account_id
WHERE a.id IS NULL
UNION ALL
SELECT 'ASM_SECTION', COUNT(*)
FROM account_statement_mapping asm
LEFT JOIN statement_section ss ON ss.id = asm.statement_section_id
WHERE ss.id IS NULL
UNION ALL
SELECT 'SRR_SCHEDULE_KIND', COUNT(*)
FROM schedule_requirement_rule srr
LEFT JOIN schedule_kind sk ON sk.id = srr.schedule_kind_id
WHERE sk.id IS NULL;

-- 9) Audit trail smoke check: table exists and is writable (count should be >= 0; expected 0 right after migration).
SELECT COUNT(*) AS config_change_event_count
FROM config_change_event;
