package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

public class TrialBalanceRowBean {
    private String accountNumber;
    private String accountName;
    private BigDecimal debit;
    private BigDecimal credit;

    // Constructor
    public TrialBalanceRowBean(String accountNumber, String accountName, BigDecimal debit, BigDecimal credit) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.debit = debit != null ? debit : BigDecimal.ZERO; // Ensure non-null
        this.credit = credit != null ? credit : BigDecimal.ZERO; // Ensure non-null
    }

    // Getters (required by JRBeanCollectionDataSource)
    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    // Setters (optional, but good practice for a JavaBean)
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit != null ? debit : BigDecimal.ZERO;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit != null ? credit : BigDecimal.ZERO;
    }
}
