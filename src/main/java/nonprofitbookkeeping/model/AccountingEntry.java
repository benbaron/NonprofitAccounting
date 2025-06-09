
package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.*;

/**
 * Represents an Accounting Entry.
 * The transaction reference is set automatically when an 
 * AccountingEntry is passed to the transaction constructor.
 * Once the transaction is set, it can't be changed.
 */
public final class AccountingEntry implements Serializable
{
	
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 5837792781542533633L;

	@JsonProperty final private BigDecimal amount;	
	@JsonProperty final private AccountSide accountSide;
	@JsonProperty final private String accountNumber;
	@JsonProperty private AccountingTransaction transaction;
	// Indicates if the transaction was set
	@JsonProperty private boolean freeze = false;
	
	
	/**
	 * 
	 * Constructor AccountingEntry
	 */
	public AccountingEntry ()
	{
		this.amount = null;
		this.accountSide = null;
		this.accountNumber = "";
	}
	
	
	/**
	 * 
	 * Constructor AccountingEntry
	 * @param amount
	 * @param accountNumber
	 * @param accountSide
	 */
	public AccountingEntry(BigDecimal amount, 
	                       String accountNumber, 
	                       AccountSide accountSide)
	{
		this.amount = checkNotNull(amount);
		this.accountNumber = checkNotNull(accountNumber);
		this.accountSide = checkNotNull(accountSide);
	}
	
	/**
	 * Gets the associated transaction.
	 * Throws a NullPointerException if no transaction is associated.
	 *
	 * @return Associated transaction
	 */
	public AccountingTransaction getTransaction()
	{
		return this.transaction;
	}
	
	/**
	 * This setter is required to enable circular references between entries and transactions.
	 *
	 * @param transaction The transaction belonging to this entry
	 */
	public void setTransaction(AccountingTransaction transaction)
	{
		this.transaction = checkNotNull(transaction);
		this.freeze = true;
	}
	
	/**
	 * 
	 * Override @see java.lang.Object#toString()
	 */
	@Override public String toString()
	{
		return MoreObjects.toStringHelper(this)
			.add("amount", this.amount.toString())
			.addValue(this.accountSide)
			.toString();
	}

	/**
	 * @return
	 */
	public AccountSide getAccountSide()
	{
		return this.accountSide;
	}

	/**
	 * @return the freeze
	 */
	public boolean isFreeze()
	{
		return this.freeze;
	}

	/**
	 * @param freeze the freeze to set
	 */
	public void setFreeze(boolean freeze)
	{
		this.freeze = freeze;
	}

	/**
	 * @return the amount
	 */
	public BigDecimal getAmount()
	{
		return this.amount;
	}

	/**
	 * @return the accountNumber
	 */
	public String getAccountNumber()
	{
		return this.accountNumber;
	}
	
	
}
