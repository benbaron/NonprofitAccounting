
package nonprofitbookkeeping.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

=======
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor; // Ensure this is present
import lombok.NoArgsConstructor; // Added
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
>>>>>>> a0d4b45 Remove binary document and zip files

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
@Entity
@Table(name = "accounting_transactions")
=======
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
@Entity
@Table(name = "transaction")
>>>>>>> a0d4b45 Remove binary document and zip files
public class AccountingTransaction implements Serializable
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = -8821254116304310L;
	
        /** Unique identifier for the transaction. */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        @JsonProperty private int id;

        /** The set of accounting entries that make up this transaction. Must not be null or empty. */

        @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
        @JsonProperty private Set<AccountingEntry> entries = new HashSet<>();

        /** Supplemental key/value records tied to this transaction. */
        @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private Set<SupplementalRecord> supplementalRecords = new HashSet<>();

        /** Additional information or metadata about the transaction, stored as key-value pairs. */
        @ElementCollection
        @CollectionTable(name = "transaction_info", joinColumns = @JoinColumn(name = "transaction_id"))
        @MapKeyColumn(name = "info_key")
        @Column(name = "info_value")
        @JsonProperty private Map<String, String> info;

        /** Owning journal for this transaction. */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "journal_id")
        private Journal journal;

        /** Key used within {@link #info} to store the record type. */
        public static final String RECORD_TYPE_KEY = "recordType";

=======
        @Column(name = "transaction_id")
        @JsonProperty
        private int id;
        /** The set of accounting entries that make up this transaction. Must not be null or empty. */
        @JsonProperty
        @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL)
        private Set<AccountingEntry> entries;
	/** Additional information or metadata about the transaction, stored as key-value pairs. */
	@JsonProperty private Map<String, String> info;
>>>>>>> a0d4b45 Remove binary document and zip files
	/** The timestamp when the transaction was booked/recorded, in milliseconds since epoch. */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	@JsonProperty private long bookingDateTimestamp;

=======
        @JsonProperty
        @Column(name = "booking_timestamp")
        private long bookingDateTimestamp;
>>>>>>> a0d4b45 Remove binary document and zip files
	/** The date of the transaction, typically in a string format like "YYYY-MM-DD". */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	@JsonProperty private String date; // Non-final

=======
        @JsonProperty
        @Column(name = "date")
        private String date; // Non-final
>>>>>>> a0d4b45 Remove binary document and zip files
	/** A descriptive memo or note for the transaction. */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	@JsonProperty private String memo; // Non-final
=======
        @JsonProperty
        @Column(name = "memo")
        private String memo; // Non-final
	
	/**
	 * Default constructor.
	 * Used by Lombok and Jackson for instantiation.
	 * Initializes fields to default values (e.g., null for objects, 0 for primitives).
	 */
	public AccountingTransaction()
	{
>>>>>>> a0d4b45 Remove binary document and zip files

	@JsonProperty private String toFrom; // Non-final
	@JsonProperty private String checkNumber; // Non-final
	@JsonProperty private String clearBank; // Non-final
	@JsonProperty private String budgetTracking; // Non-final
	@JsonProperty private String associatedFundName; // Non-final
	
	/**  
	 * Constructor AccountingTransaction
	 */
        public AccountingTransaction()
        {
                this.id = 0;
                this.entries = new HashSet<>();
                this.supplementalRecords = new HashSet<>();
                this.info = null;
                this.bookingDateTimestamp = 0;
		
		this.date = "";
		this.memo = "";
		this.toFrom = "";
		this.checkNumber = "";
		this.clearBank = "";
		this.budgetTracking = "";
		this.associatedFundName = "";		
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
                this.entries = new HashSet<>(checkNotNull(entries,
                                "entries cannot be null"));
                this.entries.forEach(e -> e.setTransaction(this));
                this.supplementalRecords = new HashSet<>();
                this.info = (info == null) ? new HashMap<>() :
                        new HashMap<>(info);
		
		this.bookingDateTimestamp = bookingDateTimestamp;
		
		this.date = "";
		this.memo = "";
		this.toFrom = "";
		this.checkNumber = "";
		this.clearBank = "";
		this.budgetTracking = "";
		this.associatedFundName = "";
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
                if (this.entries != null)
                {
                        this.entries.forEach(e -> e.setTransaction(this));
                }
        }

        /**
         * Gets the supplemental records tied to this transaction.
         *
         * @return set of supplemental records, possibly empty
         */
        public Set<SupplementalRecord> getSupplementalRecords()
        {
                return this.supplementalRecords;
        }

        /**
         * Replaces the supplemental records associated with this transaction.
         *
         * @param records new set of records
         */
        public void setSupplementalRecords(Set<SupplementalRecord> records)
        {
                this.supplementalRecords = records;
                if (this.supplementalRecords != null)
                {
                        this.supplementalRecords.forEach(r -> r.setTransaction(this));
                }
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
         * Retrieves the optional record type from the {@link #info} map.
         *
         * @return the record type string or {@code null} if not set.
         */
        public String getRecordType()
        {
                return this.info != null ? this.info.get(RECORD_TYPE_KEY) : null;
        }

        /**
         * Sets the record type inside the {@link #info} map. If {@code recordType}
         * is {@code null}, the key is removed from the map.
         *
         * @param recordType the record type to associate with this transaction
         */
        public void setRecordType(String recordType)
        {
                if (this.info == null)
                {
                        this.info = new HashMap<>();
                }

                if (recordType == null)
                {
                        this.info.remove(RECORD_TYPE_KEY);
                }
                else
                {
                        this.info.put(RECORD_TYPE_KEY, recordType);
                }
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
	 * Sets the memo for this transaction.
	 * This is an alias for {@link #setDescription(String)}.
	 * @param memo The memo text to set.
	 */
	public void setMemo(String memo)
	{
		this.memo = memo;
	}	
	
	/**
	 * @return
	 */
	public int getId()
	{
		return this.id;
	}
	
	/**
	 * @param accountingEntry
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
	 * @param i
	 */
	public void setId(int i)
	{
		this.id = i;
	}	
	
	/**
	 * @param from
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
	 * @return the toFrom
	 */
	public String getToFrom()
	{
		return this.toFrom;
		
	}

	/**
	 * @param toFrom the toFrom to set
	 */
	public void setToFrom(String toFrom)
	{
		this.toFrom = toFrom;
		
	}

	/**
	 * @return the checkNumber
	 */
	public String getCheckNumber()
	{
		return this.checkNumber;
		
	}

	/**
	 * @param checkNumber the checkNumber to set
	 */
	public void setCheckNumber(String checkNumber)
	{
		this.checkNumber = checkNumber;
		
	}

	/**
	 * @return the clearBank
	 */
	public String getClearBank()
	{
		return this.clearBank;
		
	}

	/**
	 * @param clearBank the clearBank to set
	 */
	public void setClearBank(String clearBank)
	{
		this.clearBank = clearBank;
		
	}

	/**
	 * @return the budgetTracking
	 */
	public String getBudgetTracking()
	{
		return this.budgetTracking;
		
	}

	/**
	 * @param budgetTracking the budgetTracking to set
	 */
	public void setBudgetTracking(String budgetTracking)
	{
		this.budgetTracking = budgetTracking;
		
	}

	/**
	 * @return the associatedFundName
	 */
	public String getAssociatedFundName()
	{
		return this.associatedFundName;
		
	}

	/**
	 * @param associatedFundName the associatedFundName to set
	 */
        public void setAssociatedFundName(String associatedFundName)
        {
                this.associatedFundName = associatedFundName;

        }

        /**
         * Owning journal accessor.
         *
         * @return the journal containing this transaction
         */
        public Journal getJournal()
        {
                return this.journal;
        }

        /**
         * Sets the owning journal. Primarily used when adding a transaction to a journal.
         *
         * @param journal the journal to set
         */
        public void setJournal(Journal journal)
        {
                this.journal = journal;
        }

	/**
	 * @return
	 */
	public static BigDecimal countAccountBalance()
	{
		return BigDecimal.ZERO;
	}

}
