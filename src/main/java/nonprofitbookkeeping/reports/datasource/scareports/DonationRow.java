
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

public class DonationRow
{
	private String organizationName;
	private String fedIdNumber;
	private String reason;
	private String checkNumber;
	private String checkDate;
	private BigDecimal amount;
	
	public DonationRow()
	{
		
	}
	
	public DonationRow(String organizationName, String fedIdNumber,
		String reason,
		String checkNumber, String checkDate, BigDecimal amount)
	{
		this.organizationName = organizationName;
		this.fedIdNumber = fedIdNumber;
		this.reason = reason;
		this.checkNumber = checkNumber;
		this.checkDate = checkDate;
		this.amount = amount;
		
	}
	
	/**  
	 * Constructor DonationRow
	 * @param string
	 * @param string2
	 * @param bigDecimal
	 */
	public DonationRow(String string, String string2, BigDecimal bigDecimal)
	{
		// TODO Auto-generated constructor stub
		
	}
	
	public String getOrganizationName()
	{
		return organizationName;
		
	}
	
	public void setOrganizationName(String organizationName)
	{
		this.organizationName = organizationName;
		
	}
	
	public String getFedIdNumber()
	{
		return fedIdNumber;
		
	}
	
	public void setFedIdNumber(String fedIdNumber)
	{
		this.fedIdNumber = fedIdNumber;
		
	}
	
	public String getReason()
	{
		return reason;
		
	}
	
	public void setReason(String reason)
	{
		this.reason = reason;
		
	}
	
	public String getCheckNumber()
	{
		return checkNumber;
		
	}
	
	public void setCheckNumber(String checkNumber)
	{
		this.checkNumber = checkNumber;
		
	}
	
	public String getCheckDate()
	{
		return checkDate;
		
	}
	
	public void setCheckDate(String checkDate)
	{
		this.checkDate = checkDate;
		
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
