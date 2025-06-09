/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * JournalEntry.java
 * JournalEntry
 */
package nonprofitbookkeeping.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single entry within a journal, detailing a transaction's impact
 * on a specific account. This typically includes the date, affected account,
 * debit or credit amount, and a memo.
 * Lombok's {@code @Data}, {@code @AllArgsConstructor}, and {@code @NoArgsConstructor}
 * are used for boilerplate code generation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalEntry
{
	/** The unique identifier for this journal entry. */
	@JsonProperty private String id;
	/** The date of the journal entry, typically in "YYYY-MM-DD" format. */
	@JsonProperty private String date;
	/** The debit amount. Should be non-negative. If this is a credit entry, debit might be zero or null. */
	@JsonProperty private BigDecimal debit;
	/** The credit amount. Should be non-negative. If this is a debit entry, credit might be zero or null. */
    @JsonProperty private BigDecimal credit;
    /** A descriptive memo or note for this journal entry. */
    @JsonProperty private String memo;
    /** The account name or number affected by this journal entry. */
    @JsonProperty private String account;


	/**
	 * Constructs a JournalEntry.
	 * Note: The parameter {@code value} is intended to be used for the account, but it's not assigned to the {@code account} field in this constructor.
	 * The {@code account} field will remain null unless set otherwise.
	 * @param id The unique identifier for the entry.
	 * @param date The date of the entry (e.g., "YYYY-MM-DD").
	 * @param value The account name or number (intended for the 'account' field, but not currently assigned there).
	 * @param debit The debit amount for the entry.
	 * @param credit The credit amount for the entry.
	 * @param text The memo or description for the entry (assigned to the 'memo' field).
	 */
	public JournalEntry(String id,
	                    String date,
	                    String value, // This parameter seems intended for the 'account' field
	                    BigDecimal debit,
	                    BigDecimal credit,
	                    String text)
	{
		this.id = id;
		this.date = date;
		this.debit = debit;
		this.credit = credit;
		this.memo = text;
		// this.account = value; // This line is missing to assign the 'value' to 'account'
	}

	/**
	 * Gets the unique identifier of this journal entry.
	 * @return The ID of the journal entry.
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * Gets the date of this journal entry.
	 * @return The date string (e.g., "YYYY-MM-DD").
	 */
	public String getDate()
	{
		return this.date;
	}

	/**
	 * Gets the account name or number associated with this journal entry.
	 * @return The account identifier.
	 */
	public String getAccount()
	{
		return this.account;
	}

	/**
	 * Gets the debit amount of this journal entry.
	 * @return The debit amount.
	 */
	public BigDecimal getDebit()
	{
		return this.debit;
	}

	/**
	 * Gets the credit amount of this journal entry.
	 * @return The credit amount.
	 */
	public BigDecimal getCredit()
	{
		return this.credit;
	}

	/**
	 * Gets the memo or description of this journal entry.
	 * @return The memo text.
	 */
	public String getMemo()
	{
		return this.memo;
	}

}
