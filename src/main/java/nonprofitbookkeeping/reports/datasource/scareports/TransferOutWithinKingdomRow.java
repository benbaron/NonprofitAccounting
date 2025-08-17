
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a transfer made to another SCA account within the same
 * kingdom.
 */
public class TransferOutWithinKingdomRow extends ScaRowBase {
	private String accountOrPayee;
	private String reason;
	private String checkNumber;
	private String checkDate;
	private BigDecimal amount;
	
	public TransferOutWithinKingdomRow()
	{
	
	}
	
	public TransferOutWithinKingdomRow(String accountOrPayee, String reason,
		String checkNumber, String checkDate, BigDecimal amount)
	{
		this.accountOrPayee = accountOrPayee;
		this.reason = reason;
		this.checkNumber = checkNumber;
		this.checkDate = checkDate;
		this.amount = amount;
		
	}
	
	public String getAccountOrPayee()
	{
		return accountOrPayee;
		
	}
	
	public void setAccountOrPayee(String accountOrPayee)
	{
		this.accountOrPayee = accountOrPayee;
		
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
