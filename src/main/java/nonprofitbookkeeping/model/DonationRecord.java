package nonprofitbookkeeping.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Donation workflow record linked to posted journal transaction.
 */
public class DonationRecord
{
	private String donationId;
	private String donorExternalId;
	private LocalDate donationDate;
	private BigDecimal amount;
	private String memo;
	private String cashAccountNumber;
	private String revenueAccountNumber;
	private String fundNumber;
	private Integer journalTxnId;
	private boolean receiptRequired = true;
	private LocalDateTime receiptSentAt;

	public String getDonationId(){ return this.donationId; }
	public void setDonationId(String donationId){ this.donationId = donationId; }
	public String getDonorExternalId(){ return this.donorExternalId; }
	public void setDonorExternalId(String donorExternalId){ this.donorExternalId = donorExternalId; }
	public LocalDate getDonationDate(){ return this.donationDate; }
	public void setDonationDate(LocalDate donationDate){ this.donationDate = donationDate; }
	public BigDecimal getAmount(){ return this.amount; }
	public void setAmount(BigDecimal amount){ this.amount = amount; }
	public String getMemo(){ return this.memo; }
	public void setMemo(String memo){ this.memo = memo; }
	public String getCashAccountNumber(){ return this.cashAccountNumber; }
	public void setCashAccountNumber(String cashAccountNumber){ this.cashAccountNumber = cashAccountNumber; }
	public String getRevenueAccountNumber(){ return this.revenueAccountNumber; }
	public void setRevenueAccountNumber(String revenueAccountNumber){ this.revenueAccountNumber = revenueAccountNumber; }
	public String getFundNumber(){ return this.fundNumber; }
	public void setFundNumber(String fundNumber){ this.fundNumber = fundNumber; }
	public Integer getJournalTxnId(){ return this.journalTxnId; }
	public void setJournalTxnId(Integer journalTxnId){ this.journalTxnId = journalTxnId; }
	public boolean isReceiptRequired(){ return this.receiptRequired; }
	public void setReceiptRequired(boolean receiptRequired){ this.receiptRequired = receiptRequired; }
	public LocalDateTime getReceiptSentAt(){ return this.receiptSentAt; }
	public void setReceiptSentAt(LocalDateTime receiptSentAt){ this.receiptSentAt = receiptSentAt; }
}
