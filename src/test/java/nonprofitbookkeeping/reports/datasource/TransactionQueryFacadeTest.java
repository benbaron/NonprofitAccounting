package nonprofitbookkeeping.reports.datasource;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.reports.jasper.query.TransactionQueryFacade;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionQueryFacadeTest
{
        @Test
        void filtersByTypeDateAccountAndMemo()
        {
                AccountingTransaction included = buildTransaction("2024-01-15", "Deposit memo",
                        Map.of("transactionType", "DEPOSIT"),
                        new AccountingEntry(new BigDecimal("10"), "100", AccountSide.DEBIT),
                        new AccountingEntry(new BigDecimal("10"), "200", AccountSide.CREDIT));

                AccountingTransaction outsideDate = buildTransaction("2023-12-31", "Deposit memo",
                        Map.of("transactionType", "DEPOSIT"),
                        new AccountingEntry(new BigDecimal("10"), "100", AccountSide.DEBIT),
                        new AccountingEntry(new BigDecimal("10"), "200", AccountSide.CREDIT));

                AccountingTransaction differentType = buildTransaction("2024-01-15", "Deposit memo",
                        Map.of("transactionType", "REFUND"),
                        new AccountingEntry(new BigDecimal("10"), "100", AccountSide.DEBIT),
                        new AccountingEntry(new BigDecimal("10"), "200", AccountSide.CREDIT));

                AccountingTransaction missingAccount = buildTransaction("2024-01-15", "Deposit memo",
                        Map.of("transactionType", "DEPOSIT"),
                        new AccountingEntry(new BigDecimal("10"), "300", AccountSide.DEBIT),
                        new AccountingEntry(new BigDecimal("10"), "200", AccountSide.CREDIT));

                TransactionQueryFacade query = new TransactionQueryFacade()
                        .filterByTransactionType("deposit")
                        .dateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))
                        .accounts(List.of("100", "200"), true)
                        .memoContains("deposit");

                List<AccountingTransaction> result = query.filterTransactions(
                        List.of(included, outsideDate, differentType, missingAccount));

                assertEquals(1, result.size());
                assertEquals(included, result.get(0));
        }

        @Test
        void mapsToBeans()
        {
                AccountingTransaction tx = buildTransaction("2024-02-02", "Rent",
                        Map.of("type", "EXPENSE"),
                        new AccountingEntry(new BigDecimal("50"), "400", AccountSide.DEBIT),
                        new AccountingEntry(new BigDecimal("50"), "500", AccountSide.CREDIT));

                TransactionQueryFacade query = new TransactionQueryFacade()
                        .filterByTransactionType("expense")
                        .accounts(List.of("400"), false)
                        .memoContains("rent");

                List<String> mapped = query.mapToBeans(List.of(tx),
                        t -> t.getMemo() + "-" + t.getDate());

                assertEquals(List.of("Rent-2024-02-02"), mapped);
        }

        @SafeVarargs
        private AccountingTransaction buildTransaction(String date, String memo,
                Map<String, String> info, AccountingEntry... entries)
        {
                AccountingTransaction tx = new AccountingTransaction();
                tx.setInfo(info);
                tx.setDate(date);
                tx.setMemo(memo);
                tx.setBookingDateTimestamp(LocalDate.parse(date)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli());

                for (AccountingEntry entry : entries)
                {
                        tx.addEntry(entry);
                }

                return tx;
        }
}
