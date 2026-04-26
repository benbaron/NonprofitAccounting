
package nonprofitbookkeeping.model.sclx;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "bankAccountId",
    "accountName",
    "institutionName",
    "institutionEmail",
    "institutionPhone",
    "accountNumberMasked",
    "accountType",
    "currency",
    "interestBearing",
    "signatureRequirement",
    "accountHolderName",
    "chartAccountId",
    "authorizedSigners",
    "extensions"
})
@Generated("jsonschema2pojo")
public class BankAccount {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bankAccountId")
    @Size(min = 1)
    @NotNull
    private String bankAccountId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountName")
    @Size(min = 1)
    @NotNull
    private String accountName;
    @JsonProperty("institutionName")
    private String institutionName;
    @Email
    @JsonProperty("institutionEmail")
    private String institutionEmail;
    @JsonProperty("institutionPhone")
    private String institutionPhone;
    @JsonProperty("accountNumberMasked")
    private String accountNumberMasked;
    @JsonProperty("accountType")
    private String accountType;
    @JsonProperty("currency")
    @Pattern(regexp = "^[A-Z]{3}$")
    private String currency;
    @JsonProperty("interestBearing")
    private Boolean interestBearing;
    @JsonProperty("signatureRequirement")
    private String signatureRequirement;
    @JsonProperty("accountHolderName")
    private String accountHolderName;
    @JsonProperty("chartAccountId")
    private String chartAccountId;
    @JsonProperty("authorizedSigners")
    private List<@Valid BankAccountSigner> authorizedSigners = new ArrayList<BankAccountSigner>();
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public BankAccount() {
    }

    public BankAccount(String bankAccountId, String accountName, String institutionName, String institutionEmail, String institutionPhone, String accountNumberMasked, String accountType, String currency, Boolean interestBearing, String signatureRequirement, String accountHolderName, String chartAccountId, List<@Valid BankAccountSigner> authorizedSigners, Extensions extensions) {
        super();
        this.bankAccountId = bankAccountId;
        this.accountName = accountName;
        this.institutionName = institutionName;
        this.institutionEmail = institutionEmail;
        this.institutionPhone = institutionPhone;
        this.accountNumberMasked = accountNumberMasked;
        this.accountType = accountType;
        this.currency = currency;
        this.interestBearing = interestBearing;
        this.signatureRequirement = signatureRequirement;
        this.accountHolderName = accountHolderName;
        this.chartAccountId = chartAccountId;
        this.authorizedSigners = authorizedSigners;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bankAccountId")
    public String getBankAccountId() {
        return this.bankAccountId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bankAccountId")
    public void setBankAccountId(String bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountName")
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountName")
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @JsonProperty("institutionName")
    public String getInstitutionName() {
        return this.institutionName;
    }

    @JsonProperty("institutionName")
    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    @JsonProperty("institutionEmail")
    public String getInstitutionEmail() {
        return this.institutionEmail;
    }

    @JsonProperty("institutionEmail")
    public void setInstitutionEmail(String institutionEmail) {
        this.institutionEmail = institutionEmail;
    }

    @JsonProperty("institutionPhone")
    public String getInstitutionPhone() {
        return this.institutionPhone;
    }

    @JsonProperty("institutionPhone")
    public void setInstitutionPhone(String institutionPhone) {
        this.institutionPhone = institutionPhone;
    }

    @JsonProperty("accountNumberMasked")
    public String getAccountNumberMasked() {
        return this.accountNumberMasked;
    }

    @JsonProperty("accountNumberMasked")
    public void setAccountNumberMasked(String accountNumberMasked) {
        this.accountNumberMasked = accountNumberMasked;
    }

    @JsonProperty("accountType")
    public String getAccountType() {
        return this.accountType;
    }

    @JsonProperty("accountType")
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return this.currency;
    }

    @JsonProperty("currency")
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonProperty("interestBearing")
    public Boolean getInterestBearing() {
        return this.interestBearing;
    }

    @JsonProperty("interestBearing")
    public void setInterestBearing(Boolean interestBearing) {
        this.interestBearing = interestBearing;
    }

    @JsonProperty("signatureRequirement")
    public String getSignatureRequirement() {
        return this.signatureRequirement;
    }

    @JsonProperty("signatureRequirement")
    public void setSignatureRequirement(String signatureRequirement) {
        this.signatureRequirement = signatureRequirement;
    }

    @JsonProperty("accountHolderName")
    public String getAccountHolderName() {
        return this.accountHolderName;
    }

    @JsonProperty("accountHolderName")
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    @JsonProperty("chartAccountId")
    public String getChartAccountId() {
        return this.chartAccountId;
    }

    @JsonProperty("chartAccountId")
    public void setChartAccountId(String chartAccountId) {
        this.chartAccountId = chartAccountId;
    }

    @JsonProperty("authorizedSigners")
    public List<BankAccountSigner> getAuthorizedSigners() {
        return this.authorizedSigners;
    }

    @JsonProperty("authorizedSigners")
    public void setAuthorizedSigners(List<BankAccountSigner> authorizedSigners) {
        this.authorizedSigners = authorizedSigners;
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
        sb.append(BankAccount.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("bankAccountId");
        sb.append('=');
        sb.append(((this.bankAccountId == null)?"<null>":this.bankAccountId));
        sb.append(',');
        sb.append("accountName");
        sb.append('=');
        sb.append(((this.accountName == null)?"<null>":this.accountName));
        sb.append(',');
        sb.append("institutionName");
        sb.append('=');
        sb.append(((this.institutionName == null)?"<null>":this.institutionName));
        sb.append(',');
        sb.append("institutionEmail");
        sb.append('=');
        sb.append(((this.institutionEmail == null)?"<null>":this.institutionEmail));
        sb.append(',');
        sb.append("institutionPhone");
        sb.append('=');
        sb.append(((this.institutionPhone == null)?"<null>":this.institutionPhone));
        sb.append(',');
        sb.append("accountNumberMasked");
        sb.append('=');
        sb.append(((this.accountNumberMasked == null)?"<null>":this.accountNumberMasked));
        sb.append(',');
        sb.append("accountType");
        sb.append('=');
        sb.append(((this.accountType == null)?"<null>":this.accountType));
        sb.append(',');
        sb.append("currency");
        sb.append('=');
        sb.append(((this.currency == null)?"<null>":this.currency));
        sb.append(',');
        sb.append("interestBearing");
        sb.append('=');
        sb.append(((this.interestBearing == null)?"<null>":this.interestBearing));
        sb.append(',');
        sb.append("signatureRequirement");
        sb.append('=');
        sb.append(((this.signatureRequirement == null)?"<null>":this.signatureRequirement));
        sb.append(',');
        sb.append("accountHolderName");
        sb.append('=');
        sb.append(((this.accountHolderName == null)?"<null>":this.accountHolderName));
        sb.append(',');
        sb.append("chartAccountId");
        sb.append('=');
        sb.append(((this.chartAccountId == null)?"<null>":this.chartAccountId));
        sb.append(',');
        sb.append("authorizedSigners");
        sb.append('=');
        sb.append(((this.authorizedSigners == null)?"<null>":this.authorizedSigners));
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
