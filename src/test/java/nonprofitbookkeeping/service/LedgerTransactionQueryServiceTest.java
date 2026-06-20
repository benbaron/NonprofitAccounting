package nonprofitbookkeeping.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.service.LedgerTransactionQueryService.LedgerTransactionFilter;

class LedgerTransactionQueryServiceTest
{
    @Test
    void accountFilterUsesSelectedAccountEntryAmountForSplitTransaction()
    {
        Company company = new Company();
        ChartOfAccounts chart = new ChartOfAccounts();
        chart.addAccount(new Account("1000", "Checking", AccountType.CHECKING, BigDecimal.ZERO));
        chart.addAccount(new Account("5000", "Supplies", AccountType.EXPENSE, BigDecimal.ZERO));
        chart.addAccount(new Account("5100", "Postage", AccountType.EXPENSE, BigDecimal.ZERO));
        company.setChartOfAccounts(chart);

        AccountingEntry checking = new AccountingEntry(new BigDecimal("100.00"), "1000", AccountSide.CREDIT, "Checking");
        AccountingEntry supplies = new AccountingEntry(new BigDecimal("60.00"), "5000", AccountSide.DEBIT, "Supplies");
        AccountingEntry postage = new AccountingEntry(new BigDecimal("40.00"), "5100", AccountSide.DEBIT, "Postage");
        AccountingTransaction transaction = new AccountingTransaction();
        transaction.setId(7);
        transaction.setDate("2026-06-20");
        transaction.setMemo("Split office purchase");
        transaction.setToFrom("Office Vendor");
        transaction.setEntries(new LinkedHashSet<>(List.of(checking, supplies, postage)));
        transaction.setBookingDateTimestamp(7L);
        company.getLedger().getJournal().addTransaction(transaction);

        LedgerTransactionQueryService service = new LedgerTransactionQueryService(() -> company, () -> true);

        var rows = service.query(new LedgerTransactionFilter(null, null, "Supplies", null, null, null, null, null));

        assertEquals(1, rows.size());
        assertEquals(0, new BigDecimal("60.00").compareTo(rows.get(0).displayAmount()));
        assertEquals(1, rows.get(0).lines().size());
        assertEquals(0, new BigDecimal("60.00").compareTo(rows.get(0).lines().get(0).debit()));
    }
}
