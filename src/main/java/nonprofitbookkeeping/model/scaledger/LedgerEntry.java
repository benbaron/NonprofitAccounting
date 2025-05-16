package nonprofitbookkeeping.model.scaledger;

public class LedgerEntry
{
    public String entryDate;
    public String checkNumber;
    public String cleared;
    public String toFrom;
    public String memoString;
    public String budgetTracking;   
    
    public double amount;
    public String assetAccount;
    public String incomeAccount;
    public String expenseAccount;
    public String fundName;
    
    public double amount2;
    public String assetAccount2;
    public String incomeAccount2;
    public String expenseAccount2;
    public String fundName2;
    
    public double amount3;
    public String assetAccount3;
    public String incomeAccount3;
    public String expenseAccount3;
    public String fundName3;
    
    public double amount4;
    public String assetAccount4;
    public String incomeAccount4;
    public String expenseAccount4;
    public String fundName4;

    public LedgerEntry()
    {
    	
    }
    
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

    // Returns the entry date as an Object (in practice, a Date)
    public String getEntryDate()
    {
        return this.entryDate;
    }

    public String getCheckNumber()
    {
        return this.checkNumber;
    }

    public String isCleared()
    {
        return this.cleared;
    }

    public String getToFrom()
    {
        return this.toFrom;
    }

    public String getMemoString()
    {
        return this.memoString;
    }

    public String getBudgetTracking()
    {
        return this.budgetTracking;
    }

    public double getAmount()
    {
        return this.amount;
    }

    public String getAssetAccount()
    {
        return this.assetAccount;
    }

    public String getIncomeAccount()
    {
        return this.incomeAccount;
    }

    public String getExpenseAccount()
    {
        return this.expenseAccount;
    }

    public String getFundName()
    {
        return this.fundName;
    }

    public void setCheckNumber(String valueAt)
    {
        this.checkNumber = valueAt;
    }

    public void setCleared(String boolean1)
    {
        this.cleared = boolean1;
    }

    public void setToFrom(String valueAt)
    {
        this.toFrom = valueAt;
    }

    public void setMemoString(String valueAt)
    {
        this.memoString = valueAt;
    }

    public void setBudgetTracking(String valueAt)
    {
        this.budgetTracking = valueAt;
    }

    public void setAmount(double double1)
    {
        this.amount = double1;
    }

    public void setAssetAccount(String valueAt)
    {
        this.assetAccount = valueAt;
    }

    public void setIncomeAccount(String valueAt)
    {
        this.incomeAccount = valueAt;
    }

    public void setExpenseAccount(String valueAt)
    {
        this.expenseAccount = valueAt;
    }

    public void setFundName(String valueAt)
    {
        this.fundName = valueAt;
    }
	/**
	 * @param entryDate the entryDate to set
	 */
	public void setEntryDate(String entryDate)
	{
		this.entryDate = entryDate;
	}
}
