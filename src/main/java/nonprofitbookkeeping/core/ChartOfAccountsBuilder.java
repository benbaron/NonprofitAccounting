
package nonprofitbookkeeping.core;

import nonprofitbookkeeping.model.*;

/**
 * 
 */
public class ChartOfAccountsBuilder
{
	
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
		// The accountDetails field has been removed as it was not used by the build() method.
		// Consequently, the logic to add account details here has also been removed.
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
