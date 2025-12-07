
package nonprofitbookkeeping.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nonprofitbookkeeping.model.reports.ReportConfiguration;
import nonprofitbookkeeping.persistence.DocumentRepository;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Persists {@link ReportConfiguration} definitions in the H2 database.
 *
 * <p>Historically these configurations were written to a {@code report_configurations.json}
 * file in the company directory.  The data now lives in the {@code document} table so that it is
 * part of the normal database backup process.</p>
 */
public class ReportConfigurationService
{
	
	private static final Logger LOGGER =
		Logger.getLogger(ReportConfigurationService.class.getName());
	private static final String DOCUMENT_NAME = "report_configurations";
	
	private final ObjectMapper objectMapper;
	
	public ReportConfigurationService()
	{
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
		this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		this.objectMapper
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		
	}
	
	public void saveConfigurations(List<ReportConfiguration> configs,
		File companyDirectory)
		throws IOException
	{
		
		if (configs == null)
		{
			LOGGER.warning(
				"Attempted to save a null list of report configurations. Aborting.");
			return;
		}
		
		try
		{
			String payload = this.objectMapper.writeValueAsString(configs);
			new DocumentRepository().upsert(DOCUMENT_NAME, payload);
			LOGGER.info("Report configurations saved to database document '" +
				DOCUMENT_NAME + "'.");
		}
		catch (SQLException e)
		{
			throw new IOException(
				"Failed to save report configurations to database", e);
		}
		
	}
	
	public List<ReportConfiguration> loadConfigurations(File companyDirectory)
	{		
		try
		{
			return new DocumentRepository().find(DOCUMENT_NAME)
				.map(payload ->
				{
					
					try
					{
						List<ReportConfiguration> configs =
							this.objectMapper.readValue(payload,
								new TypeReference<List<ReportConfiguration>>()
								{
								});
						LOGGER.info(
							"Report configurations loaded successfully from database document '" +
								DOCUMENT_NAME + "'.");
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
					catch (IOException ex)
					{
						LOGGER.log(Level.SEVERE,
							"Failed to deserialize report configurations JSON from database.",
							ex);
						return new ArrayList<ReportConfiguration>();
					}
					
				})
				.orElseGet(ArrayList::new);
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE,
				"Failed to load report configurations from database", e);
			return new ArrayList<>();
		}
		
	}
	
}
