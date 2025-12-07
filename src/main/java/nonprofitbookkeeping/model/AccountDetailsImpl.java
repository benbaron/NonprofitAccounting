
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
	private String parentAccountId;
	private String currency;
	private BigDecimal openingBalance;
	
	/**
	 * Constructs an AccountDetailsImpl with essential account details.
	 * Other details like accountCode, accountType, parentAccount, currency,
	 * and openingBalance are initialized to null or default values and can be set using setters.
	 * @param accountNumber The account number.
	 * @param name The name of the account.
	 * @param increaseSide The side (Debit or Credit) where the account balance increases.
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
			.add("accountNumber", this.accountNumber)
			.add("name", this.name)
			.add("increaseSide", this.increaseSide)
			.toString();
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAccountName()
	{
		return this.name;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAccountCode()
	{
		return this.accountCode;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAccountType()
	{
		return this.accountType;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getParentAccount()
	{
		return this.parentAccountId;
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCurrency()
	{
		return this.currency;
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BigDecimal getOpeningBalance()
	{
		return this.openingBalance;
		
	}
	
	/**
	 * Sets the account code.
	 * @param accountCode the accountCode to set
	 */
	public void setAccountCode(String accountCode)
	{
		this.accountCode = accountCode;
		
	}
	
	/**
	 * Sets the account type.
	 * @param accountType the accountType to set
	 */
	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
		
	}
	
	/**
	 * Sets the parent account identifier.
	 * @param parentAccountId the parent account number to set
	 */
	public void setParentAccount(String parentAccountId)
	{
		this.parentAccountId = parentAccountId;
		
	}
	
	/**
	 * Sets the currency of the account.
	 * @param currency the currency to set
	 */
	public void setCurrency(String currency)
	{
		this.currency = currency;
		
	}
	
	
	/**
	 * Sets the opening balance of the account.
	 * @param openingBalance the openingBalance to set
	 */
	public void setOpeningBalance(BigDecimal openingBalance)
	{
		this.openingBalance = openingBalance;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAccountNumber()
	{
		return this.accountNumber;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountSide getIncreaseSide()
	{
		return this.increaseSide;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIncreaseSide(AccountSide increaseSide)
	{
		this.increaseSide = increaseSide;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return this.name;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setName(String name)
	{
		this.name = name;
		
	}
	
}

