
package nonprofitbookkeeping.model.sclx;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "fitId",
    "transactionType",
    "datePosted",
    "dateUser",
    "dateAvailable",
    "checkNumber",
    "referenceNumber",
    "name",
    "memo",
    "payeeId",
    "sic",
    "serverTransactionId",
    "correctFitId",
    "correctAction",
    "extensions"
})
@Generated("jsonschema2pojo")
public class OfxTransaction {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fitId")
    private String fitId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactionType")
    private String transactionType;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("datePosted")
    private String datePosted;
    @JsonProperty("dateUser")
    private String dateUser;
    @JsonProperty("dateAvailable")
    private String dateAvailable;
    @JsonProperty("checkNumber")
    private String checkNumber;
    @JsonProperty("referenceNumber")
    private String referenceNumber;
    @JsonProperty("name")
    private String name;
    @JsonProperty("memo")
    private String memo;
    @JsonProperty("payeeId")
    private String payeeId;
    @JsonProperty("sic")
    private String sic;
    @JsonProperty("serverTransactionId")
    private String serverTransactionId;
    @JsonProperty("correctFitId")
    private String correctFitId;
    @JsonProperty("correctAction")
    private String correctAction;
    @JsonProperty("extensions")
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OfxTransaction() {
    }

    public OfxTransaction(String fitId, String transactionType, String datePosted, String dateUser, String dateAvailable, String checkNumber, String referenceNumber, String name, String memo, String payeeId, String sic, String serverTransactionId, String correctFitId, String correctAction, Extensions extensions) {
        super();
        this.fitId = fitId;
        this.transactionType = transactionType;
        this.datePosted = datePosted;
        this.dateUser = dateUser;
        this.dateAvailable = dateAvailable;
        this.checkNumber = checkNumber;
        this.referenceNumber = referenceNumber;
        this.name = name;
        this.memo = memo;
        this.payeeId = payeeId;
        this.sic = sic;
        this.serverTransactionId = serverTransactionId;
        this.correctFitId = correctFitId;
        this.correctAction = correctAction;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fitId")
    public String getFitId() {
        return fitId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fitId")
    public void setFitId(String fitId) {
        this.fitId = fitId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactionType")
    public String getTransactionType() {
        return transactionType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactionType")
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("datePosted")
    public String getDatePosted() {
        return datePosted;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("datePosted")
    public void setDatePosted(String datePosted) {
        this.datePosted = datePosted;
    }

    @JsonProperty("dateUser")
    public String getDateUser() {
        return dateUser;
    }

    @JsonProperty("dateUser")
    public void setDateUser(String dateUser) {
        this.dateUser = dateUser;
    }

    @JsonProperty("dateAvailable")
    public String getDateAvailable() {
        return dateAvailable;
    }

    @JsonProperty("dateAvailable")
    public void setDateAvailable(String dateAvailable) {
        this.dateAvailable = dateAvailable;
    }

    @JsonProperty("checkNumber")
    public String getCheckNumber() {
        return checkNumber;
    }

    @JsonProperty("checkNumber")
    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    @JsonProperty("referenceNumber")
    public String getReferenceNumber() {
        return referenceNumber;
    }

    @JsonProperty("referenceNumber")
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("memo")
    public String getMemo() {
        return memo;
    }

    @JsonProperty("memo")
    public void setMemo(String memo) {
        this.memo = memo;
    }

    @JsonProperty("payeeId")
    public String getPayeeId() {
        return payeeId;
    }

    @JsonProperty("payeeId")
    public void setPayeeId(String payeeId) {
        this.payeeId = payeeId;
    }

    @JsonProperty("sic")
    public String getSic() {
        return sic;
    }

    @JsonProperty("sic")
    public void setSic(String sic) {
        this.sic = sic;
    }

    @JsonProperty("serverTransactionId")
    public String getServerTransactionId() {
        return serverTransactionId;
    }

    @JsonProperty("serverTransactionId")
    public void setServerTransactionId(String serverTransactionId) {
        this.serverTransactionId = serverTransactionId;
    }

    @JsonProperty("correctFitId")
    public String getCorrectFitId() {
        return correctFitId;
    }

    @JsonProperty("correctFitId")
    public void setCorrectFitId(String correctFitId) {
        this.correctFitId = correctFitId;
    }

    @JsonProperty("correctAction")
    public String getCorrectAction() {
        return correctAction;
    }

    @JsonProperty("correctAction")
    public void setCorrectAction(String correctAction) {
        this.correctAction = correctAction;
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
        sb.append(OfxTransaction.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("fitId");
        sb.append('=');
        sb.append(((this.fitId == null)?"<null>":this.fitId));
        sb.append(',');
        sb.append("transactionType");
        sb.append('=');
        sb.append(((this.transactionType == null)?"<null>":this.transactionType));
        sb.append(',');
        sb.append("datePosted");
        sb.append('=');
        sb.append(((this.datePosted == null)?"<null>":this.datePosted));
        sb.append(',');
        sb.append("dateUser");
        sb.append('=');
        sb.append(((this.dateUser == null)?"<null>":this.dateUser));
        sb.append(',');
        sb.append("dateAvailable");
        sb.append('=');
        sb.append(((this.dateAvailable == null)?"<null>":this.dateAvailable));
        sb.append(',');
        sb.append("checkNumber");
        sb.append('=');
        sb.append(((this.checkNumber == null)?"<null>":this.checkNumber));
        sb.append(',');
        sb.append("referenceNumber");
        sb.append('=');
        sb.append(((this.referenceNumber == null)?"<null>":this.referenceNumber));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("memo");
        sb.append('=');
        sb.append(((this.memo == null)?"<null>":this.memo));
        sb.append(',');
        sb.append("payeeId");
        sb.append('=');
        sb.append(((this.payeeId == null)?"<null>":this.payeeId));
        sb.append(',');
        sb.append("sic");
        sb.append('=');
        sb.append(((this.sic == null)?"<null>":this.sic));
        sb.append(',');
        sb.append("serverTransactionId");
        sb.append('=');
        sb.append(((this.serverTransactionId == null)?"<null>":this.serverTransactionId));
        sb.append(',');
        sb.append("correctFitId");
        sb.append('=');
        sb.append(((this.correctFitId == null)?"<null>":this.correctFitId));
        sb.append(',');
        sb.append("correctAction");
        sb.append('=');
        sb.append(((this.correctAction == null)?"<null>":this.correctAction));
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
