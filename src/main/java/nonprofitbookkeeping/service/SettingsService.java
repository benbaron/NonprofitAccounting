
package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.persistence.JsonStorageRepository;

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
        /** Storage key used inside the database. */
        private static final String STORAGE_KEY = "settings";
	
	/** Settings cached in memory. */
	private SettingsModel settings = new SettingsModel();
	
	/** Returns the current settings instance. */
	public SettingsModel getSettings()
	{
		return this.settings;
	}
	
        /**
         * Loads settings from the shared H2 database. The company directory parameter is
         * retained for compatibility with the legacy file-based implementation.
         *
         * @param companyDir unused legacy parameter
         * @throws IOException if the database cannot be queried
         */
        public void loadSettings(File companyDir) throws IOException
        {

                ObjectMapper mapper = new ObjectMapper();

                try
                {
                        this.settings = new JsonStorageRepository().load(STORAGE_KEY)
                                .filter(payload -> !payload.isBlank())
                                .map(payload -> {
                                        try
                                        {
                                                return mapper.readValue(payload, SettingsModel.class);
                                        }
                                        catch (IOException ex)
                                        {
                                                LOGGER.log(Level.SEVERE,
                                                        "Failed to parse settings payload from H2 database.", ex);
                                                return new SettingsModel();
                                        }
                                })
                                .orElseGet(SettingsModel::new);
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to load settings from H2 database", e);
                }

        }
	
        /**
         * Saves the current settings to the shared H2 database.
         *
         * @param companyDir unused legacy parameter
         * @throws IOException if the database cannot be updated
         */
        public void saveSettings(File companyDir) throws IOException
        {

                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);

                try
                {
                        String payload = mapper.writeValueAsString(this.settings);
                        new JsonStorageRepository().save(STORAGE_KEY, payload);
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to save settings to H2 database", e);
                }

        }
	
}
