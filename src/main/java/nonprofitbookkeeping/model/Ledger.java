
package nonprofitbookkeeping.model;

import java.util.List;
import java.util.ArrayList;

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

       /**
        * Retrieves all {@link AccountingEntry} objects for a given account
        * number. This method walks through every {@link AccountingTransaction}
        * in the journal and collects entries whose {@code accountNumber}
        * matches the supplied argument.
        *
        * @param accountNumber the account number whose entries should be
        *        returned
        * @return a list of matching {@link AccountingEntry} instances. The list
        *         will be empty if no entries are found or if the ledger has no
        *         transactions.
        */
       public List<AccountingEntry> getEntriesForAccount(String accountNumber)
       {
               List<AccountingEntry> result = new ArrayList<>();

               if (accountNumber == null || accountNumber.isBlank())
               {
                       return result;
               }

               List<AccountingTransaction> transactions = getTransactions();
               if (transactions == null)
               {
                       return result;
               }

               for (AccountingTransaction transaction : transactions)
               {
                       if (transaction == null || transaction.getEntries() == null)
                               continue;

                       for (AccountingEntry entry : transaction.getEntries())
                       {
                               if (entry == null)
                                       continue;

                               if (accountNumber.equals(entry.getAccountNumber()))
                               {
                                       result.add(entry);
                               }
                       }
               }

               return result;
       }
	
}
