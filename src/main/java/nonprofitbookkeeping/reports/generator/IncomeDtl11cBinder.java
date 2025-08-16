
package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import nonprofitbookkeeping.reports.datasource.scareports.DonationNonScaRow;
import nonprofitbookkeeping.reports.datasource.scareports.DonationScaRow;
import nonprofitbookkeeping.reports.datasource.scareports.Income11cRow;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Utility binder for the INCOME_DTL_11c report page. Handles donations from
 * SCA and non-SCA sources.
 */
public class IncomeDtl11cBinder
{
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
				{
					total = total.add(v);
				}
				
			}
			
		}
		
		return total;
		
	}
	
	private static JRBeanCollectionDataSource ds(Collection<?> rows)
	{
		return new JRBeanCollectionDataSource(
			rows == null ? Collections.emptyList() : rows, false);
		
	}
	
	// ---------- Example data builders ----------
	private static List<DonationScaRow> buildScaRows()
	{
		return Arrays.asList(
			new DonationScaRow("Barony of Example", "General support", "",
				new BigDecimal("150.00")));
		
	}
	
	private static List<DonationNonScaRow> buildNonScaRows()
	{
		return Arrays.asList(
			new DonationNonScaRow("Local Business", "Sponsorship", "",
				new BigDecimal("80.00")));
		
	}
	
	// ---------- Fill & export ----------
	public static JasperPrint fillIncome11c(String jrxmlOnClasspath,
		String orgName,
		String reportTitle,
		Collection<DonationScaRow> scaRows,
		Collection<DonationNonScaRow> nonScaRows) throws Exception
	{
		List<Income11cRow> rows = new ArrayList<>();
		
		if (scaRows != null)
		{
			
			for (DonationScaRow r : scaRows)
			{
				Income11cRow row = new Income11cRow();
				row.setDonorOrSource(r.getDonorGroup());
				row.setDescription(r.getDescription());
				row.setInKindOrNotes(r.getNotes());
				row.setAmount(r.getAmount());
				rows.add(row);
			}
			
		}
		
		if (nonScaRows != null)
		{
			
			for (DonationNonScaRow r : nonScaRows)
			{
				Income11cRow row = new Income11cRow();
				row.setDonorOrSource(r.getDonorName());
				row.setDescription(r.getDescription());
				row.setInKindOrNotes(r.getNotes());
				row.setAmount(r.getAmount());
				rows.add(row);
			}
			
		}
		
		BigDecimal scaTotal = sum(scaRows, DonationScaRow::getAmount);
		BigDecimal nonScaTotal = sum(nonScaRows, DonationNonScaRow::getAmount);
		
		Map<String, Object> params = new HashMap<>();
		params.put("P_ORG_NAME", orgName);
		params.put("P_REPORT_TITLE", reportTitle);
		params.put("P_SCA_TOTAL", scaTotal);
		params.put("P_NON_SCA_TOTAL", nonScaTotal);
		
		try (InputStream in =
			IncomeDtl11cBinder.class.getResourceAsStream(jrxmlOnClasspath))
		{
			JasperReport report = JasperCompileManager.compileReport(in);
			return JasperFillManager.fillReport(report, params, ds(rows));
		}
		
	}
	
	// ---------- Demo main ----------
	public static void main(String[] args) throws Exception
	{
		String jrxml =
			"/jrxml/sca-reports/INCOME_DTL_11c_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml";
		JasperPrint print = fillIncome11c(jrxml,
			"Sample Org",
			"Income Detail 11c",
			buildScaRows(),
			buildNonScaRows());
		JasperExportManager.exportReportToPdfFile(print, "INCOME_DTL_11c.pdf");
		System.out.println("Generated PDF: INCOME_DTL_11c.pdf");
		
	}
	
}
