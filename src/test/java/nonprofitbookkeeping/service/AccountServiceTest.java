
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.service.AccountService.AccountBalance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest
{
	
	@BeforeEach
		void setUp()
	{
		AccountService.clearAccounts();
	}
	
	// --- addAccount() Tests ---
	@Test
	@DisplayName("addAccount: Valid account should be added successfully") static
		void testAddAccount_validAccount_shouldBeAdded()
	{
		Account acc = new Account("A001", "Valid Account", AccountSide.DEBIT);
		AccountService.addAccount(acc);
		List<Account> accounts = AccountService.getAllAccounts();
		assertEquals(1, accounts.size());
		assertEquals("A001", accounts.get(0).getAccountNumber());
	}
	
	@Test
	@DisplayName("addAccount: Null account should be ignored")
		void testAddAccount_nullAccount_shouldBeIgnored()
	{
		AccountService.addAccount(null);
		assertTrue(AccountService.getAllAccounts().isEmpty());
	}
	
	@Test
	@DisplayName("addAccount: Account with null ID should be ignored")
		void testAddAccount_accountWithNullId_shouldBeIgnored()
	{
		Account acc = new Account(null, "Null ID Account", AccountSide.DEBIT);
		AccountService.addAccount(acc);
		assertTrue(AccountService.getAllAccounts().isEmpty());
	}
	
	@Test
	@DisplayName("addAccount: Account with blank ID should be ignored")
		void testAddAccount_accountWithBlankId_shouldBeIgnored()
	{
		Account acc = new Account(" ", "Blank ID Account", AccountSide.DEBIT);
		AccountService.addAccount(acc);
		assertTrue(AccountService.getAllAccounts().isEmpty());
	}
	
	@Test
	@DisplayName("addAccount: Multiple distinct accounts should all be added")
		void testAddAccount_multipleDistinctAccounts_shouldAllBeAdded()
	{
		Account acc1 = new Account("A001", "Account One", AccountSide.DEBIT);
		Account acc2 = new Account("A002", "Account Two", AccountSide.CREDIT);
		AccountService.addAccount(acc1);
		AccountService.addAccount(acc2);
		assertEquals(2, AccountService.getAllAccounts().size());
	}
	
	@Test
	@DisplayName("addAccount: Account with duplicate ID should also be added (current behavior)")
		void testAddAccount_duplicateId_shouldBeAdded()
	{
		Account acc1 = new Account("A001", "Account One", AccountSide.DEBIT);
		Account acc2 = new Account("A001", "Account Two with Same ID", AccountSide.CREDIT);
		AccountService.addAccount(acc1);
		AccountService.addAccount(acc2);
		assertEquals(2, AccountService.getAllAccounts().size());
	}
	
	// --- getAllAccounts() Tests ---
	@Test
	@DisplayName("getAllAccounts: When no accounts added, should return an empty list")
		void testGetAllAccounts_whenEmpty_shouldReturnEmptyList()
	{
		assertTrue(AccountService.getAllAccounts().isEmpty());
	}
	
	@Test
	@DisplayName("getAllAccounts: After adding accounts, should return list with those accounts")
		void testGetAllAccounts_withAddedAccounts_shouldReturnAllAccounts()
	{
		Account acc1 = new Account("A001", "Checking Account", AccountSide.DEBIT);
		Account acc2 = new Account("A002", "Savings Account", AccountSide.DEBIT);
		AccountService.addAccount(acc1);
		AccountService.addAccount(acc2);
		
		List<Account> accounts = AccountService.getAllAccounts();
		assertEquals(2, accounts.size());
		assertTrue(accounts.stream().anyMatch(a -> "A001".equals(a.getAccountNumber())));
		assertTrue(accounts.stream().anyMatch(a -> "A002".equals(a.getAccountNumber())));
	}
	
	@Test
	@DisplayName("getAllAccounts: Ensure returned list is a copy")
		void testGetAllAccounts_ensureReturnedListIsCopy()
	{
		Account acc1 = new Account("A001", "Test Account", AccountSide.DEBIT);
		AccountService.addAccount(acc1);
		
		List<Account> list1 = AccountService.getAllAccounts();
		assertNotNull(list1);
		assertEquals(1, list1.size());
		
		// Try to modify the returned list
		assertThrows(UnsupportedOperationException.class, () -> {
			// Collections.unmodifiableList() is not used, so direct modification would work
			// on ArrayList copy.
			// To test true immutability of the *service's* list, we check size after trying
			// to mod external copy.
			list1.add(new Account("A002", "Dummy", AccountSide.CREDIT));
		}, "If getAllAccounts returned an unmodifiable list, adding would throw UOE. If it's a mutable copy, this won't throw.");
		
		// If the above doesn't throw (because it's a mutable copy), this check is key:
		assertEquals(1, AccountService.getAllAccounts().size(),
			"Modifying the list returned by getAllAccounts should not affect the internal list.");
		
		List<Account> list2 = AccountService.getAllAccounts();
		assertNotSame(list1, list2, "getAllAccounts should return a new list instance each time.");
	}
	
	// --- getBalanceResults() Tests ---
	@Test
	@DisplayName("getBalanceResults: When no accounts, should return empty list")
		void testGetBalanceResults_whenNoAccounts_shouldReturnEmptyList()
	{
		assertTrue(AccountService.getBalanceResults().isEmpty());
	}
	
	@Test
	@DisplayName("getBalanceResults: With accounts, should return correct AccountBalance objects")
		void testGetBalanceResults_withAccounts_shouldReturnCorrectAccountBalances()
	{
		Account acc1 = new Account("B001", "Bal Account 1", AccountSide.DEBIT);
		acc1.setOpeningBalance(new BigDecimal("100.50"));
		Account acc2 = new Account("B002", "Bal Account 2", AccountSide.CREDIT);
		acc2.setOpeningBalance(new BigDecimal("-50.25"));
		Account acc3 = new Account("B003", "Zero Bal Account", AccountSide.DEBIT);
		acc3.setOpeningBalance(BigDecimal.ZERO);
		Account acc4 = new Account("B004", "Null Bal Account", AccountSide.DEBIT);
		acc4.setOpeningBalance(null); // totalAccountBalance in Account defaults null openingBalance
										// to BigDecimal.ZERO
		
		AccountService.addAccount(acc1);
		AccountService.addAccount(acc2);
		AccountService.addAccount(acc3);
		AccountService.addAccount(acc4);
		
		List<AccountBalance> balances = AccountService.getBalanceResults();
		assertEquals(4, balances.size());
		
		assertTrue(balances
			.contains(new AccountBalance("B001", "Bal Account 1", new BigDecimal("100.50"))));
		assertTrue(balances
			.contains(new AccountBalance("B002", "Bal Account 2", new BigDecimal("-50.25"))));
		assertTrue(
			balances.contains(new AccountBalance("B003", "Zero Bal Account", BigDecimal.ZERO)));
		assertTrue(
			balances.contains(new AccountBalance("B004", "Null Bal Account", BigDecimal.ZERO)));
	}
	
	// --- removeAccount() Tests ---
	@Test
	@DisplayName("removeAccount: Existing ID should remove account and return true")
		void testRemoveAccount_existingId_shouldRemoveAccountAndReturnTrue()
	{
		Account acc1 = new Account("R001", "ToRemove", AccountSide.DEBIT);
		AccountService.addAccount(acc1);
		AccountService.addAccount(new Account("R002", "ToKeep", AccountSide.CREDIT));
		
		assertTrue(AccountService.removeAccount("R001"));
		List<Account> accounts = AccountService.getAllAccounts();
		assertEquals(1, accounts.size());
		assertEquals("R002", accounts.get(0).getAccountNumber());
	}
	
	@Test
	@DisplayName("removeAccount: Non-existent ID should do nothing and return false")
		void testRemoveAccount_nonExistentId_shouldDoNothingAndReturnFalse()
	{
		AccountService.addAccount(new Account("R001", "Account", AccountSide.DEBIT));
		assertFalse(AccountService.removeAccount("R999"));
		assertEquals(1, AccountService.getAllAccounts().size());
	}
	
	@Test
	@DisplayName("removeAccount: Null ID should do nothing and return false")
		void testRemoveAccount_nullId_shouldDoNothingAndReturnFalse()
	{
		AccountService.addAccount(new Account("R001", "Account", AccountSide.DEBIT));
		assertFalse(AccountService.removeAccount(null));
		assertEquals(1, AccountService.getAllAccounts().size());
	}
	
	@Test
	@DisplayName("removeAccount: Blank ID should do nothing and return false")
		void testRemoveAccount_blankId_shouldDoNothingAndReturnFalse()
	{
		AccountService.addAccount(new Account("R001", "Account", AccountSide.DEBIT));
		assertFalse(AccountService.removeAccount("   "));
		assertEquals(1, AccountService.getAllAccounts().size());
	}
	
	@Test
	@DisplayName("removeAccount: Removing last account should result in empty list")
		void testRemoveAccount_removingLastAccount_shouldResultInEmptyList()
	{
		Account acc1 = new Account("R001", "OnlyAccount", AccountSide.DEBIT);
		AccountService.addAccount(acc1);
		
		assertTrue(AccountService.removeAccount("R001"));
		assertTrue(AccountService.getAllAccounts().isEmpty());
	}
	
	
	// --- clearAccounts() Tests ---
	@Test
	@DisplayName("clearAccounts: After adding and then clearing, lists should be empty")
		void testClearAccounts_shouldEmptyTheAccountList()
	{
		AccountService.addAccount(new Account("C001", "Clear Test", AccountSide.DEBIT));
		assertFalse(AccountService.getAllAccounts().isEmpty(),
			"List should not be empty before clear");
		
		AccountService.clearAccounts();
		
		assertTrue(AccountService.getAllAccounts().isEmpty(),
			"getAllAccounts should be empty after clearAccounts.");
		assertTrue(AccountService.getBalanceResults().isEmpty(),
			"getBalanceResults should be empty after clearAccounts.");
	}
	
}
