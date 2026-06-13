# Unused or Dead Code Modules

This catalog highlights classes and utilities in `src/main/java` that are not referenced elsewhere in the codebase (excluding generated Javadoc). The list was derived by searching for word-boundary references with `rg -w <ClassName>` and confirming no additional usages beyond the declaration files.【e38a7f†L1-L10】 Each entry includes its location and the observed status.

## Identified Items

- `nonprofitbookkeeping.api.ReportWriterIntf` — Interface defining a `writeReport` contract that has no implementing classes or callers.【F:src/main/java/nonprofitbookkeeping/api/ReportWriterIntf.java†L2-L26】 Consider removing it or wiring it into the active reporting framework.
- `nonprofitbookkeeping.model.ofx.FileUtils` — Static helper for stripping file extensions that is never invoked.【F:src/main/java/nonprofitbookkeeping/model/ofx/FileUtils.java†L2-L46】 Safe to delete unless future OFX work needs it.
- `nonprofitbookkeeping.model.ofx.OfxTags` — Large constant set of OFX tag names with no current references.【F:src/main/java/nonprofitbookkeeping/model/ofx/OfxTags.java†L18-L550】 Likely dead legacy from the jGnash import.
- `nonprofitbookkeeping.model.ofx.OfxV2JaxbWriter` — JAXB-based OFX 2.0 writer not referenced by any service or exporter.【F:src/main/java/nonprofitbookkeeping/model/ofx/OfxV2JaxbWriter.java†L2-L127】 Could be removed or hooked into the export workflow.
- `nonprofitbookkeeping.model.ofx.TransactionType` — Enum for investment-style transaction types that is never used; existing export logic builds transaction type strings manually instead.【F:src/main/java/nonprofitbookkeeping/model/ofx/TransactionType.java†L1-L25】 Candidate for deletion.
- `nonprofitbookkeeping.reports.jasper.BalanceSheetJasperGenerator` — Legacy Jasper generator registered in bundle metadata but overridden by `BalanceResultReportGenerator` in the default registry (`putIfAbsent` ignores the bundle entry), so it is never reachable at runtime.【03ebcc†L1-L8】【F:src/main/java/nonprofitbookkeeping/service/ReportService.java†L210-L299】【F:src/main/java/nonprofitbookkeeping/reports/jasper/BalanceSheetJasperGenerator.java†L2-L70】 Remove or align the default registry to use it.
- `nonprofitbookkeeping.ui.actions.scaledger.ExcelFormulaApplier` — Placeholder clone-only implementation with no call sites.【F:src/main/java/nonprofitbookkeeping/ui/actions/scaledger/ExcelFormulaApplier.java†L1-L46】 Safe to delete until a real spreadsheet formula engine is needed.
- `nonprofitbookkeeping.ui.actions.scaledger.ExcelTableReader` — Swing-based Excel-to-table utility not referenced in code.【F:src/main/java/nonprofitbookkeeping/ui/actions/scaledger/ExcelTableReader.java†L1-L95】 Remove or replace with an active importer.
- `nonprofitbookkeeping.ui.helpers.ReportCriteriaDialog` — Standalone JavaFX dialog helper that is not invoked by panels or controllers.【F:src/main/java/nonprofitbookkeeping/ui/helpers/ReportCriteriaDialog.java†L2-L527】 Consider deleting or integrating with the report-launch UI.

## Next Steps
- Decide whether to delete these classes to reduce maintenance overhead, or wire them into the relevant workflows if they represent planned features.
- If any items are retained for future work, consider moving them under a clearly labeled `experimental` package to avoid confusion.
