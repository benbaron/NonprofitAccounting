# Semantic JSON report system

This document describes the compact workbook-modeled report format migrated from the `sca-jakarta-h2` / `npbk-javafx-h2` work into `NonprofitAccounting`.

## Purpose

The semantic JSON report system is for reports that should resemble SCA workbook pages without making the application load an Excel file at runtime.

The workbook is a design/reference artifact. The runtime application uses committed JSON resources and Java renderers.

## Runtime resource location

Templates live under:

```text
src/main/resources/nonprofitbookkeeping/report/templates/
```

Each template is named:

```text
<TemplateId>.report.json
```

Examples:

```text
BalanceStmt.report.json
IncomeStmt.report.json
WorkbookSummary.report.json
TransactionsList.report.json
AllChecksTfrs.report.json
FundTransfers.report.json
```

## Template kinds

There are two first-pass template kinds.

### sectionReport

A `sectionReport` is a statement-style form with sections and rows. Rows can be static text, dynamic values, or spacers.

Important fields:

```text
templateId
type
title
subtitle
sourceSheet
sections[].title
sections[].rows[].line
sections[].rows[].label
sections[].rows[].valueKey
sections[].rows[].format
sections[].rows[].sourceCell
sections[].rows[].note
```

### tableReport

A `tableReport` is a workbook-modeled list view. It has a `tableKey` and column definitions. Runtime data is supplied as table rows in `SemanticReportValueSet`.

Important fields:

```text
templateId
type
title
subtitle
sourceSheet
tableKey
columns[].label
columns[].field
columns[].format
columns[].width
```

## Value binding

Templates do not contain executable Excel formulas. They contain value keys.

Example:

```json
{
  "type": "valueRow",
  "label": "Total Assets",
  "valueKey": "balanceStmt.totalAssets",
  "format": "currency",
  "sourceCell": "BalanceStmt!H24"
}
```

`sourceCell` is traceability only. Calculation belongs in Java services or database queries.

The first migrated version uses `WorkbookSemanticReportService` with placeholder zeros and empty tables so the forms render immediately. Follow-up work should map the value keys to the mature `NonprofitAccounting` domain services and database tables.

## Renderers

The system has two renderers:

```text
SemanticReportRenderer      -> text and CSV
SemanticReportFxRenderer    -> JavaFX preview form/table
```

`ReportsPanelFX` exposes the semantic reports through a `Workbook Reports` button. This opens `SemanticReportPreviewPanelFX`, which lets the user choose one of the workbook-modeled reports and preview it visually.

## Design rules

1. Do not load `.xlsx` files at runtime.
2. Do not store every Excel cell in JSON.
3. Keep templates semantic and reviewable.
4. Preserve workbook sheet names and source-cell references for traceability.
5. Use Java/database services for calculations.
6. Use JSON only for layout, labels, source traceability, format hints, and value keys.

## Follow-up work

Recommended next steps:

1. Bind `balanceStmt.*` keys to real account/report-section totals.
2. Bind `incomeStmt.*` keys to income/expense activity.
3. Populate `TransactionsList`, `AllChecksTfrs`, and `FundTransfers` from ledger/bank/fund-transfer services.
4. Add export-to-file for the text/CSV renderer.
5. Refine JavaFX styling to more closely match the workbook forms.
