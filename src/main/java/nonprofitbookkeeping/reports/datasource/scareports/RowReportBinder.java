/**
 * NonprofitAccounting RowReportBinder.java RowReportBinder
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

public class RowReportBinder
{
	
	/**
	 * fill already compiled report
	 * 
	 * @param string
	 * @param orgName
	 * @param reportTitle
	 * @param tables
	 * @param extraParams
	 * 
	 * @return
	 * @throws JRException
	 */
	/* 1. you already compiled the report ----------------------------------- */
	public static JasperPrint fillPrecompiledReport(
		String string,
		String orgName,
		String reportTitle,
		List<TableSpec<?>> tables,
		Map<String, Object> extraParams) throws JRException
	{
		
		// re-use the existing parameter logic ------------------------------
		Map<String, Object> params =
			buildParams(orgName, reportTitle, tables, extraParams);
		
		return JasperFillManager.fillReport(string, params,
			new JREmptyDataSource(1));
		
	}
	
	/**
	 * fill (overload) after compiling jrxml file
	 * 
	 * @param jrxmlFile
	 * @param orgName
	 * @param reportTitle
	 * @param tables
	 * @param extraParams
	 * 
	 * @return Jasper printable document
	 * 
	 * @throws JRException
	 * @throws IOException
	 */
	
	/* 2. you only have a java.io.File -------------------------------------- */
	public static JasperPrint compileReportAndFill(
		File jrxmlFile,
		String orgName,
		String reportTitle,
		List<TableSpec<?>> tables,
		Map<String, Object> extraParams) throws JRException, IOException
	{
		// compile the report then fill it
		try (InputStream in = new FileInputStream(jrxmlFile))
		{
			JasperReport report = JasperCompileManager.compileReport(in);
			return fill(report, orgName, reportTitle, tables, extraParams);
		}
		
	}
	
	
	/**
	 * Overload 2 = fill compiled report.
	 * @param report
	 * @param orgName
	 * @param reportTitle
	 * @param tables
	 * @param extraParams
	 * @return
	 */
	private static JasperPrint fill(JasperReport report, 
		String orgName,
		String reportTitle, 
		List<TableSpec<?>> tables,
		Map<String, Object> extraParams)
	{
		// TODO Auto-generated method stub
		return null;		
	}

	/**
	 * buildParams
	 * 
	 * @param orgName
	 * @param reportTitle
	 * @param tables
	 * @param extraParams
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static Map<String, Object> buildParams(String orgName,
		String reportTitle,
		List<TableSpec<?>> tables,
		Map<String, Object> extraParams)
	{
		Map<String, Object> params = new HashMap<>();
		
		// Caller-supplied extras first so our well-known keys can overwrite if
		// needed
		if (extraParams != null && !extraParams.isEmpty())
		{
			params.putAll(extraParams);
		}
		
		if (orgName != null)
		{
			params.put("P_ORG_NAME", orgName);
		}
		
		if (reportTitle != null)
		{
			params.put("P_REPORT_TITLE", reportTitle);
		}
		
		if (tables == null || tables.isEmpty())
		{
			return params; // nothing else to bind
		}
		
		for (TableSpec<?> t : tables)
		{
			if (t == null)
				continue;
			
			// Defensive defaults
			final String rowsParam = t.rowsParamName;
			final Collection<?> rows =
				(t.rows != null) ? t.rows : Collections.emptyList();
			
			if (rowsParam == null || rowsParam.isBlank())
			{
				// No target parameter name -> skip this table spec
				continue;
			}
			
			// Bind the row list as a JRBeanCollectionDataSource (no field
			// re-lookup on getFieldValue)
			params.put(rowsParam,
				new JRBeanCollectionDataSource(rows,
					/* isUseFieldDescription */ false));
			
			// Optional total parameter
			if (t.totalParamNameOrNull != null &&
				!t.totalParamNameOrNull.isBlank() &&
				t.amountGetter != null)
			{
				Function<Object, BigDecimal> getter =
					(Function<Object, BigDecimal>) t.amountGetter;
				params.put(t.totalParamNameOrNull, sum(rows, getter));
			}
			
			// If you expose more computed values per section, add them here in
			// the same pattern.
		}
		
		return params;
		
	}
	
	/** 
	 * Null-safe sum over a collection using the provided getter.
	 * @param <T> return type
	 * @param rows : to sum
	 * @param amountGetter : method
	 * 
	 * @return the total
	 */
	private static <T> BigDecimal sum(Collection<?> rows,
		Function<Object, BigDecimal> amountGetter)
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
