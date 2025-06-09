
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
	 * Default constructor for Jackson deserialization.
	 * Initializes amount, accountSide to null and accountNumber to an empty string.
	 */
	AccountingEntry()
	{
		this.amount = null;
		this.accountSide = null;
		this.accountNumber = "";
	}
	
	/**
	 * Constructs an AccountingEntry with the specified amount, account number, and account side.
	 * @param amount The monetary amount of the entry. Must not be null.
	 * @param accountNumber The account number associated with this entry. Must not be null.
	 * @param accountSide The side of the account (Debit or Credit) this entry affects. Must not be null.
	 * @throws NullPointerException if any of the parameters are null.
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
	 * @return Associated transaction, or null if no transaction is associated.
	 */
	public AccountingTransaction getTransaction()
	{
		return this.transaction;
	}
	
	/**
	 * Sets the transaction this entry belongs to.
	 * This method is required to enable circular references between entries and transactions.
	 * Once set, the transaction is "frozen" and cannot be changed.
	 * @param transaction The transaction belonging to this entry. Must not be null.
	 * @throws NullPointerException if the transaction is null.
	 */
	public void setTransaction(AccountingTransaction transaction)
	{
		this.transaction = checkNotNull(transaction);
		this.freeze = true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override public String toString()
	{
		return MoreObjects.toStringHelper(this)
			.add("amount", this.amount.toString())
			.addValue(this.accountSide)
			.toString();
	}
	
	/**
	 * Gets the side of the account (Debit or Credit) this entry affects.
	 * @return The account side.
	 */
	public AccountSide getAccountSide()
	{
		return this.accountSide;
	}
	
	/**
	 * Checks if the transaction for this entry is frozen (i.e., has been set).
	 * @return {@code true} if the transaction has been set, {@code false} otherwise.
	 */
	public boolean isFreeze()
	{
		return this.freeze;
	}
	
	/**
	 * Sets the freeze status of the transaction association.
	 * This is typically managed internally when {@link #setTransaction(AccountingTransaction)} is called.
	 * @param freeze {@code true} to indicate the transaction is set and frozen, {@code false} otherwise.
	 */
	public void setFreeze(boolean freeze)
	{
		this.freeze = freeze;
	}
	
	/**
	 * Gets the monetary amount of this accounting entry.
	 * @return The amount.
	 */
	public BigDecimal getAmount()
	{
		return this.amount;
	}
	
	/**
	 * Gets the account number associated with this entry.
	 * @return The account number.
	 */
	public String getAccountNumber()
	{
		return this.accountNumber;
	}
	
	/**
	 * Retrieves the {@link Account} object associated with this entry's account number
	 * from the current company's chart of accounts.
	 * 
	 * @return Account object
	 */
	public Account getAccount()
	{
		return CurrentCompany
			.getCompany()
			.getChartOfAccounts()
			.getAccount(this.accountNumber);
	}
	
	
}
