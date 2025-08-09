/**
 * NonprofitAccounting RowFiller.java RowFiller
 */

package nonprofitbookkeeping.reports.generator;


import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import nonprofitbookkeeping.reports.datasource.EventIncomeRow;
import nonprofitbookkeeping.reports.datasource.FundRow;
import nonprofitbookkeeping.reports.datasource.IncomeRowBase;
import nonprofitbookkeeping.reports.datasource.InventoryRow;
import nonprofitbookkeeping.reports.datasource.MerchIncomeRow;
import nonprofitbookkeeping.reports.datasource.NewsletterRow;
import nonprofitbookkeeping.reports.datasource.OtherIncomeRow;
import nonprofitbookkeeping.reports.datasource.TransferRow;
import nonprofitbookkeeping.reports.datasource.scareports.AdvertisingExpenseRow;
import nonprofitbookkeeping.reports.datasource.scareports.BadDebtRow;
import nonprofitbookkeeping.reports.datasource.scareports.DepreciationRow;
import nonprofitbookkeeping.reports.datasource.scareports.DonationRow;
import nonprofitbookkeeping.reports.datasource.scareports.FeeHonorariumRow;
import nonprofitbookkeeping.reports.datasource.scareports.InsuranceExpenseRow;
import nonprofitbookkeeping.reports.datasource.scareports.OtherExpenseRow;

import nonprofitbookkeeping.reports.datasource.scareports.RowReportBinder;
import nonprofitbookkeeping.reports.datasource.scareports.TableSpec;
import nonprofitbookkeeping.reports.generator.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/** Convenience wrappers – one static method per row-based Jasper page. */
public final class RowPages
{
	
	private RowPages()
	{		
		/* utility class */
	}
		
	/*------------------------------------------------------------------*\
	 |  EXPENSE DETAIL – Part 12-A  (Advertising, Bad Debts, Fees/Honoraria)
	\*------------------------------------------------------------------*/
	public static JasperPrint fillExpense12a(
		String orgName,
		String reportTitle,
		Collection<AdvertisingExpenseRow> advertisingRows,
		Collection<BadDebtRow> badDebtRows,
		Collection<FeeHonorariumRow> feeRows,
		Map<String, Object> extras) throws JRException
	{
		
		return fillPrecompiledReport("/reports/EXPENSE_DTL_12a_row.jrxml",
			orgName,
			reportTitle,
			List.of(
				spec("P_EXP12_ADVERTISING_ROWS", advertisingRows, "P_TOTAL_12",
					AdvertisingExpenseRow::getAmount),
				spec("P_EXP13_BADDEBTS_ROWS", badDebtRows, "P_TOTAL_13",
					BadDebtRow::getAmount),
				spec("P_EXP17_FEES_ROWS", feeRows, "P_TOTAL_17",
					FeeHonorariumRow::getAmount)
			),
			extras);
		
	}
	




	/**
	 * @param string
	 * @param orgName
	 * @param reportTitle
	 * @param of
	 * @param extras
	 * @return
	 */
	private static JasperPrint fillPrecompiledReport(String string,
		String orgName, String reportTitle,
		List<TableSpec<? extends Object>> of, Map<String, Object> extras)
	{
		// TODO Auto-generated method stub
		return null;
		
	}

	/*------------------------------------------------------------------*\
	 |  EXPENSE DETAIL – Part 12-B  (Insurance, Other, Donations)
	\*------------------------------------------------------------------*/
	public static JasperPrint fillExpense12b(
		String orgName,
		String reportTitle,
		Collection<InsuranceExpenseRow> insuranceRows,
		Collection<OtherExpenseRow> otherRows,
		Collection<DonationRow> donationRows,
		Map<String, Object> extras) throws JRException
	{
		
		return fillPrecompiledReport("/reports/EXPENSE_DTL_12b_row.jrxml",
			orgName,
			reportTitle,
			List.of(
				spec("P_EXP20_INSURANCE_ROWS", 
					insuranceRows, 
					"P_TOTAL_20",
					InsuranceExpenseRow::getAmount),
				spec("P_EXP28_OTHER_ROWS", 
					otherRows, "P_TOTAL_28",
					OtherExpenseRow::getAmount),
				spec("P_EXP29_DONATION_ROWS", 
					donationRows, "P_TOTAL_29",
					DonationRow::getAmount)
			),
			extras);
		
	}
	

	/*------------------------------------------------------------------*\
	 |  FUNDS  (page 14)
	\*------------------------------------------------------------------*/
	public static JasperPrint fillFunds14(
		String orgName,
		String reportTitle,
		Collection<FundRow> fundRows,
		Map<String, Object> extras) throws JRException
	{
		
		return fillPrecompiledReport("/reports/FUNDS_14_row.jrxml",
			orgName,
			reportTitle,
			List.of(
				spec("P_FUND_ROWS", 
					fundRows, 
					"P_FUND_TOTAL",
					FundRow::getBalance)
			),
			extras);
		
	}
	
	/*------------------------------------------------------------------*\
	 |  INCOME DETAIL – 11-A / 11-B / 11-C
	\*------------------------------------------------------------------*/
	public static JasperPrint fillIncome11a(String orgName, String reportTitle,
		Collection<EventIncomeRow> rows,
		Map<String, Object> extras) throws JRException
	{
		return fillIncomeGeneric("/reports/INCOME_DTL_11a_row.jrxml",
			"P_INC11A_ROWS", "P_TOTAL_11A",
			orgName, reportTitle, rows, extras);
		
	}
	
	public static JasperPrint fillIncome11b(String orgName, String reportTitle,
		Collection<MerchIncomeRow> rows,
		Map<String, Object> extras) throws JRException
	{
		return fillIncomeGeneric("/reports/INCOME_DTL_11b_row.jrxml",
			"P_INC11B_ROWS", "P_TOTAL_11B",
			orgName, reportTitle, rows, extras);
		
	}
	
	public static JasperPrint fillIncome11c(String orgName, String reportTitle,
		Collection<OtherIncomeRow> rows,
		Map<String, Object> extras) throws JRException
	{
		return fillIncomeGeneric("/reports/INCOME_DTL_11c_row.jrxml",
			"P_INC11C_ROWS", "P_TOTAL_11C",
			orgName, reportTitle, rows, extras);
		
	}
	
	/*------------------------------------------------------------------*\
	 |  NEWSLETTER 15
	\*------------------------------------------------------------------*/
	public static JasperPrint fillNewsletter15(
		String orgName,
		String reportTitle,
		Collection<NewsletterRow> rows,
		Map<String, Object> extras) throws JRException
	{
		
		return fillPrecompiledReport("/reports/NEWSLETTER_15_row.jrxml",
			orgName,
			reportTitle,
			List.of(
				spec("P_NEWSLETTER_ROWS", rows, "P_TOTAL_NEWS",
					NewsletterRow::getAmount)
			),
			extras);
		
	}
	
	/*------------------------------------------------------------------*\
	 |  TRANSFER IN 9   /   TRANSFER OUT 10
	\*------------------------------------------------------------------*/
	public static JasperPrint fillTransferIn9(
		String orgName,
		String reportTitle,
		Collection<TransferRow> rows,
		Map<String, Object> extras) throws JRException
	{
		
		return fillTransferGeneric("/reports/TRANSFER_IN_9_row.jrxml",
			"P_TRANSFER_IN_ROWS", "P_TOTAL_TRANSFER_IN",
			orgName, reportTitle, rows, extras);
		
	}
	
	public static JasperPrint fillTransferOut10(
		String orgName,
		String reportTitle,
		Collection<TransferRow> rows,
		Map<String, Object> extras) throws JRException
	{
		
		return fillTransferGeneric("/reports/TRANSFER_OUT_10_row.jrxml",
			"P_TRANSFER_OUT_ROWS", "P_TOTAL_TRANSFER_OUT",
			orgName, reportTitle, rows, extras);
		
	}
	
	/*------------------------------------------------------------------*\
	 |  INVENTORY 6,  ASSET 5-A,  LIABILITY 5-B,  REGALIA 7,  DEPRECIATION 8
	 |  (pattern is identical – supply more wrappers if desired)
	\*------------------------------------------------------------------*/
	public static JasperPrint fillInventory6(
		String orgName,
		String reportTitle,
		Collection<InventoryRow> rows,
		Map<String, Object> extras) throws JRException
	{
		
		return fillPrecompiledReport("/reports/INVENTORY_DTL_6_row.jrxml",
			orgName,
			reportTitle,
			List.of(spec("P_INVENTORY_ROWS", rows, "P_TOTAL_INV",
				InventoryRow::getValue)),
			extras);
		
	}
	
	public static JasperPrint fillDepreciation8(
		String orgName,
		String reportTitle,
		Collection<DepreciationRow> fiveYearRows,
		Collection<DepreciationRow> sevenYearRows,
		Map<String, Object> extras) throws JRException
	{
		
		return fillPrecompiledReport("/reports/DEPR_DTL_8_row.jrxml",
			orgName,
			reportTitle,
			List.of(
				spec("P_DEPR_5Y_ROWS", fiveYearRows, "P_TOTAL_DEPR_5Y",
					DepreciationRow::getRemainingValue),
				spec("P_DEPR_7Y_ROWS", sevenYearRows, "P_TOTAL_DEPR_7Y",
					DepreciationRow::getRemainingValue)
			),
			extras);
		
	}
	
	/* ===== helper factory ================================================= */
	
	private static <T> TableSpec<T> spec(String rowsParam,
		Collection<T> rows,
		String totalParam,
		Function<T, BigDecimal> amountGetter)
	{
		return new TableSpec<T>(rowsParam, rows, totalParam, amountGetter);
		
	}
	
	private static <T> JasperPrint fillIncomeGeneric(String jrxml,
		String rowsParam,
		String totalParam,
		String org,
		String title,
		Collection<T> rows,
		Map<String, Object> extras) throws JRException
	{
		return fillPrecompiledReport(
			jrxml,
			org, 
			title,
			List.of(
				spec(rowsParam, rows, totalParam, null)),
			extras);
		
	}
	



	private static <T> JasperPrint fillTransferGeneric(String jrxml,
		String rowsParam,
		String totalParam,
		String org,
		String title,
		Collection<T> rows,
		Map<String, Object> extras) throws JRException
	{
		return fillPrecompiledReport(jrxml,
			org, title,
			List.of(spec(rowsParam, rows, totalParam, null)),
			extras);
		
	}
	
}
