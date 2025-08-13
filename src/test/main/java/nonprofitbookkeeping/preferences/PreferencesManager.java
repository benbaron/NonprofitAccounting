
package nonprofitbookkeeping.preferences;

import java.util.prefs.Preferences;

/**
 * Manages application-specific preferences using the Java Preferences API.
 * This class provides static methods to get and set preferences, such as the
 * last directory accessed by a file chooser.
 * <p>
 * This class previously used the same preference key for both the last
 * directory and the last write directory. The keys are now distinct so that
 * read and write dialogs can store independent values.
 * </p>
 */
public class PreferencesManager
{
        /** Key for storing the last directory used by a file chooser for general purposes. */
        private static final String LAST_DIR_KEY = "lastFileChooserDirectory";
        /**
         * Key for storing the last directory used by a file chooser for
         * write/save operations. This is intentionally separate from
         * {@link #LAST_DIR_KEY} so that read and write dialogs can remember
         * different locations.
         */
	private static final String LAST_WRITE_DIR_KEY = "last_write_directory";
        /** The {@link Preferences} node used for storing preferences for this class. */
        private static final Preferences prefs =
                Preferences.userNodeForPackage(PreferencesManager.class);

        /**
         * Preference key used in older versions when the read and write
         * directory shared the same value. Retained so we can migrate any
         * existing preference to the new dedicated write key.
         */
        private static final String LEGACY_DIR_KEY = "lastFileChooserDirectory";

        static
        {
                // If an older installation stored a directory under the legacy
                // key but the new write key is unset, copy the value so users
                // retain their preferred write directory after upgrading.
                if (!LAST_WRITE_DIR_KEY.equals(LEGACY_DIR_KEY))
                {
                        String legacy = prefs.get(LEGACY_DIR_KEY, null);
                        String writeValue = prefs.get(LAST_WRITE_DIR_KEY, null);
                        if (legacy != null && writeValue == null)
                        {
                                prefs.put(LAST_WRITE_DIR_KEY, legacy);
                        }
                }
                // Migrate from an even older key if present
                String old = prefs.get("last_directory", null);
                if (old != null && prefs.get(LAST_DIR_KEY, null) == null)
                {
                        prefs.put(LAST_DIR_KEY, old);
                        prefs.remove("last_directory");
                }
        }
	
	/**
        * Gets the last directory path that was used by a file chooser for general
        * purposes. If no directory preference is found, it defaults to the user's
        * home directory.
        *
        * @return The last directory path as a String.
        */
	public static String getLastDirectory()
	{
		return prefs.get(LAST_DIR_KEY, System.getProperty("user.home"));
	}

	/**
        * Gets the last directory path that was used by a file chooser
        * specifically for write or save operations. If no preference is set, it
        * defaults to the user's home directory.
        *
        * @return The last write directory path as a String.
        */
	public static String getLastWriteDirectory()
	{
		return prefs.get(LAST_WRITE_DIR_KEY, System.getProperty("user.home"));
	}
	
	/**
        * Sets the last directory path used by a file chooser for general
        * purposes. This path is persisted for future sessions.
        * @param path The directory path to store. If null, the behavior depends on the underlying Preferences implementation (may throw NullPointerException).
        */
	public static void setLastDirectory(String path)
	{
		prefs.put(LAST_DIR_KEY, path);
	}

	/**
        * Sets the last directory path used by a file chooser specifically for
        * write or save operations. This path is persisted for future sessions.
        * @param path The directory path to store. If null, the behavior depends on the underlying Preferences implementation (may throw NullPointerException).
        */
	public static void setLastWriteDirectory(String path)
	{
		prefs.put(LAST_WRITE_DIR_KEY, path);
	}
}
