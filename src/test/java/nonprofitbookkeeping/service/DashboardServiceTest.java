package nonprofitbookkeeping.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import nonprofitbookkeeping.model.AccountingTransaction;

class DashboardServiceTest
{
    @Test
    void recentTransactionsSortByIdDescendingAndRespectLimit()
    {
        DashboardService service = new DashboardService();
        AccountingTransaction first = transaction(1, "2026-01-05");
        AccountingTransaction newestId = transaction(9, "2026-01-02");
        AccountingTransaction middle = transaction(4, "2026-01-04");

        List<AccountingTransaction> result = service.recentTransactions(
            List.of(first, newestId, middle), LocalDate.of(2026, 1, 31), 2);

        assertEquals(List.of(9, 4),
            result.stream().map(AccountingTransaction::getId).toList());
    }

    @Test
    void recentTransactionsExcludeRowsAfterAsOfDate()
    {
        DashboardService service = new DashboardService();
        AccountingTransaction visible = transaction(3, "2026-01-10");
        AccountingTransaction future = transaction(8, "2026-02-10");

        List<AccountingTransaction> result = service.recentTransactions(
            List.of(future, visible), LocalDate.of(2026, 1, 31), 10);

        assertEquals(List.of(3),
            result.stream().map(AccountingTransaction::getId).toList());
    }

    private static AccountingTransaction transaction(int id, String date)
    {
        AccountingTransaction transaction = new AccountingTransaction();
        transaction.setId(id);
        transaction.setDate(date);
        return transaction;
    }
}
