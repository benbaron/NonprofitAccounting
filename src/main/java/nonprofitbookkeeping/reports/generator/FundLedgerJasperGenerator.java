
package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import nonprofitbookkeeping.reports.datasource.FundLedgerRowBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator for the Fund Ledger report.
 */
public class FundLedgerJasperGenerator extends AbstractReportGenerator
{
	
	@Override protected List<FundLedgerRowBean> getReportData()
	{
		nonprofitbookkeeping.model.Company company =
			nonprofitbookkeeping.model.CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
		{
			return Collections.emptyList();
		}
		
		java.util.List<FundLedgerRowBean> rows = new java.util.ArrayList<>();
		java.math.BigDecimal running = java.math.BigDecimal.ZERO;
		
		for (nonprofitbookkeeping.model.AccountingTransaction tx : company.getLedger()
			.getTransactions())
		{
			if (tx == null || tx.getEntries() == null)
			{
				continue;
			}
			
			for (nonprofitbookkeeping.model.AccountingEntry entry : tx.getEntries())
			{
				if (entry == null || entry.getAmount() == null)
				{
					continue;
				}
				
				java.math.BigDecimal debit = java.math.BigDecimal.ZERO;
				java.math.BigDecimal credit = java.math.BigDecimal.ZERO;
				
				if (entry.getAccountSide() == nonprofitbookkeeping.model.AccountSide.DEBIT)
				{
					debit = entry.getAmount();
					running = running.add(debit);
				}
				else
				{
					credit = entry.getAmount();
					running = running.subtract(credit);
				}
				
				rows.add(new FundLedgerRowBean(tx.getDate(),
					tx.getMemo() != null ? tx.getMemo() : "", debit, credit, running));
			}
			
		}
		
		return rows;
	}
	
	@Override protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		params.put("P_REPORT_TITLE", "Fund Ledger");
		
		String companyName = "N/A";
		
		if (nonprofitbookkeeping.model.CurrentCompany.getCompany() != null &&
			nonprofitbookkeeping.model.CurrentCompany.getCompany().getCompanyProfile() != null &&
			nonprofitbookkeeping.model.CurrentCompany.getCompany().getCompanyProfile()
				.getCompanyName() != null)
		{
			companyName = nonprofitbookkeeping.model.CurrentCompany.getCompany().getCompanyProfile()
				.getCompanyName();
		}
		
		params.put("P_COMPANY_NAME", companyName);
		params.put("P_FUND", "N/A");
		params.put("P_REPORT_PERIOD", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
		params.put("P_GENERATION_DATE", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
		return params;
	}
	
	@Override protected String getReportPath()
	{
		return "jrxml/FundLedger.jrxml";
	}
	
	@Override public File generateAndExportReport(String format) throws Exception
	{
		String baseName = "Fund_Ledger_" + LocalDate.now();
		
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(getReportPath()))
		{
			
			if (in == null)
			{
				throw new FileNotFoundException("JRXML not found: " + getReportPath());
			}
			
			JasperReport jasperReport = JasperCompileManager.compileReport(in);
			JRDataSource dataSource = new JRBeanCollectionDataSource(getReportData());
			JasperPrint print =
				JasperFillManager.fillReport(jasperReport, getReportParameters(), dataSource);
			File outDir = new File(getOutputDirectory());
			
			if (!outDir.exists())
			{
				outDir.mkdirs();
			}
			
			File outFile =
				new File(outDir, baseName + ("html".equalsIgnoreCase(format) ? ".html" : ".pdf"));
			
			if ("html".equalsIgnoreCase(format))
			{
				return exportToHTML(print, outFile.getAbsolutePath());
			}
			
			return exportToPDF(print, outFile.getAbsolutePath());
		}
		
	}
	
}
