# NonprofitBookkeeping SCLX concrete import target

This source set adds a concrete `NonprofitBookkeepingSclxImportTarget` that imports:

- accounts -> `AccountRepository`
- people -> `PersonRepository`
- transactions -> `JournalLedgerPersistenceGateway`
- supplemental items -> attached to imported transactions as `TxnSupplementalLineBase` rows

Collections that do not yet have stable first-class persistence in the current archive are preserved through `DocumentRepository` as raw JSON documents under names such as:

- `sclx.import.organization`
- `sclx.import.reportingPeriod`
- `sclx.import.funds`
- `sclx.import.budgets`
- `sclx.import.outstandingItems`
- `sclx.import.assets`
- `sclx.import.supplies`

## Balancing behavior

If the imported SCLX transaction is single-sided or net-unbalanced and `cashAccountReference` is configured in `SclxImportOptions`, the target adds a balancing cash entry automatically.

## Account resolution

- `AccountImportMode.AS_IS`: uses SCLX account references directly and upserts accounts from `chartOfAccounts`
- `AccountImportMode.MAPPED`: resolves account references through `accountMapping` and skips SCLX account upserts

## Notes

This target is intentionally conservative for budgets/funds/assets/supplies because the current archive does not yet expose stable dedicated repositories for all of them.
