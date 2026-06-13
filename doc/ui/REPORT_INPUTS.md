# Report bundle inputs for upstream generation

This project consumes **three linked assets** for each Jasper report:

1. A **report metadata properties file** (drives bundle registration and UI)
2. A **fieldmap CSV** (maps spreadsheet/JRXML fields → bean properties)
3. Matching **Java bean + generator classes**

This document tells an upstream tool exactly what to emit so the report loads
and renders correctly.

---

## 1) Report metadata properties file

**Location (classpath):**

```
src/main/resources/nonprofitbookkeeping/reports/<BaseName>.properties
```

**Required keys** (validated by `ReportBundles`):

| Key | Purpose | Notes |
| --- | --- | --- |
| `displayName` | User-facing name shown in the report selector UI. | Used as the key in `ReportTemplates`. |
| `generatorClass` | Fully-qualified Java class name of the generator. | Must resolve at runtime. |
| `reportType` | `ReportService.ReportType` enum constant name. | Must match `ReportType.valueOf(...)`. |
| `template` | JRXML filename (not full path). | Resolved relative to the metadata directory. |

**Optional keys:**

| Key | Purpose | Notes |
| --- | --- | --- |
| `beanClass` | Fully-qualified Java class name for the data bean. | Leave blank or omit if no bean is required. |
| `beanName` | Friendly bean name used by packaging tooling. | Only meaningful when `beanClass` is present. |
| `description` | Human-readable documentation for maintainers. | Shown in the bundle metadata. |

**Example** (`INCOME_4.properties`):

```properties
# Generated metadata for Jasper report bundle
displayName=Income 4
generatorClass=nonprofitbookkeeping.reports.jasper.generator.INCOME_4JasperGenerator
reportType=INCOME_4_JASPER
template=INCOME_4.jrxml
beanClass=nonprofitbookkeeping.reports.jasper.beans.INCOME_4Bean
beanName=INCOME_4Bean
description=Income 4. Data bean: INCOME_4Bean.
```

**Report type reminder:**
- `reportType` must be the **enum name**, not the `id()` value. Example:
  - ✅ `INCOME_4_JASPER`
  - ❌ `income_4_jasper`

---

## 2) Fieldmap CSV (report field mappings)

**Location (classpath):**

```
src/main/resources/nonprofitbookkeeping/reports/<BaseName>_fieldmap.csv
```

**Purpose:**
- Drives bean generation and runtime field mapping.
- Loaded by `FieldMapLoader` and used by JDBC-backed generators.

**CSV header + columns** (must be in this order):

```
sheetName,cellRef,fieldName,javaType,excelFormat,dbExpr
```

**Column details:**

| Column | Required | Description |
| --- | --- | --- |
| `sheetName` | Yes | Spreadsheet/JRXML sheet name. Should be consistent across rows. |
| `cellRef` | Yes | Excel-style cell reference (e.g., `A1`, `C14`). |
| `fieldName` | Yes | Bean property name (lowercase, underscores preserved). |
| `javaType` | Yes | Fully-qualified Java type (e.g., `java.lang.String`). |
| `excelFormat` | Yes | Original Excel format string or blank. |
| `dbExpr` | No | Optional DB expression used by SQL builders. |

**Parsing rules:**
- The first line is the header and is required.
- Lines starting with `#` are ignored.
- At least 5 columns are required; the 6th column is optional.
- Quoted CSV fields and escaped quotes (`""`) are supported.

**Example**:

```csv
sheetName,cellRef,fieldName,javaType,excelFormat,dbExpr
INCOME_4,A1,report_title,java.lang.String,,
INCOME_4,B6,amount_internal,java.lang.Double,"$#,##0.00","sum(amount)"
```

**Naming convention:**
- For a report named `INCOME_4`, the fieldmap must be
  `INCOME_4_fieldmap.csv` in the same resource directory as the JRXML.

---

## 3) Generated Java bean

**Location:**

```
src/main/java/nonprofitbookkeeping/reports/jasper/beans/<BaseName>Bean.java
```

**Required characteristics:**
- Public, no-arg constructor (implicit is OK).
- Private fields for each `fieldName` in the fieldmap.
- Public getters/setters following JavaBeans naming:
  - `fieldName` → `getFieldName()` / `setFieldName(...)`
- Property names should already be sanitized:
  - Lowercased
  - Underscores preserved
  - Replace spaces/punctuation with underscores
  - If the name starts with a digit, prefix with `_`

**Why naming matters:**
`DataFiller` finds setters by **capitalizing the first character** of the
field name (e.g., `total_assets` → `setTotal_assets(...)`). If the tool produces
fields that are not valid Java identifiers or do not match the fieldmap, the
runtime mapper will silently skip them.

**Example:**

```java
package nonprofitbookkeeping.reports.jasper.beans;

/** Generated bean for sheet INCOME_4 */
public class INCOME_4Bean
{
    private java.lang.Double amount_internal;

    public java.lang.Double getAmount_internal()
    {
        return amount_internal;
    }

    public void setAmount_internal(java.lang.Double v)
    {
        this.amount_internal = v;
    }
}
```

---

## 4) Generated Jasper generator class

**Location:**

```
src/main/java/nonprofitbookkeeping/reports/jasper/generator/<BaseName>JasperGenerator.java
```

**Required characteristics:**
- Must extend `AbstractReportGenerator` (or another generator base that
  ultimately provides the same methods).
- Must implement:
  - `getReportData()` → list of beans (or allow `setReportData(...)` injection)
  - `getReportParameters()` → report parameters map (can be empty)
  - `getReportPath()` → return the JRXML path
  - `getBaseName()` → return `<BaseName>`
- Should return `bundledReportPath()` in `getReportPath()` so the report path
  stays in sync with the metadata file.

**Example skeleton:**

```java
package nonprofitbookkeeping.reports.jasper.generator;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import nonprofitbookkeeping.reports.jasper.beans.INCOME_4Bean;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Skeleton generator for JRXML template INCOME_4.jrxml */
public class INCOME_4JasperGenerator extends AbstractReportGenerator
{
    @Override
    protected List<INCOME_4Bean> getReportData()
    {
        // TODO supply data beans for the report
        return Collections.emptyList();
    }

    @Override
    protected Map<String, Object> getReportParameters()
    {
        Map<String, Object> params = new HashMap<>();
        // TODO populate report parameters such as title or filters
        return params;
    }

    @Override
    protected String getReportPath()
        throws ActionCancelledException, NoFileCreatedException
    {
        return bundledReportPath();
    }

    @Override
    public String getBaseName()
    {
        return "INCOME_4";
    }
}
```

**Generator ↔ properties alignment:**
- The `generatorClass` property must point at this class.
- `getBaseName()` should match the `template` and fieldmap base name.

---

## Quick validation checklist (upstream tool)

1. **Properties** file is present, with required keys and valid `reportType`.
2. **JRXML** file exists at the path implied by `template` and metadata location.
3. **Fieldmap CSV** exists and is parseable (header + ≥5 columns per row).
4. **Bean** fields and getters/setters match the fieldmap `fieldName` values.
5. **Generator** base name matches JRXML + fieldmap + properties.
6. **Class names** are fully qualified in the properties file.

Keeping these items in sync ensures the report is discoverable in the UI,
loads its template, and receives the expected bean data at runtime.
