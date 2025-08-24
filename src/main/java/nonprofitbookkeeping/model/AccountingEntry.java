
package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;


import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;


import java.io.Serializable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.*;

/**
 * Represents an Accounting Entry.
 * The transaction reference is set automatically when an
 * AccountingEntry is passed to the transaction constructor.
 * Once the transaction is set, it can't be changed.
 */
@Entity
@Table(name = "accounting_entries")

public final class AccountingEntry implements Serializable
{
	
	
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 5837792781542533633L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY) private int id;
	
	@JsonProperty private BigDecimal amount;
	
	@Enumerated(EnumType.STRING)
	
	@JsonProperty private AccountSide accountSide;
	@JsonProperty private String accountNumber;
	@JsonProperty private String fundNumber;
	
	/**
	 * @return the fundNumber
	 */
	public String getFundNumber()
	{
		return this.fundNumber;
		
	}
	
	/**
	 * @param fundNumber the fundNumber to set
	 */
	public void setFundNumber(String fundNumber)
	{
		this.fundNumber = fundNumber;
		
	}
	
	/**
	 * Optional display name for the account. This mirrors the account's
	 * {@code name} field at the time the entry was created.  It is stored
	 * on the entry rather than the transaction so that each entry can
	 * reference its own account directly.
	 */
	@JsonProperty private String accountName;
	/** Identifier of an optional supplemental record linked to this entry. */
	@JsonProperty private String supplementalRecordId;
	
	
        // Future versions can include this.
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "transaction_id")
        @JsonIgnore private AccountingTransaction transaction;
	
	// Indicates if the transaction was set
	@JsonProperty private boolean freeze = false;
	
	
	/**
	 * Default constructor for Jackson deserialization.
	 * Initializes amount, accountSide to null and accountNumber to an empty string.
	 */
	public AccountingEntry()
	{
		this.amount = null;
		this.accountSide = null;
		this.accountNumber = "";
		this.accountName = null;
		this.fundNumber = null;
		this.supplementalRecordId = null;
		
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
		this(amount, accountNumber, accountSide, null);
		
	}
	
	/**
	 * Constructs an AccountingEntry with an explicit account name.
	 *
	 * @param amount the monetary amount of the entry
	 * @param accountNumber the account number this entry affects
	 * @param accountSide the side of the account the amount applies to
	 * @param accountName optional account name to store with the entry
	 */
	public AccountingEntry(BigDecimal amount,
		String accountNumber,
		AccountSide accountSide,
		String accountName)
	{
		this.amount = checkNotNull(amount);
		this.accountNumber = checkNotNull(accountNumber);
		this.accountSide = checkNotNull(accountSide);
		this.accountName = accountName;
		this.fundNumber = null;
		this.supplementalRecordId = null;
		
	}
	
	
        /**
         * Gets the associated transaction.
         * This is ignored during JSON serialization to prevent cycles.
         *
         * @return Associated transaction, or null if no transaction is associated.
         */
        @JsonIgnore
        public AccountingTransaction getTransaction()
        {
                return this.transaction;

        }

        /**
         * Convenience accessor that exposes only the identifier of the
         * associated transaction for serialization purposes.
         *
         * @return id of the linked transaction or {@code null} if none
         */
        @JsonProperty("transactionId")
        public Integer getTransactionId()
        {
                return (this.transaction != null) ? this.transaction.getId() : null;
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
	 * Gets the stored account name for this entry, if any.
	 *
	 * @return the account name, or {@code null} if not set
	 */
	public String getAccountName()
	{
		return this.accountName;
		
	}
	
	/**
	 * Sets the account name for this entry.
	 *
	 * @param accountName the name to associate with the entry
	 */
	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
		
	}
	
	/**
	 * Gets the identifier of the supplemental record linked to this entry, if any.
	 *
	 * @return supplemental record id or {@code null}
	 */
	public String getSupplementalRecordId()
	{
		return this.supplementalRecordId;
		
	}
	
	/**
	 * Associates this entry with a supplemental record identifier.
	 *
	 * @param supplementalRecordId identifier of the supplemental record
	 */
	public void setSupplementalRecordId(String supplementalRecordId)
	{
		this.supplementalRecordId = supplementalRecordId;
		
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
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
			.add("amount", this.amount.toString())
			.addValue(this.accountSide)
			.add("account", this.accountNumber)
			.toString();
		
	}
	
	/**
	 * Gets the database identifier for this entry.
	 *
	 * @return primary key value
	 */
	public int getId()
	{
		return this.id;
		
	}
	
}
