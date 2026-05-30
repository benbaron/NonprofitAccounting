This bundle adds a concrete SCLX import target plus a JavaFX panel/menu action to invoke it.

Included:
- importer options and account-import mode
- concrete staged records/repositories for budgets, banking items, bank statement imports
- `NonprofitBookkeepingSclxImportTarget`
- `SclxImportPanelFX`
- `ImportSclxActionFX`
- patched `NonprofitBookkeepingFX.java`

Notes:
- The panel requires an initialized database and an open company.
- Single-sided imported transactions are balanced to the configured Cash account reference.
- In `MAPPED` mode, the account mapping file is read as Java properties (`key=value`).
- This bundle is source-oriented and was not compile-checked against the full project classpath in the container.
