/**
 * NonprofitAccounting PrimaryAccountReconciliation2aBean.java
 * PrimaryAccountReconciliation2aBean
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

public class PrimaryAccountReconciliation2aBean
{
	// Header info
	private String bankName;
	private String bankAccountTitle;
	private String bankAccountType;
	private String bankAccountNumber;
	private String bankBranchPhoneAndName;
	
	// Statement / reconciliation inputs
	private BigDecimal balanceFromBankStatement;
	
	// Deposits not cleared (flattened: 6 slots)
	private String deposit1Date;
	private BigDecimal deposit1Amount;
	private String deposit2Date;
	private BigDecimal deposit2Amount;
	private String deposit3Date;
	private BigDecimal deposit3Amount;
	private String deposit4Date;
	private BigDecimal deposit4Amount;
	private String deposit5Date;
	private BigDecimal deposit5Amount;
	private String deposit6Date;
	private BigDecimal deposit6Amount;
	
	// Checks not cleared (flattened: 16 slots)
	private String check1Number;
	private String check1Date;
	private BigDecimal check1Amount;
	
	private String check2Number;
	private String check2Date;
	private BigDecimal check2Amount;
	
	// ... repeat up through check16
	private String check3Number;
	private String check3Date;
	private BigDecimal check3Amount;
	
	private String check4Number;
	private String check4Date;
	private BigDecimal check4Amount;
	
	private String check5Number;
	private String check5Date;
	private BigDecimal check5Amount;
	
	private String check6Number;
	private String check6Date;
	private BigDecimal check6Amount;
	
	private String check7Number;
	private String check7Date;
	private BigDecimal check7Amount;
	
	private String check8Number;
	private String check8Date;
	private BigDecimal check8Amount;
	
	private String check9Number;
	private String check9Date;
	private BigDecimal check9Amount;
	
	private String check10Number;
	private String check10Date;
	private BigDecimal check10Amount;
	
	private String check11Number;
	private String check11Date;
	private BigDecimal check11Amount;
	
	private String check12Number;
	private String check12Date;
	private BigDecimal check12Amount;
	
	private String check13Number;
	private String check13Date;
	private BigDecimal check13Amount;
	
	private String check14Number;
	private String check14Date;
	private BigDecimal check14Amount;
	
	private String check15Number;
	private String check15Date;
	private BigDecimal check15Amount;
	
	private String check16Number;
	private String check16Date;
	private BigDecimal check16Amount;
	
	private BigDecimal endingLedgerOrRegisterBalance;
	private String accountEarnsInterest; // could be "YES"/"NO" or adapt to
											// Boolean if you prefer
	
	// Totals / computed values (filled upstream)
	private BigDecimal depositsNotClearedTotal;
	private BigDecimal checksNotClearedTotal;
	private BigDecimal adjustedAccountBalance;
	private BigDecimal reconciliationDifference;
	
	// Getters and setters (only showing a subset; generate the rest similarly)
	public String getBankName()
	{
		return bankName;
		
	}
	
	public void setBankName(String bankName)
	{
		this.bankName = bankName;
		
	}
	
	public String getBankAccountTitle()
	{
		return bankAccountTitle;
		
	}
	
	public void setBankAccountTitle(String bankAccountTitle)
	{
		this.bankAccountTitle = bankAccountTitle;
		
	}
	
	public BigDecimal getBalanceFromBankStatement()
	{
		return balanceFromBankStatement;
		
	}
	
	public void setBalanceFromBankStatement(BigDecimal balanceFromBankStatement)
	{
		this.balanceFromBankStatement = balanceFromBankStatement;
		
	}
	
	public String getDeposit1Date()
	{
		return deposit1Date;
		
	}
	
	public void setDeposit1Date(String deposit1Date)
	{
		this.deposit1Date = deposit1Date;
		
	}
	
	public BigDecimal getDeposit1Amount()
	{
		return deposit1Amount;
		
	}
	
	public void setDeposit1Amount(BigDecimal deposit1Amount)
	{
		this.deposit1Amount = deposit1Amount;
		
	}
	
	// ... (other deposit/check getters & setters)
	
	public BigDecimal getEndingLedgerOrRegisterBalance()
	{
		return endingLedgerOrRegisterBalance;
		
	}
	
	public void setEndingLedgerOrRegisterBalance(
		BigDecimal endingLedgerOrRegisterBalance)
	{
		this.endingLedgerOrRegisterBalance = endingLedgerOrRegisterBalance;
		
	}
	
	public String getAccountEarnsInterest()
	{
		return accountEarnsInterest;
		
	}
	
	public void setAccountEarnsInterest(String accountEarnsInterest)
	{
		this.accountEarnsInterest = accountEarnsInterest;
		
	}
	
	public BigDecimal getDepositsNotClearedTotal()
	{
		return depositsNotClearedTotal;
		
	}
	
	public void setDepositsNotClearedTotal(BigDecimal depositsNotClearedTotal)
	{
		this.depositsNotClearedTotal = depositsNotClearedTotal;
		
	}
	
	public BigDecimal getChecksNotClearedTotal()
	{
		return checksNotClearedTotal;
		
	}
	
	public void setChecksNotClearedTotal(BigDecimal checksNotClearedTotal)
	{
		this.checksNotClearedTotal = checksNotClearedTotal;
		
	}
	
	public BigDecimal getAdjustedAccountBalance()
	{
		return adjustedAccountBalance;
		
	}
	
	public void setAdjustedAccountBalance(BigDecimal adjustedAccountBalance)
	{
		this.adjustedAccountBalance = adjustedAccountBalance;
		
	}
	
	public BigDecimal getReconciliationDifference()
	{
		return reconciliationDifference;
		
	}
	
	public void setReconciliationDifference(BigDecimal reconciliationDifference)
	{
		this.reconciliationDifference = reconciliationDifference;
		
	}

        public void setBankAccountType(String bankAccountType)
        {
                this.bankAccountType = bankAccountType;
        }

        public void setBankAccountNumber(String bankAccountNumber)
        {
                this.bankAccountNumber = bankAccountNumber;
        }

        public void setBankBranchPhoneAndName(String bankBranchPhoneAndName)
        {
                this.bankBranchPhoneAndName = bankBranchPhoneAndName;
        }
	
	// (Add remaining getters/setters for all checkN* fields)
}
