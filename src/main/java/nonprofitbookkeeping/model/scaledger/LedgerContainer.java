package nonprofitbookkeeping.model.scaledger;

import java.util.ArrayList;
import java.util.List;

/**
 * A container class designed to hold {@link LedgerEntry} objects, organized by fiscal quarters.
 * It provides separate lists for ledger entries corresponding to Q1, Q2, Q3, and Q4.
 */
public class LedgerContainer
{
    /**  
	 * Constructs a new LedgerContainer.
	 * Initializes the lists for each quarter's ledger entries as empty ArrayLists.
	 */
	public LedgerContainer()
	{
	}
	/** List of ledger entries for the first quarter. Initialized to an empty ArrayList. */
	public List<LedgerEntry> ledgerQ1 = new ArrayList<>();
	/** List of ledger entries for the second quarter. Initialized to an empty ArrayList. */
    public List<LedgerEntry> ledgerQ2 = new ArrayList<>();
	/** List of ledger entries for the third quarter. Initialized to an empty ArrayList. */
    public List<LedgerEntry> ledgerQ3 = new ArrayList<>();
	/** List of ledger entries for the fourth quarter. Initialized to an empty ArrayList. */
    public List<LedgerEntry> ledgerQ4 = new ArrayList<>();

	/**
	 * Gets the list of ledger entries for the first quarter (Q1).
	 * @return A list of {@link LedgerEntry} for Q1.
	 */
	public List<LedgerEntry> getLedgerQ1()
	{
		return this.ledgerQ1;
	}
	/**
	 * Sets the list of ledger entries for the first quarter (Q1).
	 * @param ledgerQ1 A list of {@link LedgerEntry} for Q1.
	 */
	public void setLedgerQ1(List<LedgerEntry> ledgerQ1)
	{
		this.ledgerQ1 = ledgerQ1;
	}
	/**
	 * Gets the list of ledger entries for the second quarter (Q2).
	 * @return A list of {@link LedgerEntry} for Q2.
	 */
	public List<LedgerEntry> getLedgerQ2()
	{
		return this.ledgerQ2;
	}
	/**
	 * Sets the list of ledger entries for the second quarter (Q2).
	 * @param ledgerQ2 A list of {@link LedgerEntry} for Q2.
	 */
	public void setLedgerQ2(List<LedgerEntry> ledgerQ2)
	{
		this.ledgerQ2 = ledgerQ2;
	}
	/**
	 * Gets the list of ledger entries for the third quarter (Q3).
	 * @return A list of {@link LedgerEntry} for Q3.
	 */
	public List<LedgerEntry> getLedgerQ3()
	{
		return this.ledgerQ3;
	}
	/**
	 * Sets the list of ledger entries for the third quarter (Q3).
	 * @param ledgerQ3 A list of {@link LedgerEntry} for Q3.
	 */
	public void setLedgerQ3(List<LedgerEntry> ledgerQ3)
	{
		this.ledgerQ3 = ledgerQ3;
	}
	/**
	 * Gets the list of ledger entries for the fourth quarter (Q4).
	 * @return A list of {@link LedgerEntry} for Q4.
	 */
	public List<LedgerEntry> getLedgerQ4()
	{
		return this.ledgerQ4;
	}
	/**
	 * Sets the list of ledger entries for the fourth quarter (Q4).
	 * @param ledgerQ4 A list of {@link LedgerEntry} for Q4.
	 */
	public void setLedgerQ4(List<LedgerEntry> ledgerQ4)
	{
		this.ledgerQ4 = ledgerQ4;
	}
}