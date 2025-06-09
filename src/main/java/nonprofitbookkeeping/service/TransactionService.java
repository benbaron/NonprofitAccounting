/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * TransactionService.java
 * TransactionService
 */
package nonprofitbookkeeping.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import nonprofitbookkeeping.model.AccountingTransaction;

/**
 * Service class for managing {@link AccountingTransaction} objects globally.
 * This service provides an in-memory storage solution for all transactions
 * across the system and includes methods for adding, retrieving, and removing transactions.
 * All methods and storage in this service are static.
 */
/**
 * Service class providing static methods for managing a global list of {@link AccountingTransaction} objects.
 * This service offers an in-memory storage solution for all transactions across the application,
 * with functionalities to add, retrieve (e.g., by account ID), remove, and clear transactions.
 * As all methods and storage are static, this service acts as a singleton-like global transaction manager.
 */
public class TransactionService
{
	/**
	 * Static in-memory list to store all {@link AccountingTransaction} objects globally.
	 * Initialized as an empty ArrayList.
	 */
	private static List<AccountingTransaction> allTransactions = new ArrayList<>();

	/**
	 * Retrieves all transactions associated with a specific account ID from the global list.
	 * <p>
	 * This method filters transactions based on the account number of their primary associated account.
	 * It safely handles cases where transactions in the list, their associated accounts,
	 * or account numbers might be null.
	 * </p>
	 *
	 * @param accountId The account number (ID) to filter transactions for.
	 *                  If null or blank, an empty list is returned.
	 * @return A new {@code List<AccountingTransaction>} containing transactions for the specified account.
	 *         Returns an empty list if {@code accountId} is invalid, no transactions are globally stored,
	 *         or no transactions match the given {@code accountId}.
	 */
	public static List<AccountingTransaction> getTransactionsForAccount(String accountId)
	{
		if (accountId == null || accountId.trim().isEmpty() || allTransactions == null) {
			return Collections.emptyList();
		}
		return allTransactions.stream()
				.filter(tx -> {
					if (tx == null || tx.getAccount() == null || tx.getAccount().getAccountNumber() == null) {
						return false; // Skip invalid or incomplete transactions
					}
					return accountId.equals(tx.getAccount().getAccountNumber());
				})
				.collect(Collectors.toList());
	}

	/**
	 * Adds a new {@link AccountingTransaction} to the global list of transactions.
	 * If the provided transaction {@code tx} is null, it is not added, and the method returns silently.
	 *
	 * @param tx The {@link AccountingTransaction} object to be added.
	 */
	public static void addTransaction(AccountingTransaction tx) {
		if (tx == null) {
			// Optionally, log a warning here if tx is null
			return;
		}
		if (allTransactions == null) { // Defensive check, though initialized at declaration
			allTransactions = new ArrayList<>();
		}
		allTransactions.add(tx);
	}

	/**
	 * Removes a transaction from the global list based on its booking date timestamp,
	 * which is used here as a transaction identifier.
	 * If the provided {@code txId} (booking date timestamp) is null, or if the global transaction list
	 * is not initialized, no action is taken and {@code false} is returned.
	 *
	 * @param txId The booking date timestamp (as a Long) of the transaction to be removed.
	 * @return {@code true} if a transaction with the matching timestamp was found and removed,
	 *         {@code false} otherwise (including if {@code txId} is null or no such transaction was found).
	 */
	public static boolean removeTransaction(Long txId) {
		if (txId == null || allTransactions == null) { // Added null check for txId
			return false;
		}
		return allTransactions.removeIf(tx ->
			tx != null &&
			txId.equals(tx.getBookingDateTimestamp()) // Assumes getBookingDateTimestamp() returns Long
		);
	}

	/**
	 * Clears all transactions from the global in-memory storage.
	 * This method is primarily intended for scenarios like resetting application state
	 * or for testing purposes to ensure a clean state between test executions.
	 */
	public static void clearAllTransactions() {
		if (allTransactions != null) {
			allTransactions.clear();
		} else {
			// This case should ideally not be reached if 'allTransactions' is always initialized.
			allTransactions = new ArrayList<>();
		}
	}
}
