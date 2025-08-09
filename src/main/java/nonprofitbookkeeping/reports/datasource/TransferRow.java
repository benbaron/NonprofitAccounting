/**
 * NonprofitAccounting
 * TransferRow.java
 * TransferRow
 */
package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

/** For Transfer In / Transfer Out worksheets. */
public final class TransferRow extends IncomeRowBase
{
	
	private String counterparty; // branch or fund
	
	public TransferRow()
	{
	
	}
	
	public TransferRow(String purpose,
		String counterparty,
		BigDecimal amount)
	{
		super(purpose, amount);
		this.counterparty = counterparty;
		
	}
	
	public String getCounterparty()
	{
		return counterparty;
		
	}
	
	public void setCounterparty(String c)
	{
		this.counterparty = c;
		
	}
	
	@Override public BigDecimal getValue()
	{
		return BigDecimal.ZERO;		
	}
	
}
