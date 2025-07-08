package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

/**
 * Row bean for the Account Ledger report.
 */
public class AccountLedgerRowBean {
    private String date;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal balance;

    public AccountLedgerRowBean(String date, String description,
                                BigDecimal debit, BigDecimal credit, BigDecimal balance) {
        this.date = date;
        this.description = description;
        this.debit = debit;
        this.credit = credit;
        this.balance = balance;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getDebit() { return debit; }
    public void setDebit(BigDecimal debit) { this.debit = debit; }

    public BigDecimal getCredit() { return credit; }
    public void setCredit(BigDecimal credit) { this.credit = credit; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
