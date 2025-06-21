
package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Objects; // Import for Objects.equals
import static com.google.common.base.Preconditions.checkNotNull;
import nonprofitbookkeeping.service.TransactionService;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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
@Entity
@Table(name = "journals")
public class Journal implements Serializable
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = -8125095337696271045L;
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        /** Primary key for the journal. */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /**
         * The list of accounting transactions recorded in this journal.
         * Direct modification of this list from outside the class is discouraged;
         * use {@link #addTransaction(AccountingTransaction)}, {@link #updateTransaction(AccountingTransaction)},
         * and {@link #deleteTransaction(long)}.
         */
        @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<AccountingTransaction> journalTransactions = new ArrayList<>();
=======
        /**
         * Transactions are persisted using {@link TransactionService}; the journal
         * does not keep an in-memory collection of them.
         */
>>>>>>> a0d4b45 Remove binary document and zip files
=======
	
	/**
	 * Transactions are persisted using {@link TransactionService}; the journal
	 * does not keep an in-memory collection of them.
	 */
>>>>>>> 3d1271c Merge branch 'feature/m2database' of https://github.com/benbaron/NonprofitAccounting.git into feature/m2database
	
	/**
	 * Adds a new accounting transaction to the journal.
	 * @param transaction The {@link AccountingTransaction} to add. Must not be null, and its booking date timestamp (used as an ID here) must not be null.
	 * @throws NullPointerException if the transaction or its booking date timestamp is null.
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        public void addTransaction(AccountingTransaction transaction)
        {
                checkNotNull(transaction, "Transaction cannot be null");
                checkNotNull(transaction.getBookingDateTimestamp(), "Transaction ID cannot be null for add operation");
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
                transaction.setJournal(this);
                this.journalTransactions.add(transaction);
=======
                try
                {
                        TransactionService.addTransaction(transaction);
                }
                catch (Exception ex)
                {
                        throw new RuntimeException("Failed to add transaction", ex);
                }
>>>>>>> a0d4b45 Remove binary document and zip files
        }
=======
	public void addTransaction(AccountingTransaction transaction)
	{
		checkNotNull(transaction, "Transaction cannot be null");
		checkNotNull(transaction.getBookingDateTimestamp(),
			"Transaction ID cannot be null for add operation");
		
		try
		{
			TransactionService.addTransaction(transaction);
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Failed to add transaction", ex);
		}
		
	}
>>>>>>> 3d1271c Merge branch 'feature/m2database' of https://github.com/benbaron/NonprofitAccounting.git into feature/m2database
	
	/**
	 * Retrieves a defensive copy of the list of all journal transactions.
	 * Modifying the returned list will not affect the journal's internal list of transactions.
	 * To modify the journal, use methods like {@link #addTransaction(AccountingTransaction)},
	 * {@link #updateTransaction(AccountingTransaction)}, or {@link #deleteTransaction(long)}.
	 * @return A new {@code List<AccountingTransaction>} containing all transactions in the journal.
	 */
	public List<AccountingTransaction> getJournalTransactions()
	{
		
		try
		{
			return TransactionService.getAllTransactions();
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Failed to load transactions", ex);
		}
		
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
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
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
                                transaction.setJournal(this);
                                this.journalTransactions.set(i, transaction); // Replace the old transaction
				return true;
			}
			
		}
		
		return false; // Transaction with the given ID not found
	}
=======
        public boolean updateTransaction(AccountingTransaction transaction)
        {
                checkNotNull(transaction, "Input transaction cannot be null for update");
                checkNotNull(transaction.getId(), "Transaction ID cannot be null for update operation");
                try
                {
                        TransactionService.updateTransaction(transaction);
                        return true;
                }
                catch (Exception ex)
                {
                        throw new RuntimeException("Failed to update transaction", ex);
                }
        }
>>>>>>> a0d4b45 Remove binary document and zip files
=======
	public boolean updateTransaction(AccountingTransaction transaction)
	{
		checkNotNull(transaction, "Input transaction cannot be null for update");
		checkNotNull(transaction.getId(), "Transaction ID cannot be null for update operation");
		
		try
		{
			TransactionService.updateTransaction(transaction);
			return true;
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Failed to update transaction", ex);
		}
		
	}
>>>>>>> 3d1271c Merge branch 'feature/m2database' of https://github.com/benbaron/NonprofitAccounting.git into feature/m2database
	
	/**
	 * Deletes a transaction from the journal based on its booking date timestamp.
	 *
	 * @param bookingDateTimestamp The booking date timestamp of the transaction to delete.
	 * @return {@code true} if a transaction with the given timestamp was found and removed, {@code false} otherwise.
	 */
	public boolean deleteTransaction(long bookingDateTimestamp)
	{
		
		try
		{
			
			// First fetch all transactions to find the matching id
			for (AccountingTransaction tx : TransactionService.getAllTransactions())
			{
				
				if (tx.getBookingDateTimestamp() == bookingDateTimestamp)
				{
					TransactionService.deleteTransaction(tx.getId());
					return true;
				}
				
			}
			
			return false;
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Failed to delete transaction", ex);
		}
		
	}
	
	/**
	 * Returns a string representation of the journal, including its transactions.
	 * @return A string summary of the journal.
	 */
	@Override public String toString()
	{
		return MoreObjects.toStringHelper(this)
			.add("transactions", "stored in database")
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
