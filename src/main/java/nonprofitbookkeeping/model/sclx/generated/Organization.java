
package nonprofitbookkeeping.model.sclx.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "organizationId",
    "name",
    "parentOrganization",
    "baseCurrency",
    "fiscalYearStart",
    "fiscalYearEnd",
    "extensions"
})
@Generated("jsonschema2pojo")
public class Organization {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("organizationId")
    @Size(min = 1)
    @NotNull
    private String organizationId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    @Size(min = 1)
    @NotNull
    private String name;
    @JsonProperty("parentOrganization")
    private String parentOrganization;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("baseCurrency")
    @Pattern(regexp = "^[A-Z]{3}$")
    @NotNull
    private String baseCurrency;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fiscalYearStart")
    @NotNull
    private String fiscalYearStart;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fiscalYearEnd")
    @NotNull
    private String fiscalYearEnd;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Organization() {
    }

    public Organization(String organizationId, String name, String parentOrganization, String baseCurrency, String fiscalYearStart, String fiscalYearEnd, Extensions extensions) {
        super();
        this.organizationId = organizationId;
        this.name = name;
        this.parentOrganization = parentOrganization;
        this.baseCurrency = baseCurrency;
        this.fiscalYearStart = fiscalYearStart;
        this.fiscalYearEnd = fiscalYearEnd;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("organizationId")
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("organizationId")
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("parentOrganization")
    public String getParentOrganization() {
        return parentOrganization;
    }

    @JsonProperty("parentOrganization")
    public void setParentOrganization(String parentOrganization) {
        this.parentOrganization = parentOrganization;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("baseCurrency")
    public String getBaseCurrency() {
        return baseCurrency;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("baseCurrency")
    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fiscalYearStart")
    public String getFiscalYearStart() {
        return fiscalYearStart;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fiscalYearStart")
    public void setFiscalYearStart(String fiscalYearStart) {
        this.fiscalYearStart = fiscalYearStart;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fiscalYearEnd")
    public String getFiscalYearEnd() {
        return fiscalYearEnd;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fiscalYearEnd")
    public void setFiscalYearEnd(String fiscalYearEnd) {
        this.fiscalYearEnd = fiscalYearEnd;
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
        sb.append(Organization.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("organizationId");
        sb.append('=');
        sb.append(((this.organizationId == null)?"<null>":this.organizationId));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("parentOrganization");
        sb.append('=');
        sb.append(((this.parentOrganization == null)?"<null>":this.parentOrganization));
        sb.append(',');
        sb.append("baseCurrency");
        sb.append('=');
        sb.append(((this.baseCurrency == null)?"<null>":this.baseCurrency));
        sb.append(',');
        sb.append("fiscalYearStart");
        sb.append('=');
        sb.append(((this.fiscalYearStart == null)?"<null>":this.fiscalYearStart));
        sb.append(',');
        sb.append("fiscalYearEnd");
        sb.append('=');
        sb.append(((this.fiscalYearEnd == null)?"<null>":this.fiscalYearEnd));
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
        result = ((result* 31)+((this.organizationId == null)? 0 :this.organizationId.hashCode()));
        result = ((result* 31)+((this.fiscalYearEnd == null)? 0 :this.fiscalYearEnd.hashCode()));
        result = ((result* 31)+((this.extensions == null)? 0 :this.extensions.hashCode()));
        result = ((result* 31)+((this.fiscalYearStart == null)? 0 :this.fiscalYearStart.hashCode()));
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.parentOrganization == null)? 0 :this.parentOrganization.hashCode()));
        result = ((result* 31)+((this.baseCurrency == null)? 0 :this.baseCurrency.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Organization) == false) {
            return false;
        }
        Organization rhs = ((Organization) other);
        return ((((((((this.organizationId == rhs.organizationId)||((this.organizationId!= null)&&this.organizationId.equals(rhs.organizationId)))&&((this.fiscalYearEnd == rhs.fiscalYearEnd)||((this.fiscalYearEnd!= null)&&this.fiscalYearEnd.equals(rhs.fiscalYearEnd))))&&((this.extensions == rhs.extensions)||((this.extensions!= null)&&this.extensions.equals(rhs.extensions))))&&((this.fiscalYearStart == rhs.fiscalYearStart)||((this.fiscalYearStart!= null)&&this.fiscalYearStart.equals(rhs.fiscalYearStart))))&&((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name))))&&((this.parentOrganization == rhs.parentOrganization)||((this.parentOrganization!= null)&&this.parentOrganization.equals(rhs.parentOrganization))))&&((this.baseCurrency == rhs.baseCurrency)||((this.baseCurrency!= null)&&this.baseCurrency.equals(rhs.baseCurrency))));
    }

}
