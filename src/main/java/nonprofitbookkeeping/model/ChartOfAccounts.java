
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
@AllArgsConstructor
@NoArgsConstructor public class ChartOfAccounts implements Serializable
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
	
}
