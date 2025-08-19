
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a transfer made to the SCA corporate office.
 */

public class TransferOutScaOfficeRow implements SupplementalRecord
{
	private String office;
	private String reason;
	private String checkNumber;
	private String checkDate;
	private BigDecimal amount;
	
	public TransferOutScaOfficeRow()
	{
	
	}
	
	public TransferOutScaOfficeRow(String office, String reason,
		String checkNumber, String checkDate, BigDecimal amount)
	{
		this.office = office;
		this.reason = reason;
		this.checkNumber = checkNumber;
		this.checkDate = checkDate;
		this.amount = amount;
		
	}
	
	public String getOffice()
	{
		return office;
		
	}
	
	public void setOffice(String office)
	{
		this.office = office;
		
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
