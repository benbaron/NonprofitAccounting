
package nonprofitbookkeeping.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor; // Ensure this is present
import lombok.NoArgsConstructor;  // Added

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a financial transaction, which is a collection of related {@link AccountingEntry} instances.
 * A transaction must be balanced, meaning the sum of its debit entries equals the sum of its credit entries.
 * This class uses Lombok for boilerplate code generation like getters, setters, constructors, etc.
 */
@Builder
@Data
@NoArgsConstructor(force = true) // Changed to force = true
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE) // For builder and potentially other
															// internal uses
public class AccountingTransaction implements Serializable
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = -8821254116304310L;
	
	/** The primary account associated with this transaction, if any. */
	@JsonProperty private Account account;
	/** The set of accounting entries that make up this transaction. Must not be null or empty. */
	@JsonProperty private Set<AccountingEntry> entries;
	/** Additional information or metadata about the transaction, stored as key-value pairs. */
	@JsonProperty private Map<String, String> info;
	/** The timestamp when the transaction was booked/recorded, in milliseconds since epoch. */
	@JsonProperty private long bookingDateTimestamp;
	/** The date of the transaction, typically in a string format like "YYYY-MM-DD". */
	@JsonProperty private String date; // Non-final
	/** A descriptive memo or note for the transaction. */
	@JsonProperty private String memo; // Non-final
	
	/**
	 * Default constructor.
	 * Used by Lombok and Jackson for instantiation.
	 * Initializes fields to default values (e.g., null for objects, 0 for primitives).
	 */
	public AccountingTransaction()
	{

	}
	
	/**
	 * Constructs an AccountingTransaction with specified details.
	 * Ensures that entries are not null and the transaction is balanced.
	 * Note: The process of setting the transaction back onto its entries is commented out
	 * to avoid circular dependency issues during construction and may need to be handled
	 * post-construction or via a builder pattern.
	 *
	 * @param account The primary account associated with this transaction. Must not be null.
	 * @param entries The set of accounting entries for this transaction. Must not be null and must contain at least two entries.
	 * @param info Optional map of additional information about the transaction. Can be null.
	 * @param bookingDateTimestamp The timestamp when the transaction was booked.
	 * @throws NullPointerException if account or entries are null.
	 * @throws IllegalArgumentException if entries is empty, contains less than two entries, or if the transaction is not balanced.
	 */
	public AccountingTransaction(Account account,
		Set<AccountingEntry> entries,
		@Nullable Map<String, String> info,
		long bookingDateTimestamp)
	{
		this.account = checkNotNull(account, "account cannot be null");
		
		// Ensure immutability for collections passed in
		this.entries = Collections
			.unmodifiableSet(new HashSet<>(checkNotNull(entries,
				"entries cannot be null")));
		this.info = (info == null) ? Collections.emptyMap() :
			Collections.unmodifiableMap(new HashMap<>(info));
		
		this.bookingDateTimestamp = bookingDateTimestamp;
		
		// Initialize other fields to defaults as per previous logic

		this.date = "";
		this.memo = "";

		checkArgument(!this.entries.isEmpty(),
			"Transaction must have at least one entry (ideally 2+ for balance)");
		checkArgument(this.entries.size() >= 2,
			"A transaction consists of at least two entries");
		
		// Temporarily remove setTransaction to break circular dependency potential
		// during construction
		// this.entries.forEach(e -> e.setTransaction(this));

		// This logic needs to be handled carefully. If AccountingEntry needs a
		// reference to AccountingTransaction upon construction, and
		// AccountingTransaction needs to validate entries that
		// might already need that back-reference, it's tricky.
		//
		// For now, to compile, I'll comment this out.
		// It's possible the builder pattern handles this by setting the transaction on
		// entries after the transaction itself is built.

		// The isBalanced check might also need to be deferred or handled carefully if
		// entries are not fully set up.
		checkArgument(isBalanced(), "Transaction unbalanced");

		// If entries are now unmodifiable, setting transaction back might not be
		// possible here.

		// This implies AccountingEntry might need to be constructed with the
		// transaction, or setTransaction needs to be called post
		// AccountingTransaction construction.
		// For now, to proceed with compilation, this line is commented out.
		// It will likely need to be addressed for full functionality.

		// this.entries.forEach(e -> e.setTransaction(this));
	}
	
	/**
	 * Gets the primary account associated with this transaction.
	 * @return The account, or null if no specific account is associated at the transaction level.
	 */
	public Account getAccount()
	{
		return this.account;
	}

	/**
	 * Sets the primary account for this transaction.
	 * @param account The account to set.
	 */
	public void setAccount(Account account)
	{
		this.account = account;
	}

	/**
	 * Gets the set of accounting entries that make up this transaction.
	 * @return An unmodifiable set of accounting entries.
	 */
	public Set<AccountingEntry> getEntries()
	{
		return this.entries;
	}

	/**
	 * Sets the accounting entries for this transaction.
	 * Note: If immutability is desired after construction, this method might be restricted or handled by a builder.
	 * @param entries The set of accounting entries to set.
	 */
	public void setEntries(Set<AccountingEntry> entries)
	{
		this.entries = entries;
	}

	/**
	 * Gets the additional information or metadata associated with this transaction.
	 * @return An unmodifiable map of information, or an empty map if no info is set.
	 */
	public Map<String, String> getInfo()
	{
		return this.info;
	}

	/**
	 * Sets the additional information for this transaction.
	 * @param info A map of key-value pairs representing additional information.
	 */
	public void setInfo(Map<String, String> info)
	{
		this.info = info;
	}

	/**
	 * Gets the booking date timestamp of the transaction.
	 * This is typically represented as milliseconds since the epoch.
	 * @return The booking date timestamp.
	 */
	public Long getBookingDateTimestamp()
	{
		return this.bookingDateTimestamp;
	}

	/**
	 * Sets the booking date timestamp for this transaction.
	 * @param bookingDateTimestamp The timestamp in milliseconds since epoch.
	 */
	public void setBookingDateTimestamp(long bookingDateTimestamp) {
		this.bookingDateTimestamp = bookingDateTimestamp;
	}

	/**
	 * Gets the serial version UID for serialization.
	 * @return The serial version UID.
	 */
	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}

	/**
	 * Gets the memo or description of the transaction.
	 * @return The transaction memo.
	 */
	public String getMemo()
	{
		return this.memo;
	}

	/**
	 * Calculates the total debit amount of the transaction.
	 * Iterates through all entries and sums the amounts of debit entries.
	 * @return The total debit amount as a BigDecimal. Returns BigDecimal.ZERO if entries are null.
	 */
	public BigDecimal getTotalAmount()
	{
		BigDecimal debitTotal = BigDecimal.ZERO;
		// BigDecimal creditTotal = BigDecimal.ZERO; // Not used in current implementation of this method
		
		if (this.entries == null)
			return BigDecimal.ZERO; // Guard against null entries

		for (AccountingEntry e : this.entries)
		{
			
			if (e.getAccountSide() == AccountSide.DEBIT)
			{
				debitTotal = debitTotal.add(e.getAmount());
			}
			// else: credit entries are not summed in this specific method's current logic for return value
			// {
			// creditTotal = creditTotal.add(e.getAmount());
			// }
			
		}
		
		return debitTotal;
	}
	
	/**
	 * Checks if the transaction is balanced.
	 * A transaction is balanced if the sum of all debit entries equals the sum of all credit entries.
	 * An empty or null set of entries is considered unbalanced (or could be defined as balanced based on rules).
	 * @return {@code true} if the transaction is balanced, {@code false} otherwise.
	 */
	public boolean isBalanced()
	{
		
		if (this.entries == null || this.entries.isEmpty()) // Or entries.size() < 2
		{
			return false; // Or true, depending on business rule for empty/single-entry transactions
		}
		
		BigDecimal balance = this.entries.stream()
			.map(e -> e.getAccountSide() == AccountSide.DEBIT ?
				e.getAmount() : e.getAmount().negate())
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		return balance.compareTo(BigDecimal.ZERO) == 0;
	}
	
	/**
	 * Returns a string representation of the transaction, including its booking date and entries.
	 * @return A string summary of the transaction.
	 */
	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Transaction ")
			.append(Instant.ofEpochMilli(this.bookingDateTimestamp).toString())
			.append("\n");
		
		if (this.entries != null)
		{
			this.entries.forEach(e -> sb.append(e).append("\n"));
		}
		else
		{
			sb.append("No entries\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * Gets the description of the transaction, which is equivalent to its memo.
	 * @return The transaction description (memo).
	 */
	public String getDescription()
	{
		return this.memo;
	}
	
	/**
	 * Gets the name of the primary account associated with this transaction.
	 * @return The name of the account, or null if no account is associated or the account has no name.
	 */
	public String getAccountName()
	{
		return this.account != null ? this.account.getName() : null;
	}
	
	/**
	 * Sets the description (memo) of the transaction.
	 * @param description The description to set.
	 */
	public void setDescription(String description)
	{
		this.memo = description;
	}
	
	/**
	 * Gets the date of the transaction.
	 * @return The transaction date string.
	 */
	public String getDate()
	{
		return this.date;
	}

	/**
	 * Sets the date of the transaction.
	 * @param date The date string to set (e.g., "YYYY-MM-DD").
	 */
	public void setDate(String date)
	{
		this.date = date;
	}
	
	/**
	 * Calculates the total balance of the primary account associated with this transaction.
	 * If no account is associated, returns BigDecimal.ZERO.
	 * @return The total balance of the associated account.
	 */
	public BigDecimal countAccountBalance()
	{
		if (this.account == null)
			return BigDecimal.ZERO;
		return this.account.totalAccountBalance();
	}
	
	/**
	 * Sets the memo for this transaction.
	 * This is an alias for {@link #setDescription(String)}.
	 * @param memo The memo text to set.
	 */
	public void setMemo(String memo)
	{
		this.memo = memo;
	}
	
	/**
	 * Placeholder method for setting the total amount.
	 * Currently, this method is a stub and does not modify the transaction's entries.
	 * Modifying the total amount would typically involve adjusting the underlying entries,
	 * which should be handled carefully, possibly via a builder or by creating a new transaction instance.
	 * @param valueOf The new total amount (currently unused).
	 */
	public void setTotalAmount(BigDecimal valueOf)
	{
		// This method was a stub. If it's meant to do something,
		// it would likely need to adjust entries, which are now final and unmodifiable
		// via this instance after construction (if entries are made unmodifiable).
		// This suggests mutation should happen via new instances or builder.
	}

	/**
	 * @return
	 */
	public Object getId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param string
	 */
	public void setAccountName(String string)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param accountingEntry
	 */
	public void addEntry(AccountingEntry accountingEntry)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param i
	 */
	public void setId(int i)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param bigDecimal
	 */
	public void setCredit(BigDecimal bigDecimal)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param zero
	 */
	public void setDebit(BigDecimal zero)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param from
	 */
	public void setBookingDateTimestamp(Timestamp from)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return
	 */
	public BigDecimal getCredit()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	public BigDecimal getDebit()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
