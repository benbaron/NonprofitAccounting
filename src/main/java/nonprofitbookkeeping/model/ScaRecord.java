package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Represents a generic row in the Statement of Cash Activities (SCA) ledger.
 * Each record captures the underlying {@link AccountingTransaction} identifier
 * and an optional record type to allow reports to relate back to the master
 * transaction.
 */
@Entity
@Table(name = "sca_records")
public class ScaRecord {

    /** Unique identifier for this SCA record. */
    @Id
    @JsonProperty
    private String id;

    /** Date of the record in ISO format (YYYY-MM-DD). */
    @JsonProperty
    private String date;

    /** Description or memo for the record. */
    @JsonProperty
    private String description;

    /** Amount associated with the record. */
    @JsonProperty
    private BigDecimal amount;

    /** Identifier of the master {@link AccountingTransaction}. */
    @JsonProperty
    private int transactionId;

    /** Optional type describing the record for quick lookups. */
    @JsonProperty
    private String recordType;

    /** Default constructor for frameworks. */
    public ScaRecord() {
    }

    /**
     * Constructs a new {@code ScaRecord} with the supplied values.
     *
     * @param id            unique identifier for the record
     * @param date          date in ISO format
     * @param description   record description
     * @param amount        monetary amount
     * @param transactionId id of the related {@link AccountingTransaction}
     * @param recordType    optional type string
     */
    public ScaRecord(String id, String date, String description, BigDecimal amount,
                     int transactionId, String recordType) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.transactionId = transactionId;
        this.recordType = recordType;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getRecordType() {
        return this.recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }
}

