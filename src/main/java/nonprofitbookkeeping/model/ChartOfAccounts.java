
package nonprofitbookkeeping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mutable container that holds every {@link Account} in the company and
 * supports parent/child relationships.
 */
@Data
public class ChartOfAccounts implements Serializable
{
	
	private static final long serialVersionUID = 6545569795380871696L;
	
	/** flat list of <em>all</em> accounts, root and child alike */
	@JsonProperty private final List<Account> chartOfAccounts = new ArrayList<>();
	
	/* ------------------------------------------------------------------ */
	/** Returns every account that has no parent. */
	public List<Account> getRootAccounts()
	{
		return this.chartOfAccounts.stream()
			.filter(a -> !a.hasParent())
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	/* ------------------------------------------------------------------ */
	/** Returns direct children of {@code parent}. */
	public List<Account> getChildren(Account parent)
	{
		return this.chartOfAccounts.stream()
			.filter(a -> parent.equals(a.getParentAccount()))
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	/* ------------------------------------------------------------------ */
	/** Adds a top-level (root) account. */
	public void addAccount(Account root)
	{
		Objects.requireNonNull(root, "account");
		root.setParentAccount(null);
		this.chartOfAccounts.add(root);
	}
	
	/* ------------------------------------------------------------------ */
	/** Adds {@code child} under the given parent. */
	public void addSubAccount(Account parent, Account child)
	{
		Objects.requireNonNull(parent, "parent");
		Objects.requireNonNull(child, "child");
		child.setParentAccount(parent);
		this.chartOfAccounts.add(child);
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Removes {@code target} and all its descendants (if any) from the chart.
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
	/** Convenience—comma-separated list of every account’s name. */
	public String getNames()
	{
		return this.chartOfAccounts.stream()
			.map(Account::getName)
			.collect(Collectors.joining(", "));
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Builds a <kbd>Map accountNumber → Account</kbd> for quick lookup.
	 *
	 * @return unmodifiable map keyed by {@link Account#getAccountNumber()}.
	 */
	public Map<String, Account> getAccountNumberToAccountDetails()
	{
		return this.chartOfAccounts.stream()
			.collect(Collectors.toUnmodifiableMap(Account::getAccountNumber,
				a -> a,
				(a, b) -> a)); // keep first
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
		if (accountNumber == null || accountNumber.trim().isEmpty()) {
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
	
}
