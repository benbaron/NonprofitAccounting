
package nonprofitbookkeeping.model.sclx;

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
    "extensions",
    "organizationType",
    "jurisdictionState",
    "parentJurisdictionState",
    "taxId",
    "parentTaxId",
    "sharesParent501c",
    "capitalizationThreshold",
    "reportingQuarter",
    "reportingYear"
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
    @JsonProperty("organizationType")
    private String organizationType;
    @JsonProperty("jurisdictionState")
    private String jurisdictionState;
    @JsonProperty("parentJurisdictionState")
    private String parentJurisdictionState;
    @JsonProperty("taxId")
    private String taxId;
    @JsonProperty("parentTaxId")
    private String parentTaxId;
    @JsonProperty("sharesParent501c")
    private Boolean sharesParent501c;
    @JsonProperty("capitalizationThreshold")
    @Pattern(regexp = "^-?[0-9]+\\.[0-9]{2}$")
    private String capitalizationThreshold;
    @JsonProperty("reportingQuarter")
    private Integer reportingQuarter;
    @JsonProperty("reportingYear")
    private Integer reportingYear;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Organization() {
    }

    public Organization(String organizationId, String name, String parentOrganization, String baseCurrency, String fiscalYearStart, String fiscalYearEnd, Extensions extensions, String organizationType, String jurisdictionState, String parentJurisdictionState, String taxId, String parentTaxId, Boolean sharesParent501c, String capitalizationThreshold, Integer reportingQuarter, Integer reportingYear) {
        super();
        this.organizationId = organizationId;
        this.name = name;
        this.parentOrganization = parentOrganization;
        this.baseCurrency = baseCurrency;
        this.fiscalYearStart = fiscalYearStart;
        this.fiscalYearEnd = fiscalYearEnd;
        this.extensions = extensions;
        this.organizationType = organizationType;
        this.jurisdictionState = jurisdictionState;
        this.parentJurisdictionState = parentJurisdictionState;
        this.taxId = taxId;
        this.parentTaxId = parentTaxId;
        this.sharesParent501c = sharesParent501c;
        this.capitalizationThreshold = capitalizationThreshold;
        this.reportingQuarter = reportingQuarter;
        this.reportingYear = reportingYear;
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

    @JsonProperty("organizationType")
    public String getOrganizationType() {
        return organizationType;
    }

    @JsonProperty("organizationType")
    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

    @JsonProperty("jurisdictionState")
    public String getJurisdictionState() {
        return jurisdictionState;
    }

    @JsonProperty("jurisdictionState")
    public void setJurisdictionState(String jurisdictionState) {
        this.jurisdictionState = jurisdictionState;
    }

    @JsonProperty("parentJurisdictionState")
    public String getParentJurisdictionState() {
        return parentJurisdictionState;
    }

    @JsonProperty("parentJurisdictionState")
    public void setParentJurisdictionState(String parentJurisdictionState) {
        this.parentJurisdictionState = parentJurisdictionState;
    }

    @JsonProperty("taxId")
    public String getTaxId() {
        return taxId;
    }

    @JsonProperty("taxId")
    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    @JsonProperty("parentTaxId")
    public String getParentTaxId() {
        return parentTaxId;
    }

    @JsonProperty("parentTaxId")
    public void setParentTaxId(String parentTaxId) {
        this.parentTaxId = parentTaxId;
    }

    @JsonProperty("sharesParent501c")
    public Boolean getSharesParent501c() {
        return sharesParent501c;
    }

    @JsonProperty("sharesParent501c")
    public void setSharesParent501c(Boolean sharesParent501c) {
        this.sharesParent501c = sharesParent501c;
    }

    @JsonProperty("capitalizationThreshold")
    public String getCapitalizationThreshold() {
        return capitalizationThreshold;
    }

    @JsonProperty("capitalizationThreshold")
    public void setCapitalizationThreshold(String capitalizationThreshold) {
        this.capitalizationThreshold = capitalizationThreshold;
    }

    @JsonProperty("reportingQuarter")
    public Integer getReportingQuarter() {
        return reportingQuarter;
    }

    @JsonProperty("reportingQuarter")
    public void setReportingQuarter(Integer reportingQuarter) {
        this.reportingQuarter = reportingQuarter;
    }

    @JsonProperty("reportingYear")
    public Integer getReportingYear() {
        return reportingYear;
    }

    @JsonProperty("reportingYear")
    public void setReportingYear(Integer reportingYear) {
        this.reportingYear = reportingYear;
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
        sb.append("organizationType");
        sb.append('=');
        sb.append(((this.organizationType == null)?"<null>":this.organizationType));
        sb.append(',');
        sb.append("jurisdictionState");
        sb.append('=');
        sb.append(((this.jurisdictionState == null)?"<null>":this.jurisdictionState));
        sb.append(',');
        sb.append("parentJurisdictionState");
        sb.append('=');
        sb.append(((this.parentJurisdictionState == null)?"<null>":this.parentJurisdictionState));
        sb.append(',');
        sb.append("taxId");
        sb.append('=');
        sb.append(((this.taxId == null)?"<null>":this.taxId));
        sb.append(',');
        sb.append("parentTaxId");
        sb.append('=');
        sb.append(((this.parentTaxId == null)?"<null>":this.parentTaxId));
        sb.append(',');
        sb.append("sharesParent501c");
        sb.append('=');
        sb.append(((this.sharesParent501c == null)?"<null>":this.sharesParent501c));
        sb.append(',');
        sb.append("capitalizationThreshold");
        sb.append('=');
        sb.append(((this.capitalizationThreshold == null)?"<null>":this.capitalizationThreshold));
        sb.append(',');
        sb.append("reportingQuarter");
        sb.append('=');
        sb.append(((this.reportingQuarter == null)?"<null>":this.reportingQuarter));
        sb.append(',');
        sb.append("reportingYear");
        sb.append('=');
        sb.append(((this.reportingYear == null)?"<null>":this.reportingYear));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
