
package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import nonprofitbookkeeping.reports.datasource.BankReconciliationRowBean;

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
 * Generator for the Bank Reconciliation report.
 */
public class BankReconciliationJasperGenerator extends AbstractReportGenerator
{
	/**
	 * 
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData()
	 */
	@Override protected List<BankReconciliationRowBean> getReportData()
	{
		nonprofitbookkeeping.model.Company company =
			nonprofitbookkeeping.model.CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
		{
			return Collections.emptyList();
		}
		
		java.util.List<BankReconciliationRowBean> rows = new java.util.ArrayList<>();
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
				
				nonprofitbookkeeping.model.Account acct =
					company.getChartOfAccounts().getAccount(entry.getAccountNumber());
				
				if (acct == null)
				{
					continue;
				}
				
				nonprofitbookkeeping.model.AccountType type = acct.getAccountType();
				
				if (type != nonprofitbookkeeping.model.AccountType.CASH &&
					type != nonprofitbookkeeping.model.AccountType.BANK &&
					type != nonprofitbookkeeping.model.AccountType.CHECKING)
				{
					continue;
				}
				
				java.math.BigDecimal deposit = java.math.BigDecimal.ZERO;
				java.math.BigDecimal withdrawal = java.math.BigDecimal.ZERO;
				
				if (entry.getAccountSide() == nonprofitbookkeeping.model.AccountSide.DEBIT)
				{
					deposit = entry.getAmount();
					running = running.add(deposit);
				}
				else
				{
					withdrawal = entry.getAmount();
					running = running.subtract(withdrawal);
				}
				
				rows.add(new BankReconciliationRowBean(tx.getDate(),
					tx.getMemo() != null ? tx.getMemo() : "", deposit, withdrawal, running));
			}
			
		}
		
		return rows;
	}
	
	@Override protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		params.put("P_REPORT_TITLE", "Bank Reconciliation");
		
		
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
		params.put("P_STATEMENT_DATE", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
		params.put("P_GENERATION_DATE", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
		return params;
	}
	
	@Override protected String getReportPath()
	{
		return "jrxml/BankReconciliation.jrxml";
	}
	
	@Override public File generateAndExportReport(String format) throws Exception
	{
		String baseName = "Bank_Reconciliation_" + LocalDate.now();
		
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
