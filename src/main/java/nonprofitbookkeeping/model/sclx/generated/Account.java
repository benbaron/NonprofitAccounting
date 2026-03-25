
package nonprofitbookkeeping.model.sclx.generated;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "Number",
    "Name",
    "Type",
    "Parent",
    "IncreaseSide",
    "OpeningBalance",
    "SupplementalKinds",
    "accountId",
    "code",
    "subtype",
    "active",
    "reportingTags",
    "extensions"
})
@Generated("jsonschema2pojo")
public class Account {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("Number")
    @Size(min = 1)
    @NotNull
    private String number;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("Name")
    @Size(min = 1)
    @NotNull
    private String name;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("Type")
    @NotNull
    private Account.AccountType type;
    @JsonProperty("Parent")
    private String parent;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("IncreaseSide")
    @NotNull
    private Account.IncreaseSide increaseSide;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("OpeningBalance")
    @Pattern(regexp = "^-?[0-9]+\\.[0-9]{2}$")
    @NotNull
    private String openingBalance;
    @JsonProperty("SupplementalKinds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<@Valid SupplementalKind> supplementalKinds = new LinkedHashSet<SupplementalKind>();
    @JsonProperty("accountId")
    private String accountId;
    @JsonProperty("code")
    private String code;
    @JsonProperty("subtype")
    private String subtype;
    @JsonProperty("active")
    private Boolean active = true;
    @JsonProperty("reportingTags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<@Valid String> reportingTags = new LinkedHashSet<String>();
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Account() {
    }

    public Account(String number, String name, Account.AccountType type, String parent, Account.IncreaseSide increaseSide, String openingBalance, Set<@Valid SupplementalKind> supplementalKinds, String accountId, String code, String subtype, Boolean active, Set<@Valid String> reportingTags, Extensions extensions) {
        super();
        this.number = number;
        this.name = name;
        this.type = type;
        this.parent = parent;
        this.increaseSide = increaseSide;
        this.openingBalance = openingBalance;
        this.supplementalKinds = supplementalKinds;
        this.accountId = accountId;
        this.code = code;
        this.subtype = subtype;
        this.active = active;
        this.reportingTags = reportingTags;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("Number")
    public String getNumber() {
        return number;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("Number")
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("Name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("Name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("Type")
    public Account.AccountType getType() {
        return type;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("Type")
    public void setType(Account.AccountType type) {
        this.type = type;
    }

    @JsonProperty("Parent")
    public String getParent() {
        return parent;
    }

    @JsonProperty("Parent")
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("IncreaseSide")
    public Account.IncreaseSide getIncreaseSide() {
        return increaseSide;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("IncreaseSide")
    public void setIncreaseSide(Account.IncreaseSide increaseSide) {
        this.increaseSide = increaseSide;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("OpeningBalance")
    public String getOpeningBalance() {
        return openingBalance;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("OpeningBalance")
    public void setOpeningBalance(String openingBalance) {
        this.openingBalance = openingBalance;
    }

    @JsonProperty("SupplementalKinds")
    public Set<SupplementalKind> getSupplementalKinds() {
        return supplementalKinds;
    }

    @JsonProperty("SupplementalKinds")
    public void setSupplementalKinds(Set<SupplementalKind> supplementalKinds) {
        this.supplementalKinds = supplementalKinds;
    }

    @JsonProperty("accountId")
    public String getAccountId() {
        return accountId;
    }

    @JsonProperty("accountId")
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("subtype")
    public String getSubtype() {
        return subtype;
    }

    @JsonProperty("subtype")
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    @JsonProperty("active")
    public Boolean getActive() {
        return active;
    }

    @JsonProperty("active")
    public void setActive(Boolean active) {
        this.active = active;
    }

    @JsonProperty("reportingTags")
    public Set<String> getReportingTags() {
        return reportingTags;
    }

    @JsonProperty("reportingTags")
    public void setReportingTags(Set<String> reportingTags) {
        this.reportingTags = reportingTags;
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
        sb.append(Account.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("number");
        sb.append('=');
        sb.append(((this.number == null)?"<null>":this.number));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("type");
        sb.append('=');
        sb.append(((this.type == null)?"<null>":this.type));
        sb.append(',');
        sb.append("parent");
        sb.append('=');
        sb.append(((this.parent == null)?"<null>":this.parent));
        sb.append(',');
        sb.append("increaseSide");
        sb.append('=');
        sb.append(((this.increaseSide == null)?"<null>":this.increaseSide));
        sb.append(',');
        sb.append("openingBalance");
        sb.append('=');
        sb.append(((this.openingBalance == null)?"<null>":this.openingBalance));
        sb.append(',');
        sb.append("supplementalKinds");
        sb.append('=');
        sb.append(((this.supplementalKinds == null)?"<null>":this.supplementalKinds));
        sb.append(',');
        sb.append("accountId");
        sb.append('=');
        sb.append(((this.accountId == null)?"<null>":this.accountId));
        sb.append(',');
        sb.append("code");
        sb.append('=');
        sb.append(((this.code == null)?"<null>":this.code));
        sb.append(',');
        sb.append("subtype");
        sb.append('=');
        sb.append(((this.subtype == null)?"<null>":this.subtype));
        sb.append(',');
        sb.append("active");
        sb.append('=');
        sb.append(((this.active == null)?"<null>":this.active));
        sb.append(',');
        sb.append("reportingTags");
        sb.append('=');
        sb.append(((this.reportingTags == null)?"<null>":this.reportingTags));
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
        result = ((result* 31)+((this.parent == null)? 0 :this.parent.hashCode()));
        result = ((result* 31)+((this.code == null)? 0 :this.code.hashCode()));
        result = ((result* 31)+((this.active == null)? 0 :this.active.hashCode()));
        result = ((result* 31)+((this.reportingTags == null)? 0 :this.reportingTags.hashCode()));
        result = ((result* 31)+((this.type == null)? 0 :this.type.hashCode()));
        result = ((result* 31)+((this.number == null)? 0 :this.number.hashCode()));
        result = ((result* 31)+((this.increaseSide == null)? 0 :this.increaseSide.hashCode()));
        result = ((result* 31)+((this.accountId == null)? 0 :this.accountId.hashCode()));
        result = ((result* 31)+((this.extensions == null)? 0 :this.extensions.hashCode()));
        result = ((result* 31)+((this.subtype == null)? 0 :this.subtype.hashCode()));
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.supplementalKinds == null)? 0 :this.supplementalKinds.hashCode()));
        result = ((result* 31)+((this.openingBalance == null)? 0 :this.openingBalance.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Account) == false) {
            return false;
        }
        Account rhs = ((Account) other);
        return ((((((((((((((this.parent == rhs.parent)||((this.parent!= null)&&this.parent.equals(rhs.parent)))&&((this.code == rhs.code)||((this.code!= null)&&this.code.equals(rhs.code))))&&((this.active == rhs.active)||((this.active!= null)&&this.active.equals(rhs.active))))&&((this.reportingTags == rhs.reportingTags)||((this.reportingTags!= null)&&this.reportingTags.equals(rhs.reportingTags))))&&((this.type == rhs.type)||((this.type!= null)&&this.type.equals(rhs.type))))&&((this.number == rhs.number)||((this.number!= null)&&this.number.equals(rhs.number))))&&((this.increaseSide == rhs.increaseSide)||((this.increaseSide!= null)&&this.increaseSide.equals(rhs.increaseSide))))&&((this.accountId == rhs.accountId)||((this.accountId!= null)&&this.accountId.equals(rhs.accountId))))&&((this.extensions == rhs.extensions)||((this.extensions!= null)&&this.extensions.equals(rhs.extensions))))&&((this.subtype == rhs.subtype)||((this.subtype!= null)&&this.subtype.equals(rhs.subtype))))&&((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name))))&&((this.supplementalKinds == rhs.supplementalKinds)||((this.supplementalKinds!= null)&&this.supplementalKinds.equals(rhs.supplementalKinds))))&&((this.openingBalance == rhs.openingBalance)||((this.openingBalance!= null)&&this.openingBalance.equals(rhs.openingBalance))));
    }

    @Generated("jsonschema2pojo")
    public enum AccountType {

        ASSET("ASSET"),
        LIABILITY("LIABILITY"),
        NET_ASSETS("NET_ASSETS"),
        REVENUE("REVENUE"),
        EXPENSE("EXPENSE");
        private final String value;
        private final static Map<String, Account.AccountType> CONSTANTS = new HashMap<String, Account.AccountType>();

        static {
            for (Account.AccountType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        AccountType(String value) {
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
        public static Account.AccountType fromValue(String value) {
            Account.AccountType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("jsonschema2pojo")
    public enum IncreaseSide {

        DEBIT("DEBIT"),
        CREDIT("CREDIT");
        private final String value;
        private final static Map<String, Account.IncreaseSide> CONSTANTS = new HashMap<String, Account.IncreaseSide>();

        static {
            for (Account.IncreaseSide c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        IncreaseSide(String value) {
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
        public static Account.IncreaseSide fromValue(String value) {
            Account.IncreaseSide constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
