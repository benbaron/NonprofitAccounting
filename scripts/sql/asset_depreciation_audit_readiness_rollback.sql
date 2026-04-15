BEGIN;

DROP INDEX IF EXISTS ix_depr_run_event_run_time;
DROP TABLE IF EXISTS depreciation_run_event;

DROP INDEX IF EXISTS uq_inventory_primary_asset_link;
DROP INDEX IF EXISTS ix_inventory_asset_asset_inventory;
ALTER TABLE inventory_asset_link DROP CONSTRAINT IF EXISTS chk_inventory_asset_link_type_domain;
ALTER TABLE inventory_asset_link DROP COLUMN IF EXISTS primary_asset_inventory_key;
ALTER TABLE inventory_asset_link DROP COLUMN IF EXISTS is_primary_link;
ALTER TABLE inventory_asset_link DROP COLUMN IF EXISTS link_type;

DROP INDEX IF EXISTS ix_depr_record_run_sequence;
DROP INDEX IF EXISTS ix_depr_record_asset_period;
ALTER TABLE depreciation_record DROP CONSTRAINT IF EXISTS uq_depr_record_run_asset;
ALTER TABLE depreciation_record DROP CONSTRAINT IF EXISTS fk_depr_record_reversal_txn;
ALTER TABLE depreciation_record DROP CONSTRAINT IF EXISTS fk_depr_record_posted_txn;
ALTER TABLE depreciation_record DROP CONSTRAINT IF EXISTS chk_depr_record_period_order;
ALTER TABLE depreciation_record DROP COLUMN IF EXISTS reversal_journal_txn_id;
ALTER TABLE depreciation_record DROP COLUMN IF EXISTS posted_journal_txn_id;
ALTER TABLE depreciation_record DROP COLUMN IF EXISTS sequence_in_run;
ALTER TABLE depreciation_record DROP COLUMN IF EXISTS period_end;
ALTER TABLE depreciation_record DROP COLUMN IF EXISTS period_start;

DROP INDEX IF EXISTS ix_depr_run_posted_txn;
DROP INDEX IF EXISTS ix_depr_run_status_period_end;
ALTER TABLE depreciation_run DROP CONSTRAINT IF EXISTS uq_depr_run_period;
ALTER TABLE depreciation_run DROP CONSTRAINT IF EXISTS fk_depr_run_posted_txn;
ALTER TABLE depreciation_run DROP CONSTRAINT IF EXISTS chk_depr_run_posted_link_by_status;
ALTER TABLE depreciation_run DROP CONSTRAINT IF EXISTS chk_depr_run_lock_metadata;
ALTER TABLE depreciation_run DROP CONSTRAINT IF EXISTS chk_depr_run_period_order;
ALTER TABLE depreciation_run DROP CONSTRAINT IF EXISTS chk_depr_run_status_domain;
ALTER TABLE depreciation_run DROP COLUMN IF EXISTS posted_txn_id;
ALTER TABLE depreciation_run DROP COLUMN IF EXISTS locked_by;
ALTER TABLE depreciation_run DROP COLUMN IF EXISTS locked_at;
ALTER TABLE depreciation_run DROP COLUMN IF EXISTS is_locked;
ALTER TABLE depreciation_run DROP COLUMN IF EXISTS run_status;
ALTER TABLE depreciation_run DROP COLUMN IF EXISTS period_end;
ALTER TABLE depreciation_run DROP COLUMN IF EXISTS period_start;

DROP INDEX IF EXISTS ix_asset_posted_acq_txn;
DROP INDEX IF EXISTS ix_asset_state_service_disposal;
ALTER TABLE asset_record_detail DROP CONSTRAINT IF EXISTS fk_asset_posted_disposal_txn;
ALTER TABLE asset_record_detail DROP CONSTRAINT IF EXISTS fk_asset_posted_acq_txn;
ALTER TABLE asset_record_detail DROP CONSTRAINT IF EXISTS chk_asset_in_service_after_acquire;
ALTER TABLE asset_record_detail DROP CONSTRAINT IF EXISTS chk_asset_disposal_state_consistency;
ALTER TABLE asset_record_detail DROP CONSTRAINT IF EXISTS chk_asset_state_domain;
ALTER TABLE asset_record_detail DROP COLUMN IF EXISTS posted_disposal_txn_id;
ALTER TABLE asset_record_detail DROP COLUMN IF EXISTS posted_acquisition_txn_id;
ALTER TABLE asset_record_detail DROP COLUMN IF EXISTS useful_life_months;
ALTER TABLE asset_record_detail DROP COLUMN IF EXISTS salvage_value;
ALTER TABLE asset_record_detail DROP COLUMN IF EXISTS depreciable_basis;
ALTER TABLE asset_record_detail DROP COLUMN IF EXISTS disposal_date;
ALTER TABLE asset_record_detail DROP COLUMN IF EXISTS in_service_date;
ALTER TABLE asset_record_detail DROP COLUMN IF EXISTS asset_state;

COMMIT;
