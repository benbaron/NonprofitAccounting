/**
 * nonprofit-scaledger-ribbon.zip_expanded PageViewer.java PageViewer
 */

package nonprofitbookkeeping.ui.actions.scaledger;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.swing.table.DefaultTableModel;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.util.FormatUtils;

/**
 * Utility class for constructing tabular data used by
 * {@link nonprofitbookkeeping.plugins.scaledger.ui.PageViewerPanel}. Rather
 * than returning placeholder content, the model now reflects the currently
 * loaded company's ledger transactions so that Swing and JavaFX viewers can
 * present meaningful information without additional wiring.
 */
public final class PageViewer
{

        private static final String[] COLUMN_HEADERS =
        { "Date", "Transaction", "Account", "Debit", "Credit", "Memo" };

        private PageViewer()
        {
        }

        /**
         * Builds a {@link DefaultTableModel} populated with the current
         * company's journal entries. When no company or ledger data is
         * available, the model still contains a single row describing the
         * empty state so that downstream exporters can surface a helpful
         * message to users.
         *
         * @return an immutable model describing the current ledger contents
         */
        public static DefaultTableModel getTableModel()
        {
                DefaultTableModel model = new DefaultTableModel(COLUMN_HEADERS, 0)
                {
                        @Override public boolean isCellEditable(int row, int column)
                        {
                                return false;
                        }
                };

                if (!CurrentCompany.isOpen())
                {
                        model.addRow(emptyStateRow("No company is currently open."));
                        return model;
                }

                Company company = CurrentCompany.getCompany();

                if (company == null)
                {
                        model.addRow(emptyStateRow("No company data available."));
                        return model;
                }

                Ledger ledger = company.getLedger();

                if (ledger == null)
                {
                        model.addRow(emptyStateRow("Ledger is not loaded for the current company."));
                        return model;
                }

                List<AccountingTransaction> transactions = new ArrayList<>(ledger.getTransactions());

                if (transactions.isEmpty())
                {
                        model.addRow(emptyStateRow("No transactions recorded."));
                        return model;
                }

                transactions.sort(Comparator.comparingLong(PageViewer::transactionSortKey));

                for (AccountingTransaction tx : transactions)
                {
                        if (tx == null || tx.getEntries() == null || tx.getEntries().isEmpty())
                        {
                                continue;
                        }

                        List<AccountingEntry> entries = new ArrayList<>(tx.getEntries());
                        entries.sort(entryComparator(company));

                        for (AccountingEntry entry : entries)
                        {
                                model.addRow(toRow(tx, entry, company));
                        }
                }

                if (model.getRowCount() == 0)
                {
                        model.addRow(emptyStateRow("Transactions contain no entries."));
                }

                return model;
        }

        private static Object[] emptyStateRow(String message)
        {
                return new Object[]
                { "-", "-", "-", "", "", message };
        }

        private static Comparator<AccountingEntry> entryComparator(Company company)
        {
                return Comparator.comparing((AccountingEntry e) -> e == null ? null : e.getAccountSide(),
                        Comparator.nullsLast((side1, side2) ->
                        {
                                if (side1 == side2)
                                {
                                        return 0;
                                }

                                if (side1 == null)
                                {
                                        return 1;
                                }

                                if (side2 == null)
                                {
                                        return -1;
                                }

                                // Debit rows should appear before credit rows for readability.
                                return side1 == AccountSide.DEBIT ? -1 : 1;
                        })).thenComparing(e -> accountName(company, e), Comparator.nullsLast(String::compareToIgnoreCase));
        }

        private static long transactionSortKey(AccountingTransaction tx)
        {
                if (tx == null)
                {
                        return Long.MAX_VALUE;
                }

                Long ts = tx.getBookingDateTimestamp();

                if (ts != null)
                {
                        return ts;
                }

                return tx.getId() == 0 ? Long.MAX_VALUE - 1L : tx.getId();
        }

        private static Object[] toRow(AccountingTransaction tx, AccountingEntry entry, Company company)
        {
                String date = formatDate(tx);
                String transactionId = String.valueOf(tx != null ? tx.getBookingDateTimestamp() : "-");
                String account = accountName(company, entry);
                String memo = tx != null ? defaultString(tx.getDescription()) : "";

                BigDecimal amount = entry != null ? entry.getAmount() : BigDecimal.ZERO;

                String debit = entry != null && entry.getAccountSide() == AccountSide.DEBIT
                        ? FormatUtils.formatCurrency(amount)
                        : "";
                String credit = entry != null && entry.getAccountSide() == AccountSide.CREDIT
                        ? FormatUtils.formatCurrency(amount)
                        : "";

                return new Object[]
                { date, transactionId, account, debit, credit, memo };
        }

        private static String formatDate(AccountingTransaction tx)
        {
                if (tx == null)
                {
                        return "";
                }

                if (tx.getDate() != null && !tx.getDate().isBlank())
                {
                        return tx.getDate();
                }

                Long timestamp = tx.getBookingDateTimestamp();

                if (timestamp == null)
                {
                        return "";
                }

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        .withLocale(Locale.US)
                        .withZone(ZoneId.systemDefault());
                return fmt.format(Instant.ofEpochMilli(timestamp));
        }

        private static String accountName(Company company, AccountingEntry entry)
        {
                if (entry == null)
                {
                        return "";
                }

                String name = entry.getAccountName();

                if (name != null && !name.isBlank())
                {
                        return name;
                }

                String number = entry.getAccountNumber();

                if (company != null && number != null && company.getChartOfAccounts() != null)
                {
                        Account account = company.getChartOfAccounts().getAccount(number);

                        if (account != null && account.getName() != null)
                        {
                                return account.getName();
                        }
                }

                return number != null ? number : "";
        }

        private static String defaultString(String value)
        {
                return value == null ? "" : value;
        }
}
