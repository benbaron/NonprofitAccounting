
package nonprofitbookkeeping.model.sclx.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dateAsOf",
    "lastConfirmed",
    "returned",
    "notes"
})
@Generated("jsonschema2pojo")
public class GuardianshipDetailsSupply {

    @JsonProperty("dateAsOf")
    private String dateAsOf;
    @JsonProperty("lastConfirmed")
    private String lastConfirmed;
    @JsonProperty("returned")
    private Boolean returned;
    @JsonProperty("notes")
    private String notes;

    /**
     * No args constructor for use in serialization
     * 
     */
    public GuardianshipDetailsSupply() {
    }

    public GuardianshipDetailsSupply(String dateAsOf, String lastConfirmed, Boolean returned, String notes) {
        super();
        this.dateAsOf = dateAsOf;
        this.lastConfirmed = lastConfirmed;
        this.returned = returned;
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

    @JsonProperty("lastConfirmed")
    public String getLastConfirmed() {
        return lastConfirmed;
    }

    @JsonProperty("lastConfirmed")
    public void setLastConfirmed(String lastConfirmed) {
        this.lastConfirmed = lastConfirmed;
    }

    @JsonProperty("returned")
    public Boolean getReturned() {
        return returned;
    }

    @JsonProperty("returned")
    public void setReturned(Boolean returned) {
        this.returned = returned;
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
        sb.append(GuardianshipDetailsSupply.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("dateAsOf");
        sb.append('=');
        sb.append(((this.dateAsOf == null)?"<null>":this.dateAsOf));
        sb.append(',');
        sb.append("lastConfirmed");
        sb.append('=');
        sb.append(((this.lastConfirmed == null)?"<null>":this.lastConfirmed));
        sb.append(',');
        sb.append("returned");
        sb.append('=');
        sb.append(((this.returned == null)?"<null>":this.returned));
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

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.notes == null)? 0 :this.notes.hashCode()));
        result = ((result* 31)+((this.returned == null)? 0 :this.returned.hashCode()));
        result = ((result* 31)+((this.dateAsOf == null)? 0 :this.dateAsOf.hashCode()));
        result = ((result* 31)+((this.lastConfirmed == null)? 0 :this.lastConfirmed.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GuardianshipDetailsSupply) == false) {
            return false;
        }
        GuardianshipDetailsSupply rhs = ((GuardianshipDetailsSupply) other);
        return (((((this.notes == rhs.notes)||((this.notes!= null)&&this.notes.equals(rhs.notes)))&&((this.returned == rhs.returned)||((this.returned!= null)&&this.returned.equals(rhs.returned))))&&((this.dateAsOf == rhs.dateAsOf)||((this.dateAsOf!= null)&&this.dateAsOf.equals(rhs.dateAsOf))))&&((this.lastConfirmed == rhs.lastConfirmed)||((this.lastConfirmed!= null)&&this.lastConfirmed.equals(rhs.lastConfirmed))));
    }

}
