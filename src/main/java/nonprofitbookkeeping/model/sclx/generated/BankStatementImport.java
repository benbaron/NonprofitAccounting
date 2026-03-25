
package nonprofitbookkeeping.model.sclx.generated;

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
    "importId",
    "sourceFormat",
    "sourceVersion",
    "statementKind",
    "bankAccount",
    "currency",
    "statementStart",
    "statementEnd",
    "ledgerBalance",
    "availableBalance",
    "documentId",
    "extensions"
})
@Generated("jsonschema2pojo")
public class BankStatementImport {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("importId")
    @Size(min = 1)
    @NotNull
    private String importId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sourceFormat")
    @NotNull
    private BankStatementImport.SourceFormat sourceFormat;
    @JsonProperty("sourceVersion")
    private String sourceVersion;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("statementKind")
    @NotNull
    private BankStatementImport.StatementKind statementKind;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bankAccount")
    @Valid
    @NotNull
    private BankAccount bankAccount;
    @JsonProperty("currency")
    @Pattern(regexp = "^[A-Z]{3}$")
    private String currency;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("statementStart")
    @NotNull
    private String statementStart;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("statementEnd")
    @NotNull
    private String statementEnd;
    @JsonProperty("ledgerBalance")
    @Valid
    private BalanceSnapshot ledgerBalance;
    @JsonProperty("availableBalance")
    @Valid
    private BalanceSnapshot availableBalance;
    @JsonProperty("documentId")
    private String documentId;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public BankStatementImport() {
    }

    public BankStatementImport(String importId, BankStatementImport.SourceFormat sourceFormat, String sourceVersion, BankStatementImport.StatementKind statementKind, BankAccount bankAccount, String currency, String statementStart, String statementEnd, BalanceSnapshot ledgerBalance, BalanceSnapshot availableBalance, String documentId, Extensions extensions) {
        super();
        this.importId = importId;
        this.sourceFormat = sourceFormat;
        this.sourceVersion = sourceVersion;
        this.statementKind = statementKind;
        this.bankAccount = bankAccount;
        this.currency = currency;
        this.statementStart = statementStart;
        this.statementEnd = statementEnd;
        this.ledgerBalance = ledgerBalance;
        this.availableBalance = availableBalance;
        this.documentId = documentId;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("importId")
    public String getImportId() {
        return importId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("importId")
    public void setImportId(String importId) {
        this.importId = importId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sourceFormat")
    public BankStatementImport.SourceFormat getSourceFormat() {
        return sourceFormat;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sourceFormat")
    public void setSourceFormat(BankStatementImport.SourceFormat sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    @JsonProperty("sourceVersion")
    public String getSourceVersion() {
        return sourceVersion;
    }

    @JsonProperty("sourceVersion")
    public void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("statementKind")
    public BankStatementImport.StatementKind getStatementKind() {
        return statementKind;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("statementKind")
    public void setStatementKind(BankStatementImport.StatementKind statementKind) {
        this.statementKind = statementKind;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bankAccount")
    public BankAccount getBankAccount() {
        return bankAccount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bankAccount")
    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return currency;
    }

    @JsonProperty("currency")
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("statementStart")
    public String getStatementStart() {
        return statementStart;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("statementStart")
    public void setStatementStart(String statementStart) {
        this.statementStart = statementStart;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("statementEnd")
    public String getStatementEnd() {
        return statementEnd;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("statementEnd")
    public void setStatementEnd(String statementEnd) {
        this.statementEnd = statementEnd;
    }

    @JsonProperty("ledgerBalance")
    public BalanceSnapshot getLedgerBalance() {
        return ledgerBalance;
    }

    @JsonProperty("ledgerBalance")
    public void setLedgerBalance(BalanceSnapshot ledgerBalance) {
        this.ledgerBalance = ledgerBalance;
    }

    @JsonProperty("availableBalance")
    public BalanceSnapshot getAvailableBalance() {
        return availableBalance;
    }

    @JsonProperty("availableBalance")
    public void setAvailableBalance(BalanceSnapshot availableBalance) {
        this.availableBalance = availableBalance;
    }

    @JsonProperty("documentId")
    public String getDocumentId() {
        return documentId;
    }

    @JsonProperty("documentId")
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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
        sb.append(BankStatementImport.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("importId");
        sb.append('=');
        sb.append(((this.importId == null)?"<null>":this.importId));
        sb.append(',');
        sb.append("sourceFormat");
        sb.append('=');
        sb.append(((this.sourceFormat == null)?"<null>":this.sourceFormat));
        sb.append(',');
        sb.append("sourceVersion");
        sb.append('=');
        sb.append(((this.sourceVersion == null)?"<null>":this.sourceVersion));
        sb.append(',');
        sb.append("statementKind");
        sb.append('=');
        sb.append(((this.statementKind == null)?"<null>":this.statementKind));
        sb.append(',');
        sb.append("bankAccount");
        sb.append('=');
        sb.append(((this.bankAccount == null)?"<null>":this.bankAccount));
        sb.append(',');
        sb.append("currency");
        sb.append('=');
        sb.append(((this.currency == null)?"<null>":this.currency));
        sb.append(',');
        sb.append("statementStart");
        sb.append('=');
        sb.append(((this.statementStart == null)?"<null>":this.statementStart));
        sb.append(',');
        sb.append("statementEnd");
        sb.append('=');
        sb.append(((this.statementEnd == null)?"<null>":this.statementEnd));
        sb.append(',');
        sb.append("ledgerBalance");
        sb.append('=');
        sb.append(((this.ledgerBalance == null)?"<null>":this.ledgerBalance));
        sb.append(',');
        sb.append("availableBalance");
        sb.append('=');
        sb.append(((this.availableBalance == null)?"<null>":this.availableBalance));
        sb.append(',');
        sb.append("documentId");
        sb.append('=');
        sb.append(((this.documentId == null)?"<null>":this.documentId));
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
        result = ((result* 31)+((this.bankAccount == null)? 0 :this.bankAccount.hashCode()));
        result = ((result* 31)+((this.sourceVersion == null)? 0 :this.sourceVersion.hashCode()));
        result = ((result* 31)+((this.availableBalance == null)? 0 :this.availableBalance.hashCode()));
        result = ((result* 31)+((this.extensions == null)? 0 :this.extensions.hashCode()));
        result = ((result* 31)+((this.importId == null)? 0 :this.importId.hashCode()));
        result = ((result* 31)+((this.statementEnd == null)? 0 :this.statementEnd.hashCode()));
        result = ((result* 31)+((this.ledgerBalance == null)? 0 :this.ledgerBalance.hashCode()));
        result = ((result* 31)+((this.statementKind == null)? 0 :this.statementKind.hashCode()));
        result = ((result* 31)+((this.currency == null)? 0 :this.currency.hashCode()));
        result = ((result* 31)+((this.documentId == null)? 0 :this.documentId.hashCode()));
        result = ((result* 31)+((this.sourceFormat == null)? 0 :this.sourceFormat.hashCode()));
        result = ((result* 31)+((this.statementStart == null)? 0 :this.statementStart.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BankStatementImport) == false) {
            return false;
        }
        BankStatementImport rhs = ((BankStatementImport) other);
        return (((((((((((((this.bankAccount == rhs.bankAccount)||((this.bankAccount!= null)&&this.bankAccount.equals(rhs.bankAccount)))&&((this.sourceVersion == rhs.sourceVersion)||((this.sourceVersion!= null)&&this.sourceVersion.equals(rhs.sourceVersion))))&&((this.availableBalance == rhs.availableBalance)||((this.availableBalance!= null)&&this.availableBalance.equals(rhs.availableBalance))))&&((this.extensions == rhs.extensions)||((this.extensions!= null)&&this.extensions.equals(rhs.extensions))))&&((this.importId == rhs.importId)||((this.importId!= null)&&this.importId.equals(rhs.importId))))&&((this.statementEnd == rhs.statementEnd)||((this.statementEnd!= null)&&this.statementEnd.equals(rhs.statementEnd))))&&((this.ledgerBalance == rhs.ledgerBalance)||((this.ledgerBalance!= null)&&this.ledgerBalance.equals(rhs.ledgerBalance))))&&((this.statementKind == rhs.statementKind)||((this.statementKind!= null)&&this.statementKind.equals(rhs.statementKind))))&&((this.currency == rhs.currency)||((this.currency!= null)&&this.currency.equals(rhs.currency))))&&((this.documentId == rhs.documentId)||((this.documentId!= null)&&this.documentId.equals(rhs.documentId))))&&((this.sourceFormat == rhs.sourceFormat)||((this.sourceFormat!= null)&&this.sourceFormat.equals(rhs.sourceFormat))))&&((this.statementStart == rhs.statementStart)||((this.statementStart!= null)&&this.statementStart.equals(rhs.statementStart))));
    }

    @Generated("jsonschema2pojo")
    public enum SourceFormat {

        OFX("OFX");
        private final String value;
        private final static Map<String, BankStatementImport.SourceFormat> CONSTANTS = new HashMap<String, BankStatementImport.SourceFormat>();

        static {
            for (BankStatementImport.SourceFormat c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        SourceFormat(String value) {
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
        public static BankStatementImport.SourceFormat fromValue(String value) {
            BankStatementImport.SourceFormat constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("jsonschema2pojo")
    public enum StatementKind {

        BANK("BANK"),
        CREDIT_CARD("CREDIT_CARD");
        private final String value;
        private final static Map<String, BankStatementImport.StatementKind> CONSTANTS = new HashMap<String, BankStatementImport.StatementKind>();

        static {
            for (BankStatementImport.StatementKind c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        StatementKind(String value) {
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
        public static BankStatementImport.StatementKind fromValue(String value) {
            BankStatementImport.StatementKind constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
