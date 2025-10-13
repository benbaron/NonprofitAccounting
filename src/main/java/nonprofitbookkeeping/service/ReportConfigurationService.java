
package nonprofitbookkeeping.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // For LocalDate serialization

import nonprofitbookkeeping.model.reports.ReportConfiguration;
import nonprofitbookkeeping.persistence.JsonStorageRepository;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for managing {@link ReportConfiguration} objects backed by the shared H2 database.
 * Configurations are persisted as JSON payloads inside the {@code json_storage} table while
 * retaining compatibility with the legacy API that accepted a company directory.
 * Jackson handles serialization/deserialization, including support for Java Time types like
 * {@link java.time.LocalDate}.
 */
public class ReportConfigurationService
{
	
	/** Logger for this class. */
	private static final Logger LOGGER =
		Logger.getLogger(ReportConfigurationService.class.getName());
        /** Storage key used for persisting configurations inside H2. */
        private static final String STORAGE_KEY = "report_configurations";
	/** Jackson ObjectMapper instance used for JSON serialization and deserialization. */
	private final ObjectMapper objectMapper;
	
	/**
	 * Constructs a new {@code ReportConfigurationService}.
	 * Initializes and configures a Jackson {@link ObjectMapper} with:
	 * <ul>
	 *   <li>Registration of {@link JavaTimeModule} for proper serialization/deserialization of Java 8 Date/Time types.</li>
	 *   <li>Enabled indented output for pretty-printed JSON.</li>
	 *   <li>Disabled writing of dates as timestamps (prefers ISO-8601 string format due to JavaTimeModule).</li>
	 * </ul>
	 */
	public ReportConfigurationService()
	{
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule()); // Essential for LocalDate
		this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}
	
        /**
         * Saves a list of {@link ReportConfiguration} objects into the shared H2 database.
         * If the provided list is {@code null}, the existing payload is removed.
         *
         * @param configs the list of {@link ReportConfiguration} objects to save
         * @param companyDirectory unused legacy parameter retained for API compatibility
         * @throws IOException if serialization fails or the database cannot be updated
         */
        public void saveConfigurations( List<ReportConfiguration> configs,
                                                                        File companyDirectory) throws IOException
        {

                if (configs == null)
                {
                        LOGGER.warning("Attempted to save a null list of report configurations. Aborting.");
                        try
                        {
                                new JsonStorageRepository().delete(STORAGE_KEY);
                        }
                        catch (SQLException e)
                        {
                                throw new IOException("Failed to clear report configurations in H2 database", e);
                        }
                        return;
                }

                try
                {
                        String payload = this.objectMapper.writeValueAsString(configs);
                        new JsonStorageRepository().save(STORAGE_KEY, payload);
                        LOGGER.info("Report configurations saved to H2 database.");
                }
                catch (IOException e)
                {
                        LOGGER.log(Level.SEVERE,
                                "Error serializing report configurations for H2 storage", e);
                        throw e; // Re-throw to allow caller to handle
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to save report configurations to H2 database", e);
                }

        }
	
        /**
         * Loads all stored report configurations from the H2 database.
         * The optional {@code companyDirectory} parameter is ignored but retained for API
         * compatibility with previous file-based implementations.
         *
         * @param companyDirectory historical parameter that is no longer used
         * @return a list of persisted configurations or an empty list when none exist
         */
        public List<ReportConfiguration> loadConfigurations(File companyDirectory)
        {

                try
                {
                        return new JsonStorageRepository().load(STORAGE_KEY)
                                .filter(payload -> !payload.isBlank())
                                .map(payload -> {
                                        try
                                        {
                                                List<ReportConfiguration> configs =
                                                        this.objectMapper.readValue(payload,
                                                                new TypeReference<List<ReportConfiguration>>()
                                                                {
                                                                });
                                                LOGGER.info("Report configurations loaded successfully from H2 database.");
                                                configs.forEach(config -> {

                                                        if (config.getConfigurationId() == null ||
                                                                config.getConfigurationId().trim().isEmpty())
                                                        {
                                                                LOGGER.warning(
                                                                        "Loaded a ReportConfiguration with a missing or empty ID. UserGivenName: " +
                                                                                config.getUserGivenName());
                                                        }

                                                });
                                                return configs;
                                        }
                                        catch (IOException e)
                                        {
                                                LOGGER.log(Level.SEVERE,
                                                        "Error parsing report configurations from H2 payload", e);
                                                return new ArrayList<ReportConfiguration>();
                                        }
                                })
                                .orElseGet(ArrayList::new);
                }
                catch (SQLException e)
                {
                        throw new RuntimeException("Failed to load report configurations from H2 database", e);
                }

        }
}
