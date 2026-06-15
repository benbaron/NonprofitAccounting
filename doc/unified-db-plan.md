# Unified database authority plan

## Current status

As of 2026-06-15, the project is in the middle of retiring `Database.ensureSchema()` as a canonical schema and seed authority.

The current working rule remains:

```text
Flyway owns schema and seed data that defines a fresh database.
JPA validates that the database matches the object model.
Database.ensureSchema() is only a temporary compatibility/backfill bridge.
```

The runtime database-open sequence is currently:

```text
Database.init(selectedPath)
FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled()
Database.get().ensureSchema()       // temporary compatibility/backfill layer
JPA bootstrap validates schema
```

Hibernate must remain configured with `hbm2ddl.auto=validate`; schema fixes should be Flyway migrations or explicit model/schema alignment changes, not a return to Hibernate schema mutation.

## Completed transition work

### Flyway baseline and follow-up migrations

Flyway is now active for the H2 application database and runs migrations from `classpath:db/migration`. The current baseline/follow-up migration set can create the runtime minimum schema used by the schema validation tests.

### JPA validation posture

The intended posture is now validation-only. JPA/Hibernate validates the resulting database instead of creating or altering schema objects.

### Runtime startup ordering

The runtime path now runs Flyway before the temporary compatibility/backfill `ensureSchema()` path. This ordering lets Flyway become the forward authority while preserving the remaining compatibility behavior until each group is retired.

### Fund-transfer integrity table shape

The fund-transfer integrity table shapes are Flyway-owned. The Java `CREATE TABLE` DDL for these artifacts has already been removed from `Database.ensureSchema()`:

```text
fund_transfer_status_transition
fund_transfer_integrity_event
fund_transfer_repair_queue
```

`EnsureSchemaFundTransferIntegrityCompatibilityValidationTest` protects the table-shape boundary by verifying that `ensureSchema()` does not change the Flyway-created fund-transfer integrity columns.

### Fund-transfer status-transition seed

The `fund_transfer_status_transition` lifecycle seed rows are now verified as Flyway-provided seed data. `FundTransferStatusTransitionSeedValidationTest` runs Flyway against a temporary H2 database without calling `Database.ensureSchema()` and asserts the expected transition matrix, including `from_status`, `to_status`, `is_allowed`, and `notes`.

Because the existing Flyway baseline already seeds those rows, no new `V010` seed migration was needed for this slice. The duplicate Java `MERGE INTO fund_transfer_status_transition` seed has been removed from `ensureFundTransferIntegrityArtifacts()`.

## Remaining `ensureSchema()` responsibilities

`ensureSchema()` is still active and still contains broad compatibility/schema/backfill behavior. It should continue to shrink in bounded, tested slices.

### Fund-transfer integrity compatibility that still remains

`ensureFundTransferIntegrityArtifacts()` still contains compatibility/repair DDL for:

```text
ALTER TABLE fund_transfer_integrity_event ADD CONSTRAINT IF NOT EXISTS fk_ft_integrity_event_transfer ...
ALTER TABLE fund_transfer_repair_queue ADD CONSTRAINT IF NOT EXISTS fk_ft_repair_transfer ...
CREATE INDEX IF NOT EXISTS ix_ft_repair_open ...
```

These are intentionally not removed yet. They should be handled in a later slice with separate metadata validation proving Flyway creates the constraints/indexes required for a fresh database and that removing the Java repair statements does not regress compatibility expectations.

### Other broad remaining areas

The remaining retirement areas include:

1. Company profile schema compatibility.
2. Legacy journal/account schema and the account identity model.
3. Canonical JPA table creation still duplicated in Java.
4. JPA constraints still duplicated or repaired in Java.
5. People, donor, counterparty, and merchant compatibility paths.
6. Operational linkage schema and backfills.
7. Reporting schedule configuration schema/seed behavior.
8. Banking/reconciliation schema and backfills.
9. Finance-posting preflight checks and repairs.
10. Compatibility views and legacy table cleanup.

## Current near-term implementation queue

Recommended next PRs:

1. Add or extend schema metadata validation for remaining fund-transfer integrity constraints and indexes.
2. Remove the remaining duplicate fund-transfer FK/index repair statements from `ensureFundTransferIntegrityArtifacts()` only after the validation exists and passes.
3. Continue moving one bounded `ensureSchema()` helper at a time into Flyway migrations or explicit compatibility/backfill code.
4. Keep documenting each removal as one of:
   - removal of duplicate Java DDL
   - compatibility repair
   - data backfill
   - seed migration
5. Avoid changing posting/write-ledger behavior while schema authority cleanup is underway.

## Non-goals for the next slices

Do not delete all of `ensureSchema()` at once.

Do not remove remaining fund-transfer FK/index compatibility statements without dedicated metadata coverage.

Do not change lifecycle semantics for fund-transfer status transitions while moving schema/seed authority.

Do not change posting or write-ledger authority as part of schema authority cleanup.

## Completion criteria

`ensureSchema()` is considered retired when:

1. Fresh development databases are fully created by Flyway.
2. Hibernate validation succeeds without Java DDL creating missing schema objects.
3. `ensureSchema()` no longer creates canonical tables, columns, constraints, indexes, or canonical seed data.
4. Any remaining `ensureSchema()` code is narrowly scoped compatibility, repair, or backfill behavior with an explicit retirement path.
5. Tests cover the normal database-open paths and Flyway-only fresh database creation.
