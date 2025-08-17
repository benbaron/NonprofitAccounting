
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Advertising row for the NEWSLETTER_15 report capturing a single
 * advertisement sold in the publication.
 */
public class Newsletter15Row implements SupplementalRecord
{
	private String advertiserName;
	private String adSize;
	private String issuesOrVolume;
	private BigDecimal amount;
	private String checkNo;
	private String checkDate;
	
	public Newsletter15Row()
	{
	
	}
	
	public Newsletter15Row(String advertiserName,
		String adSize,
		String issuesOrVolume,
		BigDecimal amount,
		String checkNo,
		String checkDate)
	{
		this.advertiserName = advertiserName;
		this.adSize = adSize;
		this.issuesOrVolume = issuesOrVolume;
		this.amount = amount;
		this.checkNo = checkNo;
		this.checkDate = checkDate;
		
	}
	
	public String getAdvertiserName()
	{
		return advertiserName;
		
	}
	
	public void setAdvertiserName(String advertiserName)
	{
		this.advertiserName = advertiserName;
		
	}
	
	public String getAdSize()
	{
		return adSize;
		
	}
	
	public void setAdSize(String adSize)
	{
		this.adSize = adSize;
		
	}
	
	public String getIssuesOrVolume()
	{
		return issuesOrVolume;
		
	}
	
	public void setIssuesOrVolume(String issuesOrVolume)
	{
		this.issuesOrVolume = issuesOrVolume;
		
	}
	
	public BigDecimal getAmount()
	{
		return amount;
		
	}
	
	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
		
	}
	
	public String getCheckNo()
	{
		return checkNo;
		
	}
	
	public void setCheckNo(String checkNo)
	{
		this.checkNo = checkNo;
		
	}
	
	public String getCheckDate()
	{
		return checkDate;
		
	}
	
	public void setCheckDate(String checkDate)
	{
		this.checkDate = checkDate;
		
	}
	
}
