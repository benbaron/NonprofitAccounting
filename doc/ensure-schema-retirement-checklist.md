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

## Classification legend

Use these states when retiring each helper or schema group:

| State | Meaning |
|---|---|
| Flyway-owned | A migration now creates/updates this schema object. Java DDL should be removed or guarded. |
| To migrate | Still created or altered primarily by `ensureSchema()` and should move to Flyway. |
| Compatibility/backfill | Keep temporarily because it repairs or populates data, not because it defines future schema. |
| Removable | Can be deleted after tests confirm Flyway/fresh DB behavior. |
| Needs decision | Requires an architectural decision before migration/removal. |

## Current `ensureSchema()` sequence

As of this checklist, `ensureSchema()` runs these high-level steps:

```text
ensureMigrationTables
ensureCompanyProfile
ensureAccountAndLegacyJournalTables
ensureJpaTables
ensureJpaConstraints
ensureFundTransferIntegrityArtifacts
backfillLegacyTxnMap
ensureCompatibilityViews
ensurePeopleAndCounterparty
runReconciledDataBackfill
ensureRemainingLegacyTables
ensureOperationalLinkageTables
runOperationalLinkBackfillMigration
runReportingScheduleConfigurationMigration
runBankingReconciliationSchemaMigration
runFinancePostingEnforcementPreflight
```

## Retirement checklist

| Area | Current role | Target classification | Next action |
|---|---|---|---|
| `ensureMigrationTables` | Creates internal `schema_migration_history` used by Java-side migrations/backfills. | Compatibility/backfill | Keep only while Java backfill keys remain. Consider replacing with Flyway callbacks or idempotent backfill migrations. |
| `ensureCompanyProfile` | Creates/extends app company profile table. | To migrate | Move full table definition to Flyway. Remove duplicate Java `CREATE/ALTER` after validation. |
| `ensureAccountAndLegacyJournalTables` | Creates legacy `account`, `account_fund`, `journal_transaction`, `journal_entry`, `transaction_info`, and related compatibility tables. | Needs decision | Keep while legacy journal remains write authority. Move stable legacy table shape to Flyway; avoid new Java DDL. |
| Canonical columns on `account` | Adds `id`, `code`, `chart_id`, `subtype`, `normal_balance`, parent and status fields. | To migrate / Needs decision | Decide final account identity model. Prefer canonical id-based Flyway shape for fresh DBs. |
| `ensureJpaTables` | Creates canonical JPA-oriented tables such as chart, fund, txn, txn_split, report mapping, aliases, and fund transfers. | To migrate | Move canonical table DDL to Flyway and remove duplicate Java creation. |
| `ensureJpaConstraints` | Adds physical constraints for part of the canonical model. | To migrate | Move FK/unique/index/check constraints into Flyway. Verify against JPA validate. |
| `ensureFundTransferIntegrityArtifacts` | No longer seeds `fund_transfer_status_transition`; still repairs/adds fund-transfer FK constraints and `ix_ft_repair_open`. | Flyway-owned target / Compatibility repair | Seed removal is covered by `FundTransferStatusTransitionSeedValidationTest`. Do not remove remaining FK/index repair statements until separate metadata validation covers them. |
| `backfillLegacyTxnMap` | Mirrors legacy journal rows into canonical `txn` / `txn_split`. | Compatibility/backfill / Needs decision | Keep while legacy journal remains write authority. Later replace with canonical-first posting. |
| `ensureCompatibilityViews` | Creates compatibility views for old/new query paths. | Compatibility/backfill | Keep only if active code still queries the views. Otherwise migrate or remove. |
| `ensurePeopleAndCounterparty` | Creates/updates people, donor, counterparty, merchant-related structures. | To migrate | Move stable schema to Flyway. Keep only data copy/backfill logic in Java if needed. |
| `runReconciledDataBackfill` | Backfills reconciliation flags/data. | Compatibility/backfill | Keep as data migration until no active dev DB requires it; otherwise turn into Flyway data migration. |
| `ensureRemainingLegacyTables` | Creates miscellaneous older app tables. | Needs decision | Inventory active code references. Remove unused tables; migrate active ones to Flyway. |
| `ensureOperationalLinkageTables` | Creates linkage tables used by operational modules. | To migrate | Move stable linkage schema to Flyway in a bounded PR. |
| `runOperationalLinkBackfillMigration` | Backfills operational links. | Compatibility/backfill | Keep only if needed for active dev data. Otherwise replace with dev DB reset guidance. |
| `runReportingScheduleConfigurationMigration` | Creates/backfills report schedule configuration. | To migrate / Compatibility | Put stable schedule tables in Flyway. Keep any data seeding/backfill separately. |
| `runBankingReconciliationSchemaMigration` | Creates/backfills banking and reconciliation structures. | To migrate | Move banking/reconciliation schema to Flyway. Keep only data repair if needed. |
| `runFinancePostingEnforcementPreflight` | Checks/repairs finance-posting prerequisites. | Compatibility/backfill | Keep as validation/preflight if useful, but avoid schema creation here. |

## Rules for future PRs

1. Every new table, column, index, FK, unique constraint, or check constraint goes into Flyway.
2. JPA must remain `hbm2ddl.auto=validate`.
3. Do not add new canonical schema DDL to `Database.ensureSchema()`.
4. If `ensureSchema()` is changed, label the change as one of:
   - compatibility
   - backfill
   - repair
   - removal of duplicate Java DDL
5. Each migration PR should include a validation test or update an existing schema validation test.
6. Prefer deleting obsolete compatibility code over preserving speculative legacy support.

## Suggested first code slice

The first code PR after this checklist should move one bounded group from Java DDL to Flyway. Good candidates are:

```text
company_profile
people/counterparty/merchant tables
operational linkage tables
report schedule configuration tables
```

Avoid starting with the legacy journal/account identity model unless the write-ledger cutover decision has been made.

## Completion criteria

`ensureSchema()` is considered retired when:

```text
1. Fresh development databases are fully created by Flyway.
2. Hibernate validates successfully without Java DDL creating missing tables.
3. ensureSchema() no longer creates canonical tables or constraints.
4. Any remaining ensureSchema() code is clearly data repair/backfill or can be deleted.
5. Tests cover the normal database-open path used by MainWindow and MainWindowAlternate.
```
