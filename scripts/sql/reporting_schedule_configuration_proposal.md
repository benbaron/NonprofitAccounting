# Reporting & Schedule Configuration Normalization Proposal

## Scope evaluated
- `report_section`
- `schedule_kind`
- `account_report_section`
- `account_schedule_requirement`
- `account_subtype_schedule_default`

## Current-state observations (code review)
1. Mapping semantics are split across account-level and subtype-level tables, but there is no first-class configuration version/release concept.
2. Existing tables do not capture who changed configuration, when, or why (limited auditability).
3. `account_subtype_schedule_default.subtype` is stored as free text, so integrity and change history depend on application behavior.
4. Statement mapping (`account_report_section`) and schedule requirements use different patterns and cannot be governed under one effective-dated release.
5. Existing constraints enforce uniqueness at point-in-time but do not support historical evolution with non-destructive supersession.

## Recommended normalized target model

### 1) Version container
**`config_release`**
- One row per publishable mapping version (`DRAFT`, `APPROVED`, `ACTIVE`, `RETIRED`).
- Supports effective dating and immutable release identity (`release_code`).

### 2) Statement taxonomy + mapping
**`statement_section`**
- Normalized statement section catalog (`report_type`, `section_code`, `section_name`, `sort_order`).
- Bound to `config_release` for versioned section definitions.

**`account_statement_mapping`**
- Effective-dated account-to-section mapping with `sign_policy` and lineage source.
- Enables non-destructive supersession (`valid_from`/`valid_to`).

### 3) Unified schedule requirements
**`schedule_requirement_rule`**
- Single rule table for both account and subtype subjects.
- `subject_kind` = `ACCOUNT` or `SUBTYPE`.
- `requirement_level` = `REQUIRED`, `OPTIONAL`, `EXCLUDED`.
- Effective-dated and release-bound for controlled rollout.

### 4) Audit trail
**`config_change_event`**
- Append-only audit log for DML changes on normalized config tables.
- Stores entity key, operation, actor, reason, and JSON snapshots.

## Compatibility approach
- Keep legacy tables during transition.
- Backfill normalized tables from legacy data into baseline release `LEGACY_BASELINE_V1`.
- Run validation parity checks before switching application reads.
- Optional phase-2: deprecate legacy tables after stable cutover.

## Deliverables
- Forward migration SQL: `scripts/sql/reporting_schedule_configuration_forward.sql`
- Rollback SQL: `scripts/sql/reporting_schedule_configuration_rollback.sql`
- Validation SQL: `scripts/sql/reporting_schedule_configuration_validation.sql`
- Risk register: `scripts/sql/reporting_schedule_configuration_risk_register.md`
