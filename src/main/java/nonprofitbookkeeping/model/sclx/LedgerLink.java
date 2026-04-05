
package nonprofitbookkeeping.model.sclx;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "transactionId",
    "lineId"
})
@Generated("jsonschema2pojo")
public class LedgerLink {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactionId")
    @Size(min = 1)
    @NotNull
    private String transactionId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lineId")
    @Size(min = 1)
    @NotNull
    private String lineId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public LedgerLink() {
    }

    public LedgerLink(String transactionId, String lineId) {
        super();
        this.transactionId = transactionId;
        this.lineId = lineId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactionId")
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lineId")
    public String getLineId() {
        return lineId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lineId")
    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(LedgerLink.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("transactionId");
        sb.append('=');
        sb.append(((this.transactionId == null)?"<null>":this.transactionId));
        sb.append(',');
        sb.append("lineId");
        sb.append('=');
        sb.append(((this.lineId == null)?"<null>":this.lineId));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
