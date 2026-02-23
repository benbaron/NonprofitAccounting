
package nonprofitbookkeeping.ui.javafx.supplemental;

import java.math.BigDecimal;

// TODO: Auto-generated Javadoc
/**
 * The Class EntryRef.
 */
public class EntryRef
{
	
	/** The entry id. */
	private final long entryId;
	
	/** The account name. */
	private final String accountName;
	
	/** The debit. */
	private final boolean debit;
	
	/** The amount. */
	private final BigDecimal amount;
	
	/**
	 * Instantiates a new entry ref.
	 *
	 * @param entryId the entry id
	 * @param accountName the account name
	 * @param debit the debit
	 * @param amount the amount
	 */
	public EntryRef(long entryId, String accountName, boolean debit,
		BigDecimal amount)
	{
		this.entryId = entryId;
		this.accountName = accountName;
		this.debit = debit;
		this.amount = amount;
		
	}
	
	/**
	 * Gets the entry id.
	 *
	 * @return the entry id
	 */
	public long getEntryId()
	{
		return this.entryId;
		
	}
	
	/**
	 * Gets the account name.
	 *
	 * @return the account name
	 */
	public String getAccountName()
	{
		return this.accountName;
		
	}
	
	/**
	 * Checks if is debit.
	 *
	 * @return true, if is debit
	 */
	public boolean isDebit()
	{
		return this.debit;
		
	}
	
	/**
	 * Gets the amount.
	 *
	 * @return the amount
	 */
	public BigDecimal getAmount()
	{
		return this.amount;
		
	}
	
	/**
	 * Override @see java.lang.Object#toString() 
	 */
	@Override
	public String toString()
	{
		String side = this.debit ? "DR" : "CR";
		return this.accountName + " (" + side + " " + this.amount + ")";
		
	}
	
}
