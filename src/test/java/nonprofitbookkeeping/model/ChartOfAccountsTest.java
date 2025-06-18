package nonprofitbookkeeping.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
// No Mockito needed if Account can be directly instantiated easily.
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
// import java.math.BigDecimal; // Not directly needed for these specific tests

import static org.junit.jupiter.api.Assertions.*;

// @ExtendWith(MockitoExtension.class) // Only if using Mockito mocks
class ChartOfAccountsTest {

    private ChartOfAccounts chart;

    // Sample accounts for testing
    private Account acc101;
    private Account acc202;
    private Account acc303;

    @BeforeEach
    void setUp() {
        this.chart = new ChartOfAccounts();

        // Initialize sample accounts - using direct instantiation
        // Assuming AccountSide is an accessible enum. If not, this might need adjustment or mocking.
        this.acc101 = new Account("101", "Cash", AccountSide.DEBIT);
        this.acc202 = new Account("202", "Accounts Payable", AccountSide.CREDIT);
        this.acc303 = new Account("303", "Revenue", AccountSide.CREDIT);
    }

    // --- Tests for getAccount(String accountNumber) ---

    @Test
    @DisplayName("getAccount: Retrieve existing account by correct number")
    void testGetAccount_existingAccount_returnsCorrectAccount() {
        this.chart.addAccount(this.acc101);
        this.chart.addAccount(this.acc202);

        Account foundAccount = this.chart.getAccount("101");
        assertNotNull(foundAccount);
        assertSame(this.acc101, foundAccount, "Should return the same Account instance for '101'.");
        assertEquals("Cash", foundAccount.getName());

        Account foundAccount2 = this.chart.getAccount("202");
        assertNotNull(foundAccount2);
        assertSame(this.acc202, foundAccount2, "Should return the same Account instance for '202'.");
    }

    @Test
    @DisplayName("getAccount: Retrieve with non-existent account number returns null")
    void testGetAccount_nonExistentAccountNumber_returnsNull() {
        this.chart.addAccount(this.acc101);
        assertNull(this.chart.getAccount("999"), "Should return null for a non-existent account number.");
    }

    @Test
    @DisplayName("getAccount: Retrieve with null account number returns null")
    void testGetAccount_nullAccountNumber_returnsNull() {
        this.chart.addAccount(this.acc101);
        assertNull(this.chart.getAccount(null), "Should return null when accountNumber is null.");
    }

    @Test
    @DisplayName("getAccount: Retrieve with blank account number returns null")
    void testGetAccount_blankAccountNumber_returnsNull() {
        this.chart.addAccount(this.acc101);
        assertNull(this.chart.getAccount("   "), "Should return null when accountNumber is blank.");
    }

    @Test
    @DisplayName("getAccount: After adding multiple accounts, retrieves correctly")
    void testGetAccount_afterAddingMultiple_retrievesCorrectly() {
        this.chart.addAccount(this.acc101);
        this.chart.addAccount(this.acc202);
        this.chart.addAccount(this.acc303);

        assertSame(this.acc202, this.chart.getAccount("202"));
        assertSame(this.acc303, this.chart.getAccount("303"));
    }

    // --- Tests for getAccounts() ---

    @Test
    @DisplayName("getAccounts: Empty chart returns an empty list")
    void testGetAccounts_emptyChart_returnsEmptyList() {
        List<Account> accounts = this.chart.getAccounts();
        assertNotNull(accounts, "getAccounts() should not return null for an empty chart.");
        assertTrue(accounts.isEmpty(), "List should be empty for a newly initialized chart.");
    }

    @Test
    @DisplayName("getAccounts: With one account, returns list with that one account")
    void testGetAccounts_withOneAccount_returnsListWithOneAccount() {
        this.chart.addAccount(this.acc101);
        List<Account> accounts = this.chart.getAccounts();
        assertEquals(1, accounts.size());
        assertSame(this.acc101, accounts.get(0), "The list should contain the added account.");
    }

    @Test
    @DisplayName("getAccounts: With multiple accounts, returns list with all accounts")
    void testGetAccounts_withMultipleAccounts_returnsListWithAllAccounts() {
        this.chart.addAccount(this.acc101);
        this.chart.addAccount(this.acc202);
        this.chart.addAccount(this.acc303);

        List<Account> accounts = this.chart.getAccounts();
        assertEquals(3, accounts.size());
        assertTrue(accounts.contains(this.acc101), "List should contain acc101.");
        assertTrue(accounts.contains(this.acc202), "List should contain acc202.");
        assertTrue(accounts.contains(this.acc303), "List should contain acc303.");
    }

    @Test
    @DisplayName("getAccounts: Returned list is a defensive copy")
    void testGetAccounts_isDefensiveCopy() {
        this.chart.addAccount(this.acc101);

        List<Account> accountsList1 = this.chart.getAccounts();
        int originalSize = accountsList1.size();
        assertNotNull(accountsList1);

        // Attempt to modify the retrieved list
        Account newAccInCopy = new Account("999", "Dummy Account", AccountSide.DEBIT);
        try {
            accountsList1.add(newAccInCopy);
        } catch (UnsupportedOperationException e) {
            // This would happen if Collections.unmodifiableList was used.
            // ChartOfAccounts returns new ArrayList<>() which is modifiable.
        }

        // Verify the original list in ChartOfAccounts is unchanged
        List<Account> accountsList2 = this.chart.getAccounts();
        assertEquals(originalSize, accountsList2.size(), "Modifying the returned list should not affect the internal list in ChartOfAccounts.");
        assertFalse(accountsList2.contains(newAccInCopy), "Internal list should not contain the account added to the copy.");

        // Also ensure list1 and list2 are different instances
        assertNotSame(accountsList1, accountsList2, "getAccounts() should return a new list instance each time.");
    }

    @Test
    @DisplayName("Legacy map setter populates accounts and fills account numbers")
    void testSetAccountNumberToAccountDetails_compatibility() {
        ChartOfAccounts legacyChart = new ChartOfAccounts();

        Map<String, Object> map = new LinkedHashMap<>();
        Account a1 = new Account();
        a1.setName("Legacy A1");
        // account number intentionally missing
        map.put("1000", a1);

        Account a2 = new Account();
        a2.setName("Legacy A2");
        map.put("2000", a2);

        legacyChart.setAccountNumberToAccountDetails(map);

        assertEquals(2, legacyChart.getAccounts().size());
        Account fetched1 = legacyChart.getAccount("1000");
        Account fetched2 = legacyChart.getAccount("2000");

        assertNotNull(fetched1);
        assertEquals("1000", fetched1.getAccountNumber());
        assertEquals("Legacy A1", fetched1.getName());

        assertNotNull(fetched2);
        assertEquals("2000", fetched2.getAccountNumber());
        assertEquals("Legacy A2", fetched2.getName());
    }
}
