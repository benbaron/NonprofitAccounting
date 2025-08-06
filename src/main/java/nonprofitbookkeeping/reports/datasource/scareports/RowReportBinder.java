/**
 * NonprofitAccounting RowReportBinder.java RowReportBinder
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

public class RowReportBinder
{
	
	/** One table (row list) within a report. */
	public static class TableSpec<T>
	{
		private final String rowsParamName; // e.g., "P_EXP12_ADVERTISING_ROWS"
		private final Collection<T> rows; // your beans for that section
		private final String totalParamNameOrNull; // e.g., "P_TOTAL_12" (optional)
		private final Function<T, BigDecimal> amountGetter; // how to sum (optional if
															// totalParamNameOrNull is null)
		
		private TableSpec(String rowsParamName,
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
		public static <T> TableSpec<T> rowsOnly(String rowsParamName, Collection<T> rows)
		{
			return new TableSpec<>(rowsParamName, rows, null, null);
			
		}
		
		/** Rows + single total (sum of amountGetter). */
		public static <T> TableSpec<T> withTotal(	String rowsParamName,
													Collection<T> rows,
													String totalParamName,
													Function<T, BigDecimal> amountGetter)
		{
			return new TableSpec<>(	rowsParamName, rows, Objects.requireNonNull(totalParamName),
									Objects.requireNonNull(amountGetter));
			
		}
		
	}
	
	/** Common fill. The top-level uses a 1-row empty data source since content is parameter-driven. */
	public static JasperPrint fill(	String jrxmlClasspath,
									String orgName,
									String reportTitle,
									List<TableSpec<?>> tables,
									Map<String, Object> extraParams) throws JRException
	{
		Map<String, Object> params = new HashMap<>();
		if (extraParams != null)
			params.putAll(extraParams);
		if (orgName != null)
			params.put("P_ORG_NAME", orgName);
		if (reportTitle != null)
			params.put("P_REPORT_TITLE", reportTitle);
		
		// Bind each row list + optional total
		for (TableSpec<?> t : tables)
		{
			params.put(t.rowsParamName, new JRBeanCollectionDataSource(t.rows, false));
			
			if (t.totalParamNameOrNull != null)
			{
				params.put(t.totalParamNameOrNull,
						sum(t.rows, (Function<Object, BigDecimal>) t.amountGetter));
			}
			
		}
		
		try (InputStream in = RowReportBinder.class.getResourceAsStream(jrxmlClasspath))
		{
			JasperReport report = JasperCompileManager.compileReport(in);
			return JasperFillManager.fillReport(report, params, new JREmptyDataSource(1));
		}
		catch (Exception ex)
		{
			if (ex instanceof JRException)
				throw (JRException) ex;
			throw new JRException("Error loading/compiling " + jrxmlClasspath, ex);
		}
		
	}
	
	private static <T> BigDecimal sum(Collection<?> rows, Function<Object, BigDecimal> amountGetter)
	{
		BigDecimal total = BigDecimal.ZERO;
		
		if (rows != null && amountGetter != null)
		{
			
			for (Object r : rows)
			{
				BigDecimal v = amountGetter.apply(r);
				if (v != null)
					total = total.add(v);
			}
			
		}
		
		return total;
		
	}
	
}
