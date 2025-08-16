
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing income from demonstrations and activity fees.
 */
public class DemoActivityIncomeRow
{
	private String from;
	private String activity;
	private BigDecimal amount;
	
	public DemoActivityIncomeRow()
	{
	
	}
	
	public DemoActivityIncomeRow(String from, String activity,
		BigDecimal amount)
	{
		this.from = from;
		this.activity = activity;
		this.amount = amount;
		
	}
	
	public String getFrom()
	{
		return from;
		
	}
	
	public void setFrom(String from)
	{
		this.from = from;
		
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
