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

import nonprofitbookkeeping.model.Ledger;

import nonprofitbookkeeping.model.Account;

/**
 * Service class for managing {@link Account} objects.
 * Provides methods for retrieving and manipulating accounts.
 * Accounts are stored in an in-memory list.
 */
/**
 * Service class providing static methods for managing {@link Account} objects.
 * This includes operations such as retrieving all accounts, calculating account balances,
 * adding new accounts, removing accounts, and clearing the account list.
 * Accounts are stored in a static in-memory list, making this service suitable for
 * scenarios where a shared, application-wide account list is appropriate.
 * For more complex applications, consider instance-based services and proper dependency injection.
 */
public class AccountService
{
	/**
	 * In-memory list to store {@link Account} objects.
	 * This static list serves as the central storage for accounts managed by this service.
	 */
	private static List<Account> accounts = new ArrayList<>();

	/**
	 * Represents the balance of a specific account, including its ID, name, and balance amount.
	 * This is a Java Record, providing a concise way to group related data.
	 *
	 * @param accountId The unique identifier (account number) of the account.
	 * @param accountName The name of the account.
	 * @param balance The current financial balance of the account.
	 */
	public static record AccountBalance(String accountId, String accountName, BigDecimal balance) {}

	/**
         * Retrieves the balance for all accounts currently managed by this service.
         * This method iterates through all stored accounts, computes their individual balances
         * using {@link Account#totalAccountBalance(Ledger)}, and returns a list of
         * {@link AccountBalance} records.
	 *
	 * @return A {@code List<AccountBalance>} containing the ID, name, and balance
	 *         for each account. Returns an empty list if no accounts are stored or available.
	 */
        public static List<AccountBalance> getBalanceResults(Ledger ledger)
        {
                List<Account> allAccounts = getAllAccounts();
                if (allAccounts.isEmpty()) {
                        return Collections.emptyList();
                }

                return allAccounts.stream()
                                .map(account -> new AccountBalance(
                                                account.getAccountNumber(),
                                                account.getName(),
                                                account.totalAccountBalance(ledger)))
                                .collect(Collectors.toList());
        }

	/**
	 * Retrieves all accounts currently managed by this service.
	 *
	 * @return A new {@code List<Account>} containing all stored accounts.
	 *         Returns an empty list if no accounts are currently stored.
	 *         The returned list is a shallow copy of the internal list, so modifications
	 *         to the list itself (e.g., adding/removing elements) will not affect the
	 *         service's internal storage. However, modifications to the {@link Account}
	 *         objects within the list will affect the original objects.
	 */
	public static List<Account> getAllAccounts()
	{
		// Return a copy to prevent external modification of the internal list structure
		return new ArrayList<>(accounts);
	}

	/**
	 * Clears all accounts from the internal in-memory storage.
	 * This method is useful for resetting the state, for example, during testing
	 * or when loading a new set of company data.
	 */
	public static void clearAccounts() {
		if (accounts != null) {
			accounts.clear();
		} else {
			// This case should ideally not be reached if 'accounts' is always initialized.
			accounts = new ArrayList<>();
		}
	}

	/**
	 * Adds a new {@link Account} to the in-memory storage.
	 * The account is not added if it is null, or if its account number is null or blank.
	 * Note: This method does not check for duplicate account numbers; the underlying list
	 * will allow accounts with the same account number if added.
	 *
	 * @param account The {@link Account} object to be added.
	 */
	public static void addAccount(Account account) {
		if (account == null || account.getAccountNumber() == null || account.getAccountNumber().trim().isEmpty()) {
			// Optionally, log a warning or throw an IllegalArgumentException here
			return;
		}
		if (accounts == null) { // Defensive check, though 'accounts' is initialized at declaration
			accounts = new ArrayList<>();
		}
		accounts.add(account);
	}

	/**
	 * Removes an account from the in-memory storage based on its account ID (account number).
	 * No action is taken if the provided {@code accountId} is null or blank, or if the internal
	 * accounts list is not initialized.
	 *
	 * @param accountId The unique identifier (account number) of the {@link Account} to be removed.
	 * @return {@code true} if an account with the specified ID was found and removed;
	 *         {@code false} otherwise (e.g., if {@code accountId} is invalid, or no such account exists).
	 */
	public static boolean removeAccount(String accountId) {
		if (accountId == null || accountId.trim().isEmpty() || accounts == null) {
			return false;
		}
		return accounts.removeIf(account -> accountId.equals(account.getAccountNumber()));
	}
	
}
