/**
 * nonprofit-scaledger-ribbon.zip_expanded JournalEntry.java JournalEntry
 */

package nonprofitbookkeeping.model;

import java.math.BigDecimal;
import java.util.Objects;

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
@Data @AllArgsConstructor @NoArgsConstructor public class JournalEntry
{
	/** The unique identifier for this journal entry. */
	@JsonProperty private String id;
	/** Identifier of the transaction this entry belongs to. */
	@JsonProperty private String transactionId;
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
	 *
	 * @param id            the unique identifier for this entry
	 * @param transactionId identifier of the parent transaction
	 * @param date          the entry date in {@code YYYY-MM-DD} format
	 * @param account       the account affected by this entry
	 * @param debit         debit amount or {@code null}
	 * @param credit        credit amount or {@code null}
	 * @param text          descriptive memo text
	 */
        public JournalEntry(String id, String transactionId, String date, String account,
                BigDecimal debit, BigDecimal credit, String text)
        {
                this.id = id;
                this.transactionId = Objects.requireNonNullElse(transactionId, id);
                this.date = date;
                this.account = account;
                this.debit = debit;
                this.credit = credit;
                this.memo = text;
	}
	
	/**
	 * Backwards compatible constructor that assumes the entry and transaction
	 * IDs are the same.
	 */
	public JournalEntry(String id, String date, String account, BigDecimal debit, BigDecimal credit,
		String text)
	{
                this(id, id, date, account, debit, credit, text);
        }

        private String normalizeTransactionId(String candidateId, String fallbackId)
        {
                if (candidateId == null || candidateId.isBlank())
                {
                        return fallbackId;
                }
                return candidateId;
        }

        /**
         * Sets the unique identifier of this journal entry.
         * Ensures that a missing transaction identifier continues to fall back to the
         * entry identifier, preserving compatibility with legacy data that only stored
         * a single identifier value.
         *
         * @param id the entry identifier to assign
         */
        public void setId(String id)
        {
                this.id = id;
                this.transactionId = normalizeTransactionId(this.transactionId, id);
        }

        /**
         * Sets the identifier of the transaction this entry belongs to.
         * A blank or {@code null} identifier automatically reuses the entry identifier
         * so existing data sets that omitted the field remain readable.
         *
         * @param transactionId the transaction identifier to assign
         */
        public void setTransactionId(String transactionId)
        {
                this.transactionId = normalizeTransactionId(transactionId, this.id);
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
	 * Gets the identifier of the transaction this entry belongs to.
	 *
	 * @return the transaction ID
	 */
	public String getTransactionId()
	{
		return this.transactionId;
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
