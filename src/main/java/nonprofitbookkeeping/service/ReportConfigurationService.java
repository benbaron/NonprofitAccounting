package nonprofitbookkeeping.service;

import jakarta.persistence.EntityManager;
import nonprofitbookkeeping.model.reports.ReportConfiguration;
import nonprofitbookkeeping.persistence.DatabaseManager;
import nonprofitbookkeeping.persistence.dao.ReportConfigurationDao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service class for managing {@link ReportConfiguration} objects.
 * <p>
 * Previous iterations persisted configurations to JSON on disk using
 * {@code JacksonDataStorer}. This implementation delegates persistence to the
 * JPA layer through {@link ReportConfigurationDao} so configurations are stored
 * in the embedded database.
 * </p>
 */
public class ReportConfigurationService {

    /** Logger for this class. */
    private static final Logger LOGGER =
            Logger.getLogger(ReportConfigurationService.class.getName());

    public ReportConfigurationService() {
    }

    /**
     * Persist the provided report configurations. The {@code companyDirectory}
     * parameter is retained for API compatibility but is no longer used.
     */
    public void saveConfigurations(List<ReportConfiguration> configs,
                                   File companyDirectory) throws IOException {
        if (configs == null) {
            LOGGER.warning("Attempted to save a null list of report configurations. Aborting.");
            return;
        }
        try (EntityManager em = DatabaseManager.getEntityManager()) {
            ReportConfigurationDao dao = new ReportConfigurationDao(em);
            dao.saveAll(configs);
        }
    }

    /**
     * Load all saved report configurations from the database. The
     * {@code companyDirectory} parameter is ignored but kept for backward
     * compatibility.
     */
    public List<ReportConfiguration> loadConfigurations(File companyDirectory) {
        try (EntityManager em = DatabaseManager.getEntityManager()) {
            ReportConfigurationDao dao = new ReportConfigurationDao(em);
            return dao.findAll();
        } catch (Exception e) {
            LOGGER.warning("Error loading report configurations: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
