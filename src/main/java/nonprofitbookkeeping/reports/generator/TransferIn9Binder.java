
package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import nonprofitbookkeeping.reports.datasource.scareports.TransferIn9Row;
import nonprofitbookkeeping.reports.datasource.scareports.TransferInOutsideKingdomRow;
import nonprofitbookkeeping.reports.datasource.scareports.TransferInWithinKingdomRow;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Utility binder for the TRANSFER_IN_9 report page. The binder wires the two
 * transfer sections (within kingdom and outside kingdom) into the Jasper
 * template and exposes simple totals for each section.
 */
public class TransferIn9Binder
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
	private static List<TransferInWithinKingdomRow> buildWithinRows()
	{
		List<TransferInWithinKingdomRow> list = new ArrayList<>();
		list.add(new TransferInWithinKingdomRow("Barony of Example", "1234",
			"2025-01-15", new BigDecimal("150.00")));
		list.add(new TransferInWithinKingdomRow("Shire of Sample", "5678",
			"2025-02-10", new BigDecimal("200.00")));
		return list;
		
	}
	
	private static List<TransferInOutsideKingdomRow> buildOutsideRows()
	{
		List<TransferInOutsideKingdomRow> list = new ArrayList<>();
		list.add(new TransferInOutsideKingdomRow("Atenveldt - Barony of Sun",
			"9012", "2025-03-05", new BigDecimal("75.00")));
		return list;
		
	}
	
	// ---------- Fill & export ----------
	public static JasperPrint fillTransferIn9(String jrxmlOnClasspath,
		String orgName,
		String reportTitle,
		Collection<TransferInWithinKingdomRow> withinRows,
		Collection<TransferInOutsideKingdomRow> outsideRows) throws Exception
	{
		List<TransferIn9Row> rows = new ArrayList<>();
		
		if (withinRows != null)
		{
			
			for (TransferInWithinKingdomRow r : withinRows)
			{
				TransferIn9Row row = new TransferIn9Row();
				row.setSection("WITHIN_KINGDOM");
				row.setAccount(r.getBranchOrAccount());
				row.setCheckNumber(r.getCheckNumber());
				row.setCheckDate(r.getCheckDate());
				row.setAmount(r.getAmount());
				rows.add(row);
			}
			
		}
		
		if (outsideRows != null)
		{
			
			for (TransferInOutsideKingdomRow r : outsideRows)
			{
				TransferIn9Row row = new TransferIn9Row();
				row.setSection("OUTSIDE_KINGDOM");
				row.setAccount(r.getKingdomAndBranch());
				row.setCheckNumber(r.getCheckNumber());
				row.setCheckDate(r.getCheckDate());
				row.setAmount(r.getAmount());
				rows.add(row);
			}
			
		}
		
		BigDecimal withinTotal =
			sum(withinRows, TransferInWithinKingdomRow::getAmount);
		BigDecimal outsideTotal =
			sum(outsideRows, TransferInOutsideKingdomRow::getAmount);
		
		Map<String, Object> params = new HashMap<>();
		params.put("P_ORG_NAME", orgName);
		params.put("P_REPORT_TITLE", reportTitle);
		params.put("P_WITHIN_TOTAL", withinTotal);
		params.put("P_OUTSIDE_TOTAL", outsideTotal);
		
		try (InputStream in =
			TransferIn9Binder.class.getResourceAsStream(jrxmlOnClasspath))
		{
			JasperReport report = JasperCompileManager.compileReport(in);
			return JasperFillManager.fillReport(report, params, ds(rows));
		}
		
	}
	
	// ---------- Demo main (compile + fill + export PDF) ----------
	public static void main(String[] args) throws Exception
	{
		String jrxml =
			"/jrxml/sca-reports/TRANSFER_IN_9_AUTO_STYLED_fixed_labeled_rowbased.jrxml";
		List<TransferInWithinKingdomRow> within = buildWithinRows();
		List<TransferInOutsideKingdomRow> outside = buildOutsideRows();
		
		JasperPrint print = fillTransferIn9(jrxml, "Sample Org",
			"Transfer In Detail", within, outside);
		JasperExportManager.exportReportToPdfFile(print, "TRANSFER_IN_9.pdf");
		System.out.println("Generated PDF: TRANSFER_IN_9.pdf");
		
	}
	
}

