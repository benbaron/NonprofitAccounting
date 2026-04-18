package nonprofitbookkeeping.model.records;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record BudgetRecord(
    String budgetId,
    String name,
    Integer fiscalYear,
    String fundId,
    Boolean active,
    String description,
    List<BudgetLineRecord> lines,
    Map<String, Object> extensions,
    String rawJson)
{
    public BudgetRecord
    {
        lines = lines == null ? List.of() : List.copyOf(lines);
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }

    public record BudgetLineRecord(
        String eventName,
        BigDecimal budgetedAmount,
        String revenueCategory,
        String expenseCategory,
        String accountId,
        String notes,
        Map<String, Object> extensions)
    {
        public BudgetLineRecord
        {
            extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
        }
    }
}
