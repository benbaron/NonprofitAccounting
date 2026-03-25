This source bundle adds concrete SCLX import/export classes for the existing
NonprofitBookkeeping archive.

Included:
- updated importer options with cash-account and account-mapping controls
- plain staged records for:
  - BankStatementRecord
  - BudgetRecord
  - BankingItemRecord
- dedicated repositories for those staged records
- concrete NonprofitBookkeepingSclxImportTarget
- concrete NonprofitBookkeepingSclxExportService

Notes:
- Transactions import through JournalLedgerPersistenceGateway.
- Single-sided / unbalanced SCLX transactions are balanced to the configured Cash account.
- Budgets, banking items, and bank statement imports are stored as concrete staged records.
- Unsupported collections still fall back to DocumentRepository preservation.
- The export service emits the core collections supported by the current archive and staged repositories.

Dependencies assumed from the existing archive:
- Jackson
- H2 / Database.get()
- existing model / persistence / service packages
