
package nonprofitbookkeeping.model.sclx;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "recordType",
    "recordId"
})
@Generated("jsonschema2pojo")
public class SupplementalRef {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("recordType")
    @NotNull
    private SupplementalRef.SupplementalRefType recordType;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("recordId")
    @Size(min = 1)
    @NotNull
    private String recordId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SupplementalRef() {
    }

    public SupplementalRef(SupplementalRef.SupplementalRefType recordType, String recordId) {
        super();
        this.recordType = recordType;
        this.recordId = recordId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("recordType")
    public SupplementalRef.SupplementalRefType getRecordType() {
        return recordType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("recordType")
    public void setRecordType(SupplementalRef.SupplementalRefType recordType) {
        this.recordType = recordType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("recordId")
    public String getRecordId() {
        return recordId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("recordId")
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SupplementalRef.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("recordType");
        sb.append('=');
        sb.append(((this.recordType == null)?"<null>":this.recordType));
        sb.append(',');
        sb.append("recordId");
        sb.append('=');
        sb.append(((this.recordId == null)?"<null>":this.recordId));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Generated("jsonschema2pojo")
    public enum SupplementalRefType {

        OUTSTANDING_ITEM("OUTSTANDING_ITEM"),
        OTHER_ASSET_ITEM("OTHER_ASSET_ITEM"),
        SUPPLEMENTAL_ITEM("SUPPLEMENTAL_ITEM");
        private final String value;
        private final static Map<String, SupplementalRef.SupplementalRefType> CONSTANTS = new HashMap<String, SupplementalRef.SupplementalRefType>();

        static {
            for (SupplementalRef.SupplementalRefType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        SupplementalRefType(String value) {
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
        public static SupplementalRef.SupplementalRefType fromValue(String value) {
            SupplementalRef.SupplementalRefType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
