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
        TrialBalanceResultIntf result = TrialBalanceService.compute(null, TEST_FROM_DATE, TEST_TO_DATE);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Ledger with null transactions list should return empty, balanced result")
    void testCompute_ledgerWithNullTransactions_returnsEmptyBalancedResult() {
        when(mockLedger.getTransactions()).thenReturn(null);
        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Ledger with empty transactions list should return empty, balanced result")
    void testCompute_ledgerWithEmptyTransactions_returnsEmptyBalancedResult() {
        when(mockLedger.getTransactions()).thenReturn(Collections.emptyList());
        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Null 'from' date should return empty, balanced result")
    void testCompute_nullFromDate_returnsEmptyBalancedResult() {
        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, null, TEST_TO_DATE);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Null 'to' date should return empty, balanced result")
    void testCompute_nullToDate_returnsEmptyBalancedResult() {
        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, null);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: 'from' date after 'to' date should return empty, balanced result")
    void testCompute_fromAfterToDate_returnsEmptyBalancedResult() {
        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, TEST_TO_DATE, TEST_FROM_DATE);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Filters transactions based on date range")
    void testCompute_filtersTransactionsBasedOnDateRange() {
        when(mockTx1.getDate()).thenReturn(TEST_FROM_DATE.minusDays(1).format(DATE_FORMATTER)); // Before range
        when(mockTx1.getEntries()).thenReturn(Set.of(mockEntry1)); // Avoid NPE if processed
        lenient().when(mockEntry1.getAccountNumber()).thenReturn("ACC1");
        lenient().when(mockEntry1.getAmount()).thenReturn(BigDecimal.TEN);
        lenient().when(mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);


        when(mockTx2.getDate()).thenReturn(TEST_FROM_DATE.format(DATE_FORMATTER)); // Inside range
        when(mockTx2.getEntries()).thenReturn(Set.of(mockEntry2));
        when(mockEntry2.getAccountNumber()).thenReturn("ACC1");
        when(mockEntry2.getAmount()).thenReturn(BigDecimal.valueOf(100));
        when(mockEntry2.getAccountSide()).thenReturn(AccountSide.DEBIT);

        when(mockTx3.getDate()).thenReturn(TEST_TO_DATE.plusDays(1).format(DATE_FORMATTER)); // After range
        when(mockTx3.getEntries()).thenReturn(Set.of(mockEntry3)); // Avoid NPE if processed
        lenient().when(mockEntry3.getAccountNumber()).thenReturn("ACC1");
        lenient().when(mockEntry3.getAmount()).thenReturn(BigDecimal.ONE);
        lenient().when(mockEntry3.getAccountSide()).thenReturn(AccountSide.DEBIT);


        when(mockLedger.getTransactions()).thenReturn(Arrays.asList(mockTx1, mockTx2, mockTx3));
        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);

        assertEquals(1, result.getDebitSums().size());
        assertEquals(100.0, result.getDebitSums().get("ACC1"), 0.001);
        assertTrue(result.getCreditSums().isEmpty());
        assertFalse(result.isBalanced()); // 100 debit, 0 credit
    }

    @Test
    @DisplayName("compute: Skips transaction with unparseable date")
    void testCompute_skipsTransactionWithUnparseableDate() {
        when(mockTx1.getDate()).thenReturn("INVALID-DATE-FORMAT");
        // No need to stub getEntries for mockTx1 as it should be skipped before that.

        when(mockTx2.getDate()).thenReturn(TEST_FROM_DATE.format(DATE_FORMATTER));
        when(mockTx2.getEntries()).thenReturn(Set.of(mockEntry1));
        when(mockEntry1.getAccountNumber()).thenReturn("ACC1");
        when(mockEntry1.getAmount()).thenReturn(BigDecimal.valueOf(50));
        when(mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);

        when(mockLedger.getTransactions()).thenReturn(Arrays.asList(mockTx1, mockTx2));
        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);

        assertEquals(1, result.getDebitSums().size());
        assertEquals(50.0, result.getDebitSums().get("ACC1"), 0.001);
    }

    @Test
    @DisplayName("compute: Correctly sums debits and credits, balanced")
    void testCompute_multipleEntries_balanced_correctSumsAndBalanceTrue() {
        when(mockTx1.getDate()).thenReturn(TEST_FROM_DATE.format(DATE_FORMATTER));
        when(mockEntry1.getAccountNumber()).thenReturn("ACC_DEBIT");
        when(mockEntry1.getAmount()).thenReturn(new BigDecimal("100.00"));
        when(mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);
        when(mockEntry2.getAccountNumber()).thenReturn("ACC_CREDIT");
        when(mockEntry2.getAmount()).thenReturn(new BigDecimal("100.00"));
        when(mockEntry2.getAccountSide()).thenReturn(AccountSide.CREDIT);
        when(mockTx1.getEntries()).thenReturn(Set.of(mockEntry1, mockEntry2));

        when(mockLedger.getTransactions()).thenReturn(List.of(mockTx1));
        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);

        assertEquals(100.0, result.getDebitSums().get("ACC_DEBIT"), 0.001);
        assertEquals(100.0, result.getCreditSums().get("ACC_CREDIT"), 0.001);
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Correctly sums debits and credits, unbalanced")
    void testCompute_multipleEntries_unbalanced_correctSumsAndBalanceFalse() {
        when(mockTx1.getDate()).thenReturn(TEST_FROM_DATE.format(DATE_FORMATTER));
        when(mockEntry1.getAccountNumber()).thenReturn("ACC_DEBIT");
        when(mockEntry1.getAmount()).thenReturn(new BigDecimal("100.00"));
        when(mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);
        when(mockEntry2.getAccountNumber()).thenReturn("ACC_CREDIT");
        when(mockEntry2.getAmount()).thenReturn(new BigDecimal("90.00")); // Unbalanced
        when(mockEntry2.getAccountSide()).thenReturn(AccountSide.CREDIT);
        when(mockTx1.getEntries()).thenReturn(Set.of(mockEntry1, mockEntry2));

        when(mockLedger.getTransactions()).thenReturn(List.of(mockTx1));
        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);

        assertEquals(100.0, result.getDebitSums().get("ACC_DEBIT"), 0.001);
        assertEquals(90.0, result.getCreditSums().get("ACC_CREDIT"), 0.001);
        assertFalse(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Skips null transaction in list")
    void testCompute_skipsNullTransactionInList() {
        when(mockTx1.getDate()).thenReturn(TEST_FROM_DATE.format(DATE_FORMATTER));
        when(mockTx1.getEntries()).thenReturn(Set.of(mockEntry1));
        when(mockEntry1.getAccountNumber()).thenReturn("ACC1");
        when(mockEntry1.getAmount()).thenReturn(BigDecimal.TEN);
        when(mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);

        when(mockLedger.getTransactions()).thenReturn(Arrays.asList(mockTx1, null, mockTx2));
        // mockTx2 is not fully stubbed, but it won't matter if null tx is skipped before processing tx2.
        // For safety, ensure tx2 doesn't contribute if it were processed (e.g. by date or no entries)
        lenient().when(mockTx2.getDate()).thenReturn(TEST_FROM_DATE.minusDays(1).format(DATE_FORMATTER)); // out of range

        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);
        assertEquals(1, result.getDebitSums().size());
        assertEquals(10.0, result.getDebitSums().get("ACC1"), 0.001);
    }

    @Test
    @DisplayName("compute: Skips transaction with null entries set")
    void testCompute_skipsTransactionWithNullEntries() {
        when(mockTx1.getDate()).thenReturn(TEST_FROM_DATE.format(DATE_FORMATTER));
        when(mockTx1.getEntries()).thenReturn(null); // Null entries

        when(mockLedger.getTransactions()).thenReturn(List.of(mockTx1));
        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);
        assertTrue(result.getDebitSums().isEmpty());
        assertTrue(result.getCreditSums().isEmpty());
        assertTrue(result.isBalanced());
    }

    @Test
    @DisplayName("compute: Skips null entry in transaction's entry set")
    void testCompute_skipsNullEntryInTransaction() {
        when(mockTx1.getDate()).thenReturn(TEST_FROM_DATE.format(DATE_FORMATTER));
        when(mockEntry1.getAccountNumber()).thenReturn("ACC1");
        when(mockEntry1.getAmount()).thenReturn(BigDecimal.TEN);
        when(mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);
        // Create a Set that allows null, though Set.of doesn't.
        Set<AccountingEntry> entriesWithNull = new java.util.HashSet<>();
        entriesWithNull.add(mockEntry1);
        entriesWithNull.add(null);
        when(mockTx1.getEntries()).thenReturn(entriesWithNull);

        when(mockLedger.getTransactions()).thenReturn(List.of(mockTx1));
        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);
        assertEquals(1, result.getDebitSums().size());
        assertEquals(10.0, result.getDebitSums().get("ACC1"), 0.001);
    }

    @Test
    @DisplayName("compute: Skips entry with null amount")
    void testCompute_skipsEntryWithNullAmount() {
        when(mockTx1.getDate()).thenReturn(TEST_FROM_DATE.format(DATE_FORMATTER));
        when(mockEntry1.getAccountNumber()).thenReturn("ACC1");
        when(mockEntry1.getAmount()).thenReturn(null); // Null amount
        when(mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);
        when(mockTx1.getEntries()).thenReturn(Set.of(mockEntry1));

        when(mockLedger.getTransactions()).thenReturn(List.of(mockTx1));
        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);
        assertTrue(result.getDebitSums().isEmpty());
    }

    @Test
    @DisplayName("compute: Skips entry with null or blank account number")
    void testCompute_skipsEntryWithNullOrBlankAccountNumber() {
        when(mockTx1.getDate()).thenReturn(TEST_FROM_DATE.format(DATE_FORMATTER));

        when(mockEntry1.getAccountNumber()).thenReturn(null); // Null account number
        when(mockEntry1.getAmount()).thenReturn(BigDecimal.TEN);
        when(mockEntry1.getAccountSide()).thenReturn(AccountSide.DEBIT);

        when(mockEntry2.getAccountNumber()).thenReturn("  "); // Blank account number
        when(mockEntry2.getAmount()).thenReturn(BigDecimal.valueOf(20));
        when(mockEntry2.getAccountSide()).thenReturn(AccountSide.DEBIT);

        when(mockEntry3.getAccountNumber()).thenReturn("ACC_VALID");
        when(mockEntry3.getAmount()).thenReturn(BigDecimal.valueOf(30));
        when(mockEntry3.getAccountSide()).thenReturn(AccountSide.DEBIT);

        when(mockTx1.getEntries()).thenReturn(Set.of(mockEntry1, mockEntry2, mockEntry3));
        when(mockLedger.getTransactions()).thenReturn(List.of(mockTx1));
        TrialBalanceResultIntf result = TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);

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

        when(mockTrialBalanceResultFromStatic.getDebitSums()).thenReturn(expectedDebits);
        when(mockTrialBalanceResultFromStatic.getCreditSums()).thenReturn(expectedCredits);
        when(mockTrialBalanceResultFromStatic.isBalanced()).thenReturn(expectedBalance);

        try (MockedStatic<TrialBalanceService> mockedStaticService = mockStatic(TrialBalanceService.class)) {
            // Make the static compute method return our mocked result when called by the constructor
            mockedStaticService.when(() -> TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, TEST_TO_DATE))
                             .thenReturn(mockTrialBalanceResultFromStatic);

            TrialBalanceService serviceInstance = new TrialBalanceService(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);

            // Verify static compute was called by constructor
            mockedStaticService.verify(() -> TrialBalanceService.compute(mockLedger, TEST_FROM_DATE, TEST_TO_DATE));

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
        when(mockTrialBalanceResultFromStatic.getDebitSums()).thenReturn(expectedDebits);

        try (MockedStatic<TrialBalanceService> mockedStaticService = mockStatic(TrialBalanceService.class)) {
            mockedStaticService.when(() -> TrialBalanceService.compute(any(Ledger.class), any(LocalDate.class), any(LocalDate.class)))
                             .thenReturn(mockTrialBalanceResultFromStatic);

            TrialBalanceService serviceInstance = new TrialBalanceService(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);
            assertEquals(expectedDebits, serviceInstance.getDebitSums());
        }
    }

    @Test
    @DisplayName("Instance getCreditSums: Returns data from result held by instance")
    void testGetCreditSums_returnsDataFromComputedResult() {
        Map<String, Double> expectedCredits = Map.of("ACC_C", 350.50);
        when(mockTrialBalanceResultFromStatic.getCreditSums()).thenReturn(expectedCredits);

        try (MockedStatic<TrialBalanceService> mockedStaticService = mockStatic(TrialBalanceService.class)) {
            mockedStaticService.when(() -> TrialBalanceService.compute(any(Ledger.class), any(LocalDate.class), any(LocalDate.class)))
                             .thenReturn(mockTrialBalanceResultFromStatic);

            TrialBalanceService serviceInstance = new TrialBalanceService(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);
            assertEquals(expectedCredits, serviceInstance.getCreditSums());
        }
    }

    @Test
    @DisplayName("Instance isBalanced: Returns data from result held by instance")
    void testIsBalanced_returnsDataFromComputedResult() {
        boolean expectedIsBalanced = false;
        when(mockTrialBalanceResultFromStatic.isBalanced()).thenReturn(expectedIsBalanced);

        try (MockedStatic<TrialBalanceService> mockedStaticService = mockStatic(TrialBalanceService.class)) {
            mockedStaticService.when(() -> TrialBalanceService.compute(any(Ledger.class), any(LocalDate.class), any(LocalDate.class)))
                             .thenReturn(mockTrialBalanceResultFromStatic);

            TrialBalanceService serviceInstance = new TrialBalanceService(mockLedger, TEST_FROM_DATE, TEST_TO_DATE);
            assertEquals(expectedIsBalanced, serviceInstance.isBalanced());
        }
    }
}
