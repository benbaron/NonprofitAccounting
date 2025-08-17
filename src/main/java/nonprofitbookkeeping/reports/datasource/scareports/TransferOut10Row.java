
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Generic row used by the TRANSFER_OUT_10 report. The {@code section} field
 * distinguishes transfers within the kingdom, outside the kingdom, or to the
 * SCA corporate office.
 */
public class TransferOut10Row extends ScaRowBase {
	private String section;
	private String toAccountOrPayee;
	private String reason;
	private String checkNumber;
	private String checkDate;
	private BigDecimal amount;
	
	public TransferOut10Row()
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
	
	public String getToAccountOrPayee()
	{
		return toAccountOrPayee;
		
	}
	
	public void setToAccountOrPayee(String toAccountOrPayee)
	{
		this.toAccountOrPayee = toAccountOrPayee;
		
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
