
package nonprofitbookkeeping.model;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.*;

/**
 * Represents an Accounting Entry.
 * The transaction reference is set automatically when an AccountingEntry is passed to the transaction constructor.
 * Once the transaction is set, it can't be changed.
 */
public final class AccountingEntry implements Serializable
{
	
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 5837792781542533633L;

	final private BigDecimal amount;
	
	final private AccountSide accountSide;
	
	final private String accountNumber;
	
	private AccountingTransaction transaction;
	// Indicates if the transaction was set
	private boolean freeze = false;
	
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
		checkArgument(amount.signum() == 1, "Accounting entries can't have a negative amount");
		checkArgument(!accountNumber.isEmpty());
	}
	
	/**
	 * Gets the associated transaction.
	 * Throws a NullPointerException if no transaction is associated.
	 *
	 * @return Associated transaction
	 */
	public AccountingTransaction getTransaction()
	{
		checkNotNull(this.transaction, "Getter returning null. You have to set a transaction.");
		return this.transaction;
	}
	
	/**
	 * This setter is required to enable circular references between entries and transactions.
	 *
	 * @param transaction The transaction belonging to this entry
	 */
	public void setTransaction(AccountingTransaction transaction)
	{
		checkState(!this.freeze, "An AccountingEntry's transaction can only be set once");
		checkNotNull(transaction);
		this.transaction = transaction;
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
