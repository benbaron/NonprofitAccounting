
package nonprofitbookkeeping.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import java.io.Serializable;

/**
 * Represents a set of accounts and their transactions.
 */
@Data
final public class Ledger implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 8752049840895321935L;

	@JsonProperty final private Journal journal = new Journal();
	
	/**
	 * getTransactions
	 * @return list of transactions
	 */
	public List<AccountingTransaction> getTransactions()
	{
		return this.journal.getJournalTransactions();
	}


	/**
	 * getJournal
	 * @return the journal
	 */
	public Journal getJournal()
	{
		return this.journal;
	}
	
}
