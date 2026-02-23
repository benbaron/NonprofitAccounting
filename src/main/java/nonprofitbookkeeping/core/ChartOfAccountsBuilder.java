package nonprofitbookkeeping.core;

import nonprofitbookkeeping.model.*;

// TODO: Auto-generated Javadoc
/**
 * The Class ChartOfAccountsBuilder.
 */
public class ChartOfAccountsBuilder
{

/** The chart. */
private final ChartOfAccounts chart = new ChartOfAccounts();
	
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
	public ChartOfAccountsBuilder addAccount(
		String accountNumber,
		String name,
		AccountSide increaseSide)
	{
		Account acc = new Account();
		acc.setAccountNumber(accountNumber);
		acc.setName(name);
		acc.setIncreaseSide(increaseSide);
		this.chart.addAccount(acc);
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
		return this.chart;
	}

	
}
