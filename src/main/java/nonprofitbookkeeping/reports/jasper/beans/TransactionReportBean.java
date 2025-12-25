package nonprofitbookkeeping.reports.jasper.beans;

import java.math.BigDecimal;

/**
 * Basic bean for the TransactionReport template.
 */
public class TransactionReportBean
{
	private String date;
	private String memo;
	private String accountNumber;
	private BigDecimal amount;
	
	public TransactionReportBean()
	{
	}
	
	public TransactionReportBean(String date, String memo,
		String accountNumber, BigDecimal amount)
	{
		this.date = date;
		this.memo = memo;
		this.accountNumber = accountNumber;
		this.amount = amount;
	}
	
	public String getDate()
	{
		return this.date;
	}
	
	public void setDate(String date)
	{
		this.date = date;
	}
	
	public String getMemo()
	{
		return this.memo;
	}
	
	public void setMemo(String memo)
	{
		this.memo = memo;
	}
	
	public String getAccountNumber()
	{
		return this.accountNumber;
	}
	
	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}
	
	public BigDecimal getAmount()
	{
		return this.amount;
	}
	
	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}
}
