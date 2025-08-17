
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Generic row used by the TRANSFER_IN_9 report. The {@code section} field
 * distinguishes between transfers from within the kingdom and those from
 * outside the kingdom.
 */
public class TransferIn9Row implements SupplementalRecord
{
	private String section;
	private String account;
	private String checkNumber;
	private String checkDate;
	private BigDecimal amount;
	
	public TransferIn9Row()
	{
	
	}
	
	public String getSection()
	{
		return section;
		
	}
	
	public void setSection(String section)
	{
		this.section = section;
		
	}
	
	public String getAccount()
	{
		return account;
		
	}
	
	public void setAccount(String account)
	{
		this.account = account;
		
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

