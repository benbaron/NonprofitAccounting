
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

@Getter // Automatically generates getter methods
@Setter // Automatically generates setter methods
@NoArgsConstructor // Generates a no-argument constructor

/**
 * Represents a collection of transactions.
 */
public class Journal implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -8125095337696271045L;
	// Ensure @JsonProperty is appropriate if this list is directly set
	// during deserialization.
	// If only add/update/delete methods are used post-construction, it's fine.
	@JsonProperty final private List<AccountingTransaction> journalTransactions = new ArrayList<>();
	
	/**
	 * 
	 * @param transaction
	 */
	public void addTransaction(AccountingTransaction transaction)
	{
		checkNotNull(transaction, "Transaction cannot be null");
		checkNotNull(transaction.getBookingDateTimestamp(), "Transaction ID cannot be null for add operation");
		this.journalTransactions.add(transaction);
	}
	
	/**
	 * 
	 * @return
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
	 * The transaction is identified by its ID.
	 *
	 * @param transaction The transaction with updated information. Must not be null and must have an ID.
	 * @return true if a transaction was found and updated, false otherwise.
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
	 * Deletes a transaction from the journal based on its ID.
	 *
	 * @param l The ID of the transaction to delete. Must not be null.
	 * @return true if a transaction was found and removed, false otherwise.
	 */
	public boolean deleteTransaction(long l)
	{
		checkNotNull(l, 
			"Transaction ID cannot be null for delete operation");
		return this.journalTransactions.removeIf(tx -> Objects.equals(tx.getBookingDateTimestamp(),
			l));
	}
	
	/**
	 * 
	 * Override @see java.lang.Object#toString()
	 */
	@Override public String toString()
	{
		return MoreObjects.toStringHelper(this)
			.add("transactions", this.journalTransactions)
			.toString();
	}
	
}
