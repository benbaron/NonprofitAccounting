# Flyway migrations

This directory is the start of the transition from `Database.ensureSchema()` and Hibernate `hbm2ddl.auto=update` toward explicit, versioned database migrations.

## Current strategy

The current codebase is still a hybrid runtime model:

- legacy journal write/read tables: `journal_transaction`, `journal_entry`, `transaction_info`
- canonical posting/read-model tables: `txn`, `txn_split`, `fund`, `counterparty`, JPA lookup/link tables
- operational tables for donations, grants, banking, fund transfers, depreciation, and read models
- repository-owned import/staging tables that still self-create in their repositories

Therefore `V001__baseline_current_runtime_schema.sql` is intentionally a **transitional runtime baseline**, not the final canonical accounting schema.

## Do not switch Hibernate yet

Do not switch Hibernate from `update` to `validate` until a fresh database created only from Flyway is proven to satisfy the active code and JPA mappings.

## Next phases

1. Make Flyway run before JPA at startup.
2. Compare a fresh Flyway-created DB against a fresh `Database.ensureSchema()` DB.
3. Add missing repository-owned staging tables into follow-up migrations.
4. Switch Hibernate to `validate`.
5. Reduce `Database.ensureSchema()` to compatibility/backfill logic.
6. Migrate the posting facade from legacy `JournalRepository` writes to canonical `PostingService` writes.
7. Remove or replace legacy `journal_*` tables only after current services no longer depend on them.
