
package nonprofitbookkeeping.model.sclx;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "eventName",
    "budgetedAmount",
    "revenueCategory",
    "expenseCategory",
    "accountId",
    "notes",
    "extensions"
})
@Generated("jsonschema2pojo")
public class BudgetLine {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("eventName")
    @Size(min = 1)
    @NotNull
    private String eventName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("budgetedAmount")
    @Pattern(regexp = "^-?[0-9]+\\.[0-9]{2}$")
    @NotNull
    private String budgetedAmount;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("revenueCategory")
    @NotNull
    private BudgetLine.BudgetRevenueCategory revenueCategory;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("expenseCategory")
    @NotNull
    private BudgetLine.BudgetExpenseCategory expenseCategory;
    @JsonProperty("accountId")
    private String accountId;
    @JsonProperty("notes")
    private String notes;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public BudgetLine() {
    }

    public BudgetLine(String eventName, String budgetedAmount, BudgetLine.BudgetRevenueCategory revenueCategory, BudgetLine.BudgetExpenseCategory expenseCategory, String accountId, String notes, Extensions extensions) {
        super();
        this.eventName = eventName;
        this.budgetedAmount = budgetedAmount;
        this.revenueCategory = revenueCategory;
        this.expenseCategory = expenseCategory;
        this.accountId = accountId;
        this.notes = notes;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("eventName")
    public String getEventName() {
        return eventName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("eventName")
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("budgetedAmount")
    public String getBudgetedAmount() {
        return budgetedAmount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("budgetedAmount")
    public void setBudgetedAmount(String budgetedAmount) {
        this.budgetedAmount = budgetedAmount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("revenueCategory")
    public BudgetLine.BudgetRevenueCategory getRevenueCategory() {
        return revenueCategory;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("revenueCategory")
    public void setRevenueCategory(BudgetLine.BudgetRevenueCategory revenueCategory) {
        this.revenueCategory = revenueCategory;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("expenseCategory")
    public BudgetLine.BudgetExpenseCategory getExpenseCategory() {
        return expenseCategory;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("expenseCategory")
    public void setExpenseCategory(BudgetLine.BudgetExpenseCategory expenseCategory) {
        this.expenseCategory = expenseCategory;
    }

    @JsonProperty("accountId")
    public String getAccountId() {
        return accountId;
    }

    @JsonProperty("accountId")
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @JsonProperty("notes")
    public String getNotes() {
        return notes;
    }

    @JsonProperty("notes")
    public void setNotes(String notes) {
        this.notes = notes;
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
        sb.append(BudgetLine.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("eventName");
        sb.append('=');
        sb.append(((this.eventName == null)?"<null>":this.eventName));
        sb.append(',');
        sb.append("budgetedAmount");
        sb.append('=');
        sb.append(((this.budgetedAmount == null)?"<null>":this.budgetedAmount));
        sb.append(',');
        sb.append("revenueCategory");
        sb.append('=');
        sb.append(((this.revenueCategory == null)?"<null>":this.revenueCategory));
        sb.append(',');
        sb.append("expenseCategory");
        sb.append('=');
        sb.append(((this.expenseCategory == null)?"<null>":this.expenseCategory));
        sb.append(',');
        sb.append("accountId");
        sb.append('=');
        sb.append(((this.accountId == null)?"<null>":this.accountId));
        sb.append(',');
        sb.append("notes");
        sb.append('=');
        sb.append(((this.notes == null)?"<null>":this.notes));
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

    @Generated("jsonschema2pojo")
    public enum BudgetExpenseCategory {

        OFFICERS("Officers"),
        SUPPLIES("Supplies"),
        ADMIN_MAINTENANCE("AdminMaintenance"),
        EQUIPMENT_REGALIA("EquipmentRegalia"),
        MARKETING("Marketing"),
        OVERHEAD("Overhead");
        private final String value;
        private final static Map<String, BudgetLine.BudgetExpenseCategory> CONSTANTS = new HashMap<String, BudgetLine.BudgetExpenseCategory>();

        static {
            for (BudgetLine.BudgetExpenseCategory c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        BudgetExpenseCategory(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static BudgetLine.BudgetExpenseCategory fromValue(String value) {
            BudgetLine.BudgetExpenseCategory constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("jsonschema2pojo")
    public enum BudgetRevenueCategory {

        RESTRICTED_REVENUE("RestrictedRevenue"),
        UNRESTRICTED_REVENUE("UnrestrictedRevenue"),
        GENERAL_REVENUE("GeneralRevenue");
        private final String value;
        private final static Map<String, BudgetLine.BudgetRevenueCategory> CONSTANTS = new HashMap<String, BudgetLine.BudgetRevenueCategory>();

        static {
            for (BudgetLine.BudgetRevenueCategory c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        BudgetRevenueCategory(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static BudgetLine.BudgetRevenueCategory fromValue(String value) {
            BudgetLine.BudgetRevenueCategory constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
