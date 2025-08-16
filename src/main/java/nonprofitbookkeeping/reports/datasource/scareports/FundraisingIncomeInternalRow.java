
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing fundraising income generated from an internal SCA event.
 */
public class FundraisingIncomeInternalRow
{
	private String event;
	private String activity;
	private BigDecimal amount;
	
	public FundraisingIncomeInternalRow()
	{
	
	}
	
	public FundraisingIncomeInternalRow(String event, String activity,
		BigDecimal amount)
	{
		this.event = event;
		this.activity = activity;
		this.amount = amount;
		
	}
	
	public String getEvent()
	{
		return event;
		
	}
	
	public void setEvent(String event)
	{
		this.event = event;
		
	}
	
	public String getActivity()
	{
		return activity;
		
	}
	
	public void setActivity(String activity)
	{
		this.activity = activity;
		
	}
	
	public BigDecimal getAmount()
	{
		return amount;
		
	}
	
	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
		
	}
	
}
