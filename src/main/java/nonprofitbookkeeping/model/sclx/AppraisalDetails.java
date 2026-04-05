
package nonprofitbookkeeping.model.sclx;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "appraiserName",
    "appraisalDate",
    "revisedValue",
    "extensions"
})
@Generated("jsonschema2pojo")
public class AppraisalDetails {

    @JsonProperty("appraiserName")
    private String appraiserName;
    @JsonProperty("appraisalDate")
    private String appraisalDate;
    @JsonProperty("revisedValue")
    @Pattern(regexp = "^-?[0-9]+\\.[0-9]{2}$")
    private String revisedValue;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AppraisalDetails() {
    }

    public AppraisalDetails(String appraiserName, String appraisalDate, String revisedValue, Extensions extensions) {
        super();
        this.appraiserName = appraiserName;
        this.appraisalDate = appraisalDate;
        this.revisedValue = revisedValue;
        this.extensions = extensions;
    }

    @JsonProperty("appraiserName")
    public String getAppraiserName() {
        return appraiserName;
    }

    @JsonProperty("appraiserName")
    public void setAppraiserName(String appraiserName) {
        this.appraiserName = appraiserName;
    }

    @JsonProperty("appraisalDate")
    public String getAppraisalDate() {
        return appraisalDate;
    }

    @JsonProperty("appraisalDate")
    public void setAppraisalDate(String appraisalDate) {
        this.appraisalDate = appraisalDate;
    }

    @JsonProperty("revisedValue")
    public String getRevisedValue() {
        return revisedValue;
    }

    @JsonProperty("revisedValue")
    public void setRevisedValue(String revisedValue) {
        this.revisedValue = revisedValue;
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
        sb.append(AppraisalDetails.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("appraiserName");
        sb.append('=');
        sb.append(((this.appraiserName == null)?"<null>":this.appraiserName));
        sb.append(',');
        sb.append("appraisalDate");
        sb.append('=');
        sb.append(((this.appraisalDate == null)?"<null>":this.appraisalDate));
        sb.append(',');
        sb.append("revisedValue");
        sb.append('=');
        sb.append(((this.revisedValue == null)?"<null>":this.revisedValue));
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
