
package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

import lombok.Getter;
import lombok.Setter;

import lombok.NoArgsConstructor;
import java.io.Serializable;

@Getter          // Automatically generates getter methods
@Setter          // Automatically generates setter methods
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
	@JsonProperty final private List<AccountingTransaction> journalTransactions = new ArrayList<>();
	
	/**
	 * 
	 * @param transaction
	 */
	public void addTransaction(AccountingTransaction transaction)
	{
		checkNotNull(transaction);
		this.journalTransactions.add(transaction);
	}
	
	/**
	 * 
	 * @return
	 */
	public List<AccountingTransaction> getJournalTransactions()
	{
		return new ArrayList<>(this.journalTransactions);
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
