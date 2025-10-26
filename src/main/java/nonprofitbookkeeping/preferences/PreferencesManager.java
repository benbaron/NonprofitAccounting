
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
	private static final String LAST_DIR_KEY = "last_directory";
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
        /** Key for storing the last selected database file location. */
        private static final String LAST_DATABASE_PATH_KEY = "last_database_path";

        static
        {
                // The application previously stored both read and write
                // directories under the same key.  If that value exists but the
                // new dedicated write key is empty, copy the existing value so
                // the preference is preserved after upgrading.
                String shared = prefs.get(LAST_DIR_KEY, null);
                if (shared != null && prefs.get(LAST_WRITE_DIR_KEY, null) == null)
                {
                        prefs.put(LAST_WRITE_DIR_KEY, shared);
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

        /**
        * Returns the last database file path that the user selected when opening or creating
        * an H2 database. The stored value includes the file name, typically ending with
        * {@code .mv.db}. If no preference has been stored this method returns {@code null}.
        *
        * @return the previously selected database file path, or {@code null} if none recorded.
        */
        public static String getLastDatabasePath()
        {
                return prefs.get(LAST_DATABASE_PATH_KEY, null);
        }

        /**
        * Persists the path of the last opened or created database file so the application can
        * offer it as the default location the next time the chooser is displayed. Supplying
        * a {@code null} or blank value clears the stored preference.
        *
        * @param path absolute path to the database file (usually ending in {@code .mv.db}).
        */
        public static void setLastDatabasePath(String path)
        {
                if (path == null || path.trim().isEmpty())
                {
                        prefs.remove(LAST_DATABASE_PATH_KEY);
                }
                else
                {
                        prefs.put(LAST_DATABASE_PATH_KEY, path);
                }
        }
}
