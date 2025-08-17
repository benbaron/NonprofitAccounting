package nonprofitbookkeeping.persistence.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.model.reports.ReportConfiguration;

import java.util.List;

/**
 * DAO for {@link ReportConfiguration} entities.
 */
public class ReportConfigurationDao {
    private final EntityManager entityManager;

    public ReportConfigurationDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void save(ReportConfiguration config) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        if (entityManager.find(ReportConfiguration.class, config.getConfigurationId()) == null) {
            entityManager.persist(config);
        } else {
            entityManager.merge(config);
        }
        tx.commit();
    }

    public void saveAll(List<ReportConfiguration> configs) {
        configs.forEach(this::save);
    }

    public List<ReportConfiguration> findAll() {
        return entityManager.createQuery("SELECT c FROM ReportConfiguration c", ReportConfiguration.class)
                .getResultList();
    }
}
