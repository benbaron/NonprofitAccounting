
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		LoggerFactory.getLogger(ReportConfigurationService.class);
	private static final String DOCUMENT_NAME = "report_configurations";
	
	private final ObjectMapper objectMapper;
	
	/**
	 * Creates a service instance with an {@link ObjectMapper} configured for Java time
	 * serialization and human-readable output.
	 */
	public ReportConfigurationService()
	{
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
		this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		this.objectMapper
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		
	}
	
	/**
	 * Persists the supplied report configurations to the {@code document} table. The
	 * {@code companyDirectory} argument is retained for backward compatibility but ignored.
	 *
	 * @param configs           report configurations to save; a {@code null} collection is
	 *                          ignored after logging a warning
	 * @param companyDirectory  unused legacy parameter preserved to avoid API churn
	 * @throws IOException if serialization fails or the database cannot be updated
	 */
	public void saveConfigurations(List<ReportConfiguration> configs,
		File companyDirectory)
		throws IOException
	{
		
		if (configs == null)
		{
			LOGGER.warn(
				"Attempted to save a null list of report configurations. Aborting.");
			return;
		}
		
		try
		{
			String payload = this.objectMapper.writeValueAsString(configs);
			new DocumentRepository().upsert(DOCUMENT_NAME, payload);
			LOGGER.info(
				"Report configurations saved to database document '{}'.",
				DOCUMENT_NAME);
		}
		catch (SQLException e)
		{
			throw new IOException(
				"Failed to save report configurations to database", e);
		}
		
	}
	
	/**
	 * Loads all stored report configurations from the {@code document} table. Invalid records
	 * (such as those missing an ID) are logged but returned to the caller for further handling.
	 * The {@code companyDirectory} argument is retained for backward compatibility but ignored.
	 *
	 * @param companyDirectory unused legacy parameter preserved to avoid API churn
	 * @return list of {@link ReportConfiguration} objects; never {@code null}
	 */
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
							"Report configurations loaded successfully from database document '{}'.",
							DOCUMENT_NAME);
						configs.forEach(config -> {
							
							if (config.getConfigurationId() == null ||
								config.getConfigurationId().trim().isEmpty())
							{
								LOGGER.warn(
									"Loaded a ReportConfiguration with a missing or empty ID. UserGivenName: {}",
									config.getUserGivenName());
							}
							
						});
						return configs;
					}
					catch (IOException ex)
					{
						LOGGER.error(
							"Failed to deserialize report configurations JSON from database.",
							ex);
						return new ArrayList<ReportConfiguration>();
					}
					
				})
				.orElseGet(() -> new ArrayList<ReportConfiguration>());
		}
		catch (SQLException e)
		{
			LOGGER.error("Failed to load report configurations from database", e);
			return new ArrayList<>();
		}
		
	}
	
}
