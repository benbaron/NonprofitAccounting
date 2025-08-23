package nonprofitbookkeeping.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.persistence.entity.CompanyEntity;

import java.io.IOException;
import java.util.Optional;

/**
 * Repository for storing and retrieving {@link Company} instances. The full
 * company object is serialized to JSON and stored in the {@link CompanyEntity}.
 */
public class CompanyRepository {
    private final EntityManager entityManager;
    private final ObjectMapper mapper = new ObjectMapper();

    public CompanyRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Persist the given company and return its database identifier.
     */
    public long create(Company company) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        CompanyEntity entity = new CompanyEntity();
        entity.setName(company.getName());
        try {
            entity.setJsonData(mapper.writeValueAsString(company));
        } catch (JsonProcessingException e) {
            tx.rollback();
            throw new RuntimeException("Failed to serialize company", e);
        }
        entityManager.persist(entity);
        tx.commit();
        return entity.getId();
    }

    /**
     * Persist the given company, updating an existing row if the ID already
     * exists. The generated or existing identifier is returned and also stored
     * back on the {@link Company} instance.
     *
     * @param company the company to persist
     * @return the database identifier for the company
     */
    public long saveOrUpdate(Company company) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        CompanyEntity entity;
        if (company.getId() != null) {
            entity = entityManager.find(CompanyEntity.class, company.getId());
            if (entity == null) {
                entity = new CompanyEntity();
                entity.setId(company.getId());
            }
        } else {
            entity = new CompanyEntity();
        }
        entity.setName(company.getName());
        try {
            entity.setJsonData(mapper.writeValueAsString(company));
        } catch (JsonProcessingException e) {
            tx.rollback();
            throw new RuntimeException("Failed to serialize company", e);
        }
        CompanyEntity merged = entityManager.merge(entity);
        tx.commit();
        company.setId(merged.getId());
        return merged.getId();
    }

    /**
     * Retrieve a company by its ID.
     */
    public Optional<Company> findById(long id) {
        CompanyEntity entity = entityManager.find(CompanyEntity.class, id);
        if (entity == null) {
            return Optional.empty();
        }
        try {
            Company company = mapper.readValue(entity.getJsonData(), Company.class);
            return Optional.of(company);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Delete a company by ID.
     */
    public boolean delete(long id) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        CompanyEntity entity = entityManager.find(CompanyEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
            tx.commit();
            return true;
        }
        tx.commit();
        return false;

    }
}
