
package nonprofitbookkeeping.service;

import java.util.List;

import nonprofitbookkeeping.model.AccountingTransaction;

import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.Set; // Added for listReconcilableAccounts
import java.util.HashSet; // Added for listReconcilableAccounts
import java.util.stream.Collectors; // Added for listReconcilableAccounts
import nonprofitbookkeeping.model.Account; // Added for type usage

/**
 * Provides functionality for reconciling account transactions.
 * This service can manage a list of unreconciled transactions on an instance basis
 * and also provides static utility methods for reconciliation tasks.
 */
public class ReconciliationService 
{
	// Instance-based storage for unreconciled transactions
	private List<AccountingTransaction> unreconciledTransactions;

	/**
	 * Constructs a new ReconciliationService instance, initializing an empty list
	 * for unreconciled transactions.
	 */
	public ReconciliationService() {
		this.unreconciledTransactions = new ArrayList<>();
	}

	/**
	 * Adds a transaction to the list of unreconciled transactions for this service instance.
	 * This method is primarily useful for setting up test scenarios or if transactions
	 * are managed externally and then added to this service for reconciliation.
	 *
	 * @param tx The {@link AccountingTransaction} to add. If null, the transaction is not added.
	 */
	public void addTransactionToReconcile(AccountingTransaction tx) {
		if (tx == null) {
			return;
		}
		if (this.unreconciledTransactions == null) { // Defensive, should be init by constructor
			this.unreconciledTransactions = new ArrayList<>();
		}
		this.unreconciledTransactions.add(tx);
	}
	
	// Existing static methods (left as-is)

	/**
	 * Fetches unreconciled entries for a specific account between the given date range.
	 *
	 * @param account the account name
	 * @param from the start date
	 * @param to the end date
	 * @return List of unreconciled entries
	 */
	public static List<String[]> getUnreconciledEntries(String account, String from, String to)
	{
		// In a real implementation, this would fetch data from a database or API
		List<String[]> entries = new ArrayList<>();
		
		// Simulating unreconciled entries (normally fetched from database)
		entries.add(new String[]
		{ "TXN001", "100.00", "Payment for services", "Unreconciled" });
		entries.add(new String[]
		{ "TXN002", "50.00", "Refund", "Unreconciled" });
		
		return entries;
	}
	
	/**
	 * Reconciles a specific entry by its transaction ID.
	 *
	 * @param txnId the transaction ID
	 * @return true if reconciliation was successful, false otherwise
	 */
	public static boolean reconcileEntry(String txnId)
	{
		// In a real implementation, the transaction ID would be marked as reconciled in
		// a database
		System.out.println("Reconciled transaction ID: " + txnId);
		return true;
	}


	/**
	 * Retrieves a list of unreconciled transactions for a specific account ID
	 * from the transactions managed by this service instance.
	 *
	 * @param accountId The account number (ID) to filter unreconciled transactions for.
	 * @return A new {@code List<AccountingTransaction>} containing unreconciled transactions
	 *         for the specified account. Returns an empty list if the accountId is null/blank,
	 *         no transactions are present, or no transactions match the accountId.
	 */
	public List<AccountingTransaction> getUnreconciled(String accountId)
	{
		if (accountId == null || accountId.trim().isEmpty() || this.unreconciledTransactions == null) {
			return Collections.emptyList();
		}
		return this.unreconciledTransactions.stream()
				.filter(tx -> tx.getAccount() != null && accountId.equals(tx.getAccount().getAccountNumber()))
				.collect(Collectors.toList());
	}

	/**
	 * Lists the account numbers of all unique accounts that have unreconciled transactions
	 * currently managed by this service instance.
	 *
	 * @return A {@code List<String>} of unique account numbers. Returns an empty list
	 *         if there are no unreconciled transactions.
	 */
	public List<String> listReconcilableAccounts()
	{
		if (this.unreconciledTransactions == null || this.unreconciledTransactions.isEmpty()) {
			return Collections.emptyList();
		}
		Set<String> accountIds = new HashSet<>();
		for (AccountingTransaction tx : this.unreconciledTransactions) {
			if (tx != null && tx.getAccount() != null && tx.getAccount().getAccountNumber() != null) {
				accountIds.add(tx.getAccount().getAccountNumber());
			}
		}
		return new ArrayList<>(accountIds);
	}

	/**
	 * Performs a simplified reconciliation process for a given account.
	 * This method removes transactions from the instance's list of unreconciled transactions
	 * if their ID is present in the {@code clearedTransactionIds} list and they belong
	 * to the specified {@code accountId}.
	 * <p>
	 * Note: The {@code statementDate} and {@code statementEndingBalance} parameters are
	 * included for future enhancements and are not used in the current simplified logic.
	 * </p>
	 *
	 * @param accountId The account number (ID) for which to reconcile transactions.
	 * @param statementDate The date of the bank statement (currently not used).
	 * @param statementEndingBalance The ending balance from the bank statement (currently not used).
	 * @param clearedTransactionIds A list of transaction IDs that have been cleared.
	 */
	public void reconcile(String accountId, String statementDate, BigDecimal statementEndingBalance, List<String> clearedTransactionIds)
	{
		if (accountId == null || accountId.trim().isEmpty() || clearedTransactionIds == null || this.unreconciledTransactions == null) {
			return;
		}
		
		this.unreconciledTransactions.removeIf(tx -> 
			tx != null && 
			tx.getAccount() != null && 
			accountId.equals(tx.getAccount().getAccountNumber()) &&
			tx.getId() != null && // Ensure transaction ID itself is not null before checking containment
			clearedTransactionIds.contains(tx.getId())
		);
	}
	
}
