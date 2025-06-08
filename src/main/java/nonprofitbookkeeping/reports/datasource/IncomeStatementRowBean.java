package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

/**
 * Represents a single row in an Income Statement (Profit & Loss) report.
 * This bean is used as a data source for report generation, typically with JasperReports or similar libraries.
 * It includes fields for the account category (e.g., "Income", "Expense"), account name, and the amount.
 */
public class IncomeStatementRowBean {
    /** The category of the account (e.g., "Income", "Cost of Goods Sold", "Operating Expense"). */
    private String accountCategory;
    /** The specific name of the account (e.g., "Donations", "Salaries", "Rent"). */
    private String accountName;
    /** The monetary amount for this account line in the report period. */
    private BigDecimal amount;

    /**
     * Constructs an {@code IncomeStatementRowBean}.
     *
     * @param accountCategory The category of the account.
     * @param accountName The name of the account.
     * @param amount The monetary amount for this row.
     */
    public IncomeStatementRowBean(String accountCategory, String accountName, BigDecimal amount) {
        this.accountCategory = accountCategory;
        this.accountName = accountName;
        this.amount = amount;
    }

    /**
     * Gets the account category for this row.
     * @return The account category string.
     */
    public String getAccountCategory() {
        return accountCategory;
    }

    /**
     * Gets the account name for this row.
     * @return The account name string.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Gets the monetary amount for this row.
     * @return The amount as a {@link BigDecimal}.
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Sets the account category for this row.
     * @param accountCategory The account category string to set.
     */
    public void setAccountCategory(String accountCategory) {
        this.accountCategory = accountCategory;
    }

    /**
     * Sets the account name for this row.
     * @param accountName The account name string to set.
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * Sets the monetary amount for this row.
     * @param amount The amount to set.
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
