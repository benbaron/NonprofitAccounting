
package nonprofitbookkeeping.service;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class PreferencesService
{
	private static final String PREFS_FILE_NAME = "preferences.properties";
	private static final String DEFAULT_DIR_KEY = "defaultCompanyDir";
	private static final String LAST_FILE_KEY = "lastUsedCompanyFile";
	
	private static final Properties props = new Properties();
	private static final Path configPath =
		Paths.get(System.getProperty("user.home"), ".nonprofitbookkeeping", PREFS_FILE_NAME);
	
	static
	{
		
		try
		{
			Files.createDirectories(configPath.getParent());
			
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
			e.printStackTrace();
		}
		
	}
	
	public static String getDefaultCompanyDir()
	{
		return props.getProperty(DEFAULT_DIR_KEY,
			Paths.get(System.getProperty("user.home"), "NonprofitBookkeeping").toString());
	}
	
	public static void setDefaultCompanyDir(String path)
	{
		props.setProperty(DEFAULT_DIR_KEY, path);
		save();
	}
	
	public static String getLastUsedCompanyFile()
	{
		return props.getProperty(LAST_FILE_KEY, "");
	}
	
	public static void setLastUsedCompanyFile(String filePath)
	{
		props.setProperty(LAST_FILE_KEY, filePath);
		save();
	}
	
	private static void save()
	{		
		try (OutputStream out = Files.newOutputStream(configPath))
		{
			props.store(out, "Nonprofit Bookkeeping Preferences");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
}
