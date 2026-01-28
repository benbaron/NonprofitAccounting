
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
public final class FieldMapEntry
{
	
	/** The sheet name. */
	private final String sheetName;
	
	/** The cell ref. */
	private final String cellRef;
	
	/** The field name. */
	private final String fieldName;
	
	/** The java type. */
	private final String javaType;
	
	/** The excel format. */
	private final String excelFormat;
	
	/** The db expr. */
	private final String dbExpr;
	
	/**
	 * Instantiates a new field map entry.
	 *
	 * @param sheetName the sheet name
	 * @param cellRef the cell ref
	 * @param fieldName the field name
	 * @param javaType the java type
	 * @param excelFormat the excel format
	 * @param dbExpr the db expr
	 */
	public FieldMapEntry(
		String sheetName,
		String cellRef,
		String fieldName,
		String javaType,
		String excelFormat,
		String dbExpr
	)
	{
		this.sheetName = sheetName;
		this.cellRef = cellRef;
		this.fieldName = fieldName;
		this.javaType = javaType;
		this.excelFormat = excelFormat;
		this.dbExpr = dbExpr;
		
	}
	
	/**
	 * Gets the field name.
	 *
	 * @return the field name
	 */
	public String getFieldName()
	{
		return this.fieldName;
		
	}

	/**
	 * Gets the sheet name.
	 *
	 * @return the sheet name
	 */
	public String getSheetName()
	{
		return this.sheetName;
	}

	/**
	 * Gets the cell reference.
	 *
	 * @return the cell reference
	 */
	public String getCellRef()
	{
		return this.cellRef;
	}

	/**
	 * Gets the java type.
	 *
	 * @return the java type
	 */
	public String getJavaType()
	{
		return this.javaType;
	}

	/**
	 * Gets the excel format string.
	 *
	 * @return the excel format
	 */
	public String getExcelFormat()
	{
		return this.excelFormat;
	}
	
	/**
	 * Optional SQL expression for this field, e.g. "a.name" or "s.opening_balance".
	 * May be null if the CSV has only 5 columns.
	 *
	 * @return the db expr
	 */
	public String getDbExpr()
	{
		return this.dbExpr;
		
	}
	
}
