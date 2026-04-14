package nonprofitbookkeeping.model;

public class BankIdentityRecord
{
	private String bankIdRecordId;
	private String bankId;
	private String bankName;
	private String accountId;
	private String accountType;

	public String getBankIdRecordId(){ return this.bankIdRecordId; }
	public void setBankIdRecordId(String bankIdRecordId){ this.bankIdRecordId = bankIdRecordId; }
	public String getBankId(){ return this.bankId; }
	public void setBankId(String bankId){ this.bankId = bankId; }
	public String getBankName(){ return this.bankName; }
	public void setBankName(String bankName){ this.bankName = bankName; }
	public String getAccountId(){ return this.accountId; }
	public void setAccountId(String accountId){ this.accountId = accountId; }
	public String getAccountType(){ return this.accountType; }
	public void setAccountType(String accountType){ this.accountType = accountType; }
}
