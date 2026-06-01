# Flyway baseline transition plan

## Decision context

This project is still in early development. There are no significant production or historical legacy databases that must be preserved exactly.

That changes the schema strategy. The application does not need to carry a long compatibility tail for many unknown legacy H2 files. It can move more aggressively to one clear schema authority.

## Decision

Move toward this authority split:

```text
Flyway
  Owns database creation and versioned schema changes.

JPA / Hibernate
  Owns the Java object model and validates that the database matches it.

Database.ensureSchema()
  Becomes temporary compatibility/bootstrap code only, then is reduced or removed for canonical schema creation.
```

The desired Hibernate setting is:

```text
hibernate.hbm2ddl.auto=validate
```

not:

```text
hibernate.hbm2ddl.auto=update
```

Hibernate should not silently patch the schema once Flyway is authoritative.

## Why not keep `ensureSchema()` as the authority?

`Database.ensureSchema()` has been useful because it can create tables, alter old databases, run backfills, and repair inconsistent development files.

But it has disadvantages as the long-term schema authority:

1. It accumulates conditional DDL in Java code.
2. It does not provide a clean migration history.
3. It can diverge from JPA entity mappings.
4. It is harder to review than ordered SQL migration files.
5. It makes it unclear whether a database was changed by Java DDL, Hibernate `update`, or a migration tool.

Because this is early development, we can avoid entrenching that pattern.

## Why Flyway now?

Flyway is a good fit once schema changes need to become explicit and reviewable.

A migration history gives the project files such as:

```text
src/main/resources/db/migration/V001__baseline_schema.sql
src/main/resources/db/migration/V002__add_budget_category.sql
src/main/resources/db/migration/V003__add_report_template_tables.sql
```

Each schema change becomes a concrete artifact that can be reviewed, tested, and applied in order.

## Proposed transition phases

### Phase 0: Freeze the direction

Adopt this rule for new work:

```text
New canonical schema changes should be written as Flyway migrations, not new ad hoc DDL in Database.ensureSchema().
```

During this phase, `ensureSchema()` may still exist so the application keeps running.

### Phase 1: Choose the canonical model

Before writing the baseline migration, decide which current tables are canonical and which are compatibility/staging.

Canonical candidates:

```text
chart_of_accounts
account
fund
counterparty
txn
txn_split
activity
merchant
schedule_kind
report_section
account_alias
fund_alias
account_report_section
account_schedule_requirement
account_subtype_schedule_default
fund_transfer
fund_transfer_status_transition
fund_transfer_integrity_event
```

Compatibility or staging candidates:

```text
journal_transaction
journal_entry
transaction_info
txn_supplemental_line
company_profile
company_store
document
json_storage
donor
person
undeposited_funds_item
imported_* tables
```

This list should be verified against current UI and service dependencies before deleting or demoting anything.

### Phase 2: Create `V001__baseline_schema.sql`

Create a first baseline migration that builds the chosen development schema from scratch.

Because there are no significant legacy databases, the baseline does not need to preserve every historical table shape. It should define the intended application schema.

Important baseline choices:

1. Decide whether `account.account_number` remains a permanent field or only a legacy/import alias.
2. Make `account.id` the real primary key if the JPA model is canonical.
3. Enforce `account.chart_id NOT NULL` if `Account.chart` remains mandatory.
4. Add `uq_account_code(chart_id, code)` if account code uniqueness is per chart.
5. Align `txn_split.amount_signed` scale and check constraints.
6. Include `counterparty.notes` and `merchant.notes` if the JPA entities keep those fields.
7. Add FKs and indexes for `activity_id`, `merchant_id`, aliases, report-section links, schedule requirements, and fund hierarchy.
8. Decide whether read-model tables are baseline tables or later migration-created tables.

### Phase 3: Add a Flyway runner

Add an application startup/migration path that calls Flyway explicitly before JPA is initialized.

The startup order should be:

```text
open/create database
run Flyway migrations
start JPA with hbm2ddl.auto=validate
open UI
```

This avoids Hibernate attempting to create/alter tables before Flyway runs.

### Phase 4: Switch Hibernate to validate

After the baseline migration and any required follow-up migrations create the schema JPA expects, change both places currently using `update`:

```text
src/main/resources/META-INF/persistence.xml
src/main/java/org/nonprofitbookkeeping/persistence/Jpa.java
```

from:

```text
hibernate.hbm2ddl.auto=update
```

to:

```text
hibernate.hbm2ddl.auto=validate
```

### Phase 5: Reduce `ensureSchema()`

After Flyway can create a fresh development database successfully, reduce `Database.ensureSchema()`.

Possible end-state:

```text
Database.ensureSchema()
  - verifies database availability
  - optionally runs compatibility checks
  - optionally invokes Flyway
  - does not create canonical tables directly
```

If all development databases can be recreated, the old DDL blocks can be removed rather than maintained.

## Suggested immediate implementation sequence

1. Add this plan and migration directory documentation.
2. Add a metadata/snapshot test that records the current `ensureSchema()` schema output.
3. Decide canonical-vs-compatibility table list.
4. Generate or hand-author `V001__baseline_schema.sql` from the chosen canonical model.
5. Add a Flyway runner/service.
6. Create a fresh database from Flyway and compare it against JPA validation.
7. Switch Hibernate to `validate`.
8. Remove or quarantine canonical DDL from `ensureSchema()`.

## Non-goals for the first baseline PR

Do not attempt to preserve every old development database automatically.

Do not allow both Flyway and Hibernate `update` to act as schema mutators long-term.

Do not copy the current `ensureSchema()` output blindly if it preserves accidental legacy compromises.

## Open questions

1. Should `journal_transaction` / `journal_entry` remain real tables, or should the canonical `txn` / `txn_split` model replace them entirely?
2. Should `person` and `donor` collapse into `counterparty`, or remain separate workflow tables?
3. Should imported SCLX records stay as staging tables permanently, or only until promotion into native models?
4. Should read-model tables be rebuilt projections rather than canonical persisted data?
5. Should development users be expected to recreate databases when the baseline changes before the app reaches production maturity?
