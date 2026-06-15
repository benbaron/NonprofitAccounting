# Schema authority drift report

## Purpose

This report records the current database schema authority model and the remaining drift after the first Flyway transition work.

The repository has moved past the original state where Flyway was only a dependency and Hibernate could still mutate schema. The current transition model is now:

```text
Flyway migrations run first.
Database.ensureSchema() still runs afterward as a temporary compatibility/backfill path.
JPA/Hibernate validates the resulting schema and must not create or alter tables.
```

Because this project is still in early development and there are no significant legacy databases to preserve, the preferred direction is to retire broad Java DDL aggressively rather than preserve long-lived compatibility machinery.

## Current authority status

### Flyway

Flyway is now the forward migration mechanism.

`nonprofitbookkeeping.core.FlywayMigrationRunner` runs Flyway against the selected H2 database using:

```text
classpath:db/migration
baselineOnMigrate(true)
baselineVersion("0")
```

Flyway runs before the compatibility schema path when a database is opened.

New schema changes should be made as versioned migrations under:

```text
src/main/resources/db/migration/V###__description.sql
```

New tables, new columns, new constraints, and new indexes should not be added directly to `Database.ensureSchema()` unless they are explicitly temporary compatibility/backfill logic.

### `Database.ensureSchema()`

`Database.ensureSchema()` is no longer the desired long-term schema authority, but it is still active in the runtime database-open path.

It currently remains a compatibility/backfill layer after Flyway. It still creates or adjusts many schema objects, including legacy journal tables, canonical JPA tables, constraints, compatibility views, people/counterparty tables, operational linkage tables, banking/reconciliation structures, reporting schedule configuration, and finance posting preflight checks.

The next goal is to shrink this method so that it no longer creates canonical schema. It should eventually become one of these:

```text
1. a narrow backfill/repair coordinator, or
2. a temporary development-only compatibility hook, or
3. deleted once Flyway fully owns fresh development databases.
```

### JPA / Hibernate

JPA describes the intended canonical object model under `org.nonprofitbookkeeping.model`.

Hibernate is now configured as validation-only:

```text
hibernate.hbm2ddl.auto=validate
```

This is the correct posture for the transition. Hibernate should not be allowed to silently create or alter schema. If validation fails, the fix should be a Flyway migration or an intentional model/schema alignment change, not a return to `hbm2ddl.auto=update`.

## Current runtime sequence

The desired runtime database-open sequence is:

```text
Database.init(selectedPath)
FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled()
Database.get().ensureSchema()       // temporary compatibility/backfill layer
JPA bootstrap validates schema
```

This ordering is intentionally conservative while `ensureSchema()` is being retired. It lets Flyway start becoming the source of forward schema changes without breaking code paths that still rely on existing Java DDL/backfill behavior.

## Remaining drift and cleanup areas

### 1. `account` identity model is still mixed

The legacy table shape uses:

```text
account.account_number
```

as the original primary identifier, while the canonical JPA model expects:

```text
account.id
```

as the primary key and treats `chart_id + code` as the business uniqueness rule.

Target direction:

```text
Fresh Flyway databases should prefer the canonical id-based shape.
Legacy account_number compatibility should be isolated to legacy import/journal compatibility paths.
```

### 2. Legacy journal tables remain write authority

The current write model is intentionally hybrid:

```text
journal_transaction / journal_entry = write ledger for now
txn / txn_split = synchronized canonical mirror/reference layer
```

This is now documented separately in the posting model docs. Do not remove `journal_*` writes until there is an explicit posting cutover plan.

Target direction:

```text
Short term: keep PostingFacade as the write boundary.
Medium term: prove every write produces correct legacy and canonical rows.
Later: decide whether canonical txn/txn_split become write authority.
```

### 3. `ensureSchema()` still creates canonical tables and constraints

Even with Flyway active, `ensureSchema()` still contains substantial canonical DDL. This is acceptable only as a transitional bridge.

Target direction:

```text
Move bounded groups of DDL into Flyway migrations.
After each group, remove or gate the duplicate Java DDL.
Keep only data repair/backfill code that cannot be expressed cleanly as migration SQL.
```

### 4. Relationship and constraint enforcement still needs mechanical verification

The earlier drift review identified areas that need fresh metadata verification after the Flyway work:

```text
account uniqueness and nullability
fund hierarchy constraints and indexes
txn_split activity/merchant references
alias/link table constraints
counterparty and merchant notes columns
txn_split amount scale versus cents-only checks
fund_transfer constraints
```

Some of these may already be fixed by the new migrations or validation gates. They should not be treated as current defects until checked against the fresh Flyway-created database metadata.

Target direction:

```text
Use a metadata test/tool to compare fresh Flyway schema, post-ensureSchema schema, and JPA validation requirements.
```

### 5. Development databases can be reset aggressively

Because there are no significant legacy databases to preserve, it is acceptable to avoid elaborate compatibility work for old experimental database shapes.

Target direction:

```text
Prefer clean Flyway baseline plus documented dev DB reset over complex compatibility code.
Avoid preserving awkward legacy schema decisions unless they still support active write/import paths.
```

## Policy for new schema work

Effective immediately:

```text
1. New schema objects go in Flyway migrations.
2. JPA remains hbm2ddl.auto=validate.
3. Do not re-enable Hibernate update.
4. Do not add new canonical CREATE TABLE / ALTER TABLE statements to ensureSchema().
5. ensureSchema() changes must be labeled as compatibility, repair, or backfill.
6. Any temporary duplicate DDL between Flyway and ensureSchema() needs a retirement note.
```

## Recommended next implementation sequence

1. Add `doc/ensure-schema-retirement-checklist.md` and classify every `ensureSchema()` helper as Flyway-owned, still-to-migrate, compatibility/backfill, or removable.
2. Add or extend a schema metadata validation test so that fresh Flyway databases and post-compatibility databases can be compared mechanically.
3. Move the next bounded `ensureSchema()` group into Flyway.
4. Remove or guard the corresponding duplicate Java DDL.
5. Repeat until `ensureSchema()` no longer creates canonical schema.
6. Only after schema authority is clean, revisit the write-authority cutover from legacy journal tables to canonical `txn` / `txn_split`.

## Near-term PR queue

Recommended next PRs:

```text
1. Add ensureSchema retirement checklist.                  // this planning slice
2. Add schema metadata snapshot/validation tool.
3. Move one bounded schema group from ensureSchema to Flyway.
4. Document MainWindowAlternate parity checklist.
5. Continue posting model tests around PostingFacade and canonical references.
```

The main architectural rule is now simple:

```text
Flyway changes schema.
JPA validates schema.
ensureSchema survives only as a temporary compatibility/backfill bridge.
```
