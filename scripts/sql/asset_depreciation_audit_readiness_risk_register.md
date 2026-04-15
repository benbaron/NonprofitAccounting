# Asset & Depreciation Audit Readiness Risk Register

| Risk ID | Risk | Likelihood | Impact | Mitigation | Rollout Stage |
|---|---|---:|---:|---|---|
| AR-01 | Historical data violates new lifecycle constraints (missing disposal dates, invalid states). | High | High | Run validation SQL first; backfill `asset_state` and disposal metadata in batches; deploy constraints after cleanup window. | Pre-migration + Stage 1 |
| AR-02 | Duplicate depreciation rows exist for same run/asset and block unique constraint creation. | Medium | High | Precompute duplicates, load into remediation queue, archive superseded rows before enabling unique constraint. | Stage 1 |
| AR-03 | Application writes may fail after making run immutability fields mandatory. | Medium | High | Ship schema in compatibility mode first (defaults + nullable columns), then update service logic, then tighten checks. | Stage 1-2 |
| AR-04 | `posted_txn_id` linkage cannot be populated for legacy runs. | Medium | Medium | Allow non-POSTED status for historical runs and annotate as `VOIDED`/`CALCULATED`; populate links only for post-cutover runs. | Stage 2 |
| AR-05 | Generated-column uniqueness behavior differs across DB engines. | Low | Medium | Use `primary_asset_inventory_key` + unique index where supported; if a target engine lacks computed columns, replace with trigger/service guard plus validation query gate. | Stage 1 |
| AR-06 | Monthly close process slows due to additional validation queries. | Medium | Medium | Add indexes from forward migration, execute validation incrementally during month rather than only at close. | Stage 2 |
| AR-07 | Rollback complexity increases after new columns are populated by application code. | Low | High | Keep rollback SQL tested in lower environment and preserve snapshot backup before production migration. | Go-live |
| AR-08 | Inconsistent posting model between canonical and legacy transaction tables causes reconciliation gaps. | Medium | High | Standardize operational report to include both `journal_transaction` and canonical posting references during transition. | Stage 2-3 |

## Sequenced rollout recommendation
1. **Stage 0 (Assess):** run validation SQL on current data; estimate cleanup scope.
2. **Stage 1 (Expand):** apply additive columns and non-breaking indexes; keep writes backward compatible.
3. **Stage 2 (Backfill + App Update):** backfill lifecycle/run posting metadata and deploy service-layer state machine/immutability checks.
4. **Stage 3 (Enforce):** enable stricter constraints and unique rules once validation is consistently clean.
5. **Stage 4 (Operate):** embed monthly close checklist and validation SQL into close runbook.
