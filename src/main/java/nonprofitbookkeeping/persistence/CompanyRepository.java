package nonprofitbookkeeping.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.persistence.entity.CompanyEntity;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository for storing and retrieving {@link Company} instances. The full
 * company object is serialized to JSON and stored in the {@link CompanyEntity}.
 */
public class CompanyRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyRepository.class);

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
            LOGGER.error("Failed to serialize company '{}' (id: {}).", company.getName(), company.getId(), e);
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
        boolean isNew = false;
        if (company.getId() != null) {
            entity = entityManager.find(CompanyEntity.class, company.getId());
            if (entity == null) {
                entity = new CompanyEntity();
                entity.setId(company.getId());
                isNew = true;
            }
        } else {
            entity = new CompanyEntity();
            isNew = true;
        }
        entity.setName(company.getName());
        try {
            entity.setJsonData(mapper.writeValueAsString(company));
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize company '{}' (id: {}).", company.getName(), company.getId(), e);
            tx.rollback();
            throw new RuntimeException("Failed to serialize company", e);
        }
        if (isNew) {
            entityManager.persist(entity);
        } else {
            entity = entityManager.merge(entity);
        }
        tx.commit();
        company.setId(entity.getId());
        return entity.getId();
    }

    /**
     * Retrieve the identifier of the first company stored in the
     * database.
     *
     * <p>This is used by legacy workflows that assume a single
     * company instance and simply need <em>any</em> company to be
     * loaded.</p>
     */
    public Optional<Long> findFirstId() {
        return entityManager.createQuery(
                        "SELECT c.id FROM CompanyEntity c ORDER BY c.id ASC",
                        Long.class)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    /**
     * Retrieve a company by its ID.
     */
    public Optional<Company> findById(long id) {
        CompanyEntity entity = entityManager.find(CompanyEntity.class, id);
        if (entity == null) {
            return Optional.empty();
        }
        if (entity.getJsonData() == null || entity.getJsonData().isBlank()) {
            Company company = new Company();
            company.setId(entity.getId());
            if (entity.getName() != null) {
                company.getCompanyProfile().setCompanyName(entity.getName());
            }
            return Optional.of(company);
        }
        try {
            Company company = mapper.readValue(entity.getJsonData(), Company.class);
            return Optional.of(company);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieve all company entities.
     */
    public java.util.List<CompanyEntity> findAll() {
        return entityManager.createQuery("SELECT c FROM CompanyEntity c", CompanyEntity.class)
                .getResultList();
    }

    /**
     * @return total number of company records present
     */
    public long count() {
        return entityManager.createQuery("SELECT COUNT(c) FROM CompanyEntity c", Long.class)
                .getSingleResult();
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
