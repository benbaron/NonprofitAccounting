/**
 * NonprofitAccounting
 * EventIncomeRow.java
 * EventIncomeRow
 */
package nonprofitbookkeeping.reports.datasource.scareports;


import java.math.BigDecimal;

/** Row representing one event’s income (gross & net). */
public final class EventIncomeRow extends IncomeRowBase
{
	
	private BigDecimal net; // after expenses
	
	public EventIncomeRow()
	{
	
	}
	
	public EventIncomeRow(String eventName,
		BigDecimal gross,
		BigDecimal net)
	{
		super(eventName, gross);
		this.net = net;
		
	}
	
	// additional column
	public BigDecimal getNet()
	{
		return net;
		
	}
	
	public void setNet(BigDecimal n)
	{
		this.net = n;
		
	}
	
}
