BEGIN;

-- 1) Asset lifecycle hardening.
ALTER TABLE asset_record_detail
  ADD COLUMN IF NOT EXISTS asset_state VARCHAR(30) DEFAULT 'DRAFT' NOT NULL;
ALTER TABLE asset_record_detail
  ADD COLUMN IF NOT EXISTS in_service_date DATE;
ALTER TABLE asset_record_detail
  ADD COLUMN IF NOT EXISTS disposal_date DATE;
ALTER TABLE asset_record_detail
  ADD COLUMN IF NOT EXISTS depreciable_basis DECIMAL(19,2);
ALTER TABLE asset_record_detail
  ADD COLUMN IF NOT EXISTS salvage_value DECIMAL(19,2) DEFAULT 0;
ALTER TABLE asset_record_detail
  ADD COLUMN IF NOT EXISTS useful_life_months INT;
ALTER TABLE asset_record_detail
  ADD COLUMN IF NOT EXISTS posted_acquisition_txn_id INT;
ALTER TABLE asset_record_detail
  ADD COLUMN IF NOT EXISTS posted_disposal_txn_id INT;

ALTER TABLE asset_record_detail
  ADD CONSTRAINT IF NOT EXISTS chk_asset_state_domain
  CHECK (asset_state IN ('DRAFT','ACTIVE','HELD_FOR_SALE','DISPOSED','RETIRED'));

ALTER TABLE asset_record_detail
  ADD CONSTRAINT IF NOT EXISTS chk_asset_disposal_state_consistency
  CHECK (
    (asset_state IN ('DISPOSED','RETIRED') AND disposal_date IS NOT NULL)
    OR
    (asset_state NOT IN ('DISPOSED','RETIRED') AND disposal_date IS NULL)
  );

ALTER TABLE asset_record_detail
  ADD CONSTRAINT IF NOT EXISTS chk_asset_in_service_after_acquire
  CHECK (
    in_service_date IS NULL OR date_acquired IS NULL OR in_service_date >= date_acquired
  );

ALTER TABLE asset_record_detail
  ADD CONSTRAINT IF NOT EXISTS fk_asset_posted_acq_txn
  FOREIGN KEY (posted_acquisition_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL;

ALTER TABLE asset_record_detail
  ADD CONSTRAINT IF NOT EXISTS fk_asset_posted_disposal_txn
  FOREIGN KEY (posted_disposal_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS ix_asset_state_service_disposal
  ON asset_record_detail(asset_state, in_service_date, disposal_date);
CREATE INDEX IF NOT EXISTS ix_asset_posted_acq_txn
  ON asset_record_detail(posted_acquisition_txn_id);

-- 2) Depreciation run immutability + posting linkage.
ALTER TABLE depreciation_run
  ADD COLUMN IF NOT EXISTS period_start DATE;
ALTER TABLE depreciation_run
  ADD COLUMN IF NOT EXISTS period_end DATE;
ALTER TABLE depreciation_run
  ADD COLUMN IF NOT EXISTS run_status VARCHAR(20) DEFAULT 'DRAFT' NOT NULL;
ALTER TABLE depreciation_run
  ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE depreciation_run
  ADD COLUMN IF NOT EXISTS locked_at TIMESTAMP;
ALTER TABLE depreciation_run
  ADD COLUMN IF NOT EXISTS locked_by VARCHAR(120);
ALTER TABLE depreciation_run
  ADD COLUMN IF NOT EXISTS posted_txn_id INT;

ALTER TABLE depreciation_run
  ADD CONSTRAINT IF NOT EXISTS chk_depr_run_status_domain
  CHECK (run_status IN ('DRAFT','CALCULATED','POSTED','VOIDED'));

ALTER TABLE depreciation_run
  ADD CONSTRAINT IF NOT EXISTS chk_depr_run_period_order
  CHECK (period_start IS NULL OR period_end IS NULL OR period_start <= period_end);

ALTER TABLE depreciation_run
  ADD CONSTRAINT IF NOT EXISTS chk_depr_run_lock_metadata
  CHECK ((is_locked = FALSE) OR (is_locked = TRUE AND locked_at IS NOT NULL));

ALTER TABLE depreciation_run
  ADD CONSTRAINT IF NOT EXISTS chk_depr_run_posted_link_by_status
  CHECK (
    (run_status = 'POSTED' AND posted_txn_id IS NOT NULL)
    OR
    (run_status <> 'POSTED' AND posted_txn_id IS NULL)
  );

ALTER TABLE depreciation_run
  ADD CONSTRAINT IF NOT EXISTS fk_depr_run_posted_txn
  FOREIGN KEY (posted_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL;

ALTER TABLE depreciation_run
  ADD CONSTRAINT IF NOT EXISTS uq_depr_run_period UNIQUE (period_start, period_end);

CREATE INDEX IF NOT EXISTS ix_depr_run_status_period_end
  ON depreciation_run(run_status, period_end, created_at);
CREATE INDEX IF NOT EXISTS ix_depr_run_posted_txn
  ON depreciation_run(posted_txn_id);

-- 3) Depreciation record anti-duplication + linkage.
ALTER TABLE depreciation_record
  ADD COLUMN IF NOT EXISTS period_start DATE;
ALTER TABLE depreciation_record
  ADD COLUMN IF NOT EXISTS period_end DATE;
ALTER TABLE depreciation_record
  ADD COLUMN IF NOT EXISTS sequence_in_run INT;
ALTER TABLE depreciation_record
  ADD COLUMN IF NOT EXISTS posted_journal_txn_id INT;
ALTER TABLE depreciation_record
  ADD COLUMN IF NOT EXISTS reversal_journal_txn_id INT;

ALTER TABLE depreciation_record
  ADD CONSTRAINT IF NOT EXISTS chk_depr_record_period_order
  CHECK (period_start IS NULL OR period_end IS NULL OR period_start <= period_end);

ALTER TABLE depreciation_record
  ADD CONSTRAINT IF NOT EXISTS fk_depr_record_posted_txn
  FOREIGN KEY (posted_journal_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL;

ALTER TABLE depreciation_record
  ADD CONSTRAINT IF NOT EXISTS fk_depr_record_reversal_txn
  FOREIGN KEY (reversal_journal_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL;

ALTER TABLE depreciation_record
  ADD CONSTRAINT IF NOT EXISTS uq_depr_record_run_asset UNIQUE (depreciation_run_id, asset_record_id);

CREATE INDEX IF NOT EXISTS ix_depr_record_asset_period
  ON depreciation_record(asset_record_id, period_end, depreciation_date);
CREATE INDEX IF NOT EXISTS ix_depr_record_run_sequence
  ON depreciation_record(depreciation_run_id, sequence_in_run);

-- 4) Inventory-to-asset linkage governance.
ALTER TABLE inventory_asset_link
  ADD COLUMN IF NOT EXISTS link_type VARCHAR(40) DEFAULT 'COMPONENT' NOT NULL;
ALTER TABLE inventory_asset_link
  ADD COLUMN IF NOT EXISTS is_primary_link BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE inventory_asset_link
  ADD COLUMN IF NOT EXISTS primary_asset_inventory_key VARCHAR(255)
  GENERATED ALWAYS AS (CASE WHEN is_primary_link THEN inventory_item_id ELSE NULL END);

ALTER TABLE inventory_asset_link
  ADD CONSTRAINT IF NOT EXISTS chk_inventory_asset_link_type_domain
  CHECK (link_type IN ('CAPITALIZED_FROM_INVENTORY','COMPONENT','DISPOSAL_SOURCE'));

CREATE INDEX IF NOT EXISTS ix_inventory_asset_asset_inventory
  ON inventory_asset_link(asset_record_id, inventory_item_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_inventory_primary_asset_link
  ON inventory_asset_link(primary_asset_inventory_key);

-- Optional immutable append-only event log for run state changes.
CREATE TABLE IF NOT EXISTS depreciation_run_event (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  depreciation_run_id VARCHAR(255) NOT NULL,
  event_type VARCHAR(40) NOT NULL,
  event_detail VARCHAR(1000),
  actor VARCHAR(120),
  occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_depr_run_event_run
    FOREIGN KEY (depreciation_run_id) REFERENCES depreciation_run(depreciation_run_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_depr_run_event_run_time
  ON depreciation_run_event(depreciation_run_id, occurred_at);

COMMIT;
