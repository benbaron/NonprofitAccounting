
package nonprofitbookkeeping.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // For LocalDate serialization

import nonprofitbookkeeping.model.reports.ReportConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportConfigurationService
{
	
	private static final Logger LOGGER =
		Logger.getLogger(ReportConfigurationService.class.getName());
	private static final String CONFIG_FILE_NAME = "report_configurations.json";
	private final ObjectMapper objectMapper;
	
	public ReportConfigurationService()
	{
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule()); // Essential for LocalDate
		this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Or configure
																					// date format
																					// if needed
	}
	
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
			LOGGER.severe("Invalid company directory provided for saving report configurations: " +
				companyDirectory);
			throw new IOException("Invalid company directory for saving report configurations.");
		}
		
		File configFile = new File(companyDirectory, CONFIG_FILE_NAME);
		
		try
		{
			objectMapper.writeValue(configFile, configs);
			LOGGER.info("Report configurations saved to: " + configFile.getAbsolutePath());
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE,
				"Error saving report configurations to " + configFile.getAbsolutePath(), e);
			throw e; // Re-throw to allow caller to handle
		}
		
	}
	
	public List<ReportConfiguration> loadConfigurations(File companyDirectory)
	{
		
		if (companyDirectory == null || !companyDirectory.isDirectory())
		{
			LOGGER
				.warning("Invalid company directory provided for loading report configurations: " +
					companyDirectory);
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
				objectMapper.readValue(configFile, new TypeReference<List<ReportConfiguration>>()
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
	
}
