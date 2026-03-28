
package nonprofitbookkeeping.model.sclx.generated;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "outstandingItemId",
    "kind",
    "ledgerLink",
    "workbookLink",
    "dateSentOrReceived",
    "incomingCheckOrTransferDate",
    "transferIdOrCheckNumber",
    "dateShowsOnStatement",
    "personOrBusinessName",
    "detailsNotes",
    "fromToCardMerchant",
    "accountForPaymentOrDeposit",
    "amount",
    "dateReversed",
    "reversalReasonAndApproval",
    "reversalLedgerLink",
    "status",
    "extensions"
})
@Generated("jsonschema2pojo")
public class OutstandingItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("outstandingItemId")
    @Size(min = 1)
    @NotNull
    private String outstandingItemId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    @NotNull
    private OutstandingItem.OutstandingItemKind kind;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ledgerLink")
    @NotNull
    private Object ledgerLink;
    @JsonProperty("workbookLink")
    @Valid
    private WorkbookLink workbookLink;
    @JsonProperty("dateSentOrReceived")
    private String dateSentOrReceived;
    @JsonProperty("incomingCheckOrTransferDate")
    private String incomingCheckOrTransferDate;
    @JsonProperty("transferIdOrCheckNumber")
    private String transferIdOrCheckNumber;
    @JsonProperty("dateShowsOnStatement")
    private String dateShowsOnStatement;
    @JsonProperty("personOrBusinessName")
    private String personOrBusinessName;
    @JsonProperty("detailsNotes")
    private String detailsNotes;
    @JsonProperty("fromToCardMerchant")
    private String fromToCardMerchant;
    @JsonProperty("accountForPaymentOrDeposit")
    private String accountForPaymentOrDeposit;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amount")
    @Pattern(regexp = "^[0-9]+\\.[0-9]{2}$")
    @NotNull
    private String amount;
    @JsonProperty("dateReversed")
    private String dateReversed;
    @JsonProperty("reversalReasonAndApproval")
    private String reversalReasonAndApproval;
    @JsonProperty("reversalLedgerLink")
    private Object reversalLedgerLink;
    @JsonProperty("status")
    private OutstandingItem.OutstandingItemStatus status;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OutstandingItem() {
    }

    public OutstandingItem(String outstandingItemId, OutstandingItem.OutstandingItemKind kind, Object ledgerLink, WorkbookLink workbookLink, String dateSentOrReceived, String incomingCheckOrTransferDate, String transferIdOrCheckNumber, String dateShowsOnStatement, String personOrBusinessName, String detailsNotes, String fromToCardMerchant, String accountForPaymentOrDeposit, String amount, String dateReversed, String reversalReasonAndApproval, Object reversalLedgerLink, OutstandingItem.OutstandingItemStatus status, Extensions extensions) {
        super();
        this.outstandingItemId = outstandingItemId;
        this.kind = kind;
        this.ledgerLink = ledgerLink;
        this.workbookLink = workbookLink;
        this.dateSentOrReceived = dateSentOrReceived;
        this.incomingCheckOrTransferDate = incomingCheckOrTransferDate;
        this.transferIdOrCheckNumber = transferIdOrCheckNumber;
        this.dateShowsOnStatement = dateShowsOnStatement;
        this.personOrBusinessName = personOrBusinessName;
        this.detailsNotes = detailsNotes;
        this.fromToCardMerchant = fromToCardMerchant;
        this.accountForPaymentOrDeposit = accountForPaymentOrDeposit;
        this.amount = amount;
        this.dateReversed = dateReversed;
        this.reversalReasonAndApproval = reversalReasonAndApproval;
        this.reversalLedgerLink = reversalLedgerLink;
        this.status = status;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("outstandingItemId")
    public String getOutstandingItemId() {
        return outstandingItemId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("outstandingItemId")
    public void setOutstandingItemId(String outstandingItemId) {
        this.outstandingItemId = outstandingItemId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    public OutstandingItem.OutstandingItemKind getKind() {
        return kind;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    public void setKind(OutstandingItem.OutstandingItemKind kind) {
        this.kind = kind;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ledgerLink")
    public Object getLedgerLink() {
        return ledgerLink;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ledgerLink")
    public void setLedgerLink(Object ledgerLink) {
        this.ledgerLink = ledgerLink;
    }

    @JsonProperty("workbookLink")
    public WorkbookLink getWorkbookLink() {
        return workbookLink;
    }

    @JsonProperty("workbookLink")
    public void setWorkbookLink(WorkbookLink workbookLink) {
        this.workbookLink = workbookLink;
    }

    @JsonProperty("dateSentOrReceived")
    public String getDateSentOrReceived() {
        return dateSentOrReceived;
    }

    @JsonProperty("dateSentOrReceived")
    public void setDateSentOrReceived(String dateSentOrReceived) {
        this.dateSentOrReceived = dateSentOrReceived;
    }

    @JsonProperty("incomingCheckOrTransferDate")
    public String getIncomingCheckOrTransferDate() {
        return incomingCheckOrTransferDate;
    }

    @JsonProperty("incomingCheckOrTransferDate")
    public void setIncomingCheckOrTransferDate(String incomingCheckOrTransferDate) {
        this.incomingCheckOrTransferDate = incomingCheckOrTransferDate;
    }

    @JsonProperty("transferIdOrCheckNumber")
    public String getTransferIdOrCheckNumber() {
        return transferIdOrCheckNumber;
    }

    @JsonProperty("transferIdOrCheckNumber")
    public void setTransferIdOrCheckNumber(String transferIdOrCheckNumber) {
        this.transferIdOrCheckNumber = transferIdOrCheckNumber;
    }

    @JsonProperty("dateShowsOnStatement")
    public String getDateShowsOnStatement() {
        return dateShowsOnStatement;
    }

    @JsonProperty("dateShowsOnStatement")
    public void setDateShowsOnStatement(String dateShowsOnStatement) {
        this.dateShowsOnStatement = dateShowsOnStatement;
    }

    @JsonProperty("personOrBusinessName")
    public String getPersonOrBusinessName() {
        return personOrBusinessName;
    }

    @JsonProperty("personOrBusinessName")
    public void setPersonOrBusinessName(String personOrBusinessName) {
        this.personOrBusinessName = personOrBusinessName;
    }

    @JsonProperty("detailsNotes")
    public String getDetailsNotes() {
        return detailsNotes;
    }

    @JsonProperty("detailsNotes")
    public void setDetailsNotes(String detailsNotes) {
        this.detailsNotes = detailsNotes;
    }

    @JsonProperty("fromToCardMerchant")
    public String getFromToCardMerchant() {
        return fromToCardMerchant;
    }

    @JsonProperty("fromToCardMerchant")
    public void setFromToCardMerchant(String fromToCardMerchant) {
        this.fromToCardMerchant = fromToCardMerchant;
    }

    @JsonProperty("accountForPaymentOrDeposit")
    public String getAccountForPaymentOrDeposit() {
        return accountForPaymentOrDeposit;
    }

    @JsonProperty("accountForPaymentOrDeposit")
    public void setAccountForPaymentOrDeposit(String accountForPaymentOrDeposit) {
        this.accountForPaymentOrDeposit = accountForPaymentOrDeposit;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amount")
    public String getAmount() {
        return amount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amount")
    public void setAmount(String amount) {
        this.amount = amount;
    }

    @JsonProperty("dateReversed")
    public String getDateReversed() {
        return dateReversed;
    }

    @JsonProperty("dateReversed")
    public void setDateReversed(String dateReversed) {
        this.dateReversed = dateReversed;
    }

    @JsonProperty("reversalReasonAndApproval")
    public String getReversalReasonAndApproval() {
        return reversalReasonAndApproval;
    }

    @JsonProperty("reversalReasonAndApproval")
    public void setReversalReasonAndApproval(String reversalReasonAndApproval) {
        this.reversalReasonAndApproval = reversalReasonAndApproval;
    }

    @JsonProperty("reversalLedgerLink")
    public Object getReversalLedgerLink() {
        return reversalLedgerLink;
    }

    @JsonProperty("reversalLedgerLink")
    public void setReversalLedgerLink(Object reversalLedgerLink) {
        this.reversalLedgerLink = reversalLedgerLink;
    }

    @JsonProperty("status")
    public OutstandingItem.OutstandingItemStatus getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(OutstandingItem.OutstandingItemStatus status) {
        this.status = status;
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
        sb.append(OutstandingItem.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("outstandingItemId");
        sb.append('=');
        sb.append(((this.outstandingItemId == null)?"<null>":this.outstandingItemId));
        sb.append(',');
        sb.append("kind");
        sb.append('=');
        sb.append(((this.kind == null)?"<null>":this.kind));
        sb.append(',');
        sb.append("ledgerLink");
        sb.append('=');
        sb.append(((this.ledgerLink == null)?"<null>":this.ledgerLink));
        sb.append(',');
        sb.append("workbookLink");
        sb.append('=');
        sb.append(((this.workbookLink == null)?"<null>":this.workbookLink));
        sb.append(',');
        sb.append("dateSentOrReceived");
        sb.append('=');
        sb.append(((this.dateSentOrReceived == null)?"<null>":this.dateSentOrReceived));
        sb.append(',');
        sb.append("incomingCheckOrTransferDate");
        sb.append('=');
        sb.append(((this.incomingCheckOrTransferDate == null)?"<null>":this.incomingCheckOrTransferDate));
        sb.append(',');
        sb.append("transferIdOrCheckNumber");
        sb.append('=');
        sb.append(((this.transferIdOrCheckNumber == null)?"<null>":this.transferIdOrCheckNumber));
        sb.append(',');
        sb.append("dateShowsOnStatement");
        sb.append('=');
        sb.append(((this.dateShowsOnStatement == null)?"<null>":this.dateShowsOnStatement));
        sb.append(',');
        sb.append("personOrBusinessName");
        sb.append('=');
        sb.append(((this.personOrBusinessName == null)?"<null>":this.personOrBusinessName));
        sb.append(',');
        sb.append("detailsNotes");
        sb.append('=');
        sb.append(((this.detailsNotes == null)?"<null>":this.detailsNotes));
        sb.append(',');
        sb.append("fromToCardMerchant");
        sb.append('=');
        sb.append(((this.fromToCardMerchant == null)?"<null>":this.fromToCardMerchant));
        sb.append(',');
        sb.append("accountForPaymentOrDeposit");
        sb.append('=');
        sb.append(((this.accountForPaymentOrDeposit == null)?"<null>":this.accountForPaymentOrDeposit));
        sb.append(',');
        sb.append("amount");
        sb.append('=');
        sb.append(((this.amount == null)?"<null>":this.amount));
        sb.append(',');
        sb.append("dateReversed");
        sb.append('=');
        sb.append(((this.dateReversed == null)?"<null>":this.dateReversed));
        sb.append(',');
        sb.append("reversalReasonAndApproval");
        sb.append('=');
        sb.append(((this.reversalReasonAndApproval == null)?"<null>":this.reversalReasonAndApproval));
        sb.append(',');
        sb.append("reversalLedgerLink");
        sb.append('=');
        sb.append(((this.reversalLedgerLink == null)?"<null>":this.reversalLedgerLink));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
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
        result = ((result* 31)+((this.amount == null)? 0 :this.amount.hashCode()));
        result = ((result* 31)+((this.fromToCardMerchant == null)? 0 :this.fromToCardMerchant.hashCode()));
        result = ((result* 31)+((this.kind == null)? 0 :this.kind.hashCode()));
        result = ((result* 31)+((this.personOrBusinessName == null)? 0 :this.personOrBusinessName.hashCode()));
        result = ((result* 31)+((this.reversalReasonAndApproval == null)? 0 :this.reversalReasonAndApproval.hashCode()));
        result = ((result* 31)+((this.incomingCheckOrTransferDate == null)? 0 :this.incomingCheckOrTransferDate.hashCode()));
        result = ((result* 31)+((this.transferIdOrCheckNumber == null)? 0 :this.transferIdOrCheckNumber.hashCode()));
        result = ((result* 31)+((this.reversalLedgerLink == null)? 0 :this.reversalLedgerLink.hashCode()));
        result = ((result* 31)+((this.workbookLink == null)? 0 :this.workbookLink.hashCode()));
        result = ((result* 31)+((this.detailsNotes == null)? 0 :this.detailsNotes.hashCode()));
        result = ((result* 31)+((this.extensions == null)? 0 :this.extensions.hashCode()));
        result = ((result* 31)+((this.outstandingItemId == null)? 0 :this.outstandingItemId.hashCode()));
        result = ((result* 31)+((this.ledgerLink == null)? 0 :this.ledgerLink.hashCode()));
        result = ((result* 31)+((this.dateSentOrReceived == null)? 0 :this.dateSentOrReceived.hashCode()));
        result = ((result* 31)+((this.dateShowsOnStatement == null)? 0 :this.dateShowsOnStatement.hashCode()));
        result = ((result* 31)+((this.accountForPaymentOrDeposit == null)? 0 :this.accountForPaymentOrDeposit.hashCode()));
        result = ((result* 31)+((this.dateReversed == null)? 0 :this.dateReversed.hashCode()));
        result = ((result* 31)+((this.status == null)? 0 :this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OutstandingItem) == false) {
            return false;
        }
        OutstandingItem rhs = ((OutstandingItem) other);
        return (((((((((((((((((((this.amount == rhs.amount)||((this.amount!= null)&&this.amount.equals(rhs.amount)))&&((this.fromToCardMerchant == rhs.fromToCardMerchant)||((this.fromToCardMerchant!= null)&&this.fromToCardMerchant.equals(rhs.fromToCardMerchant))))&&((this.kind == rhs.kind)||((this.kind!= null)&&this.kind.equals(rhs.kind))))&&((this.personOrBusinessName == rhs.personOrBusinessName)||((this.personOrBusinessName!= null)&&this.personOrBusinessName.equals(rhs.personOrBusinessName))))&&((this.reversalReasonAndApproval == rhs.reversalReasonAndApproval)||((this.reversalReasonAndApproval!= null)&&this.reversalReasonAndApproval.equals(rhs.reversalReasonAndApproval))))&&((this.incomingCheckOrTransferDate == rhs.incomingCheckOrTransferDate)||((this.incomingCheckOrTransferDate!= null)&&this.incomingCheckOrTransferDate.equals(rhs.incomingCheckOrTransferDate))))&&((this.transferIdOrCheckNumber == rhs.transferIdOrCheckNumber)||((this.transferIdOrCheckNumber!= null)&&this.transferIdOrCheckNumber.equals(rhs.transferIdOrCheckNumber))))&&((this.reversalLedgerLink == rhs.reversalLedgerLink)||((this.reversalLedgerLink!= null)&&this.reversalLedgerLink.equals(rhs.reversalLedgerLink))))&&((this.workbookLink == rhs.workbookLink)||((this.workbookLink!= null)&&this.workbookLink.equals(rhs.workbookLink))))&&((this.detailsNotes == rhs.detailsNotes)||((this.detailsNotes!= null)&&this.detailsNotes.equals(rhs.detailsNotes))))&&((this.extensions == rhs.extensions)||((this.extensions!= null)&&this.extensions.equals(rhs.extensions))))&&((this.outstandingItemId == rhs.outstandingItemId)||((this.outstandingItemId!= null)&&this.outstandingItemId.equals(rhs.outstandingItemId))))&&((this.ledgerLink == rhs.ledgerLink)||((this.ledgerLink!= null)&&this.ledgerLink.equals(rhs.ledgerLink))))&&((this.dateSentOrReceived == rhs.dateSentOrReceived)||((this.dateSentOrReceived!= null)&&this.dateSentOrReceived.equals(rhs.dateSentOrReceived))))&&((this.dateShowsOnStatement == rhs.dateShowsOnStatement)||((this.dateShowsOnStatement!= null)&&this.dateShowsOnStatement.equals(rhs.dateShowsOnStatement))))&&((this.accountForPaymentOrDeposit == rhs.accountForPaymentOrDeposit)||((this.accountForPaymentOrDeposit!= null)&&this.accountForPaymentOrDeposit.equals(rhs.accountForPaymentOrDeposit))))&&((this.dateReversed == rhs.dateReversed)||((this.dateReversed!= null)&&this.dateReversed.equals(rhs.dateReversed))))&&((this.status == rhs.status)||((this.status!= null)&&this.status.equals(rhs.status))));
    }

    @Generated("jsonschema2pojo")
    public enum OutstandingItemKind {

        CHECK("CHECK"),
        TRANSFER("TRANSFER"),
        DEPOSIT("DEPOSIT"),
        INCOMING_CHECK("INCOMING_CHECK"),
        CARD_ITEM("CARD_ITEM"),
        OTHER("OTHER");
        private final String value;
        private final static Map<String, OutstandingItem.OutstandingItemKind> CONSTANTS = new HashMap<String, OutstandingItem.OutstandingItemKind>();

        static {
            for (OutstandingItem.OutstandingItemKind c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        OutstandingItemKind(String value) {
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
        public static OutstandingItem.OutstandingItemKind fromValue(String value) {
            OutstandingItem.OutstandingItemKind constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("jsonschema2pojo")
    public enum OutstandingItemStatus {

        OUTSTANDING("OUTSTANDING"),
        CLEARED("CLEARED"),
        REVERSED("REVERSED"),
        VOID("VOID"),
        PENDING("PENDING");
        private final String value;
        private final static Map<String, OutstandingItem.OutstandingItemStatus> CONSTANTS = new HashMap<String, OutstandingItem.OutstandingItemStatus>();

        static {
            for (OutstandingItem.OutstandingItemStatus c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        OutstandingItemStatus(String value) {
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
        public static OutstandingItem.OutstandingItemStatus fromValue(String value) {
            OutstandingItem.OutstandingItemStatus constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
