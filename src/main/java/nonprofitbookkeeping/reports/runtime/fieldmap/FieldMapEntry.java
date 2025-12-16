package nonprofitbookkeeping.reports.runtime.fieldmap;

/**
 * One row from a fieldmap CSV.
 */
public final class FieldMapEntry
{
    private final String sheetName;
    private final String cellRef;
    private final String fieldName;
    private final String javaType;
    private final String excelFormat;
    private final String dbExpr;

    public FieldMapEntry(String sheetName,
                         String cellRef,
                         String fieldName,
                         String javaType,
                         String excelFormat,
                         String dbExpr)
    {
        this.sheetName = sheetName;
        this.cellRef = cellRef;
        this.fieldName = fieldName;
        this.javaType = javaType;
        this.excelFormat = excelFormat;
        this.dbExpr = dbExpr;
    }

    public String getSheetName()
    {
        return sheetName;
    }

    public String getCellRef()
    {
        return cellRef;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public String getJavaType()
    {
        return javaType;
    }

    public String getExcelFormat()
    {
        return excelFormat;
    }

    public String getDbExpr()
    {
        return dbExpr;
    }
}