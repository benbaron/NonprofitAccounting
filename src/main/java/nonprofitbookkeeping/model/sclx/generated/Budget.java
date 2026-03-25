
package nonprofitbookkeeping.model.sclx.generated;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "budgetId",
    "name",
    "fiscalYear",
    "fundId",
    "active",
    "description",
    "lines",
    "extensions"
})
@Generated("jsonschema2pojo")
public class Budget {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("budgetId")
    @Size(min = 1)
    @NotNull
    private String budgetId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    @Size(min = 1)
    @NotNull
    private String name;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fiscalYear")
    @NotNull
    private Integer fiscalYear;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fundId")
    @Size(min = 1)
    @NotNull
    private String fundId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("active")
    @NotNull
    private Boolean active;
    @JsonProperty("description")
    private String description;
    @JsonProperty("lines")
    private List<@Valid BudgetLine> lines = new ArrayList<BudgetLine>();
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Budget() {
    }

    public Budget(String budgetId, String name, Integer fiscalYear, String fundId, Boolean active, String description, List<@Valid BudgetLine> lines, Extensions extensions) {
        super();
        this.budgetId = budgetId;
        this.name = name;
        this.fiscalYear = fiscalYear;
        this.fundId = fundId;
        this.active = active;
        this.description = description;
        this.lines = lines;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("budgetId")
    public String getBudgetId() {
        return budgetId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("budgetId")
    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fiscalYear")
    public Integer getFiscalYear() {
        return fiscalYear;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fiscalYear")
    public void setFiscalYear(Integer fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fundId")
    public String getFundId() {
        return fundId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fundId")
    public void setFundId(String fundId) {
        this.fundId = fundId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("active")
    public Boolean getActive() {
        return active;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("active")
    public void setActive(Boolean active) {
        this.active = active;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("lines")
    public List<BudgetLine> getLines() {
        return lines;
    }

    @JsonProperty("lines")
    public void setLines(List<BudgetLine> lines) {
        this.lines = lines;
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
        sb.append(Budget.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("budgetId");
        sb.append('=');
        sb.append(((this.budgetId == null)?"<null>":this.budgetId));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("fiscalYear");
        sb.append('=');
        sb.append(((this.fiscalYear == null)?"<null>":this.fiscalYear));
        sb.append(',');
        sb.append("fundId");
        sb.append('=');
        sb.append(((this.fundId == null)?"<null>":this.fundId));
        sb.append(',');
        sb.append("active");
        sb.append('=');
        sb.append(((this.active == null)?"<null>":this.active));
        sb.append(',');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
        sb.append(',');
        sb.append("lines");
        sb.append('=');
        sb.append(((this.lines == null)?"<null>":this.lines));
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
        result = ((result* 31)+((this.fundId == null)? 0 :this.fundId.hashCode()));
        result = ((result* 31)+((this.extensions == null)? 0 :this.extensions.hashCode()));
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.active == null)? 0 :this.active.hashCode()));
        result = ((result* 31)+((this.description == null)? 0 :this.description.hashCode()));
        result = ((result* 31)+((this.budgetId == null)? 0 :this.budgetId.hashCode()));
        result = ((result* 31)+((this.lines == null)? 0 :this.lines.hashCode()));
        result = ((result* 31)+((this.fiscalYear == null)? 0 :this.fiscalYear.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Budget) == false) {
            return false;
        }
        Budget rhs = ((Budget) other);
        return (((((((((this.fundId == rhs.fundId)||((this.fundId!= null)&&this.fundId.equals(rhs.fundId)))&&((this.extensions == rhs.extensions)||((this.extensions!= null)&&this.extensions.equals(rhs.extensions))))&&((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name))))&&((this.active == rhs.active)||((this.active!= null)&&this.active.equals(rhs.active))))&&((this.description == rhs.description)||((this.description!= null)&&this.description.equals(rhs.description))))&&((this.budgetId == rhs.budgetId)||((this.budgetId!= null)&&this.budgetId.equals(rhs.budgetId))))&&((this.lines == rhs.lines)||((this.lines!= null)&&this.lines.equals(rhs.lines))))&&((this.fiscalYear == rhs.fiscalYear)||((this.fiscalYear!= null)&&this.fiscalYear.equals(rhs.fiscalYear))));
    }

}
