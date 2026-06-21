
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
    "personId",
    "legalName",
    "membershipNumber",
    "membershipExpiry",
    "role",
    "status",
    "extensions"
})
@Generated("jsonschema2pojo")
public class BankAccountSigner {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("personId")
    @Size(min = 1)
    @NotNull
    private String personId;
    @JsonProperty("legalName")
    private String legalName;
    @JsonProperty("membershipNumber")
    private String membershipNumber;
    @JsonProperty("membershipExpiry")
    private String membershipExpiry;
    @JsonProperty("role")
    private String role;
    @JsonProperty("status")
    private String status;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public BankAccountSigner() {
    }

    public BankAccountSigner(String personId, String legalName, String membershipNumber, String membershipExpiry, String role, String status, Extensions extensions) {
        super();
        this.personId = personId;
        this.legalName = legalName;
        this.membershipNumber = membershipNumber;
        this.membershipExpiry = membershipExpiry;
        this.role = role;
        this.status = status;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("personId")
    public String getPersonId() {
        return this.personId;
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

    @JsonProperty("legalName")
    public String getLegalName() {
        return this.legalName;
    }

    @JsonProperty("legalName")
    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    @JsonProperty("membershipNumber")
    public String getMembershipNumber() {
        return this.membershipNumber;
    }

    @JsonProperty("membershipNumber")
    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
    }

    @JsonProperty("membershipExpiry")
    public String getMembershipExpiry() {
        return this.membershipExpiry;
    }

    @JsonProperty("membershipExpiry")
    public void setMembershipExpiry(String membershipExpiry) {
        this.membershipExpiry = membershipExpiry;
    }

    @JsonProperty("role")
    public String getRole() {
        return this.role;
    }

    @JsonProperty("role")
    public void setRole(String role) {
        this.role = role;
    }

    @JsonProperty("status")
    public String getStatus() {
        return this.status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("extensions")
    public Extensions getExtensions() {
        return this.extensions;
    }

    @JsonProperty("extensions")
    public void setExtensions(Extensions extensions) {
        this.extensions = extensions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(BankAccountSigner.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("personId");
        sb.append('=');
        sb.append(((this.personId == null)?"<null>":this.personId));
        sb.append(',');
        sb.append("legalName");
        sb.append('=');
        sb.append(((this.legalName == null)?"<null>":this.legalName));
        sb.append(',');
        sb.append("membershipNumber");
        sb.append('=');
        sb.append(((this.membershipNumber == null)?"<null>":this.membershipNumber));
        sb.append(',');
        sb.append("membershipExpiry");
        sb.append('=');
        sb.append(((this.membershipExpiry == null)?"<null>":this.membershipExpiry));
        sb.append(',');
        sb.append("role");
        sb.append('=');
        sb.append(((this.role == null)?"<null>":this.role));
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

}
