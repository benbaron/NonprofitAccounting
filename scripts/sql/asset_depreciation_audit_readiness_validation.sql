-- Pre/post migration structural checks.
SELECT TABLE_NAME, COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME IN (
  'ASSET_RECORD_DETAIL',
  'DEPRECIATION_RUN',
  'DEPRECIATION_RECORD',
  'INVENTORY_ASSET_LINK'
)
  AND COLUMN_NAME IN (
    'ASSET_STATE','IN_SERVICE_DATE','DISPOSAL_DATE','POSTED_ACQUISITION_TXN_ID','POSTED_DISPOSAL_TXN_ID',
    'PERIOD_START','PERIOD_END','RUN_STATUS','IS_LOCKED','LOCKED_AT','POSTED_TXN_ID',
    'SEQUENCE_IN_RUN','POSTED_JOURNAL_TXN_ID','REVERSAL_JOURNAL_TXN_ID',
    'LINK_TYPE','IS_PRIMARY_LINK','PRIMARY_ASSET_INVENTORY_KEY'
  )
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 1) Asset lifecycle violations.
SELECT asset_record_id, asset_state, date_acquired, in_service_date, disposal_date
FROM asset_record_detail
WHERE (asset_state IN ('DISPOSED','RETIRED') AND disposal_date IS NULL)
   OR (asset_state NOT IN ('DISPOSED','RETIRED') AND disposal_date IS NOT NULL)
   OR (in_service_date IS NOT NULL AND date_acquired IS NOT NULL AND in_service_date < date_acquired);

-- 2) Missing posted transaction links for assets that should be active/disposed.
SELECT asset_record_id, asset_state, posted_acquisition_txn_id, posted_disposal_txn_id
FROM asset_record_detail
WHERE (asset_state IN ('ACTIVE','HELD_FOR_SALE') AND posted_acquisition_txn_id IS NULL)
   OR (asset_state IN ('DISPOSED','RETIRED') AND posted_disposal_txn_id IS NULL);

-- 3) Depreciation run status/link consistency violations.
SELECT depreciation_run_id, run_status, is_locked, locked_at, posted_txn_id, period_start, period_end
FROM depreciation_run
WHERE (is_locked = TRUE AND locked_at IS NULL)
   OR (run_status = 'POSTED' AND posted_txn_id IS NULL)
   OR (run_status <> 'POSTED' AND posted_txn_id IS NOT NULL)
   OR (period_start IS NOT NULL AND period_end IS NOT NULL AND period_start > period_end);

-- 4) Duplicate period windows (should be zero with unique constraint).
SELECT period_start, period_end, COUNT(*) AS dup_count
FROM depreciation_run
GROUP BY period_start, period_end
HAVING COUNT(*) > 1;

-- 5) Duplicate depreciation rows for same run+asset.
SELECT depreciation_run_id, asset_record_id, COUNT(*) AS dup_count
FROM depreciation_record
GROUP BY depreciation_run_id, asset_record_id
HAVING COUNT(*) > 1;

-- 6) Records outside run period (should be zero).
SELECT dr.depreciation_record_id,
       dr.depreciation_run_id,
       dr.asset_record_id,
       dr.depreciation_date,
       r.period_start,
       r.period_end
FROM depreciation_record dr
JOIN depreciation_run r ON r.depreciation_run_id = dr.depreciation_run_id
WHERE dr.depreciation_date IS NOT NULL
  AND r.period_start IS NOT NULL
  AND r.period_end IS NOT NULL
  AND (dr.depreciation_date < r.period_start OR dr.depreciation_date > r.period_end);

-- 7) Posted run amount mismatch vs detail sum.
SELECT r.depreciation_run_id,
       r.posted_txn_id,
       COALESCE(SUM(dr.net_depreciation), 0) AS detail_total
FROM depreciation_run r
LEFT JOIN depreciation_record dr ON dr.depreciation_run_id = r.depreciation_run_id
WHERE r.run_status = 'POSTED'
GROUP BY r.depreciation_run_id, r.posted_txn_id;

-- 8) Inventory items with more than one primary asset link.
SELECT inventory_item_id, COUNT(*) AS primary_link_count
FROM inventory_asset_link
WHERE is_primary_link = TRUE
GROUP BY inventory_item_id
HAVING COUNT(*) > 1;

-- 9) Orphan checks.
SELECT dr.depreciation_record_id
FROM depreciation_record dr
LEFT JOIN asset_record_detail a ON a.asset_record_id = dr.asset_record_id
WHERE dr.asset_record_id IS NOT NULL
  AND a.asset_record_id IS NULL;

SELECT l.inventory_item_id, l.asset_record_id
FROM inventory_asset_link l
LEFT JOIN asset_record_detail a ON a.asset_record_id = l.asset_record_id
WHERE a.asset_record_id IS NULL;
