
package nonprofitbookkeeping.model;

import java.util.Date;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "amount",
    "asOf"
})
@Generated("jsonschema2pojo")
public class BalanceSnapshot {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amount")
    @Pattern(regexp = "^-?[0-9]+\\.[0-9]{2}$")
    @NotNull
    private String amount;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("asOf")
    @NotNull
    private Date asOf;

    /**
     * No args constructor for use in serialization
     * 
     */
    public BalanceSnapshot() {
    }

    public BalanceSnapshot(String amount, Date asOf) {
        super();
        this.amount = amount;
        this.asOf = asOf;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amount")
    public String getAmount() {
        return amount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amount")
    public void setAmount(String amount) {
        this.amount = amount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("asOf")
    public Date getAsOf() {
        return asOf;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("asOf")
    public void setAsOf(Date asOf) {
        this.asOf = asOf;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(BalanceSnapshot.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("amount");
        sb.append('=');
        sb.append(((this.amount == null)?"<null>":this.amount));
        sb.append(',');
        sb.append("asOf");
        sb.append('=');
        sb.append(((this.asOf == null)?"<null>":this.asOf));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
