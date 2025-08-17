
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

public class Income11cRow extends ScaRowBase {
	private String donorOrSource;
	private String description;
	private String inKindOrNotes;
	private BigDecimal amount;
	
	public Income11cRow()
	{
		
	}
	
	public String getDonorOrSource()
	{
		return donorOrSource;
		
	}
	
	public void setDonorOrSource(String v)
	{
		this.donorOrSource = v;
		
	}
	
	public String getDescription()
	{
		return description;
		
	}
	
	public void setDescription(String v)
	{
		this.description = v;
		
	}
	
	public String getInKindOrNotes()
	{
		return inKindOrNotes;
		
	}
	
	public void setInKindOrNotes(String v)
	{
		this.inKindOrNotes = v;
		
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
