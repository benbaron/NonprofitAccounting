package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

/**
 * JavaBean representing a single entry in the General Journal report.
 */
public class GeneralJournalRowBean {
    private String date;
    private String account;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;

    public GeneralJournalRowBean(String date, String account, String description,
                                 BigDecimal debit, BigDecimal credit) {
        this.date = date;
        this.account = account;
        this.description = description;
        this.debit = debit;
        this.credit = credit;
    }

    public String getDate() { return this.date; }
    public void setDate(String date) { this.date = date; }

    public String getAccount() { return this.account; }
    public void setAccount(String account) { this.account = account; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getDebit() { return this.debit; }
    public void setDebit(BigDecimal debit) { this.debit = debit; }

    public BigDecimal getCredit() { return this.credit; }
    public void setCredit(BigDecimal credit) { this.credit = credit; }
}
