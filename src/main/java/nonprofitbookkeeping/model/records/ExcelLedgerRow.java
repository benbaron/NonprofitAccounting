
package nonprofitbookkeeping.model.records;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents a single row from an imported Excel ledger.
 * The row contains general transaction info and up to four
 * allocation groups consisting of amount and account details.
 */
@Data public class ExcelLedgerRow
{
	
	/** The balance. */
	private BigDecimal balance;
	/** Transaction date from the spreadsheet. */
	private LocalDate date;
	/** Optional check number. */
	private String checkNumber;
	/** Indicator of which bank cleared the transaction. */
	private String clearBank;
	/** Payee or source/destination. */
	private String toFrom;
	/** Memo or notes column. */
	private String memoNotes;
	/** Optional budget tracking value. */
	private String budgetTracking;
	
	/** Optional subtotal - should be the sum of the allocation amounts. */
	private BigDecimal netTotal;
	/** Optional sheet name for the ledger page. */
	private String sheetName;
	
	
	/** List of up to four allocation groups. */
	private List<Allocation> allocations = new ArrayList<>();
	
        /**
         * Constructor ExcelLedgerRow.
         */
        public ExcelLedgerRow()
        {
                this.balance = BigDecimal.ZERO;
                this.netTotal = BigDecimal.ZERO;
                this.allocations = new ArrayList<>();
        }
	

	/**
	 * Gets the balance.
	 *
	 * @return the balance
	 */
	public BigDecimal getBalance()
	{
		return this.balance;
		
	}
	
	/**
	 * Sets the balance.
	 *
	 * @param balance the balance to set
	 */
	public void setBalance(BigDecimal balance)
	{
		System.out.print(balance + ";");
		this.balance = balance;
		
	}
	
	/**
	 * Gets the date.
	 *
	 * @return the date
	 */
	public LocalDate getDate()
	{
		return this.date;
		
	}
	
	/**
	 * Sets the date.
	 *
	 * @param date the date to set
	 */
	public void setDate(LocalDate date)
	{
		System.out.print(date + ";");
		this.date = date;
		
	}
	
	/**
	 * Gets the check number.
	 *
	 * @return the checkNumber
	 */
	public String getCheckNumber()
	{
		return this.checkNumber;
		
	}
	
	/**
	 * Sets the check number.
	 *
	 * @param checkNumber the checkNumber to set
	 */
	public void setCheckNumber(String checkNumber)
	{
		System.out.print(checkNumber + ";");
		this.checkNumber = checkNumber;
		
	}
	
	/**
	 * Gets the clear bank.
	 *
	 * @return the clearBank
	 */
	public String getClearBank()
	{
		return this.clearBank;
		
	}
	
	/**
	 * Sets the clear bank.
	 *
	 * @param clearBank the clearBank to set
	 */
	public void setClearBank(String clearBank)
	{
		System.out.print(clearBank + ";");
		this.clearBank = clearBank;
		
	}
	
	/**
	 * Gets the to from.
	 *
	 * @return the toFrom
	 */
	public String getToFrom()
	{
		return this.toFrom;
		
	}
	
	/**
	 * Sets the to from.
	 *
	 * @param toFrom the toFrom to set
	 */
	public void setToFrom(String toFrom)
	{
		System.out.print(toFrom + ";");
		this.toFrom = toFrom;
		
	}
	
	/**
	 * Gets the memo notes.
	 *
	 * @return the memoNotes
	 */
	public String getMemoNotes()
	{
		return this.memoNotes;
		
	}
	
	/**
	 * Sets the memo notes.
	 *
	 * @param memoNotes the memoNotes to set
	 */
	public void setMemoNotes(String memoNotes)
	{
		System.out.print(memoNotes + ";");
		this.memoNotes = memoNotes;
		
	}
	
	/**
	 * Gets the budget tracking.
	 *
	 * @return the budgetTracking
	 */
	public String getBudgetTracking()
	{
		return this.budgetTracking;
		
	}
	
	/**
	 * Sets the budget tracking.
	 *
	 * @param budgetTracking the budgetTracking to set
	 */
	public void setBudgetTracking(String budgetTracking)
	{
		System.out.print(budgetTracking + ";");
		this.budgetTracking = budgetTracking;
		
	}
	
	/**
	 * Gets the net total.
	 *
	 * @return the netTotal
	 */
	public BigDecimal getNetTotal()
	{
		return this.netTotal;
		
	}
	
	/**
	 * Sets the net total.
	 *
	 * @param netTotal the netTotal to set
	 */
	public void setNetTotal(BigDecimal netTotal)
	{
		System.out.print(netTotal + ";");
		this.netTotal = netTotal;
		
	}

	/**
	 * Gets the sheet name.
	 *
	 * @return the sheetName
	 */
	public String getSheetName()
	{
		return this.sheetName;
	}

	/**
	 * Sets the sheet name.
	 *
	 * @param sheetName the sheetName to set
	 */
	public void setSheetName(String sheetName)
	{
		System.out.print(sheetName + ";");
		this.sheetName = sheetName;
	}
	
	/**
	 * Gets the allocations.
	 *
	 * @return the allocations
	 */
	public List<Allocation> getAllocations()
	{
		return this.allocations;
		
	}
	
	/**
	 * Sets the allocations.
	 *
	 * @param allocations the allocations to set
	 */
	public void setAllocations(List<Allocation> allocations)
	{
		this.allocations = allocations;
		
	}
	
	/**
	 * Override @see java.lang.Object#toString()
	 *
	 * @return the string
	 */
	@Override public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ExcelLedgerRow [balance=")
				.append(this.balance)
				.append(", date=")
				.append(this.date)
				.append(", checkNumber=")
				.append(this.checkNumber)
				.append(", clearBank=")
				.append(this.clearBank)
				.append(", toFrom=")
				.append(this.toFrom)
				.append(", memoNotes=")
				.append(this.memoNotes)
				.append(", budgetTracking=")
				.append(this.budgetTracking)
				.append(", netTotal=")
				.append(this.netTotal)
				.append(", sheetName=")
				.append(this.sheetName)
				.append(", allocations=")
				.append(this.allocations)
				.append("]");
		return builder.toString();
		
	}
	/**
	 * Represents one amount allocation within a row.
	 */
	@Data @NoArgsConstructor public static class Allocation
	{
		
		/** The amount. */
		private BigDecimal amount;
		
		/** The asset liability account. */
		private String assetLiabilityAccount;
		
		/** The income category. */
		private String incomeCategory;
		
		/** The expense category. */
		private String expenseCategory;
		
		/** The fund. */
		private String fund;
		
		/**
		 * Gets the amount.
		 *
		 * @return the amount
		 */
		public BigDecimal getAmount()
		{
			return this.amount;
			
		}
		
		/**
		 * Sets the amount.
		 *
		 * @param amount the amount to set
		 */
		public void setAmount(BigDecimal amount)
		{
			System.out.print(amount + ";");
			this.amount = amount;
			
		}
		
		/**
		 * Gets the asset liability account.
		 *
		 * @return the assetLiabilityAccount
		 */
		public String getAssetLiabilityAccount()
		{
			return this.assetLiabilityAccount;
			
		}
		
		/**
		 * Sets the asset liability account.
		 *
		 * @param assetLiabilityAccount the assetLiabilityAccount to set
		 */
		public void setAssetLiabilityAccount(String assetLiabilityAccount)
		{
			System.out.print(assetLiabilityAccount + ";");
			this.assetLiabilityAccount = assetLiabilityAccount;
			
		}
		
		/**
		 * Gets the income category.
		 *
		 * @return the incomeCategory
		 */
		public String getIncomeCategory()
		{
			return this.incomeCategory;
			
		}
		
		/**
		 * Sets the income category.
		 *
		 * @param incomeCategory the incomeCategory to set
		 */
		public void setIncomeCategory(String incomeCategory)
		{
			System.out.print(incomeCategory + ";");
			this.incomeCategory = incomeCategory;
			
		}
		
		/**
		 * Gets the expense category.
		 *
		 * @return the expenseCategory
		 */
		public String getExpenseCategory()
		{
			return this.expenseCategory;
			
		}
		
		/**
		 * Sets the expense category.
		 *
		 * @param expenseCategory the expenseCategory to set
		 */
		public void setExpenseCategory(String expenseCategory)
		{
			System.out.print(expenseCategory + ";");
			this.expenseCategory = expenseCategory;
			
		}
		
		/**
		 * Gets the fund.
		 *
		 * @return the fund
		 */
		public String getFund()
		{
			return this.fund;
			
		}
		
		/**
		 * Sets the fund.
		 *
		 * @param fund the fund to set
		 */
		public void setFund(String fund)
		{
			System.out.print(fund + ";");
			this.fund = fund;
			
		}
		

		/**
		 * Override @see java.lang.Object#toString()
		 *
		 * @return the string
		 */
		@Override public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("Allocation [amount=");
			builder.append(this.amount);
			builder.append(", assetLiabilityAccount=");
			builder.append(this.assetLiabilityAccount);
			builder.append(", incomeCategory=");
			builder.append(this.incomeCategory);
			builder.append(", expenseCategory=");
			builder.append(this.expenseCategory);
			builder.append(", fund=");
			builder.append(this.fund);
			builder.append("]");
			return builder.toString();
			
		}
		
		
	}

	
}
