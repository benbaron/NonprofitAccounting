/**
 * NonprofitAccounting RowReports.java RowReports
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Unified binder for all row-based JRXML reports.
 * - Centralizes JRBeanCollectionDataSource wiring
 * - Computes (optional) totals
 * - Provides one-call builders per report
 */
public final class RowReports implements SupplementalRecord
{
	
	private RowReports()
	{
		
	}
	
	/* ========================================================= JRXML CLASSPATH
	 * CONSTANTS Adjust these to match your resources layout.
	 * ========================================================= */
	public static final String JRXML_EXP_12A =
		"/reports/EXPENSE_DTL_12a_ROW_BASED.jrxml";
	public static final String JRXML_EXP_12B =
		"/reports/EXPENSE_DTL_12b_ROW_BASED.jrxml";
	public static final String JRXML_ASSET_5A =
		"/reports/ASSET_DTL_5a_ROW_BASED.jrxml";
	public static final String JRXML_LIAB_5B =
		"/reports/LIABILITY_DTL_5b_ROW_BASED.jrxml";
	public static final String JRXML_INV_6 =
		"/reports/INVENTORY_DTL_6_ROW_BASED.jrxml";
	public static final String JRXML_REG_7 =
		"/reports/REGALIA_SALES_DTL_7_ROW_BASED.jrxml";
	public static final String JRXML_DEPR_8 =
		"/reports/DEPR_DTL_8_ROW_BASED.jrxml";
	public static final String JRXML_TRIN_9 =
		"/reports/TRANSFER_IN_9_ROW_BASED.jrxml";
	public static final String JRXML_TROUT_10 =
		"/reports/TRANSFER_OUT_10_ROW_BASED.jrxml";
	public static final String JRXML_INC_11A =
		"/reports/INCOME_DTL_11a_ROW_BASED.jrxml";
	public static final String JRXML_INC_11B =
		"/reports/INCOME_DTL_11b_ROW_BASED.jrxml";
	public static final String JRXML_INC_11C =
		"/reports/INCOME_DTL_11c_ROW_BASED.jrxml";
	public static final String JRXML_INC_4 =
		"/reports/INCOME_4_ROW_BASED.jrxml";
	public static final String JRXML_FUNDS_14 =
		"/reports/FUNDS_14_ROW_BASED.jrxml";
	public static final String JRXML_NEWS_15 =
		"/reports/NEWSLETTER_15_ROW_BASED.jrxml";
	
	/* ========================================================= GENERIC BINDING
	 * CORE ========================================================= */
	/** One table (row list) within a report. */
	public static class TableSpec<T>
	{
		private final String rowsParamName; // e.g. "P_EXP12_ADVERTISING_ROWS"
		private final Collection<T> rows; // section beans
		private final String totalParamNameOrNull; // e.g. "P_TOTAL_12"
													// (optional)
		private final Function<T, BigDecimal> amountGetter; // only needed if
															// totalParamName
															// provided
		
		/**
		 * 
		 * Constructor TableSpec
		 * @param rowsParamName
		 * @param rows
		 * @param totalParamNameOrNull
		 * @param amountGetter
		 */
		private TableSpec(String rowsParamName,
			Collection<T> rows,
			String totalParamNameOrNull,
			Function<T, BigDecimal> amountGetter)
		{
			this.rowsParamName =
				Objects.requireNonNull(rowsParamName, "rowsParamName");
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
		
		/** Rows + single total (sum via amountGetter). */
		public static <T> TableSpec<T> withTotal(String rowsParamName,
			Collection<T> rows,
			String totalParamName,
			Function<T, BigDecimal> amountGetter)
		{
			return new TableSpec<>(rowsParamName, rows,
				Objects.requireNonNull(totalParamName, "totalParamName"),
				Objects.requireNonNull(amountGetter, "amountGetter"));
			
		}
		
	}
	
	/**
	 * Core fill: compiles JRXML (from classpath), binds row lists + optional totals,
	 * and fills with a 1-row empty data source (content is parameter-driven).
	 */
	public static JasperPrint fill(String jrxmlClasspath,
		String orgName,
		String reportTitle,
		List<TableSpec<?>> tables,
		Map<String, Object> extraParams) throws JRException
	{
		
		Map<String, Object> params = new HashMap<>();
		
		if (extraParams != null)
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
		
		// Bind each section
		for (TableSpec<?> t : tables)
		{
			params.put(t.rowsParamName,
				new JRBeanCollectionDataSource(t.rows, false));
			
			if (t.totalParamNameOrNull != null)
			{
				params.put(t.totalParamNameOrNull,
					sum(t.rows, (Function<Object, BigDecimal>) t.amountGetter));
			}
			
		}
		
		try (InputStream in =
			RowReports.class.getResourceAsStream(jrxmlClasspath))
		{
			if (in == null)
				throw new JRException(
					"JRXML not found on classpath: " + jrxmlClasspath);
			JasperReport report = JasperCompileManager.compileReport(in);
			return JasperFillManager.fillReport(report, params,
				new JREmptyDataSource(1));
		}
		catch (JRException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new JRException("Error loading/compiling " + jrxmlClasspath,
				e);
		}
		
	}
	
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
	
	/* ========================================================= CONVENIENCE
	 * BUILDERS (BY REPORT) Replace bean type names or getters to match your
	 * code. ========================================================= */
	
	// ---------- EXPENSE 12a ----------
	public static <
		A, B, C> JasperPrint
		buildExpense12a(
			String org,
			String title,
			Collection<A> advertisingRows,
			Function<A, BigDecimal> advAmountGetter,
			Collection<B> badDebtRows,
			Function<B, BigDecimal> badAmountGetter,
			Collection<C> feeRows,
			Function<C, BigDecimal> feeAmountGetter) throws JRException
	{
		return fill(JRXML_EXP_12A, org, title,
			Arrays.asList(
				TableSpec.withTotal("P_EXP12_ADVERTISING_ROWS", advertisingRows,
					"P_TOTAL_12", advAmountGetter),
				TableSpec.withTotal("P_EXP13_BADDEBTS_ROWS", badDebtRows,
					"P_TOTAL_13",
					badAmountGetter),
				TableSpec.withTotal("P_EXP17_FEES_ROWS", feeRows, "P_TOTAL_17",
					feeAmountGetter)),
			null);
		
	}
	
	// ---------- EXPENSE 12b ----------
	public static <
		I, O, D> JasperPrint
		buildExpense12b(
			String org,
			String title,
			Collection<I> insuranceRows,
			Function<I, BigDecimal> insAmountGetter,
			Collection<O> otherRows,
			Function<O, BigDecimal> otherAmountGetter,
			Collection<D> donationRows,
			Function<D, BigDecimal> donationAmountGetter) throws JRException
	{
		return fill(JRXML_EXP_12B, org, title,
			Arrays.asList(
				TableSpec.withTotal("P_EXP20_INSURANCE_ROWS", insuranceRows,
					"P_TOTAL_20",
					insAmountGetter),
				TableSpec.withTotal("P_EXP28_OTHER_ROWS", otherRows,
					"P_TOTAL_28",
					otherAmountGetter),
				TableSpec.withTotal("P_EXP29_DONATION_ROWS", donationRows,
					"P_TOTAL_29",
					donationAmountGetter)),
			null);
		
	}
	
	// ---------- ASSET 5a ----------
	public static <T> JasperPrint buildAsset5a(
		String org,
		String title,
		Collection<T> assetRows) throws JRException
	{
		return fill(JRXML_ASSET_5A, org, title,
			Collections.singletonList(
				TableSpec.rowsOnly("P_ASSET_ROWS", assetRows)),
			null);
		
	}
	
	// ---------- LIABILITY 5b (three sections,
	// prior/current handled in JRXML or via extra params)
	// ----------
	public static <
		D, P, L> JasperPrint
		buildLiability5b(
			String org,
			String title,
			Collection<D> deferredRows,
			// each D bean has eventName, priorAmount, currentAmount
			// (consumed by JRXML)
			Collection<P> payableRows,
			// each P bean has owedTo, reason, priorAmount, currentAmount
			Collection<L> otherRows,
			// each L bean has owedTo, reason, priorAmount, currentAmount
			Map<String, Object> extraTotals
	// optional: put any section totals your JRXML expects
		) throws JRException
	{
		return fill(JRXML_LIAB_5B, org, title,
			Arrays.asList(
				TableSpec.rowsOnly("P_DEFERRED_REVENUE_ROWS", deferredRows),
				TableSpec.rowsOnly("P_PAYABLE_ROWS", payableRows),
				TableSpec.rowsOnly("P_OTHER_LIABILITY_ROWS", otherRows)),
			extraTotals);
		
	}
	
	// ---------- INVENTORY 6 ----------
	public static <T> JasperPrint buildInventory6(
		String org,
		String title,
		Collection<T> inventoryRows,
		Function<T, BigDecimal> amountGetter) throws JRException
	{
		return fill(JRXML_INV_6, org, title,
			Collections.singletonList(
				TableSpec.withTotal("P_INVENTORY_ROWS", inventoryRows,
					"P_INVENTORY_TOTAL",
					amountGetter)),
			null);
		
	}
	
	// ---------- REGALIA 7 (3-section layout) ----------
	public static <P, S, K> JasperPrint buildRegalia7(
		String org,
		String title,
		Collection<P> productionRows,
		Function<P, BigDecimal> productionAmount,
		Collection<S> salesRows,
		Function<S, BigDecimal> salesAmount,
		Collection<K> stockRows,
		Function<K, BigDecimal> stockAmount) throws JRException
	{
		return fill(JRXML_REG_7, org, title,
			Arrays.asList(
				TableSpec.withTotal("P_REGALIA_PRODUCTION_ROWS", productionRows,
					"P_TOTAL_PRODUCTION", productionAmount),
				TableSpec.withTotal("P_REGALIA_SALES_ROWS", salesRows,
					"P_TOTAL_SALES",
					salesAmount),
				TableSpec.withTotal("P_REGALIA_STOCK_ROWS", stockRows,
					"P_TOTAL_STOCK",
					stockAmount)),
			null);
		
	}
	
	// ---------- DEPRECIATION 8 (5-year & 7-year) ----------
	public static <
		T> JasperPrint
		buildDepreciation8(
			String org,
			String title,
			Collection<T> depr5yRows,
			Function<T, BigDecimal> depr5yAmount,
			Collection<T> depr7yRows,
			Function<T, BigDecimal> depr7yAmount) throws JRException
	{
		return fill(JRXML_DEPR_8, org, title,
			Arrays.asList(
				TableSpec.withTotal("P_DEPR_5Y_ROWS", depr5yRows,
					"P_DEPR_5Y_TOTAL",
					depr5yAmount),
				TableSpec.withTotal("P_DEPR_7Y_ROWS", depr7yRows,
					"P_DEPR_7Y_TOTAL",
					depr7yAmount)),
			null);
		
	}
	
	// ---------- TRANSFER IN 9 ----------
	public static <
		T> JasperPrint
		buildTransferIn9(
			String org,
			String title,
			Collection<T> rows,
			Function<T, BigDecimal> amountGetter) throws JRException
	{
		return fill(JRXML_TRIN_9, org, title,
			Collections.singletonList(
				TableSpec.withTotal("P_TRANSFER_IN_ROWS", rows,
					"P_TOTAL_TRANSFER_IN",
					amountGetter)),
			null);
		
	}
	
	// ---------- TRANSFER OUT 10 ----------
	public static <T> JasperPrint buildTransferOut10(
		String org,
		String title,
		Collection<T> rows,
		Function<T,
			BigDecimal> amountGetter)
		throws JRException
	{
		return fill(JRXML_TROUT_10, org, title,
			Collections.singletonList(
				TableSpec.withTotal("P_TRANSFER_OUT_ROWS", rows,
					"P_TOTAL_TRANSFER_OUT",
					amountGetter)),
			null);
		
	}
	
	/**
	 * 
	 * @param <T>
	 * @param org
	 * @param title
	 * @param rows
	 * @param amountGetter
	 * @return
	 * @throws JRException
	 */
	// ---------- INCOME DETAIL 11a / 11b / 11c ----------
	public static <T> JasperPrint buildIncome11a(
		String org,
		String title,
		Collection<T> rows,
		Function<T, BigDecimal> amountGetter) throws JRException
	{
		return fill(JRXML_INC_11A, org, title,
			Collections.singletonList(
				TableSpec.withTotal("P_INCOME_11A_ROWS", rows, "P_TOTAL_11A",
					amountGetter)),
			null);
		
	}
	
	/**
	 * 
	 * @param <T>
	 * @param org
	 * @param title
	 * @param rows
	 * @param amountGetter
	 * @return
	 * @throws JRException
	 */
	public static <T> JasperPrint buildIncome11b(
		String org,
		String title,
		Collection<T> rows,
		Function<T, BigDecimal> amountGetter) throws JRException
	{
		return fill(JRXML_INC_11B, org, title,
			Collections.singletonList(
				TableSpec.withTotal("P_INCOME_11B_ROWS", rows, "P_TOTAL_11B",
					amountGetter)),
			null);
		
	}
	
	/**
	 * 
	 * @param <T>
	 * @param org
	 * @param title
	 * @param rows
	 * @param amountGetter
	 * @return
	 * @throws JRException
	 */
	public static <T> JasperPrint buildIncome11c(
		String org,
		String title,
		Collection<T> rows,
		Function<T, BigDecimal> amountGetter) throws JRException
	{
		return fill(JRXML_INC_11C,
			org,
			title,
			Collections.singletonList(
				TableSpec.withTotal("P_INCOME_11C_ROWS", rows, "P_TOTAL_11C",
					amountGetter)),
			null);
		
	}
	
	// ---------- INCOME 4 ----------
	public static <T> JasperPrint buildIncome4(
		String org,
		String title,
		Collection<T> rows,
		Function<T,
			BigDecimal> amountGetter)
		throws JRException
	{
		return fill(JRXML_INC_4, org, title,
			Collections.singletonList(
				TableSpec.withTotal("P_INCOME_ROWS", rows, "P_INCOME_TOTAL",
					amountGetter)),
			null);
		
	}
	
	// ---------- FUNDS 14 ----------
	public static <T> JasperPrint buildFunds14(
		String org,
		String title,
		Collection<T> rows,
		Function<T,
			BigDecimal> balanceGetter)
		throws JRException
	{
		return fill(JRXML_FUNDS_14, org, title,
			Collections.singletonList(
				TableSpec.withTotal("P_FUNDS_ROWS", rows, "P_FUNDS_TOTAL",
					balanceGetter)),
			null);
		
	}
	
	// ---------- NEWSLETTER 15 ----------
	public static <T> JasperPrint buildNewsletter15(
		String org,
		String title,
		Collection<T> rows,
		Function<T,
			BigDecimal> amountGetter)
		throws JRException
	{
		return fill(JRXML_NEWS_15, org, title,
			Collections.singletonList(
				TableSpec.withTotal("P_NEWSLETTER_ROWS", rows,
					"P_NEWSLETTER_TOTAL",
					amountGetter)),
			null);
		
	}
	
	/* ========================================================= EXPORT HELPERS
	 * ========================================================= */
	public static void exportPdf(JasperPrint jp, String outPath)
		throws JRException
	{
		JasperExportManager.exportReportToPdfFile(jp, outPath);
		
	}
	
	public static void exportHtml(JasperPrint jp, String outPath)
		throws JRException
	{
		JasperExportManager.exportReportToHtmlFile(jp, outPath);
		
	}
	
}
