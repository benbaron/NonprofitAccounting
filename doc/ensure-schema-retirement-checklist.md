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
ensureAccountAndLegacyJournalTables
backfillLegacyTxnMap
ensurePeopleAndCounterparty
runReconciledDataBackfill
runOperationalLinkBackfillMigration
runFinancePostingEnforcementPreflight
```

No remaining `ensureSchema()` step should create tables, indexes, foreign keys, views, or other canonical schema objects. Any future schema change belongs in Flyway.

## Remaining responsibility review

| Area | Current role | Classification | Review decision | Next action |
|---|---|---|---|---|
| Java migration-marker helpers (`isMigrationApplied`, `markMigrationApplied`) | Reads/writes `schema_migration_history` rows for Java backfill idempotency. | Transitional compatibility marker | Keep for now because `runReconciledDataBackfill` and `runOperationalLinkBackfillMigration` still need idempotency markers; do not reintroduce Java DDL for the marker table. | Remove the marker helpers only after the two Java backfills are either moved to Flyway data migrations or retired. |
| `ensureAccountAndLegacyJournalTables` | Normalizes legacy `account` data (`code`, `normal_balance`). | Data repair / compatibility | Keep temporarily. This is data mutation, not schema ownership, but it should be renamed or moved out of `ensureSchema()` after legacy journal authority is decided. | Add/extend tests around legacy account normalization before removal or relocation. |
| `backfillLegacyTxnMap` | Populates `legacy_txn_map` from legacy journal rows and canonical `txn` rows. | Data backfill / needs decision | Keep while legacy journal and canonical transaction models coexist. | Decide canonical-first vs legacy-first transaction write authority before retiring. |
| `ensurePeopleAndCounterparty` | Backfills donor `external_id` and person `type`. | Data repair / compatibility | Keep temporarily. Schema/index creation has been removed; remaining statements repair data only. | Rename in a later cleanup to make the data-repair role explicit. |
| `runReconciledDataBackfill` | Seeds default chart/fund when missing, links accounts to a chart, mirrors legacy journal rows into canonical `txn`/`txn_split`, copies party data, and parses legacy dates. | Data backfill / high-risk | Keep for now. It transforms transaction data and should not be deleted without a write-authority decision and focused backfill tests. | Split into a dedicated compatibility/backfill service or move safe seed data to Flyway; keep transaction transforms guarded until canonical write authority is settled. |
| `runOperationalLinkBackfillMigration` | Links donation/grant operational records to journal transactions and queues unresolved backfill issues. | Data backfill / medium-risk | Keep for now. Existing operational schema guards cover table shape, but this routine mutates operational data and queue rows. | Add focused tests for matched/unmatched donation and grant backfill behavior before moving or retiring it. |
| `runFinancePostingEnforcementPreflight` | Fails startup when finance posting enforcement exceptions exist. | Runtime preflight | Keep as validation/preflight, not schema work. | Consider moving to a dedicated startup validation service once `ensureSchema()` is renamed or retired. |

## Immediate follow-up slices

1. Add focused tests documenting `runOperationalLinkBackfillMigration` behavior for matched and unmatched donation/grant records.
2. Add focused tests documenting `runReconciledDataBackfill` behavior for legacy journal to canonical transaction mirroring and date parsing.
3. After coverage exists, move startup compatibility/backfill routines into a clearly named component such as `DatabaseCompatibilityBackfills`.
4. Once Java backfills are gone or relocated, delete the Java migration-marker helpers and stop using `schema_migration_history` outside Flyway-managed data.

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
