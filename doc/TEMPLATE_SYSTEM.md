# XLSX Template System Manual

## Purpose
The XLSX template system fills a pre-existing Excel workbook (a form or report
layout) using field map metadata and report beans. It is designed for cases
where a fixed Excel form needs to be populated at known cell addresses rather
than generated through JasperReports.

## Key Inputs

### Excel template file
Provide a `.xlsx` file that already contains the report layout. The template is
loaded and updated in place, then written to a new output file.

### Field map CSV
Each report template has a corresponding `*_fieldmap.csv` file that maps report
fields to specific sheet names and cell references.

Field map columns (in order):
1. `sheetName` — Excel sheet name in the template.
2. `cellRef` — Excel A1-style cell reference (for example, `B5`).
3. `fieldName` — Bean getter field name used to read a value.
4. `javaType` — Fully-qualified Java type (used by Jasper tooling and metadata).
5. `excelFormat` — Optional Excel data format string.
6. `dbExpr` — Optional SQL expression for Jasper field overrides.

### Report context
When generating a report via the non-Jasper XLSX path, the `ReportContext` can
carry the template file reference (`excelTemplateFile`) and the optional
`nullPlaceholder` configuration used during export.

## How the Writer Works

1. **Open the template**: The `ExcelTemplateWriter` loads the workbook from the
   provided template file using Apache POI.
2. **Resolve field map entries**: Each `FieldMapEntry` provides a sheet name,
   cell reference, and field name.
3. **Read bean values**: The writer uses reflection to call the getter that
   corresponds to the field name (for example, `getAmount()` for `amount`).
4. **Write values to cells**:
   - Numbers are written as numeric cells.
   - Booleans are written as boolean cells.
   - `LocalDate` values are converted to SQL dates and written as numeric cells.
   - Everything else is written as a string.
5. **Apply formatting**: If a field map entry includes an `excelFormat`, the
   writer creates or reuses a cached `CellStyle` with that data format.
6. **Handle nulls**: When a value is null, the writer applies the
   `nullPlaceholder` string from `ReportContext` unless the field name is listed
   in `nullPlaceholderSkipFields`, in which case the cell is left blank.
7. **Save output**: The updated workbook is written to the requested output
   file.

## Typical Flow

1. Choose a template file (for example, `reports/MyForm.xlsx`).
2. Ensure the template has a `*_fieldmap.csv` entry for each cell to fill.
3. Build or reuse a report bean with getters matching the field map.
4. Call `ExcelTemplateWriter.writeTemplate(...)` with:
   - the template file,
   - the field map,
   - the report bean (or bean list reduced to one header bean), and
   - the output file destination.

## Example file set

The following example shows a minimal field map and the matching template cells
you would populate.

### Example field map (`EXAMPLE_REPORT_fieldmap.csv`)

```
Summary, B2, organization_name, java.lang.String, ,
Summary, B3, reporting_period, java.lang.String, ,
Summary, B5, total_revenue, java.math.BigDecimal, "$#,##0.00",
Summary, B6, total_expense, java.math.BigDecimal, "$#,##0.00",
Summary, B8, report_date, java.time.LocalDate, "yyyy-mm-dd",
```

Notes:
- The sixth column (`dbExpr`) is left blank for template-only usage.

### Example template layout (`EXAMPLE_REPORT.xlsx`)

- Sheet name: `Summary`
- Cells:
  - `B2`: Organization name
  - `B3`: Reporting period label
  - `B5`: Total revenue
  - `B6`: Total expense
  - `B8`: Report date

### Example report bean (getter names)

- `getOrganization_name()`
- `getReporting_period()`
- `getTotal_revenue()`
- `getTotal_expense()`
- `getReport_date()`

When the writer runs, it looks up each getter, writes the value into the mapped
cell, and applies the `excelFormat` column when provided.

## Troubleshooting

- **Missing template file**: The writer throws an `IOException` if the template
  is not found.
- **Missing field map**: The writer throws an `IOException` if no field map is
  provided.
- **Unknown field names**: If a getter is missing, the writer leaves the target
  cell blank (or uses the null placeholder, depending on configuration).
