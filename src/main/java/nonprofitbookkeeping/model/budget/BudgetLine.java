package nonprofitbookkeeping.model.budget;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetLine {
    private String accountId; // Assumes Account.getId() is the unique identifier
    private String accountName; // For display/reference
    private BigDecimal totalBudgetedAmount;
    private Periodicity periodicity = Periodicity.ANNUAL; // Default to ANNUAL
    private List<BigDecimal> periodicAmounts = new ArrayList<>(); // Initialize to avoid null
    private String fundId; // Optional

    /**
     * Constructor with essential fields, defaulting periodicity.
     * @param accountId The unique ID of the account.
     * @param accountName The name of the account.
     * @param totalBudgetedAmount The total budgeted amount for the account.
     */
    public BudgetLine(String accountId, String accountName, BigDecimal totalBudgetedAmount) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.totalBudgetedAmount = totalBudgetedAmount;
        // periodicity defaults to ANNUAL
        // periodicAmounts defaults to empty list
    }

    /**
     * Constructor with essential fields and specific periodicity.
     * @param accountId The unique ID of the account.
     * @param accountName The name of the account.
     * @param totalBudgetedAmount The total budgeted amount for the account.
     * @param periodicity The periodicity of the budget line.
     */
    public BudgetLine(String accountId, String accountName, BigDecimal totalBudgetedAmount, Periodicity periodicity) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.totalBudgetedAmount = totalBudgetedAmount;
        this.periodicity = periodicity;
        // periodicAmounts defaults to empty list, to be populated based on periodicity if needed
    }

    // Custom setter for periodicity to potentially validate periodicAmounts size.
    // For now, just a standard setter. Validation can be added later.
    public void setPeriodicity(Periodicity periodicity) {
        this.periodicity = periodicity;
        // Future enhancement: if periodicAmounts is not null, validate its size against the new periodicity
        // or clear/reset it. For now, manual management is assumed.
    }

    // Custom setter for periodicAmounts to potentially validate against periodicity.
    public void setPeriodicAmounts(List<BigDecimal> periodicAmounts) {
        // Future enhancement: validate size against this.periodicity
        // Example validation (can be more sophisticated):
        // if (periodicAmounts != null && this.periodicity != null) {
        //     if (this.periodicity == Periodicity.MONTHLY && periodicAmounts.size() != 12 && !periodicAmounts.isEmpty()) {
        //         throw new IllegalArgumentException("Monthly periodicity requires 12 periodic amounts or an empty list if total is used.");
        //     }
        //     if (this.periodicity == Periodicity.QUARTERLY && periodicAmounts.size() != 4 && !periodicAmounts.isEmpty()) {
        //         throw new IllegalArgumentException("Quarterly periodicity requires 4 periodic amounts or an empty list if total is used.");
        //     }
        //     if (this.periodicity == Periodicity.ANNUAL && !periodicAmounts.isEmpty()) {
        //        // For ANNUAL, periodicAmounts should typically be empty if totalBudgetedAmount is the source of truth.
        //        // Or, it could contain a single value equal to totalBudgetedAmount. This depends on design.
        //     }
        // }
        this.periodicAmounts = periodicAmounts;
    }
}
