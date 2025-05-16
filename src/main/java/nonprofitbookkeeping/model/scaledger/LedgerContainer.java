package nonprofitbookkeeping.model.scaledger;

import java.util.ArrayList;
import java.util.List;

public class LedgerContainer
{
    /**  
	 * Constructor LedgerContainer
	 */
	public LedgerContainer()
	{
	}
	public List<LedgerEntry> ledgerQ1 = new ArrayList<>();
    public List<LedgerEntry> ledgerQ2 = new ArrayList<>();
    public List<LedgerEntry> ledgerQ3 = new ArrayList<>();
    public List<LedgerEntry> ledgerQ4 = new ArrayList<>();
	/**
	 * @return the ledgerQ1
	 */
	public List<LedgerEntry> getLedgerQ1()
	{
		return this.ledgerQ1;
	}
	/**
	 * @param ledgerQ1 the ledgerQ1 to set
	 */
	public void setLedgerQ1(List<LedgerEntry> ledgerQ1)
	{
		this.ledgerQ1 = ledgerQ1;
	}
	/**
	 * @return the ledgerQ2
	 */
	public List<LedgerEntry> getLedgerQ2()
	{
		return this.ledgerQ2;
	}
	/**
	 * @param ledgerQ2 the ledgerQ2 to set
	 */
	public void setLedgerQ2(List<LedgerEntry> ledgerQ2)
	{
		this.ledgerQ2 = ledgerQ2;
	}
	/**
	 * @return the ledgerQ3
	 */
	public List<LedgerEntry> getLedgerQ3()
	{
		return this.ledgerQ3;
	}
	/**
	 * @param ledgerQ3 the ledgerQ3 to set
	 */
	public void setLedgerQ3(List<LedgerEntry> ledgerQ3)
	{
		this.ledgerQ3 = ledgerQ3;
	}
	/**
	 * @return the ledgerQ4
	 */
	public List<LedgerEntry> getLedgerQ4()
	{
		return this.ledgerQ4;
	}
	/**
	 * @param ledgerQ4 the ledgerQ4 to set
	 */
	public void setLedgerQ4(List<LedgerEntry> ledgerQ4)
	{
		this.ledgerQ4 = ledgerQ4;
	}
}