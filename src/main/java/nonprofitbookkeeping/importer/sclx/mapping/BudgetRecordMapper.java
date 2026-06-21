package nonprofitbookkeeping.importer.sclx.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nonprofitbookkeeping.importer.sclx.jackson.JacksonJsonNodeSupport;
import nonprofitbookkeeping.model.records.BudgetRecord;
import nonprofitbookkeeping.model.records.BudgetRecord.BudgetLineRecord;

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
        return new BudgetRecord(
            JacksonJsonNodeSupport.text(node, "budgetId"),
            JacksonJsonNodeSupport.text(node, "name"),
            JacksonJsonNodeSupport.integer(node, "fiscalYear"),
            JacksonJsonNodeSupport.text(node, "fundId"),
            node == null || node.get("active") == null || node.get("active").isNull() ? null : node.get("active").asBoolean(),
            JacksonJsonNodeSupport.text(node, "description"),
            budgetLines(node == null ? null : node.get("lines")),
            JacksonJsonNodeSupport.objectMap(node, "extensions", this.objectMapper),
            node == null ? null : node.toString()
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
                JacksonJsonNodeSupport.text(node, "revenueCategory"),
                JacksonJsonNodeSupport.text(node, "expenseCategory"),
                JacksonJsonNodeSupport.text(node, "accountId"),
                JacksonJsonNodeSupport.text(node, "notes"),
                JacksonJsonNodeSupport.objectMap(node, "extensions", this.objectMapper)
            ));
        }
        return List.copyOf(result);
    }
}
