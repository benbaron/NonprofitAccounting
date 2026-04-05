
package nonprofitbookkeeping.model.sclx;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "minimumReaderVersion",
    "lossyDowngradeTo"
})
@Generated("jsonschema2pojo")
public class Compatibility {

    @JsonProperty("minimumReaderVersion")
    private String minimumReaderVersion;
    @JsonProperty("lossyDowngradeTo")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<@Valid String> lossyDowngradeTo = new LinkedHashSet<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Compatibility() {
    }

    public Compatibility(String minimumReaderVersion, Set<@Valid String> lossyDowngradeTo) {
        super();
        this.minimumReaderVersion = minimumReaderVersion;
        this.lossyDowngradeTo = lossyDowngradeTo;
    }

    @JsonProperty("minimumReaderVersion")
    public String getMinimumReaderVersion() {
        return minimumReaderVersion;
    }

    @JsonProperty("minimumReaderVersion")
    public void setMinimumReaderVersion(String minimumReaderVersion) {
        this.minimumReaderVersion = minimumReaderVersion;
    }

    @JsonProperty("lossyDowngradeTo")
    public Set<String> getLossyDowngradeTo() {
        return lossyDowngradeTo;
    }

    @JsonProperty("lossyDowngradeTo")
    public void setLossyDowngradeTo(Set<String> lossyDowngradeTo) {
        this.lossyDowngradeTo = lossyDowngradeTo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Compatibility.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("minimumReaderVersion");
        sb.append('=');
        sb.append(((this.minimumReaderVersion == null)?"<null>":this.minimumReaderVersion));
        sb.append(',');
        sb.append("lossyDowngradeTo");
        sb.append('=');
        sb.append(((this.lossyDowngradeTo == null)?"<null>":this.lossyDowngradeTo));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
