
package nonprofitbookkeeping.model;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
		this.accountNumber = checkNotNull(accountNumber);
		this.increaseSide = checkNotNull(increaseSide);
		this.name = checkNotNull(name);
		
		checkArgument(!accountNumber.isEmpty());
		checkArgument(!name.isEmpty());
		
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
	 * Override @see nonprofitbookkeeping.api.AccountDetails#getAccountNumber() 
	 */
	@Override public String getAccountNumber()
	{
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * Override @see nonprofitbookkeeping.api.AccountDetails#getIncreaseSide() 
	 */
	@Override public AccountSide getIncreaseSide()
	{
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * Override @see nonprofitbookkeeping.api.AccountDetails#getName() 
	 */
	@Override public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * Override @see nonprofitbookkeeping.api.AccountDetails#setAccountNumber(java.lang.String) 
	 */
	@Override public void setAccountNumber(String accountNumber)
	{
		// TODO Auto-generated method stub
		
	}


	/**
	 * Override @see nonprofitbookkeeping.api.AccountDetails#setIncreaseSide(nonprofitbookkeeping.model.AccountSide) 
	 */
	@Override public void setIncreaseSide(AccountSide increaseSide)
	{
		// TODO Auto-generated method stub
		
	}


	/**
	 * Override @see nonprofitbookkeeping.api.AccountDetails#setName(java.lang.String) 
	 */
	@Override public void setName(String name)
	{
		// TODO Auto-generated method stub
		
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



}

