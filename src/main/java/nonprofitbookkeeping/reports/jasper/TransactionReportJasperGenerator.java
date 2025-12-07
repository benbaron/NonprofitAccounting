
package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.reports.datasource.LedgerQueryCriteria;
import nonprofitbookkeeping.reports.datasource.LedgerQueryFacade;
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
        private final LedgerQueryFacade queryFacade;
        private final LedgerQueryCriteria criteria;
        private List<nonprofitbookkeeping.model.AccountingTransaction> providedTransactions;

        public TransactionReportJasperGenerator()
        {
                this(new LedgerQueryFacade(), LedgerQueryCriteria.builder().build());
        }

        public TransactionReportJasperGenerator(LedgerQueryFacade queryFacade,
                LedgerQueryCriteria criteria)
        {
                this.queryFacade = queryFacade == null ? new LedgerQueryFacade() : queryFacade;
                this.criteria = criteria == null ? LedgerQueryCriteria.builder().build() : criteria;
        }

        /**
         * Optionally provide transactions directly instead of reading from the current company.
         * Useful for testing and ad-hoc report generation pipelines.
         *
         * @param transactions transactions to render in the report
         * @return this generator for fluent use
         */
        public TransactionReportJasperGenerator withTransactions(
                List<AccountingTransaction> transactions)
        {
                this.providedTransactions = transactions;
                return this;
        }
	/**
	 * 
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData()
	 */
	@Override protected List<TransactionReportRowBean> getReportData()
	{
		nonprofitbookkeeping.model.Company company =
			nonprofitbookkeeping.model.CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
		{
			return Collections.emptyList();
		}
		
                List<AccountingTransaction> txns = selectTransactions(company);

                if (txns == null)
                {
                        return Collections.emptyList();
                }

                txns.sort(Comparator.comparingLong(AccountingTransaction::getBookingDateTimestamp));

                return this.queryFacade.queryFromTransactions(txns,
                        this.criteria,
                        tx -> mapToBean(company, tx));
        }

        private List<AccountingTransaction> selectTransactions(
                nonprofitbookkeeping.model.Company company)
        {
                if (this.providedTransactions != null)
                {
                        return this.providedTransactions;
                }

                if (company == null || company.getLedger() == null)
                {
                        return Collections.emptyList();
                }

                return company.getLedger().getTransactions();
        }

        private TransactionReportRowBean mapToBean(nonprofitbookkeeping.model.Company company,
                AccountingTransaction tx)
        {
                if (tx == null || tx.getEntries() == null || tx.getEntries().isEmpty())
                {
                        return null;
                }

                AccountingEntry first = tx.getEntries().iterator().next();
                nonprofitbookkeeping.model.Account acct = (company == null || company.getChartOfAccounts() == null)
                        ? null
                        : company.getChartOfAccounts().getAccount(first.getAccountNumber());

                String debit = "0";
                String credit = "0";
                java.math.BigDecimal totalDebit = java.math.BigDecimal.ZERO;
                java.math.BigDecimal totalCredit = java.math.BigDecimal.ZERO;

                for (nonprofitbookkeeping.model.AccountingEntry entry : tx.getEntries())
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

                if (totalDebit.compareTo(java.math.BigDecimal.ZERO) != 0)
                        debit = totalDebit.toPlainString();
                if (totalCredit.compareTo(java.math.BigDecimal.ZERO) != 0)
                        credit = totalCredit.toPlainString();

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
