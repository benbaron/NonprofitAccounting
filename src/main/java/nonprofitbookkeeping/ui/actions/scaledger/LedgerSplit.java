package nonprofitbookkeeping.ui.actions.scaledger;

import java.math.BigDecimal;

/**
 * One accounting leg ("split") from a single ledger row.
 *
 * Each split has:
 *  - amount
 *  - an account/category classification
 *      (assetLiabilityAccount OR incomeCategory OR expenseCategory;
 *       usually only one of these is set)
 *  - a fund (e.g. "General Fund")
 *
 * We ALSO store canonicalCategory after translation for downstream
 * NonprofitAccounting / SCALedger use.
 */
public class LedgerSplit
{
    /** Raw amount from the sheet (M, R, X, AC columns etc.). */
    private BigDecimal amount;

    /** Raw Asset/Liability Account from dropdown (N / S / Y / AD columns). */
    private String assetLiabilityAccount;

    /** Raw Income Category from dropdown (O / T / Z / AE columns). */
    private String incomeCategory;

    /** Raw Expense Category from dropdown (P / U / AA / AF columns). */
    private String expenseCategory;

    /** Raw Fund value from dropdown (Q / V / AB / AG columns). */
    private String fund;

    /**
     * Canonical account/category name after applying ChartTranslationMap.
     * We resolve which column was actually populated and map that literal
     * string to a canonical chart-of-accounts string.
     *
     * We do NOT alter punctuation when we store canonical keys.
     */
    private String canonicalCategory;

    public LedgerSplit()
    {
    }

    public BigDecimal getAmount()
    {
        return this.amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getAssetLiabilityAccount()
    {
        return this.assetLiabilityAccount;
    }

    public void setAssetLiabilityAccount(String assetLiabilityAccount)
    {
        this.assetLiabilityAccount = assetLiabilityAccount;
    }

    public String getIncomeCategory()
    {
        return this.incomeCategory;
    }

    public void setIncomeCategory(String incomeCategory)
    {
        this.incomeCategory = incomeCategory;
    }

    public String getExpenseCategory()
    {
        return this.expenseCategory;
    }

    public void setExpenseCategory(String expenseCategory)
    {
        this.expenseCategory = expenseCategory;
    }

    public String getFund()
    {
        return this.fund;
    }

    public void setFund(String fund)
    {
        this.fund = fund;
    }

    public String getCanonicalCategory()
    {
        return this.canonicalCategory;
    }

    public void setCanonicalCategory(String canonicalCategory)
    {
        this.canonicalCategory = canonicalCategory;
    }

    /**
     * Returns whichever raw category cell was actually populated in the sheet:
     *  - assetLiabilityAccount
     *  - or incomeCategory
     *  - or expenseCategory
     *
     * The ledger UI is designed so that only one of these will be set
     * for a given split.
     */
    public String getPrimaryRawCategory()
    {
        if (this.assetLiabilityAccount != null && !this.assetLiabilityAccount.isBlank())
        {
            return this.assetLiabilityAccount;
        }
        if (this.incomeCategory != null && !this.incomeCategory.isBlank())
        {
            return this.incomeCategory;
        }
        if (this.expenseCategory != null && !this.expenseCategory.isBlank())
        {
            return this.expenseCategory;
        }
        return null;
    }

    /**
     * Convenience: true if this split is effectively empty
     * (no amount and no category).
     */
    public boolean isEmpty()
    {
        boolean amountIsZero = (this.amount == null
            || this.amount.compareTo(java.math.BigDecimal.ZERO) == 0);

        String primary = getPrimaryRawCategory();
        boolean noCategory = (primary == null || primary.isBlank());

        return amountIsZero && noCategory;
    }
}
