
package nonprofitbookkeeping.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents a set of accounts and their transactions.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
final public class Ledger implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 8752049840895321935L;

	final private Journal journal = new Journal();
	
	/**  
	 * Constructor Ledger
	 */
	public Ledger()
	{
	}
	
	
	public List<AccountingTransaction>getTransactions()
	{
		return this.journal.getJournalTransactions();
	}
	
}
