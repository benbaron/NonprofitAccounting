package nonprofitbookkeeping.importer.sclx.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nonprofitbookkeeping.importer.sclx.jackson.JacksonJsonNodeSupport;
import nonprofitbookkeeping.model.records.BankStatementRecord;
import nonprofitbookkeeping.model.records.BankStatementRecord.BalanceSnapshot;
import nonprofitbookkeeping.model.records.BankStatementRecord.BankAccountRef;
import nonprofitbookkeeping.model.records.BankStatementRecord.SourceFormat;
import nonprofitbookkeeping.model.records.BankStatementRecord.StatementKind;

/**
 * Maps one SCLX bankStatementImport node to a final BankStatementRecord.
 */
public final class BankStatementRecordMapper {

    private final ObjectMapper objectMapper;

    public BankStatementRecordMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BankStatementRecord fromSclx(JsonNode node) {
        JsonNode bankAccountNode = node == null ? null : node.get("bankAccount");
        JsonNode ledgerBalanceNode = node == null ? null : node.get("ledgerBalance");
        JsonNode availableBalanceNode = node == null ? null : node.get("availableBalance");

        BankAccountRef bankAccount = bankAccountNode == null || bankAccountNode.isNull()
            ? null
            : new BankAccountRef(
                JacksonJsonNodeSupport.text(bankAccountNode, "bankId"),
                JacksonJsonNodeSupport.text(bankAccountNode, "accountId"),
                JacksonJsonNodeSupport.text(bankAccountNode, "accountType")
            );

        BalanceSnapshot ledgerBalance = balanceSnapshot(ledgerBalanceNode);
        BalanceSnapshot availableBalance = balanceSnapshot(availableBalanceNode);

        return new BankStatementRecord(
            JacksonJsonNodeSupport.text(node, "importId"),
            JacksonJsonNodeSupport.enumValue(node, "sourceFormat", SourceFormat.class),
            JacksonJsonNodeSupport.text(node, "sourceVersion"),
            JacksonJsonNodeSupport.enumValue(node, "statementKind", StatementKind.class),
            bankAccount,
            JacksonJsonNodeSupport.text(node, "currency"),
            JacksonJsonNodeSupport.localDate(node, "statementStart"),
            JacksonJsonNodeSupport.localDate(node, "statementEnd"),
            ledgerBalance,
            availableBalance,
            JacksonJsonNodeSupport.text(node, "documentId"),
            JacksonJsonNodeSupport.objectMap(node, "extensions", objectMapper),
            node == null ? null : node.toString()
        );
    }

    private BalanceSnapshot balanceSnapshot(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return new BalanceSnapshot(
            JacksonJsonNodeSupport.decimal(node, "amount"),
            JacksonJsonNodeSupport.offsetDateTime(node, "asOf")
        );
    }
}
