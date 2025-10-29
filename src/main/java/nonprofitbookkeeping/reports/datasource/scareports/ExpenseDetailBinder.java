/**
 * NonprofitAccounting ExpenseDetailBinder.java ExpenseDetailBinder
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

public class ExpenseDetailBinder
{
	
	// ---------- Helpers ----------
	private static <T> BigDecimal sum(Collection<T> rows,
		Function<T, BigDecimal> getter)
	{
		BigDecimal total = BigDecimal.ZERO;
		
		if (rows != null)
		{
			
			for (T r : rows)
			{
				BigDecimal v = getter.apply(r);
				if (v != null)
					total = total.add(v);
			}
			
		}
		
		return total;
		
	}
	
	private static JRBeanCollectionDataSource ds(Collection<?> rows)
	{
		// second arg 'false' so an empty collection renders zero rows (no
		// "fake" row)
		return new JRBeanCollectionDataSource(
			rows == null ? Collections.emptyList() : rows, false);
		
	}
	
	// ----------
	// Example data builders (replace with your real data wiring)
	// ----------
	private static List<AdvertisingExpenseRow> buildAdvertisingRows()
	{
		List<AdvertisingExpenseRow> list = new ArrayList<>();
		list.add(new AdvertisingExpenseRow("SCA Times", "2025-01-10",
			new BigDecimal("125.00")));
		list.add(new AdvertisingExpenseRow("Local Herald", "2025-01-22",
			new BigDecimal("60.00")));
		return list;
		
	}
	
	/**
	 * 
	 * @return
	 */
	private static List<BadDebtRow> buildBadDebtRows()
	{
		return Arrays.asList(
			new BadDebtRow("John Smith", "Uncollectible site fee",
				new BigDecimal("25.00")));
		
	}
	
	/**
	 * 
	 * @return
	 */
	private static List<FeeHonorariumRow> buildFeeRows()
	{
		return Arrays.asList(
			new FeeHonorariumRow("Jane Doe", "Musician for court",
				new BigDecimal("150.00")));
		
	}
	
	/**
	 * 
	 * @return
	 */
	private static List<InsuranceExpenseRow> buildInsuranceRows()
	{
		return Arrays.asList(
			new InsuranceExpenseRow("COI – Park Rental", "2025-02-01",
				new BigDecimal("225.00")));
		
	}
	
	/**
	 * 
	 * @return
	 */
	private static List<OtherExpenseRow> buildOtherRows()
	{
		return Arrays.asList(
			new OtherExpenseRow("Storage Unit", "February rent",
				new BigDecimal("89.99")));
		
	}
	
	/**
	 * 
	 * @return
	 */
	private static List<DonationRow> buildDonationRows()
	{
		return Arrays.asList(
			new DonationRow("501(c)(3) Partner", "Event proceeds donation",
				new BigDecimal("200.00")));
		
	}
	
	// ---------- Fill & export: 12a ----------
	public static JasperPrint fillExpense12a(String jrxmlOnClasspath,
		String orgName,
		String reportTitle,
		Collection<AdvertisingExpenseRow> advertisingRows,
		Collection<BadDebtRow> badDebtRows,
		Collection<
			FeeHonorariumRow> feeRows)
		throws Exception
	{
		
		Map<String, Object> params = new HashMap<>();
		params.put("P_ORG_NAME", orgName);
		params.put("P_REPORT_TITLE", reportTitle);
		
		// Row collections
		params.put("P_EXP12_ADVERTISING_ROWS", ds(advertisingRows));
		params.put("P_EXP13_BADDEBTS_ROWS", ds(badDebtRows));
		params.put("P_EXP17_FEES_ROWS", ds(feeRows));
		
		// Totals (assuming each bean has getAmount())
		params.put("P_TOTAL_12",
			sum(advertisingRows, AdvertisingExpenseRow::getAmount));
		params.put("P_TOTAL_13", sum(badDebtRows, BadDebtRow::getAmount));
		params.put("P_TOTAL_17", sum(feeRows, FeeHonorariumRow::getAmount));
		
		try (InputStream in =
			ExpenseDetailBinder.class.getResourceAsStream(jrxmlOnClasspath))
		{
			JasperReport report = null;
			
			try
			{
				report = JasperCompileManager.compileReport(in);
			}
			catch (JRException e)
			{
				Throwable t = e;
				
				while (t != null)
				{
					System.err.println("Cause: " + t.getClass().getName() +
						" - " + t.getMessage());
					t = t.getCause();
				}
				
				throw e;
			}
			
			// Top-level has no detail rows; use an empty data source of size 1
			return JasperFillManager.fillReport(report, params,
				new JREmptyDataSource(1));
		}
		
	}
	
	// ---------- Fill & export: 12b ----------
	public static JasperPrint fillExpense12b(String jrxmlOnClasspath,
		String orgName,
		String reportTitle,
		Collection<InsuranceExpenseRow> insuranceRows,
		Collection<OtherExpenseRow> otherRows,
		Collection<
			DonationRow> donationRows)
		throws Exception
	{
		
		Map<String, Object> params = new HashMap<>();
		params.put("P_ORG_NAME", orgName);
		params.put("P_REPORT_TITLE", reportTitle);
		
		// Row collections
		params.put("P_EXP20_INSURANCE_ROWS", ds(insuranceRows));
		params.put("P_EXP28_OTHER_ROWS", ds(otherRows));
		params.put("P_EXP29_DONATION_ROWS", ds(donationRows));
		
		// Totals
		params.put("P_TOTAL_20",
			sum(insuranceRows, InsuranceExpenseRow::getAmount));
		params.put("P_TOTAL_28", sum(otherRows, OtherExpenseRow::getAmount));
		params.put("P_TOTAL_29", sum(donationRows, DonationRow::getAmount));
		
		try (InputStream in =
			ExpenseDetailBinder.class.getResourceAsStream(jrxmlOnClasspath))
		{
			JasperReport report = null;
			
			try
			{
				report = JasperCompileManager.compileReport(in);
			}
			catch (JRException e)
			{				
				Throwable t = e;
				
				while (t != null)
				{
					System.err.println("Cause: " + t.getClass().getName() +
						" - " + t.getMessage());
					t = t.getCause();
				}
				
				throw e;
			}
			
			return JasperFillManager.fillReport(report, params,
				new JREmptyDataSource(1));
		}
		
	}
	
	// ---------- Demo main (compile + fill + export PDFs) ----------
	public static void main(String[] args) throws Exception
	{
		// Replace paths with where you put the JRXMLs on your classpath
		String jrxml12a = "/reports/EXPENSE_DTL_12a_ROW_BASED.jrxml";
		String jrxml12b = "/reports/EXPENSE_DTL_12b_ROW_BASED.jrxml";
		
		// Build example data (replace with your real data)
		List<AdvertisingExpenseRow> advertising = buildAdvertisingRows();
		List<BadDebtRow> badDebts = buildBadDebtRows();
		List<FeeHonorariumRow> fees = buildFeeRows();
		
		List<InsuranceExpenseRow> insurance = buildInsuranceRows();
		List<OtherExpenseRow> others = buildOtherRows();
		List<DonationRow> donations = buildDonationRows();
		
		JasperPrint print12a = fillExpense12a(
			jrxml12a, "Your Group, Inc.", "Expense Detail (Part 1)",
			advertising, badDebts, fees);
		JasperPrint print12b = fillExpense12b(
			jrxml12b, "Your Group, Inc.", "Expense Detail (Part 2)",
			insurance, others, donations);
		
		// Export to PDF
		JasperExportManager.exportReportToPdfFile(print12a,
			"EXPENSE_DTL_12a_ROW_BASED.pdf");
		JasperExportManager.exportReportToPdfFile(print12b,
			"EXPENSE_DTL_12b_ROW_BASED.pdf");
		
		System.out.println(
			"Generated PDFs: EXPENSE_DTL_12a_ROW_BASED.pdf, EXPENSE_DTL_12b_ROW_BASED.pdf");
		
	}
	
}
