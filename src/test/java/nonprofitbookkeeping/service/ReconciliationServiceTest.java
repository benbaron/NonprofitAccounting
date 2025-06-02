package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountingTransaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient; // For lenient stubbing if needed
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    private ReconciliationService service;

    @Mock
    private AccountingTransaction mockTx1, mockTx2, mockTx3, mockTx4;

    @Mock
    private Account mockAccount1, mockAccount2, mockAccountNullNum;

    // Common statement details (not used by current reconcile logic but part of signature)
    private final String STATEMENT_DATE = "2023-01-31";
    private final BigDecimal STATEMENT_ENDING_BALANCE = new BigDecimal("1000.00");


    @BeforeEach
    void setUp() {
        this.service = new ReconciliationService();

        // Common stubs - can be overridden in specific tests if needed
        lenient().when(this.mockAccount1.getAccountNumber()).thenReturn("ACC1");
        lenient().when(this.mockAccount2.getAccountNumber()).thenReturn("ACC2");
        lenient().when(this.mockAccountNullNum.getAccountNumber()).thenReturn(null); // Account with null number

        lenient().when(this.mockTx1.getAccount()).thenReturn(this.mockAccount1);
        lenient().when(this.mockTx1.getBookingDateTimestamp()).thenReturn(1L);

        lenient().when(this.mockTx2.getAccount()).thenReturn(this.mockAccount1); // Another tx for ACC1
        lenient().when(this.mockTx2.getBookingDateTimestamp()).thenReturn(2L);

        lenient().when(this.mockTx3.getAccount()).thenReturn(this.mockAccount2); // Tx for ACC2
        lenient().when(this.mockTx3.getBookingDateTimestamp()).thenReturn(3L);

        lenient().when(this.mockTx4.getAccount()).thenReturn(this.mockAccount1); // Tx for ACC1 with different ID
        lenient().when(this.mockTx4.getBookingDateTimestamp()).thenReturn(4L);
    }

    // --- Constructor Test ---
    @Test
    @DisplayName("Constructor: New instance should have an empty unreconciled list")
    void testConstructor_newInstance_unreconciledListIsEmpty() {
        assertTrue(ReconciliationService.getUnreconciled("anyAccount").isEmpty(), "Newly created service should have no unreconciled transactions.");
        assertTrue(ReconciliationService.listReconcilableAccounts().isEmpty(), "Newly created service should have no reconcilable accounts.");
    }

    // --- addTransactionToReconcile Tests ---
    @Test
    @DisplayName("addTransactionToReconcile: Valid transaction should be added")
    void testAddTransactionToReconcile_validTransaction_isAdded() {
        this.service.addTransactionToReconcile(this.mockTx1);
        assertEquals(1, ReconciliationService.getUnreconciled("ACC1").size());
        assertSame(this.mockTx1, ReconciliationService.getUnreconciled("ACC1").get(0));
    }

    @Test
    @DisplayName("addTransactionToReconcile: Null transaction should be ignored")
    void testAddTransactionToReconcile_nullTransaction_isIgnored() {
        this.service.addTransactionToReconcile(null);
        assertTrue(ReconciliationService.listReconcilableAccounts().isEmpty());
    }

    // --- getUnreconciled Tests ---
    @Test
    @DisplayName("getUnreconciled: Null accountId should return empty list")
    void testGetUnreconciled_nullAccountId_returnsEmptyList() {
        this.service.addTransactionToReconcile(this.mockTx1);
        assertTrue(ReconciliationService.getUnreconciled(null).isEmpty());
    }

    @Test
    @DisplayName("getUnreconciled: Blank accountId should return empty list")
    void testGetUnreconciled_blankAccountId_returnsEmptyList() {
        this.service.addTransactionToReconcile(this.mockTx1);
        assertTrue(ReconciliationService.getUnreconciled("   ").isEmpty());
    }

    @Test
    @DisplayName("getUnreconciled: No transactions added should return empty list")
    void testGetUnreconciled_noTransactionsAdded_returnsEmptyList() {
        assertTrue(ReconciliationService.getUnreconciled("ACC1").isEmpty());
    }

    @Test
    @DisplayName("getUnreconciled: Returns only transactions for the specified accountId")
    void testGetUnreconciled_transactionsForSpecificAccount_returnsOnlyMatchingTransactions() {
        this.service.addTransactionToReconcile(this.mockTx1); // ACC1
        this.service.addTransactionToReconcile(this.mockTx2); // ACC1
        this.service.addTransactionToReconcile(this.mockTx3); // ACC2

        List<AccountingTransaction> acc1Transactions = ReconciliationService.getUnreconciled("ACC1");
        assertEquals(2, acc1Transactions.size());
        assertTrue(acc1Transactions.contains(this.mockTx1));
        assertTrue(acc1Transactions.contains(this.mockTx2));

        List<AccountingTransaction> acc2Transactions = ReconciliationService.getUnreconciled("ACC2");
        assertEquals(1, acc2Transactions.size());
        assertTrue(acc2Transactions.contains(this.mockTx3));
    }

    @Test
    @DisplayName("getUnreconciled: AccountId with no transactions should return empty list")
    void testGetUnreconciled_accountIdWithNoTransactions_returnsEmptyList() {
        this.service.addTransactionToReconcile(this.mockTx1); // ACC1
        assertTrue(ReconciliationService.getUnreconciled("NONEXISTENT_ACC").isEmpty());
    }

    @Test
    @DisplayName("getUnreconciled: Transaction with null Account object should be skipped")
    void testGetUnreconciled_transactionWithNullAccount_isSkipped() {
        when(this.mockTx1.getAccount()).thenReturn(null);
        this.service.addTransactionToReconcile(this.mockTx1);
        assertTrue(ReconciliationService.getUnreconciled("ACC1").isEmpty());
    }

    @Test
    @DisplayName("getUnreconciled: Transaction with null Account number should be skipped")
    void testGetUnreconciled_transactionWithNullAccountNumber_isSkipped() {
        when(this.mockTx1.getAccount()).thenReturn(this.mockAccountNullNum); // mockAccountNullNum returns null for getAccountNumber()
        this.service.addTransactionToReconcile(this.mockTx1);
        assertTrue(ReconciliationService.getUnreconciled("ACC1").isEmpty()); // Won't match ACC1
        assertTrue(ReconciliationService.getUnreconciled(null).isEmpty()); // Also won't match null due to filter logic
    }


    // --- listReconcilableAccounts Tests ---
    @Test
    @DisplayName("listReconcilableAccounts: No transactions should return empty list")
    void testListReconcilableAccounts_noTransactions_returnsEmptyList() {
        assertTrue(ReconciliationService.listReconcilableAccounts().isEmpty());
    }

    @Test
    @DisplayName("listReconcilableAccounts: Transactions for one account returns single ID")
    void testListReconcilableAccounts_transactionsForOneAccount_returnsSingleAccountId() {
        this.service.addTransactionToReconcile(this.mockTx1);
        this.service.addTransactionToReconcile(this.mockTx2);
        List<String> accountIds = ReconciliationService.listReconcilableAccounts();
        assertEquals(1, accountIds.size());
        assertEquals("ACC1", accountIds.get(0));
    }

    @Test
    @DisplayName("listReconcilableAccounts: Transactions for multiple accounts returns unique IDs")
    void testListReconcilableAccounts_transactionsForMultipleAccounts_returnsUniqueAccountIds() {
        this.service.addTransactionToReconcile(this.mockTx1); // ACC1
        this.service.addTransactionToReconcile(this.mockTx3); // ACC2
        List<String> accountIds = ReconciliationService.listReconcilableAccounts();
        assertEquals(2, accountIds.size());
        assertTrue(accountIds.contains("ACC1"));
        assertTrue(accountIds.contains("ACC2"));
    }

    @Test
    @DisplayName("listReconcilableAccounts: Handles transactions with null account or account number gracefully")
    void testListReconcilableAccounts_withNullOrPartialTransactionData_handlesGracefully() {
        this.service.addTransactionToReconcile(this.mockTx1); // ACC1

        AccountingTransaction txWithNullAccount = mock(AccountingTransaction.class);
        when(txWithNullAccount.getAccount()).thenReturn(null);
        this.service.addTransactionToReconcile(txWithNullAccount);

        AccountingTransaction txWithNullAccNum = mock(AccountingTransaction.class);
        when(txWithNullAccNum.getAccount()).thenReturn(this.mockAccountNullNum); // Account with null number
        this.service.addTransactionToReconcile(txWithNullAccNum);

        List<String> accountIds = ReconciliationService.listReconcilableAccounts();
        assertEquals(1, accountIds.size());
        assertEquals("ACC1", accountIds.get(0));
    }


    // --- reconcile Tests ---
    @Test
    @DisplayName("reconcile: Null accountId should make no changes")
    void testReconcile_nullAccountId_noChange() {
        this.service.addTransactionToReconcile(this.mockTx1);
        this.service.reconcile(null, this.STATEMENT_DATE, 
        	this.STATEMENT_ENDING_BALANCE, List.of(1L));
        assertEquals(1, ReconciliationService.getUnreconciled("ACC1").size());
    }

    @Test
    @DisplayName("reconcile: Blank accountId should make no changes")
    void testReconcile_blankAccountId_noChange() {
        this.service.addTransactionToReconcile(this.mockTx1);
        this.service.reconcile("  ", this.STATEMENT_DATE, 
        	this.STATEMENT_ENDING_BALANCE, List.of(1L));
        assertEquals(1, ReconciliationService.getUnreconciled("ACC1").size());
    }

    @Test
    @DisplayName("reconcile: Null clearedTransactionIds should make no changes")
    void testReconcile_nullClearedTransactionIds_noChange() {
        this.service.addTransactionToReconcile(this.mockTx1);
        this.service.reconcile("ACC1", this.STATEMENT_DATE, this.STATEMENT_ENDING_BALANCE, null);
        assertEquals(1, ReconciliationService.getUnreconciled("ACC1").size());
    }

    @Test
    @DisplayName("reconcile: Removes cleared transactions for specific account, leaves others")
    void testReconcile_validInput_removesClearedTransactionsForSpecificAccount() {
        this.service.addTransactionToReconcile(this.mockTx1); // ACC1, TXN101
        this.service.addTransactionToReconcile(this.mockTx2); // ACC1, TXN102
        this.service.addTransactionToReconcile(this.mockTx3); // ACC2, TXN201
        this.service.addTransactionToReconcile(this.mockTx4); // ACC1, TXN103

        List<Long> clearedIdsForAcc1 = Arrays.asList(1L, 3L);
        this.service.reconcile("ACC1", this.STATEMENT_DATE, this.STATEMENT_ENDING_BALANCE, clearedIdsForAcc1);

        List<AccountingTransaction> acc1Unreconciled = ReconciliationService.getUnreconciled("ACC1");
        assertEquals(1, acc1Unreconciled.size(), "ACC1 should have one unreconciled transaction left.");
        assertSame(this.mockTx2, acc1Unreconciled.get(0), "TXN102 should be the remaining transaction for ACC1.");

        List<AccountingTransaction> acc2Unreconciled = ReconciliationService.getUnreconciled("ACC2");
        assertEquals(1, acc2Unreconciled.size(), "ACC2 transactions should be unaffected.");
        assertSame(this.mockTx3, acc2Unreconciled.get(0));
    }

    @Test
    @DisplayName("reconcile: Cleared IDs not matching any transactions for the account make no change to that account")
    void testReconcile_clearedIdsDoNotMatchAnyTransactions_noChange() {
        this.service.addTransactionToReconcile(this.mockTx1); // ACC1, TXN101
        this.service.addTransactionToReconcile(this.mockTx2); // ACC1, TXN102

        List<Long> nonMatchingClearedIds = Arrays.asList(9L, 8L);
        this.service.reconcile("ACC1", this.STATEMENT_DATE, this.STATEMENT_ENDING_BALANCE, nonMatchingClearedIds);

        assertEquals(2, ReconciliationService.getUnreconciled("ACC1").size(), "No transactions should be removed if IDs don't match.");
    }

    @Test
    @DisplayName("reconcile: Reconciling all transactions for an account empties its list")
    void testReconcile_reconcileAllTransactionsForAccount_emptiesListForThatAccount() {
        this.service.addTransactionToReconcile(this.mockTx1); // ACC1, TXN101
        this.service.addTransactionToReconcile(this.mockTx2); // ACC1, TXN102
        this.service.addTransactionToReconcile(this.mockTx3); // ACC2, TXN201

        List<Long> allAcc1ClearedIds = Arrays.asList(1L, 2L);
        this.service.reconcile("ACC1", this.STATEMENT_DATE, this.STATEMENT_ENDING_BALANCE, allAcc1ClearedIds);

        assertTrue(ReconciliationService.getUnreconciled("ACC1").isEmpty(), "ACC1 should have no unreconciled transactions left.");
        assertFalse(ReconciliationService.getUnreconciled("ACC2").isEmpty(), "ACC2 transactions should remain.");
    }

    @Test
    @DisplayName("reconcile: Transaction with null ID in service is not removed by reconcile")
    void testReconcile_transactionWithNullId_isNotRemoved() {
        AccountingTransaction txWithNullId = mock(AccountingTransaction.class);
        when(txWithNullId.getAccount()).thenReturn(this.mockAccount1);
        when(txWithNullId.getBookingDateTimestamp()).thenReturn(null); // Transaction with null ID
        this.service.addTransactionToReconcile(txWithNullId);
        this.service.addTransactionToReconcile(this.mockTx1); // ACC1, TXN101

        this.service.reconcile("ACC1", this.STATEMENT_DATE, 
        	this.STATEMENT_ENDING_BALANCE, List.of(1L, -1L));

        List<AccountingTransaction> acc1Unreconciled = ReconciliationService.getUnreconciled("ACC1");
        assertEquals(1, acc1Unreconciled.size(), "Only transaction 1 should be removed.");
        assertSame(txWithNullId, acc1Unreconciled.get(0), "Transaction with null ID should remain.");
    }
}
