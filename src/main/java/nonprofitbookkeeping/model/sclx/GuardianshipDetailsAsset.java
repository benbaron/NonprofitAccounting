
package nonprofitbookkeeping.model.sclx;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dateAsOf",
    "confirmed",
    "confirmationStatus",
    "notes"
})
@Generated("jsonschema2pojo")
public class GuardianshipDetailsAsset {

    @JsonProperty("dateAsOf")
    private String dateAsOf;
    @JsonProperty("confirmed")
    private Boolean confirmed;
    @JsonProperty("confirmationStatus")
    private GuardianshipDetailsAsset.ConfirmationStatus confirmationStatus;
    @JsonProperty("notes")
    private String notes;

    /**
     * No args constructor for use in serialization
     * 
     */
    public GuardianshipDetailsAsset() {
    }

    public GuardianshipDetailsAsset(String dateAsOf, Boolean confirmed, GuardianshipDetailsAsset.ConfirmationStatus confirmationStatus, String notes) {
        super();
        this.dateAsOf = dateAsOf;
        this.confirmed = confirmed;
        this.confirmationStatus = confirmationStatus;
        this.notes = notes;
    }

    @JsonProperty("dateAsOf")
    public String getDateAsOf() {
        return dateAsOf;
    }

    @JsonProperty("dateAsOf")
    public void setDateAsOf(String dateAsOf) {
        this.dateAsOf = dateAsOf;
    }

    @JsonProperty("confirmed")
    public Boolean getConfirmed() {
        return confirmed;
    }

    @JsonProperty("confirmed")
    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    @JsonProperty("confirmationStatus")
    public GuardianshipDetailsAsset.ConfirmationStatus getConfirmationStatus() {
        return confirmationStatus;
    }

    @JsonProperty("confirmationStatus")
    public void setConfirmationStatus(GuardianshipDetailsAsset.ConfirmationStatus confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }

    @JsonProperty("notes")
    public String getNotes() {
        return notes;
    }

    @JsonProperty("notes")
    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(GuardianshipDetailsAsset.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("dateAsOf");
        sb.append('=');
        sb.append(((this.dateAsOf == null)?"<null>":this.dateAsOf));
        sb.append(',');
        sb.append("confirmed");
        sb.append('=');
        sb.append(((this.confirmed == null)?"<null>":this.confirmed));
        sb.append(',');
        sb.append("confirmationStatus");
        sb.append('=');
        sb.append(((this.confirmationStatus == null)?"<null>":this.confirmationStatus));
        sb.append(',');
        sb.append("notes");
        sb.append('=');
        sb.append(((this.notes == null)?"<null>":this.notes));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Generated("jsonschema2pojo")
    public enum ConfirmationStatus {

        CONFIRMED("CONFIRMED"),
        UNCONFIRMED("UNCONFIRMED"),
        UNKNOWN("UNKNOWN");
        private final String value;
        private final static Map<String, GuardianshipDetailsAsset.ConfirmationStatus> CONSTANTS = new HashMap<String, GuardianshipDetailsAsset.ConfirmationStatus>();

        static {
            for (GuardianshipDetailsAsset.ConfirmationStatus c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        ConfirmationStatus(String value) {
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
        public static GuardianshipDetailsAsset.ConfirmationStatus fromValue(String value) {
            GuardianshipDetailsAsset.ConfirmationStatus constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
