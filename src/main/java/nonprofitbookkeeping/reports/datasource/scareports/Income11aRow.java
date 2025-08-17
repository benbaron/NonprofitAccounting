
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

public class Income11aRow implements SupplementalRecord
{
	private java.lang.String category;
	private java.lang.String fromOrPlace;
	private java.lang.String activity;
	private BigDecimal amount;
	
	public Income11aRow()
	{
		
	}
	
	public java.lang.String getCategory()
	{
		return category;
		
	}
	
	public void setCategory(java.lang.String v)
	{
		this.category = v;
		
	}
	
	public java.lang.String getFromOrPlace()
	{
		return fromOrPlace;
		
	}
	
	public void setFromOrPlace(java.lang.String v)
	{
		this.fromOrPlace = v;
		
	}
	
	public java.lang.String getActivity()
	{
		return activity;
		
	}
	
	public void setActivity(java.lang.String v)
	{
		this.activity = v;
		
	}
	
	public BigDecimal getAmount()
	{
		return amount;
		
	}
	
	public void setAmount(BigDecimal v)
	{
		this.amount = v;
		
	}
	
}
