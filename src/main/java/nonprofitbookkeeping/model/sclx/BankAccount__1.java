
package nonprofitbookkeeping.model.sclx;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "bankId",
    "accountId",
    "accountType"
})
@Generated("jsonschema2pojo")
public class BankAccount__1 {

    @JsonProperty("bankId")
    private String bankId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountId")
    private String accountId;
    @JsonProperty("accountType")
    private String accountType;

    /**
     * No args constructor for use in serialization
     * 
     */
    public BankAccount__1() {
    }

    public BankAccount__1(String bankId, String accountId, String accountType) {
        super();
        this.bankId = bankId;
        this.accountId = accountId;
        this.accountType = accountType;
    }

    @JsonProperty("bankId")
    public String getBankId() {
        return bankId;
    }

    @JsonProperty("bankId")
    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountId")
    public String getAccountId() {
        return accountId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountId")
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @JsonProperty("accountType")
    public String getAccountType() {
        return accountType;
    }

    @JsonProperty("accountType")
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(BankAccount__1 .class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("bankId");
        sb.append('=');
        sb.append(((this.bankId == null)?"<null>":this.bankId));
        sb.append(',');
        sb.append("accountId");
        sb.append('=');
        sb.append(((this.accountId == null)?"<null>":this.accountId));
        sb.append(',');
        sb.append("accountType");
        sb.append('=');
        sb.append(((this.accountType == null)?"<null>":this.accountType));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
