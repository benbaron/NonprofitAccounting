package org.nonprofitbookkeeping.ui;

import org.junit.jupiter.api.Test;
import org.nonprofitbookkeeping.service.FundBalanceRow;
import org.nonprofitbookkeeping.ui.MonthlyCloseChecklistService.ChecklistStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonthlyCloseChecklistServiceTest
{
    @Test
    void blocksUntilDatabaseAndCompanyAreOpen()
    {
        FakeGateway gateway = new FakeGateway(false, false);
        MonthlyCloseChecklistService service = new MonthlyCloseChecklistService(gateway);

        var state = service.calculate(LocalDate.of(2026, 5, 31));

        assertEquals(1, state.items().size());
        assertEquals(ChecklistStatus.BLOCKED, state.items().get(0).status());
        assertTrue(state.items().get(0).detail().contains("Open a database"));
    }

    @Test
    void usesServiceBackedCountsAndExplicitNotWiredStates()
    {
        FakeGateway gateway = new FakeGateway(true, true);
        gateway.accounts = List.of("Checking");
        gateway.undepositedCount = 2;
        gateway.fundBalances = List.of(new FundBalanceRow("GEN", "General", BigDecimal.TEN));
        gateway.reportCount = 5;
        MonthlyCloseChecklistService service = new MonthlyCloseChecklistService(gateway);

        var state = service.calculate(LocalDate.of(2026, 5, 31));

        assertEquals(7, state.items().size());
        assertEquals(ChecklistStatus.ACTION_REQUIRED, find(state, "Reconcile bank accounts").status());
        assertEquals(ChecklistStatus.ACTION_REQUIRED, find(state, "Review undeposited funds").status());
        assertEquals(ChecklistStatus.NOT_WIRED, find(state, "Resolve pending imports").status());
        assertEquals(ChecklistStatus.ACTION_REQUIRED, find(state, "Verify fund balances").status());
        assertEquals(ChecklistStatus.ACTION_REQUIRED, find(state, "Generate required reports").status());
        assertEquals(ChecklistStatus.ACTION_REQUIRED, find(state, "Export/backup database").status());
        assertEquals(ChecklistStatus.NOT_WIRED, find(state, "Lock/close period").status());
    }

    @Test
    void zeroUndepositedFundsIsCompleteButFundBalancesStillRequireReview()
    {
        FakeGateway gateway = new FakeGateway(true, true);
        gateway.accounts = List.of("Checking");
        gateway.undepositedCount = 0;
        gateway.fundBalances = List.of(new FundBalanceRow("GEN", "General", BigDecimal.ZERO));
        gateway.reportCount = 1;

        var state = new MonthlyCloseChecklistService(gateway).calculate(LocalDate.of(2026, 5, 31));

        assertEquals(ChecklistStatus.COMPLETE, find(state, "Review undeposited funds").status());
        assertEquals(ChecklistStatus.ACTION_REQUIRED, find(state, "Verify fund balances").status());
    }

    private static MonthlyCloseChecklistService.CloseChecklistItem find(MonthlyCloseChecklistService.CloseChecklistState state, String label)
    {
        return state.items().stream().filter(item -> item.label().equals(label)).findFirst().orElseThrow();
    }

    private static class FakeGateway implements MonthlyCloseChecklistService.CloseChecklistGateway
    {
        final boolean openDatabase;
        final boolean openCompany;
        List<String> accounts = List.of();
        int undepositedCount;
        List<FundBalanceRow> fundBalances = List.of();
        int reportCount;

        FakeGateway(boolean openDatabase, boolean openCompany)
        {
            this.openDatabase = openDatabase;
            this.openCompany = openCompany;
        }

        public boolean isDatabaseOpen() { return this.openDatabase; }
        public boolean isCompanyOpen() { return this.openCompany; }
        public List<String> reconcilableAccounts() { return this.accounts; }
        public int undepositedFundsCount() { return this.undepositedCount; }
        public List<FundBalanceRow> fundBalancesAsOf(LocalDate periodEnd) { return this.fundBalances; }
        public int reportCatalogCount() { return this.reportCount; }
    }
}
