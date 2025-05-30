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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient; // For lenient stubbing if needed

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
        service = new ReconciliationService();

        // Common stubs - can be overridden in specific tests if needed
        lenient().when(mockAccount1.getAccountNumber()).thenReturn("ACC1");
        lenient().when(mockAccount2.getAccountNumber()).thenReturn("ACC2");
        lenient().when(mockAccountNullNum.getAccountNumber()).thenReturn(null); // Account with null number

        lenient().when(mockTx1.getAccount()).thenReturn(mockAccount1);
        lenient().when(mockTx1.getId()).thenReturn("TXN101");
        
        lenient().when(mockTx2.getAccount()).thenReturn(mockAccount1); // Another tx for ACC1
        lenient().when(mockTx2.getId()).thenReturn("TXN102");

        lenient().when(mockTx3.getAccount()).thenReturn(mockAccount2); // Tx for ACC2
        lenient().when(mockTx3.getId()).thenReturn("TXN201");
        
        lenient().when(mockTx4.getAccount()).thenReturn(mockAccount1); // Tx for ACC1 with different ID
        lenient().when(mockTx4.getId()).thenReturn("TXN103");
    }

    // --- Constructor Test ---
    @Test
    @DisplayName("Constructor: New instance should have an empty unreconciled list")
    void testConstructor_newInstance_unreconciledListIsEmpty() {
        assertTrue(service.getUnreconciled("anyAccount").isEmpty(), "Newly created service should have no unreconciled transactions.");
        assertTrue(service.listReconcilableAccounts().isEmpty(), "Newly created service should have no reconcilable accounts.");
    }

    // --- addTransactionToReconcile Tests ---
    @Test
    @DisplayName("addTransactionToReconcile: Valid transaction should be added")
    void testAddTransactionToReconcile_validTransaction_isAdded() {
        service.addTransactionToReconcile(mockTx1);
        assertEquals(1, service.getUnreconciled("ACC1").size());
        assertSame(mockTx1, service.getUnreconciled("ACC1").get(0));
    }

    @Test
    @DisplayName("addTransactionToReconcile: Null transaction should be ignored")
    void testAddTransactionToReconcile_nullTransaction_isIgnored() {
        service.addTransactionToReconcile(null);
        assertTrue(service.listReconcilableAccounts().isEmpty());
    }

    // --- getUnreconciled Tests ---
    @Test
    @DisplayName("getUnreconciled: Null accountId should return empty list")
    void testGetUnreconciled_nullAccountId_returnsEmptyList() {
        service.addTransactionToReconcile(mockTx1);
        assertTrue(service.getUnreconciled(null).isEmpty());
    }

    @Test
    @DisplayName("getUnreconciled: Blank accountId should return empty list")
    void testGetUnreconciled_blankAccountId_returnsEmptyList() {
        service.addTransactionToReconcile(mockTx1);
        assertTrue(service.getUnreconciled("   ").isEmpty());
    }

    @Test
    @DisplayName("getUnreconciled: No transactions added should return empty list")
    void testGetUnreconciled_noTransactionsAdded_returnsEmptyList() {
        assertTrue(service.getUnreconciled("ACC1").isEmpty());
    }

    @Test
    @DisplayName("getUnreconciled: Returns only transactions for the specified accountId")
    void testGetUnreconciled_transactionsForSpecificAccount_returnsOnlyMatchingTransactions() {
        service.addTransactionToReconcile(mockTx1); // ACC1
        service.addTransactionToReconcile(mockTx2); // ACC1
        service.addTransactionToReconcile(mockTx3); // ACC2

        List<AccountingTransaction> acc1Transactions = service.getUnreconciled("ACC1");
        assertEquals(2, acc1Transactions.size());
        assertTrue(acc1Transactions.contains(mockTx1));
        assertTrue(acc1Transactions.contains(mockTx2));

        List<AccountingTransaction> acc2Transactions = service.getUnreconciled("ACC2");
        assertEquals(1, acc2Transactions.size());
        assertTrue(acc2Transactions.contains(mockTx3));
    }

    @Test
    @DisplayName("getUnreconciled: AccountId with no transactions should return empty list")
    void testGetUnreconciled_accountIdWithNoTransactions_returnsEmptyList() {
        service.addTransactionToReconcile(mockTx1); // ACC1
        assertTrue(service.getUnreconciled("NONEXISTENT_ACC").isEmpty());
    }
    
    @Test
    @DisplayName("getUnreconciled: Transaction with null Account object should be skipped")
    void testGetUnreconciled_transactionWithNullAccount_isSkipped() {
        when(mockTx1.getAccount()).thenReturn(null);
        service.addTransactionToReconcile(mockTx1);
        assertTrue(service.getUnreconciled("ACC1").isEmpty());
    }

    @Test
    @DisplayName("getUnreconciled: Transaction with null Account number should be skipped")
    void testGetUnreconciled_transactionWithNullAccountNumber_isSkipped() {
        when(mockTx1.getAccount()).thenReturn(mockAccountNullNum); // mockAccountNullNum returns null for getAccountNumber()
        service.addTransactionToReconcile(mockTx1);
        assertTrue(service.getUnreconciled("ACC1").isEmpty()); // Won't match ACC1
        assertTrue(service.getUnreconciled(null).isEmpty()); // Also won't match null due to filter logic
    }


    // --- listReconcilableAccounts Tests ---
    @Test
    @DisplayName("listReconcilableAccounts: No transactions should return empty list")
    void testListReconcilableAccounts_noTransactions_returnsEmptyList() {
        assertTrue(service.listReconcilableAccounts().isEmpty());
    }

    @Test
    @DisplayName("listReconcilableAccounts: Transactions for one account returns single ID")
    void testListReconcilableAccounts_transactionsForOneAccount_returnsSingleAccountId() {
        service.addTransactionToReconcile(mockTx1);
        service.addTransactionToReconcile(mockTx2);
        List<String> accountIds = service.listReconcilableAccounts();
        assertEquals(1, accountIds.size());
        assertEquals("ACC1", accountIds.get(0));
    }

    @Test
    @DisplayName("listReconcilableAccounts: Transactions for multiple accounts returns unique IDs")
    void testListReconcilableAccounts_transactionsForMultipleAccounts_returnsUniqueAccountIds() {
        service.addTransactionToReconcile(mockTx1); // ACC1
        service.addTransactionToReconcile(mockTx3); // ACC2
        List<String> accountIds = service.listReconcilableAccounts();
        assertEquals(2, accountIds.size());
        assertTrue(accountIds.contains("ACC1"));
        assertTrue(accountIds.contains("ACC2"));
    }

    @Test
    @DisplayName("listReconcilableAccounts: Handles transactions with null account or account number gracefully")
    void testListReconcilableAccounts_withNullOrPartialTransactionData_handlesGracefully() {
        service.addTransactionToReconcile(mockTx1); // ACC1
        
        AccountingTransaction txWithNullAccount = mock(AccountingTransaction.class);
        when(txWithNullAccount.getAccount()).thenReturn(null);
        service.addTransactionToReconcile(txWithNullAccount);
        
        AccountingTransaction txWithNullAccNum = mock(AccountingTransaction.class);
        when(txWithNullAccNum.getAccount()).thenReturn(mockAccountNullNum); // Account with null number
        service.addTransactionToReconcile(txWithNullAccNum);

        List<String> accountIds = service.listReconcilableAccounts();
        assertEquals(1, accountIds.size());
        assertEquals("ACC1", accountIds.get(0));
    }


    // --- reconcile Tests ---
    @Test
    @DisplayName("reconcile: Null accountId should make no changes")
    void testReconcile_nullAccountId_noChange() {
        service.addTransactionToReconcile(mockTx1);
        service.reconcile(null, STATEMENT_DATE, STATEMENT_ENDING_BALANCE, List.of("TXN101"));
        assertEquals(1, service.getUnreconciled("ACC1").size());
    }

    @Test
    @DisplayName("reconcile: Blank accountId should make no changes")
    void testReconcile_blankAccountId_noChange() {
        service.addTransactionToReconcile(mockTx1);
        service.reconcile("  ", STATEMENT_DATE, STATEMENT_ENDING_BALANCE, List.of("TXN101"));
        assertEquals(1, service.getUnreconciled("ACC1").size());
    }

    @Test
    @DisplayName("reconcile: Null clearedTransactionIds should make no changes")
    void testReconcile_nullClearedTransactionIds_noChange() {
        service.addTransactionToReconcile(mockTx1);
        service.reconcile("ACC1", STATEMENT_DATE, STATEMENT_ENDING_BALANCE, null);
        assertEquals(1, service.getUnreconciled("ACC1").size());
    }

    @Test
    @DisplayName("reconcile: Removes cleared transactions for specific account, leaves others")
    void testReconcile_validInput_removesClearedTransactionsForSpecificAccount() {
        service.addTransactionToReconcile(mockTx1); // ACC1, TXN101
        service.addTransactionToReconcile(mockTx2); // ACC1, TXN102
        service.addTransactionToReconcile(mockTx3); // ACC2, TXN201
        service.addTransactionToReconcile(mockTx4); // ACC1, TXN103

        List<String> clearedIdsForAcc1 = Arrays.asList("TXN101", "TXN103");
        service.reconcile("ACC1", STATEMENT_DATE, STATEMENT_ENDING_BALANCE, clearedIdsForAcc1);

        List<AccountingTransaction> acc1Unreconciled = service.getUnreconciled("ACC1");
        assertEquals(1, acc1Unreconciled.size(), "ACC1 should have one unreconciled transaction left.");
        assertSame(mockTx2, acc1Unreconciled.get(0), "TXN102 should be the remaining transaction for ACC1.");

        List<AccountingTransaction> acc2Unreconciled = service.getUnreconciled("ACC2");
        assertEquals(1, acc2Unreconciled.size(), "ACC2 transactions should be unaffected.");
        assertSame(mockTx3, acc2Unreconciled.get(0));
    }

    @Test
    @DisplayName("reconcile: Cleared IDs not matching any transactions for the account make no change to that account")
    void testReconcile_clearedIdsDoNotMatchAnyTransactions_noChange() {
        service.addTransactionToReconcile(mockTx1); // ACC1, TXN101
        service.addTransactionToReconcile(mockTx2); // ACC1, TXN102
        
        List<String> nonMatchingClearedIds = Arrays.asList("TXN999", "TXN888");
        service.reconcile("ACC1", STATEMENT_DATE, STATEMENT_ENDING_BALANCE, nonMatchingClearedIds);
        
        assertEquals(2, service.getUnreconciled("ACC1").size(), "No transactions should be removed if IDs don't match.");
    }

    @Test
    @DisplayName("reconcile: Reconciling all transactions for an account empties its list")
    void testReconcile_reconcileAllTransactionsForAccount_emptiesListForThatAccount() {
        service.addTransactionToReconcile(mockTx1); // ACC1, TXN101
        service.addTransactionToReconcile(mockTx2); // ACC1, TXN102
        service.addTransactionToReconcile(mockTx3); // ACC2, TXN201
        
        List<String> allAcc1ClearedIds = Arrays.asList("TXN101", "TXN102");
        service.reconcile("ACC1", STATEMENT_DATE, STATEMENT_ENDING_BALANCE, allAcc1ClearedIds);
        
        assertTrue(service.getUnreconciled("ACC1").isEmpty(), "ACC1 should have no unreconciled transactions left.");
        assertFalse(service.getUnreconciled("ACC2").isEmpty(), "ACC2 transactions should remain.");
    }
    
    @Test
    @DisplayName("reconcile: Transaction with null ID in service is not removed by reconcile")
    void testReconcile_transactionWithNullId_isNotRemoved() {
        AccountingTransaction txWithNullId = mock(AccountingTransaction.class);
        when(txWithNullId.getAccount()).thenReturn(mockAccount1);
        when(txWithNullId.getId()).thenReturn(null); // Transaction with null ID
        service.addTransactionToReconcile(txWithNullId);
        service.addTransactionToReconcile(mockTx1); // ACC1, TXN101

        service.reconcile("ACC1", STATEMENT_DATE, STATEMENT_ENDING_BALANCE, List.of("TXN101", "someOtherId"));

        List<AccountingTransaction> acc1Unreconciled = service.getUnreconciled("ACC1");
        assertEquals(1, acc1Unreconciled.size(), "Only TXN101 should be removed.");
        assertSame(txWithNullId, acc1Unreconciled.get(0), "Transaction with null ID should remain.");
    }
}
