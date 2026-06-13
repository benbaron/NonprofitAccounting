# Migration Cleanup Plan

This document records the agreed database migration cleanup plan for the current `NonprofitAccounting` archive.

The project is still in early development and there are no significant production legacy databases to preserve. That changes the long-term schema strategy: the application should move to an explicit Flyway-managed schema sooner rather than preserving a complicated compatibility layer indefinitely.

However, the current codebase still has many live database references to both legacy JDBC tables and the newer canonical JPA tables. Therefore the first Flyway migration should be a **current-runtime baseline**, not a final clean canonical schema.

## Vocabulary

### Baseline

A **baseline** is the first official Flyway schema file that can create a fresh database capable of running the current application.

Recommended first migration name:

```text
src/main/resources/db/migration/V001__baseline_current_runtime_schema.sql
```

This does not mean the schema is final or ideal. It means:

```text
This is the schema shape required by the application as it exists today.
Future schema changes must be explicit Flyway migrations.
```

Later migrations can clean up legacy structures after the current code has stopped depending on them.

### Current-runtime baseline vs final canonical schema

A current-runtime baseline preserves current compatibility:

```text
journal_transaction / journal_entry / transaction_info
plus
txn / txn_split
plus
supporting lookup, import, read-model, and operational tables
```

A final canonical schema would eventually prefer:

```text
txn / txn_split as the sole accounting posting model
legacy journal tables removed or converted to compatibility views
Flyway as schema authority
JPA as validate-only mapping
ensureSchema() reduced or retired for canonical tables
```

The first migration should be the current-runtime baseline because that is the most compatible with the code that exists now.

## Target authority split

The desired end state is:

| Responsibility | Owner |
|---|---|
| Schema creation | Flyway migrations |
| Versioned schema changes | Flyway migrations |
| Java object model | JPA/Hibernate entities |
| Runtime validation of schema compatibility | Hibernate `validate` |
| Legacy repair/backfill, if needed | Small explicit repair tools, not broad `ensureSchema()` |

The current transitional state is:

| Mechanism | Current role |
|---|---|
| `Database.ensureSchema()` | Actual deployed schema creator/upgrader |
| JPA/Hibernate | Canonical model but also mutates schema through `hbm2ddl.auto=update` |
| Flyway | Dependency present, not yet authoritative |

Because this is early development, the project should move away from this transitional state sooner rather than keeping it for long-term compatibility.

## Important current-code finding

The current codebase is not yet canonical-only.

Several live services still write or read the legacy journal model:

```text
journal_transaction
journal_entry
transaction_info
```

At the same time, newer services and read models increasingly use:

```text
txn
txn_split
```

There is also bridge code that synchronizes legacy journal rows into canonical `txn` / `txn_split` rows.

Therefore the most compatible first Flyway baseline should include both sets of tables.

Do not create a clean `txn` / `txn_split`-only baseline yet unless the current code is first changed to stop using `journal_transaction`, `journal_entry`, and `transaction_info`.

## Minimum viable current-code baseline

The following list is the minimum viable table set based on current database-touching functions. Some names may need final confirmation against concrete repository class names before the SQL is written, but this is the intended scope.

### Core accounting and canonical lookup tables

```text
account
account_fund
chart_of_accounts
account_alias
fund
fund_alias
counterparty
activity
merchant
schedule_kind
report_section
account_report_section
account_schedule_requirement
account_subtype_schedule_default
```

Notes:

- `account` is currently mixed legacy/canonical. Current code still uses `account_number`, while canonical code expects `id`.
- `account_fund` is legacy-compatible and should stay in the first baseline because current account persistence still manages it.
- `chart_of_accounts`, `report_section`, and related tables support the canonical account/reporting model.

### Legacy journal write model

```text
journal_transaction
journal_entry
transaction_info
```

These should remain in the first Flyway baseline because current `JournalRepository` and related services still depend on them.

Long-term target:

```text
Replace these as primary write tables with txn / txn_split.
Eventually remove them or convert them to compatibility views/mirror tables.
```

### Canonical posting model

```text
txn
txn_split
```

These are the intended canonical double-entry posting tables.

`txn` is the transaction header.

`txn_split` is the accounting posting line. Despite the name, it should be treated as a double-entry posting row, not merely a spreadsheet category split.

For posted transactions, service logic must enforce:

```text
at least two split rows
no zero-value split rows
total debits equal total credits
required account/fund dimensions are present
```

The current signed-amount design interprets debit/credit display from the account normal balance.

### Canonical sync and review support

```text
legacy_txn_map
alias_review_queue
```

These are transitional support tables.

Keep them in the first baseline if the bridge/sync code remains active.

Long-term target:

```text
Remove legacy_txn_map when canonical posting is primary.
Keep alias_review_queue if import/reconciliation workflows need unresolved alias review.
```

### Supplemental schedule support

```text
txn_supplemental_line
```

Keep in the first baseline because current journal persistence and supplemental-line loading still refer to it.

Long-term target:

```text
Rework supplemental schedule details to reference canonical txn / txn_split directly.
```

### Company/profile/document/support tables

```text
company_profile
company_store
document
json_storage
schema_migration_history
```

Notes:

- `company_profile` is needed for company metadata.
- `document` is needed if documents/attachments remain active.
- `json_storage` should not become a canonical data model, but it may be useful for raw import/source preservation.
- `schema_migration_history` is transitional. It should be replaced by Flyway's schema history once Flyway becomes the authority.

### Donation and fund-transfer operational tables

```text
donation_record
donation_journal_link
fund_transfer
fund_transfer_status_transition
fund_transfer_integrity_event
```

Keep these if the current donation and fund-transfer workflows remain enabled.

Long-term target:

```text
Move links from legacy journal ids toward canonical txn ids.
```

### Grant/depreciation/read-model support

```text
grant_record
grant_posting_link
depreciation_run
depreciation_record
rm_donation_summary
rm_grant_summary
rm_fund_summary
rm_reconciliation_summary
rm_depreciation_summary
```

The `rm_*` tables are rebuildable read models, not primary source of truth.

Keep them in the first baseline if the current read-model maintenance code is active.

Long-term target:

```text
Treat read models as rebuildable projections from canonical txn / txn_split and related operational records.
```

### Banking and reconciliation tables

These names should be confirmed against the active repository classes before writing DDL, but the current feature set requires a banking/reconciliation group.

Expected baseline group:

```text
banking_transaction_record
bank_statement
bank_statement_record
bank_identity
reconciliation operational tables
undeposited_funds_item
```

Long-term target:

```text
Canonicalize bank statements, imported bank lines, reconciliation runs, and matches.
Avoid duplicating the same bank event in several incompatible tables.
```

### SCLX/import staging tables

The current SCLX import target stages collections into concrete record tables. Keep the staging tables if current import/export is part of the runnable application.

Expected baseline group:

```text
imported_organization_record
imported_reporting_period_record
imported_fund_record
imported_budget_record
imported_event_record
imported_document_record
imported_outstanding_item_record
imported_other_asset_item_record
imported_asset_record
imported_supply_record
imported_banking_item_record
imported_bank_statement_record
```

Long-term target:

```text
Keep staging tables for import audit/review.
Promote accepted data into canonical tables.
Avoid letting staging records become the canonical application model.
```

## Tables that should not be introduced into the first baseline unless current code requires them

Do not add speculative future tables just because they may be useful later.

Examples:

```text
report_template
report_run
report_output
budget_revision
inventory_movement
new BudgetCategory tables until that feature lands in this archive
final-canonical replacement tables that current code does not use
```

Add these later through explicit migrations when the feature branch needs them.

## Cleanup sequence

### Phase 1: Create the current-runtime Flyway baseline

Add:

```text
src/main/resources/db/migration/V001__baseline_current_runtime_schema.sql
```

This migration should create the minimum viable current-code table subset listed above.

Goal:

```text
A fresh development database created only by Flyway should allow the current application to start and exercise current database-backed functions.
```

This phase should preserve compatibility, not redesign everything.

### Phase 2: Add a Flyway runner

Add startup or explicit migration-tool code that runs Flyway before the application opens the database.

The app should fail clearly if the database cannot be migrated.

### Phase 3: Switch Hibernate from update to validate

After the Flyway baseline is complete and works, change Hibernate configuration from:

```text
hibernate.hbm2ddl.auto=update
```

to:

```text
hibernate.hbm2ddl.auto=validate
```

Goal:

```text
Hibernate verifies that the database matches the entity mappings, but it no longer silently changes the schema.
```

### Phase 4: Reduce Database.ensureSchema()

Once Flyway can create the working schema, reduce `Database.ensureSchema()` so it no longer creates canonical tables as the primary schema path.

Possible transitional behavior:

```text
ensureSchema() may verify expected objects exist.
ensureSchema() may run small compatibility/backfill checks.
ensureSchema() should not be the main schema authority.
```

### Phase 5: Invert posting dependency

Current compatibility path:

```text
AccountingTransaction
  -> JournalRepository
  -> journal_transaction / journal_entry / transaction_info
  -> CanonicalJournalSyncAdapter
  -> txn / txn_split
```

Target path:

```text
PostingCommand or transaction editor model
  -> canonical PostingService
  -> txn / txn_split
```

During transition, if legacy UI or export code still needs the old journal shape, generate it from canonical data or maintain a compatibility mirror.

### Phase 6: Retire legacy journal tables

Only after code no longer writes or reads them as primary tables, remove or replace:

```text
journal_transaction
journal_entry
transaction_info
legacy_txn_map
account_fund
```

Possible end states:

```text
Drop them entirely.
Keep them as read-only compatibility views.
Keep mirror tables only for a short transitional release.
```

### Phase 7: Clean account identity

Current `account` is mixed:

```text
legacy code uses account_number
canonical code expects id
```

Target:

```text
account.id is the true primary key
account.code / account_number is a business code, not the database identity
legacy account_number references are migrated or removed
```

### Phase 8: Normalize people/donors/counterparties

Target preference:

```text
counterparty is the primary party table
payees, vendors, donors, merchants, and people are roles/dimensions around counterparty where practical
```

Do not keep separate person/donor/vendor tables unless they represent genuinely separate lifecycle concepts.

### Phase 9: Normalize supplies/inventory/assets

Target preference:

```text
Supplies fold into inventory.
Assets and inventory should either share a common item model or have a clearly documented boundary.
```

### Phase 10: Add future features through migrations only

After Flyway is authoritative, every new schema change should be a migration:

```text
V002__add_budget_category.sql
V003__add_reference_number_to_txn.sql
V004__add_reconciliation_indexes.sql
```

Do not add new schema through `ensureSchema()` or Hibernate `update`.

## Immediate next technical tasks

1. Generate or hand-author `V001__baseline_current_runtime_schema.sql` from the current runtime schema.
2. Add a test that creates a blank H2 database using Flyway only.
3. Start the app or core repository/service tests against that Flyway-created database.
4. Fix missing tables/columns until current DB-backed functions pass.
5. Switch Hibernate to `validate` only after step 4 passes.
6. Begin canonical posting cutover in small PRs.

## Design rule going forward

Use this rule for future schema decisions:

```text
If current code actively reads or writes a table, include it in the transitional baseline.
If a table is only useful for a future feature, do not include it yet.
If a table is a legacy duplicate, include it only until the current code stops depending on it.
If a table is a read model, include it but treat it as rebuildable.
If a table is import staging, include it only if import audit/review remains a current feature.
```

The first goal is compatibility. The second goal is cleanup. The cleanup should happen through later explicit Flyway migrations, not by making the initial baseline too idealized to run the current application.
