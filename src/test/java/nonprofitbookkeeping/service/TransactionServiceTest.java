package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountingTransaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountingTransaction mockTx1, mockTx2, mockTx3, mockTxNullId, mockTxNullAccount, mockTxNullAccNum;

    @Mock
    private Account mockAccount1, mockAccount2, mockAccountForNullNum;

    @BeforeEach
    void setUp() {
        TransactionService.clearAllTransactions();

        // Common stubs
        lenient().when(mockAccount1.getAccountNumber()).thenReturn("ACC1");
        lenient().when(mockAccount2.getAccountNumber()).thenReturn("ACC2");
        lenient().when(mockAccountForNullNum.getAccountNumber()).thenReturn(null);


        lenient().when(mockTx1.getId()).thenReturn("TXN1");
        lenient().when(mockTx1.getAccount()).thenReturn(mockAccount1);

        lenient().when(mockTx2.getId()).thenReturn("TXN2");
        lenient().when(mockTx2.getAccount()).thenReturn(mockAccount2);

        lenient().when(mockTx3.getId()).thenReturn("TXN3");
        lenient().when(mockTx3.getAccount()).thenReturn(mockAccount1); // Another transaction for ACC1

        lenient().when(mockTxNullId.getId()).thenReturn(null);
        lenient().when(mockTxNullId.getAccount()).thenReturn(mockAccount1);

        lenient().when(mockTxNullAccount.getId()).thenReturn("TXN_NULL_ACC");
        lenient().when(mockTxNullAccount.getAccount()).thenReturn(null);

        lenient().when(mockTxNullAccNum.getId()).thenReturn("TXN_NULL_ACC_NUM");
        lenient().when(mockTxNullAccNum.getAccount()).thenReturn(mockAccountForNullNum);

    }

    // --- addTransaction Tests ---
    @Test
    @DisplayName("addTransaction: Valid transaction should be added")
    void testAddTransaction_validTransaction_isAddedAndRetrievable() {
        TransactionService.addTransaction(mockTx1);
        List<AccountingTransaction> acc1Txs = TransactionService.getTransactionsForAccount("ACC1");
        assertEquals(1, acc1Txs.size());
        assertSame(mockTx1, acc1Txs.get(0));
    }

    @Test
    @DisplayName("addTransaction: Null transaction should be ignored")
    void testAddTransaction_nullTransaction_isIgnored() {
        TransactionService.addTransaction(null);
        assertTrue(TransactionService.getTransactionsForAccount("ACC1").isEmpty());
        // Check overall list size if a method like getAllTransactions existed, for now, check via specific account
    }

    // --- removeTransaction Tests ---
    @Test
    @DisplayName("removeTransaction: Existing ID removes transaction and returns true")
    void testRemoveTransaction_existingId_removesTransactionAndReturnsTrue() {
        TransactionService.addTransaction(mockTx1);
        TransactionService.addTransaction(mockTx2);

        assertTrue(TransactionService.removeTransaction("TXN1"));
        List<AccountingTransaction> acc1Txs = TransactionService.getTransactionsForAccount("ACC1");
        assertTrue(acc1Txs.isEmpty());
        assertEquals(1, TransactionService.getTransactionsForAccount("ACC2").size()); // ACC2 should still have its tx
    }

    @Test
    @DisplayName("removeTransaction: Non-existent ID does nothing and returns false")
    void testRemoveTransaction_nonExistentId_doesNothingAndReturnsFalse() {
        TransactionService.addTransaction(mockTx1);
        assertFalse(TransactionService.removeTransaction("TXN_NON_EXISTENT"));
        assertEquals(1, TransactionService.getTransactionsForAccount("ACC1").size());
    }

    @Test
    @DisplayName("removeTransaction: Null ID does nothing and returns false")
    void testRemoveTransaction_nullId_doesNothingAndReturnsFalse() {
        TransactionService.addTransaction(mockTx1);
        assertFalse(TransactionService.removeTransaction(null));
        assertEquals(1, TransactionService.getTransactionsForAccount("ACC1").size());
    }

    @Test
    @DisplayName("removeTransaction: Blank ID does nothing and returns false")
    void testRemoveTransaction_blankId_doesNothingAndReturnsFalse() {
        TransactionService.addTransaction(mockTx1);
        assertFalse(TransactionService.removeTransaction("   "));
        assertEquals(1, TransactionService.getTransactionsForAccount("ACC1").size());
    }

    @Test
    @DisplayName("removeTransaction: Skips transaction with null ID during removal scan")
    void testRemoveTransaction_withTransactionHavingNullIdInList_handlesGracefully() {
        TransactionService.addTransaction(mockTxNullId); // This transaction has a null ID
        TransactionService.addTransaction(mockTx1); // TXN1

        // Attempt to remove a valid ID, should not be affected by mockTxNullId
        assertTrue(TransactionService.removeTransaction("TXN1"));
        // Attempt to remove using null, should not remove mockTxNullId by this specific call
        assertFalse(TransactionService.removeTransaction(null));

        List<AccountingTransaction> acc1Txs = TransactionService.getTransactionsForAccount("ACC1");
        // Expecting mockTxNullId to still be there if it was for ACC1
        // and if removeTransaction(null) didn't remove it.
        // The removeIf logic `tx != null && tx.getId() != null && txId.equals(tx.getId())` protects against this.
        assertTrue(acc1Txs.stream().anyMatch(tx -> tx == mockTxNullId), "Transaction with null ID should remain if not targeted by a valid ID removal.");
    }


    // --- getTransactionsForAccount Tests ---
    @Test
    @DisplayName("getTransactionsForAccount: Null accountId returns empty list")
    void testGetTransactionsForAccount_nullAccountId_returnsEmptyList() {
        TransactionService.addTransaction(mockTx1);
        assertTrue(TransactionService.getTransactionsForAccount(null).isEmpty());
    }

    @Test
    @DisplayName("getTransactionsForAccount: Blank accountId returns empty list")
    void testGetTransactionsForAccount_blankAccountId_returnsEmptyList() {
        TransactionService.addTransaction(mockTx1);
        assertTrue(TransactionService.getTransactionsForAccount("  ").isEmpty());
    }

    @Test
    @DisplayName("getTransactionsForAccount: No transactions exist returns empty list")
    void testGetTransactionsForAccount_noTransactionsExist_returnsEmptyList() {
        assertTrue(TransactionService.getTransactionsForAccount("ACC1").isEmpty());
    }

    @Test
    @DisplayName("getTransactionsForAccount: Returns only matching transactions")
    void testGetTransactionsForAccount_returnsOnlyMatchingTransactions() {
        TransactionService.addTransaction(mockTx1); // ACC1
        TransactionService.addTransaction(mockTx2); // ACC2
        TransactionService.addTransaction(mockTx3); // ACC1

        List<AccountingTransaction> acc1Txs = TransactionService.getTransactionsForAccount("ACC1");
        assertEquals(2, acc1Txs.size());
        assertTrue(acc1Txs.contains(mockTx1));
        assertTrue(acc1Txs.contains(mockTx3));

        List<AccountingTransaction> acc2Txs = TransactionService.getTransactionsForAccount("ACC2");
        assertEquals(1, acc2Txs.size());
        assertTrue(acc2Txs.contains(mockTx2));
    }

    @Test
    @DisplayName("getTransactionsForAccount: For accountId with no transactions returns empty list")
    void testGetTransactionsForAccount_forAccountIdWithNoTransactions_returnsEmptyList() {
        TransactionService.addTransaction(mockTx1); // ACC1
        assertTrue(TransactionService.getTransactionsForAccount("ACC_NO_TXNS").isEmpty());
    }

    @Test
    @DisplayName("getTransactionsForAccount: Handles null transaction in storage gracefully")
    void testGetTransactionsForAccount_handlesNullTransactionInStorage() {
        TransactionService.addTransaction(mockTx1);
        TransactionService.addTransaction(null); // Manually add null to simulate potential issue
        TransactionService.addTransaction(mockTx3);

        List<AccountingTransaction> acc1Txs = TransactionService.getTransactionsForAccount("ACC1");
        assertEquals(2, acc1Txs.size()); // Should skip the null transaction
        assertTrue(acc1Txs.contains(mockTx1));
        assertTrue(acc1Txs.contains(mockTx3));
    }

    @Test
    @DisplayName("getTransactionsForAccount: Handles transaction with null account gracefully")
    void testGetTransactionsForAccount_handlesTransactionWithNullAccount() {
        TransactionService.addTransaction(mockTx1); // ACC1
        TransactionService.addTransaction(mockTxNullAccount); // Has null account

        List<AccountingTransaction> acc1Txs = TransactionService.getTransactionsForAccount("ACC1");
        assertEquals(1, acc1Txs.size());
        assertSame(mockTx1, acc1Txs.get(0));
    }

    @Test
    @DisplayName("getTransactionsForAccount: Handles transaction with null account number gracefully")
    void testGetTransactionsForAccount_handlesTransactionWithNullAccountNumber() {
        TransactionService.addTransaction(mockTx1); // ACC1
        TransactionService.addTransaction(mockTxNullAccNum); // Account has null account number

        List<AccountingTransaction> acc1Txs = TransactionService.getTransactionsForAccount("ACC1");
        assertEquals(1, acc1Txs.size());
        assertSame(mockTx1, acc1Txs.get(0));

        // Also check that it doesn't get returned for a null query if that were possible
        // (current service logic requires non-blank accountId for query)
    }


    // --- clearAllTransactions Test ---
    @Test
    @DisplayName("clearAllTransactions: Empties the transaction list")
    void testClearAllTransactions_emptiesTransactionList() {
        TransactionService.addTransaction(mockTx1);
        TransactionService.addTransaction(mockTx2);
        // assertFalse(TransactionService.getTransactionsForAccount("ACC1").isEmpty()); // This would be false if list was global not just for ACC1

        TransactionService.clearAllTransactions();
        assertTrue(TransactionService.getTransactionsForAccount("ACC1").isEmpty());
        assertTrue(TransactionService.getTransactionsForAccount("ACC2").isEmpty());
    }
}
