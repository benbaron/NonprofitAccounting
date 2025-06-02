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
public class TransactionService
{
	/**
	 * Static in-memory list to store all AccountingTransaction objects.
	 */
	private static List<AccountingTransaction> allTransactions = new ArrayList<>();

	/**
	 * Retrieves all transactions associated with a specific account ID.
	 * <p>
	 * This method filters the global list of transactions. It handles cases where
	 * transactions in the list, their associated accounts, or account numbers might be null.
	 * </p>
	 *
	 * @param accountId The account number (ID) to filter transactions for.
	 * @return A new {@code List<AccountingTransaction>} containing transactions
	 *         for the specified account. Returns an empty list if the accountId is null/blank,
	 *         no transactions are present, or no transactions match the accountId.
	 */
	public static List<AccountingTransaction> getTransactionsForAccount(String accountId)
	{
		if (accountId == null || accountId.trim().isEmpty() || allTransactions == null) {
			return Collections.emptyList();
		}
		return allTransactions.stream()
				.filter(tx -> {
					if (tx == null || tx.getAccount() == null || tx.getAccount().getAccountNumber() == null) {
						return false;
					}
					return accountId.equals(tx.getAccount().getAccountNumber());
				})
				.collect(Collectors.toList());
	}

	/**
	 * Adds a new transaction to the global list of transactions.
	 * If the provided transaction is null, it is not added.
	 *
	 * @param tx The {@link AccountingTransaction} object to be added.
	 */
	public static void addTransaction(AccountingTransaction tx) {
		if (tx == null) {
			// Optionally, log a warning
			return;
		}
		if (allTransactions == null) { // Defensive check, though initialized at declaration
			allTransactions = new ArrayList<>();
		}
		allTransactions.add(tx);
	}

	/**
	 * Removes a transaction from the global list based on its transaction ID.
	 * If the provided transaction ID is null or blank, no action is taken.
	 *
	 * @param txId The unique identifier of the transaction to be removed.
	 * @return {@code true} if a transaction was removed as a result of this call,
	 *         {@code false} otherwise (including if the txId is invalid or no
	 *         such transaction was found).
	 */
	public static boolean removeTransaction(Long txId) {
		if (allTransactions == null) {
			return false;
		}
		return allTransactions.removeIf(tx ->
			tx != null &&
			txId.equals(tx.getBookingDateTimestamp())
		);
	}

	/**
	 * Clears all transactions from the global in-memory storage.
	 * This method is primarily intended for testing purposes to ensure a clean state
	 * between test executions.
	 */
	public static void clearAllTransactions() {
		if (allTransactions != null) {
			allTransactions.clear();
		} else {
			// Should not happen if initialized at declaration, but defensive
			allTransactions = new ArrayList<>();
		}
	}
}
