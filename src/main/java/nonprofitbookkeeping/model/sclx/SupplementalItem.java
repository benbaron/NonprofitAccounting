
package nonprofitbookkeeping.model.sclx;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "supplementalItemId",
    "kind",
    "counterpartyName",
    "personId",
    "year",
    "reason",
    "subtypeCode",
    "eventBudgetLabel",
    "budgetId",
    "sourceLabel",
    "amountAsOf",
    "ledgerRowIndex",
    "workbookLink",
    "extensions"
})
@Generated("jsonschema2pojo")
public class SupplementalItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("supplementalItemId")
    @Size(min = 1)
    @NotNull
    private String supplementalItemId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    @NotNull
    private SupplementalKind kind;
    @JsonProperty("counterpartyName")
    private String counterpartyName;
    @JsonProperty("personId")
    private String personId;
    @JsonProperty("year")
    private Integer year;
    @JsonProperty("reason")
    private String reason;
    @JsonProperty("subtypeCode")
    private String subtypeCode;
    @JsonProperty("eventBudgetLabel")
    private String eventBudgetLabel;
    @JsonProperty("budgetId")
    private String budgetId;
    @JsonProperty("sourceLabel")
    private String sourceLabel;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amountAsOf")
    @Pattern(regexp = "^[0-9]+\\.[0-9]{2}$")
    @NotNull
    private String amountAsOf;
    @JsonProperty("ledgerRowIndex")
    private Integer ledgerRowIndex;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workbookLink")
    @Valid
    @NotNull
    private WorkbookLink workbookLink;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SupplementalItem() {
    }

    public SupplementalItem(String supplementalItemId, SupplementalKind kind, String counterpartyName, String personId, Integer year, String reason, String subtypeCode, String eventBudgetLabel, String budgetId, String sourceLabel, String amountAsOf, Integer ledgerRowIndex, WorkbookLink workbookLink, Extensions extensions) {
        super();
        this.supplementalItemId = supplementalItemId;
        this.kind = kind;
        this.counterpartyName = counterpartyName;
        this.personId = personId;
        this.year = year;
        this.reason = reason;
        this.subtypeCode = subtypeCode;
        this.eventBudgetLabel = eventBudgetLabel;
        this.budgetId = budgetId;
        this.sourceLabel = sourceLabel;
        this.amountAsOf = amountAsOf;
        this.ledgerRowIndex = ledgerRowIndex;
        this.workbookLink = workbookLink;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("supplementalItemId")
    public String getSupplementalItemId() {
        return supplementalItemId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("supplementalItemId")
    public void setSupplementalItemId(String supplementalItemId) {
        this.supplementalItemId = supplementalItemId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    public SupplementalKind getKind() {
        return kind;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    public void setKind(SupplementalKind kind) {
        this.kind = kind;
    }

    @JsonProperty("counterpartyName")
    public String getCounterpartyName() {
        return counterpartyName;
    }

    @JsonProperty("counterpartyName")
    public void setCounterpartyName(String counterpartyName) {
        this.counterpartyName = counterpartyName;
    }

    @JsonProperty("personId")
    public String getPersonId() {
        return personId;
    }

    @JsonProperty("personId")
    public void setPersonId(String personId) {
        this.personId = personId;
    }

    @JsonProperty("year")
    public Integer getYear() {
        return year;
    }

    @JsonProperty("year")
    public void setYear(Integer year) {
        this.year = year;
    }

    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }

    @JsonProperty("reason")
    public void setReason(String reason) {
        this.reason = reason;
    }

    @JsonProperty("subtypeCode")
    public String getSubtypeCode() {
        return subtypeCode;
    }

    @JsonProperty("subtypeCode")
    public void setSubtypeCode(String subtypeCode) {
        this.subtypeCode = subtypeCode;
    }

    @JsonProperty("eventBudgetLabel")
    public String getEventBudgetLabel() {
        return eventBudgetLabel;
    }

    @JsonProperty("eventBudgetLabel")
    public void setEventBudgetLabel(String eventBudgetLabel) {
        this.eventBudgetLabel = eventBudgetLabel;
    }

    @JsonProperty("budgetId")
    public String getBudgetId() {
        return budgetId;
    }

    @JsonProperty("budgetId")
    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    @JsonProperty("sourceLabel")
    public String getSourceLabel() {
        return sourceLabel;
    }

    @JsonProperty("sourceLabel")
    public void setSourceLabel(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amountAsOf")
    public String getAmountAsOf() {
        return amountAsOf;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amountAsOf")
    public void setAmountAsOf(String amountAsOf) {
        this.amountAsOf = amountAsOf;
    }

    @JsonProperty("ledgerRowIndex")
    public Integer getLedgerRowIndex() {
        return ledgerRowIndex;
    }

    @JsonProperty("ledgerRowIndex")
    public void setLedgerRowIndex(Integer ledgerRowIndex) {
        this.ledgerRowIndex = ledgerRowIndex;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workbookLink")
    public WorkbookLink getWorkbookLink() {
        return workbookLink;
    }

    /**
     * 
     * (Required)
     * 
     */
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
        sb.append(SupplementalItem.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("supplementalItemId");
        sb.append('=');
        sb.append(((this.supplementalItemId == null)?"<null>":this.supplementalItemId));
        sb.append(',');
        sb.append("kind");
        sb.append('=');
        sb.append(((this.kind == null)?"<null>":this.kind));
        sb.append(',');
        sb.append("counterpartyName");
        sb.append('=');
        sb.append(((this.counterpartyName == null)?"<null>":this.counterpartyName));
        sb.append(',');
        sb.append("personId");
        sb.append('=');
        sb.append(((this.personId == null)?"<null>":this.personId));
        sb.append(',');
        sb.append("year");
        sb.append('=');
        sb.append(((this.year == null)?"<null>":this.year));
        sb.append(',');
        sb.append("reason");
        sb.append('=');
        sb.append(((this.reason == null)?"<null>":this.reason));
        sb.append(',');
        sb.append("subtypeCode");
        sb.append('=');
        sb.append(((this.subtypeCode == null)?"<null>":this.subtypeCode));
        sb.append(',');
        sb.append("eventBudgetLabel");
        sb.append('=');
        sb.append(((this.eventBudgetLabel == null)?"<null>":this.eventBudgetLabel));
        sb.append(',');
        sb.append("budgetId");
        sb.append('=');
        sb.append(((this.budgetId == null)?"<null>":this.budgetId));
        sb.append(',');
        sb.append("sourceLabel");
        sb.append('=');
        sb.append(((this.sourceLabel == null)?"<null>":this.sourceLabel));
        sb.append(',');
        sb.append("amountAsOf");
        sb.append('=');
        sb.append(((this.amountAsOf == null)?"<null>":this.amountAsOf));
        sb.append(',');
        sb.append("ledgerRowIndex");
        sb.append('=');
        sb.append(((this.ledgerRowIndex == null)?"<null>":this.ledgerRowIndex));
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

}
