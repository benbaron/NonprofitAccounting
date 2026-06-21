package org.nonprofitbookkeeping.model;

import jakarta.persistence.*;
import java.time.*;


@Entity
@Table(name = "txn",
       indexes = {
           @Index(name = "ix_txn_date", columnList = "txn_date"),
           @Index(name = "ix_txn_payee", columnList = "payee_id")
       })
/**
 * Represents the Txn component in the nonprofit bookkeeping application.
 */
public class Txn
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "txn_date", nullable = false)
    private LocalDate txnDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id")
    private Counterparty payee;

    @Column(length = 500)
    private String memo;

    /**
     * Bank register account (an ASSET account representing the specific bank/PayPal register).
     * This provides the “bank account” column users expect in the Ledger UI.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private Account bankAccount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() { return this.id; }
    public LocalDate getTxnDate() { return this.txnDate; }
    public void setTxnDate(LocalDate txnDate) { this.txnDate = txnDate; }
    public Counterparty getPayee() { return this.payee; }
    public void setPayee(Counterparty payee) { this.payee = payee; }
    public String getMemo() { return this.memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public Account getBankAccount() { return this.bankAccount; }
    public void setBankAccount(Account bankAccount) { this.bankAccount = bankAccount; }
    public Instant getCreatedAt() { return this.createdAt; }
    public Instant getUpdatedAt() { return this.updatedAt; }
    public void touchUpdatedAt() { this.updatedAt = Instant.now(); }
}
