
package nonprofitbookkeeping.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
	

	private String accountNumber;
	
	private AccountSide increaseSide;
		
	private String name;

	private String accountCode;

	private String accountType;

	private Account parentAccount;

	private String currency;

	private BigDecimal openingBalance;
	

	/**
	 * 
	 * Constructor Account
	 * @param accountNumber
	 * @param name
	 * @param increaseSide
	 */
	public Account(String accountNumber, String name, AccountSide increaseSide)
	{
		this.accountNumber = accountNumber;
		this.name = name;
		this.increaseSide = increaseSide;
	}
	

	/**  
	 * Constructor Account
	 */
	public Account()
	{
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
	 * Tally and return the balance.
	 * @return balance on the account
	 */
	public BigDecimal totalAccountBalance()
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
	 * @return the accountNumber
	 */
	public String getAccountNumber()
	{
		return this.accountNumber;
	}


	/**
	 * @param accountNumber the accountNumber to set
	 */
	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}


	/**
	 * @return the increaseSide
	 */
	public AccountSide getIncreaseSide()
	{
		return this.increaseSide;
	}


	/**
	 * @param increaseSide the increaseSide to set
	 */
	public void setIncreaseSide(AccountSide increaseSide)
	{
		this.increaseSide = increaseSide;
	}


	/**
	 * @return the name
	 */
	public String getName()
	{
		return this.name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}


	/**
	 * @return the accountCode
	 */
	public String getAccountCode()
	{
		return this.accountCode;
	}


	/**
	 * @param accountCode the accountCode to set
	 */
	public void setAccountCode(String accountCode)
	{
		this.accountCode = accountCode;
	}


	/**
	 * @return the accountType
	 */
	public String getAccountType()
	{
		return this.accountType;
	}


	/**
	 * @param accountType the accountType to set
	 */
	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}


	/**
	 * @return the parentAccount
	 */
	public Account getParentAccount()
	{
		return this.parentAccount;
	}


	/**
	 * @param parentAccount the parentAccount to set
	 */
	public void setParentAccount(Account parentAccount)
	{
		this.parentAccount = parentAccount;
	}


	/**
	 * @return the currency
	 */
	public String getCurrency()
	{
		return this.currency;
	}


	/**
	 * @param currency the currency to set
	 */
	public void setCurrency(String currency)
	{
		this.currency = currency;
	}


	/**
	 * @return the openingBalance
	 */
	public BigDecimal getOpeningBalance()
	{
		return this.openingBalance;
	}


	/**
	 * @param openingBalance the openingBalance to set
	 */
	public void setOpeningBalance(BigDecimal openingBalance)
	{
		this.openingBalance = openingBalance;
	}

	/**
	 * @return
	 */
	public List<Fund> getFunds()
	{
		return getAssociatedFunds();
	}


	/**
	 * @return
	 */
	public boolean hasParent()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
