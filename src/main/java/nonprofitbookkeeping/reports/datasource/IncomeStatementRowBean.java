package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

public class IncomeStatementRowBean {
    private String accountCategory; // e.g., "Income", "Expense"
    private String accountName;
    private BigDecimal amount;

    // Constructor
    public IncomeStatementRowBean(String accountCategory, String accountName, BigDecimal amount) {
        this.accountCategory = accountCategory;
        this.accountName = accountName;
        this.amount = amount;
    }

    // Getters (required by JRBeanCollectionDataSource)
    public String getAccountCategory() {
        return accountCategory;
    }

    public String getAccountName() {
        return accountName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    // Setters (optional, but good practice for a JavaBean)
    public void setAccountCategory(String accountCategory) {
        this.accountCategory = accountCategory;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
