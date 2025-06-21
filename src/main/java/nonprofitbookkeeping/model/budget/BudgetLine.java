
package nonprofitbookkeeping.model.budget;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import nonprofitbookkeeping.model.budget.BudgetLinePeriodAmount;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

/**
 * Represents a single line item within a budget.
 * Each budget line is associated with a specific account and includes details such as
 * the total budgeted amount, the periodicity of the budget (e.g., annual, monthly),
 * and optionally, amounts broken down by period and an associated fund ID.
 */
@Entity
@Table(name = "budget_line")
public class BudgetLine
{
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "line_id")
        private Long lineId;

        /** The unique identifier of the account associated with this budget line. */
        @Column(name = "account_id")
        private String accountId; // Assumes Account.getId() is the unique identifier
        /** The name of the account, for display or reference purposes. */
        @Column(name = "account_name")
        private String accountName; // For display/reference
        /** The total budgeted amount for this account line for the entire budget period. */
        @Column(name = "total_amount")
        private BigDecimal totalBudgetedAmount;
        /** The periodicity of how the budget is broken down (e.g., ANNUAL, MONTHLY, QUARTERLY). Defaults to ANNUAL. */
        @Enumerated(EnumType.STRING)
        private Periodicity periodicity = Periodicity.ANNUAL; // Default to ANNUAL
	/**
	 * A list of amounts budgeted for each sub-period (e.g., monthly amounts if periodicity is MONTHLY).
	 * Initialized to an empty ArrayList.
	 */
        @OneToMany(mappedBy = "budgetLine", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<BudgetLinePeriodAmount> periodicAmounts = new ArrayList<>();
        /** The identifier of a specific fund to which this budget line applies, if any. Optional. */
        @Column(name = "fund_id")
        private String fundId; // Optional
	
	/**
	 * Default constructor.
	 * Initializes fields to default values (null for objects, default for primitives like Periodicity).
	 * {@code periodicAmounts} is initialized to an empty list.
	 * {@code periodicity} defaults to {@link Periodicity#ANNUAL}.
	 */
	public BudgetLine()
	{
		
	}
	
	/**  
	 * Constructs a BudgetLine with specified details.
	 * @param accountId The ID of the associated account.
	 * @param accountName The name of the associated account.
	 * @param totalBudgetedAmount The total amount budgeted for this line.
	 * @param periodicity The periodicity of the budget amounts (e.g., ANNUAL, MONTHLY).
	 * @param periodicAmounts A list of amounts for each sub-period, if applicable.
	 * @param fundId The ID of the fund this budget line is associated with (optional).
	 */
        public BudgetLine(String accountId, String accountName, BigDecimal totalBudgetedAmount,
                Periodicity periodicity, List<BudgetLinePeriodAmount> periodicAmounts, String fundId)
	{
		this.accountId = accountId;
		this.accountName = accountName;
		this.totalBudgetedAmount = totalBudgetedAmount;
		this.periodicity = periodicity;
		this.periodicAmounts = periodicAmounts;
		this.fundId = fundId;
	}

	/**
	 * Sets the periodicity for this budget line.
	 * Future enhancements could include validating the size of {@code periodicAmounts}
	 * against the new periodicity or resetting/clearing {@code periodicAmounts}.
	 * For now, manual management of consistency between periodicity and periodicAmounts is assumed.
	 * @param periodicity The {@link Periodicity} to set.
	 */
	public void setPeriodicity(Periodicity periodicity)
	{
		this.periodicity = periodicity;
		// Future enhancement: if periodicAmounts is not null, validate its size against
		// the new periodicity
		// or clear/reset it. For now, manual management is assumed.
	}
	
	/**
	 * Sets the list of amounts budgeted for each sub-period.
	 * Future enhancements could include validating the size of this list against the current {@code periodicity}.
	 * For example, if periodicity is MONTHLY, this list might be expected to have 12 entries.
         * @param periodicAmounts A list of {@link BudgetLinePeriodAmount} representing amounts for each sub-period.
	 */
        public void setPeriodicAmounts(List<BudgetLinePeriodAmount> periodicAmounts)
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
	 * Gets the ID of the account associated with this budget line.
	 * @return The account ID.
	 */
	public String getAccountId()
	{
		return this.accountId;
	}

	/**
	 * Sets the ID of the account associated with this budget line.
	 * @param accountId The account ID to set.
	 */
	public void setAccountId(String accountId)
	{
		this.accountId = accountId;
	}

	/**
	 * Gets the name of the account associated with this budget line.
	 * @return The account name.
	 */
	public String getAccountName()
	{
		return this.accountName;
	}

	/**
	 * Sets the name of the account associated with this budget line.
	 * @param accountName The account name to set.
	 */
	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
	}

	/**
	 * Gets the total budgeted amount for this budget line.
	 * @return The total budgeted amount.
	 */
	public BigDecimal getTotalBudgetedAmount()
	{
		return this.totalBudgetedAmount;
	}

	/**
	 * Sets the total budgeted amount for this budget line.
	 * @param totalBudgetedAmount The total budgeted amount to set.
	 */
	public void setTotalBudgetedAmount(BigDecimal totalBudgetedAmount)
	{
		this.totalBudgetedAmount = totalBudgetedAmount;
	}

	/**
	 * Gets the ID of the fund associated with this budget line, if any.
	 * @return The fund ID, or null if no specific fund is associated.
	 */
	public String getFundId()
	{
		return this.fundId;
	}

	/**
	 * Sets the ID of the fund associated with this budget line.
	 * @param fundId The fund ID to set. Can be null.
	 */
	public void setFundId(String fundId)
	{
		this.fundId = fundId;
	}

	/**
	 * Gets the list of amounts budgeted for each sub-period.
	 * The interpretation of this list depends on the {@link #getPeriodicity()}.
	 * @return A list of periodic amounts.
	 */
        public List<BudgetLinePeriodAmount> getPeriodicAmounts()
	{
		return this.periodicAmounts;
	}

	/**
	 * Gets the periodicity for this budget line.
	 * This determines how the {@code periodicAmounts} are interpreted (e.g., monthly, quarterly).
	 * @return The {@link Periodicity} enum value.
	 */
	public Periodicity getPeriodicity()
	{
		return this.periodicity;
	}
	
}
