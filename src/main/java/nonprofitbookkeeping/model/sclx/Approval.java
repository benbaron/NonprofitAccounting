
package nonprofitbookkeeping.model.sclx;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "policyRequired",
    "committeeApprovalRef",
    "approvedBy",
    "approvalDate",
    "notes",
    "extensions"
})
@Generated("jsonschema2pojo")
public class Approval {

    @JsonProperty("policyRequired")
    private Boolean policyRequired;
    @JsonProperty("committeeApprovalRef")
    private String committeeApprovalRef;
    @JsonProperty("approvedBy")
    private List<@Valid String> approvedBy = new ArrayList<String>();
    @JsonProperty("approvalDate")
    private String approvalDate;
    @JsonProperty("notes")
    private String notes;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Approval() {
    }

    public Approval(Boolean policyRequired, String committeeApprovalRef, List<@Valid String> approvedBy, String approvalDate, String notes, Extensions extensions) {
        super();
        this.policyRequired = policyRequired;
        this.committeeApprovalRef = committeeApprovalRef;
        this.approvedBy = approvedBy;
        this.approvalDate = approvalDate;
        this.notes = notes;
        this.extensions = extensions;
    }

    @JsonProperty("policyRequired")
    public Boolean getPolicyRequired() {
        return policyRequired;
    }

    @JsonProperty("policyRequired")
    public void setPolicyRequired(Boolean policyRequired) {
        this.policyRequired = policyRequired;
    }

    @JsonProperty("committeeApprovalRef")
    public String getCommitteeApprovalRef() {
        return committeeApprovalRef;
    }

    @JsonProperty("committeeApprovalRef")
    public void setCommitteeApprovalRef(String committeeApprovalRef) {
        this.committeeApprovalRef = committeeApprovalRef;
    }

    @JsonProperty("approvedBy")
    public List<String> getApprovedBy() {
        return approvedBy;
    }

    @JsonProperty("approvedBy")
    public void setApprovedBy(List<String> approvedBy) {
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

    @JsonProperty("notes")
    public String getNotes() {
        return notes;
    }

    @JsonProperty("notes")
    public void setNotes(String notes) {
        this.notes = notes;
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
        sb.append(Approval.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("policyRequired");
        sb.append('=');
        sb.append(((this.policyRequired == null)?"<null>":this.policyRequired));
        sb.append(',');
        sb.append("committeeApprovalRef");
        sb.append('=');
        sb.append(((this.committeeApprovalRef == null)?"<null>":this.committeeApprovalRef));
        sb.append(',');
        sb.append("approvedBy");
        sb.append('=');
        sb.append(((this.approvedBy == null)?"<null>":this.approvedBy));
        sb.append(',');
        sb.append("approvalDate");
        sb.append('=');
        sb.append(((this.approvalDate == null)?"<null>":this.approvalDate));
        sb.append(',');
        sb.append("notes");
        sb.append('=');
        sb.append(((this.notes == null)?"<null>":this.notes));
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

}
