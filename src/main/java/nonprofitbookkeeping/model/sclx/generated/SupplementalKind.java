
package nonprofitbookkeeping.model.sclx.generated;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@Generated("jsonschema2pojo")
public enum SupplementalKind {

    RECEIVABLE("RECEIVABLE"),
    PREPAID_EXPENSE("PREPAID_EXPENSE"),
    OTHER_ASSET("OTHER_ASSET"),
    DEFERRED_REVENUE("DEFERRED_REVENUE"),
    PAYABLE("PAYABLE"),
    OTHER_LIABILITY("OTHER_LIABILITY");
    private final String value;
    private final static Map<String, SupplementalKind> CONSTANTS = new HashMap<String, SupplementalKind>();

    static {
        for (SupplementalKind c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    SupplementalKind(String value) {
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
    public static SupplementalKind fromValue(String value) {
        SupplementalKind constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
