
package nonprofitbookkeeping.model;

import lombok.Data;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;


/**
 * Mutable container that holds every {@link Account} in the company and
 * supports parent/child relationships.
 */
@Data public class ChartOfAccounts implements Serializable
{
	
	private static final long serialVersionUID = 6545569795380871696L;
	
	/**
	 * Flat list of <em>all</em> accounts, root and child alike.
	 * This list is managed internally and holds all accounts in the chart.
	 */
	@JsonProperty private final List<Account> chartOfAccounts = new ArrayList<>();
	
	/* ------------------------------------------------------------------ */
	/**
	 * Returns a list of every account that does not have a parent account.
	 * These are considered the top-level accounts in the chart.
	 * @return A new {@code List<Account>} containing all root accounts.
	 *         Returns an empty list if there are no root accounts or no accounts at all.
	 */
	public List<Account> getRootAccounts()
	{
		return this.chartOfAccounts.stream()
			.filter(a -> !a.hasParent())
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Returns a list of accounts that are direct children of the specified parent account.
	 * @param parent The {@link Account} whose children are to be retrieved.
	 * @return A new {@code List<Account>} containing the direct children of the parent.
	 *         Returns an empty list if the parent has no children or the parent is not in the chart.
	 * @throws NullPointerException if parent is null.
	 */
	public List<Account> getChildren(Account parent)
	{
		Objects.requireNonNull(parent, "parent cannot be null");
		return this.chartOfAccounts.stream()
			.filter(a -> parent.equals(a.getParentAccount()))
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Adds a top-level (root) account to the chart of accounts.
	 * The account's parent will be explicitly set to null.
	 * @param root The {@link Account} to add as a root account.
	 * @throws NullPointerException if root is null.
	 */
	public void addAccount(Account root)
	{
		Objects.requireNonNull(root, "account");
		root.setParentAccount(null);
		this.chartOfAccounts.add(root);
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Adds a child account under the specified parent account.
	 * @param parent The {@link Account} that will be the parent of the child.
	 * @param child The {@link Account} to add as a child.
	 * @throws NullPointerException if parent or child is null.
	 */
	public void addSubAccount(Account parent, Account child)
	{
		Objects.requireNonNull(parent, "parent");
		Objects.requireNonNull(child, "child");
		child.setParentAccount(parent);
		this.chartOfAccounts.add(child);
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Removes the specified {@code target} account and all its descendants (if any) from the chart.
	 * If the target account is null, the method does nothing.
	 * @param target The {@link Account} to remove.
	 */
	public void removeAccount(Account target)
	{
		if (target == null)
			return;
		/* remove children first (depth-first) */
		getChildren(target).forEach(this::removeAccount);
		this.chartOfAccounts.remove(target);
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Returns a comma-separated string of all account names in the chart.
	 * This is a convenience method for a quick overview of account names.
	 * @return A string containing all account names, separated by commas.
	 *         Returns an empty string if there are no accounts.
	 */
	public String getAccountNames()
	{
		return this.chartOfAccounts.stream()
			.map(Account::getName)
			.collect(Collectors.joining(", "));
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Builds and returns an unmodifiable Map where keys are account numbers
	 * and values are the corresponding {@link Account} objects.
	 * This map is useful for quick lookups of accounts by their number.
	 * If duplicate account numbers exist (which should ideally be prevented),
	 * the account that appears first in the internal list will be kept.
	 *
	 * @return An unmodifiable {@code Map<String, Account>}.
	 */
	
	public Map<String, Account> getAccountNumberToAccountDetails()
	{
		return this.chartOfAccounts.stream()
			.collect(Collectors.toUnmodifiableMap(Account::getAccountNumber,
				a -> a,
				(a, b) -> a)); // keep first
	}
	
	/**
	 * Compatibility setter for older JSON that stored accounts in a map
	 * keyed by account number. When such data is deserialized, this setter
	 * populates the internal {@link #chartOfAccounts} list so that the rest
	 * of the application can operate normally.
	 *
	 * @param map a map of account numbers to {@link Account} objects
	 */
	@JsonSetter("accountNumberToAccountDetails") public
		void setAccountNumberToAccountDetails(Map<String, Account> map)
	{
		this.chartOfAccounts.clear();
		
		if (map != null)
		{
			this.chartOfAccounts.addAll(map.values());
		}
		
	}
	
	/**
	 * Retrieves an account by its display name.
	 * This is a convenience lookup used when UI components store the
	 * account name instead of the unique account number.
	 *
	 * @param accountName the name of the account
	 * @return the matching {@link Account} or {@code null} if none found
	 */
	public Account getAccountByName(String accountName)
	{
		
		if (accountName == null || accountName.isBlank())
		{
			return null;
		}
		
		for (Account acc : this.chartOfAccounts)
		{
			
			if (accountName.equals(acc.getName()))
			{
				return acc;
			}
			
		}
		
		return null;
	}
	
	/**
	 * Retrieves a specific {@link Account} from the chart by its account number.
	 * This method provides a convenient way to look up an account using its unique identifier.
	 * It utilizes an internally generated map for efficient retrieval.
	 *
	 * @param accountNumber The account number of the account to retrieve.
	 *                      If null or blank, this method will return null.
	 * @return The {@link Account} object if found, or {@code null} if no account
	 *         matches the given account number, or if the input accountNumber is invalid.
	 */
	public Account getAccount(String accountNumber)
	{
		
		if (accountNumber == null || accountNumber.trim().isEmpty())
		{
			return null;
		}
		
		Map<String, Account> accountMap = this.getAccountNumberToAccountDetails();
		return accountMap.get(accountNumber);
	}
	
	/**
	 * Returns a list of all accounts contained in this chart of accounts.
	 * This list includes all accounts, whether they are root accounts or child accounts.
	 * <p>
	 * The returned list is a shallow copy of the internal list of accounts.
	 * Modifications to the returned list will not affect the chart of accounts itself.
	 * However, modifications to the {@link Account} objects within the list will affect
	 * the accounts stored in the chart.
	 * </p>
	 *
	 * @return A new {@code List<Account>} containing all accounts in the chart.
	 *         Returns an empty list if the chart of accounts has no accounts.
	 */
	public List<Account> getAccounts()
	{
		// this.chartOfAccounts is final and initialized, so it won't be null.
		return new ArrayList<>(this.chartOfAccounts);
	}
	
	/**
	 * Removes an account (and its descendants) from the chart of accounts using its account number.
	 * @param accountNumber The account number of the account to remove.
	 * @return {@code true} if the account was found (and thus removal was attempted),
	 *         {@code false} if no account with the given number was found (in which case no action is taken).
	 *         Note: The current implementation always returns true after attempting removal,
	 *         it might be more accurate to reflect if the account existed and was removed.
	 */
	public boolean removeAccount(String accountNumber)
	{
		Account accountToRemove = getAccount(accountNumber);
		
		if (accountToRemove != null)
		{
			removeAccount(accountToRemove);
			return true; // Account found and removal attempted
		}
		
		return false; // Account not found
	}
	
	
}
