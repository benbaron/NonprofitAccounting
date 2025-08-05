
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.reports.datasource.AccountSummaryRowBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
	/**
	 * 
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData()
	 */
	@Override protected List<AccountSummaryRowBean> getReportData()
	{
		Company company =
			CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
		{
			return Collections.emptyList();
		}
		
		List<AccountSummaryRowBean> rows = new ArrayList<>();
		
		for (nonprofitbookkeeping.model.Account acct : company.getChartOfAccounts().getAccounts())
		{
			if (acct == null)
				continue;
			
			BigDecimal bal = acct.totalAccountBalance(company.getLedger());
			String debit = bal.compareTo(BigDecimal.ZERO) >= 0 ? bal.toPlainString() : "";
			String credit =
				bal.compareTo(java.math.BigDecimal.ZERO) < 0 ? bal.abs().toPlainString() : "";
			
			rows.add(new AccountSummaryRowBean("", "", "", "", "", debit, credit,
				acct.getAccountNumber(), acct.getName()));
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

	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getBaseName() 
	 */
	@Override protected String getBaseName()
	{
		return "Account_Summary_" + LocalDate.now();
		
	}
	
}
