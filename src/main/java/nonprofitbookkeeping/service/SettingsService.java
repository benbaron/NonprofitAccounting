
package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.persistence.DocumentRepository;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for loading and saving application settings.
 */
public class SettingsService
{
	private static final Logger LOGGER = Logger.getLogger(SettingsService.class.getName());
        /** Database document name where settings are persisted. */
        private static final String DOCUMENT_NAME = "settings";
        private static final ObjectMapper MAPPER = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
	
	/** Settings cached in memory. */
	private SettingsModel settings = new SettingsModel();
	
	/** Returns the current settings instance. */
	public SettingsModel getSettings()
	{
		return this.settings;
	}
	
        /**
         * Loads settings from the persistent document store.
         *
         * @param companyDir retained for backwards compatibility but ignored by the method
         * @throws IOException if reading from the database fails
         */
	public void loadSettings(File companyDir) throws IOException
	{
		
                try
                {
                        new DocumentRepository().find(DOCUMENT_NAME)
                                .ifPresent(payload -> {
                                        try
                                        {
                                                this.settings = MAPPER.readValue(payload, SettingsModel.class);
                                                LOGGER.info("Settings loaded from database document '" + DOCUMENT_NAME
                                                        + "'.");
                                        }
                                        catch (IOException ex)
                                        {
                                                LOGGER.log(Level.SEVERE,
                                                        "Failed to deserialize settings JSON from database", ex);
                                        }
                                });
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to load settings from database", e);
                }

        }
	
        /**
         * Saves current settings to the persistent document store.
         *
         * @param companyDir retained for backwards compatibility but ignored by the method
         * @throws IOException if the database write fails
         */
	public void saveSettings(File companyDir) throws IOException
	{
		
                try
                {
                        String payload = MAPPER.writeValueAsString(this.settings);
                        new DocumentRepository().upsert(DOCUMENT_NAME, payload);
                        LOGGER.info("Settings saved to database document '" + DOCUMENT_NAME + "'.");
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to save settings to database", e);
                }

        }

}
