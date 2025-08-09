
package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

/**
 * Bean for rows in the Bank Reconciliation report.
 */
public class BankReconciliationRowBean
{
	private String date;
	private String description;
	private BigDecimal deposit;
	private BigDecimal withdrawal;
	private BigDecimal balance;
	
	public BankReconciliationRowBean(String date, String description, BigDecimal deposit,
		BigDecimal withdrawal, BigDecimal balance)
	{
		this.date = date;
		this.description = description;
		this.deposit = deposit;
		this.withdrawal = withdrawal;
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
	
	public BigDecimal getDeposit()
	{
		return this.deposit;
	}
	
	public void setDeposit(BigDecimal deposit)
	{
		this.deposit = deposit;
	}
	
	public BigDecimal getWithdrawal()
	{
		return this.withdrawal;
	}
	
	public void setWithdrawal(BigDecimal withdrawal)
	{
		this.withdrawal = withdrawal;
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
