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
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
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
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(email, that.email)
                && Objects.equals(phone, that.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, phone);
    }

    @Override
    public String toString() {
        return "DonorContact{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", email='" + email + '\''
                + ", phone='" + phone + '\''
                + '}';
    }
}
