
package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nonprofitbookkeeping.model.SettingsModel;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for loading and saving application settings.
 */
public class SettingsService
{
	private static final Logger LOGGER = Logger.getLogger(SettingsService.class.getName());
	/** File name where settings are persisted. */
	private static final String SETTINGS_FILE = "settings.json";
	
	/** Settings cached in memory. */
	private SettingsModel settings = new SettingsModel();
	
	/** Returns the current settings instance. */
	public SettingsModel getSettings()
	{
		return this.settings;
	}
	
	/**
	 * Loads settings from the given company directory. If the file does not
	 * exist, the in-memory settings remain empty.
	 *
	 * @param companyDir directory containing the settings file
	 * @throws IOException if the directory is invalid or read fails
	 */
	public void loadSettings(File companyDir) throws IOException
	{
		
		if (companyDir == null || !companyDir.isDirectory())
		{
			throw new IOException("Company directory is invalid or not provided.");
		}
		
		File target = new File(companyDir, SETTINGS_FILE);
		
		if (!target.exists() || target.length() == 0)
		{
			return; // nothing to load
		}
		
		ObjectMapper mapper = new ObjectMapper();
		
		try
		{
			this.settings = mapper.readValue(target, SettingsModel.class);
		}
		catch (IOException ex)
		{
			LOGGER.log(Level.SEVERE, "Failed to load settings from " + target.getAbsolutePath(),
				ex);
			throw ex;
		}
		
	}
	
	/**
	 * Saves current settings to the given company directory.
	 *
	 * @param companyDir directory to store the settings file
	 * @throws IOException if write fails or directory invalid
	 */
	public void saveSettings(File companyDir) throws IOException
	{
		
		if (companyDir == null || !companyDir.isDirectory())
		{
			throw new IOException("Company directory is invalid or not provided.");
		}
		
		File target = new File(companyDir, SETTINGS_FILE);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		try
		{
			mapper.writeValue(target, this.settings);
		}
		catch (IOException ex)
		{
			LOGGER.log(Level.SEVERE, "Failed to save settings to " + target.getAbsolutePath(), ex);
			throw ex;
		}
		
	}
	
}
