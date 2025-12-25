package nonprofitbookkeeping.reports.jasper.runtime;

/**
 * One row from a *_fieldmap.csv file.
 *
 * Columns (in order):
 *   0: sheetName      - Excel sheet name
 *   1: cellRef        - Excel A1-style reference (e.g. "B5")
 *   2: fieldName      - JRXML / bean field name (e.g. "cashamount")
 *   3: javaType       - fully-qualified Java type (e.g. "java.math.BigDecimal")
 *   4: excelFormat    - raw Excel data format string (may be empty)
 *   5: dbExpr         - OPTIONAL; SQL expression to compute this field (e.g. "s.opening_balance")
 */
public final class FieldMapEntry {

    private final String sheetName;
    private final String cellRef;
    private final String fieldName;
    private final String javaType;
    private final String excelFormat;
    private final String dbExpr;

    public FieldMapEntry(
        String sheetName,
        String cellRef,
        String fieldName,
        String javaType,
        String excelFormat,
        String dbExpr
    ) {
        this.sheetName = sheetName;
        this.cellRef = cellRef;
        this.fieldName = fieldName;
        this.javaType = javaType;
        this.excelFormat = excelFormat;
        this.dbExpr = dbExpr;
    }

    public String getSheetName() {
        return this.sheetName;
    }

    public String getCellRef() {
        return this.cellRef;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public String getJavaType() {
        return this.javaType;
    }

    public String getExcelFormat() {
        return this.excelFormat;
    }

    /**
     * Optional SQL expression for this field, e.g. "a.name" or "s.opening_balance".
     * May be null if the CSV has only 5 columns.
     */
    public String getDbExpr() {
        return this.dbExpr;
    }
}
