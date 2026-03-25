
package nonprofitbookkeeping.model.sclx.generated;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "approvedBy",
    "approvalDate",
    "reason",
    "numberRemoved",
    "removed",
    "removalType"
})
@Generated("jsonschema2pojo")
public class RemovalDetailsAsset {

    @JsonProperty("approvedBy")
    private String approvedBy;
    @JsonProperty("approvalDate")
    private String approvalDate;
    @JsonProperty("reason")
    private String reason;
    @JsonProperty("numberRemoved")
    private Integer numberRemoved;
    @JsonProperty("removed")
    private Boolean removed;
    @JsonProperty("removalType")
    private RemovalDetailsAsset.RemovalType removalType;

    /**
     * No args constructor for use in serialization
     * 
     */
    public RemovalDetailsAsset() {
    }

    public RemovalDetailsAsset(String approvedBy, String approvalDate, String reason, Integer numberRemoved, Boolean removed, RemovalDetailsAsset.RemovalType removalType) {
        super();
        this.approvedBy = approvedBy;
        this.approvalDate = approvalDate;
        this.reason = reason;
        this.numberRemoved = numberRemoved;
        this.removed = removed;
        this.removalType = removalType;
    }

    @JsonProperty("approvedBy")
    public String getApprovedBy() {
        return approvedBy;
    }

    @JsonProperty("approvedBy")
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    @JsonProperty("approvalDate")
    public String getApprovalDate() {
        return approvalDate;
    }

    @JsonProperty("approvalDate")
    public void setApprovalDate(String approvalDate) {
        this.approvalDate = approvalDate;
    }

    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }

    @JsonProperty("reason")
    public void setReason(String reason) {
        this.reason = reason;
    }

    @JsonProperty("numberRemoved")
    public Integer getNumberRemoved() {
        return numberRemoved;
    }

    @JsonProperty("numberRemoved")
    public void setNumberRemoved(Integer numberRemoved) {
        this.numberRemoved = numberRemoved;
    }

    @JsonProperty("removed")
    public Boolean getRemoved() {
        return removed;
    }

    @JsonProperty("removed")
    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

    @JsonProperty("removalType")
    public RemovalDetailsAsset.RemovalType getRemovalType() {
        return removalType;
    }

    @JsonProperty("removalType")
    public void setRemovalType(RemovalDetailsAsset.RemovalType removalType) {
        this.removalType = removalType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(RemovalDetailsAsset.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("approvedBy");
        sb.append('=');
        sb.append(((this.approvedBy == null)?"<null>":this.approvedBy));
        sb.append(',');
        sb.append("approvalDate");
        sb.append('=');
        sb.append(((this.approvalDate == null)?"<null>":this.approvalDate));
        sb.append(',');
        sb.append("reason");
        sb.append('=');
        sb.append(((this.reason == null)?"<null>":this.reason));
        sb.append(',');
        sb.append("numberRemoved");
        sb.append('=');
        sb.append(((this.numberRemoved == null)?"<null>":this.numberRemoved));
        sb.append(',');
        sb.append("removed");
        sb.append('=');
        sb.append(((this.removed == null)?"<null>":this.removed));
        sb.append(',');
        sb.append("removalType");
        sb.append('=');
        sb.append(((this.removalType == null)?"<null>":this.removalType));
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
        result = ((result* 31)+((this.removalType == null)? 0 :this.removalType.hashCode()));
        result = ((result* 31)+((this.reason == null)? 0 :this.reason.hashCode()));
        result = ((result* 31)+((this.approvalDate == null)? 0 :this.approvalDate.hashCode()));
        result = ((result* 31)+((this.removed == null)? 0 :this.removed.hashCode()));
        result = ((result* 31)+((this.approvedBy == null)? 0 :this.approvedBy.hashCode()));
        result = ((result* 31)+((this.numberRemoved == null)? 0 :this.numberRemoved.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RemovalDetailsAsset) == false) {
            return false;
        }
        RemovalDetailsAsset rhs = ((RemovalDetailsAsset) other);
        return (((((((this.removalType == rhs.removalType)||((this.removalType!= null)&&this.removalType.equals(rhs.removalType)))&&((this.reason == rhs.reason)||((this.reason!= null)&&this.reason.equals(rhs.reason))))&&((this.approvalDate == rhs.approvalDate)||((this.approvalDate!= null)&&this.approvalDate.equals(rhs.approvalDate))))&&((this.removed == rhs.removed)||((this.removed!= null)&&this.removed.equals(rhs.removed))))&&((this.approvedBy == rhs.approvedBy)||((this.approvedBy!= null)&&this.approvedBy.equals(rhs.approvedBy))))&&((this.numberRemoved == rhs.numberRemoved)||((this.numberRemoved!= null)&&this.numberRemoved.equals(rhs.numberRemoved))));
    }

    @Generated("jsonschema2pojo")
    public enum RemovalType {

        LOST("LOST"),
        SOLD("SOLD"),
        DONATED("DONATED"),
        DESTROYED("DESTROYED"),
        RETURNED("RETURNED");
        private final String value;
        private final static Map<String, RemovalDetailsAsset.RemovalType> CONSTANTS = new HashMap<String, RemovalDetailsAsset.RemovalType>();

        static {
            for (RemovalDetailsAsset.RemovalType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        RemovalType(String value) {
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
        public static RemovalDetailsAsset.RemovalType fromValue(String value) {
            RemovalDetailsAsset.RemovalType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
