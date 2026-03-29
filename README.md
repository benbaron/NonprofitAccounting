# NonprofitBookkeeping SCLX concrete import target (v2)

This source bundle contains an updated concrete `NonprofitBookkeepingSclxImportTarget`
that now imports these SCLX collections into concrete records instead of caching them
as raw collection JSON:

- `organization` -> `nonprofitbookkeeping.model.impex.OrganizationRecord`
- `reportingPeriod` -> `nonprofitbookkeeping.model.impex.ReportingPeriodRecord`
- `funds` -> `nonprofitbookkeeping.model.impex.FundRecord`
- `budgets` -> `nonprofitbookkeeping.model.impex.BudgetRecord`
- `events` -> `nonprofitbookkeeping.model.impex.EventRecord`
- `documents` -> `nonprofitbookkeeping.model.impex.DocumentRecord`
- `outstandingItems` -> `nonprofitbookkeeping.model.impex.OutstandingItemRecord`
- `otherAssetItems` -> `nonprofitbookkeeping.model.impex.OtherAssetItemRecord`
- `assets` -> `nonprofitbookkeeping.model.impex.AssetRecord`
- `supplies` -> `nonprofitbookkeeping.model.impex.SupplyRecord`
- `bankingItems` -> `nonprofitbookkeeping.model.impex.BankingItemRecord`
- `bankStatementImports` -> `nonprofitbookkeeping.model.impex.BankStatementRecord`

## What changed

The target now persists the remaining stable SCLX collections through concrete repositories:

- `OrganizationRecordRepository`
- `ReportingPeriodRecordRepository`
- `FundRecordRepository`
- `BudgetRecordRepository`
- `EventRecordRepository`
- `DocumentRecordRepository`
- `OutstandingItemRecordRepository`
- `OtherAssetItemRecordRepository`
- `AssetRecordRepository`
- `SupplyRecordRepository`
- `BankingItemRecordRepository`
- `BankStatementRecordRepository`

Those repositories create and upsert into concrete staging/import tables named:

- `imported_organization_record`
- `imported_reporting_period_record`
- `imported_fund_record`
- `imported_budget_record`
- `imported_event_record`
- `imported_document_record`
- `imported_outstanding_item_record`
- `imported_other_asset_item_record`
- `imported_asset_record`
- `imported_supply_record`
- `imported_banking_item_record`
- `imported_bank_statement_record`

## What still uses raw document preservation

The target still preserves only import summary / unmatched supplemental items
through `DocumentRepository`. The staging-oriented concrete record path now covers
the stable top-level SCLX collections listed above.

## Included source files

### Concrete target
- `nonprofitbookkeeping.importer.sclx.NonprofitBookkeepingSclxImportTarget`
- `nonprofitbookkeeping.importer.sclx.SclxImportOptions`
- `nonprofitbookkeeping.importer.sclx.AccountImportMode`

### Final records
- `nonprofitbookkeeping.model.impex.BankStatementRecord`
- `nonprofitbookkeeping.model.impex.BudgetRecord`
- `nonprofitbookkeeping.model.impex.BankingItemRecord`

### Concrete repositories
- `nonprofitbookkeeping.persistence.impex.BankStatementRecordRepository`
- `nonprofitbookkeeping.persistence.impex.BudgetRecordRepository`
- `nonprofitbookkeeping.persistence.impex.BankingItemRecordRepository`

### JSON support / mappers
- `nonprofitbookkeeping.importer.sclx.jackson.*`
- `nonprofitbookkeeping.importer.sclx.mapping.*`

## Dependencies

You will need Jackson with JSR-310 support:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.2</version>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
    <version>2.17.2</version>
</dependency>
```

The source also assumes the existing NonprofitBookkeeping archive classes are present,
especially:
- `JournalLedgerPersistenceGateway`
- `AccountRepository`
- `PersonRepository`
- `DocumentRepository`
- `Account`
- `AccountingTransaction`
- `AccountingEntry`
- `Person`
- supplemental line model classes

## Notes

- The updated target still honors:
  - configurable Cash counter-account
  - account import mode (`AS_IS` vs `MAPPED`)
- single-sided or net-unbalanced SCLX transactions are balanced to the configured Cash account
- `BudgetRecord`, `BankingItemRecord`, and `BankStatementRecord` are imported concretely, not only archived as raw collection JSON
- the repositories are intentionally staging-oriented so these records can later be merged/promoted into richer native models

## Eclipse JavaFX launch

For Eclipse-specific startup instructions for `org.nonprofitbookkeeping.ui.FxMain`, see `doc/eclipse-javafx-launch.md`.
