package nonprofitbookkeeping.model.ofx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;

/**
 * Tests for {@link InvestmentTransaction} helper methods.
 */
class InvestmentTransactionTest
{

        @AfterEach
        void resetCompany()
        {
                CurrentCompany.forceCompanyLoad(null);
        }

        @Test
        void getTotalSumsLedgerEntriesRespectingAccountSide()
        {
                Company company = new Company();

                Account investment = new Account("200", "Investments", AccountSide.DEBIT);
                Account offset = new Account("999", "Offset", AccountSide.CREDIT);

                company.getChartOfAccounts().addAccount(investment);
                company.getChartOfAccounts().addAccount(offset);

                // First transaction increases the balance.
                Set<AccountingEntry> firstEntries = new LinkedHashSet<>();
                firstEntries.add(new AccountingEntry(new BigDecimal("100.00"), investment.getAccountNumber(),
                        AccountSide.DEBIT, investment.getName()));
                firstEntries.add(new AccountingEntry(new BigDecimal("100.00"), offset.getAccountNumber(),
                        AccountSide.CREDIT, offset.getName()));

                AccountingTransaction first = new AccountingTransaction();
                first.setEntries(firstEntries);
                first.setBookingDateTimestamp(1L);
                company.getLedger().getJournal().addTransaction(first);

                // Second transaction decreases the balance.
                Set<AccountingEntry> secondEntries = new LinkedHashSet<>();
                secondEntries.add(new AccountingEntry(new BigDecimal("40.00"), investment.getAccountNumber(),
                        AccountSide.CREDIT, investment.getName()));
                secondEntries.add(new AccountingEntry(new BigDecimal("40.00"), offset.getAccountNumber(),
                        AccountSide.DEBIT, offset.getName()));

                AccountingTransaction second = new AccountingTransaction();
                second.setEntries(secondEntries);
                second.setBookingDateTimestamp(2L);
                company.getLedger().getJournal().addTransaction(second);

                CurrentCompany.forceCompanyLoad(company);

                assertEquals(new BigDecimal("60.00"), InvestmentTransaction.getTotal(investment));
        }
}
