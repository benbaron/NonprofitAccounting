# `Database.ensureSchema()` retirement checklist

## Purpose

This checklist tracks the remaining responsibilities inside `nonprofitbookkeeping.core.Database#ensureSchema()` so they can be moved out of broad Java DDL and into explicit Flyway migrations or narrowly scoped compatibility/backfill code.

Current transition rule:

```text
Flyway changes schema.
JPA validates schema.
ensureSchema remains only as a temporary compatibility/backfill bridge.
```

Because this project is still in early development and there are no significant legacy databases to preserve, prefer clean Flyway ownership and development database reset over elaborate compatibility code.

## Current `ensureSchema()` sequence

As of this review, `ensureSchema()` runs these high-level steps:

```text
FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled()
DatabaseCompatibilityBackfills.run
  - normalizeLegacyAccounts
  - backfillLegacyTxnMap
  - repairPeopleAndCounterparties
  - runReconciledDataBackfill
  - runOperationalLinkBackfillMigration
runFinancePostingEnforcementPreflight
```

No remaining `ensureSchema()` step should create tables, indexes, foreign keys, views, or other canonical schema objects. Any future schema change belongs in Flyway.

## Remaining responsibility review

| Area | Current role | Classification | Review decision | Next action |
|---|---|---|---|---|
| Java migration-marker helpers (`isMigrationApplied`, `markMigrationApplied`) | Reads/writes `schema_migration_history` rows for Java backfill idempotency inside `DatabaseCompatibilityBackfills`. | Transitional compatibility marker | Keep for now because `runReconciledDataBackfill` and `runOperationalLinkBackfillMigration` still need idempotency markers; do not reintroduce Java DDL for the marker table. | Remove the marker helpers only after the two Java backfills are either moved to Flyway data migrations or retired. |
| `normalizeLegacyAccounts` | Normalizes legacy `account` data (`code`, `normal_balance`). | Data repair / compatibility | Keep temporarily. This is data mutation, not schema ownership, but it should be renamed or moved out of `ensureSchema()` after legacy journal authority is decided. | Focused tests now document legacy account normalization; decide whether to keep it until legacy journal write authority is settled or retire it with the legacy write path. |
| `backfillLegacyTxnMap` | Populates `legacy_txn_map` from legacy journal rows and canonical `txn` rows with matching IDs. | Data backfill / needs decision | Keep while legacy journal and canonical transaction models coexist. Focused tests now document its narrow same-ID, missing-row behavior. | Decide canonical-first vs legacy-first transaction write authority before retiring. |
| `repairPeopleAndCounterparties` | Backfills donor `external_id` and person `type`. | Data repair / compatibility | Keep temporarily. Schema/index creation has been removed; remaining statements repair data only, and focused tests now document the behavior. | Decide whether to keep this repair until party/profile write authority is settled or retire it with the legacy write path. |
| `runReconciledDataBackfill` | Seeds default chart/fund when missing, links accounts to a chart, mirrors legacy journal rows into canonical `txn`/`txn_split`, copies party data, and parses legacy dates. | Data backfill / high-risk | Keep for now. It transforms transaction data and now has focused behavior coverage before any future retirement decision. | Move safe seed data to Flyway where appropriate; keep transaction transforms guarded until canonical write authority is settled. |
| `runOperationalLinkBackfillMigration` | Links donation/grant operational records to journal transactions and queues unresolved backfill issues. | Data backfill / medium-risk | Keep for now. Existing operational schema guards cover table shape, and focused behavior tests now document matched/unmatched donation and grant handling. | Decide whether this backfill should move to Flyway data migration, remain in `DatabaseCompatibilityBackfills`, or be retired for development databases. |
| `runFinancePostingEnforcementPreflight` | Fails startup when finance posting enforcement exceptions exist. | Runtime preflight | Keep as validation/preflight, not schema work. | Consider moving to a dedicated startup validation service once `ensureSchema()` is renamed or retired. |

## Immediate follow-up slices

1. Completed: focused tests document `runOperationalLinkBackfillMigration` behavior for matched and unmatched donation/grant records.
2. Completed: focused tests document `runReconciledDataBackfill` behavior for legacy journal to canonical transaction mirroring and date parsing.
3. Completed: startup compatibility/backfill routines have moved into `DatabaseCompatibilityBackfills`.
4. Completed: focused tests document `backfillLegacyTxnMap` behavior for same-ID legacy/canonical rows, pre-existing mappings, and unmatched rows.
5. Completed: focused tests document `repairPeopleAndCounterparties` behavior for donor external IDs and person type defaults.
6. Once Java backfills are gone or relocated, delete the Java migration-marker helpers and stop using `schema_migration_history` outside Flyway-managed data.

## Rules for future PRs

1. Every new table, column, index, FK, unique constraint, or check constraint goes into Flyway.
2. JPA must remain `hbm2ddl.auto=validate`.
3. Do not add new canonical schema DDL to `Database.ensureSchema()`.
4. If `ensureSchema()` is changed, label the change as one of:
   - compatibility
   - backfill
   - repair
   - preflight
   - removal of duplicate Java DDL
5. Each migration/backfill PR should include a validation test or update an existing focused test.
6. Prefer deleting obsolete compatibility code over preserving speculative legacy support.

## Completion criteria

`ensureSchema()` is considered retired when:

```text
1. Fresh development databases are fully created by Flyway.
2. Hibernate validates successfully without Java DDL creating missing tables.
3. ensureSchema() no longer creates canonical tables or constraints.
4. Any remaining ensureSchema() code is clearly data repair/backfill/preflight or can be deleted.
5. Tests cover the normal database-open path used by MainWindow and MainWindowAlternate.
```
