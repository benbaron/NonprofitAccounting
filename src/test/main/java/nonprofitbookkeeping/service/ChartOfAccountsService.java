
package nonprofitbookkeeping.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import nonprofitbookkeeping.model.*;

/**
 * Thin façade over {@link ChartOfAccounts} that provides CRUD helpers used by
 * the UI layer as well as import/export utilities. It wraps a {@link ChartOfAccounts}
 * instance and provides higher-level operations or simplified access to its functionalities.
 */
public class ChartOfAccountsService
{
	
	/** The underlying {@link ChartOfAccounts} model instance this service operates on. */
	private ChartOfAccounts coa;
	
	/**
	 * Constructs a new {@code ChartOfAccountsService} that will operate on the given
	 * {@link ChartOfAccounts} instance.
	 *
	 * @param coa The {@link ChartOfAccounts} model to be managed by this service. Must not be null.
	 * @throws NullPointerException if {@code coa} is null.
	 */
	public ChartOfAccountsService(ChartOfAccounts coa)
	{
		
		if (coa == null)
		{
			throw new NullPointerException("ChartOfAccounts cannot be null.");
		}
		
		this.coa = coa;
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Retrieves a list of all root accounts (accounts without a parent) from the chart of accounts.
	 * Delegates to {@link ChartOfAccounts#getRootAccounts()}.
	 *
	 * @return A {@link List} of root {@link Account} objects.
	 */
	public List<Account> roots()
	{
		return this.coa.getRootAccounts();
	}
	
	/**
	 * Retrieves a list of direct children for the given parent account.
	 * Delegates to {@link ChartOfAccounts#getChildren(Account)}.
	 *
	 * @param a The parent {@link Account}.
	 * @return A {@link List} of child {@link Account} objects.
	 */
	public List<Account> childrenOf(Account a)
	{
		return this.coa.getChildren(a);
	}
	
	/**
	 * Adds a new root account to the chart of accounts.
	 * Delegates to {@link ChartOfAccounts#addAccount(Account)}.
	 *
	 * @param det The {@link Account} to add as a root account.
	 */
	public void addRoot(Account det)
	{
		this.coa.addAccount(det);
	}
	
	/**
	 * Adds a child account under a specified parent account in the chart of accounts.
	 * Delegates to {@link ChartOfAccounts#addSubAccount(Account, Account)}.
	 *
	 * @param p The parent {@link Account}.
	 * @param det The child {@link Account} to add.
	 */
	public void addChild(Account p, Account det)
	{
		this.coa.addSubAccount(p, det);
	}
	
	/**
	 * Deletes the specified account and all its descendants from the chart of accounts.
	 * Delegates to {@link ChartOfAccounts#removeAccount(Account)}.
	 * 
	 * @param target The {@link Account} to delete. If null, the underlying method may handle it or throw an error.
	 */
	public void delete(Account target)
	{
		this.coa.removeAccount(target);
	}
	
	/**
	 * Updates the properties of a target {@link Account} object.
	 * Sets the name, account type, and opening balance of the target account.
	 * 
	 * @param tgt The {@link Account} object to update. Must not be null.
	 * @param name The new name for the account.
	 * @param accountType The new {@link AccountType} for the account.
	 * @param openingBal The new opening balance for the account.
	 * @throws NullPointerException if {@code tgt} is null.
	 */
	public static void update(	Account tgt, 
	                          	String name, 
	                          	AccountType accountType,
								BigDecimal openingBal)
	{
		
		if (tgt == null)
		{
			throw new NullPointerException("Target account (tgt) cannot be null for update.");
		}
		
		tgt.setName(name);
		tgt.setAccountType(accountType);
		tgt.setOpeningBalance(openingBal);
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Finds the first account whose {@linkplain Account#getAccountNumber() account number}
	 * matches the provided {@code num}.
	 * This search is performed on the current state of the chart of accounts.
	 *
	 * @param num The account number to search for.
	 * @return An {@link Optional} containing the found {@link Account} if present,
	 *         otherwise an empty {@link Optional}.
	 */
	public Optional<Account> findByNumber(String num)
	{
		
		if (num == null)
		{
			return Optional.empty();
		}
		
		return this.coa.createAccountNumberMap().asMap().values().stream()
			.filter(a -> num.equals(a.getAccountNumber())) // Ensure num.equals to avoid NPE if
			                                               // account number can be null
			.findFirst();
	}
	
	
	/**
	 * Returns a direct reference to the underlying {@link ChartOfAccounts} model instance
	 * that this service is managing.
	 *
	 * @return The wrapped {@link ChartOfAccounts} object.
	 */
	public ChartOfAccounts asChart()
	{
		return this.coa;
	}
	
	/**
	 * Replaces the underlying {@link ChartOfAccounts} instance this service
	 * operates on.
	 *
	 * @param newChart the new chart of accounts to manage
	 * @throws NullPointerException if {@code newChart} is null
	 */
	public void setChart(ChartOfAccounts newChart)
	{
		
		if (newChart == null)
		{
			throw new NullPointerException("ChartOfAccounts cannot be null.");
		}
		
		this.coa = newChart;
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Replaces all accounts in the current chart of accounts with those from
	 * an imported {@link ChartOfAccounts} object. This operation first clears all existing
	 * root accounts (and their descendants) from the current chart, then performs a deep copy
	 * of each root account (and its entire subtree) from the {@code imported} chart.
	 * Child relationships from the imported chart are preserved in the current chart.
	 *
	 * @param imported The {@link ChartOfAccounts} to copy from. Must not be {@code null}.
	 * @throws NullPointerException if {@code imported} is null.
	 */
	public void replaceChart(ChartOfAccounts imported)
	{
		
		if (imported == null)
		{
			throw new NullPointerException("Imported ChartOfAccounts cannot be null.");
		}
		
		/* 1) remove every root from the current chart */
		// Create a copy of the list to avoid ConcurrentModificationException if
		// underlying removeAccount modifies the list being iterated
		new java.util.ArrayList<>(this.coa.getRootAccounts()).forEach(this.coa::removeAccount);
		
		/* 2) deep-copy each root (and its subtree) from the imported chart */
		imported.getRootAccounts().forEach(root -> copyRecursive(imported, root, null));
	}
	
	/**
	 * Recursively copies an account ({@code src}) and its entire subtree of child accounts
	 * into the current chart of accounts managed by this service.
	 * If {@code parentDest} is null, the {@code src} account is added as a root account.
	 * Otherwise, it's added as a child of {@code parentDest}.
	 * This is a helper method primarily used by {@link #replaceChart(ChartOfAccounts)}.
	 *
	 * @param src The source {@link Account} to copy.
	 * @param parentDest The destination parent {@link Account} in the current chart under which
	 *                   the {@code src} account (and its children) should be copied.
	 *                   If null, {@code src} is copied as a root account.
	 */
	private void copyRecursive(ChartOfAccounts sourceChart, Account src, Account parentDest)
	{
		/* clone basic fields */
		Account clone = new Account();
		clone.setAccountNumber(src.getAccountNumber());
		clone.setName(src.getName());
		clone.setAccountType(src.getAccountType());
		clone.setOpeningBalance(src.getOpeningBalance());
		
		/* attach to chart */
		if (parentDest == null)
			this.coa.addAccount(clone);
		else
			this.coa.addSubAccount(parentDest, clone);
		
		/* recurse into children */
		for (Account child : sourceChart.getChildren(src))
		{
			copyRecursive(sourceChart, child, clone);
		}
		
	}
	
	
}
