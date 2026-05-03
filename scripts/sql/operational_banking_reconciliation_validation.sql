-- Prompt C: Operational Banking Reconciliation
-- Validation checks (scaffold)

-- 1) Invalid statement status values
SELECT id, status
FROM bank_statement
WHERE status NOT IN ('OPEN','IN_REVIEW','CLOSED','LOCKED');

-- 2) Invalid banking match status values
SELECT banking_record_id, match_status
FROM banking_transaction_record
WHERE match_status NOT IN ('NEW','UNMATCHED','AUTO_MATCHED','MATCH_CONFIRMED','RECONCILED','DUPLICATE','ADJUSTED','STALE_UNMATCHED');

-- 3) Fingerprint uniqueness violations
SELECT bank_id_record_id, source_fingerprint, COUNT(*) AS dup_count
FROM banking_transaction_record
WHERE source_fingerprint IS NOT NULL
GROUP BY bank_id_record_id, source_fingerprint
HAVING COUNT(*) > 1;

-- 4) Reconciled rows without closed/locked statement
SELECT btr.banking_record_id, btr.statement_id, btr.match_status, bs.status AS statement_status
FROM banking_transaction_record btr
LEFT JOIN bank_statement bs ON bs.id = btr.statement_id
WHERE btr.match_status = 'RECONCILED'
  AND COALESCE(bs.status, 'OPEN') NOT IN ('CLOSED','LOCKED');
