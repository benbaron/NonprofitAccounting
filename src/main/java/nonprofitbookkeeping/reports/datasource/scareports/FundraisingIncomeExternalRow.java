
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing fundraising income generated from an activity outside the kingdom.
 */
public class FundraisingIncomeExternalRow extends ScaRowBase {
	private String place;
	private String activity;
	private BigDecimal amount;
	
	public FundraisingIncomeExternalRow()
	{
	
	}
	
	public FundraisingIncomeExternalRow(String place, String activity,
		BigDecimal amount)
	{
		this.place = place;
		this.activity = activity;
		this.amount = amount;
		
	}
	
	public String getPlace()
	{
		return place;
		
	}
	
	public void setPlace(String place)
	{
		this.place = place;
		
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
