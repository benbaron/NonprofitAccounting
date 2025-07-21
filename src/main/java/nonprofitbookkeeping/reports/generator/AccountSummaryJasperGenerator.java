
package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import nonprofitbookkeeping.reports.datasource.AccountSummaryRowBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Basic generator for the Account Summary report.
 * <p>
 * This implementation currently returns an empty data set and does not
 * populate any parameters. It is primarily intended to demonstrate how
 * to compile and export the {@code AccountSummary.jrxml} template.
 * </p>
 */
public class AccountSummaryJasperGenerator extends AbstractReportGenerator
{
	
       @Override protected List<AccountSummaryRowBean> getReportData()
       {
               nonprofitbookkeeping.model.Company company =
                               nonprofitbookkeeping.model.CurrentCompany.getCompany();

               if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
               {
                       return Collections.emptyList();
               }

               java.util.List<AccountSummaryRowBean> rows = new java.util.ArrayList<>();

               for (nonprofitbookkeeping.model.Account acct : company.getChartOfAccounts().getAccounts())
               {
                       if (acct == null)
                               continue;

                       java.math.BigDecimal bal = acct.totalAccountBalance(company.getLedger());
                       String debit = bal.compareTo(java.math.BigDecimal.ZERO) >= 0 ? bal.toPlainString() : "";
                       String credit = bal.compareTo(java.math.BigDecimal.ZERO) < 0 ? bal.abs().toPlainString() : "";

                       rows.add(new AccountSummaryRowBean(
                                       "",
                                       "",
                                       "",
                                       "",
                                       "",
                                       debit,
                                       credit,
                                       acct.getAccountNumber(),
                                       acct.getName()));
               }

               return rows;
       }
	
	@Override protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
	}
	
	@Override protected String getReportPath()
	{
		return "jrxml/AccountSummary.jrxml";
	}
	
	@Override public File generateAndExportReport(String format) throws Exception
	{
		String jrxml = getReportPath();
		
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(jrxml))
		{
			
			if (in == null)
			{
				throw new FileNotFoundException("Report template not found: " + jrxml);
			}
			
			JasperReport report = JasperCompileManager.compileReport(in);
			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(getReportData());
			JasperPrint print = JasperFillManager.fillReport(report, getReportParameters(), ds);
			
			File outDir = new File(getOutputDirectory());
			
			if (!outDir.exists())
			{
				outDir.mkdirs();
			}
			
			String base = "Account_Summary_" + LocalDate.now();
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
