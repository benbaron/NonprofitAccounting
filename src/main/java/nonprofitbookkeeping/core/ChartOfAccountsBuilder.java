
package nonprofitbookkeeping.core;

import nonprofitbookkeeping.api.AccountDetails;
import nonprofitbookkeeping.model.*;


import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class ChartOfAccountsBuilder
{
	private Set<AccountDetails> accountDetails = new HashSet<>();
	
	/**
	 * Constructs a ChartOfAccountsBuilder.
	 */
	public ChartOfAccountsBuilder()
	{
	}
	
	/**
	 * Creates a new ChartOfAccountsBuilder.
	 * @return A new ChartOfAccountsBuilder instance.
	 */
	public static ChartOfAccountsBuilder create()
	{
		return new ChartOfAccountsBuilder();
	}
	
	/**
	 * Adds a new account to the chart of accounts.
	 * @param accountNumber The account number.
	 * @param name The name of the account.
	 * @param increaseSide The side where the account increases (Debit or Credit).
	 * @return This ChartOfAccountsBuilder instance for chaining.
	 */
	public ChartOfAccountsBuilder addAccount(	String accountNumber, 
	                                         	String name,
												AccountSide increaseSide)
	{
		AccountDetails accountDetails1 = 
			new AccountDetailsImpl(accountNumber, name, increaseSide);
		this.accountDetails.add(accountDetails1);
		return this;
	}
	
	/**
	 * Builds the ChartOfAccounts.
	 * @return A new ChartOfAccounts instance.
	 * Note: The current implementation returns a new ChartOfAccounts without using the added accountDetails.
	 * This might be a placeholder for future implementation.
	 */
	public ChartOfAccounts build()
	{
		return new ChartOfAccounts();
	}
	
}
