package nonprofitbookkeeping.model.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Lightweight record associating a report row bean with the originating
 * accounting transaction.
 */
public class ReportRowRecord {

    /** Identifier of the originating {@code AccountingTransaction}. */
    @JsonProperty
    private String transactionId;

    /**
     * Serialized form of the row bean. Stored as a generic JSON tree so that
     * arbitrary row bean types can be persisted without a compile-time
     * dependency on their classes.
     */
    @JsonProperty
    private JsonNode row;

    /** Default constructor for deserialization. */
    public ReportRowRecord() {
    }

    /**
     * Creates a new record pairing a transaction id with a serialized row.
     *
     * @param transactionId id of the originating transaction
     * @param row           JSON representation of the row bean
     */
    public ReportRowRecord(String transactionId, JsonNode row) {
        this.transactionId = transactionId;
        this.row = row;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public JsonNode getRow() {
        return row;
    }

    public void setRow(JsonNode row) {
        this.row = row;
    }
}

