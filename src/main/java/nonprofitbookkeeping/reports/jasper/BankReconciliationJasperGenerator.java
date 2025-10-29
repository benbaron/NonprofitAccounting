
package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.reports.datasource.BankReconciliationRowBean;

import java.math.BigDecimal;
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
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData()
	 */
	@Override protected List<BankReconciliationRowBean> getReportData()
	{
		nonprofitbookkeeping.model.Company company =
			nonprofitbookkeeping.model.CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
		{
			return Collections.emptyList();
		}
		
		List<BankReconciliationRowBean> rows = new java.util.ArrayList<>();
		BigDecimal running = BigDecimal.ZERO;
		
		for (AccountingTransaction tx : company.getLedger()
			.getTransactions())
		{
			if (tx == null || tx.getEntries() == null)
			{
				continue;
			}
			
			for (AccountingEntry entry : tx.getEntries())
			{
				if (entry == null || entry.getAmount() == null)
				{
					continue;
				}
				
				Account acct =
					company.getChartOfAccounts().getAccount(entry.getAccountNumber());
				
				if (acct == null)
				{
					continue;
				}
				
				AccountType type = acct.getAccountType();
				
				if (type != AccountType.CASH &&
					type != AccountType.BANK &&
					type != AccountType.CHECKING)
				{
					continue;
				}
				
				BigDecimal deposit = BigDecimal.ZERO;
				BigDecimal withdrawal = BigDecimal.ZERO;
				
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
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getBaseName() 
	 */
	@Override public String getBaseName()
	{
		return "Bank_Reconciliation_" + LocalDate.now();
		
	}
	
}
