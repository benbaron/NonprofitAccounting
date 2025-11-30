# Potentially unused or dead code modules

This catalog lists Java types that appear unreferenced outside their defining
file based on a simple token scan. It is meant as a starting point for cleanup
work, not a definitive list—reflection, configuration files, or dynamic loading
may still require some of these classes.

## Method

1. Use `scripts/find_unused_modules.py` to scan `src/main/java` and count type
   name occurrences across both main and test sources.
2. Flag any class/interface/enum/record with zero references outside its own
   file, or whose only external references come from test sources (types
   containing a `public static void main` are skipped so entry points are not
   misclassified).
3. Review the report manually before deleting code, especially for reflective
   lookups or Jasper report beans.

Run the scan locally with:

```bash
python3 scripts/find_unused_modules.py
```

## Findings

The following types were flagged as unused (either no external references or only referenced from JUnit tests):

| Type | File | Reason |
| --- | --- | --- |
| net.sf.jasperreports.engine.xml.JacksonReportLoader | src/main/java/net/sf/jasperreports/engine/xml/JacksonReportLoader.java | No external references |
| nonprofitbookkeeping.api.ReportWriterIntf | src/main/java/nonprofitbookkeeping/api/ReportWriterIntf.java | No external references |
| nonprofitbookkeeping.core.ChartOfAccountsBuilder | src/main/java/nonprofitbookkeeping/core/ChartOfAccountsBuilder.java | No external references |
| nonprofitbookkeeping.core.JacksonDataStorer | src/main/java/nonprofitbookkeeping/core/JacksonDataStorer.java | Referenced only from tests |
| nonprofitbookkeeping.model.AccountDetailsImpl | src/main/java/nonprofitbookkeeping/model/AccountDetailsImpl.java | No external references |
| nonprofitbookkeeping.model.JournalEntry | src/main/java/nonprofitbookkeeping/model/JournalEntry.java | Referenced only from tests |
| nonprofitbookkeeping.model.ofx.FileUtils | src/main/java/nonprofitbookkeeping/model/ofx/FileUtils.java | No external references |
| nonprofitbookkeeping.model.ofx.InvestmentTransaction | src/main/java/nonprofitbookkeeping/model/ofx/InvestmentTransaction.java | Referenced only from tests |
| nonprofitbookkeeping.model.ofx.Ofx20Writer | src/main/java/nonprofitbookkeeping/model/ofx/Ofx20Writer.java | No external references |
| nonprofitbookkeeping.model.ofx.OfxTags | src/main/java/nonprofitbookkeeping/model/ofx/OfxTags.java | No external references |
| nonprofitbookkeeping.model.ofx.OfxV2JaxbWriter | src/main/java/nonprofitbookkeeping/model/ofx/OfxV2JaxbWriter.java | No external references |
| nonprofitbookkeeping.model.ofx.TransactionType | src/main/java/nonprofitbookkeeping/model/ofx/TransactionType.java | No external references |
| nonprofitbookkeeping.plugins.sample.SamplePlugin | src/main/java/nonprofitbookkeeping/plugins/sample/SamplePlugin.java | No external references |
| nonprofitbookkeeping.reports.ReportBundlePackager | src/main/java/nonprofitbookkeeping/reports/ReportBundlePackager.java | Referenced only from tests |
| nonprofitbookkeeping.reports.datasource.scareports.ASSET_DTL_5aBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/ASSET_DTL_5aBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.ASSET_DTL_5cBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/ASSET_DTL_5cBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.BALANCE_3Bean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/BALANCE_3Bean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.COMMENTSBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/COMMENTSBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.CONTACT_INFO_1Bean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/CONTACT_INFO_1Bean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.ContentsBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/ContentsBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.DEPR_DTL_8Bean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/DEPR_DTL_8Bean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.DEPR_DTL_8bBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/DEPR_DTL_8bBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.DEPR_DTL_8cBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/DEPR_DTL_8cBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.EXPENSE_DTL_12aBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/EXPENSE_DTL_12aBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.EXPENSE_DTL_12bBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/EXPENSE_DTL_12bBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.FINANCE_COMM_13Bean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/FINANCE_COMM_13Bean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.FUNDS_14Bean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/FUNDS_14Bean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.FreeFormBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/FreeFormBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.INCOME_4Bean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/INCOME_4Bean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.INCOME_DTL_11aBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/INCOME_DTL_11aBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.INCOME_DTL_11bBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/INCOME_DTL_11bBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.INCOME_DTL_11cBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/INCOME_DTL_11cBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.INVENTORY_DTL_6Bean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/INVENTORY_DTL_6Bean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.INVENTORY_DTL_6bBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/INVENTORY_DTL_6bBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.LIABILITY_DTL_5bBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/LIABILITY_DTL_5bBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.LIABILITY_DTL_5dBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/LIABILITY_DTL_5dBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.NEWSLETTER_15Bean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/NEWSLETTER_15Bean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.PRIMARY_ACCOUNT_2aBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/PRIMARY_ACCOUNT_2aBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.REGALIA_SALES_DTL_7Bean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/REGALIA_SALES_DTL_7Bean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.REGALIA_SALES_DTL_7bBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/REGALIA_SALES_DTL_7bBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.SECONDARY_ACCOUNTS_2bBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/SECONDARY_ACCOUNTS_2bBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.SECONDARY_ACCOUNTS_2cBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/SECONDARY_ACCOUNTS_2cBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.SECONDARY_ACCOUNTS_2dBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/SECONDARY_ACCOUNTS_2dBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.TRANSFER_IN_9Bean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/TRANSFER_IN_9Bean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.TRANSFER_IN_9bBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/TRANSFER_IN_9bBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.TRANSFER_IN_9cBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/TRANSFER_IN_9cBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.TRANSFER_IN_9dBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/TRANSFER_IN_9dBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.TRANSFER_OUT_10Bean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/TRANSFER_OUT_10Bean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.TRANSFER_OUT_10bBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/TRANSFER_OUT_10bBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.TRANSFER_OUT_10cBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/TRANSFER_OUT_10cBean.java | No external references |
| nonprofitbookkeeping.reports.datasource.scareports.TRANSFER_OUT_10dBean | src/main/java/nonprofitbookkeeping/reports/datasource/scareports/TRANSFER_OUT_10dBean.java | No external references |
| nonprofitbookkeeping.reports.jasper.BalanceSheetJasperGenerator | src/main/java/nonprofitbookkeeping/reports/jasper/BalanceSheetJasperGenerator.java | No external references |
| nonprofitbookkeeping.service.CustomerService | src/main/java/nonprofitbookkeeping/service/CustomerService.java | Referenced only from tests |
| nonprofitbookkeeping.service.FinancialFormulaService | src/main/java/nonprofitbookkeeping/service/FinancialFormulaService.java | Referenced only from tests |
| nonprofitbookkeeping.service.LedgerReportWriter | src/main/java/nonprofitbookkeeping/service/LedgerReportWriter.java | No external references |
| nonprofitbookkeeping.ui.actions.HelpAction | src/main/java/nonprofitbookkeeping/ui/actions/HelpAction.java | Referenced only from tests |
| nonprofitbookkeeping.ui.actions.ReportGenerator | src/main/java/nonprofitbookkeeping/ui/actions/ReportGenerator.java | No external references |
| nonprofitbookkeeping.ui.actions.scaledger.ExcelFormulaApplier | src/main/java/nonprofitbookkeeping/ui/actions/scaledger/ExcelFormulaApplier.java | No external references |
| nonprofitbookkeeping.ui.actions.scaledger.ExcelTableReader | src/main/java/nonprofitbookkeeping/ui/actions/scaledger/ExcelTableReader.java | No external references |
| nonprofitbookkeeping.ui.helpers.ReportCriteriaDialog | src/main/java/nonprofitbookkeeping/ui/helpers/ReportCriteriaDialog.java | No external references |
| nonprofitbookkeeping.ui.javafx.BudgetEditorDialogFX | src/main/java/nonprofitbookkeeping/ui/javafx/BudgetEditorDialogFX.java | Referenced only from tests |
| nonprofitbookkeeping.ui.panels.DashboardPanelFX | src/main/java/nonprofitbookkeeping/ui/panels/DashboardPanelFX.java | Referenced only from tests |
| nonprofitbookkeeping.ui.panels.DatePanelFX | src/main/java/nonprofitbookkeeping/ui/panels/DatePanelFX.java | No external references |
| nonprofitbookkeeping.ui.panels.JournalPanelFX | src/main/java/nonprofitbookkeeping/ui/panels/JournalPanelFX.java | Referenced only from tests |
| nonprofitbookkeeping.ui.panels.PageViewerFX | src/main/java/nonprofitbookkeeping/ui/panels/PageViewerFX.java | No external references |
| nonprofitbookkeeping.ui.panels.ReconcilePanelFX | src/main/java/nonprofitbookkeeping/ui/panels/ReconcilePanelFX.java | No external references |
| nonprofitbookkeeping.ui.panels.ReportConfigurationTableModel | src/main/java/nonprofitbookkeeping/ui/panels/ReportConfigurationTableModel.java | No external references |
| nonprofitbookkeeping.ui.panels.ReportsPanelFX | src/main/java/nonprofitbookkeeping/ui/panels/ReportsPanelFX.java | Referenced only from tests |
| nonprofitbookkeeping.ui.panels.skeletons.SkeletonCoaPanel | src/main/java/nonprofitbookkeeping/ui/panels/skeletons/SkeletonCoaPanel.java | Referenced only from tests |

Review these candidates to confirm they can be removed or refactored before deleting them.
