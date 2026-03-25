
package nonprofitbookkeeping.model.sclx.generated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/**
 * A transaction record may be a canonical balanced accounting transaction or a worksheet-native ledger entry exported directly from a source workbook. One or more lines are permitted.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "transactionId",
    "transactionDate",
    "postingDate",
    "description",
    "reference",
    "status",
    "source",
    "documentIds",
    "eventId",
    "approval",
    "bankTiming",
    "budgetTiming",
    "budgetId",
    "lines",
    "workbookLink",
    "extensions"
})
@Generated("jsonschema2pojo")
public class Transaction {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactionId")
    @Size(min = 1)
    @NotNull
    private String transactionId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactionDate")
    @NotNull
    private String transactionDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("postingDate")
    @NotNull
    private String postingDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("description")
    @Size(min = 1)
    @NotNull
    private String description;
    @JsonProperty("reference")
    private String reference;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("status")
    @NotNull
    private Transaction.TransactionStatus status;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("source")
    @NotNull
    private Transaction.TransactionSource source;
    @JsonProperty("documentIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<@Valid String> documentIds = new LinkedHashSet<String>();
    @JsonProperty("eventId")
    private String eventId;
    @JsonProperty("approval")
    @Valid
    private Approval approval;
    @JsonProperty("bankTiming")
    private Transaction.TimingValue bankTiming;
    @JsonProperty("budgetTiming")
    private Transaction.TimingValue budgetTiming;
    @JsonProperty("budgetId")
    private String budgetId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lines")
    @Size(min = 1)
    @NotNull
    private List<@Valid TransactionLine> lines = new ArrayList<TransactionLine>();
    @JsonProperty("workbookLink")
    @Valid
    private WorkbookLink workbookLink;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Transaction() {
    }

    public Transaction(String transactionId, String transactionDate, String postingDate, String description, String reference, Transaction.TransactionStatus status, Transaction.TransactionSource source, Set<@Valid String> documentIds, String eventId, Approval approval, Transaction.TimingValue bankTiming, Transaction.TimingValue budgetTiming, String budgetId, List<@Valid TransactionLine> lines, WorkbookLink workbookLink, Extensions extensions) {
        super();
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.postingDate = postingDate;
        this.description = description;
        this.reference = reference;
        this.status = status;
        this.source = source;
        this.documentIds = documentIds;
        this.eventId = eventId;
        this.approval = approval;
        this.bankTiming = bankTiming;
        this.budgetTiming = budgetTiming;
        this.budgetId = budgetId;
        this.lines = lines;
        this.workbookLink = workbookLink;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactionId")
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactionDate")
    public String getTransactionDate() {
        return transactionDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactionDate")
    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("postingDate")
    public String getPostingDate() {
        return postingDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("postingDate")
    public void setPostingDate(String postingDate) {
        this.postingDate = postingDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @JsonProperty("reference")
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("status")
    public Transaction.TransactionStatus getStatus() {
        return status;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("status")
    public void setStatus(Transaction.TransactionStatus status) {
        this.status = status;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("source")
    public Transaction.TransactionSource getSource() {
        return source;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("source")
    public void setSource(Transaction.TransactionSource source) {
        this.source = source;
    }

    @JsonProperty("documentIds")
    public Set<String> getDocumentIds() {
        return documentIds;
    }

    @JsonProperty("documentIds")
    public void setDocumentIds(Set<String> documentIds) {
        this.documentIds = documentIds;
    }

    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @JsonProperty("approval")
    public Approval getApproval() {
        return approval;
    }

    @JsonProperty("approval")
    public void setApproval(Approval approval) {
        this.approval = approval;
    }

    @JsonProperty("bankTiming")
    public Transaction.TimingValue getBankTiming() {
        return bankTiming;
    }

    @JsonProperty("bankTiming")
    public void setBankTiming(Transaction.TimingValue bankTiming) {
        this.bankTiming = bankTiming;
    }

    @JsonProperty("budgetTiming")
    public Transaction.TimingValue getBudgetTiming() {
        return budgetTiming;
    }

    @JsonProperty("budgetTiming")
    public void setBudgetTiming(Transaction.TimingValue budgetTiming) {
        this.budgetTiming = budgetTiming;
    }

    @JsonProperty("budgetId")
    public String getBudgetId() {
        return budgetId;
    }

    @JsonProperty("budgetId")
    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lines")
    public List<TransactionLine> getLines() {
        return lines;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lines")
    public void setLines(List<TransactionLine> lines) {
        this.lines = lines;
    }

    @JsonProperty("workbookLink")
    public WorkbookLink getWorkbookLink() {
        return workbookLink;
    }

    @JsonProperty("workbookLink")
    public void setWorkbookLink(WorkbookLink workbookLink) {
        this.workbookLink = workbookLink;
    }

    @JsonProperty("extensions")
    public Extensions getExtensions() {
        return extensions;
    }

    @JsonProperty("extensions")
    public void setExtensions(Extensions extensions) {
        this.extensions = extensions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Transaction.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("transactionId");
        sb.append('=');
        sb.append(((this.transactionId == null)?"<null>":this.transactionId));
        sb.append(',');
        sb.append("transactionDate");
        sb.append('=');
        sb.append(((this.transactionDate == null)?"<null>":this.transactionDate));
        sb.append(',');
        sb.append("postingDate");
        sb.append('=');
        sb.append(((this.postingDate == null)?"<null>":this.postingDate));
        sb.append(',');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
        sb.append(',');
        sb.append("reference");
        sb.append('=');
        sb.append(((this.reference == null)?"<null>":this.reference));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
        sb.append(',');
        sb.append("source");
        sb.append('=');
        sb.append(((this.source == null)?"<null>":this.source));
        sb.append(',');
        sb.append("documentIds");
        sb.append('=');
        sb.append(((this.documentIds == null)?"<null>":this.documentIds));
        sb.append(',');
        sb.append("eventId");
        sb.append('=');
        sb.append(((this.eventId == null)?"<null>":this.eventId));
        sb.append(',');
        sb.append("approval");
        sb.append('=');
        sb.append(((this.approval == null)?"<null>":this.approval));
        sb.append(',');
        sb.append("bankTiming");
        sb.append('=');
        sb.append(((this.bankTiming == null)?"<null>":this.bankTiming));
        sb.append(',');
        sb.append("budgetTiming");
        sb.append('=');
        sb.append(((this.budgetTiming == null)?"<null>":this.budgetTiming));
        sb.append(',');
        sb.append("budgetId");
        sb.append('=');
        sb.append(((this.budgetId == null)?"<null>":this.budgetId));
        sb.append(',');
        sb.append("lines");
        sb.append('=');
        sb.append(((this.lines == null)?"<null>":this.lines));
        sb.append(',');
        sb.append("workbookLink");
        sb.append('=');
        sb.append(((this.workbookLink == null)?"<null>":this.workbookLink));
        sb.append(',');
        sb.append("extensions");
        sb.append('=');
        sb.append(((this.extensions == null)?"<null>":this.extensions));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.eventId == null)? 0 :this.eventId.hashCode()));
        result = ((result* 31)+((this.approval == null)? 0 :this.approval.hashCode()));
        result = ((result* 31)+((this.description == null)? 0 :this.description.hashCode()));
        result = ((result* 31)+((this.postingDate == null)? 0 :this.postingDate.hashCode()));
        result = ((result* 31)+((this.source == null)? 0 :this.source.hashCode()));
        result = ((result* 31)+((this.budgetId == null)? 0 :this.budgetId.hashCode()));
        result = ((result* 31)+((this.transactionDate == null)? 0 :this.transactionDate.hashCode()));
        result = ((result* 31)+((this.transactionId == null)? 0 :this.transactionId.hashCode()));
        result = ((result* 31)+((this.documentIds == null)? 0 :this.documentIds.hashCode()));
        result = ((result* 31)+((this.reference == null)? 0 :this.reference.hashCode()));
        result = ((result* 31)+((this.workbookLink == null)? 0 :this.workbookLink.hashCode()));
        result = ((result* 31)+((this.extensions == null)? 0 :this.extensions.hashCode()));
        result = ((result* 31)+((this.budgetTiming == null)? 0 :this.budgetTiming.hashCode()));
        result = ((result* 31)+((this.lines == null)? 0 :this.lines.hashCode()));
        result = ((result* 31)+((this.status == null)? 0 :this.status.hashCode()));
        result = ((result* 31)+((this.bankTiming == null)? 0 :this.bankTiming.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Transaction) == false) {
            return false;
        }
        Transaction rhs = ((Transaction) other);
        return (((((((((((((((((this.eventId == rhs.eventId)||((this.eventId!= null)&&this.eventId.equals(rhs.eventId)))&&((this.approval == rhs.approval)||((this.approval!= null)&&this.approval.equals(rhs.approval))))&&((this.description == rhs.description)||((this.description!= null)&&this.description.equals(rhs.description))))&&((this.postingDate == rhs.postingDate)||((this.postingDate!= null)&&this.postingDate.equals(rhs.postingDate))))&&((this.source == rhs.source)||((this.source!= null)&&this.source.equals(rhs.source))))&&((this.budgetId == rhs.budgetId)||((this.budgetId!= null)&&this.budgetId.equals(rhs.budgetId))))&&((this.transactionDate == rhs.transactionDate)||((this.transactionDate!= null)&&this.transactionDate.equals(rhs.transactionDate))))&&((this.transactionId == rhs.transactionId)||((this.transactionId!= null)&&this.transactionId.equals(rhs.transactionId))))&&((this.documentIds == rhs.documentIds)||((this.documentIds!= null)&&this.documentIds.equals(rhs.documentIds))))&&((this.reference == rhs.reference)||((this.reference!= null)&&this.reference.equals(rhs.reference))))&&((this.workbookLink == rhs.workbookLink)||((this.workbookLink!= null)&&this.workbookLink.equals(rhs.workbookLink))))&&((this.extensions == rhs.extensions)||((this.extensions!= null)&&this.extensions.equals(rhs.extensions))))&&((this.budgetTiming == rhs.budgetTiming)||((this.budgetTiming!= null)&&this.budgetTiming.equals(rhs.budgetTiming))))&&((this.lines == rhs.lines)||((this.lines!= null)&&this.lines.equals(rhs.lines))))&&((this.status == rhs.status)||((this.status!= null)&&this.status.equals(rhs.status))))&&((this.bankTiming == rhs.bankTiming)||((this.bankTiming!= null)&&this.bankTiming.equals(rhs.bankTiming))));
    }

    @Generated("jsonschema2pojo")
    public enum TimingValue {

        NOW("NOW"),
        PREVIOUSLY("PREVIOUSLY"),
        LATER("LATER"),
        NONE("NONE");
        private final String value;
        private final static Map<String, Transaction.TimingValue> CONSTANTS = new HashMap<String, Transaction.TimingValue>();

        static {
            for (Transaction.TimingValue c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TimingValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Transaction.TimingValue fromValue(String value) {
            Transaction.TimingValue constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("jsonschema2pojo")
    public enum TransactionSource {

        MANUAL("MANUAL"),
        BANK_IMPORT("BANK_IMPORT"),
        OFX_IMPORT("OFX_IMPORT"),
        CSV_IMPORT("CSV_IMPORT"),
        OPENING_BALANCE("OPENING_BALANCE"),
        SYSTEM_GENERATED("SYSTEM_GENERATED"),
        ADJUSTMENT("ADJUSTMENT");
        private final String value;
        private final static Map<String, Transaction.TransactionSource> CONSTANTS = new HashMap<String, Transaction.TransactionSource>();

        static {
            for (Transaction.TransactionSource c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TransactionSource(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Transaction.TransactionSource fromValue(String value) {
            Transaction.TransactionSource constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("jsonschema2pojo")
    public enum TransactionStatus {

        DRAFT("DRAFT"),
        POSTED("POSTED"),
        VOID("VOID"),
        REVERSED("REVERSED");
        private final String value;
        private final static Map<String, Transaction.TransactionStatus> CONSTANTS = new HashMap<String, Transaction.TransactionStatus>();

        static {
            for (Transaction.TransactionStatus c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TransactionStatus(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Transaction.TransactionStatus fromValue(String value) {
            Transaction.TransactionStatus constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
