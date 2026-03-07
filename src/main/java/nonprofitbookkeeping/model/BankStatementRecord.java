package nonprofitbookkeeping.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BankStatementRecord
{
	private String bankName;
	private String accountLabel;
	private LocalDate statementDate;
	private BigDecimal statementBalance;
	private BigDecimal ledgerBalance;
	private BigDecimal outstanding;
	private BigDecimal bankAfterOutstanding;
	private BigDecimal difference;
	private String ledgerStatus;
	private String institutionName;
	private String institutionContact;
	private String accountNumber;
	private String accountType;
	private String signatureRequirement;
	private String interestBearing;
	private String currency;

	public String getBankName(){ return this.bankName; }
	public void setBankName(String bankName){ this.bankName = bankName; }
	public String getAccountLabel(){ return this.accountLabel; }
	public void setAccountLabel(String accountLabel){ this.accountLabel = accountLabel; }
	public LocalDate getStatementDate(){ return this.statementDate; }
	public void setStatementDate(LocalDate statementDate){ this.statementDate = statementDate; }
	public BigDecimal getStatementBalance(){ return this.statementBalance; }
	public void setStatementBalance(BigDecimal statementBalance){ this.statementBalance = statementBalance; }
	public BigDecimal getLedgerBalance(){ return this.ledgerBalance; }
	public void setLedgerBalance(BigDecimal ledgerBalance){ this.ledgerBalance = ledgerBalance; }
	public BigDecimal getOutstanding(){ return this.outstanding; }
	public void setOutstanding(BigDecimal outstanding){ this.outstanding = outstanding; }
	public BigDecimal getBankAfterOutstanding(){ return this.bankAfterOutstanding; }
	public void setBankAfterOutstanding(BigDecimal bankAfterOutstanding){ this.bankAfterOutstanding = bankAfterOutstanding; }
	public BigDecimal getDifference(){ return this.difference; }
	public void setDifference(BigDecimal difference){ this.difference = difference; }
	public String getLedgerStatus(){ return this.ledgerStatus; }
	public void setLedgerStatus(String ledgerStatus){ this.ledgerStatus = ledgerStatus; }
	public String getInstitutionName(){ return this.institutionName; }
	public void setInstitutionName(String institutionName){ this.institutionName = institutionName; }
	public String getInstitutionContact(){ return this.institutionContact; }
	public void setInstitutionContact(String institutionContact){ this.institutionContact = institutionContact; }
	public String getAccountNumber(){ return this.accountNumber; }
	public void setAccountNumber(String accountNumber){ this.accountNumber = accountNumber; }
	public String getAccountType(){ return this.accountType; }
	public void setAccountType(String accountType){ this.accountType = accountType; }
	public String getSignatureRequirement(){ return this.signatureRequirement; }
	public void setSignatureRequirement(String signatureRequirement){ this.signatureRequirement = signatureRequirement; }
	public String getInterestBearing(){ return this.interestBearing; }
	public void setInterestBearing(String interestBearing){ this.interestBearing = interestBearing; }
	public String getCurrency(){ return this.currency; }
	public void setCurrency(String currency){ this.currency = currency; }
}
