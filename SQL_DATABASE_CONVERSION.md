# SQL Database Conversion Notes

## Progress
- Converted several core service classes to use SQL persistence via `DatabaseManager`.
- Added H2 database dependency in `pom.xml` and implemented a `DatabaseManager` utility for connection handling and table creation.
- Implemented SQL table creation for modules such as accounts, donors, inventory, grants, funds, customers, budgets, and report configurations.
- Added database backup support through `DatabaseManager.backupDatabase(File)`.

- Major ledger operations now persist transactions and entries via `TransactionService` and `DatabaseManager`, though components like `ReconciliationService` remain largely stubbed.
- Table definitions in `DatabaseManager` partially diverge from `SQL_SCHEMA_PROPOSAL.md` (e.g., donation tracking). Align schema and code.
- Implement SQL-backed implementations for remaining services including file import/export and advanced reconciliation logic.
- Ensure foreign key relationships and constraints match the proposed schema.
- Investigate Maven build failures due to network restrictions and provide an offline-friendly build approach if needed.

