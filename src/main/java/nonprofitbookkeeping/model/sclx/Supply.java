
package nonprofitbookkeeping.model.sclx;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "supplyId",
    "itemNumber",
    "dateAcquired",
    "description",
    "count",
    "approxValueTotal",
    "valuePerItem",
    "guardian",
    "guardianshipDetails",
    "removalDetails",
    "additionalNotes",
    "relatedTransactionIds",
    "relatedLineIds",
    "extensions"
})
@Generated("jsonschema2pojo")
public class Supply {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("supplyId")
    private String supplyId;
    @JsonProperty("itemNumber")
    private String itemNumber;
    @JsonProperty("dateAcquired")
    private String dateAcquired;
    @JsonProperty("description")
    private String description;
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("approxValueTotal")
    private String approxValueTotal;
    @JsonProperty("valuePerItem")
    private String valuePerItem;
    @JsonProperty("guardian")
    private Guardian guardian;
    @JsonProperty("guardianshipDetails")
    private GuardianshipDetailsSupply guardianshipDetails;
    @JsonProperty("removalDetails")
    private RemovalDetailsSupply removalDetails;
    @JsonProperty("additionalNotes")
    private String additionalNotes;
    @JsonProperty("relatedTransactionIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> relatedTransactionIds = new LinkedHashSet<String>();
    @JsonProperty("relatedLineIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> relatedLineIds = new LinkedHashSet<String>();
    @JsonProperty("extensions")
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Supply() {
    }

    public Supply(String supplyId, String itemNumber, String dateAcquired, String description, Integer count, String approxValueTotal, String valuePerItem, Guardian guardian, GuardianshipDetailsSupply guardianshipDetails, RemovalDetailsSupply removalDetails, String additionalNotes, Set<String> relatedTransactionIds, Set<String> relatedLineIds, Extensions extensions) {
        super();
        this.supplyId = supplyId;
        this.itemNumber = itemNumber;
        this.dateAcquired = dateAcquired;
        this.description = description;
        this.count = count;
        this.approxValueTotal = approxValueTotal;
        this.valuePerItem = valuePerItem;
        this.guardian = guardian;
        this.guardianshipDetails = guardianshipDetails;
        this.removalDetails = removalDetails;
        this.additionalNotes = additionalNotes;
        this.relatedTransactionIds = relatedTransactionIds;
        this.relatedLineIds = relatedLineIds;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("supplyId")
    public String getSupplyId() {
        return supplyId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("supplyId")
    public void setSupplyId(String supplyId) {
        this.supplyId = supplyId;
    }

    @JsonProperty("itemNumber")
    public String getItemNumber() {
        return itemNumber;
    }

    @JsonProperty("itemNumber")
    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    @JsonProperty("dateAcquired")
    public String getDateAcquired() {
        return dateAcquired;
    }

    @JsonProperty("dateAcquired")
    public void setDateAcquired(String dateAcquired) {
        this.dateAcquired = dateAcquired;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    @JsonProperty("count")
    public void setCount(Integer count) {
        this.count = count;
    }

    @JsonProperty("approxValueTotal")
    public String getApproxValueTotal() {
        return approxValueTotal;
    }

    @JsonProperty("approxValueTotal")
    public void setApproxValueTotal(String approxValueTotal) {
        this.approxValueTotal = approxValueTotal;
    }

    @JsonProperty("valuePerItem")
    public String getValuePerItem() {
        return valuePerItem;
    }

    @JsonProperty("valuePerItem")
    public void setValuePerItem(String valuePerItem) {
        this.valuePerItem = valuePerItem;
    }

    @JsonProperty("guardian")
    public Guardian getGuardian() {
        return guardian;
    }

    @JsonProperty("guardian")
    public void setGuardian(Guardian guardian) {
        this.guardian = guardian;
    }

    @JsonProperty("guardianshipDetails")
    public GuardianshipDetailsSupply getGuardianshipDetails() {
        return guardianshipDetails;
    }

    @JsonProperty("guardianshipDetails")
    public void setGuardianshipDetails(GuardianshipDetailsSupply guardianshipDetails) {
        this.guardianshipDetails = guardianshipDetails;
    }

    @JsonProperty("removalDetails")
    public RemovalDetailsSupply getRemovalDetails() {
        return removalDetails;
    }

    @JsonProperty("removalDetails")
    public void setRemovalDetails(RemovalDetailsSupply removalDetails) {
        this.removalDetails = removalDetails;
    }

    @JsonProperty("additionalNotes")
    public String getAdditionalNotes() {
        return additionalNotes;
    }

    @JsonProperty("additionalNotes")
    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    @JsonProperty("relatedTransactionIds")
    public Set<String> getRelatedTransactionIds() {
        return relatedTransactionIds;
    }

    @JsonProperty("relatedTransactionIds")
    public void setRelatedTransactionIds(Set<String> relatedTransactionIds) {
        this.relatedTransactionIds = relatedTransactionIds;
    }

    @JsonProperty("relatedLineIds")
    public Set<String> getRelatedLineIds() {
        return relatedLineIds;
    }

    @JsonProperty("relatedLineIds")
    public void setRelatedLineIds(Set<String> relatedLineIds) {
        this.relatedLineIds = relatedLineIds;
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
        sb.append(Supply.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("supplyId");
        sb.append('=');
        sb.append(((this.supplyId == null)?"<null>":this.supplyId));
        sb.append(',');
        sb.append("itemNumber");
        sb.append('=');
        sb.append(((this.itemNumber == null)?"<null>":this.itemNumber));
        sb.append(',');
        sb.append("dateAcquired");
        sb.append('=');
        sb.append(((this.dateAcquired == null)?"<null>":this.dateAcquired));
        sb.append(',');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
        sb.append(',');
        sb.append("count");
        sb.append('=');
        sb.append(((this.count == null)?"<null>":this.count));
        sb.append(',');
        sb.append("approxValueTotal");
        sb.append('=');
        sb.append(((this.approxValueTotal == null)?"<null>":this.approxValueTotal));
        sb.append(',');
        sb.append("valuePerItem");
        sb.append('=');
        sb.append(((this.valuePerItem == null)?"<null>":this.valuePerItem));
        sb.append(',');
        sb.append("guardian");
        sb.append('=');
        sb.append(((this.guardian == null)?"<null>":this.guardian));
        sb.append(',');
        sb.append("guardianshipDetails");
        sb.append('=');
        sb.append(((this.guardianshipDetails == null)?"<null>":this.guardianshipDetails));
        sb.append(',');
        sb.append("removalDetails");
        sb.append('=');
        sb.append(((this.removalDetails == null)?"<null>":this.removalDetails));
        sb.append(',');
        sb.append("additionalNotes");
        sb.append('=');
        sb.append(((this.additionalNotes == null)?"<null>":this.additionalNotes));
        sb.append(',');
        sb.append("relatedTransactionIds");
        sb.append('=');
        sb.append(((this.relatedTransactionIds == null)?"<null>":this.relatedTransactionIds));
        sb.append(',');
        sb.append("relatedLineIds");
        sb.append('=');
        sb.append(((this.relatedLineIds == null)?"<null>":this.relatedLineIds));
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
