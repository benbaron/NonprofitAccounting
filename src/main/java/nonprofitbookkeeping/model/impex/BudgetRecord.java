package nonprofitbookkeeping.model.impex;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Final normalized budget record derived from SCLX.
 */
public record BudgetRecord(
    String budgetId,
    String name,
    int fiscalYear,
    String fundId,
    boolean active,
    String description,
    List<BudgetLineRecord> lines,
    Map<String, Object> extensions
) {

    public BudgetRecord {
        if (isBlank(budgetId)) {
            throw new IllegalArgumentException("budgetId is required.");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("name is required.");
        }
        if (isBlank(fundId)) {
            throw new IllegalArgumentException("fundId is required.");
        }
        lines = lines == null ? List.of() : List.copyOf(lines);
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }

    public record BudgetLineRecord(
        String eventName,
        BigDecimal budgetedAmount,
        BudgetRevenueCategory revenueCategory,
        BudgetExpenseCategory expenseCategory,
        String accountId,
        String notes,
        Map<String, Object> extensions
    ) {
        public BudgetLineRecord {
            if (isBlank(eventName)) {
                throw new IllegalArgumentException("eventName is required.");
            }
            if (budgetedAmount == null) {
                throw new IllegalArgumentException("budgetedAmount is required.");
            }
            if (revenueCategory == null) {
                throw new IllegalArgumentException("revenueCategory is required.");
            }
            if (expenseCategory == null) {
                throw new IllegalArgumentException("expenseCategory is required.");
            }
            extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
        }
    }

    public enum BudgetRevenueCategory {
        RestrictedRevenue,
        UnrestrictedRevenue,
        GeneralRevenue
    }

    public enum BudgetExpenseCategory {
        Officers,
        Supplies,
        AdminMaintenance,
        EquipmentRegalia,
        Marketing,
        Overhead
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
