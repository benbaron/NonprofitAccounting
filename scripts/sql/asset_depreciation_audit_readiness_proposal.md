# Asset & Depreciation Audit Readiness Proposal

## Scope evaluated
- `asset_record_detail`
- `inventory_asset_link`
- `depreciation_run`
- `depreciation_record`

## Current-state observations (code review)
1. Asset lifecycle semantics are implied (`date_acquired`/`date_sold`) but there is no explicit state machine (`DRAFT`, `ACTIVE`, `DISPOSED`, etc.).
2. Depreciation runs are mutable and do not carry period boundaries, run status, approver metadata, or posting linkage to accounting transactions.
3. `depreciation_record` lacks uniqueness rules to prevent duplicate rows for the same asset + run + period.
4. Journal linkage exists at the asset and inventory-link level, but there is no explicit run-to-posted-transaction control for close-time reconciliation.
5. Index coverage is minimal for common close queries (open assets, run status queue, run/detail joins).

## Recommended target model

### 1) Asset lifecycle governance (`asset_record_detail`)
Add explicit lifecycle and audit columns:
- `asset_state` (`DRAFT`, `ACTIVE`, `HELD_FOR_SALE`, `DISPOSED`, `RETIRED`)
- `in_service_date`, `disposal_date`
- `depreciable_basis`, `salvage_value`, `useful_life_months`
- `posted_acquisition_txn_id`, `posted_disposal_txn_id`

Enforce lifecycle checks:
- Disposed/retired assets must have `disposal_date`.
- Non-disposed assets must not have `disposal_date`.
- `in_service_date >= date_acquired` when both are present.

### 2) Depreciation run immutability + posting linkage (`depreciation_run`)
Add run controls:
- `period_start`, `period_end`
- `run_status` (`DRAFT`, `CALCULATED`, `POSTED`, `VOIDED`)
- `is_locked`, `locked_at`, `locked_by`
- `posted_txn_id` (journal posting created from the run)

Enforce controls:
- If `is_locked = TRUE`, `locked_at` must be populated.
- `posted_txn_id` required for `POSTED`, prohibited otherwise.
- Unique `(period_start, period_end)` to prevent duplicate run windows.

### 3) Depreciation record anti-duplication + auditability (`depreciation_record`)
Add fields:
- `period_start`, `period_end`
- `sequence_in_run`
- `posted_journal_txn_id` and `reversal_journal_txn_id`

Enforce rules:
- Unique `(depreciation_run_id, asset_record_id)`.
- `period_start <= period_end`.
- Positive `net_depreciation` for standard runs; reversals recorded with explicit reversal linkage.

### 4) Inventory linkage traceability (`inventory_asset_link`)
Enhance linkage governance:
- `link_type` (`CAPITALIZED_FROM_INVENTORY`, `COMPONENT`, `DISPOSAL_SOURCE`)
- `is_primary_link`
- Generated key column (`primary_asset_inventory_key`) + unique index for one primary link per inventory item.

## Suggested indexes
- `asset_record_detail(asset_state, in_service_date, disposal_date)`
- `asset_record_detail(posted_acquisition_txn_id)`
- `depreciation_run(run_status, period_end, created_at)`
- `depreciation_run(posted_txn_id)`
- `depreciation_record(depreciation_run_id, asset_record_id)` unique
- `depreciation_record(asset_record_id, period_end, depreciation_date)`
- `inventory_asset_link(asset_record_id, inventory_item_id)`

## Monthly close checklist (asset + depreciation)
1. Confirm all acquisitions/disposals are posted and linked to `asset_record_detail` posting columns.
2. Validate no assets remain in `DRAFT` with in-service dates in or before closing month.
3. Create depreciation run for closing period (`period_start`/`period_end`) and calculate details.
4. Review exceptions: disposed assets with depreciation after disposal, duplicate asset/run records, missing life/basis metadata.
5. Lock run (`is_locked=TRUE`) after review approval.
6. Post depreciation journal transaction and store `depreciation_run.posted_txn_id`.
7. Validate posted amount equals sum of run details.
8. Archive validation output with approver and timestamp evidence.

## Deliverables
- Forward migration SQL: `scripts/sql/asset_depreciation_audit_readiness_forward.sql`
- Rollback SQL: `scripts/sql/asset_depreciation_audit_readiness_rollback.sql`
- Validation SQL: `scripts/sql/asset_depreciation_audit_readiness_validation.sql`
- Risk register: `scripts/sql/asset_depreciation_audit_readiness_risk_register.md`
