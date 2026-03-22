package nonprofitbookkeeping.importer.sclx.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nonprofitbookkeeping.importer.sclx.jackson.JacksonJsonNodeSupport;
import nonprofitbookkeeping.model.impex.BankingItemRecord;
import nonprofitbookkeeping.model.impex.BankingItemRecord.BankingItemKind;
import nonprofitbookkeeping.model.impex.BankingItemRecord.BankingItemSource;
import nonprofitbookkeeping.model.impex.BankingItemRecord.BankingItemStatus;
import nonprofitbookkeeping.model.impex.BankingItemRecord.OfxTransactionRecord;

/**
 * Maps one SCLX bankingItem node to a final BankingItemRecord.
 */
public final class BankingItemRecordMapper {

    private final ObjectMapper objectMapper;

    public BankingItemRecordMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BankingItemRecord fromSclx(JsonNode node) {
        return new BankingItemRecord(
            JacksonJsonNodeSupport.text(node, "bankingItemId"),
            JacksonJsonNodeSupport.enumValue(node, "kind", BankingItemKind.class),
            JacksonJsonNodeSupport.text(node, "bankAccountId"),
            JacksonJsonNodeSupport.text(node, "transactionId"),
            JacksonJsonNodeSupport.stringList(node, "lineIds"),
            JacksonJsonNodeSupport.localDate(node, "clearedDate"),
            JacksonJsonNodeSupport.decimal(node, "amount"),
            JacksonJsonNodeSupport.text(node, "checkNumber"),
            JacksonJsonNodeSupport.text(node, "payee"),
            JacksonJsonNodeSupport.localDate(node, "depositDate"),
            JacksonJsonNodeSupport.text(node, "payer"),
            JacksonJsonNodeSupport.text(node, "depositId"),
            JacksonJsonNodeSupport.text(node, "memo"),
            JacksonJsonNodeSupport.enumValue(node, "source", BankingItemSource.class),
            JacksonJsonNodeSupport.enumValue(node, "status", BankingItemStatus.class),
            JacksonJsonNodeSupport.text(node, "importId"),
            ofx(node == null ? null : node.get("ofx")),
            JacksonJsonNodeSupport.objectMap(node, "extensions", objectMapper)
        );
    }

    private OfxTransactionRecord ofx(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        return new OfxTransactionRecord(
            JacksonJsonNodeSupport.text(node, "fitId"),
            JacksonJsonNodeSupport.text(node, "transactionType"),
            JacksonJsonNodeSupport.localDate(node, "datePosted"),
            JacksonJsonNodeSupport.localDate(node, "dateUser"),
            JacksonJsonNodeSupport.localDate(node, "dateAvailable"),
            JacksonJsonNodeSupport.text(node, "checkNumber"),
            JacksonJsonNodeSupport.text(node, "referenceNumber"),
            JacksonJsonNodeSupport.text(node, "name"),
            JacksonJsonNodeSupport.text(node, "memo"),
            JacksonJsonNodeSupport.text(node, "payeeId"),
            JacksonJsonNodeSupport.text(node, "sic"),
            JacksonJsonNodeSupport.text(node, "serverTransactionId"),
            JacksonJsonNodeSupport.text(node, "correctFitId"),
            JacksonJsonNodeSupport.text(node, "correctAction"),
            JacksonJsonNodeSupport.objectMap(node, "extensions", objectMapper)
        );
    }
}
