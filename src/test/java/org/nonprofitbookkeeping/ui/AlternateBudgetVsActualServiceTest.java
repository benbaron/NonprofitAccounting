package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.records.BudgetRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlternateBudgetVsActualServiceTest
{
    @Test
    void calculatesBudgetActualAndVarianceFromFixturesWithoutDemoData()
    {
        Company company = new Company();
        addTransaction(company, "2026-02-15", "5000", AccountSide.DEBIT, "General", "75.25");
        addTransaction(company, "2025-12-31", "5000", AccountSide.DEBIT, "General", "99.00");
        BudgetRecord budget = new BudgetRecord("budget-2026", "Operating", 2026, "General", true, "fixture",
            List.of(new BudgetRecord.BudgetLineRecord(null, new BigDecimal("100.00"), null, null, "5000", null, Map.of())),
            Map.of(), null);
        AlternateBudgetVsActualService service = new AlternateBudgetVsActualService(() -> company, () -> true, () -> List.of(budget));

        List<AlternateBudgetVsActualService.BudgetVsActualRow> rows = service.calculate(
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "General");

        assertEquals(1, rows.size());
        assertEquals("5000", rows.get(0).accountId());
        assertEquals(new BigDecimal("100.00"), rows.get(0).budget());
        assertEquals(new BigDecimal("75.25"), rows.get(0).actual());
        assertEquals(new BigDecimal("24.75"), rows.get(0).variance());
    }

    private static void addTransaction(Company company, String date, String account, AccountSide side, String fund, String amount)
    {
        AccountingEntry entry = new AccountingEntry(new BigDecimal(amount), account, side, "Expense");
        entry.setFundNumber(fund);
        AccountingEntry offset = new AccountingEntry(new BigDecimal(amount), "1000", side == AccountSide.DEBIT ? AccountSide.CREDIT : AccountSide.DEBIT, "Cash");
        offset.setFundNumber(fund);
        AccountingTransaction tx = new AccountingTransaction();
        tx.setDate(date);
        tx.setBookingDateTimestamp(System.nanoTime());
        tx.setEntries(new LinkedHashSet<>(List.of(entry, offset)));
        company.getLedger().getJournal().addTransaction(tx);
    }
}
