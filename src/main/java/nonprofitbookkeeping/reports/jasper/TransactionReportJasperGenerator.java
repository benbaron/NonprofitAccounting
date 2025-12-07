
package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.reports.datasource.TransactionQueryFacade;
import nonprofitbookkeeping.reports.datasource.TransactionReportRowBean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Generator for the Transaction report.
 */
public class TransactionReportJasperGenerator extends AbstractReportGenerator
{
	/**
	 * 
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData()
	 */
        @Override protected List<TransactionReportRowBean> getReportData()
        {
                Company company = CurrentCompany.getCompany();

                if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
                {
                        return Collections.emptyList();
                }

                List<AccountingTransaction> txns = company.getLedger().getTransactions();
                if (txns == null)
                {
                        return Collections.emptyList();
                }

                txns.sort(Comparator.comparingLong(AccountingTransaction::getBookingDateTimestamp));

                TransactionQueryFacade query = new TransactionQueryFacade();
                List<TransactionReportRowBean> data = query.mapToBeans(txns,
                        tx -> toRowBean(tx, company));

                return data;
        }

        private TransactionReportRowBean toRowBean(AccountingTransaction tx, Company company)
        {
                AccountingEntry first = tx.getEntries().iterator().next();
                Account acct = company.getChartOfAccounts().getAccount(first.getAccountNumber());

                java.math.BigDecimal totalDebit = java.math.BigDecimal.ZERO;
                java.math.BigDecimal totalCredit = java.math.BigDecimal.ZERO;

                for (AccountingEntry entry : tx.getEntries())
                {
                        if (entry.getAccountSide() == AccountSide.DEBIT)
                        {
                                totalDebit = totalDebit.add(entry.getAmount());
                        }
                        else
                        {
                                totalCredit = totalCredit.add(entry.getAmount());
                        }
                }

                String debit = totalDebit.compareTo(java.math.BigDecimal.ZERO) == 0
                        ? "0"
                        : totalDebit.toPlainString();
                String credit = totalCredit.compareTo(java.math.BigDecimal.ZERO) == 0
                        ? "0"
                        : totalCredit.toPlainString();

                return new TransactionReportRowBean(String.valueOf(tx.getBookingDateTimestamp()),
                        tx.getDate(), tx.getMemo() != null ? tx.getMemo() : "",
                        tx.getMemo() != null ? tx.getMemo() : "", "", tx.getDate(),
                        acct != null ? acct.getAccountNumber() : first.getAccountNumber(),
                        acct != null ? acct.getName() : first.getAccountNumber(), "", debit, credit);
        }
	
	@Override protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
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
		return "Transaction_Report_" + LocalDate.now();
		
	}
	
}
