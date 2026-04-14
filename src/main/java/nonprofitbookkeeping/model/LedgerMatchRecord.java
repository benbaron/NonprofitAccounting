package nonprofitbookkeeping.model;

import java.time.LocalDateTime;

public class LedgerMatchRecord
{
	private String ledgerRecordId;
	private String ledgerId;
	private Long journalEntryId;
	private String bankIdRecordId;
	private String bankingRecordId;
	private String matchGroupId;
	private String matchMethod;
	private String reviewerUser;
	private LocalDateTime reviewedAt;
	private String linkStatus;

	public String getLedgerRecordId(){ return this.ledgerRecordId; }
	public void setLedgerRecordId(String ledgerRecordId){ this.ledgerRecordId = ledgerRecordId; }
	public String getLedgerId(){ return this.ledgerId; }
	public void setLedgerId(String ledgerId){ this.ledgerId = ledgerId; }
	public Long getJournalEntryId(){ return this.journalEntryId; }
	public void setJournalEntryId(Long journalEntryId){ this.journalEntryId = journalEntryId; }
	public String getBankIdRecordId(){ return this.bankIdRecordId; }
	public void setBankIdRecordId(String bankIdRecordId){ this.bankIdRecordId = bankIdRecordId; }
	public String getBankingRecordId(){ return this.bankingRecordId; }
	public void setBankingRecordId(String bankingRecordId){ this.bankingRecordId = bankingRecordId; }
	public String getMatchGroupId(){ return this.matchGroupId; }
	public void setMatchGroupId(String matchGroupId){ this.matchGroupId = matchGroupId; }
	public String getMatchMethod(){ return this.matchMethod; }
	public void setMatchMethod(String matchMethod){ this.matchMethod = matchMethod; }
	public String getReviewerUser(){ return this.reviewerUser; }
	public void setReviewerUser(String reviewerUser){ this.reviewerUser = reviewerUser; }
	public LocalDateTime getReviewedAt(){ return this.reviewedAt; }
	public void setReviewedAt(LocalDateTime reviewedAt){ this.reviewedAt = reviewedAt; }
	public String getLinkStatus(){ return this.linkStatus; }
	public void setLinkStatus(String linkStatus){ this.linkStatus = linkStatus; }
}
