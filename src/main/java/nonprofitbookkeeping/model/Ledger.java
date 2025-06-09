
package nonprofitbookkeeping.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import java.io.Serializable;

/**
 * Represents a ledger, which primarily consists of a {@link Journal} containing all
 * accounting transactions. This class acts as a container for the journal.
 * Lombok's {@code @Data} annotation is used for generating boilerplate code like
 * getters, setters (if applicable), {@code toString()}, {@code equals()}, and {@code hashCode()}.
 */
@Data
final public class Ledger implements Serializable
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = 8752049840895321935L;

	/**
	 * The journal containing all accounting transactions for this ledger.
	 * This field is final and initialized to a new Journal instance.
	 * It is marked with {@code @JsonProperty} for serialization/deserialization.
	 */
	@JsonProperty final private Journal journal = new Journal();
	
	/**
	 * Retrieves all transactions recorded in this ledger's journal.
	 * This is a convenience method that delegates to the underlying journal.
	 * @return A list of {@link AccountingTransaction}s.
	 */
	public List<AccountingTransaction>getTransactions()
	{
		return this.journal.getJournalTransactions();
	}


	/**
	 * Gets the journal associated with this ledger.
	 * The journal contains all the accounting transactions.
	 * @return The {@link Journal} instance.
	 */
	public Journal getJournal()
	{
		return this.journal;
	}
	
}
