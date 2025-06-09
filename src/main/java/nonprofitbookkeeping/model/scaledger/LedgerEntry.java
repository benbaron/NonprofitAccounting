package nonprofitbookkeeping.model.scaledger;

/**
 * Represents a single entry in a ledger, potentially supporting splits across multiple accounts or funds.
 * This class holds details such as date, check number, payee/payer, memo, budget tracking information,
 * and amounts distributed to various account and fund combinations.
 * The structure with fields like `amount2`, `assetAccount2`, etc., suggests it can handle
 * up to four splits or components for a single transaction.
 */
public class LedgerEntry
{
    /** The date of the ledger entry (e.g., "YYYY-MM-DD"). */
    public String entryDate;
    /** The check number associated with the entry, if applicable. */
    public String checkNumber;
    /** A string indicating whether the transaction has cleared (e.g., "Yes", "No", "Reconciled"). */
    public String cleared;
    /** The payee or payer of the transaction. */
    public String toFrom;
    /** A memo or description for the entry. */
    public String memoString;
    /** Information related to budget tracking for this entry. */
    public String budgetTracking;   
    
    // First part of a potentially split entry
    /** The amount for the first part of the entry. */
    public double amount;
    /** The asset account for the first part of the entry. */
    public String assetAccount;
    /** The income account for the first part of the entry. */
    public String incomeAccount;
    /** The expense account for the first part of the entry. */
    public String expenseAccount;
    /** The fund name associated with the first part of the entry. */
    public String fundName;
    
    // Second part of a potentially split entry
    /** The amount for the second part of the entry, if applicable. */
    public double amount2;
    /** The asset account for the second part of the entry, if applicable. */
    public String assetAccount2;
    /** The income account for the second part of the entry, if applicable. */
    public String incomeAccount2;
    /** The expense account for the second part of the entry, if applicable. */
    public String expenseAccount2;
    /** The fund name associated with the second part of the entry, if applicable. */
    public String fundName2;
    
    // Third part of a potentially split entry
    /** The amount for the third part of the entry, if applicable. */
    public double amount3;
    /** The asset account for the third part of the entry, if applicable. */
    public String assetAccount3;
    /** The income account for the third part of the entry, if applicable. */
    public String incomeAccount3;
    /** The expense account for the third part of the entry, if applicable. */
    public String expenseAccount3;
    /** The fund name associated with the third part of the entry, if applicable. */
    public String fundName3;
    
    // Fourth part of a potentially split entry
    /** The amount for the fourth part of the entry, if applicable. */
    public double amount4;
    /** The asset account for the fourth part of the entry, if applicable. */
    public String assetAccount4;
    /** The income account for the fourth part of the entry, if applicable. */
    public String incomeAccount4;
    /** The expense account for the fourth part of the entry, if applicable. */
    public String expenseAccount4;
    /** The fund name associated with the fourth part of the entry, if applicable. */
    public String fundName4;

    /**
     * Default constructor for LedgerEntry.
     * Initializes all fields to their default values (null for Strings, 0.0 for doubles).
     */
    public LedgerEntry()
    {
    	
    }
    
    /**
     * Determines the primary account type based on which account field (asset, income, or expense) is populated for the first part of the entry.
     * It checks {@code assetAccount}, then {@code incomeAccount}, then {@code expenseAccount}.
     * If none are populated, it returns "Unknown".
     * @return A string representing the account type ("Asset", "Income", "Expense", or "Unknown").
     */
    public String getAccountType()
    {
        if (this.assetAccount != null && !this.assetAccount.isEmpty())
        {
            return "Asset";
        }
        if (this.incomeAccount != null && !this.incomeAccount.isEmpty())
        {
            return "Income";
        }
        if (this.expenseAccount != null && !this.expenseAccount.isEmpty())
        {
            return "Expense";
        }
        return "Unknown";
    }

    /**
     * Gets the entry date.
     * @return The entry date string.
     */
    public String getEntryDate()
    {
        return this.entryDate;
    }

    /**
     * Gets the check number associated with this entry.
     * @return The check number string, or null if not applicable.
     */
    public String getCheckNumber()
    {
        return this.checkNumber;
    }

    /**
     * Gets the cleared status of this entry.
     * @return The cleared status string (e.g., "Yes", "No").
     */
    public String isCleared()
    {
        return this.cleared;
    }

    /**
     * Gets the payee or payer of this transaction.
     * @return The 'to/from' party string.
     */
    public String getToFrom()
    {
        return this.toFrom;
    }

    /**
     * Gets the memo or description for this entry.
     * @return The memo string.
     */
    public String getMemoString()
    {
        return this.memoString;
    }

    /**
     * Gets the budget tracking information for this entry.
     * @return The budget tracking string.
     */
    public String getBudgetTracking()
    {
        return this.budgetTracking;
    }

    /**
     * Gets the amount for the first part of this entry.
     * @return The amount as a double.
     */
    public double getAmount()
    {
        return this.amount;
    }

    /**
     * Gets the asset account for the first part of this entry.
     * @return The asset account string.
     */
    public String getAssetAccount()
    {
        return this.assetAccount;
    }

    /**
     * Gets the income account for the first part of this entry.
     * @return The income account string.
     */
    public String getIncomeAccount()
    {
        return this.incomeAccount;
    }

    /**
     * Gets the expense account for the first part of this entry.
     * @return The expense account string.
     */
    public String getExpenseAccount()
    {
        return this.expenseAccount;
    }

    /**
     * Gets the fund name associated with the first part of this entry.
     * @return The fund name string.
     */
    public String getFundName()
    {
        return this.fundName;
    }

    /**
     * Sets the check number for this entry.
     * @param valueAt The check number string to set.
     */
    public void setCheckNumber(String valueAt)
    {
        this.checkNumber = valueAt;
    }

    /**
     * Sets the cleared status for this entry.
     * @param boolean1 The cleared status string to set (e.g., "Yes", "No").
     */
    public void setCleared(String boolean1)
    {
        this.cleared = boolean1;
    }

    /**
     * Sets the payee or payer for this transaction.
     * @param valueAt The 'to/from' party string to set.
     */
    public void setToFrom(String valueAt)
    {
        this.toFrom = valueAt;
    }

    /**
     * Sets the memo or description for this entry.
     * @param valueAt The memo string to set.
     */
    public void setMemoString(String valueAt)
    {
        this.memoString = valueAt;
    }

    /**
     * Sets the budget tracking information for this entry.
     * @param valueAt The budget tracking string to set.
     */
    public void setBudgetTracking(String valueAt)
    {
        this.budgetTracking = valueAt;
    }

    /**
     * Sets the amount for the first part of this entry.
     * @param double1 The amount to set.
     */
    public void setAmount(double double1)
    {
        this.amount = double1;
    }

    /**
     * Sets the asset account for the first part of this entry.
     * @param valueAt The asset account string to set.
     */
    public void setAssetAccount(String valueAt)
    {
        this.assetAccount = valueAt;
    }

    /**
     * Sets the income account for the first part of this entry.
     * @param valueAt The income account string to set.
     */
    public void setIncomeAccount(String valueAt)
    {
        this.incomeAccount = valueAt;
    }

    /**
     * Sets the expense account for the first part of this entry.
     * @param valueAt The expense account string to set.
     */
    public void setExpenseAccount(String valueAt)
    {
        this.expenseAccount = valueAt;
    }

    /**
     * Sets the fund name associated with the first part of this entry.
     * @param valueAt The fund name string to set.
     */
    public void setFundName(String valueAt)
    {
        this.fundName = valueAt;
    }
	/**
	 * Sets the entry date.
	 * @param entryDate The entry date string to set (e.g., "YYYY-MM-DD").
	 */
	public void setEntryDate(String entryDate)
	{
		this.entryDate = entryDate;
	}
}
