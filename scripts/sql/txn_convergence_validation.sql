-- canonical per-transaction out-of-balance rows (must return 0 rows)
SELECT ts.txn_id,
       ROUND(SUM(ts.amount_signed), 2) AS net_amount,
       COUNT(*) AS split_count
FROM txn_split ts
GROUP BY ts.txn_id
HAVING ROUND(SUM(ts.amount_signed), 2) <> 0;

-- canonical transactions with fewer than 2 splits (must return 0 rows)
SELECT t.id
FROM txn t
LEFT JOIN txn_split ts ON ts.txn_id = t.id
GROUP BY t.id
HAVING COUNT(ts.id) < 2;

-- orphan split references (must return 0 rows)
SELECT ts.id, ts.txn_id, ts.account_id, ts.fund_id
FROM txn_split ts
LEFT JOIN txn t ON t.id = ts.txn_id
LEFT JOIN account a ON a.id = ts.account_id
LEFT JOIN fund f ON f.id = ts.fund_id
WHERE t.id IS NULL OR a.id IS NULL OR f.id IS NULL;

-- legacy vs canonical mapped txn net parity (must return 0 rows)
WITH legacy_net AS (
  SELECT je.txn_id AS legacy_txn_id,
         ROUND(SUM(CASE
              WHEN UPPER(COALESCE(je.account_side,'DEBIT')) = 'CREDIT' THEN -ABS(je.amount)
              ELSE ABS(je.amount)
         END), 2) AS legacy_net
  FROM journal_entry je
  GROUP BY je.txn_id
), canon_net AS (
  SELECT m.legacy_txn_id,
         ROUND(SUM(ts.amount_signed), 2) AS canon_net
  FROM legacy_txn_map m
  JOIN txn_split ts ON ts.txn_id = m.canonical_txn_id
  GROUP BY m.legacy_txn_id
)
SELECT l.legacy_txn_id, l.legacy_net, c.canon_net
FROM legacy_net l
JOIN canon_net c ON c.legacy_txn_id = l.legacy_txn_id
WHERE l.legacy_net <> c.canon_net;

-- global parity over mapped population (delta must be 0)
WITH l AS (
  SELECT ROUND(SUM(CASE
      WHEN UPPER(COALESCE(account_side,'DEBIT'))='CREDIT' THEN -ABS(amount)
      ELSE ABS(amount)
    END), 2) AS total_legacy
  FROM journal_entry je
  WHERE EXISTS (SELECT 1 FROM legacy_txn_map m WHERE m.legacy_txn_id = je.txn_id)
), c AS (
  SELECT ROUND(SUM(ts.amount_signed), 2) AS total_canon
  FROM txn_split ts
  WHERE EXISTS (SELECT 1 FROM legacy_txn_map m WHERE m.canonical_txn_id = ts.txn_id)
)
SELECT l.total_legacy, c.total_canon, ROUND(l.total_legacy - c.total_canon, 2) AS delta
FROM l CROSS JOIN c;
