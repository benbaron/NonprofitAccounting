
package nonprofitbookkeeping.model;

import com.google.common.base.MoreObjects;

import nonprofitbookkeeping.api.AccountDetails;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Represents an immutable account description.
 */
public final class AccountDetailsImpl implements AccountDetails, Serializable
{
	
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -4978532679309538623L;

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
	 * Constructor AccountDetailsImpl
	 * @param accountNumber
	 * @param name
	 * @param increaseSide
	 */
	public AccountDetailsImpl(String accountNumber, 
	                          String name, 
	                          AccountSide increaseSide)
	{
		this.accountNumber = accountNumber;
		this.increaseSide = increaseSide;
		this.name = name;
	}
	

	/**
	 * Override @see nonprofitbookkeeping.model.AccountDetails#toString() 
	 */
	@Override public String toString()
	{
		return MoreObjects.toStringHelper(this)
			.add("accountNumber", this.accountNumber)
			.add("name", this.name)
			.add("increaseSide", this.increaseSide)
			.toString();
	}
	
	/**
	 * Override @see nonprofitbookkeeping.model.AccountDetails#getAccountName() 
	 */
	@Override public String getAccountName()
	{
		return this.name;
	}

	/**
	 * Override @see nonprofitbookkeeping.api.AccountDetails#getAccountCode() 
	 */
	@Override public String getAccountCode()
	{
		return this.accountCode;
	}

	/**
	 * Override @see nonprofitbookkeeping.api.AccountDetails#getAccountType() 
	 */
	@Override public String getAccountType()
	{
		return this.accountType;
	}

	/**
	 * Override @see nonprofitbookkeeping.api.AccountDetails#getParentAccount() 
	 */
	@Override public Account getParentAccount()
	{
		// TODO Auto-generated method stub
		return this.parentAccount;
	}


	/**
	 * Override @see nonprofitbookkeeping.api.AccountDetails#getCurrency() 
	 */
	@Override public String getCurrency()
	{
		return this.currency;
	}


	/**
	 * Override @see nonprofitbookkeeping.api.AccountDetails#getOpeningBalance() 
	 */
	@Override public BigDecimal getOpeningBalance()
	{
		return this.openingBalance;
	}

	/**
	 * @param accountCode the accountCode to set
	 */
	public void setAccountCode(String accountCode)
	{
		this.accountCode = accountCode;
	}

	/**
	 * @param accountType the accountType to set
	 */
	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}

	/**
	 * @param parentAccount the parentAccount to set
	 */
	public void setParentAccount(Account parentAccount)
	{
		this.parentAccount = parentAccount;
	}

	/**
	 * @param currency the currency to set
	 */
	public void setCurrency(String currency)
	{
		this.currency = currency;
	}


	/**
	 * @param openingBalance the openingBalance to set
	 */
	public void setOpeningBalance(BigDecimal openingBalance)
	{
		this.openingBalance = openingBalance;
	}

	/**
	 * @return the accountNumber
	 */
	@Override public String getAccountNumber()
	{
		return this.accountNumber;
	}

	/**
	 * @param accountNumber the accountNumber to set
	 */
	@Override public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}


	/**
	 * @return the increaseSide
	 */
	@Override public AccountSide getIncreaseSide()
	{
		return this.increaseSide;
	}

	/**
	 * @param increaseSide the increaseSide to set
	 */
	@Override public void setIncreaseSide(AccountSide increaseSide)
	{
		this.increaseSide = increaseSide;
	}

	/**
	 * @return the name
	 */
	@Override public String getName()
	{
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	@Override public void setName(String name)
	{
		this.name = name;
	}

}

