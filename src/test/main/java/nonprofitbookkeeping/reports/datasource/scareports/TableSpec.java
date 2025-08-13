/**
 * NonprofitAccounting TableSpec.java TableSpec
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;

/** One table (row list) within a report. */
public class TableSpec<T>
{
	final String rowsParamName; // e.g., "P_EXP12_ADVERTISING_ROWS"
	final Collection<T> rows; // your beans for that section
	final String totalParamNameOrNull; // e.g., "P_TOTAL_12"
										// (optional)
	final Function<T, BigDecimal> amountGetter; // how to sum
												// (optional if
												// totalParamNameOrNull
												// is null)
	
	/**
	 * Constructor TableSpec
	 * @param rowsParamName
	 * @param rows
	 * @param totalParamNameOrNull
	 * @param amountGetter
	 */
	public TableSpec(String rowsParamName,
		Collection<T> rows,
		String totalParamNameOrNull,
		Function<T, BigDecimal> amountGetter)
	{
		this.rowsParamName = Objects.requireNonNull(rowsParamName);
		this.rows = rows == null ? Collections.emptyList() : rows;
		this.totalParamNameOrNull = totalParamNameOrNull;
		this.amountGetter = amountGetter;
		
	}
	
	/** Rows only (no auto total). */
	public static <T> TableSpec<T> rowsOnly(String rowsParamName,
		Collection<T> rows)
	{
		return new TableSpec<>(rowsParamName, rows, null, null);
		
	}
	
	/** Rows + single total (sum of amountGetter). */
	public static <T> TableSpec<T> withTotal(String rowsParamName,
		Collection<T> rows,
		String totalParamName,
		Function<T, BigDecimal> amountGetter)
	{
		return new TableSpec<>(rowsParamName, rows,
			Objects.requireNonNull(totalParamName),
			Objects.requireNonNull(amountGetter));
		
	}
	
}
