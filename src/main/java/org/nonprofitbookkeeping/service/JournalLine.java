package org.nonprofitbookkeeping.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A presentational general-journal line, derived from TxnSplit + Account.normalBalance.
 * This is what you show when you click “Journal View” in any screen.
 */
public class JournalLine
{
    private final LocalDate date;
    private final Long txnId;
    private final String memo;
    private final String payee;
    private final String accountCode;
    private final String accountName;
    private final String fundCode;
    private final String fundName;
    private final BigDecimal debit;
    private final BigDecimal credit;

    public JournalLine(LocalDate date,
                       Long txnId,
                       String memo,
                       String payee,
                       String accountCode,
                       String accountName,
                       String fundCode,
                       String fundName,
                       BigDecimal debit,
                       BigDecimal credit)
    {
        this.date = date;
        this.txnId = txnId;
        this.memo = memo;
        this.payee = payee;
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.fundCode = fundCode;
        this.fundName = fundName;
        this.debit = debit;
        this.credit = credit;
    }

    public LocalDate getDate() { return this.date; }
    public Long getTxnId() { return this.txnId; }
    public String getMemo() { return this.memo; }
    public String getPayee() { return this.payee; }
    public String getAccountCode() { return this.accountCode; }
    public String getAccountName() { return this.accountName; }
    public String getFundCode() { return this.fundCode; }
    public String getFundName() { return this.fundName; }
    public BigDecimal getDebit() { return this.debit; }
    public BigDecimal getCredit() { return this.credit; }
}
