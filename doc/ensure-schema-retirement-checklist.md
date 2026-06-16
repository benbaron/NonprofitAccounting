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
  - LegacyTransactionMapCompatibilityBackfill
  - ReconciledDataCompatibilityBackfill
runFinancePostingEnforcementPreflight
```

No remaining `ensureSchema()` step should create tables, indexes, foreign keys, views, or other canonical schema objects. Any future schema change belongs in Flyway.

## Remaining responsibility review

| Area | Current role | Classification | Review decision | Next action |
|---|---|---|---|---|
| Java migration-marker helpers (`isMigrationApplied`, `markMigrationApplied`) | Retired from Java startup; `DatabaseCompatibilityBackfills` now relies on idempotent SQL guards instead of writing `schema_migration_history`. | Removed compatibility marker | Completed. Backfills no longer read or write Java migration markers. | Keep `schema_migration_history` only as a Flyway-owned legacy table until broader schema cleanup removes or documents it. |
| Legacy account normalization | Retired from Java startup; legacy `account.code` and `account.normal_balance` are normalized by Flyway migration `V016__backfill_legacy_account_normalization.sql`. | Data repair / moved to Flyway | Completed. This safe one-time account repair no longer runs during `ensureSchema()`. | Keep migration coverage while legacy account rows may need compatibility normalization. |
| `LegacyTransactionMapCompatibilityBackfill` | Populates `legacy_txn_map` from legacy journal rows and canonical `txn` rows with matching IDs. | Data backfill / needs decision | Keep while legacy journal and canonical transaction models coexist. It is now isolated from higher-risk transaction mirroring. | Decide canonical-first vs legacy-first transaction write authority before retiring. |
| `repairPeopleAndCounterparties` | Retired from Java startup; donor `external_id`, person `type`, and party-derived `counterparty` rows are backfilled by Flyway migration `V015__backfill_party_counterparty_compatibility.sql`. | Data repair / moved to Flyway | Completed. This safe party/counterparty repair no longer runs during `ensureSchema()`. | Keep focused migration coverage and remove this row once downstream code no longer needs legacy party compatibility notes. |
| `ReconciledDataCompatibilityBackfill` | Coordinates remaining transaction-transform subroutines that mirror legacy journal rows into canonical `txn`/`txn_split` and parse legacy dates. Default chart/fund seed data, party/counterparty repair data, account normalization, and account chart linking now live in Flyway. | Data backfill / high-risk | Keep for now. It transforms transaction data and is now isolated from low-risk account and operational-link repairs. | Keep transaction transforms guarded until canonical write authority is settled; continue moving safe one-time data repairs to Flyway where appropriate. |
| `OperationalLinkCompatibilityBackfill` | Retired from Java startup; deterministic donation/grant journal linking and unresolved-link queue population now run in Flyway migration `V018__backfill_operational_journal_links.sql`. | Data backfill / moved to Flyway | Completed. This medium-risk operational linkage backfill no longer runs during `ensureSchema()`. | Keep migration coverage while operational records may need journal-link compatibility backfills. |
| `runFinancePostingEnforcementPreflight` | Fails startup when finance posting enforcement exceptions exist. | Runtime preflight | Keep as validation/preflight, not schema work. | Consider moving to a dedicated startup validation service once `ensureSchema()` is renamed or retired. |

## Immediate follow-up slices

1. Completed: focused tests document `runOperationalLinkBackfillMigration` behavior for matched and unmatched donation/grant records.
2. Completed: focused tests document `runReconciledDataBackfill` behavior for legacy journal to canonical transaction mirroring and date parsing.
3. Completed: startup compatibility/backfill routines have moved into `DatabaseCompatibilityBackfills`.
4. Completed: focused tests document `backfillLegacyTxnMap` behavior for same-ID legacy/canonical rows, pre-existing mappings, and unmatched rows.
5. Completed: focused tests document `repairPeopleAndCounterparties` behavior for donor external IDs and person type defaults.
6. Completed: `runReconciledDataBackfill` is decomposed into decision-sized subroutines with a subroutine decision map.
7. Completed: default legacy chart/fund seed data moved from startup compatibility code to Flyway migration `V014__seed_default_legacy_chart_and_fund.sql`.
8. Completed: safe party/counterparty repairs moved from startup compatibility code to Flyway migration `V015__backfill_party_counterparty_compatibility.sql`.
9. Completed: Java backfill marker helpers were removed; startup backfills no longer read or write `schema_migration_history`.
10. Completed: startup compatibility backfills are separated by ownership/risk behind a small `DatabaseCompatibilityBackfills` orchestrator.
11. Completed: safe legacy account normalization moved from startup compatibility code to Flyway migration `V016__backfill_legacy_account_normalization.sql`.
12. Completed: safe legacy account chart linking moved from startup compatibility code to Flyway migration `V017__backfill_legacy_account_chart_links.sql`.
13. Completed: deterministic operational journal linking and unresolved-link queue population moved from startup compatibility code to Flyway migration `V018__backfill_operational_journal_links.sql`.

## `runReconciledDataBackfill` subroutine decision map

| Subroutine | Current role | Classification | Decision rule |
|---|---|---|---|
| `seedDefaultChartAndFund` | Retired from Java startup; default legacy chart and `GENERAL` fund are seeded by Flyway migration `V014__seed_default_legacy_chart_and_fund.sql`. | Static seed / moved to Flyway | Keep in Flyway while fresh databases require these defaults; remove this row after downstream startup backfills no longer assume default fund/chart data. |
| `linkLegacyAccountsToDefaultChart` | Retired from Java startup; account rows without `chart_id` are linked by Flyway migration `V017__backfill_legacy_account_chart_links.sql`. | Data repair / moved to Flyway | Keep migration coverage while legacy account rows may need compatibility chart linkage. |
| `mirrorLegacyJournalTransactions` | Creates canonical `txn` rows for legacy `journal_transaction` rows that do not already have canonical rows. | Data transform / high-risk | Keep guarded until canonical transaction write authority is settled; do not move blindly because it creates canonical transactions from legacy state. |
| `mirrorLegacyJournalSplits` | Creates canonical `txn_split` rows from legacy `journal_entry` rows. | Data transform / high-risk | Keep guarded with transaction mirroring; retire only after canonical writes are authoritative. |
| `backfillCounterpartiesFromPeopleAndDonors` | Retired from Java startup; `counterparty` rows from `person` and `donor` data are backfilled by Flyway migration `V015__backfill_party_counterparty_compatibility.sql`. | Data repair / moved to Flyway | Keep in Flyway while existing party data may need compatibility counterparties; remove this row once current write paths fully own counterparty creation. |
| `updateTxnDatesFromLegacyText` | Parses legacy `journal_transaction.date_text` values into canonical `txn.txn_date`. | Data transform / parsing compatibility | Keep only while legacy textual dates need migration; otherwise retire once canonical dates are authoritative. |

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
