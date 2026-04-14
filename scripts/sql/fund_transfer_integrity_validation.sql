-- 1) Invalid status values (must return 0 rows)
SELECT ft.id, ft.status
FROM fund_transfer ft
WHERE ft.status NOT IN ('DRAFT','APPROVED','POSTING','POSTED','FAILED','VOIDED');

-- 2) Posted-link/status mismatch (must return 0 rows)
SELECT ft.id, ft.status, ft.posted_txn_id
FROM fund_transfer ft
WHERE (ft.status = 'POSTED' AND ft.posted_txn_id IS NULL)
   OR (ft.status <> 'POSTED' AND ft.posted_txn_id IS NOT NULL);

-- 3) Missing posted txn rows (must return 0 rows)
SELECT ft.id, ft.posted_txn_id
FROM fund_transfer ft
LEFT JOIN txn t ON t.id = ft.posted_txn_id
WHERE ft.posted_txn_id IS NOT NULL
  AND t.id IS NULL;

-- 4) Posted txn must balance to zero (must return 0 rows)
SELECT ft.id AS transfer_id,
       ft.posted_txn_id,
       ROUND(SUM(ts.amount_signed), 2) AS net_amount
FROM fund_transfer ft
JOIN txn_split ts ON ts.txn_id = ft.posted_txn_id
WHERE ft.status = 'POSTED'
GROUP BY ft.id, ft.posted_txn_id
HAVING ROUND(SUM(ts.amount_signed), 2) <> 0;

-- 5) Transfer must include from-fund and to-fund legs whose nets match equal/opposite transfer amount.
--    Additional lines are permitted only if they do not alter required leg nets.
WITH posted_leg AS (
  SELECT ft.id AS transfer_id,
         ft.amount,
         ft.from_fund_id,
         ft.to_fund_id,
         ft.posted_txn_id,
         SUM(CASE WHEN ts.fund_id = ft.from_fund_id THEN ts.amount_signed ELSE 0 END) AS from_net,
         SUM(CASE WHEN ts.fund_id = ft.to_fund_id THEN ts.amount_signed ELSE 0 END) AS to_net,
         COUNT(CASE WHEN ts.fund_id = ft.from_fund_id THEN 1 END) AS from_lines,
         COUNT(CASE WHEN ts.fund_id = ft.to_fund_id THEN 1 END) AS to_lines
  FROM fund_transfer ft
  JOIN txn_split ts ON ts.txn_id = ft.posted_txn_id
  WHERE ft.status = 'POSTED'
  GROUP BY ft.id, ft.amount, ft.from_fund_id, ft.to_fund_id, ft.posted_txn_id
)
SELECT transfer_id, posted_txn_id, amount, from_net, to_net, from_lines, to_lines
FROM posted_leg
WHERE ROUND(from_net, 2) <> ROUND(-amount, 2)
   OR ROUND(to_net, 2) <> ROUND(amount, 2)
   OR from_lines < 1
   OR to_lines < 1;

-- 6) Duplicate posted txn link (must return 0 rows)
SELECT posted_txn_id, COUNT(*) AS transfer_count
FROM fund_transfer
WHERE posted_txn_id IS NOT NULL
GROUP BY posted_txn_id
HAVING COUNT(*) > 1;

-- 7) Long-running posting states (operational warning list)
SELECT ft.id, ft.transfer_date, ft.status, ft.updated_at
FROM fund_transfer ft
WHERE ft.status IN ('POSTING','FAILED')
  AND ft.updated_at < DATEADD('DAY', -1, CURRENT_TIMESTAMP)
ORDER BY ft.updated_at;

-- 8) Populate repair queue for unresolved mismatches (idempotent insert by transfer+issue open state)
INSERT INTO fund_transfer_repair_queue(transfer_id, issue_code, issue_detail, proposed_action)
SELECT v.transfer_id,
       v.issue_code,
       v.issue_detail,
       v.proposed_action
FROM (
  SELECT ft.id AS transfer_id,
         'STATUS_POSTED_LINK_MISMATCH' AS issue_code,
         'status=' || ft.status || ', posted_txn_id=' || COALESCE(CAST(ft.posted_txn_id AS VARCHAR), 'NULL') AS issue_detail,
         'Set status/posting link consistently. If txn exists and valid, set POSTED; else clear link and set APPROVED/FAILED.' AS proposed_action
  FROM fund_transfer ft
  WHERE (ft.status = 'POSTED' AND ft.posted_txn_id IS NULL)
     OR (ft.status <> 'POSTED' AND ft.posted_txn_id IS NOT NULL)

  UNION ALL

  SELECT ft.id AS transfer_id,
         'POSTED_TXN_NOT_BALANCED' AS issue_code,
         'posted_txn_id=' || CAST(ft.posted_txn_id AS VARCHAR) || ' is out of balance.' AS issue_detail,
         'Create reversing/adjusting txn_split lines or re-post transfer atomically.' AS proposed_action
  FROM fund_transfer ft
  JOIN txn_split ts ON ts.txn_id = ft.posted_txn_id
  WHERE ft.status = 'POSTED'
  GROUP BY ft.id, ft.posted_txn_id
  HAVING ROUND(SUM(ts.amount_signed), 2) <> 0
) v
LEFT JOIN fund_transfer_repair_queue q
  ON q.transfer_id = v.transfer_id
 AND q.issue_code = v.issue_code
 AND q.resolved_at IS NULL
WHERE q.id IS NULL;
