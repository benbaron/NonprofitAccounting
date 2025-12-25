package nonprofitbookkeeping.model;

import java.io.Serializable;
import java.util.Objects;

/** Simple contact information for a donor. */
public class DonorContact implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String email;
    private String phone;

    public DonorContact() {
        // Default constructor required for frameworks and serialization
    }

    public DonorContact(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DonorContact)) {
            return false;
        }
        DonorContact that = (DonorContact) o;
        return Objects.equals(this.id, that.id)
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.email, that.email)
                && Objects.equals(this.phone, that.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name, this.email, this.phone);
    }

    @Override
    public String toString() {
        return "DonorContact{"
                + "id='" + this.id + '\''
                + ", name='" + this.name + '\''
                + ", email='" + this.email + '\''
                + ", phone='" + this.phone + '\''
                + '}';
    }
}
