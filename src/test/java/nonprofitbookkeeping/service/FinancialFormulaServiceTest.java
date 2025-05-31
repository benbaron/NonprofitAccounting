package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.AccountingTransaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Initialize mocks
class FinancialFormulaServiceTest {

    @Mock
    private Ledger mockLedger; // Mocked Ledger

    @Mock
    private AccountingTransaction mockTxn1; // Mocked Transaction 1

    @Mock
    private AccountingTransaction mockTxn2; // Mocked Transaction 2

    @Mock
    private AccountingTransaction mockTxn3; // Mocked Transaction 3


    @Test
    @DisplayName("applyFormulas: Null Ledger should return 0.0")
    void testApplyFormulas_nullLedger_returnsZero() {
        double result = FinancialFormulaService.applyFormulas(null);
        assertEquals(0.0, result, "Result should be 0.0 for a null ledger.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with null transactions list should return 0.0")
    void testApplyFormulas_ledgerWithNullTransactions_returnsZero() {
        when(this.mockLedger.getTransactions()).thenReturn(null);
        double result = FinancialFormulaService.applyFormulas(this.mockLedger);
        assertEquals(0.0, result, "Result should be 0.0 if transactions list is null.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with empty transactions list should return 0.0")
    void testApplyFormulas_ledgerWithEmptyTransactions_returnsZero() {
        when(this.mockLedger.getTransactions()).thenReturn(Collections.emptyList());
        double result = FinancialFormulaService.applyFormulas(this.mockLedger);
        assertEquals(0.0, result, "Result should be 0.0 if transactions list is empty.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with one transaction should return its amount")
    void testApplyFormulas_ledgerWithSingleTransaction_returnsCorrectSum() {
        when(this.mockTxn1.getTotalAmount()).thenReturn(new BigDecimal("123.45"));
        when(this.mockLedger.getTransactions()).thenReturn(List.of(this.mockTxn1));

        double result = FinancialFormulaService.applyFormulas(this.mockLedger);
        assertEquals(123.45, result, 0.001, "Result should be the amount of the single transaction.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with multiple transactions should return their sum")
    void testApplyFormulas_ledgerWithMultipleTransactions_returnsCorrectSum() {
        when(this.mockTxn1.getTotalAmount()).thenReturn(new BigDecimal("100.50"));
        when(this.mockTxn2.getTotalAmount()).thenReturn(new BigDecimal("50.25"));
        when(this.mockLedger.getTransactions()).thenReturn(Arrays.asList(this.mockTxn1, this.mockTxn2));

        double result = FinancialFormulaService.applyFormulas(this.mockLedger);
        assertEquals(150.75, result, 0.001, "Result should be the sum of all transaction amounts.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with transactions having null amounts should ignore nulls")
    void testApplyFormulas_ledgerWithTransactionReturningNullAmount_ignoresNullAmount() {
        when(this.mockTxn1.getTotalAmount()).thenReturn(new BigDecimal("75.00"));
        when(this.mockTxn2.getTotalAmount()).thenReturn(null); // This transaction's amount is null
        when(this.mockTxn3.getTotalAmount()).thenReturn(new BigDecimal("25.00"));
        when(this.mockLedger.getTransactions()).thenReturn(Arrays.asList(this.mockTxn1, this.mockTxn2, this.mockTxn3));

        double result = FinancialFormulaService.applyFormulas(this.mockLedger);
        assertEquals(100.00, result, 0.001, "Result should sum non-null amounts, ignoring nulls.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with a mix of null and non-null transaction objects should skip nulls")
    void testApplyFormulas_ledgerWithNullTransactionInList_skipsNullTransaction() {
        when(this.mockTxn1.getTotalAmount()).thenReturn(new BigDecimal("200.00"));
        // mockTxn2 is not stubbed for getTotalAmount as it won't be called if the object is null in list
        when(this.mockTxn3.getTotalAmount()).thenReturn(new BigDecimal("50.00"));
        // The list itself contains a null object
        when(this.mockLedger.getTransactions()).thenReturn(Arrays.asList(this.mockTxn1, null, this.mockTxn3));

        double result = FinancialFormulaService.applyFormulas(this.mockLedger);
        assertEquals(250.00, result, 0.001, "Result should sum amounts from non-null transactions, skipping null transaction objects.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with all transaction amounts being null")
    void testApplyFormulas_allTransactionAmountsNull() {
        when(this.mockTxn1.getTotalAmount()).thenReturn(null);
        when(this.mockTxn2.getTotalAmount()).thenReturn(null);
        when(this.mockLedger.getTransactions()).thenReturn(Arrays.asList(this.mockTxn1, this.mockTxn2));

        double result = FinancialFormulaService.applyFormulas(this.mockLedger);
        assertEquals(0.0, result, 0.001, "Result should be 0.0 if all transaction amounts are null.");
    }

    @Test
    @DisplayName("applyFormulas: Ledger with a transaction amount of zero")
    void testApplyFormulas_transactionAmountZero() {
        when(this.mockTxn1.getTotalAmount()).thenReturn(new BigDecimal("10.00"));
        when(this.mockTxn2.getTotalAmount()).thenReturn(BigDecimal.ZERO);
        when(this.mockTxn3.getTotalAmount()).thenReturn(new BigDecimal("5.00"));
        when(this.mockLedger.getTransactions()).thenReturn(Arrays.asList(this.mockTxn1, this.mockTxn2, this.mockTxn3));

        double result = FinancialFormulaService.applyFormulas(this.mockLedger);
        assertEquals(15.00, result, 0.001, "Result should correctly include transactions with zero amount.");
    }
}
