You are working in:

`benbaron/NonprofitAccounting`

This is a **Maven** Java project. Do not use Gradle.

## Overall project direction

The project is retiring broad Java schema authority from:

```text
nonprofitbookkeeping.core.Database#ensureSchema()
```

and moving schema ownership to Flyway.

The working rule is:

```text
Flyway owns schema.
JPA validates schema.
Database.ensureSchema() remains only as a temporary compatibility/backfill bridge.
```

The long-term goal is for fresh development databases to be created by Flyway, validated by Hibernate/JPA, and not depend on Java `CREATE TABLE`, `ALTER TABLE`, or schema-shaping logic in `Database.ensureSchema()`.

## Completed steps so far

### 1. Flyway baseline established

Flyway migrations exist under:

```text
src/main/resources/db/migration/
```

`FlywayBaselineValidationTest` validates that Flyway creates the current runtime minimum schema.

### 2. Company-profile guard completed

A guard test was added proving that after Flyway creates `COMPANY_PROFILE`, running `Database.ensureSchema()` does not change the `COMPANY_PROFILE` column set.

### 3. Company-profile Java DDL cleanup completed

`Database.ensureSchema()` no longer calls:

```java
ensureCompanyProfile(st);
```

The `ensureCompanyProfile(...)` helper was removed.

Java no longer creates or alters `company_profile`.

### 4. Party/profile guard completed

A guard test was added proving that after Flyway creates the party/profile tables, running `Database.ensureSchema()` does not change column sets for:

```text
PERSON
DONOR
COUNTERPARTY
MERCHANT
```

### 5. Party/profile Java DDL cleanup completed

Duplicate Java table-shape DDL for `donor` and `person` was removed from `ensurePeopleAndCounterparty(...)`.

Compatibility repair/index/constraint statements were preserved.

### 6. Fund-transfer integrity guard completed

PR #684 was merged.

It added:

```text
EnsureSchemaFundTransferIntegrityCompatibilityValidationTest
```

That guard proves that after Flyway creates the fund-transfer integrity table group, running `Database.ensureSchema()` does not change column sets for:

```text
FUND_TRANSFER
FUND_TRANSFER_STATUS_TRANSITION
FUND_TRANSFER_INTEGRITY_EVENT
FUND_TRANSFER_REPAIR_QUEUE
```

### 7. Fund-transfer integrity Java DDL helper prepared

PR #685 is open or should be merged/copied before the current task.

It adds:

```text
tools/schema/remove_fund_transfer_integrity_java_ddl.py
```

The helper removes exact Java `CREATE TABLE` blocks from `ensureFundTransferIntegrityArtifacts()` for:

```text
fund_transfer_status_transition
fund_transfer_integrity_event
fund_transfer_repair_queue
```

It intentionally preserves:

```text
status-transition MERGE seed
foreign-key repair statements
index statements
```

## Current task

Complete the actual production-code cleanup for the fund-transfer integrity table-shape DDL.

This task should produce a PR that changes:

```text
src/main/java/nonprofitbookkeeping/core/Database.java
```

The PR should remove duplicate Java `CREATE TABLE` DDL that Flyway already owns.

## Current task workflow

Start from current `main`.

If PR #685 is already merged, the helper should already exist.

If PR #685 is not merged, either merge it first or copy its helper script into the branch:

```text
tools/schema/remove_fund_transfer_integrity_java_ddl.py
```

Run:

```bash
python tools/schema/remove_fund_transfer_integrity_java_ddl.py
```

Inspect:

```bash
git diff -- src/main/java/nonprofitbookkeeping/core/Database.java
```

Expected diff:

```text
- remove CREATE TABLE block for fund_transfer_status_transition
- remove CREATE TABLE block for fund_transfer_integrity_event
- remove CREATE TABLE block for fund_transfer_repair_queue
```

Expected preserved code:

```text
MERGE INTO fund_transfer_status_transition ...
ALTER TABLE fund_transfer_integrity_event ADD CONSTRAINT ...
ALTER TABLE fund_transfer_repair_queue ADD CONSTRAINT ...
CREATE INDEX IF NOT EXISTS ix_ft_repair_open ...
```

Do not remove the `MERGE` seed in this stage.

Do not remove FK repair statements in this stage.

Do not remove index statements in this stage.

Do not modify Flyway migrations in this stage.

Do not change posting/write-ledger behavior.

Run focused Maven tests:

```bash
mvn -Dtest=FlywayBaselineValidationTest,EnsureSchemaFundTransferIntegrityCompatibilityValidationTest test
```

Optionally run the broader suite:

```bash
mvn test
```

Commit:

```bash
git add src/main/java/nonprofitbookkeeping/core/Database.java
git commit -m "Remove duplicate fund transfer integrity Java DDL"
```

Open a PR titled:

```text
Remove duplicate fund transfer integrity Java DDL
```

## Future roadmap

### Stage A — Move fund-transfer status-transition seed to Flyway

After the current PR lands, inspect the remaining `ensureFundTransferIntegrityArtifacts()` body.

The likely remaining seed statement is:

```text
MERGE INTO fund_transfer_status_transition ...
```

Do not delete this blindly.

Next steps:

1. Add a test proving Flyway alone seeds the expected `fund_transfer_status_transition` rows.
2. If Flyway does not already seed them, add a new Flyway migration.
3. Move the seed rows into Flyway.
4. Remove the Java `MERGE` seed from `ensureFundTransferIntegrityArtifacts()`.
5. Run Maven tests.

Suggested test:

```text
src/test/java/nonprofitbookkeeping/schema/FundTransferStatusTransitionSeedValidationTest.java
```

Suggested Maven command:

```bash
mvn -Dtest=FlywayBaselineValidationTest,EnsureSchemaFundTransferIntegrityCompatibilityValidationTest,FundTransferStatusTransitionSeedValidationTest test
```

Suggested PR title:

```text
Move fund transfer status transition seed out of ensureSchema
```

### Stage B — Handle fund-transfer integrity FK/index repair

After seed data is moved, inspect remaining statements such as:

```text
ALTER TABLE fund_transfer_integrity_event ADD CONSTRAINT ...
ALTER TABLE fund_transfer_repair_queue ADD CONSTRAINT ...
CREATE INDEX IF NOT EXISTS ix_ft_repair_open ...
```

Classify each statement:

```text
schema constraint
schema index
data repair
obsolete duplicate
```

If Flyway already owns the FK/index shape, add or extend a guard test to prove `ensureSchema()` does not change the FK/index set.

Then remove duplicate Java FK/index DDL in a separate PR.

Suggested PR title:

```text
Remove duplicate fund transfer integrity FK/index Java DDL
```

### Stage C — Decide whether `ensureFundTransferIntegrityArtifacts()` can be deleted

After table DDL, seed data, FK repair, and index repair are removed or migrated, check whether the method has any remaining useful compatibility behavior.

If it is empty or only redundant, remove:

```java
ensureFundTransferIntegrityArtifacts(st);
```

from `ensureSchema()` and delete the helper method.

Suggested PR title:

```text
Retire ensureFundTransferIntegrityArtifacts
```

### Stage D — Move to reporting schedule configuration tables

Next likely bounded group:

```text
runReportingScheduleConfigurationMigration
schedule_kind
report_section
account_report_section
account_schedule_requirement
account_subtype_schedule_default
```

Pattern:

1. Add guard test proving Flyway owns the column sets.
2. Move any seed/default rows to Flyway if needed.
3. Remove duplicate Java table/column DDL.
4. Preserve true compatibility/backfill behavior until separately reviewed.

Suggested PR sequence:

```text
Validate ensureSchema preserves reporting schedule columns
Move reporting schedule seed data to Flyway
Remove duplicate reporting schedule Java DDL
Retire reporting schedule Java migration helper
```

### Stage E — Move operational linkage tables

Next likely group:

```text
ensureOperationalLinkageTables
runOperationalLinkBackfillMigration
```

Pattern:

1. Identify all tables created by this path.
2. Confirm Flyway creates them.
3. Add `ensureSchema()` idempotence guard.
4. Move backfill data to Flyway or decide it is obsolete for development DBs.
5. Remove Java DDL.

Suggested PR sequence:

```text
Validate ensureSchema preserves operational linkage columns
Remove duplicate operational linkage Java DDL
Retire operational linkage Java backfill if obsolete
```

### Stage F — Move banking and reconciliation schema

Next likely group:

```text
runBankingReconciliationSchemaMigration
BANK_STATEMENT
BANK_ID_RECORD
BANKING_TRANSACTION_RECORD
LEDGER_RECORD
```

Pattern:

1. Confirm Flyway owns table/column shape.
2. Add guard test.
3. Preserve real reconciliation data backfills until reviewed.
4. Remove duplicate Java schema creation.

Suggested PR sequence:

```text
Validate ensureSchema preserves banking reconciliation columns
Remove duplicate banking reconciliation Java DDL
Move reconciliation backfill to Flyway or retire it
```

### Stage G — Inventory remaining legacy tables

The risky areas are:

```text
ensureAccountAndLegacyJournalTables
ensureRemainingLegacyTables
backfillLegacyTxnMap
```

These may involve active legacy write paths and should not be removed casually.

Before deleting Java DDL here:

1. Identify active code paths still writing legacy journal/account tables.
2. Decide whether legacy journal remains write authority or canonical `txn`/`txn_split` is now the write authority.
3. Add tests for fresh Flyway-created DB startup.
4. Only then remove duplicate Java DDL.

Suggested PR sequence:

```text
Inventory legacy ensureSchema responsibilities
Validate Flyway owns legacy journal/account table shapes
Remove duplicate legacy journal Java DDL
Move legacy transaction map backfill or retire it
```

### Stage H — Compatibility views

Inspect:

```text
ensureCompatibilityViews
```

Views are different from table DDL. Decide whether active code still queries them.

If active code uses them:

```text
move view definitions to Flyway
validate view existence in tests
remove Java CREATE VIEW logic
```

If active code does not use them:

```text
remove obsolete views and callers
```

Suggested PR title:

```text
Move compatibility view definitions to Flyway
```

### Stage I — Final ensureSchema retirement

When all schema-shaping logic is gone, `ensureSchema()` should no longer create canonical schema.

Final desired startup model:

```text
Database.init(...)
Flyway migrate
Hibernate/JPA validate
small compatibility/backfill routines only if still needed
```

At the end, either delete `ensureSchema()` or rename/reduce it to something more honest, such as:

```text
runLegacyCompatibilityBackfills()
runStartupDataRepairs()
```

## Final acceptance criteria

```text
1. Fresh development databases are created by Flyway.
2. Hibernate/JPA validates against the Flyway-created schema.
3. Database.ensureSchema() no longer creates or alters canonical schema objects.
4. Any remaining Java startup database work is clearly limited to data repair, data backfill, or temporary compatibility.
5. The normal database-open path is covered by tests.
```

## Streamlined rules for future slices

Use a risk-tiered workflow rather than one guard PR for every small removal.

```text
1. Remove duplicate Java DDL whenever Flyway already creates the same table/column/index/constraint and an existing schema test covers the affected area.
2. Add a new guard test only when coverage is missing, the object is high-risk, or behavior is ambiguous.
3. Group related removals by subsystem instead of one object at a time.
4. Keep seed/backfill/repair behavior until it is either moved to Flyway, proven obsolete, or explicitly reclassified.
5. Use Maven, not Gradle.
6. Avoid broad formatting changes.
7. Do not change posting/write-ledger behavior unless the PR is explicitly about that.
```

## Practical risk tiers

```text
Low risk:
- Duplicate CREATE TABLE / ALTER TABLE / CREATE INDEX already present in Flyway baseline.
- Existing tests validate the table/column group.
- No data transformation or posting behavior involved.
Action: remove in a grouped cleanup PR.

Medium risk:
- Seed/default data, compatibility views, or startup repair statements.
- Existing tests partially cover the area.
Action: add or extend one focused test, then move/remove the Java code.

High risk:
- Legacy journal/account identity model.
- Posting/write-ledger behavior.
- Backfills that transform real transaction data.
- Anything affecting canonical txn/txn_split writes.
Action: guard first, then remove in smaller PRs.
```

## Preferred PR pattern

```text
1. Identify a subsystem.
2. Confirm Flyway owns its schema shape.
3. Confirm existing test coverage or add one focused test.
4. Remove all clearly duplicate Java schema DDL for that subsystem in one PR.
5. Leave true seed/backfill/repair code for a separate PR unless it is obviously obsolete.
```

## Current project rule of thumb

```text
Do not require a new guard test for every table.
Require enough coverage for the subsystem being changed.
```

Maven focused-test pattern:

```bash
mvn -Dtest=TestClassOne,TestClassTwo test
```

Broader test command:

```bash
mvn test
```
