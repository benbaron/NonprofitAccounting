package org.nonprofitbookkeeping.model;

import jakarta.persistence.*;
import java.time.*;
import java.math.*;


@Entity
@Table(name = "fund_transfer",
       indexes = {
           @Index(name = "ix_ft_date", columnList = "transfer_date"),
           @Index(name = "ix_ft_posted", columnList = "posted_txn_id")
       })
/**
 * Represents the FundTransfer component in the nonprofit bookkeeping application.
 */
public class FundTransfer
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "from_fund_id", nullable = false)
    private Fund fromFund;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "to_fund_id", nullable = false)
    private Fund toFund;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(length = 500)
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FundTransferStatus status = FundTransferStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_txn_id")
    private Txn postedTxn;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Transient
    private FundTransferStatus originalStatus;

    @PostLoad
    void captureOriginalStatus()
    {
        this.originalStatus = this.status;
    }

    @PrePersist
    @PreUpdate
    void validateIntegrity()
    {
        if (this.status == null)
        {
            throw new IllegalStateException("FundTransfer status is required.");
        }
        if (this.amount == null || this.amount.signum() <= 0)
        {
            throw new IllegalStateException("FundTransfer amount must be positive.");
        }
        if (this.fromFund == null || this.toFund == null)
        {
            throw new IllegalStateException("FundTransfer from/to fund references are required.");
        }
        Long fromFundId = this.fromFund.getId();
        Long toFundId = this.toFund.getId();
        if (this.fromFund == this.toFund || (fromFundId != null && fromFundId.equals(toFundId)))
        {
            throw new IllegalStateException("FundTransfer from/to funds must be distinct.");
        }
        if (this.status == FundTransferStatus.POSTED && this.postedTxn == null)
        {
            throw new IllegalStateException("POSTED FundTransfer requires postedTxn.");
        }
        if (this.status != FundTransferStatus.POSTED && this.postedTxn != null)
        {
            throw new IllegalStateException("Only POSTED FundTransfer can reference postedTxn.");
        }
        if (this.originalStatus != null && !FundTransferStatus.isTransitionAllowed(this.originalStatus, this.status))
        {
            throw new IllegalStateException(
                "Illegal FundTransfer status transition: " + this.originalStatus + " -> " + this.status);
        }
        touchUpdatedAt();
    }

    public Long getId() { return this.id; }
    public LocalDate getTransferDate() { return this.transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }
    public Fund getFromFund() { return this.fromFund; }
    public void setFromFund(Fund fromFund) { this.fromFund = fromFund; }
    public Fund getToFund() { return this.toFund; }
    public void setToFund(Fund toFund) { this.toFund = toFund; }
    public BigDecimal getAmount() { return this.amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMemo() { return this.memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public FundTransferStatus getStatus() { return this.status; }
    public void setStatus(FundTransferStatus status)
    {
        if (this.status != null && status != null
            && !FundTransferStatus.isTransitionAllowed(this.status, status))
        {
            throw new IllegalStateException(
                "Illegal FundTransfer status transition: " + this.status + " -> " + status);
        }
        this.status = status;
    }
    public Txn getPostedTxn() { return this.postedTxn; }
    public void setPostedTxn(Txn postedTxn) { this.postedTxn = postedTxn; }
    public Instant getCreatedAt() { return this.createdAt; }
    public Instant getUpdatedAt() { return this.updatedAt; }
    public void touchUpdatedAt() { this.updatedAt = Instant.now(); }
}
