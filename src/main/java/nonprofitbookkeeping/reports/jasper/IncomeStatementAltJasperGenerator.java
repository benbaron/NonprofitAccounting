
package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.reports.datasource.IncomeStatementAltRowBean;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator for the alternate Income Statement report.
 */
public class IncomeStatementAltJasperGenerator extends AbstractReportGenerator
{
	/**
	 * 
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData()
	 */
	@Override protected List<IncomeStatementAltRowBean> getReportData()
	{
		nonprofitbookkeeping.model.Company company =
			nonprofitbookkeeping.model.CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
		{
			return Collections.emptyList();
		}
		
		java.util.Map<String, java.math.BigDecimal> totals = new java.util.HashMap<>();
		
		for (nonprofitbookkeeping.model.AccountingTransaction tx : company.getLedger()
			.getTransactions())
		{
			if (tx == null || tx.getEntries() == null)
				continue;
			
			for (nonprofitbookkeeping.model.AccountingEntry entry : tx.getEntries())
			{
				if (entry == null || entry.getAmount() == null)
					continue;
				
				nonprofitbookkeeping.model.Account acct =
					company.getChartOfAccounts().getAccount(entry.getAccountNumber());
				
				if (acct == null || acct.getAccountType() == null)
					continue;
				
				if (acct.getAccountType() == nonprofitbookkeeping.model.AccountType.INCOME ||
					acct.getAccountType() == nonprofitbookkeeping.model.AccountType.EXPENSE)
				{
					java.math.BigDecimal amt =
						totals.getOrDefault(acct.getName(), java.math.BigDecimal.ZERO);
					
					if ((acct.getAccountType() == nonprofitbookkeeping.model.AccountType.INCOME &&
						entry.getAccountSide() == nonprofitbookkeeping.model.AccountSide.CREDIT) ||
						(acct.getAccountType() == nonprofitbookkeeping.model.AccountType.EXPENSE &&
							entry.getAccountSide() == nonprofitbookkeeping.model.AccountSide.DEBIT))
					{
						amt = amt.add(entry.getAmount());
					}
					else
					{
						amt = amt.subtract(entry.getAmount());
					}
					
					totals.put(acct.getName(), amt);
				}
				
			}
			
		}
		
		java.util.List<IncomeStatementAltRowBean> rows = new java.util.ArrayList<>();
		
		for (java.util.Map.Entry<String, java.math.BigDecimal> e : totals.entrySet())
		{
			rows.add(new IncomeStatementAltRowBean(e.getKey(), e.getValue()));
		}
		
		return rows;
	}
	
	@Override protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		params.put("P_REPORT_TITLE", "Income Statement");
		params.put("P_GENERATION_DATE", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
		return params;
	}
	
	@Override protected String getReportPath()
	{
		return bundledReportPath();
	}
	

	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getBaseName() 
	 */
	@Override public String getBaseName()
	{
		return "Income_Statement_" + LocalDate.now();
		
	}
	
}
