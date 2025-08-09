
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.reports.datasource.AccountLedgerRowBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator for the Account Ledger report.
 */
public class AccountLedgerJasperGenerator extends AbstractReportGenerator
{
	/**
	 * 
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData()
	 */
	@Override protected List<AccountLedgerRowBean> getReportData()
	{
		nonprofitbookkeeping.model.Company company =
				nonprofitbookkeeping.model.CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null)
		{
			return Collections.emptyList();
		}
		
		List<AccountLedgerRowBean> rows = new ArrayList<>();
		BigDecimal running = BigDecimal.ZERO;
		
		List<nonprofitbookkeeping.model.AccountingTransaction> txns =
				company.getLedger().getTransactions();
		
		if (txns == null)
		{
			return rows;
		}
		
		txns.sort(Comparator.comparingLong(
				nonprofitbookkeeping.model.AccountingTransaction::getBookingDateTimestamp));
		
		for (nonprofitbookkeeping.model.AccountingTransaction tx : txns)
		{
			if (tx == null || tx.getEntries() == null)
			{
				continue;
			}
			
			BigDecimal debit = BigDecimal.ZERO;
			BigDecimal credit = BigDecimal.ZERO;
			
			for (AccountingEntry entry : tx.getEntries())
			{
				if (entry == null || entry.getAmount() == null)
					continue;
				
				if (entry.getAccountSide() == nonprofitbookkeeping.model.AccountSide.DEBIT)
				{
					debit = debit.add(entry.getAmount());
				}
				else
				{
					credit = credit.add(entry.getAmount());
				}
				
			}
			
			running = running.add(debit).subtract(credit);
			
			rows.add(new AccountLedgerRowBean(	tx.getDate(),
												tx.getMemo() != null ? 
													tx.getMemo() : "", 
													debit,
													credit, 
													running));
		}
		
		return rows;
		
	}
	
	@Override protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		params.put("P_REPORT_TITLE", "Account Ledger");
		
		String companyName = "N/A";
		
		if (nonprofitbookkeeping.model.CurrentCompany.getCompany() != null &&
				nonprofitbookkeeping.model.CurrentCompany.getCompany().getCompanyProfile() !=
						null &&
				nonprofitbookkeeping.model.CurrentCompany.getCompany().getCompanyProfile()
						.getCompanyName() != null)
		{
			companyName = nonprofitbookkeeping.model.CurrentCompany.getCompany().getCompanyProfile()
					.getCompanyName();
		}
		
		params.put("P_COMPANY_NAME", companyName);
		params.put("P_ACCOUNT", "N/A");
		params.put("P_REPORT_PERIOD", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
		params.put("P_GENERATION_DATE", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
		return params;
		
	}
	
	@Override protected String getReportPath()
	{
		return "jrxml/AccountLedger.jrxml";
		
	}
	
	// write output
	@Override public String getBaseName()
	{
		return "Account_Ledger_" + LocalDate.now();	
	}
	


	
}
