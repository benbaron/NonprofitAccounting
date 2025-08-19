
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;


public class Income11bRow implements SupplementalRecord
{
	private String category;
	private String description;
	private String itemsOrCount;
	private BigDecimal amount;
	
	public Income11bRow()
	{
		
	}
	
	public String getCategory()
	{
		return category;
		
	}
	
	public void setCategory(String v)
	{
		this.category = v;
		
	}
	
	public String getDescription()
	{
		return description;
		
	}
	
	public void setDescription(String v)
	{
		this.description = v;
		
	}
	
	public String getItemsOrCount()
	{
		return itemsOrCount;
		
	}
	
	public void setItemsOrCount(String v)
	{
		this.itemsOrCount = v;
		
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
