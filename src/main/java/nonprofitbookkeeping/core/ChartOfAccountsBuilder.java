
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
	 * 
	 * Constructor ChartOfAccountsBuilder
	 */
	public ChartOfAccountsBuilder()
	{
	}
	
	/**
	 * 
	 * @return
	 */
	public static ChartOfAccountsBuilder create()
	{
		return new ChartOfAccountsBuilder();
	}
	
	/**
	 * 
	 * @param accountNumber
	 * @param name
	 * @param increaseSide
	 * @return
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
	 * 
	 * @return
	 */
	public ChartOfAccounts build()
	{
		return new ChartOfAccounts();
	}
	
}
