
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.GeneralLedgerRowBean;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator for the General Ledger report.
 */
public class GeneralLedgerJasperGenerator extends AbstractReportGenerator
{
	/**
	 * 
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData()
	 */
	@Override protected List<GeneralLedgerRowBean> getReportData()
	{
		nonprofitbookkeeping.model.Company company =
			nonprofitbookkeeping.model.CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null)
		{
			return Collections.emptyList();
		}
		
		java.util.List<GeneralLedgerRowBean> rows = new java.util.ArrayList<>();
		
		java.util.Map<String, java.math.BigDecimal> balances = new java.util.HashMap<>();
		
		java.util.List<nonprofitbookkeeping.model.AccountingTransaction> txns =
			company.getLedger().getTransactions();
		
		if (txns == null)
		{
			return rows;
		}
		
		txns.sort(java.util.Comparator.comparingLong(
			nonprofitbookkeeping.model.AccountingTransaction::getBookingDateTimestamp));
		
		for (nonprofitbookkeeping.model.AccountingTransaction tx : txns)
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
				
				String acct = entry.getAccountNumber();
				java.math.BigDecimal bal = balances.getOrDefault(acct, java.math.BigDecimal.ZERO);
				
				java.math.BigDecimal debit = java.math.BigDecimal.ZERO;
				java.math.BigDecimal credit = java.math.BigDecimal.ZERO;
				
				if (entry.getAccountSide() == nonprofitbookkeeping.model.AccountSide.DEBIT)
				{
					debit = entry.getAmount();
					bal = bal.add(debit);
				}
				else
				{
					credit = entry.getAmount();
					bal = bal.subtract(credit);
				}
				
				balances.put(acct, bal);
				
				rows.add(new GeneralLedgerRowBean(tx.getDate(), acct,
					tx.getMemo() != null ? tx.getMemo() : "", debit, credit, bal));
			}
			
		}
		
		return rows;
	}
	
	@Override protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		params.put("P_REPORT_TITLE", "General Ledger");
		
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
		params.put("P_REPORT_PERIOD", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
		params.put("P_GENERATION_DATE", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
		return params;
	}
	
	@Override protected String getReportPath()
	{
		return "jrxml/GeneralLedger.jrxml";
	}
	

	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getBaseName() 
	 */
	@Override public String getBaseName()
	{
		return "General_Ledger_" + LocalDate.now();
		
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#setReportData(java.util.List) 
	 */
	@Override
	public void setReportData(List<?> data)
	{
		// TODO Auto-generated method stub
		
		
	}
	
}
