
package nonprofitbookkeeping.model.sclx.generated;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
    "lineId",
    "accountId",
    "description",
    "debit",
    "credit",
    "fundId",
    "budgetId",
    "eventId",
    "personId",
    "documentId",
    "memo",
    "usedFor",
    "itemNumber",
    "quantity",
    "tags",
    "restrictionTag",
    "reportSection",
    "supplementalRefs",
    "workbookLink",
    "extensions"
})
@Generated("jsonschema2pojo")
public class TransactionLine {

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountId")
    @Size(min = 1)
    @NotNull
    private String accountId;
    @JsonProperty("description")
    private String description;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("debit")
    @Pattern(regexp = "^[0-9]+\\.[0-9]{2}$")
    @NotNull
    private String debit;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("credit")
    @Pattern(regexp = "^[0-9]+\\.[0-9]{2}$")
    @NotNull
    private String credit;
    @JsonProperty("fundId")
    private String fundId;
    @JsonProperty("budgetId")
    private String budgetId;
    @JsonProperty("eventId")
    private String eventId;
    @JsonProperty("personId")
    private String personId;
    @JsonProperty("documentId")
    private String documentId;
    @JsonProperty("memo")
    private String memo;
    @JsonProperty("usedFor")
    private String usedFor;
    @JsonProperty("itemNumber")
    private String itemNumber;
    @JsonProperty("quantity")
    private Integer quantity;
    @JsonProperty("tags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<@Valid String> tags = new LinkedHashSet<String>();
    @JsonProperty("restrictionTag")
    private String restrictionTag;
    @JsonProperty("reportSection")
    private String reportSection;
    @JsonProperty("supplementalRefs")
    private List<@Valid SupplementalRef> supplementalRefs = new ArrayList<SupplementalRef>();
    @JsonProperty("workbookLink")
    @Valid
    private WorkbookLink workbookLink;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public TransactionLine() {
    }

    public TransactionLine(String lineId, String accountId, String description, String debit, String credit, String fundId, String budgetId, String eventId, String personId, String documentId, String memo, String usedFor, String itemNumber, Integer quantity, Set<@Valid String> tags, String restrictionTag, String reportSection, List<@Valid SupplementalRef> supplementalRefs, WorkbookLink workbookLink, Extensions extensions) {
        super();
        this.lineId = lineId;
        this.accountId = accountId;
        this.description = description;
        this.debit = debit;
        this.credit = credit;
        this.fundId = fundId;
        this.budgetId = budgetId;
        this.eventId = eventId;
        this.personId = personId;
        this.documentId = documentId;
        this.memo = memo;
        this.usedFor = usedFor;
        this.itemNumber = itemNumber;
        this.quantity = quantity;
        this.tags = tags;
        this.restrictionTag = restrictionTag;
        this.reportSection = reportSection;
        this.supplementalRefs = supplementalRefs;
        this.workbookLink = workbookLink;
        this.extensions = extensions;
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

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("debit")
    public String getDebit() {
        return debit;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("debit")
    public void setDebit(String debit) {
        this.debit = debit;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("credit")
    public String getCredit() {
        return credit;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("credit")
    public void setCredit(String credit) {
        this.credit = credit;
    }

    @JsonProperty("fundId")
    public String getFundId() {
        return fundId;
    }

    @JsonProperty("fundId")
    public void setFundId(String fundId) {
        this.fundId = fundId;
    }

    @JsonProperty("budgetId")
    public String getBudgetId() {
        return budgetId;
    }

    @JsonProperty("budgetId")
    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @JsonProperty("personId")
    public String getPersonId() {
        return personId;
    }

    @JsonProperty("personId")
    public void setPersonId(String personId) {
        this.personId = personId;
    }

    @JsonProperty("documentId")
    public String getDocumentId() {
        return documentId;
    }

    @JsonProperty("documentId")
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @JsonProperty("memo")
    public String getMemo() {
        return memo;
    }

    @JsonProperty("memo")
    public void setMemo(String memo) {
        this.memo = memo;
    }

    @JsonProperty("usedFor")
    public String getUsedFor() {
        return usedFor;
    }

    @JsonProperty("usedFor")
    public void setUsedFor(String usedFor) {
        this.usedFor = usedFor;
    }

    @JsonProperty("itemNumber")
    public String getItemNumber() {
        return itemNumber;
    }

    @JsonProperty("itemNumber")
    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    @JsonProperty("quantity")
    public Integer getQuantity() {
        return quantity;
    }

    @JsonProperty("quantity")
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @JsonProperty("tags")
    public Set<String> getTags() {
        return tags;
    }

    @JsonProperty("tags")
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @JsonProperty("restrictionTag")
    public String getRestrictionTag() {
        return restrictionTag;
    }

    @JsonProperty("restrictionTag")
    public void setRestrictionTag(String restrictionTag) {
        this.restrictionTag = restrictionTag;
    }

    @JsonProperty("reportSection")
    public String getReportSection() {
        return reportSection;
    }

    @JsonProperty("reportSection")
    public void setReportSection(String reportSection) {
        this.reportSection = reportSection;
    }

    @JsonProperty("supplementalRefs")
    public List<SupplementalRef> getSupplementalRefs() {
        return supplementalRefs;
    }

    @JsonProperty("supplementalRefs")
    public void setSupplementalRefs(List<SupplementalRef> supplementalRefs) {
        this.supplementalRefs = supplementalRefs;
    }

    @JsonProperty("workbookLink")
    public WorkbookLink getWorkbookLink() {
        return workbookLink;
    }

    @JsonProperty("workbookLink")
    public void setWorkbookLink(WorkbookLink workbookLink) {
        this.workbookLink = workbookLink;
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
        sb.append(TransactionLine.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("lineId");
        sb.append('=');
        sb.append(((this.lineId == null)?"<null>":this.lineId));
        sb.append(',');
        sb.append("accountId");
        sb.append('=');
        sb.append(((this.accountId == null)?"<null>":this.accountId));
        sb.append(',');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
        sb.append(',');
        sb.append("debit");
        sb.append('=');
        sb.append(((this.debit == null)?"<null>":this.debit));
        sb.append(',');
        sb.append("credit");
        sb.append('=');
        sb.append(((this.credit == null)?"<null>":this.credit));
        sb.append(',');
        sb.append("fundId");
        sb.append('=');
        sb.append(((this.fundId == null)?"<null>":this.fundId));
        sb.append(',');
        sb.append("budgetId");
        sb.append('=');
        sb.append(((this.budgetId == null)?"<null>":this.budgetId));
        sb.append(',');
        sb.append("eventId");
        sb.append('=');
        sb.append(((this.eventId == null)?"<null>":this.eventId));
        sb.append(',');
        sb.append("personId");
        sb.append('=');
        sb.append(((this.personId == null)?"<null>":this.personId));
        sb.append(',');
        sb.append("documentId");
        sb.append('=');
        sb.append(((this.documentId == null)?"<null>":this.documentId));
        sb.append(',');
        sb.append("memo");
        sb.append('=');
        sb.append(((this.memo == null)?"<null>":this.memo));
        sb.append(',');
        sb.append("usedFor");
        sb.append('=');
        sb.append(((this.usedFor == null)?"<null>":this.usedFor));
        sb.append(',');
        sb.append("itemNumber");
        sb.append('=');
        sb.append(((this.itemNumber == null)?"<null>":this.itemNumber));
        sb.append(',');
        sb.append("quantity");
        sb.append('=');
        sb.append(((this.quantity == null)?"<null>":this.quantity));
        sb.append(',');
        sb.append("tags");
        sb.append('=');
        sb.append(((this.tags == null)?"<null>":this.tags));
        sb.append(',');
        sb.append("restrictionTag");
        sb.append('=');
        sb.append(((this.restrictionTag == null)?"<null>":this.restrictionTag));
        sb.append(',');
        sb.append("reportSection");
        sb.append('=');
        sb.append(((this.reportSection == null)?"<null>":this.reportSection));
        sb.append(',');
        sb.append("supplementalRefs");
        sb.append('=');
        sb.append(((this.supplementalRefs == null)?"<null>":this.supplementalRefs));
        sb.append(',');
        sb.append("workbookLink");
        sb.append('=');
        sb.append(((this.workbookLink == null)?"<null>":this.workbookLink));
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

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.eventId == null)? 0 :this.eventId.hashCode()));
        result = ((result* 31)+((this.itemNumber == null)? 0 :this.itemNumber.hashCode()));
        result = ((result* 31)+((this.quantity == null)? 0 :this.quantity.hashCode()));
        result = ((result* 31)+((this.restrictionTag == null)? 0 :this.restrictionTag.hashCode()));
        result = ((result* 31)+((this.lineId == null)? 0 :this.lineId.hashCode()));
        result = ((result* 31)+((this.description == null)? 0 :this.description.hashCode()));
        result = ((result* 31)+((this.memo == null)? 0 :this.memo.hashCode()));
        result = ((result* 31)+((this.supplementalRefs == null)? 0 :this.supplementalRefs.hashCode()));
        result = ((result* 31)+((this.budgetId == null)? 0 :this.budgetId.hashCode()));
        result = ((result* 31)+((this.reportSection == null)? 0 :this.reportSection.hashCode()));
        result = ((result* 31)+((this.tags == null)? 0 :this.tags.hashCode()));
        result = ((result* 31)+((this.workbookLink == null)? 0 :this.workbookLink.hashCode()));
        result = ((result* 31)+((this.accountId == null)? 0 :this.accountId.hashCode()));
        result = ((result* 31)+((this.fundId == null)? 0 :this.fundId.hashCode()));
        result = ((result* 31)+((this.extensions == null)? 0 :this.extensions.hashCode()));
        result = ((result* 31)+((this.usedFor == null)? 0 :this.usedFor.hashCode()));
        result = ((result* 31)+((this.personId == null)? 0 :this.personId.hashCode()));
        result = ((result* 31)+((this.documentId == null)? 0 :this.documentId.hashCode()));
        result = ((result* 31)+((this.debit == null)? 0 :this.debit.hashCode()));
        result = ((result* 31)+((this.credit == null)? 0 :this.credit.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TransactionLine) == false) {
            return false;
        }
        TransactionLine rhs = ((TransactionLine) other);
        return (((((((((((((((((((((this.eventId == rhs.eventId)||((this.eventId!= null)&&this.eventId.equals(rhs.eventId)))&&((this.itemNumber == rhs.itemNumber)||((this.itemNumber!= null)&&this.itemNumber.equals(rhs.itemNumber))))&&((this.quantity == rhs.quantity)||((this.quantity!= null)&&this.quantity.equals(rhs.quantity))))&&((this.restrictionTag == rhs.restrictionTag)||((this.restrictionTag!= null)&&this.restrictionTag.equals(rhs.restrictionTag))))&&((this.lineId == rhs.lineId)||((this.lineId!= null)&&this.lineId.equals(rhs.lineId))))&&((this.description == rhs.description)||((this.description!= null)&&this.description.equals(rhs.description))))&&((this.memo == rhs.memo)||((this.memo!= null)&&this.memo.equals(rhs.memo))))&&((this.supplementalRefs == rhs.supplementalRefs)||((this.supplementalRefs!= null)&&this.supplementalRefs.equals(rhs.supplementalRefs))))&&((this.budgetId == rhs.budgetId)||((this.budgetId!= null)&&this.budgetId.equals(rhs.budgetId))))&&((this.reportSection == rhs.reportSection)||((this.reportSection!= null)&&this.reportSection.equals(rhs.reportSection))))&&((this.tags == rhs.tags)||((this.tags!= null)&&this.tags.equals(rhs.tags))))&&((this.workbookLink == rhs.workbookLink)||((this.workbookLink!= null)&&this.workbookLink.equals(rhs.workbookLink))))&&((this.accountId == rhs.accountId)||((this.accountId!= null)&&this.accountId.equals(rhs.accountId))))&&((this.fundId == rhs.fundId)||((this.fundId!= null)&&this.fundId.equals(rhs.fundId))))&&((this.extensions == rhs.extensions)||((this.extensions!= null)&&this.extensions.equals(rhs.extensions))))&&((this.usedFor == rhs.usedFor)||((this.usedFor!= null)&&this.usedFor.equals(rhs.usedFor))))&&((this.personId == rhs.personId)||((this.personId!= null)&&this.personId.equals(rhs.personId))))&&((this.documentId == rhs.documentId)||((this.documentId!= null)&&this.documentId.equals(rhs.documentId))))&&((this.debit == rhs.debit)||((this.debit!= null)&&this.debit.equals(rhs.debit))))&&((this.credit == rhs.credit)||((this.credit!= null)&&this.credit.equals(rhs.credit))));
    }

}
