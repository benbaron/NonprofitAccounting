
package nonprofitbookkeeping.preferences;

import java.util.prefs.Preferences;

/**
 * Manages application preferences.
 * This class currently stores the last directory used by a file chooser.
 */
public class PreferencesManager
{
	private static final String LAST_DIR_KEY = "lastFileChooserDirectory";
	private static final String LAST_WRITE_DIR_KEY = "lastFileChooserDirectory";
	private static final Preferences prefs =
		Preferences.userNodeForPackage(PreferencesManager.class);
	
	/**
	 * Gets the last directory that was used.
	 * Defaults to the user's home directory if not set.
	 *
	 * @return the last directory path
	 */
	public static String getLastDirectory()
	{
		return prefs.get(LAST_DIR_KEY, System.getProperty("user.home"));
	}
	/**
	 * Gets the last directory that was used.
	 * Defaults to the user's home directory if not set.
	 *
	 * @return the last directory path
	 */
	public static String getLastWriteDirectory()
	{
		return prefs.get(LAST_WRITE_DIR_KEY, System.getProperty("user.home"));
	}
	
	/**
	 * Sets the last directory used.
	 *
	 * @param path the directory path to store
	 */
	public static void setLastDirectory(String path)
	{
		prefs.put(LAST_DIR_KEY, path);
	}
	/**
	 * Sets the last directory used.
	 *
	 * @param path the directory path to store
	 */
	public static void setLastWriteDirectory(String path)
	{
		prefs.put(LAST_WRITE_DIR_KEY, path);
	}
}
