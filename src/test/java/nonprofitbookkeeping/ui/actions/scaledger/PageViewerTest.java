package nonprofitbookkeeping.ui.actions.scaledger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.table.DefaultTableModel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;


/**
 * The Class PageViewerTest.
 */
public class PageViewerTest
{

        /**
         * Reset.
         */
        @AfterEach
        public void reset()
        {
                CurrentCompany.forceCompanyLoad(null);
        }

        /**
         * Test model reflects current ledger.
         */
        @Test
        public void testModelReflectsCurrentLedger()
        {
                Company company = buildSampleCompany();
                CurrentCompany.forceCompanyLoad(company);

                DefaultTableModel model = PageViewer.getTableModel();

                assertEquals(6, model.getColumnCount());
                assertEquals("Date", model.getColumnName(0));
                assertEquals("Transaction", model.getColumnName(1));
                assertEquals("Account", model.getColumnName(2));
                assertEquals(2, model.getRowCount());
                assertEquals("2024-01-01", model.getValueAt(0, 0));
                assertEquals("Cash", model.getValueAt(0, 2));
                assertEquals("$50.00", model.getValueAt(0, 3));
                assertEquals("", model.getValueAt(0, 4));
                assertEquals("Service revenue", model.getValueAt(0, 5));

                assertEquals("2024-01-01", model.getValueAt(1, 0));
                assertEquals("Revenue", model.getValueAt(1, 2));
                assertEquals("", model.getValueAt(1, 3));
                assertEquals("$50.00", model.getValueAt(1, 4));
        }

        /**
         * Test model includes empty state when closed.
         */
        @Test
        public void testModelIncludesEmptyStateWhenClosed()
        {
                CurrentCompany.forceCompanyLoad(null);
                DefaultTableModel model = PageViewer.getTableModel();

                assertEquals(6, model.getColumnCount());
                assertEquals(1, model.getRowCount());
                assertEquals("No company is currently open.", model.getValueAt(0, 5));
        }

        /**
         * Builds the sample company.
         *
         * @return the company
         */
        private static Company buildSampleCompany()
        {
                Company company = new Company();
                company.getCompanyProfile().setCompanyName("Demo Co");

                Account cash = new Account("100", "Cash", AccountType.ASSET, BigDecimal.ZERO);
                Account revenue = new Account("400", "Revenue", AccountType.INCOME, BigDecimal.ZERO);
                company.getChartOfAccounts().addAccount(cash);
                company.getChartOfAccounts().addAccount(revenue);

                Set<AccountingEntry> entries = new LinkedHashSet<>();
                entries.add(new AccountingEntry(new BigDecimal("50.00"), "100", AccountSide.DEBIT, "Cash"));
                entries.add(new AccountingEntry(new BigDecimal("50.00"), "400", AccountSide.CREDIT, "Revenue"));

                AccountingTransaction tx = new AccountingTransaction(new Account(), entries, null, 1L);
                tx.setDate("2024-01-01");
                tx.setDescription("Service revenue");

                company.getLedger().getJournal().addTransaction(tx);
                return company;
        }
}
