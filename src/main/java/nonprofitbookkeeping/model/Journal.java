
package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Import for Objects.equals
import static com.google.common.base.Preconditions.checkNotNull;

import lombok.Getter;
import lombok.Setter;

import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.sql.Timestamp;

@Getter // Automatically generates getter methods
@Setter // Automatically generates setter methods
@NoArgsConstructor // Generates a no-argument constructor

/**
 * Represents a journal, which is a chronological record of {@link AccountingTransaction}s.
 * This class provides functionalities to add, retrieve, update, and delete transactions.
 * It uses Lombok for generating getters, setters, and a no-argument constructor.
 */
public class Journal implements Serializable
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = -8125095337696271045L;

	/**
	 * The list of accounting transactions recorded in this journal.
	 * This field is final and initialized to an empty ArrayList.

	 * It is marked with {@code @JsonProperty} for serialization/deserialization purposes.
	 * Direct modification of this list from outside the class is discouraged;
	 * use {@link #addTransaction(AccountingTransaction)}, {@link #updateTransaction(AccountingTransaction)},
	 * and {@link #deleteTransaction(long)}.
	 */
	@JsonProperty final private List<AccountingTransaction> journalTransactions = new ArrayList<>();
	
	/**
	 * Adds a new accounting transaction to the journal.
	 * @param transaction The {@link AccountingTransaction} to add. Must not be null, and its booking date timestamp (used as an ID here) must not be null.
	 * @throws NullPointerException if the transaction or its booking date timestamp is null.
	 */
	public void addTransaction(AccountingTransaction transaction)
	{
		checkNotNull(transaction, "Transaction cannot be null");
		checkNotNull(transaction.getBookingDateTimestamp(), "Transaction ID cannot be null for add operation"); // Assuming bookingDateTimestamp is used as a unique ID for some operations
		this.journalTransactions.add(transaction);
	}
	
	/**
	 * Retrieves a defensive copy of the list of all journal transactions.
	 * Modifying the returned list will not affect the journal's internal list of transactions.
	 * To modify the journal, use methods like {@link #addTransaction(AccountingTransaction)},
	 * {@link #updateTransaction(AccountingTransaction)}, or {@link #deleteTransaction(long)}.
	 * @return A new {@code List<AccountingTransaction>} containing all transactions in the journal.
	 */
	public List<AccountingTransaction> getJournalTransactions()
	{
		// Returns a defensive copy, so direct modification of the returned list won't
		// affect the original.
		// This is good practice. Callers will need to use add/update/delete methods of
		// this class.
		return new ArrayList<>(this.journalTransactions);
	}
	
	/**
	 * Updates an existing transaction in the journal.
	 * The transaction to be updated is identified by its booking date timestamp.
	 *
	 * @param transaction The transaction containing the updated information.
	 *                    Must not be null, and its booking date timestamp must not be null.
	 * @return {@code true} if a transaction with a matching booking date timestamp was found and updated, {@code false} otherwise.
	 * @throws NullPointerException if the input transaction or its booking date timestamp is null.
	 */
	public boolean updateTransaction(AccountingTransaction transaction)
	{
		checkNotNull(transaction, "Input transaction cannot be null for update");
		checkNotNull(transaction.getBookingDateTimestamp(),
			"Transaction ID cannot be null for update operation");
		
		for (int i = 0; i < this.journalTransactions.size(); i++)
		{
			AccountingTransaction existingTx = this.journalTransactions.get(i);
			
			if (Objects.equals(existingTx.getBookingDateTimestamp(), transaction.getBookingDateTimestamp()))
			{
				this.journalTransactions.set(i, transaction); // Replace the old transaction
				return true;
			}
			
		}
		
		return false; // Transaction with the given ID not found
	}
	
	/**
	 * Deletes a transaction from the journal based on its booking date timestamp.
	 *
	 * @param bookingDateTimestamp The booking date timestamp of the transaction to delete.
	 * @return {@code true} if a transaction with the given timestamp was found and removed, {@code false} otherwise.
	 */
	public boolean deleteTransaction(long bookingDateTimestamp)
	{
		// checkNotNull is not strictly needed for a primitive long, but if it were an Object Long:
		// checkNotNull(bookingDateTimestamp, "Transaction ID cannot be null for delete operation");
		return this.journalTransactions.removeIf(tx -> Objects.equals(tx.getBookingDateTimestamp(),
			bookingDateTimestamp));
	}
	
	/**
	 * Returns a string representation of the journal, including its transactions.
	 * @return A string summary of the journal.
	 */
	@Override public String toString()
	{
		return MoreObjects.toStringHelper(this)
			.add("transactions", this.journalTransactions)
			.toString();
	}

	/**
	 * @param any
	 */
        public void deleteTransaction(Timestamp any)
        {
                if (any != null)
                {
                        deleteTransaction(any.getTime());
                }

        }
	
}
