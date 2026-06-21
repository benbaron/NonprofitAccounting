
package nonprofitbookkeeping.model.sclx;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "sheetKey",
    "ledgerRowIndex"
})
@Generated("jsonschema2pojo")
public class WorkbookLink {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sheetKey")
    @Size(min = 1)
    @NotNull
    private String sheetKey;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ledgerRowIndex")
    @DecimalMin("1")
    @NotNull
    private Integer ledgerRowIndex;

    /**
     * No args constructor for use in serialization
     * 
     */
    public WorkbookLink() {
    }

    public WorkbookLink(String sheetKey, Integer ledgerRowIndex) {
        super();
        this.sheetKey = sheetKey;
        this.ledgerRowIndex = ledgerRowIndex;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sheetKey")
    public String getSheetKey() {
        return this.sheetKey;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sheetKey")
    public void setSheetKey(String sheetKey) {
        this.sheetKey = sheetKey;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ledgerRowIndex")
    public Integer getLedgerRowIndex() {
        return this.ledgerRowIndex;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ledgerRowIndex")
    public void setLedgerRowIndex(Integer ledgerRowIndex) {
        this.ledgerRowIndex = ledgerRowIndex;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(WorkbookLink.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("sheetKey");
        sb.append('=');
        sb.append(((this.sheetKey == null)?"<null>":this.sheetKey));
        sb.append(',');
        sb.append("ledgerRowIndex");
        sb.append('=');
        sb.append(((this.ledgerRowIndex == null)?"<null>":this.ledgerRowIndex));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
