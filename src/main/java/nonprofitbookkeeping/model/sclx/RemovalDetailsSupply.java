
package nonprofitbookkeeping.model.sclx;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "approvedBy",
    "reason",
    "numberRemoved",
    "removed",
    "removalType"
})
@Generated("jsonschema2pojo")
public class RemovalDetailsSupply {

    @JsonProperty("approvedBy")
    private String approvedBy;
    @JsonProperty("reason")
    private String reason;
    @JsonProperty("numberRemoved")
    private Integer numberRemoved;
    @JsonProperty("removed")
    private Boolean removed;
    @JsonProperty("removalType")
    private nonprofitbookkeeping.model.sclx.RemovalDetailsAsset.RemovalType removalType;

    /**
     * No args constructor for use in serialization
     * 
     */
    public RemovalDetailsSupply() {
    }

    public RemovalDetailsSupply(String approvedBy, String reason, Integer numberRemoved, Boolean removed, nonprofitbookkeeping.model.sclx.RemovalDetailsAsset.RemovalType removalType) {
        super();
        this.approvedBy = approvedBy;
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
    public nonprofitbookkeeping.model.sclx.RemovalDetailsAsset.RemovalType getRemovalType() {
        return removalType;
    }

    @JsonProperty("removalType")
    public void setRemovalType(nonprofitbookkeeping.model.sclx.RemovalDetailsAsset.RemovalType removalType) {
        this.removalType = removalType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(RemovalDetailsSupply.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("approvedBy");
        sb.append('=');
        sb.append(((this.approvedBy == null)?"<null>":this.approvedBy));
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

}
