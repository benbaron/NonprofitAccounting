
package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.TransactionReportRowBean;
import nonprofitbookkeeping.reports.query.TransactionQueryFacade;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Generator responsible for producing the Jasper Transaction report. The
 * generator pulls transactions using {@link TransactionQueryFacade}, converts
 * them into {@link nonprofitbookkeeping.reports.datasource.TransactionReportRowBean}
 * instances, and delegates rendering to the shared {@link AbstractReportGenerator}
 * infrastructure.
 */
public class TransactionReportJasperGenerator extends AbstractReportGenerator
{
        private final TransactionQueryFacade queryFacade;
        private final TransactionQueryFacade.QueryConfig queryConfig;
        private final ReportContext context;

        /**
         * Creates a transaction report generator with a default, empty
         * {@link ReportContext} and the current company's transactions.
         */
        public TransactionReportJasperGenerator()
        {
                this(new ReportContext());
        }

        /**
         * Creates a transaction report generator using the provided context and
         * querying transactions from the current company.
         *
         * @param context context describing date ranges, accounts, and type
         *                filters for the report
         */
        public TransactionReportJasperGenerator(ReportContext context)
        {
                this(context, new TransactionQueryFacade());
        }

        /**
         * Creates a generator with explicit collaborators, primarily for testing.
         *
         * @param context     report configuration and filter criteria
         * @param queryFacade facade used to fetch and filter transactions
         */
        TransactionReportJasperGenerator(ReportContext context, TransactionQueryFacade queryFacade)
        {
                this.context = context == null ? new ReportContext() : context;
                this.queryFacade = queryFacade == null ? new TransactionQueryFacade() : queryFacade;
                this.queryConfig = buildQueryConfig(this.context);
        }

        /**
         * Retrieves and maps transactions into row beans used by the Jasper
         * template.
         *
         * @return ordered list of report row beans
         */
        @Override protected List<TransactionReportRowBean> getReportData()
        {
                return this.queryFacade.queryAndMap(this.queryConfig, this::toRowBean);
        }

        /**
         * Converts a transaction record into a single report row bean, summing
         * debit and credit amounts for the scoped entries.
         *
         * @param record transaction record produced by the query facade
         * @return populated row bean or {@code null} if the record cannot be
         *         mapped
         */
        private TransactionReportRowBean toRowBean(TransactionQueryFacade.TransactionRecord record)
        {
                AccountingTransaction transaction = record.transaction();
                List<AccountingEntry> entries = record.entries();
                if (entries == null || entries.isEmpty())
                {
                        return null;
                }

                AccountingEntry primaryEntry = entries.stream()
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
                if (primaryEntry == null)
                {
                        return null;
                }

                Company company = CurrentCompany.getCompany();
                ChartOfAccounts chart = company == null ? null : company.getChartOfAccounts();
                Account account = chart == null ? null : chart.getAccount(primaryEntry.getAccountNumber());

                java.math.BigDecimal totalDebit = java.math.BigDecimal.ZERO;
                java.math.BigDecimal totalCredit = java.math.BigDecimal.ZERO;

                for (AccountingEntry entry : entries)
                {
                        if (entry == null)
                        {
                                continue;
                        }

                        if (entry.getAccountSide() == AccountSide.DEBIT)
                        {
                                totalDebit = totalDebit.add(entry.getAmount());
                        }
                        else
                        {
                                totalCredit = totalCredit.add(entry.getAmount());
                        }

                }

                String debit = totalDebit.compareTo(java.math.BigDecimal.ZERO) != 0
                        ? totalDebit.toPlainString() : "0";
                String credit = totalCredit.compareTo(java.math.BigDecimal.ZERO) != 0
                        ? totalCredit.toPlainString() : "0";

                String memo = transaction.getMemo() != null ? transaction.getMemo() : "";
                String accountNumber = account != null ? account.getAccountNumber() : primaryEntry.getAccountNumber();
                String accountName = account != null ? account.getName() : primaryEntry.getAccountNumber();

                return new TransactionReportRowBean(String.valueOf(transaction.getBookingDateTimestamp()),
                        transaction.getDate(), memo,
                        memo, "", transaction.getDate(),
                        accountNumber,
                        accountName, "", debit, credit);
        }

        @Override protected Map<String, Object> getReportParameters()
        {
                return Collections.emptyMap();
        }

        /**
         * Resolves the bundled report path for the Jasper template.
         */
        @Override protected String getReportPath()
        {
                return bundledReportPath();
        }

        /**
         * Provides a date-stamped base name for the generated report output.
         *
         * @return base name used by the {@link AbstractReportGenerator}
         */
        @Override public String getBaseName()
        {
                return "Transaction_Report_" + LocalDate.now();

        }

        /**
         * Builds a query configuration based on the supplied report context,
         * capturing date ranges, account filters, memo filters, and transaction
         * side constraints.
         */
        private static TransactionQueryFacade.QueryConfig buildQueryConfig(ReportContext context)
        {
                TransactionQueryFacade.QueryConfig.Builder builder = TransactionQueryFacade.QueryConfig
                        .builder()
                        .withDateRange(context.getStartDate(), context.getEndDate())
                        .withAccounts(context.getAccountIdsForDetailReport(), context.isRequireAllAccounts())
                        .withMemoSubstring(context.getMemoFilter());

                AccountSide side = parseSide(context.getTransactionType());
                builder.withTransactionType(side);
                return builder.build();
        }

        /**
         * Parses a transaction side string into the corresponding enum value.
         *
         * @param sideText text representation of the side
         * @return matching {@link AccountSide} or {@code null} when invalid or
         *         absent
         */
        private static AccountSide parseSide(String sideText)
        {
                if (sideText == null || sideText.isBlank())
                {
                        return null;
                }

                try
                {
                        return AccountSide.valueOf(sideText.trim().toUpperCase());
                }
                catch (IllegalArgumentException ex)
                {
                        return null;
                }
        }
}
