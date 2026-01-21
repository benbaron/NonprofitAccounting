package nonprofitbookkeeping.ui.javafx.supplemental;

import java.math.BigDecimal;

public class EntryRef
{
	private final long entryId;
	private final String accountName;
	private final boolean debit;
	private final BigDecimal amount;

	public EntryRef(long entryId, String accountName, boolean debit, BigDecimal amount)
	{
		this.entryId = entryId;
		this.accountName = accountName;
		this.debit = debit;
		this.amount = amount;
	}

	public long getEntryId()
	{
		return this.entryId;
	}

	public String getAccountName()
	{
		return this.accountName;
	}

	public boolean isDebit()
	{
		return this.debit;
	}

	public BigDecimal getAmount()
	{
		return this.amount;
	}

	@Override
	public String toString()
	{
		String side = this.debit ? "DR" : "CR";
		return this.accountName + " (" + side + " " + this.amount + ")";
	}
}
