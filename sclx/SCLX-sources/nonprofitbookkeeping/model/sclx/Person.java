
package nonprofitbookkeeping.model.sclx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    "extensions",
    "scaName",
    "membershipNumber",
    "membershipExpiry",
    "addresses",
    "emails",
    "phones"
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
    @JsonProperty("scaName")
    private String scaName;
    @JsonProperty("membershipNumber")
    private String membershipNumber;
    @JsonProperty("membershipExpiry")
    private String membershipExpiry;
    @JsonProperty("addresses")
    private List<@Valid Address> addresses = new ArrayList<Address>();
    @JsonProperty("emails")
    private List<@Valid String> emails = new ArrayList<String>();
    @JsonProperty("phones")
    private List<@Valid String> phones = new ArrayList<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Person() {
    }

    public Person(String personId, String displayName, Person.PersonKind kind, String email, String phone, Extensions extensions, String scaName, String membershipNumber, String membershipExpiry, List<@Valid Address> addresses, List<@Valid String> emails, List<@Valid String> phones) {
        super();
        this.personId = personId;
        this.displayName = displayName;
        this.kind = kind;
        this.email = email;
        this.phone = phone;
        this.extensions = extensions;
        this.scaName = scaName;
        this.membershipNumber = membershipNumber;
        this.membershipExpiry = membershipExpiry;
        this.addresses = addresses;
        this.emails = emails;
        this.phones = phones;
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

    @JsonProperty("scaName")
    public String getScaName() {
        return scaName;
    }

    @JsonProperty("scaName")
    public void setScaName(String scaName) {
        this.scaName = scaName;
    }

    @JsonProperty("membershipNumber")
    public String getMembershipNumber() {
        return membershipNumber;
    }

    @JsonProperty("membershipNumber")
    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
    }

    @JsonProperty("membershipExpiry")
    public String getMembershipExpiry() {
        return membershipExpiry;
    }

    @JsonProperty("membershipExpiry")
    public void setMembershipExpiry(String membershipExpiry) {
        this.membershipExpiry = membershipExpiry;
    }

    @JsonProperty("addresses")
    public List<Address> getAddresses() {
        return addresses;
    }

    @JsonProperty("addresses")
    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    @JsonProperty("emails")
    public List<String> getEmails() {
        return emails;
    }

    @JsonProperty("emails")
    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    @JsonProperty("phones")
    public List<String> getPhones() {
        return phones;
    }

    @JsonProperty("phones")
    public void setPhones(List<String> phones) {
        this.phones = phones;
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
        sb.append("scaName");
        sb.append('=');
        sb.append(((this.scaName == null)?"<null>":this.scaName));
        sb.append(',');
        sb.append("membershipNumber");
        sb.append('=');
        sb.append(((this.membershipNumber == null)?"<null>":this.membershipNumber));
        sb.append(',');
        sb.append("membershipExpiry");
        sb.append('=');
        sb.append(((this.membershipExpiry == null)?"<null>":this.membershipExpiry));
        sb.append(',');
        sb.append("addresses");
        sb.append('=');
        sb.append(((this.addresses == null)?"<null>":this.addresses));
        sb.append(',');
        sb.append("emails");
        sb.append('=');
        sb.append(((this.emails == null)?"<null>":this.emails));
        sb.append(',');
        sb.append("phones");
        sb.append('=');
        sb.append(((this.phones == null)?"<null>":this.phones));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
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
