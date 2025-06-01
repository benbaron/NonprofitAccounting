
package nonprofitbookkeeping.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	
	public List<AccountingTransaction>getTransactions()
	{
		return this.journal.getJournalTransactions();
	}


	/**
	 * @return
	 */
	public Journal getJournal()
	{
		return journal;
	}
	
}
