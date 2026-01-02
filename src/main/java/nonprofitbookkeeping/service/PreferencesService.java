
package nonprofitbookkeeping.service;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Manages application preferences, such as default directories and last used files.
 * Preferences are stored in a ".properties" file located in a ".nonprofitbookkeeping"
 * directory within the user's home directory.
 * This class uses static methods to provide global access to preference settings.
 */
public class PreferencesService
{
	/** The name of the preferences file. */
	private static final String PREFS_FILE_NAME = "preferences.properties";
	/** Key for storing the default directory for company files. */
	private static final String DEFAULT_DIR_KEY = "defaultCompanyDir";
	/** Key for storing the path of the last used company file. */
	private static final String LAST_FILE_KEY = "lastUsedCompanyFile";
	/** Key for storing the identifier of the last opened company. */
	private static final String LAST_COMPANY_ID_KEY = "lastUsedCompanyId";
	
	/** The {@link Properties} object used to store and manage preferences. */
	private static final Properties props = new Properties();
	/** The {@link Path} to the preferences file. */
	private static final Path configPath =
		Paths.get(System.getProperty("user.home"), ".nonprofitbookkeeping",
			PREFS_FILE_NAME);
	
	/**
	 * Static initializer block to load preferences from the properties file when the class is loaded.
	 * It ensures the configuration directory exists and attempts to load existing preferences.
	 * If an {@link IOException} occurs during this process, it is printed to the error stream,
	 * and the application will proceed with default or empty preferences.
	 */
	static
	{
		
		try
		{
			Files.createDirectories(configPath.getParent()); // Ensure
																// .nonprofitbookkeeping
																// directory
																// exists
			
			if (Files.exists(configPath))
			{
				
				try (InputStream in = Files.newInputStream(configPath))
				{
					props.load(in);
				}
				
			}
			
		}
		catch (IOException e)
		{
			// Consider logging this more formally or handling it based on
			// application requirements
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Gets the default directory where company files are stored.
	 * If no default directory preference is set, it defaults to a "NonprofitBookkeeping"
	 * subdirectory within the user's home directory.
	 *
	 * @return The default company directory path as a String.
	 */
	public static String getDefaultCompanyDir()
	{
		return props.getProperty(DEFAULT_DIR_KEY,
			Paths.get(System.getProperty("user.home"), "NonprofitBookkeeping")
				.toString());
		
	}
	
	/**
	 * Sets the default directory for company files and saves this preference.
	 *
	 * @param path The directory path to set as the default. If null, the behavior
	 *             of {@link Properties#setProperty(String, String)} for null values will apply (may throw NPE).
	 */
	public static void setDefaultCompanyDir(String path)
	{
		props.setProperty(DEFAULT_DIR_KEY, path);
		save();
		
	}
	
	/**
	 * Gets the path of the last used company file.
	 * If no last used file preference is set, it defaults to an empty string.
	 *
	 * @return The path of the last used company file as a String, or an empty string if not set.
	 */
	public static String getLastUsedCompanyFile()
	{
		return props.getProperty(LAST_FILE_KEY, "");
		
	}
	
	/**
	 * Sets the path of the last used company file and saves this preference.
	 *
	 * @param filePath The file path to set as the last used. If null, the behavior
	 *                 of {@link Properties#setProperty(String, String)} for null values will apply (may throw NPE).
	 */
	public static void setLastUsedCompanyFile(String filePath)
	{
		
		if (filePath == null)
		{
			props.remove(LAST_FILE_KEY);
		}
		else
		{
			props.setProperty(LAST_FILE_KEY, filePath);
		}
		
		save();
		
	}
	
	/**
	 * Returns the identifier of the last company that was opened, if available.
	 */
	public static Long getLastUsedCompanyId()
	{
		String value = props.getProperty(LAST_COMPANY_ID_KEY);
		
		if (value == null || value.isBlank())
		{
			return null;
		}
		
		try
		{
			return Long.parseLong(value);
		}
		catch (NumberFormatException ex)
		{
			return null;
		}
		
	}
	
	/**
	 * Stores the identifier of the most recently opened company.
	 */
	public static void setLastUsedCompanyId(Long companyId)
	{
		
		if (companyId == null)
		{
			props.remove(LAST_COMPANY_ID_KEY);
		}
		else
		{
			props.setProperty(LAST_COMPANY_ID_KEY, Long.toString(companyId));
		}
		
		save();
		
	}
	
	/**
	 * Saves the current state of the {@link #props} object to the preferences file defined by {@link #configPath}.
	 * If an {@link IOException} occurs during saving, it is printed to the error stream.
	 */
	private static void save()
	{
		
		try (OutputStream out = Files.newOutputStream(configPath))
		{
			props.store(out, "Nonprofit Bookkeeping Preferences");
		}
		catch (IOException e)
		{
			// Consider logging this more formally or handling it based on
			// application requirements
			e.printStackTrace();
		}
		
	}
	
	/** Singleton instance of the service. */
	private static final PreferencesService INSTANCE = new PreferencesService();
	
	/** Private constructor to prevent external instantiation. */
	private PreferencesService()
	{
	
	}
	
	/**
	 * @return
	 */
	public static PreferencesService getInstance()
	{
		return INSTANCE;
		
	}
	
}
