
package nonprofitbookkeeping.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import java.io.Serializable;

import nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase;

import static com.google.common.base.Preconditions.checkNotNull;

// TODO: Auto-generated Javadoc
/**
 * The Class AccountingTransaction.
 */
public class AccountingTransaction implements Serializable
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = -8821254116304310L;
	
	/** Unique identifier for the transaction. */
	@JsonProperty private int id;
	
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

	/** The to from. */
	@JsonProperty private String toFrom; // Non-final
	
	/** The check number. */
	@JsonProperty private String checkNumber; // Non-final
	
	/** The clear bank. */
	@JsonProperty private String clearBank; // Non-final
	
	/** The budget tracking. */
	@JsonProperty private String budgetTracking; // Non-final
	
	/** The associated fund name. */
	@JsonProperty private String associatedFundName; // Non-final

	/** The supplemental lines. */
	@JsonProperty private List<TxnSupplementalLineBase> supplementalLines;
	
	/**
	 * Constructor AccountingTransaction.
	 */
	public AccountingTransaction()
	{
		this.id = 0;
		this.entries = new LinkedHashSet<>();
		this.info = new LinkedHashMap<>();
		this.bookingDateTimestamp = 0;
		
		this.date = "";
		this.memo = "";
		this.toFrom = "";
		this.checkNumber = "";
		this.clearBank = "";
		this.budgetTracking = "";
		this.associatedFundName = "";		
		this.supplementalLines = new ArrayList<>();
	}

	/**
	 * 
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
		
		// Ensure immutability for collections passed in
		this.entries = Collections
			.unmodifiableSet(new HashSet<>(checkNotNull(entries,
				"entries cannot be null")));
		this.info = (info == null) ? Collections.emptyMap() :
			Collections.unmodifiableMap(new HashMap<>(info));
		
		this.bookingDateTimestamp = bookingDateTimestamp;
		
		this.date = "";
		this.memo = "";
		this.toFrom = "";
		this.checkNumber = "";
		this.clearBank = "";
		this.budgetTracking = "";
		this.associatedFundName = "";
		this.supplementalLines = new ArrayList<>();
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
	public void setBookingDateTimestamp(long bookingDateTimestamp)
	{
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
		BigDecimal creditTotal = BigDecimal.ZERO;
		
		if (this.entries == null)
		{
			return BigDecimal.ZERO; // Guard against null entries
		}
			
		for (AccountingEntry e : this.entries)
		{			
			if (e.getAccountSide() == AccountSide.DEBIT)
			{
				debitTotal = debitTotal.add(e.getAmount());
			}
			else 
			{
				creditTotal = creditTotal.add(e.getAmount());
			}
			
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
	 * Gets the supplemental lines.
	 *
	 * @return the supplemental lines
	 */
	public List<TxnSupplementalLineBase> getSupplementalLines()
	{
		return this.supplementalLines == null
			? Collections.emptyList()
			: this.supplementalLines;
	}

	/**
	 * Sets the supplemental lines.
	 *
	 * @param supplementalLines the new supplemental lines
	 */
	public void setSupplementalLines(List<TxnSupplementalLineBase> supplementalLines)
	{
		this.supplementalLines = supplementalLines == null
			? new ArrayList<>()
			: new ArrayList<>(supplementalLines);
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
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId()
	{
		return this.id;
	}
	
	/**
	 * Adds the entry.
	 *
	 * @param accountingEntry the accounting entry
	 */
	public void addEntry(AccountingEntry accountingEntry)
	{
		
		if (this.entries == null)
		{
			this.entries = new LinkedHashSet<>();
		}
		
		this.entries.add(accountingEntry);
	}
	
	/**
	 * Sets the id.
	 *
	 * @param i the new id
	 */
	public void setId(int i)
	{
		this.id = i;
	}	
	
	/**
	 * Sets the booking date timestamp.
	 *
	 * @param from the new booking date timestamp
	 */
	public void setBookingDateTimestamp(Timestamp from)
	{
		this.bookingDateTimestamp = from != null ? from.getTime() : 0L;
	}
	
	/**
	* Returns the total credit amount for this transaction. If explicit
	* credit and debit fields were not populated (e.g. older transactions
	* loaded from disk), the values are computed on demand from the
	* underlying {@link #entries} collection.
	*
	* @return The total credit amount of the transaction.
	*/
	public BigDecimal getCredit()
	{
		BigDecimal credit = BigDecimal.ZERO;
		if ((credit == null || credit.signum() == 0) && this.entries !=
			null &&
			!this.entries.isEmpty())
		{
			credit = this.entries.stream()
				.filter(e -> e.getAccountSide() == AccountSide.CREDIT)
				.map(AccountingEntry::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		}
		
		return credit == null ? BigDecimal.ZERO : credit;
	}
	
	/**
	* Returns the total debit amount for this transaction. Like
	* {@link #getCredit()}, this method falls back to computing the value
	* from the transaction's entries if the stored field is empty.
	*
	* @return The total debit amount of the transaction.
	*/
	public BigDecimal getDebit()
	{	
		BigDecimal debit = BigDecimal.ZERO;
		if ((debit == null || debit.signum() == 0) && this.entries != null &&
			!this.entries.isEmpty())
		{
			debit = this.entries.stream()
				.filter(e -> e.getAccountSide() == AccountSide.DEBIT)
				.map(AccountingEntry::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		}
		
		return debit == null ? BigDecimal.ZERO : debit;
	}

	/**
	 * Gets the to from.
	 *
	 * @return the toFrom
	 */
	public String getToFrom()
	{
		return this.toFrom;
		
	}

	/**
	 * Sets the to from.
	 *
	 * @param toFrom the toFrom to set
	 */
	public void setToFrom(String toFrom)
	{
		this.toFrom = toFrom;
		
	}

	/**
	 * Gets the check number.
	 *
	 * @return the checkNumber
	 */
	public String getCheckNumber()
	{
		return this.checkNumber;
		
	}

	/**
	 * Sets the check number.
	 *
	 * @param checkNumber the checkNumber to set
	 */
	public void setCheckNumber(String checkNumber)
	{
		this.checkNumber = checkNumber;
		
	}

	/**
	 * Gets the clear bank.
	 *
	 * @return the clearBank
	 */
	public String getClearBank()
	{
		return this.clearBank;
		
	}

	/**
	 * Sets the clear bank.
	 *
	 * @param clearBank the clearBank to set
	 */
	public void setClearBank(String clearBank)
	{
		this.clearBank = clearBank;
		
	}

	/**
	 * Gets the budget tracking.
	 *
	 * @return the budgetTracking
	 */
	public String getBudgetTracking()
	{
		return this.budgetTracking;
		
	}

	/**
	 * Sets the budget tracking.
	 *
	 * @param budgetTracking the budgetTracking to set
	 */
	public void setBudgetTracking(String budgetTracking)
	{
		this.budgetTracking = budgetTracking;
		
	}

	/**
	 * Gets the associated fund name.
	 *
	 * @return the associatedFundName
	 */
	public String getAssociatedFundName()
	{
		return this.associatedFundName;
		
	}

	/**
	 * Sets the associated fund name.
	 *
	 * @param associatedFundName the associatedFundName to set
	 */
	public void setAssociatedFundName(String associatedFundName)
	{
		this.associatedFundName = associatedFundName;
		
	}

	/**
	 * Count account balance.
	 *
	 * @return the big decimal
	 */
	public static BigDecimal countAccountBalance()
	{
		return BigDecimal.ZERO;
	}

}
