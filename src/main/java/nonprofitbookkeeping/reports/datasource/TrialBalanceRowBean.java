package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

/**
 * Represents a single row in a Trial Balance report.
 * This bean is used as a data source for report generation, typically with JasperReports or similar libraries.
 * It includes fields for the account number, account name, and the corresponding debit or credit balance.
 */
public class TrialBalanceRowBean {
    /** The account number. */
    private String accountNumber;
    /** The name of the account. */
    private String accountName;
    /** The total debit balance for the account. Initialized to BigDecimal.ZERO if null is passed to constructor or setter. */
    private BigDecimal debit;
    /** The total credit balance for the account. Initialized to BigDecimal.ZERO if null is passed to constructor or setter. */
    private BigDecimal credit;

    /**
     * Constructs a {@code TrialBalanceRowBean}.
     * Initializes debit and credit amounts to {@link BigDecimal#ZERO} if null values are provided.
     *
     * @param accountNumber The account number.
     * @param accountName The name of the account.
     * @param debit The debit balance for the account. If null, defaults to BigDecimal.ZERO.
     * @param credit The credit balance for the account. If null, defaults to BigDecimal.ZERO.
     */
    public TrialBalanceRowBean(String accountNumber, String accountName, BigDecimal debit, BigDecimal credit) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.debit = debit != null ? debit : BigDecimal.ZERO; // Ensure non-null
        this.credit = credit != null ? credit : BigDecimal.ZERO; // Ensure non-null
    }

    /**
     * Gets the account number for this row.
     * @return The account number string.
     */
    public String getAccountNumber() {
        return this.accountNumber;
    }

    /**
     * Gets the account name for this row.
     * @return The account name string.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Gets the debit balance for this account.
     * @return The debit amount as a {@link BigDecimal}. Will not be null.
     */
    public BigDecimal getDebit() {
        return this.debit;
    }

    /**
     * Gets the credit balance for this account.
     * @return The credit amount as a {@link BigDecimal}. Will not be null.
     */
    public BigDecimal getCredit() {
        return this.credit;
    }

    /**
     * Sets the account number for this row.
     * @param accountNumber The account number string to set.
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * Sets the account name for this row.
     * @param accountName The account name string to set.
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * Sets the debit balance for this account.
     * If a null value is provided, the debit balance will be set to {@link BigDecimal#ZERO}.
     * @param debit The debit amount to set.
     */
    public void setDebit(BigDecimal debit) {
        this.debit = debit != null ? debit : BigDecimal.ZERO;
    }

    /**
     * Sets the credit balance for this account.
     * If a null value is provided, the credit balance will be set to {@link BigDecimal#ZERO}.
     * @param credit The credit amount to set.
     */
    public void setCredit(BigDecimal credit) {
        this.credit = credit != null ? credit : BigDecimal.ZERO;
    }
}
