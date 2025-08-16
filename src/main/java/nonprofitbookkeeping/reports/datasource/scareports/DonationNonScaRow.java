
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a donation received from a non-SCA individual or organization.
 */
public class DonationNonScaRow
{
	private String donorName;
	private String description;
	private String notes;
	private BigDecimal amount;
	
	public DonationNonScaRow()
	{
	
	}
	
	public DonationNonScaRow(String donorName, String description, String notes,
		BigDecimal amount)
	{
		this.donorName = donorName;
		this.description = description;
		this.notes = notes;
		this.amount = amount;
		
	}
	
	public String getDonorName()
	{
		return donorName;
		
	}
	
	public void setDonorName(String donorName)
	{
		this.donorName = donorName;
		
	}
	
	public String getDescription()
	{
		return description;
		
	}
	
	public void setDescription(String description)
	{
		this.description = description;
		
	}
	
	public String getNotes()
	{
		return notes;
		
	}
	
	public void setNotes(String notes)
	{
		this.notes = notes;
		
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
