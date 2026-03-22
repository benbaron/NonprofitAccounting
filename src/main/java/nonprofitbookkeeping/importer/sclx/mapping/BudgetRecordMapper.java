package nonprofitbookkeeping.importer.sclx.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nonprofitbookkeeping.importer.sclx.jackson.JacksonJsonNodeSupport;
import nonprofitbookkeeping.model.impex.BudgetRecord;
import nonprofitbookkeeping.model.impex.BudgetRecord.BudgetExpenseCategory;
import nonprofitbookkeeping.model.impex.BudgetRecord.BudgetLineRecord;
import nonprofitbookkeeping.model.impex.BudgetRecord.BudgetRevenueCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps one SCLX budget node to a final BudgetRecord.
 */
public final class BudgetRecordMapper {

    private final ObjectMapper objectMapper;

    public BudgetRecordMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BudgetRecord fromSclx(JsonNode node) {
        Integer fiscalYear = JacksonJsonNodeSupport.integer(node, "fiscalYear");
        return new BudgetRecord(
            JacksonJsonNodeSupport.text(node, "budgetId"),
            JacksonJsonNodeSupport.text(node, "name"),
            fiscalYear == null ? 0 : fiscalYear,
            JacksonJsonNodeSupport.text(node, "fundId"),
            JacksonJsonNodeSupport.bool(node, "active", false),
            JacksonJsonNodeSupport.text(node, "description"),
            budgetLines(node == null ? null : node.get("lines")),
            JacksonJsonNodeSupport.objectMap(node, "extensions", objectMapper)
        );
    }

    private List<BudgetLineRecord> budgetLines(JsonNode linesNode) {
        if (linesNode == null || linesNode.isNull() || !linesNode.isArray()) {
            return List.of();
        }

        List<BudgetLineRecord> result = new ArrayList<>();
        for (JsonNode node : linesNode) {
            result.add(new BudgetLineRecord(
                JacksonJsonNodeSupport.text(node, "eventName"),
                JacksonJsonNodeSupport.decimal(node, "budgetedAmount"),
                JacksonJsonNodeSupport.enumValue(node, "revenueCategory", BudgetRevenueCategory.class),
                JacksonJsonNodeSupport.enumValue(node, "expenseCategory", BudgetExpenseCategory.class),
                JacksonJsonNodeSupport.text(node, "accountId"),
                JacksonJsonNodeSupport.text(node, "notes"),
                JacksonJsonNodeSupport.objectMap(node, "extensions", objectMapper)
            ));
        }
        return List.copyOf(result);
    }
}
