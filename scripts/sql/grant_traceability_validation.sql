-- Prompt G: Grant/Donor/Program Traceability
-- Validation checks (pre/post migration and invariants)

-- V1: Orphan checks for references from grant_record.
SELECT 'ORPHAN_DONOR' AS check_name, COUNT(*) AS row_count
FROM grant_record gr
LEFT JOIN donor d ON d.id = gr.donor_id
WHERE gr.donor_id IS NOT NULL AND d.id IS NULL
UNION ALL
SELECT 'ORPHAN_PERSON', COUNT(*)
FROM grant_record gr
LEFT JOIN person p ON p.id = gr.person_id
WHERE gr.person_id IS NOT NULL AND p.id IS NULL
UNION ALL
SELECT 'ORPHAN_CONTACT_PERSON', COUNT(*)
FROM grant_record gr
LEFT JOIN person p ON p.id = gr.contact_person_id
WHERE gr.contact_person_id IS NOT NULL AND p.id IS NULL
UNION ALL
SELECT 'ORPHAN_COUNTERPARTY', COUNT(*)
FROM grant_record gr
LEFT JOIN counterparty cp ON cp.id = gr.counterparty_id
WHERE gr.counterparty_id IS NOT NULL AND cp.id IS NULL
UNION ALL
SELECT 'ORPHAN_ACTIVITY', COUNT(*)
FROM grant_record gr
LEFT JOIN activity a ON a.id = gr.activity_id
WHERE gr.activity_id IS NOT NULL AND a.id IS NULL
UNION ALL
SELECT 'ORPHAN_FUND', COUNT(*)
FROM grant_record gr
LEFT JOIN fund f ON f.id = gr.fund_id
WHERE gr.fund_id IS NOT NULL AND f.id IS NULL
UNION ALL
SELECT 'ORPHAN_CANONICAL_TXN', COUNT(*)
FROM grant_record gr
LEFT JOIN txn t ON t.id = gr.canonical_txn_id
WHERE gr.canonical_txn_id IS NOT NULL AND t.id IS NULL
UNION ALL
SELECT 'ORPHAN_LEGACY_TXN', COUNT(*)
FROM grant_record gr
LEFT JOIN journal_transaction jt ON jt.id = gr.journal_txn_id
WHERE gr.journal_txn_id IS NOT NULL AND jt.id IS NULL;

-- V2: Required-contact rule violations.
SELECT grant_record_id, grant_id, donor_id, person_id, counterparty_id
FROM grant_record
WHERE donor_id IS NULL
  AND person_id IS NULL
  AND counterparty_id IS NULL
  AND NULLIF(TRIM(grantor), '') IS NULL;

-- V3: Invalid period and compliance statuses.
SELECT grant_record_id, period_start, period_end, restriction_class, compliance_status
FROM grant_record
WHERE (period_start IS NOT NULL AND period_end IS NOT NULL AND period_start > period_end)
   OR restriction_class NOT IN ('RESTRICTED','UNRESTRICTED','BOARD_DESIGNATED')
   OR compliance_status NOT IN ('IN_GOOD_STANDING','LATE_REPORT','AT_RISK','SUSPENDED','CLOSED');

-- V4: grant_posting_link integrity checks (xor, sign, and orphan target).
SELECT gpl.id, gpl.grant_record_id, gpl.posting_model, gpl.txn_split_id, gpl.journal_entry_id,
       gpl.posting_role, gpl.recognized_amount
FROM grant_posting_link gpl
LEFT JOIN grant_record gr ON gr.grant_record_id = gpl.grant_record_id
LEFT JOIN txn_split ts ON ts.id = gpl.txn_split_id
LEFT JOIN journal_entry je ON je.id = gpl.journal_entry_id
WHERE gr.grant_record_id IS NULL
   OR ((gpl.txn_split_id IS NULL AND gpl.journal_entry_id IS NULL)
       OR (gpl.txn_split_id IS NOT NULL AND gpl.journal_entry_id IS NOT NULL))
   OR (gpl.txn_split_id IS NOT NULL AND ts.id IS NULL)
   OR (gpl.journal_entry_id IS NOT NULL AND je.id IS NULL)
   OR gpl.recognized_amount = 0;

-- V5: Restricted-vs-unrestricted reference report totals.
SELECT
  restriction_class,
  COUNT(*) AS grant_count,
  SUM(awarded_amount) AS total_awarded,
  SUM(recognized_amount) AS total_recognized,
  SUM(unrecognized_balance) AS total_unrecognized
FROM v_grant_restriction_reporting
GROUP BY restriction_class
ORDER BY restriction_class;

-- V6: Compliance due-date queue (operational readiness).
SELECT grant_record_id, grant_id, compliance_status, next_report_due
FROM grant_record
WHERE compliance_status IN ('LATE_REPORT','AT_RISK')
   OR (next_report_due IS NOT NULL AND next_report_due < CURRENT_DATE)
ORDER BY next_report_due NULLS LAST;
