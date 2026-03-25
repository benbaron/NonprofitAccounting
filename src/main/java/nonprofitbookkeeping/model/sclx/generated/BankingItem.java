
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
    "bankingItemId",
    "kind",
    "bankAccountId",
    "transactionId",
    "lineIds",
    "clearedDate",
    "amount",
    "checkNumber",
    "payee",
    "depositDate",
    "payer",
    "depositId",
    "memo",
    "source",
    "status",
    "importId",
    "ofx",
    "extensions"
})
@Generated("jsonschema2pojo")
public class BankingItem {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bankingItemId")
    @Size(min = 1)
    @NotNull
    private String bankingItemId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    @NotNull
    private BankingItem.BankingItemKind kind;
    @JsonProperty("bankAccountId")
    private String bankAccountId;
    @JsonProperty("transactionId")
    private String transactionId;
    @JsonProperty("lineIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<@Valid String> lineIds = new LinkedHashSet<String>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("clearedDate")
    @NotNull
    private String clearedDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amount")
    @Pattern(regexp = "^[0-9]+\\.[0-9]{2}$")
    @NotNull
    private String amount;
    @JsonProperty("checkNumber")
    private String checkNumber;
    @JsonProperty("payee")
    private String payee;
    @JsonProperty("depositDate")
    private String depositDate;
    @JsonProperty("payer")
    private String payer;
    @JsonProperty("depositId")
    private String depositId;
    @JsonProperty("memo")
    private String memo;
    @JsonProperty("source")
    private BankingItem.BankingItemSource source;
    @JsonProperty("status")
    private BankingItem.BankingItemStatus status;
    @JsonProperty("importId")
    private String importId;
    @JsonProperty("ofx")
    @Valid
    private OfxTransaction ofx;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public BankingItem() {
    }

    public BankingItem(String bankingItemId, BankingItem.BankingItemKind kind, String bankAccountId, String transactionId, Set<@Valid String> lineIds, String clearedDate, String amount, String checkNumber, String payee, String depositDate, String payer, String depositId, String memo, BankingItem.BankingItemSource source, BankingItem.BankingItemStatus status, String importId, OfxTransaction ofx, Extensions extensions) {
        super();
        this.bankingItemId = bankingItemId;
        this.kind = kind;
        this.bankAccountId = bankAccountId;
        this.transactionId = transactionId;
        this.lineIds = lineIds;
        this.clearedDate = clearedDate;
        this.amount = amount;
        this.checkNumber = checkNumber;
        this.payee = payee;
        this.depositDate = depositDate;
        this.payer = payer;
        this.depositId = depositId;
        this.memo = memo;
        this.source = source;
        this.status = status;
        this.importId = importId;
        this.ofx = ofx;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bankingItemId")
    public String getBankingItemId() {
        return bankingItemId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bankingItemId")
    public void setBankingItemId(String bankingItemId) {
        this.bankingItemId = bankingItemId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    public BankingItem.BankingItemKind getKind() {
        return kind;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    public void setKind(BankingItem.BankingItemKind kind) {
        this.kind = kind;
    }

    @JsonProperty("bankAccountId")
    public String getBankAccountId() {
        return bankAccountId;
    }

    @JsonProperty("bankAccountId")
    public void setBankAccountId(String bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId;
    }

    @JsonProperty("transactionId")
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @JsonProperty("lineIds")
    public Set<String> getLineIds() {
        return lineIds;
    }

    @JsonProperty("lineIds")
    public void setLineIds(Set<String> lineIds) {
        this.lineIds = lineIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("clearedDate")
    public String getClearedDate() {
        return clearedDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("clearedDate")
    public void setClearedDate(String clearedDate) {
        this.clearedDate = clearedDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amount")
    public String getAmount() {
        return amount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("amount")
    public void setAmount(String amount) {
        this.amount = amount;
    }

    @JsonProperty("checkNumber")
    public String getCheckNumber() {
        return checkNumber;
    }

    @JsonProperty("checkNumber")
    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    @JsonProperty("payee")
    public String getPayee() {
        return payee;
    }

    @JsonProperty("payee")
    public void setPayee(String payee) {
        this.payee = payee;
    }

    @JsonProperty("depositDate")
    public String getDepositDate() {
        return depositDate;
    }

    @JsonProperty("depositDate")
    public void setDepositDate(String depositDate) {
        this.depositDate = depositDate;
    }

    @JsonProperty("payer")
    public String getPayer() {
        return payer;
    }

    @JsonProperty("payer")
    public void setPayer(String payer) {
        this.payer = payer;
    }

    @JsonProperty("depositId")
    public String getDepositId() {
        return depositId;
    }

    @JsonProperty("depositId")
    public void setDepositId(String depositId) {
        this.depositId = depositId;
    }

    @JsonProperty("memo")
    public String getMemo() {
        return memo;
    }

    @JsonProperty("memo")
    public void setMemo(String memo) {
        this.memo = memo;
    }

    @JsonProperty("source")
    public BankingItem.BankingItemSource getSource() {
        return source;
    }

    @JsonProperty("source")
    public void setSource(BankingItem.BankingItemSource source) {
        this.source = source;
    }

    @JsonProperty("status")
    public BankingItem.BankingItemStatus getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(BankingItem.BankingItemStatus status) {
        this.status = status;
    }

    @JsonProperty("importId")
    public String getImportId() {
        return importId;
    }

    @JsonProperty("importId")
    public void setImportId(String importId) {
        this.importId = importId;
    }

    @JsonProperty("ofx")
    public OfxTransaction getOfx() {
        return ofx;
    }

    @JsonProperty("ofx")
    public void setOfx(OfxTransaction ofx) {
        this.ofx = ofx;
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
        sb.append(BankingItem.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("bankingItemId");
        sb.append('=');
        sb.append(((this.bankingItemId == null)?"<null>":this.bankingItemId));
        sb.append(',');
        sb.append("kind");
        sb.append('=');
        sb.append(((this.kind == null)?"<null>":this.kind));
        sb.append(',');
        sb.append("bankAccountId");
        sb.append('=');
        sb.append(((this.bankAccountId == null)?"<null>":this.bankAccountId));
        sb.append(',');
        sb.append("transactionId");
        sb.append('=');
        sb.append(((this.transactionId == null)?"<null>":this.transactionId));
        sb.append(',');
        sb.append("lineIds");
        sb.append('=');
        sb.append(((this.lineIds == null)?"<null>":this.lineIds));
        sb.append(',');
        sb.append("clearedDate");
        sb.append('=');
        sb.append(((this.clearedDate == null)?"<null>":this.clearedDate));
        sb.append(',');
        sb.append("amount");
        sb.append('=');
        sb.append(((this.amount == null)?"<null>":this.amount));
        sb.append(',');
        sb.append("checkNumber");
        sb.append('=');
        sb.append(((this.checkNumber == null)?"<null>":this.checkNumber));
        sb.append(',');
        sb.append("payee");
        sb.append('=');
        sb.append(((this.payee == null)?"<null>":this.payee));
        sb.append(',');
        sb.append("depositDate");
        sb.append('=');
        sb.append(((this.depositDate == null)?"<null>":this.depositDate));
        sb.append(',');
        sb.append("payer");
        sb.append('=');
        sb.append(((this.payer == null)?"<null>":this.payer));
        sb.append(',');
        sb.append("depositId");
        sb.append('=');
        sb.append(((this.depositId == null)?"<null>":this.depositId));
        sb.append(',');
        sb.append("memo");
        sb.append('=');
        sb.append(((this.memo == null)?"<null>":this.memo));
        sb.append(',');
        sb.append("source");
        sb.append('=');
        sb.append(((this.source == null)?"<null>":this.source));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
        sb.append(',');
        sb.append("importId");
        sb.append('=');
        sb.append(((this.importId == null)?"<null>":this.importId));
        sb.append(',');
        sb.append("ofx");
        sb.append('=');
        sb.append(((this.ofx == null)?"<null>":this.ofx));
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
        result = ((result* 31)+((this.ofx == null)? 0 :this.ofx.hashCode()));
        result = ((result* 31)+((this.amount == null)? 0 :this.amount.hashCode()));
        result = ((result* 31)+((this.checkNumber == null)? 0 :this.checkNumber.hashCode()));
        result = ((result* 31)+((this.depositId == null)? 0 :this.depositId.hashCode()));
        result = ((result* 31)+((this.kind == null)? 0 :this.kind.hashCode()));
        result = ((result* 31)+((this.memo == null)? 0 :this.memo.hashCode()));
        result = ((result* 31)+((this.source == null)? 0 :this.source.hashCode()));
        result = ((result* 31)+((this.payer == null)? 0 :this.payer.hashCode()));
        result = ((result* 31)+((this.transactionId == null)? 0 :this.transactionId.hashCode()));
        result = ((result* 31)+((this.lineIds == null)? 0 :this.lineIds.hashCode()));
        result = ((result* 31)+((this.payee == null)? 0 :this.payee.hashCode()));
        result = ((result* 31)+((this.depositDate == null)? 0 :this.depositDate.hashCode()));
        result = ((result* 31)+((this.extensions == null)? 0 :this.extensions.hashCode()));
        result = ((result* 31)+((this.bankingItemId == null)? 0 :this.bankingItemId.hashCode()));
        result = ((result* 31)+((this.importId == null)? 0 :this.importId.hashCode()));
        result = ((result* 31)+((this.bankAccountId == null)? 0 :this.bankAccountId.hashCode()));
        result = ((result* 31)+((this.clearedDate == null)? 0 :this.clearedDate.hashCode()));
        result = ((result* 31)+((this.status == null)? 0 :this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BankingItem) == false) {
            return false;
        }
        BankingItem rhs = ((BankingItem) other);
        return (((((((((((((((((((this.ofx == rhs.ofx)||((this.ofx!= null)&&this.ofx.equals(rhs.ofx)))&&((this.amount == rhs.amount)||((this.amount!= null)&&this.amount.equals(rhs.amount))))&&((this.checkNumber == rhs.checkNumber)||((this.checkNumber!= null)&&this.checkNumber.equals(rhs.checkNumber))))&&((this.depositId == rhs.depositId)||((this.depositId!= null)&&this.depositId.equals(rhs.depositId))))&&((this.kind == rhs.kind)||((this.kind!= null)&&this.kind.equals(rhs.kind))))&&((this.memo == rhs.memo)||((this.memo!= null)&&this.memo.equals(rhs.memo))))&&((this.source == rhs.source)||((this.source!= null)&&this.source.equals(rhs.source))))&&((this.payer == rhs.payer)||((this.payer!= null)&&this.payer.equals(rhs.payer))))&&((this.transactionId == rhs.transactionId)||((this.transactionId!= null)&&this.transactionId.equals(rhs.transactionId))))&&((this.lineIds == rhs.lineIds)||((this.lineIds!= null)&&this.lineIds.equals(rhs.lineIds))))&&((this.payee == rhs.payee)||((this.payee!= null)&&this.payee.equals(rhs.payee))))&&((this.depositDate == rhs.depositDate)||((this.depositDate!= null)&&this.depositDate.equals(rhs.depositDate))))&&((this.extensions == rhs.extensions)||((this.extensions!= null)&&this.extensions.equals(rhs.extensions))))&&((this.bankingItemId == rhs.bankingItemId)||((this.bankingItemId!= null)&&this.bankingItemId.equals(rhs.bankingItemId))))&&((this.importId == rhs.importId)||((this.importId!= null)&&this.importId.equals(rhs.importId))))&&((this.bankAccountId == rhs.bankAccountId)||((this.bankAccountId!= null)&&this.bankAccountId.equals(rhs.bankAccountId))))&&((this.clearedDate == rhs.clearedDate)||((this.clearedDate!= null)&&this.clearedDate.equals(rhs.clearedDate))))&&((this.status == rhs.status)||((this.status!= null)&&this.status.equals(rhs.status))));
    }

    @Generated("jsonschema2pojo")
    public enum BankingItemKind {

        CHECK("CHECK"),
        DEPOSIT("DEPOSIT"),
        OTHER_WITHDRAWAL("OTHER_WITHDRAWAL"),
        OTHER_CREDIT("OTHER_CREDIT"),
        BANK_FEE("BANK_FEE"),
        INTEREST("INTEREST"),
        ADJUSTMENT("ADJUSTMENT");
        private final String value;
        private final static Map<String, BankingItem.BankingItemKind> CONSTANTS = new HashMap<String, BankingItem.BankingItemKind>();

        static {
            for (BankingItem.BankingItemKind c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        BankingItemKind(String value) {
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
        public static BankingItem.BankingItemKind fromValue(String value) {
            BankingItem.BankingItemKind constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("jsonschema2pojo")
    public enum BankingItemSource {

        MANUAL("MANUAL"),
        BANK_IMPORT("BANK_IMPORT"),
        BANK_RECONCILIATION("BANK_RECONCILIATION"),
        OFX_IMPORT("OFX_IMPORT"),
        SYSTEM_GENERATED("SYSTEM_GENERATED");
        private final String value;
        private final static Map<String, BankingItem.BankingItemSource> CONSTANTS = new HashMap<String, BankingItem.BankingItemSource>();

        static {
            for (BankingItem.BankingItemSource c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        BankingItemSource(String value) {
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
        public static BankingItem.BankingItemSource fromValue(String value) {
            BankingItem.BankingItemSource constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("jsonschema2pojo")
    public enum BankingItemStatus {

        PENDING("PENDING"),
        OUTSTANDING("OUTSTANDING"),
        CLEARED("CLEARED"),
        VOID("VOID");
        private final String value;
        private final static Map<String, BankingItem.BankingItemStatus> CONSTANTS = new HashMap<String, BankingItem.BankingItemStatus>();

        static {
            for (BankingItem.BankingItemStatus c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        BankingItemStatus(String value) {
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
        public static BankingItem.BankingItemStatus fromValue(String value) {
            BankingItem.BankingItemStatus constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
