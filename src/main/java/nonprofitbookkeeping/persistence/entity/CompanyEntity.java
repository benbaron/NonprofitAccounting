package nonprofitbookkeeping.persistence.entity;

import jakarta.persistence.*;

/**
 * Simple JPA entity used for persisting {@link nonprofitbookkeeping.model.Company}
 * objects as a JSON blob. Only the primary key and company name are stored
 * separately; the rest of the company data is serialized.
 */
@Entity
@Table(name = "companies")
public class CompanyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob
    @Column(name = "json_data")
    private String jsonData;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}
