
package nonprofitbookkeeping.model.sclx;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "assetId",
    "dateAcquired",
    "description",
    "itemCount",
    "approxValueTotal",
    "valuePerItem",
    "itemType",
    "usedFor",
    "lotPaidTotal",
    "lotItemCount",
    "currentGuardian",
    "guardianshipDetails",
    "removalDetails",
    "relatedTransactionIds",
    "relatedLineIds",
    "extensions",
    "appraisalDetails"
})
@Generated("jsonschema2pojo")
public class Asset {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("assetId")
    @Size(min = 1)
    @NotNull
    private String assetId;
    @JsonProperty("dateAcquired")
    private String dateAcquired;
    @JsonProperty("description")
    private String description;
    @JsonProperty("itemCount")
    private Integer itemCount;
    @JsonProperty("approxValueTotal")
    @Pattern(regexp = "^-?[0-9]+\\.[0-9]{2}$")
    private String approxValueTotal;
    @JsonProperty("valuePerItem")
    @Pattern(regexp = "^-?[0-9]+\\.[0-9]{2}$")
    private String valuePerItem;
    @JsonProperty("itemType")
    private String itemType;
    @JsonProperty("usedFor")
    private String usedFor;
    @JsonProperty("lotPaidTotal")
    @Pattern(regexp = "^-?[0-9]+\\.[0-9]{2}$")
    private String lotPaidTotal;
    @JsonProperty("lotItemCount")
    private Integer lotItemCount;
    @JsonProperty("currentGuardian")
    @Valid
    private Guardian currentGuardian;
    @JsonProperty("guardianshipDetails")
    @Valid
    private GuardianshipDetailsAsset guardianshipDetails;
    @JsonProperty("removalDetails")
    @Valid
    private RemovalDetailsAsset removalDetails;
    @JsonProperty("relatedTransactionIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<@Valid String> relatedTransactionIds = new LinkedHashSet<String>();
    @JsonProperty("relatedLineIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<@Valid String> relatedLineIds = new LinkedHashSet<String>();
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;
    @JsonProperty("appraisalDetails")
    @Valid
    private AppraisalDetails appraisalDetails;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Asset() {
    }

    public Asset(String assetId, String dateAcquired, String description, Integer itemCount, String approxValueTotal, String valuePerItem, String itemType, String usedFor, String lotPaidTotal, Integer lotItemCount, Guardian currentGuardian, GuardianshipDetailsAsset guardianshipDetails, RemovalDetailsAsset removalDetails, Set<@Valid String> relatedTransactionIds, Set<@Valid String> relatedLineIds, Extensions extensions, AppraisalDetails appraisalDetails) {
        super();
        this.assetId = assetId;
        this.dateAcquired = dateAcquired;
        this.description = description;
        this.itemCount = itemCount;
        this.approxValueTotal = approxValueTotal;
        this.valuePerItem = valuePerItem;
        this.itemType = itemType;
        this.usedFor = usedFor;
        this.lotPaidTotal = lotPaidTotal;
        this.lotItemCount = lotItemCount;
        this.currentGuardian = currentGuardian;
        this.guardianshipDetails = guardianshipDetails;
        this.removalDetails = removalDetails;
        this.relatedTransactionIds = relatedTransactionIds;
        this.relatedLineIds = relatedLineIds;
        this.extensions = extensions;
        this.appraisalDetails = appraisalDetails;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("assetId")
    public String getAssetId() {
        return this.assetId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("assetId")
    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    @JsonProperty("dateAcquired")
    public String getDateAcquired() {
        return this.dateAcquired;
    }

    @JsonProperty("dateAcquired")
    public void setDateAcquired(String dateAcquired) {
        this.dateAcquired = dateAcquired;
    }

    @JsonProperty("description")
    public String getDescription() {
        return this.description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("itemCount")
    public Integer getItemCount() {
        return this.itemCount;
    }

    @JsonProperty("itemCount")
    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    @JsonProperty("approxValueTotal")
    public String getApproxValueTotal() {
        return this.approxValueTotal;
    }

    @JsonProperty("approxValueTotal")
    public void setApproxValueTotal(String approxValueTotal) {
        this.approxValueTotal = approxValueTotal;
    }

    @JsonProperty("valuePerItem")
    public String getValuePerItem() {
        return this.valuePerItem;
    }

    @JsonProperty("valuePerItem")
    public void setValuePerItem(String valuePerItem) {
        this.valuePerItem = valuePerItem;
    }

    @JsonProperty("itemType")
    public String getItemType() {
        return this.itemType;
    }

    @JsonProperty("itemType")
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    @JsonProperty("usedFor")
    public String getUsedFor() {
        return this.usedFor;
    }

    @JsonProperty("usedFor")
    public void setUsedFor(String usedFor) {
        this.usedFor = usedFor;
    }

    @JsonProperty("lotPaidTotal")
    public String getLotPaidTotal() {
        return this.lotPaidTotal;
    }

    @JsonProperty("lotPaidTotal")
    public void setLotPaidTotal(String lotPaidTotal) {
        this.lotPaidTotal = lotPaidTotal;
    }

    @JsonProperty("lotItemCount")
    public Integer getLotItemCount() {
        return this.lotItemCount;
    }

    @JsonProperty("lotItemCount")
    public void setLotItemCount(Integer lotItemCount) {
        this.lotItemCount = lotItemCount;
    }

    @JsonProperty("currentGuardian")
    public Guardian getCurrentGuardian() {
        return this.currentGuardian;
    }

    @JsonProperty("currentGuardian")
    public void setCurrentGuardian(Guardian currentGuardian) {
        this.currentGuardian = currentGuardian;
    }

    @JsonProperty("guardianshipDetails")
    public GuardianshipDetailsAsset getGuardianshipDetails() {
        return this.guardianshipDetails;
    }

    @JsonProperty("guardianshipDetails")
    public void setGuardianshipDetails(GuardianshipDetailsAsset guardianshipDetails) {
        this.guardianshipDetails = guardianshipDetails;
    }

    @JsonProperty("removalDetails")
    public RemovalDetailsAsset getRemovalDetails() {
        return this.removalDetails;
    }

    @JsonProperty("removalDetails")
    public void setRemovalDetails(RemovalDetailsAsset removalDetails) {
        this.removalDetails = removalDetails;
    }

    @JsonProperty("relatedTransactionIds")
    public Set<String> getRelatedTransactionIds() {
        return this.relatedTransactionIds;
    }

    @JsonProperty("relatedTransactionIds")
    public void setRelatedTransactionIds(Set<String> relatedTransactionIds) {
        this.relatedTransactionIds = relatedTransactionIds;
    }

    @JsonProperty("relatedLineIds")
    public Set<String> getRelatedLineIds() {
        return this.relatedLineIds;
    }

    @JsonProperty("relatedLineIds")
    public void setRelatedLineIds(Set<String> relatedLineIds) {
        this.relatedLineIds = relatedLineIds;
    }

    @JsonProperty("extensions")
    public Extensions getExtensions() {
        return this.extensions;
    }

    @JsonProperty("extensions")
    public void setExtensions(Extensions extensions) {
        this.extensions = extensions;
    }

    @JsonProperty("appraisalDetails")
    public AppraisalDetails getAppraisalDetails() {
        return this.appraisalDetails;
    }

    @JsonProperty("appraisalDetails")
    public void setAppraisalDetails(AppraisalDetails appraisalDetails) {
        this.appraisalDetails = appraisalDetails;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Asset.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("assetId");
        sb.append('=');
        sb.append(((this.assetId == null)?"<null>":this.assetId));
        sb.append(',');
        sb.append("dateAcquired");
        sb.append('=');
        sb.append(((this.dateAcquired == null)?"<null>":this.dateAcquired));
        sb.append(',');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
        sb.append(',');
        sb.append("itemCount");
        sb.append('=');
        sb.append(((this.itemCount == null)?"<null>":this.itemCount));
        sb.append(',');
        sb.append("approxValueTotal");
        sb.append('=');
        sb.append(((this.approxValueTotal == null)?"<null>":this.approxValueTotal));
        sb.append(',');
        sb.append("valuePerItem");
        sb.append('=');
        sb.append(((this.valuePerItem == null)?"<null>":this.valuePerItem));
        sb.append(',');
        sb.append("itemType");
        sb.append('=');
        sb.append(((this.itemType == null)?"<null>":this.itemType));
        sb.append(',');
        sb.append("usedFor");
        sb.append('=');
        sb.append(((this.usedFor == null)?"<null>":this.usedFor));
        sb.append(',');
        sb.append("lotPaidTotal");
        sb.append('=');
        sb.append(((this.lotPaidTotal == null)?"<null>":this.lotPaidTotal));
        sb.append(',');
        sb.append("lotItemCount");
        sb.append('=');
        sb.append(((this.lotItemCount == null)?"<null>":this.lotItemCount));
        sb.append(',');
        sb.append("currentGuardian");
        sb.append('=');
        sb.append(((this.currentGuardian == null)?"<null>":this.currentGuardian));
        sb.append(',');
        sb.append("guardianshipDetails");
        sb.append('=');
        sb.append(((this.guardianshipDetails == null)?"<null>":this.guardianshipDetails));
        sb.append(',');
        sb.append("removalDetails");
        sb.append('=');
        sb.append(((this.removalDetails == null)?"<null>":this.removalDetails));
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
        sb.append("appraisalDetails");
        sb.append('=');
        sb.append(((this.appraisalDetails == null)?"<null>":this.appraisalDetails));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
