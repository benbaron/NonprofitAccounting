package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.Ledger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancialFormulaServiceTest {

    private static class StubTransaction extends AccountingTransaction
    {
        private final BigDecimal amount;

        StubTransaction(BigDecimal amount)
        {
            this.amount = amount;
        }

        @Override
        public BigDecimal getTotalAmount()
        {
            return this.amount;
        }
    }

    private Ledger ledgerWithTransactions(List<AccountingTransaction> transactions)
    {
        Ledger ledger = new Ledger();
        ledger.getJournal().replaceAllTransactions(transactions);
        return ledger;
    }

    private AccountingTransaction transactionWithAmount(BigDecimal amount)
    {
        AccountingTransaction tx = new AccountingTransaction();
        Set<AccountingEntry> entries = new LinkedHashSet<>();
        entries.add(new AccountingEntry(amount, "1000", AccountSide.DEBIT));
        tx.setEntries(entries);
        return tx;
    }


    @Test
    @DisplayName("applyFormulas: Null Ledger should return 0.0")
    void testApplyFormulas_nullLedger_returnsZero() {
        double result = FinancialFormulaService.applyFormulas(null);
        assertEquals(0.0, result, "Result should be 0.0 for a null ledger.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with null transactions list should return 0.0")
    void testApplyFormulas_ledgerWithNullTransactions_returnsZero() {
        Ledger ledger = ledgerWithTransactions(Collections.emptyList());
        double result = FinancialFormulaService.applyFormulas(ledger);
        assertEquals(0.0, result, "Result should be 0.0 if transactions list is empty.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with empty transactions list should return 0.0")
    void testApplyFormulas_ledgerWithEmptyTransactions_returnsZero() {
        Ledger ledger = ledgerWithTransactions(Collections.emptyList());
        double result = FinancialFormulaService.applyFormulas(ledger);
        assertEquals(0.0, result, "Result should be 0.0 if transactions list is empty.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with one transaction should return its amount")
    void testApplyFormulas_ledgerWithSingleTransaction_returnsCorrectSum() {
        Ledger ledger = ledgerWithTransactions(List.of(transactionWithAmount(new BigDecimal("123.45"))));
        double result = FinancialFormulaService.applyFormulas(ledger);
        assertEquals(123.45, result, 0.001, "Result should be the amount of the single transaction.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with multiple transactions should return their sum")
    void testApplyFormulas_ledgerWithMultipleTransactions_returnsCorrectSum() {
        Ledger ledger = ledgerWithTransactions(Arrays.asList(
            transactionWithAmount(new BigDecimal("100.50")),
            transactionWithAmount(new BigDecimal("50.25"))));
        double result = FinancialFormulaService.applyFormulas(ledger);
        assertEquals(150.75, result, 0.001, "Result should be the sum of all transaction amounts.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with transactions having null amounts should ignore nulls")
    void testApplyFormulas_ledgerWithTransactionReturningNullAmount_ignoresNullAmount() {
        Ledger ledger = ledgerWithTransactions(Arrays.asList(
            new StubTransaction(new BigDecimal("75.00")),
            new StubTransaction(null),
            new StubTransaction(new BigDecimal("25.00"))));
        double result = FinancialFormulaService.applyFormulas(ledger);
        assertEquals(100.00, result, 0.001, "Result should sum non-null amounts, ignoring nulls.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with a mix of null and non-null transaction objects should skip nulls")
    void testApplyFormulas_ledgerWithNullTransactionInList_skipsNullTransaction() {
        Ledger ledger = ledgerWithTransactions(Arrays.asList(
            new StubTransaction(new BigDecimal("200.00")),
            null,
            new StubTransaction(new BigDecimal("50.00"))));
        double result = FinancialFormulaService.applyFormulas(ledger);
        assertEquals(250.00, result, 0.001, "Result should sum amounts from non-null transactions, skipping null transaction objects.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with all transaction amounts being null")
    void testApplyFormulas_allTransactionAmountsNull() {
        Ledger ledger = ledgerWithTransactions(Arrays.asList(
            new StubTransaction(null),
            new StubTransaction(null)));
        double result = FinancialFormulaService.applyFormulas(ledger);
        assertEquals(0.0, result, 0.001, "Result should be 0.0 if all transaction amounts are null.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with a transaction amount of zero")
    void testApplyFormulas_transactionAmountZero() {
        Ledger ledger = ledgerWithTransactions(Arrays.asList(
            new StubTransaction(new BigDecimal("10.00")),
            new StubTransaction(BigDecimal.ZERO),
            new StubTransaction(new BigDecimal("5.00"))));
        double result = FinancialFormulaService.applyFormulas(ledger);
        assertEquals(15.00, result, 0.001, "Result should correctly include transactions with zero amount.");
    }
}
