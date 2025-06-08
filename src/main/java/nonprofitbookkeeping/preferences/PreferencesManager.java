
package nonprofitbookkeeping.preferences;

import java.util.prefs.Preferences;

/**
 * Manages application-specific preferences using the Java Preferences API.
 * This class provides static methods to get and set preferences, such as the
 * last directory accessed by a file chooser.
 * <p>
 * Note: There appears to be a duplication in keys (`LAST_DIR_KEY` and `LAST_WRITE_DIR_KEY`
 * are the same), meaning methods related to "last directory" and "last write directory"
 * will operate on the exact same preference value.
 * </p>
 */
public class PreferencesManager
{
	/** Key for storing the last directory used by a file chooser for general purposes. */
	private static final String LAST_DIR_KEY = "lastFileChooserDirectory";
	/**
	 * Key for storing the last directory used by a file chooser for write/save operations.
	 * Currently, this is identical to {@link #LAST_DIR_KEY}, leading to an overlap.
	 */
	private static final String LAST_WRITE_DIR_KEY = "last_write_directory";
	/** The {@link Preferences} node used for storing preferences for this class. */
	private static final Preferences prefs =
		Preferences.userNodeForPackage(PreferencesManager.class);
	
	/**
	 * Gets the last directory path that was used by a file chooser for general purposes.
	 * If no directory preference is found, it defaults to the user's home directory.
	 * <p>
	 * Note: Due to {@code LAST_DIR_KEY} and {@code LAST_WRITE_DIR_KEY} being identical,
	 * this method currently retrieves the same value as {@link #getLastWriteDirectory()}.
	 * </p>
	 * @return The last directory path as a String.
	 */
	public static String getLastDirectory()
	{
		return prefs.get(LAST_DIR_KEY, System.getProperty("user.home"));
	}

	/**
	 * Gets the last directory path that was used by a file chooser, specifically intended for write/save operations.
	 * If no directory preference is found, it defaults to the user's home directory.
	 * <p>
	 * Note: Due to {@code LAST_DIR_KEY} and {@code LAST_WRITE_DIR_KEY} being identical,
	 * this method currently retrieves the same value as {@link #getLastDirectory()}.
	 * </p>
	 * @return The last write directory path as a String.
	 */
	public static String getLastWriteDirectory()
	{
		return prefs.get(LAST_WRITE_DIR_KEY, System.getProperty("user.home"));
	}
	
	/**
	 * Sets the last directory path used by a file chooser for general purposes.
	 * This path is persisted for future sessions.
	 * <p>
	 * Note: Due to {@code LAST_DIR_KEY} and {@code LAST_WRITE_DIR_KEY} being identical,
	 * this method currently sets the same preference value as {@link #setLastWriteDirectory(String)}.
	 * </p>
	 * @param path The directory path to store. If null, the behavior depends on the underlying Preferences implementation (may throw NullPointerException).
	 */
	public static void setLastDirectory(String path)
	{
		prefs.put(LAST_DIR_KEY, path);
	}

	/**
	 * Sets the last directory path used by a file chooser, specifically intended for write/save operations.
	 * This path is persisted for future sessions.
	 * <p>
	 * Note: Due to {@code LAST_DIR_KEY} and {@code LAST_WRITE_DIR_KEY} being identical,
	 * this method currently sets the same preference value as {@link #setLastDirectory(String)}.
	 * </p>
	 * @param path The directory path to store. If null, the behavior depends on the underlying Preferences implementation (may throw NullPointerException).
	 */
	public static void setLastWriteDirectory(String path)
	{
		prefs.put(LAST_WRITE_DIR_KEY, path);
	}
}
