package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.model.Company;

import java.util.List;
import java.util.Optional;

/** Repository for {@link Company} entities. */
public class CompanyRepository {

    /** Persist or update a company. */
    public Company save(Company company) {
        try (EntityManager em = DatabaseManager.getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            if (company.getId() == null) {
                em.persist(company);
            } else {
                company = em.merge(company);
            }
            tx.commit();
            return company;
        }
    }

    /** Find a company by id. */
    public Optional<Company> findById(Long id) {
        try (EntityManager em = DatabaseManager.getEntityManager()) {
            return Optional.ofNullable(em.find(Company.class, id));
        }
    }

    /** Retrieve all companies. */
    public List<Company> findAll() {
        try (EntityManager em = DatabaseManager.getEntityManager()) {
            return em.createQuery("SELECT c FROM Company c", Company.class).getResultList();
        }
    }
}
