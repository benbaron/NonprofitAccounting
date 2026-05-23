
package nonprofitbookkeeping.model.sclx;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "officeAssignmentId",
    "personId",
    "organizationId",
    "roleTitle",
    "membershipNumber",
    "membershipExpiry",
    "startDate",
    "endDate",
    "active",
    "extensions"
})
@Generated("jsonschema2pojo")
public class OfficeAssignment {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("officeAssignmentId")
    @Size(min = 1)
    @NotNull
    private String officeAssignmentId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("personId")
    @Size(min = 1)
    @NotNull
    private String personId;
    @JsonProperty("organizationId")
    private String organizationId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleTitle")
    @Size(min = 1)
    @NotNull
    private String roleTitle;
    @JsonProperty("membershipNumber")
    private String membershipNumber;
    @JsonProperty("membershipExpiry")
    private String membershipExpiry;
    @JsonProperty("startDate")
    private String startDate;
    @JsonProperty("endDate")
    private String endDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("active")
    @NotNull
    private Boolean active;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OfficeAssignment() {
    }

    public OfficeAssignment(String officeAssignmentId, String personId, String organizationId, String roleTitle, String membershipNumber, String membershipExpiry, String startDate, String endDate, Boolean active, Extensions extensions) {
        super();
        this.officeAssignmentId = officeAssignmentId;
        this.personId = personId;
        this.organizationId = organizationId;
        this.roleTitle = roleTitle;
        this.membershipNumber = membershipNumber;
        this.membershipExpiry = membershipExpiry;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("officeAssignmentId")
    public String getOfficeAssignmentId() {
        return officeAssignmentId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("officeAssignmentId")
    public void setOfficeAssignmentId(String officeAssignmentId) {
        this.officeAssignmentId = officeAssignmentId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("personId")
    public String getPersonId() {
        return personId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("personId")
    public void setPersonId(String personId) {
        this.personId = personId;
    }

    @JsonProperty("organizationId")
    public String getOrganizationId() {
        return organizationId;
    }

    @JsonProperty("organizationId")
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleTitle")
    public String getRoleTitle() {
        return roleTitle;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("roleTitle")
    public void setRoleTitle(String roleTitle) {
        this.roleTitle = roleTitle;
    }

    @JsonProperty("membershipNumber")
    public String getMembershipNumber() {
        return membershipNumber;
    }

    @JsonProperty("membershipNumber")
    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
    }

    @JsonProperty("membershipExpiry")
    public String getMembershipExpiry() {
        return membershipExpiry;
    }

    @JsonProperty("membershipExpiry")
    public void setMembershipExpiry(String membershipExpiry) {
        this.membershipExpiry = membershipExpiry;
    }

    @JsonProperty("startDate")
    public String getStartDate() {
        return startDate;
    }

    @JsonProperty("startDate")
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @JsonProperty("endDate")
    public String getEndDate() {
        return endDate;
    }

    @JsonProperty("endDate")
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("active")
    public Boolean getActive() {
        return active;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("active")
    public void setActive(Boolean active) {
        this.active = active;
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
        sb.append(OfficeAssignment.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("officeAssignmentId");
        sb.append('=');
        sb.append(((this.officeAssignmentId == null)?"<null>":this.officeAssignmentId));
        sb.append(',');
        sb.append("personId");
        sb.append('=');
        sb.append(((this.personId == null)?"<null>":this.personId));
        sb.append(',');
        sb.append("organizationId");
        sb.append('=');
        sb.append(((this.organizationId == null)?"<null>":this.organizationId));
        sb.append(',');
        sb.append("roleTitle");
        sb.append('=');
        sb.append(((this.roleTitle == null)?"<null>":this.roleTitle));
        sb.append(',');
        sb.append("membershipNumber");
        sb.append('=');
        sb.append(((this.membershipNumber == null)?"<null>":this.membershipNumber));
        sb.append(',');
        sb.append("membershipExpiry");
        sb.append('=');
        sb.append(((this.membershipExpiry == null)?"<null>":this.membershipExpiry));
        sb.append(',');
        sb.append("startDate");
        sb.append('=');
        sb.append(((this.startDate == null)?"<null>":this.startDate));
        sb.append(',');
        sb.append("endDate");
        sb.append('=');
        sb.append(((this.endDate == null)?"<null>":this.endDate));
        sb.append(',');
        sb.append("active");
        sb.append('=');
        sb.append(((this.active == null)?"<null>":this.active));
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
