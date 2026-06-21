package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.AccountingTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlternateReconciliationServiceTest
{
    @Test
    void calculatesDifferenceFromBeginningEndingAndClearedRows()
    {
        AlternateReconciliationService service = new AlternateReconciliationService(new FakeGateway());
        var rows = List.of(
            new AlternateReconciliationService.ReconciliationRow(1L, "2026-01-01", "Deposit", new BigDecimal("25.50")),
            new AlternateReconciliationService.ReconciliationRow(2L, "2026-01-02", "Check", new BigDecimal("-5.25")));
        rows.get(0).setCleared(true);
        rows.get(1).setCleared(true);

        var summary = service.summarize(new BigDecimal("100.00"), new BigDecimal("120.25"), rows);

        assertEquals(new BigDecimal("20.25"), summary.clearedTotal());
        assertEquals(new BigDecimal("0.00"), summary.difference());
    }

    @Test
    void rowUsesBooleanPropertyForCheckboxBinding()
    {
        var row = new AlternateReconciliationService.ReconciliationRow(1L, "2026-01-01", "Deposit", BigDecimal.TEN);
        row.clearedProperty().set(true);
        assertTrue(row.isCleared());
        row.setCleared(false);
        assertFalse(row.clearedProperty().get());
    }

    @Test
    void invalidEndingBalanceIsValidationErrorNotZero()
    {
        AlternateReconciliationService service = new AlternateReconciliationService(new FakeGateway());
        var parsed = service.parseEndingBalance("not-a-number");
        assertFalse(parsed.valid());
        assertNull(parsed.balance());
        assertTrue(parsed.message().contains("valid number"));
    }

    @Test
    void saveBlocksNonzeroDifferenceAndDoesNotCallGateway()
    {
        FakeGateway gateway = new FakeGateway();
        AlternateReconciliationService service = new AlternateReconciliationService(gateway);
        var row = new AlternateReconciliationService.ReconciliationRow(7L, "2026-01-01", "Deposit", new BigDecimal("10.00"));
        row.setCleared(true);

        SaveResult result = service.save("1000", LocalDate.of(2026, 1, 31), BigDecimal.ZERO, "15.00", List.of(row));

        assertEquals(SaveResult.Status.FAILED, result.status());
        assertTrue(result.message().contains("Cannot save reconciliation"));
        assertTrue(gateway.clearedIds.isEmpty());
    }

    @Test
    void savePassesSelectedRowsWhenDifferenceIsZero()
    {
        FakeGateway gateway = new FakeGateway();
        AlternateReconciliationService service = new AlternateReconciliationService(gateway);
        var selected = new AlternateReconciliationService.ReconciliationRow(7L, "2026-01-01", "Deposit", new BigDecimal("10.00"));
        var unselected = new AlternateReconciliationService.ReconciliationRow(8L, "2026-01-02", "Fee", new BigDecimal("2.00"));
        selected.setCleared(true);

        SaveResult result = service.save("1000", LocalDate.of(2026, 1, 31), BigDecimal.ZERO, "10.00", List.of(selected, unselected));

        assertEquals(SaveResult.Status.SAVED, result.status());
        assertEquals("1000", gateway.account);
        assertEquals("2026-01-31", gateway.statementDate);
        assertEquals(new BigDecimal("10.00"), gateway.endingBalance);
        assertEquals(List.of(7L), gateway.clearedIds);
    }

    private static class FakeGateway implements AlternateReconciliationService.ReconciliationGateway
    {
        String account;
        String statementDate;
        BigDecimal endingBalance;
        List<Long> clearedIds = new ArrayList<>();

        public List<String> listAccounts() { return List.of("1000"); }
        public List<AccountingTransaction> loadTransactions(String account) { return List.of(); }
        public void reconcile(String account, String statementDate, BigDecimal endingBalance, List<Long> clearedIds)
        {
            this.account = account;
            this.statementDate = statementDate;
            this.endingBalance = endingBalance;
            this.clearedIds = new ArrayList<>(clearedIds);
        }
    }
}
