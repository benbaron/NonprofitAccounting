
package nonprofitbookkeeping.model;


import nonprofitbookkeeping.api.AccountDetails;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents an account with entries, supporting many-to-many relationship with funds.
 */

final public class Account implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -1149966185433260549L;
	
	private List<Fund> associatedFunds = new ArrayList<>(); // List to hold associated funds
	
	private AccountDetails accountDetails = new AccountDetailsImpl(null, null, null);
	
	/**
	 * 
	 * Constructor Account
	 * @param accountDetails
	 */
	public Account(AccountDetails accountDetails)
	{
		this.accountDetails = checkNotNull(accountDetails);
	}
	
	/**
	 * 
	 * Constructor Account
	 * @param accountNumber
	 * @param name
	 * @param increaseSide
	 */
	public Account(String accountNumber, String name, AccountSide increaseSide)
	{
		this.accountDetails = new AccountDetailsImpl(accountNumber, name, increaseSide);
	}
	

	/**
	 * Associates a fund with this account.
	 *
	 * @param fund the fund to associate
	 */
	public void addFund(Fund fund)
	{		
		if (!this.associatedFunds.contains(fund))
		{
			this.associatedFunds.add(fund);
			fund.addAccount(this); // Add this account to the fund's list of accounts
		}
		
	}
	
	/**
	 * Removes an associated fund from this account.
	 *
	 * @param fund the fund to remove
	 */
	public void removeFund(Fund fund)
	{
		this.associatedFunds.remove(fund);
		fund.removeAccount(this); // Remove this account from the fund's list of accounts
	}
	


	/**
	 * @return
	 */
	public String getAccountName()
	{
		// TODO Auto-generated method stub
		return this.accountDetails.getAccountName();
	}

	/**
	 * @return
	 */
	public List<Fund> getFunds()
	{
		return this.associatedFunds;
	}

	/**
	 * @return
	 */
	public BigDecimal getBalance()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the associatedFunds
	 */
	public List<Fund> getAssociatedFunds()
	{
		return this.associatedFunds;
	}

	/**
	 * @param associatedFunds the associatedFunds to set
	 */
	public void setAssociatedFunds(List<Fund> associatedFunds)
	{
		this.associatedFunds = associatedFunds;
	}

	/**
	 * @return the accountDetails
	 */
	public AccountDetails getAccountDetails()
	{
		return this.accountDetails;
	}

	/**
	 * @param accountDetails the accountDetails to set
	 */
	public void setAccountDetails(AccountDetails accountDetails)
	{
		this.accountDetails = accountDetails;
	}

}
