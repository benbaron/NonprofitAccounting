
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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "otherAssetItemId",
    "ledgerLink",
    "workbookLink",
    "paidTo",
    "year",
    "reason",
    "type",
    "typeCode",
    "eventBudgetLabel",
    "amountAsOfPriorYearEnd",
    "paidReturnedOnLedgerRowIndex",
    "settlementLedgerLink",
    "status",
    "extensions"
})
@Generated("jsonschema2pojo")
public class OtherAssetItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("otherAssetItemId")
    @Size(min = 1)
    @NotNull
    private String otherAssetItemId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ledgerLink")
    @Valid
    @NotNull
    private LedgerLink ledgerLink;
    @JsonProperty("workbookLink")
    @Valid
    private WorkbookLink workbookLink;
    @JsonProperty("paidTo")
    private String paidTo;
    @JsonProperty("year")
    private Integer year;
    @JsonProperty("reason")
    private String reason;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    @NotNull
    private OtherAssetItem.OtherAssetItemType type;
    @JsonProperty("typeCode")
    private OtherAssetItem.TypeCode typeCode;
    @JsonProperty("eventBudgetLabel")
    private String eventBudgetLabel;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amountAsOfPriorYearEnd")
    @Pattern(regexp = "^[0-9]+\\.[0-9]{2}$")
    @NotNull
    private String amountAsOfPriorYearEnd;
    @JsonProperty("paidReturnedOnLedgerRowIndex")
    @DecimalMin("1")
    private Integer paidReturnedOnLedgerRowIndex;
    @JsonProperty("settlementLedgerLink")
    @Valid
    private LedgerLink settlementLedgerLink;
    @JsonProperty("status")
    private OtherAssetItem.OtherAssetItemStatus status;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OtherAssetItem() {
    }

    public OtherAssetItem(String otherAssetItemId, LedgerLink ledgerLink, WorkbookLink workbookLink, String paidTo, Integer year, String reason, OtherAssetItem.OtherAssetItemType type, OtherAssetItem.TypeCode typeCode, String eventBudgetLabel, String amountAsOfPriorYearEnd, Integer paidReturnedOnLedgerRowIndex, LedgerLink settlementLedgerLink, OtherAssetItem.OtherAssetItemStatus status, Extensions extensions) {
        super();
        this.otherAssetItemId = otherAssetItemId;
        this.ledgerLink = ledgerLink;
        this.workbookLink = workbookLink;
        this.paidTo = paidTo;
        this.year = year;
        this.reason = reason;
        this.type = type;
        this.typeCode = typeCode;
        this.eventBudgetLabel = eventBudgetLabel;
        this.amountAsOfPriorYearEnd = amountAsOfPriorYearEnd;
        this.paidReturnedOnLedgerRowIndex = paidReturnedOnLedgerRowIndex;
        this.settlementLedgerLink = settlementLedgerLink;
        this.status = status;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("otherAssetItemId")
    public String getOtherAssetItemId() {
        return otherAssetItemId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("otherAssetItemId")
    public void setOtherAssetItemId(String otherAssetItemId) {
        this.otherAssetItemId = otherAssetItemId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ledgerLink")
    public LedgerLink getLedgerLink() {
        return ledgerLink;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ledgerLink")
    public void setLedgerLink(LedgerLink ledgerLink) {
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

    @JsonProperty("paidTo")
    public String getPaidTo() {
        return paidTo;
    }

    @JsonProperty("paidTo")
    public void setPaidTo(String paidTo) {
        this.paidTo = paidTo;
    }

    @JsonProperty("year")
    public Integer getYear() {
        return year;
    }

    @JsonProperty("year")
    public void setYear(Integer year) {
        this.year = year;
    }

    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }

    @JsonProperty("reason")
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public OtherAssetItem.OtherAssetItemType getType() {
        return type;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(OtherAssetItem.OtherAssetItemType type) {
        this.type = type;
    }

    @JsonProperty("typeCode")
    public OtherAssetItem.TypeCode getTypeCode() {
        return typeCode;
    }

    @JsonProperty("typeCode")
    public void setTypeCode(OtherAssetItem.TypeCode typeCode) {
        this.typeCode = typeCode;
    }

    @JsonProperty("eventBudgetLabel")
    public String getEventBudgetLabel() {
        return eventBudgetLabel;
    }

    @JsonProperty("eventBudgetLabel")
    public void setEventBudgetLabel(String eventBudgetLabel) {
        this.eventBudgetLabel = eventBudgetLabel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amountAsOfPriorYearEnd")
    public String getAmountAsOfPriorYearEnd() {
        return amountAsOfPriorYearEnd;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amountAsOfPriorYearEnd")
    public void setAmountAsOfPriorYearEnd(String amountAsOfPriorYearEnd) {
        this.amountAsOfPriorYearEnd = amountAsOfPriorYearEnd;
    }

    @JsonProperty("paidReturnedOnLedgerRowIndex")
    public Integer getPaidReturnedOnLedgerRowIndex() {
        return paidReturnedOnLedgerRowIndex;
    }

    @JsonProperty("paidReturnedOnLedgerRowIndex")
    public void setPaidReturnedOnLedgerRowIndex(Integer paidReturnedOnLedgerRowIndex) {
        this.paidReturnedOnLedgerRowIndex = paidReturnedOnLedgerRowIndex;
    }

    @JsonProperty("settlementLedgerLink")
    public LedgerLink getSettlementLedgerLink() {
        return settlementLedgerLink;
    }

    @JsonProperty("settlementLedgerLink")
    public void setSettlementLedgerLink(LedgerLink settlementLedgerLink) {
        this.settlementLedgerLink = settlementLedgerLink;
    }

    @JsonProperty("status")
    public OtherAssetItem.OtherAssetItemStatus getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(OtherAssetItem.OtherAssetItemStatus status) {
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
        sb.append(OtherAssetItem.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("otherAssetItemId");
        sb.append('=');
        sb.append(((this.otherAssetItemId == null)?"<null>":this.otherAssetItemId));
        sb.append(',');
        sb.append("ledgerLink");
        sb.append('=');
        sb.append(((this.ledgerLink == null)?"<null>":this.ledgerLink));
        sb.append(',');
        sb.append("workbookLink");
        sb.append('=');
        sb.append(((this.workbookLink == null)?"<null>":this.workbookLink));
        sb.append(',');
        sb.append("paidTo");
        sb.append('=');
        sb.append(((this.paidTo == null)?"<null>":this.paidTo));
        sb.append(',');
        sb.append("year");
        sb.append('=');
        sb.append(((this.year == null)?"<null>":this.year));
        sb.append(',');
        sb.append("reason");
        sb.append('=');
        sb.append(((this.reason == null)?"<null>":this.reason));
        sb.append(',');
        sb.append("type");
        sb.append('=');
        sb.append(((this.type == null)?"<null>":this.type));
        sb.append(',');
        sb.append("typeCode");
        sb.append('=');
        sb.append(((this.typeCode == null)?"<null>":this.typeCode));
        sb.append(',');
        sb.append("eventBudgetLabel");
        sb.append('=');
        sb.append(((this.eventBudgetLabel == null)?"<null>":this.eventBudgetLabel));
        sb.append(',');
        sb.append("amountAsOfPriorYearEnd");
        sb.append('=');
        sb.append(((this.amountAsOfPriorYearEnd == null)?"<null>":this.amountAsOfPriorYearEnd));
        sb.append(',');
        sb.append("paidReturnedOnLedgerRowIndex");
        sb.append('=');
        sb.append(((this.paidReturnedOnLedgerRowIndex == null)?"<null>":this.paidReturnedOnLedgerRowIndex));
        sb.append(',');
        sb.append("settlementLedgerLink");
        sb.append('=');
        sb.append(((this.settlementLedgerLink == null)?"<null>":this.settlementLedgerLink));
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
        result = ((result* 31)+((this.paidReturnedOnLedgerRowIndex == null)? 0 :this.paidReturnedOnLedgerRowIndex.hashCode()));
        result = ((result* 31)+((this.paidTo == null)? 0 :this.paidTo.hashCode()));
        result = ((result* 31)+((this.reason == null)? 0 :this.reason.hashCode()));
        result = ((result* 31)+((this.otherAssetItemId == null)? 0 :this.otherAssetItemId.hashCode()));
        result = ((result* 31)+((this.year == null)? 0 :this.year.hashCode()));
        result = ((result* 31)+((this.type == null)? 0 :this.type.hashCode()));
        result = ((result* 31)+((this.eventBudgetLabel == null)? 0 :this.eventBudgetLabel.hashCode()));
        result = ((result* 31)+((this.amountAsOfPriorYearEnd == null)? 0 :this.amountAsOfPriorYearEnd.hashCode()));
        result = ((result* 31)+((this.typeCode == null)? 0 :this.typeCode.hashCode()));
        result = ((result* 31)+((this.workbookLink == null)? 0 :this.workbookLink.hashCode()));
        result = ((result* 31)+((this.extensions == null)? 0 :this.extensions.hashCode()));
        result = ((result* 31)+((this.settlementLedgerLink == null)? 0 :this.settlementLedgerLink.hashCode()));
        result = ((result* 31)+((this.ledgerLink == null)? 0 :this.ledgerLink.hashCode()));
        result = ((result* 31)+((this.status == null)? 0 :this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OtherAssetItem) == false) {
            return false;
        }
        OtherAssetItem rhs = ((OtherAssetItem) other);
        return (((((((((((((((this.paidReturnedOnLedgerRowIndex == rhs.paidReturnedOnLedgerRowIndex)||((this.paidReturnedOnLedgerRowIndex!= null)&&this.paidReturnedOnLedgerRowIndex.equals(rhs.paidReturnedOnLedgerRowIndex)))&&((this.paidTo == rhs.paidTo)||((this.paidTo!= null)&&this.paidTo.equals(rhs.paidTo))))&&((this.reason == rhs.reason)||((this.reason!= null)&&this.reason.equals(rhs.reason))))&&((this.otherAssetItemId == rhs.otherAssetItemId)||((this.otherAssetItemId!= null)&&this.otherAssetItemId.equals(rhs.otherAssetItemId))))&&((this.year == rhs.year)||((this.year!= null)&&this.year.equals(rhs.year))))&&((this.type == rhs.type)||((this.type!= null)&&this.type.equals(rhs.type))))&&((this.eventBudgetLabel == rhs.eventBudgetLabel)||((this.eventBudgetLabel!= null)&&this.eventBudgetLabel.equals(rhs.eventBudgetLabel))))&&((this.amountAsOfPriorYearEnd == rhs.amountAsOfPriorYearEnd)||((this.amountAsOfPriorYearEnd!= null)&&this.amountAsOfPriorYearEnd.equals(rhs.amountAsOfPriorYearEnd))))&&((this.typeCode == rhs.typeCode)||((this.typeCode!= null)&&this.typeCode.equals(rhs.typeCode))))&&((this.workbookLink == rhs.workbookLink)||((this.workbookLink!= null)&&this.workbookLink.equals(rhs.workbookLink))))&&((this.extensions == rhs.extensions)||((this.extensions!= null)&&this.extensions.equals(rhs.extensions))))&&((this.settlementLedgerLink == rhs.settlementLedgerLink)||((this.settlementLedgerLink!= null)&&this.settlementLedgerLink.equals(rhs.settlementLedgerLink))))&&((this.ledgerLink == rhs.ledgerLink)||((this.ledgerLink!= null)&&this.ledgerLink.equals(rhs.ledgerLink))))&&((this.status == rhs.status)||((this.status!= null)&&this.status.equals(rhs.status))));
    }

    @Generated("jsonschema2pojo")
    public enum OtherAssetItemStatus {

        OUTSTANDING("OUTSTANDING"),
        PAID("PAID"),
        RETURNED("RETURNED"),
        CLEARED("CLEARED"),
        VOID("VOID");
        private final String value;
        private final static Map<String, OtherAssetItem.OtherAssetItemStatus> CONSTANTS = new HashMap<String, OtherAssetItem.OtherAssetItemStatus>();

        static {
            for (OtherAssetItem.OtherAssetItemStatus c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        OtherAssetItemStatus(String value) {
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
        public static OtherAssetItem.OtherAssetItemStatus fromValue(String value) {
            OtherAssetItem.OtherAssetItemStatus constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("jsonschema2pojo")
    public enum OtherAssetItemType {

        CASH_ADVANCE("CASH_ADVANCE"),
        SITE_SECURITY_DEPOSIT("SITE_SECURITY_DEPOSIT"),
        OTHER("OTHER");
        private final String value;
        private final static Map<String, OtherAssetItem.OtherAssetItemType> CONSTANTS = new HashMap<String, OtherAssetItem.OtherAssetItemType>();

        static {
            for (OtherAssetItem.OtherAssetItemType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        OtherAssetItemType(String value) {
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
        public static OtherAssetItem.OtherAssetItemType fromValue(String value) {
            OtherAssetItem.OtherAssetItemType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("jsonschema2pojo")
    public enum TypeCode {

        C("C"),
        S("S"),
        O("O");
        private final String value;
        private final static Map<String, OtherAssetItem.TypeCode> CONSTANTS = new HashMap<String, OtherAssetItem.TypeCode>();

        static {
            for (OtherAssetItem.TypeCode c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        TypeCode(String value) {
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
        public static OtherAssetItem.TypeCode fromValue(String value) {
            OtherAssetItem.TypeCode constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
