
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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "personId",
    "displayName",
    "kind",
    "email",
    "phone",
    "extensions"
})
@Generated("jsonschema2pojo")
public class Person {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("personId")
    @Size(min = 1)
    @NotNull
    private String personId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("displayName")
    @Size(min = 1)
    @NotNull
    private String displayName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    @NotNull
    private Person.PersonKind kind;
    @Email
    @JsonProperty("email")
    private String email;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Person() {
    }

    public Person(String personId, String displayName, Person.PersonKind kind, String email, String phone, Extensions extensions) {
        super();
        this.personId = personId;
        this.displayName = displayName;
        this.kind = kind;
        this.email = email;
        this.phone = phone;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("personId")
    public String getPersonId() {
        return personId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("personId")
    public void setPersonId(String personId) {
        this.personId = personId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("displayName")
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("displayName")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    public Person.PersonKind getKind() {
        return kind;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("kind")
    public void setKind(Person.PersonKind kind) {
        this.kind = kind;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("phone")
    public String getPhone() {
        return phone;
    }

    @JsonProperty("phone")
    public void setPhone(String phone) {
        this.phone = phone;
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
        sb.append(Person.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("personId");
        sb.append('=');
        sb.append(((this.personId == null)?"<null>":this.personId));
        sb.append(',');
        sb.append("displayName");
        sb.append('=');
        sb.append(((this.displayName == null)?"<null>":this.displayName));
        sb.append(',');
        sb.append("kind");
        sb.append('=');
        sb.append(((this.kind == null)?"<null>":this.kind));
        sb.append(',');
        sb.append("email");
        sb.append('=');
        sb.append(((this.email == null)?"<null>":this.email));
        sb.append(',');
        sb.append("phone");
        sb.append('=');
        sb.append(((this.phone == null)?"<null>":this.phone));
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
        result = ((result* 31)+((this.extensions == null)? 0 :this.extensions.hashCode()));
        result = ((result* 31)+((this.phone == null)? 0 :this.phone.hashCode()));
        result = ((result* 31)+((this.displayName == null)? 0 :this.displayName.hashCode()));
        result = ((result* 31)+((this.kind == null)? 0 :this.kind.hashCode()));
        result = ((result* 31)+((this.personId == null)? 0 :this.personId.hashCode()));
        result = ((result* 31)+((this.email == null)? 0 :this.email.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Person) == false) {
            return false;
        }
        Person rhs = ((Person) other);
        return (((((((this.extensions == rhs.extensions)||((this.extensions!= null)&&this.extensions.equals(rhs.extensions)))&&((this.phone == rhs.phone)||((this.phone!= null)&&this.phone.equals(rhs.phone))))&&((this.displayName == rhs.displayName)||((this.displayName!= null)&&this.displayName.equals(rhs.displayName))))&&((this.kind == rhs.kind)||((this.kind!= null)&&this.kind.equals(rhs.kind))))&&((this.personId == rhs.personId)||((this.personId!= null)&&this.personId.equals(rhs.personId))))&&((this.email == rhs.email)||((this.email!= null)&&this.email.equals(rhs.email))));
    }

    @Generated("jsonschema2pojo")
    public enum PersonKind {

        VENDOR("VENDOR"),
        CUSTOMER("CUSTOMER"),
        MEMBER("MEMBER"),
        OFFICER("OFFICER"),
        BRANCH("BRANCH"),
        OTHER("OTHER");
        private final String value;
        private final static Map<String, Person.PersonKind> CONSTANTS = new HashMap<String, Person.PersonKind>();

        static {
            for (Person.PersonKind c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        PersonKind(String value) {
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
        public static Person.PersonKind fromValue(String value) {
            Person.PersonKind constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
