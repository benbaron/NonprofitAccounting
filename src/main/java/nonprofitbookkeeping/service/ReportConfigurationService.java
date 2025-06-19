
package nonprofitbookkeeping.service;

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
import jakarta.persistence.EntityManager;
=======
>>>>>>> b1f07f2 Extend SQL support
import nonprofitbookkeeping.model.reports.ReportConfiguration;
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
import nonprofitbookkeeping.persistence.DatabaseManager;
import nonprofitbookkeeping.persistence.dao.ReportConfigurationDao;
=======
import nonprofitbookkeeping.ui.helpers.DateSelectionMode;
>>>>>>> b1f07f2 Extend SQL support
=======
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // For LocalDate serialization
>>>>>>> 6159d55 Revert service changes

import nonprofitbookkeeping.model.reports.ReportConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service class for managing {@link ReportConfiguration} objects.
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
 * <p>
 * Previous iterations persisted configurations to JSON on disk using
 * {@code JacksonDataStorer}. This implementation delegates persistence to the
 * JPA layer through {@link ReportConfigurationDao} so configurations are stored
 * in the embedded database.
 * </p>
=======
 * Data is persisted using the embedded SQL database via {@link DatabaseManager}.
>>>>>>> b1f07f2 Extend SQL support
=======
 * This service handles the persistence of report configurations by saving them to
 * and loading them from a JSON file ("report_configurations.json") within a specified
 * company directory. It utilizes Jackson for JSON serialization/deserialization,
 * including support for Java Time types like {@link java.time.LocalDate}.
>>>>>>> 6159d55 Revert service changes
 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
public class ReportConfigurationService {
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file

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
=======
    private static final Logger LOGGER = Logger.getLogger(ReportConfigurationService.class.getName());

    /** Constructs a new service instance. */
    public ReportConfigurationService() {
    }

    /**
     * Saves the provided configurations to the database. Existing rows with the
     * same configuration ID will be replaced.
     */
    public void saveConfigurations(List<ReportConfiguration> configs, File companyDirectory) throws IOException {
        if (configs == null) {
            LOGGER.warning("Attempted to save a null list of report configurations. Aborting.");
            return;
        }
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "MERGE INTO report_configuration(configuration_id,user_given_name,report_type,date_selection_mode,relative_date_range,specific_start_date,specific_end_date,fund_ids,output_format,account_ids) KEY(configuration_id) VALUES(?,?,?,?,?,?,?,?,?,?)")) {
            for (ReportConfiguration rc : configs) {
                ps.setString(1, rc.getConfigurationId());
                ps.setString(2, rc.getUserGivenName());
                ps.setString(3, rc.getReportType());
                ps.setString(4, rc.getDateSelectionMode() == null ? null : rc.getDateSelectionMode().name());
                ps.setString(5, rc.getRelativeDateRange());
                ps.setDate(6, rc.getSpecificStartDate() == null ? null : Date.valueOf(rc.getSpecificStartDate()));
                ps.setDate(7, rc.getSpecificEndDate() == null ? null : Date.valueOf(rc.getSpecificEndDate()));
                ps.setString(8, listToString(rc.getFundIds()));
                ps.setString(9, rc.getOutputFormat());
                ps.setString(10, listToString(rc.getAccountIdsForDetailReport()));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving report configurations", e);
        }
    }

    /**
     * Loads all report configurations from the database.
     */
    public List<ReportConfiguration> loadConfigurations(File companyDirectory) {
        List<ReportConfiguration> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT configuration_id,user_given_name,report_type,date_selection_mode,relative_date_range,specific_start_date,specific_end_date,fund_ids,output_format,account_ids FROM report_configuration")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ReportConfiguration rc = new ReportConfiguration();
                rc.setConfigurationId(rs.getString(1));
                rc.setUserGivenName(rs.getString(2));
                rc.setReportType(rs.getString(3));
                String mode = rs.getString(4);
                if (mode != null) rc.setDateSelectionMode(DateSelectionMode.valueOf(mode));
                rc.setRelativeDateRange(rs.getString(5));
                Date sd = rs.getDate(6);
                if (sd != null) rc.setSpecificStartDate(sd.toLocalDate());
                Date ed = rs.getDate(7);
                if (ed != null) rc.setSpecificEndDate(ed.toLocalDate());
                rc.setFundIds(stringToList(rs.getString(8)));
                rc.setOutputFormat(rs.getString(9));
                rc.setAccountIdsForDetailReport(stringToList(rs.getString(10)));
                list.add(rc);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading report configurations", e);
        }
        return list;
    }

    private static String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }

    private static List<String> stringToList(String s) {
        if (s == null || s.isEmpty()) return new ArrayList<>();
        return Arrays.asList(s.split(","));
>>>>>>> b1f07f2 Extend SQL support
    }
=======
public class ReportConfigurationService
{
	
	/** Logger for this class. */
	private static final Logger LOGGER =
		Logger.getLogger(ReportConfigurationService.class.getName());
	/** The standard filename for storing report configurations in JSON format. */
	private static final String CONFIG_FILE_NAME = "report_configurations.json";
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
		this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Or configure
																					// date format
																					// if needed
	}
	
	/**
	 * Saves a list of {@link ReportConfiguration} objects to a JSON file
	 * named "report_configurations.json" within the specified company directory.
	 * If the provided list of configurations is null, the method logs a warning and returns without saving.
	 * If the list is empty, an empty JSON array will be saved.
	 *
	 * @param configs The list of {@link ReportConfiguration} objects to save.
	 * @param companyDirectory The {@link File} object representing the directory where
	 *                         the configurations file should be stored. Must not be null and must be a valid directory.
	 * @throws IOException If the {@code companyDirectory} is invalid, or if an error occurs
	 *                     during file writing or JSON serialization.
	 */
	public void saveConfigurations(	List<ReportConfiguration> configs,
									File companyDirectory) throws IOException
	{
		
		if (configs == null)
		{
			LOGGER.warning("Attempted to save a null list of report configurations. Aborting.");
			// Optionally, save an empty list instead: configs = new ArrayList<>();
			return;
		}
		
		if (companyDirectory == null || !companyDirectory.isDirectory())
		{
			LOGGER.severe("Invalid company directory provided for saving report configurations: " + // Consider using Level.WARNING for non-fatal issues if not throwing.
				(companyDirectory != null ? companyDirectory.getAbsolutePath() : "null"));
			throw new IOException("Invalid company directory for saving report configurations.");
		}
		
		File configFile = new File(companyDirectory, CONFIG_FILE_NAME);
		
		try
		{
			this.objectMapper.writeValue(configFile, configs);
			LOGGER.info("Report configurations saved to: " + configFile.getAbsolutePath());
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE,
				"Error saving report configurations to " + configFile.getAbsolutePath(), e);
			throw e; // Re-throw to allow caller to handle
		}
		
	}
	
	/**
	 * Loads a list of {@link ReportConfiguration} objects from the "report_configurations.json"
	 * file located within the specified company directory.
	 * <p>
	 * If the {@code companyDirectory} is invalid, or if the configuration file does not exist,
	 * is not a file, or is empty, an empty list is returned and appropriate messages are logged.
	 * If an error occurs during JSON deserialization, an error is logged, and an empty list is returned.
	 * This method also logs a warning if a loaded configuration has a missing or empty ID.
	 * </p>
	 *
	 * @param companyDirectory The {@link File} object representing the directory where
	 *                         the "report_configurations.json" file is located.
	 *                         Must not be null and must be a valid directory.
	 * @return A {@code List<ReportConfiguration>} objects. Returns an empty list if the file
	 *         is not found, is empty, or if an error occurs during loading or parsing.
	 */
	public List<ReportConfiguration> loadConfigurations(File companyDirectory)
	{
		
		if (companyDirectory == null || !companyDirectory.isDirectory())
		{
			LOGGER
				.warning("Invalid company directory provided for loading report configurations: " +
					(companyDirectory != null ? companyDirectory.getAbsolutePath() : "null"));
			return new ArrayList<>();
		}
		
		File configFile = new File(companyDirectory, CONFIG_FILE_NAME);
		
		if (!configFile.exists() || !configFile.isFile() || configFile.length() == 0)
		{
			LOGGER.info(
				"Report configurations file not found or is empty. Returning empty list. Path: " +
					configFile.getAbsolutePath());
			return new ArrayList<>();
		}
		
		try
		{
			List<ReportConfiguration> configs =
				this.objectMapper.readValue(configFile, new TypeReference<List<ReportConfiguration>>()
				{
				});
			LOGGER.info(
				"Report configurations loaded successfully from: " + configFile.getAbsolutePath());
			// Ensure IDs are present (though constructor should handle for new ones)
			configs.forEach(config -> {
				
				if (config.getConfigurationId() == null ||
					config.getConfigurationId().trim().isEmpty())
				{
					// This case should ideally not happen if configs are always saved with IDs.
					// For robustness, could assign a new one, but log a warning as it implies data
					// issue.
					LOGGER.warning(
						"Loaded a ReportConfiguration with a missing or empty ID. UserGivenName: " +
							config.getUserGivenName());
					// Example: if(config.getConfigurationId() == null)
					// config.setConfigurationId(UUID.randomUUID().toString());
					// However, modifying loaded data might be unexpected. Logging is safer.
				}
				
			});
			return configs;
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE,
				"Error loading report configurations from " + configFile.getAbsolutePath(), e);
			return new ArrayList<>(); // Return empty list on error
		}
		
	}
	
>>>>>>> 6159d55 Revert service changes
}
