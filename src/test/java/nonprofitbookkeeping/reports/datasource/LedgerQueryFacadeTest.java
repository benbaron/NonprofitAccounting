package nonprofitbookkeeping.reports.datasource;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LedgerQueryFacadeTest
{
        private final LedgerQueryFacade facade = new LedgerQueryFacade();

        @Test
        void filtersByDateRangeAndMemo()
        {
                AccountingTransaction jan = txn("2024-01-01", "membership");
                AccountingTransaction feb = txn("2024-02-01", "Rent paid");
                AccountingTransaction mar = txn("2024-03-15", "rent accrual");

                LedgerQueryCriteria criteria = LedgerQueryCriteria.builder()
                        .startDate(LocalDate.of(2024, 2, 1))
                        .endDate(LocalDate.of(2024, 3, 31))
                        .memoContains("rent")
                        .build();

                List<String> memos = this.facade.queryFromTransactions(List.of(jan, feb, mar),
                        criteria,
                        AccountingTransaction::getMemo);

                assertEquals(List.of("Rent paid", "rent accrual"), memos);
        }

        @Test
        void filtersByAccountMembership()
        {
                AccountingTransaction primaryOnly = txn("2024-04-01", "memo");
                primaryOnly.setEntries(new LinkedHashSet<>(List.of(
                        new AccountingEntry(BigDecimal.ONE, "1000", AccountSide.DEBIT))));

                AccountingTransaction bothAccounts = txn("2024-04-02", "memo");
                bothAccounts.setEntries(new LinkedHashSet<>(Arrays.asList(
                        new AccountingEntry(BigDecimal.ONE, "1000", AccountSide.DEBIT),
                        new AccountingEntry(BigDecimal.ONE, "2000", AccountSide.CREDIT))));

                LedgerQueryCriteria requireAll = LedgerQueryCriteria.builder()
                        .addAccountNumber("1000")
                        .addAccountNumber("2000")
                        .requireAllAccounts(true)
                        .build();

                List<String> dates = this.facade.queryFromTransactions(List.of(primaryOnly, bothAccounts),
                        requireAll,
                        AccountingTransaction::getDate);

                assertEquals(List.of("2024-04-02"), dates);

                LedgerQueryCriteria any = LedgerQueryCriteria.builder()
                        .addAccountNumber("2000")
                        .requireAllAccounts(false)
                        .build();

                List<String> anyResult = this.facade.queryFromTransactions(List.of(primaryOnly, bothAccounts),
                        any,
                        AccountingTransaction::getDate);

                assertEquals(List.of("2024-04-02"), anyResult);
        }

        @Test
        void respectsTransactionSideAndCustomPredicate()
        {
                AccountingTransaction debit = txn("2024-05-01", "debit");
                AccountingTransaction credit = txn("2024-05-02", "credit");
                credit.setEntries(new LinkedHashSet<>(List.of(
                        new AccountingEntry(BigDecimal.ONE, "3000", AccountSide.CREDIT))));
                credit.setCheckNumber("ABC123");

                LedgerQueryCriteria criteria = LedgerQueryCriteria.builder()
                        .transactionSide(AccountSide.CREDIT)
                        .addPredicate(txn -> txn.getCheckNumber() != null && txn.getCheckNumber().startsWith("ABC"))
                        .build();

                List<String> results = this.facade.queryFromTransactions(List.of(debit, credit),
                        criteria,
                        AccountingTransaction::getMemo);

                assertEquals(List.of("credit"), results);
                assertTrue(results.get(0).contains("credit"));
        }

        private AccountingTransaction txn(String date, String memo)
        {
                AccountingTransaction tx = new AccountingTransaction();
                tx.setBookingDateTimestamp(LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
                tx.setDate(date);
                tx.setMemo(memo);
                tx.setEntries(new LinkedHashSet<>(List.of(
                        new AccountingEntry(BigDecimal.TEN, "1000", AccountSide.DEBIT))));
                return tx;
        }
}
