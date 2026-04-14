package nonprofitbookkeeping.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BankingTransactionRecord
{
	private String bankingRecordId;
	private String bankIdRecordId;
	private Long statementId;
	private Integer journalTxnId;
	private Long fundId;
	private LocalDate transactionDate;
	private String externalTransactionId;
	private String sourceFingerprint;
	private String normalizedDescription;
	private BigDecimal amount;
	private String matchStatus;
	private LocalDateTime matchedAt;
	private boolean anomalyDuplicate;
	private boolean anomalyAmountOutlier;
	private boolean anomalyDateOutlier;

	public String getBankingRecordId(){ return this.bankingRecordId; }
	public void setBankingRecordId(String bankingRecordId){ this.bankingRecordId = bankingRecordId; }
	public String getBankIdRecordId(){ return this.bankIdRecordId; }
	public void setBankIdRecordId(String bankIdRecordId){ this.bankIdRecordId = bankIdRecordId; }
	public Long getStatementId(){ return this.statementId; }
	public void setStatementId(Long statementId){ this.statementId = statementId; }
	public Integer getJournalTxnId(){ return this.journalTxnId; }
	public void setJournalTxnId(Integer journalTxnId){ this.journalTxnId = journalTxnId; }
	public Long getFundId(){ return this.fundId; }
	public void setFundId(Long fundId){ this.fundId = fundId; }
	public LocalDate getTransactionDate(){ return this.transactionDate; }
	public void setTransactionDate(LocalDate transactionDate){ this.transactionDate = transactionDate; }
	public String getExternalTransactionId(){ return this.externalTransactionId; }
	public void setExternalTransactionId(String externalTransactionId){ this.externalTransactionId = externalTransactionId; }
	public String getSourceFingerprint(){ return this.sourceFingerprint; }
	public void setSourceFingerprint(String sourceFingerprint){ this.sourceFingerprint = sourceFingerprint; }
	public String getNormalizedDescription(){ return this.normalizedDescription; }
	public void setNormalizedDescription(String normalizedDescription){ this.normalizedDescription = normalizedDescription; }
	public BigDecimal getAmount(){ return this.amount; }
	public void setAmount(BigDecimal amount){ this.amount = amount; }
	public String getMatchStatus(){ return this.matchStatus; }
	public void setMatchStatus(String matchStatus){ this.matchStatus = matchStatus; }
	public LocalDateTime getMatchedAt(){ return this.matchedAt; }
	public void setMatchedAt(LocalDateTime matchedAt){ this.matchedAt = matchedAt; }
	public boolean isAnomalyDuplicate(){ return this.anomalyDuplicate; }
	public void setAnomalyDuplicate(boolean anomalyDuplicate){ this.anomalyDuplicate = anomalyDuplicate; }
	public boolean isAnomalyAmountOutlier(){ return this.anomalyAmountOutlier; }
	public void setAnomalyAmountOutlier(boolean anomalyAmountOutlier){ this.anomalyAmountOutlier = anomalyAmountOutlier; }
	public boolean isAnomalyDateOutlier(){ return this.anomalyDateOutlier; }
	public void setAnomalyDateOutlier(boolean anomalyDateOutlier){ this.anomalyDateOutlier = anomalyDateOutlier; }
}
