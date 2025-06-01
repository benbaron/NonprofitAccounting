
package nonprofitbookkeeping.model.budget;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

public class BudgetLine
{

	private String accountId; // Assumes Account.getId() is the unique identifier
	private String accountName; // For display/reference
	private BigDecimal totalBudgetedAmount;
	private Periodicity periodicity = Periodicity.ANNUAL; // Default to ANNUAL
	private List<BigDecimal> periodicAmounts = new ArrayList<>(); // Initialize to avoid null
	private String fundId; // Optional
	
	public BudgetLine()
	{
		
	}
	
	/**  
	 * Constructor BudgetLine
	 * @param accountId
	 * @param accountName
	 * @param totalBudgetedAmount
	 * @param periodicity
	 * @param periodicAmounts
	 * @param fundId
	 */
	public BudgetLine(String accountId, String accountName, BigDecimal totalBudgetedAmount,
		Periodicity periodicity, List<BigDecimal> periodicAmounts, String fundId)
	{
		this.accountId = accountId;
		this.accountName = accountName;
		this.totalBudgetedAmount = totalBudgetedAmount;
		this.periodicity = periodicity;
		this.periodicAmounts = periodicAmounts;
		this.fundId = fundId;
	}

	// Custom setter for periodicity to potentially validate periodicAmounts size.
	// For now, just a standard setter. Validation can be added later.
	public void setPeriodicity(Periodicity periodicity)
	{
		this.periodicity = periodicity;
		// Future enhancement: if periodicAmounts is not null, validate its size against
		// the new periodicity
		// or clear/reset it. For now, manual management is assumed.
	}
	
	// Custom setter for periodicAmounts to potentially validate against
	// periodicity.
	public void setPeriodicAmounts(List<BigDecimal> periodicAmounts)
	{
		// Future enhancement: validate size against this.periodicity
		// Example validation (can be more sophisticated):
		// if (periodicAmounts != null && this.periodicity != null) {
		// if (this.periodicity == Periodicity.MONTHLY && periodicAmounts.size() != 12
		// && !periodicAmounts.isEmpty()) {
		// throw new IllegalArgumentException("Monthly periodicity requires 12 periodic
		// amounts or an empty list if total is used.");
		// }
		// if (this.periodicity == Periodicity.QUARTERLY && periodicAmounts.size() != 4
		// && !periodicAmounts.isEmpty()) {
		// throw new IllegalArgumentException("Quarterly periodicity requires 4 periodic
		// amounts or an empty list if total is used.");
		// }
		// if (this.periodicity == Periodicity.ANNUAL && !periodicAmounts.isEmpty()) {
		// // For ANNUAL, periodicAmounts should typically be empty if
		// totalBudgetedAmount is the source of truth.
		// // Or, it could contain a single value equal to totalBudgetedAmount. This
		// depends on design.
		// }
		// }
		this.periodicAmounts = periodicAmounts;
	}


	/**
	 * @return the accountId
	 */
	public String getAccountId()
	{
		return this.accountId;
	}

	/**
	 * @param accountId the accountId to set
	 */
	public void setAccountId(String accountId)
	{
		this.accountId = accountId;
	}

	/**
	 * @return the accountName
	 */
	public String getAccountName()
	{
		return this.accountName;
	}

	/**
	 * @param accountName the accountName to set
	 */
	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
	}

	/**
	 * @return the totalBudgetedAmount
	 */
	public BigDecimal getTotalBudgetedAmount()
	{
		return this.totalBudgetedAmount;
	}

	/**
	 * @param totalBudgetedAmount the totalBudgetedAmount to set
	 */
	public void setTotalBudgetedAmount(BigDecimal totalBudgetedAmount)
	{
		this.totalBudgetedAmount = totalBudgetedAmount;
	}

	/**
	 * @return the fundId
	 */
	public String getFundId()
	{
		return this.fundId;
	}

	/**
	 * @param fundId the fundId to set
	 */
	public void setFundId(String fundId)
	{
		this.fundId = fundId;
	}

	/**
	 * @return the periodicAmounts
	 */
	public List<BigDecimal> getPeriodicAmounts()
	{
		return this.periodicAmounts;
	}

	/**
	 * @return the periodicity
	 */
	public Periodicity getPeriodicity()
	{
		return this.periodicity;
	}
	
}
