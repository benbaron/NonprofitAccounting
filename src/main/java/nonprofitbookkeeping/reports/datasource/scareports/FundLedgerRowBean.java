
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Bean representing a row in the Fund Ledger report.
 */
public class FundLedgerRowBean extends ScaRowBase {
	private String date;
	private String description;
	private BigDecimal debit;
	private BigDecimal credit;
	private BigDecimal balance;
	
	public FundLedgerRowBean(String date, String description, BigDecimal debit, BigDecimal credit,
		BigDecimal balance)
	{
		this.date = date;
		this.description = description;
		this.debit = debit;
		this.credit = credit;
		this.balance = balance;
	}
	
	public String getDate()
	{
		return this.date;
	}
	
	public void setDate(String date)
	{
		this.date = date;
	}
	
	public String getDescription()
	{
		return this.description;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public BigDecimal getDebit()
	{
		return this.debit;
	}
	
	public void setDebit(BigDecimal debit)
	{
		this.debit = debit;
	}
	
	public BigDecimal getCredit()
	{
		return this.credit;
	}
	
	public void setCredit(BigDecimal credit)
	{
		this.credit = credit;
	}
	
	public BigDecimal getBalance()
	{
		return this.balance;
	}
	
	public void setBalance(BigDecimal balance)
	{
		this.balance = balance;
	}
	
}
