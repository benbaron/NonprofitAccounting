
package nonprofitbookkeeping.model.sclx.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "bankId",
    "accountId",
    "accountType"
})
@Generated("jsonschema2pojo")
public class BankAccount {

    @JsonProperty("bankId")
    private String bankId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountId")
    @NotNull
    private String accountId;
    @JsonProperty("accountType")
    private String accountType;

    /**
     * No args constructor for use in serialization
     * 
     */
    public BankAccount() {
    }

    public BankAccount(String bankId, String accountId, String accountType) {
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
        sb.append(BankAccount.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.accountId == null)? 0 :this.accountId.hashCode()));
        result = ((result* 31)+((this.bankId == null)? 0 :this.bankId.hashCode()));
        result = ((result* 31)+((this.accountType == null)? 0 :this.accountType.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BankAccount) == false) {
            return false;
        }
        BankAccount rhs = ((BankAccount) other);
        return ((((this.accountId == rhs.accountId)||((this.accountId!= null)&&this.accountId.equals(rhs.accountId)))&&((this.bankId == rhs.bankId)||((this.bankId!= null)&&this.bankId.equals(rhs.bankId))))&&((this.accountType == rhs.accountType)||((this.accountType!= null)&&this.accountType.equals(rhs.accountType))));
    }

}
