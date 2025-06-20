/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * AccountService.java
 * AccountService
 */
package nonprofitbookkeeping.service;

import java.math.BigDecimal; // Added for AccountBalance and method return type
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors; // Added for cleaner list transformation

import nonprofitbookkeeping.model.Account;

/**
 * Service class for managing {@link Account} objects.
 * Provides methods for retrieving and manipulating accounts.
 * Accounts are stored in an in-memory list.
 */
public class AccountService
{
	/**
	 * In-memory list to store Account objects.
	 */
	private static List<Account> accounts = new ArrayList<>();

	/**
	 * Represents the balance of a specific account.
	 * This record is used to return structured account balance information.
	 *
	 * @param accountId The unique identifier of the account.
	 * @param accountName The name of the account.
	 * @param balance The current balance of the account.
	 */
	public static record AccountBalance(String accountId, String accountName, BigDecimal balance) {}

	/**
	 * Retrieves the balance for all accounts.
	 * This method iterates through all stored accounts, computes their balances,
	 * and returns a list of {@link AccountBalance} objects.
	 *
	 * @return A {@code List<AccountBalance>} containing the ID, name, and balance
	 *         for each account. Returns an empty list if no accounts are stored.
	 */
	public static List<AccountBalance> getBalanceResults()
	{
		List<Account> allAccounts = getAllAccounts();
		if (allAccounts.isEmpty()) {
			return Collections.emptyList();
		}

		return allAccounts.stream()
				.map(account -> new AccountBalance(
						account.getAccountNumber(),
						account.getName(),
						account.totalAccountBalance()))
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves all accounts currently stored in the system.
	 *
	 * @return A new {@code List<Account>} containing all stored accounts.
	 *         Returns an empty list if no accounts are present.
	 *         This is a copy of the internal list, so modifications to the
	 *         returned list will not affect the internal storage.
	 */
        public static List<Account> getAllAccounts()
        {
                // Return an unmodifiable copy to prevent callers from mutating
                // the internal list or assuming the returned list is mutable.
                // Using Collections.unmodifiableList ensures attempts to modify
                // the returned list throw an UnsupportedOperationException,
                // matching the expectations of the unit tests.
                return Collections.unmodifiableList(new ArrayList<>(accounts));
        }

	/**
	 * Clears all accounts from the in-memory storage.
	 * This method is primarily intended for testing purposes to ensure a clean state
	 * between test executions.
	 */
	public static void clearAccounts() {
		if (accounts != null) {
			accounts.clear();
		} else {
			// Should not happen if initialized at declaration, but defensive
			accounts = new ArrayList<>();
		}
	}

	/**
	 * Adds a new account to the in-memory storage.
	 * If the provided account is null, or its account number is null or blank,
	 * the account is not added. This implementation allows duplicate accounts
	 * if their account numbers are the same (as List allows duplicates).
	 *
	 * @param account The {@link Account} object to be added.
	 */
	public static void addAccount(Account account) {
		if (account == null || account.getAccountNumber() == null || account.getAccountNumber().trim().isEmpty()) {
			// Optionally, log a warning here
			return;
		}
		if (accounts == null) { // Defensive check, though initialized at declaration
			accounts = new ArrayList<>();
		}
		accounts.add(account);
	}

	/**
	 * Removes an account from the in-memory storage based on its account ID.
	 * If the provided account ID is null or blank, no action is taken.
	 *
	 * @param accountId The unique identifier (account number) of the account to be removed.
	 * @return {@code true} if an account was removed as a result of this call,
	 *         {@code false} otherwise (including if the accountId is invalid or no
	 *         such account was found).
	 */
	public static boolean removeAccount(String accountId) {
		if (accountId == null || accountId.trim().isEmpty() || accounts == null) {
			return false;
		}
		return accounts.removeIf(account -> accountId.equals(account.getAccountNumber()));
	}
	
}
