
package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import nonprofitbookkeeping.reports.datasource.scareports.TransferOut10Row;
import nonprofitbookkeeping.reports.datasource.scareports.TransferOutOutsideKingdomRow;
import nonprofitbookkeeping.reports.datasource.scareports.TransferOutScaOfficeRow;
import nonprofitbookkeeping.reports.datasource.scareports.TransferOutWithinKingdomRow;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Utility binder for the TRANSFER_OUT_10 report page. The binder aggregates the
 * three transfer sections (within kingdom, outside kingdom and to the SCA
 * corporate office) and exposes simple totals for each.
 */
public class TransferOut10Binder
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
		return new JRBeanCollectionDataSource(
			rows == null ? Collections.emptyList() : rows, false);
		
	}
	
	// ---------- Example data builders ----------
	private static List<TransferOutWithinKingdomRow> buildWithinRows()
	{
		List<TransferOutWithinKingdomRow> list = new ArrayList<>();
		list.add(new TransferOutWithinKingdomRow("Barony of Example",
			"Event advance", "1001", "2025-04-01", new BigDecimal("125.00")));
		return list;
		
	}
	
	private static List<TransferOutOutsideKingdomRow> buildOutsideRows()
	{
		List<TransferOutOutsideKingdomRow> list = new ArrayList<>();
		list.add(new TransferOutOutsideKingdomRow("Atenveldt - Barony of Sun",
			"Prize reimbursement", "2002", "2025-04-15",
			new BigDecimal("75.00")));
		return list;
		
	}
	
	private static List<TransferOutScaOfficeRow> buildScaRows()
	{
		List<TransferOutScaOfficeRow> list = new ArrayList<>();
		list.add(new TransferOutScaOfficeRow("Society Exchequer",
			"Quarterly tithe", "3003", "2025-05-01", new BigDecimal("50.00")));
		return list;
		
	}
	
	// ---------- Fill & export ----------
	public static JasperPrint fillTransferOut10(String jrxmlOnClasspath,
		String orgName,
		String reportTitle,
		Collection<TransferOutWithinKingdomRow> withinRows,
		Collection<TransferOutOutsideKingdomRow> outsideRows,
		Collection<TransferOutScaOfficeRow> scaRows) throws Exception
	{
		List<TransferOut10Row> rows = new ArrayList<>();
		
		if (withinRows != null)
		{
			
			for (TransferOutWithinKingdomRow r : withinRows)
			{
				TransferOut10Row row = new TransferOut10Row();
				row.setSection("WITHIN_KINGDOM");
				row.setToAccountOrPayee(r.getAccountOrPayee());
				row.setReason(r.getReason());
				row.setCheckNumber(r.getCheckNumber());
				row.setCheckDate(r.getCheckDate());
				row.setAmount(r.getAmount());
				rows.add(row);
			}
			
		}
		
		if (outsideRows != null)
		{
			
			for (TransferOutOutsideKingdomRow r : outsideRows)
			{
				TransferOut10Row row = new TransferOut10Row();
				row.setSection("OUTSIDE_KINGDOM");
				row.setToAccountOrPayee(r.getKingdomAndBranch());
				row.setReason(r.getReason());
				row.setCheckNumber(r.getCheckNumber());
				row.setCheckDate(r.getCheckDate());
				row.setAmount(r.getAmount());
				rows.add(row);
			}
			
		}
		
		if (scaRows != null)
		{
			
			for (TransferOutScaOfficeRow r : scaRows)
			{
				TransferOut10Row row = new TransferOut10Row();
				row.setSection("SCA_OFFICE");
				row.setToAccountOrPayee(r.getOffice());
				row.setReason(r.getReason());
				row.setCheckNumber(r.getCheckNumber());
				row.setCheckDate(r.getCheckDate());
				row.setAmount(r.getAmount());
				rows.add(row);
			}
			
		}
		
		BigDecimal withinTotal =
			sum(withinRows, TransferOutWithinKingdomRow::getAmount);
		BigDecimal outsideTotal =
			sum(outsideRows, TransferOutOutsideKingdomRow::getAmount);
		BigDecimal scaTotal = sum(scaRows, TransferOutScaOfficeRow::getAmount);
		
		Map<String, Object> params = new HashMap<>();
		params.put("P_ORG_NAME", orgName);
		params.put("P_REPORT_TITLE", reportTitle);
		params.put("P_WITHIN_TOTAL", withinTotal);
		params.put("P_OUTSIDE_TOTAL", outsideTotal);
		params.put("P_SCA_TOTAL", scaTotal);
		
		try (InputStream in =
			TransferOut10Binder.class.getResourceAsStream(jrxmlOnClasspath))
		{
			JasperReport report = JasperCompileManager.compileReport(in);
			return JasperFillManager.fillReport(report, params, ds(rows));
		}
		
	}
	
	// ---------- Demo main (compile + fill + export PDF) ----------
	public static void main(String[] args) throws Exception
	{
		String jrxml =
			"/jrxml/sca-reports/TRANSFER_OUT_10_AUTO_STYLED_fixed_labeled_-_Copy_rowbased.jrxml";
		List<TransferOutWithinKingdomRow> within = buildWithinRows();
		List<TransferOutOutsideKingdomRow> outside = buildOutsideRows();
		List<TransferOutScaOfficeRow> sca = buildScaRows();
		
		JasperPrint print = fillTransferOut10(jrxml, "Sample Org",
			"Transfer Out Detail", within, outside, sca);
		JasperExportManager.exportReportToPdfFile(print, "TRANSFER_OUT_10.pdf");
		System.out.println("Generated PDF: TRANSFER_OUT_10.pdf");
		
	}
	
}
