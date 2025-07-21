
package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import nonprofitbookkeeping.reports.datasource.TransactionReportRowBean;

import java.util.ArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Generator for the Transaction report.
 */
public class TransactionReportJasperGenerator extends AbstractReportGenerator
{
	/**
	 * 
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData()
	 */
	@Override protected List<TransactionReportRowBean> getReportData()
	{
		nonprofitbookkeeping.model.Company company =
			nonprofitbookkeeping.model.CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
		{
			return Collections.emptyList();
		}
		
		List<TransactionReportRowBean> data = new ArrayList<>();
		
		List<nonprofitbookkeeping.model.AccountingTransaction> txns =
			company.getLedger().getTransactions();
		
		if (txns == null)
		{
			return data;
		}
		
		txns.sort(java.util.Comparator.comparingLong(
			nonprofitbookkeeping.model.AccountingTransaction::getBookingDateTimestamp));
		
		for (nonprofitbookkeeping.model.AccountingTransaction tx : txns)
		{
			if (tx == null || tx.getEntries() == null)
				continue;
			
			nonprofitbookkeeping.model.AccountingEntry first = tx.getEntries().iterator().next();
			nonprofitbookkeeping.model.Account acct =
				company.getChartOfAccounts().getAccount(first.getAccountNumber());
			
			String debit = "0";
			String credit = "0";
			java.math.BigDecimal totalDebit = java.math.BigDecimal.ZERO;
			java.math.BigDecimal totalCredit = java.math.BigDecimal.ZERO;
			
			for (nonprofitbookkeeping.model.AccountingEntry entry : tx.getEntries())
			{
				
				if (entry.getAccountSide() == nonprofitbookkeeping.model.AccountSide.DEBIT)
				{
					totalDebit = totalDebit.add(entry.getAmount());
				}
				else
				{
					totalCredit = totalCredit.add(entry.getAmount());
				}
				
			}
			
			if (totalDebit.compareTo(java.math.BigDecimal.ZERO) != 0)
				debit = totalDebit.toPlainString();
			if (totalCredit.compareTo(java.math.BigDecimal.ZERO) != 0)
				credit = totalCredit.toPlainString();
			
			data.add(new TransactionReportRowBean(String.valueOf(tx.getBookingDateTimestamp()),
				tx.getDate(), tx.getMemo() != null ? tx.getMemo() : "",
				tx.getMemo() != null ? tx.getMemo() : "", "", tx.getDate(),
				acct != null ? acct.getAccountNumber() : first.getAccountNumber(),
				acct != null ? acct.getName() : first.getAccountNumber(), "", debit, credit));
		}
		
		return data;
	}
	
	@Override protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
	}
	
	@Override protected String getReportPath()
	{
		return "jrxml/TransactionReport.jrxml";
	}
	
	@Override public File generateAndExportReport(String format) throws Exception
	{
		
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(getReportPath()))
		{
			
			if (in == null)
			{
				throw new FileNotFoundException("Report template not found: " + getReportPath());
			}
			
			JasperReport report = JasperCompileManager.compileReport(in);
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(getReportData());
			JasperPrint print = JasperFillManager.fillReport(report, getReportParameters(), ds);
			
			File outDir = new File(getOutputDirectory());
			
			if (!outDir.exists())
			{
				outDir.mkdirs();
			}
			
			String base = "Transaction_Report_" + LocalDate.now();
			File out =
				new File(outDir, base + ("html".equalsIgnoreCase(format) ? ".html" : ".pdf"));
			
			if ("html".equalsIgnoreCase(format))
			{
				return exportToHTML(print, out.getAbsolutePath());
			}
			
			return exportToPDF(print, out.getAbsolutePath());
		}
		
	}
	
}
