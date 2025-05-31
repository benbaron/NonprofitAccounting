package nonprofitbookkeeping.service;

import nonprofitbookkeeping.api.TrialBalanceResultIntf;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Ledger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrialBalanceServiceTest {

    @Mock
    private Ledger mockLedger;
    @Mock
    private AccountingTransaction mockTx1, mockTx2, mockTx3;
    @Mock
    private AccountingEntry mockEntry1, mockEntry2, mockEntry3, mockEntry4;
    @Mock
    private TrialBalanceResultIntf mockTrialBalanceResultFromStatic;

    private final LocalDate TEST_FROM_DATE = LocalDate.of(2023, 1, 1);
    private final LocalDate TEST_TO_DATE = LocalDate.of(2023, 1, 31);
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    // --- Tests for static compute(...) method ---

    @Test
    @DisplayName("compute: Null Ledger should return empty, balanced result")
    void testCompute_nullLedger_returnsEmptyBalancedResult() {
        TrialBalanceResultIntf result = TrialBalanceService.compute(null, this.TEST_FROM_DATE, this.TEST_TO_DATE);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Ledger with null transactions list should return empty, balanced result")
    void testCompute_ledgerWithNullTransactions_returnsEmptyBalancedResult() {
        when(this.mockLedger.getTransactions()).thenReturn(null);
        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Ledger with empty transactions list should return empty, balanced result")
    void testCompute_ledgerWithEmptyTransactions_returnsEmptyBalancedResult() {
        when(this.mockLedger.getTransactions()).thenReturn(Collections.emptyList());
        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Null 'from' date should return empty, balanced result")
    void testCompute_nullFromDate_returnsEmptyBalancedResult() {
        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, null, this.TEST_TO_DATE);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Null 'to' date should return empty, balanced result")
    void testCompute_nullToDate_returnsEmptyBalancedResult() {
        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, null);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: 'from' date after 'to' date should return empty, balanced result")
    void testCompute_fromAfterToDate_returnsEmptyBalancedResult() {
        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, this.TEST_TO_DATE, this.TEST_FROM_DATE);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Filters transactions based on date range")
    void testCompute_filtersTransactionsBasedOnDateRange() {
        when(this.mockTx1.getDate()).thenReturn(this.TEST_FROM_DATE.minusDays(1).format(this.DATE_FORMATTER)); // Before range
        when(this.mockTx1.getEntries()).thenReturn(Set.of(this.mockEntry1)); // Avoid NPE if processed
        lenient().when(this.mockEntry1.getAccountNumber()).thenReturn("ACC1");
        lenient().when(this.mockEntry1.getAmount()).thenReturn(BigDecimal.TEN);
        lenient().when(this.mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);


        when(this.mockTx2.getDate()).thenReturn(this.TEST_FROM_DATE.format(this.DATE_FORMATTER)); // Inside range
        when(this.mockTx2.getEntries()).thenReturn(Set.of(this.mockEntry2));
        when(this.mockEntry2.getAccountNumber()).thenReturn("ACC1");
        when(this.mockEntry2.getAmount()).thenReturn(BigDecimal.valueOf(100));
        when(this.mockEntry2.getAccountSide()).thenReturn(AccountSide.DEBIT);

        when(this.mockTx3.getDate()).thenReturn(this.TEST_TO_DATE.plusDays(1).format(this.DATE_FORMATTER)); // After range
        when(this.mockTx3.getEntries()).thenReturn(Set.of(this.mockEntry3)); // Avoid NPE if processed
        lenient().when(this.mockEntry3.getAccountNumber()).thenReturn("ACC1");
        lenient().when(this.mockEntry3.getAmount()).thenReturn(BigDecimal.ONE);
        lenient().when(this.mockEntry3.getAccountSide()).thenReturn(AccountSide.DEBIT);


        when(this.mockLedger.getTransactions()).thenReturn(Arrays.asList(this.mockTx1, this.mockTx2, this.mockTx3));
        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);

        assertEquals(1, result.getDebitSums().size());
        assertEquals(100.0, result.getDebitSums().get("ACC1"), 0.001);
        assertTrue(result.getCreditSums().isEmpty());
        assertFalse(result.isBalanced()); // 100 debit, 0 credit
    }

    @Test
    @DisplayName("compute: Skips transaction with unparseable date")
    void testCompute_skipsTransactionWithUnparseableDate() {
        when(this.mockTx1.getDate()).thenReturn("INVALID-DATE-FORMAT");
        // No need to stub getEntries for mockTx1 as it should be skipped before that.

        when(this.mockTx2.getDate()).thenReturn(this.TEST_FROM_DATE.format(this.DATE_FORMATTER));
        when(this.mockTx2.getEntries()).thenReturn(Set.of(this.mockEntry1));
        when(this.mockEntry1.getAccountNumber()).thenReturn("ACC1");
        when(this.mockEntry1.getAmount()).thenReturn(BigDecimal.valueOf(50));
        when(this.mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);

        when(this.mockLedger.getTransactions()).thenReturn(Arrays.asList(this.mockTx1, this.mockTx2));
        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);

        assertEquals(1, result.getDebitSums().size());
        assertEquals(50.0, result.getDebitSums().get("ACC1"), 0.001);
    }

    @Test
    @DisplayName("compute: Correctly sums debits and credits, balanced")
    void testCompute_multipleEntries_balanced_correctSumsAndBalanceTrue() {
        when(this.mockTx1.getDate()).thenReturn(this.TEST_FROM_DATE.format(this.DATE_FORMATTER));
        when(this.mockEntry1.getAccountNumber()).thenReturn("ACC_DEBIT");
        when(this.mockEntry1.getAmount()).thenReturn(new BigDecimal("100.00"));
        when(this.mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);
        when(this.mockEntry2.getAccountNumber()).thenReturn("ACC_CREDIT");
        when(this.mockEntry2.getAmount()).thenReturn(new BigDecimal("100.00"));
        when(this.mockEntry2.getAccountSide()).thenReturn(AccountSide.CREDIT);
        when(this.mockTx1.getEntries()).thenReturn(Set.of(this.mockEntry1, this.mockEntry2));

        when(this.mockLedger.getTransactions()).thenReturn(List.of(this.mockTx1));
        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);

        assertEquals(100.0, result.getDebitSums().get("ACC_DEBIT"), 0.001);
        assertEquals(100.0, result.getCreditSums().get("ACC_CREDIT"), 0.001);
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Correctly sums debits and credits, unbalanced")
    void testCompute_multipleEntries_unbalanced_correctSumsAndBalanceFalse() {
        when(this.mockTx1.getDate()).thenReturn(this.TEST_FROM_DATE.format(this.DATE_FORMATTER));
        when(this.mockEntry1.getAccountNumber()).thenReturn("ACC_DEBIT");
        when(this.mockEntry1.getAmount()).thenReturn(new BigDecimal("100.00"));
        when(this.mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);
        when(this.mockEntry2.getAccountNumber()).thenReturn("ACC_CREDIT");
        when(this.mockEntry2.getAmount()).thenReturn(new BigDecimal("90.00")); // Unbalanced
        when(this.mockEntry2.getAccountSide()).thenReturn(AccountSide.CREDIT);
        when(this.mockTx1.getEntries()).thenReturn(Set.of(this.mockEntry1, this.mockEntry2));

        when(this.mockLedger.getTransactions()).thenReturn(List.of(this.mockTx1));
        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);

        assertEquals(100.0, result.getDebitSums().get("ACC_DEBIT"), 0.001);
        assertEquals(90.0, result.getCreditSums().get("ACC_CREDIT"), 0.001);
        assertFalse(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Skips null transaction in list")
    void testCompute_skipsNullTransactionInList() {
        when(this.mockTx1.getDate()).thenReturn(this.TEST_FROM_DATE.format(this.DATE_FORMATTER));
        when(this.mockTx1.getEntries()).thenReturn(Set.of(this.mockEntry1));
        when(this.mockEntry1.getAccountNumber()).thenReturn("ACC1");
        when(this.mockEntry1.getAmount()).thenReturn(BigDecimal.TEN);
        when(this.mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);

        when(this.mockLedger.getTransactions()).thenReturn(Arrays.asList(this.mockTx1, null, this.mockTx2));
        // mockTx2 is not fully stubbed, but it won't matter if null tx is skipped before processing tx2.
        // For safety, ensure tx2 doesn't contribute if it were processed (e.g. by date or no entries)
        lenient().when(this.mockTx2.getDate()).thenReturn(this.TEST_FROM_DATE.minusDays(1).format(this.DATE_FORMATTER)); // out of range

        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);
        assertEquals(1, result.getDebitSums().size());
        assertEquals(10.0, result.getDebitSums().get("ACC1"), 0.001);
    }

    @Test
    @DisplayName("compute: Skips transaction with null entries set")
    void testCompute_skipsTransactionWithNullEntries() {
        when(this.mockTx1.getDate()).thenReturn(this.TEST_FROM_DATE.format(this.DATE_FORMATTER));
        when(this.mockTx1.getEntries()).thenReturn(null); // Null entries

        when(this.mockLedger.getTransactions()).thenReturn(List.of(this.mockTx1));
        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Skips null entry in transaction's entry set")
    void testCompute_skipsNullEntryInTransaction() {
        when(this.mockTx1.getDate()).thenReturn(this.TEST_FROM_DATE.format(this.DATE_FORMATTER));
        when(this.mockEntry1.getAccountNumber()).thenReturn("ACC1");
        when(this.mockEntry1.getAmount()).thenReturn(BigDecimal.TEN);
        when(this.mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);
        // Create a Set that allows null, though Set.of doesn't.
        Set<AccountingEntry> entriesWithNull = new java.util.HashSet<>();
        entriesWithNull.add(this.mockEntry1);
        entriesWithNull.add(null);
        when(this.mockTx1.getEntries()).thenReturn(entriesWithNull);

        when(this.mockLedger.getTransactions()).thenReturn(List.of(this.mockTx1));
        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);
        assertEquals(1, result.getDebitSums().size());
        assertEquals(10.0, result.getDebitSums().get("ACC1"), 0.001);
    }

    @Test
    @DisplayName("compute: Skips entry with null amount")
    void testCompute_skipsEntryWithNullAmount() {
        when(this.mockTx1.getDate()).thenReturn(this.TEST_FROM_DATE.format(this.DATE_FORMATTER));
        when(this.mockEntry1.getAccountNumber()).thenReturn("ACC1");
        when(this.mockEntry1.getAmount()).thenReturn(null); // Null amount
        when(this.mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);
        when(this.mockTx1.getEntries()).thenReturn(Set.of(this.mockEntry1));

        when(this.mockLedger.getTransactions()).thenReturn(List.of(this.mockTx1));
        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);
        assertTrue(result.getDebitSums().isEmpty());
    }

    @Test
    @DisplayName("compute: Skips entry with null or blank account number")
    void testCompute_skipsEntryWithNullOrBlankAccountNumber() {
        when(this.mockTx1.getDate()).thenReturn(this.TEST_FROM_DATE.format(this.DATE_FORMATTER));

        when(this.mockEntry1.getAccountNumber()).thenReturn(null); // Null account number
        when(this.mockEntry1.getAmount()).thenReturn(BigDecimal.TEN);
        when(this.mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);

        when(this.mockEntry2.getAccountNumber()).thenReturn("  "); // Blank account number
        when(this.mockEntry2.getAmount()).thenReturn(BigDecimal.valueOf(20));
        when(this.mockEntry2.getAccountSide()).thenReturn(AccountSide.DEBIT);

        when(this.mockEntry3.getAccountNumber()).thenReturn("ACC_VALID");
        when(this.mockEntry3.getAmount()).thenReturn(BigDecimal.valueOf(30));
        when(this.mockEntry3.getAccountSide()).thenReturn(AccountSide.DEBIT);

        when(this.mockTx1.getEntries()).thenReturn(Set.of(this.mockEntry1, this.mockEntry2, this.mockEntry3));
        when(this.mockLedger.getTransactions()).thenReturn(List.of(this.mockTx1));
        TrialBalanceResultIntf result = TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);

        assertEquals(1, result.getDebitSums().size());
        assertTrue(result.getDebitSums().containsKey("ACC_VALID"));
        assertEquals(30.0, result.getDebitSums().get("ACC_VALID"), 0.001);
    }


    // --- Tests for Constructor and Instance Methods ---
    @Test
    @DisplayName("Constructor: Calls static compute and stores its result")
    void testConstructor_callsStaticComputeAndStoresResult() {
        Map<String, Double> expectedDebits = Map.of("D1", 100.0);
        Map<String, Double> expectedCredits = Map.of("C1", 100.0);
        boolean expectedBalance = true;

        when(this.mockTrialBalanceResultFromStatic.getDebitSums()).thenReturn(expectedDebits);
        when(this.mockTrialBalanceResultFromStatic.getCreditSums()).thenReturn(expectedCredits);
        when(this.mockTrialBalanceResultFromStatic.isBalanced()).thenReturn(expectedBalance);

        try (MockedStatic<TrialBalanceService> mockedStaticService = mockStatic(TrialBalanceService.class)) {
            // Make the static compute method return our mocked result when called by the constructor
            mockedStaticService.when(() -> TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE))
                             .thenReturn(this.mockTrialBalanceResultFromStatic);

            TrialBalanceService serviceInstance = new TrialBalanceService(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);

            // Verify static compute was called by constructor
            mockedStaticService.verify(() -> TrialBalanceService.compute(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE));

            // Assert that instance methods return values from the mocked result
            assertEquals(expectedDebits, serviceInstance.getDebitSums());
            assertEquals(expectedCredits, serviceInstance.getCreditSums());
            assertEquals(expectedBalance, serviceInstance.isBalanced());
        }
    }

    @Test
    @DisplayName("Instance getDebitSums: Returns data from result held by instance")
    void testGetDebitSums_returnsDataFromComputedResult() {
        Map<String, Double> expectedDebits = Map.of("ACC_D", 250.75);
        when(this.mockTrialBalanceResultFromStatic.getDebitSums()).thenReturn(expectedDebits);

        try (MockedStatic<TrialBalanceService> mockedStaticService = mockStatic(TrialBalanceService.class)) {
            mockedStaticService.when(() -> TrialBalanceService.compute(any(Ledger.class), any(LocalDate.class), any(LocalDate.class)))
                             .thenReturn(this.mockTrialBalanceResultFromStatic);

            TrialBalanceService serviceInstance = new TrialBalanceService(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);
            assertEquals(expectedDebits, serviceInstance.getDebitSums());
        }
    }

    @Test
    @DisplayName("Instance getCreditSums: Returns data from result held by instance")
    void testGetCreditSums_returnsDataFromComputedResult() {
        Map<String, Double> expectedCredits = Map.of("ACC_C", 350.50);
        when(this.mockTrialBalanceResultFromStatic.getCreditSums()).thenReturn(expectedCredits);

        try (MockedStatic<TrialBalanceService> mockedStaticService = mockStatic(TrialBalanceService.class)) {
            mockedStaticService.when(() -> TrialBalanceService.compute(any(Ledger.class), any(LocalDate.class), any(LocalDate.class)))
                             .thenReturn(this.mockTrialBalanceResultFromStatic);

            TrialBalanceService serviceInstance = new TrialBalanceService(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);
            assertEquals(expectedCredits, serviceInstance.getCreditSums());
        }
    }

    @Test
    @DisplayName("Instance isBalanced: Returns data from result held by instance")
    void testIsBalanced_returnsDataFromComputedResult() {
        boolean expectedIsBalanced = false;
        when(this.mockTrialBalanceResultFromStatic.isBalanced()).thenReturn(expectedIsBalanced);

        try (MockedStatic<TrialBalanceService> mockedStaticService = mockStatic(TrialBalanceService.class)) {
            mockedStaticService.when(() -> TrialBalanceService.compute(any(Ledger.class), any(LocalDate.class), any(LocalDate.class)))
                             .thenReturn(this.mockTrialBalanceResultFromStatic);

            TrialBalanceService serviceInstance = new TrialBalanceService(this.mockLedger, this.TEST_FROM_DATE, this.TEST_TO_DATE);
            assertEquals(expectedIsBalanced, serviceInstance.isBalanced());
        }
    }
}
