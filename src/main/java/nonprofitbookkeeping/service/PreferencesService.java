package nonprofitbookkeeping.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Manages application preferences, such as default directories and last used
 * files. Preferences are stored in a properties file under the user's home
 * directory and are globally accessible to UI components.
 */
public class PreferencesService
{
    private static final String PREFS_FILE_NAME = "preferences.properties";
    private static final String DEFAULT_DIR_KEY = "defaultCompanyDir";
    private static final String LAST_FILE_KEY = "lastUsedCompanyFile";
    private static final String LAST_COMPANY_ID_KEY = "lastUsedCompanyId";
    private static final String THEME_KEY = "uiTheme";
    private static final String PENDING_ROW_TEXT_COLOR_KEY =
        "pendingRowTextColor";
    private static final String JOURNAL_STORED_LINE_ORDER_KEY =
        "journalPreserveStoredLineOrder";

    private static final Properties props = new Properties();
    private static final Path configPath = Paths.get(
        System.getProperty("user.home"), ".nonprofitbookkeeping",
        PREFS_FILE_NAME);

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
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static String getDefaultCompanyDir()
    {
        return props.getProperty(DEFAULT_DIR_KEY,
            Paths.get(System.getProperty("user.home"),
                "NonprofitBookkeeping").toString());
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

    public static void setLastUsedCompanyId(Long companyId)
    {
        if (companyId == null)
        {
            props.remove(LAST_COMPANY_ID_KEY);
        }
        else
        {
            props.setProperty(LAST_COMPANY_ID_KEY,
                Long.toString(companyId));
        }
        save();
    }

    public static String getThemePreference()
    {
        return props.getProperty(THEME_KEY, "System");
    }

    public static void setThemePreference(String theme)
    {
        if (theme == null || theme.isBlank())
        {
            props.remove(THEME_KEY);
        }
        else
        {
            props.setProperty(THEME_KEY, theme);
        }
        save();
    }

    public static String getPendingRowTextColorPreference()
    {
        return props.getProperty(PENDING_ROW_TEXT_COLOR_KEY, "Black");
    }

    public static void setPendingRowTextColorPreference(String value)
    {
        if (value == null || value.isBlank())
        {
            props.remove(PENDING_ROW_TEXT_COLOR_KEY);
        }
        else
        {
            props.setProperty(PENDING_ROW_TEXT_COLOR_KEY, value);
        }
        save();
    }

    /**
     * Returns whether journal transaction lines should retain their stored
     * debit/credit order. The default is {@code true}.
     */
    public static boolean isJournalStoredLineOrderPreserved()
    {
        return Boolean.parseBoolean(
            props.getProperty(JOURNAL_STORED_LINE_ORDER_KEY, "true"));
    }

    /**
     * Sets whether journal lines retain stored order. When false, journal
     * displays may group debit lines before credit lines.
     */
    public static void setJournalStoredLineOrderPreserved(boolean preserved)
    {
        props.setProperty(JOURNAL_STORED_LINE_ORDER_KEY,
            Boolean.toString(preserved));
        save();
    }

    private static void save()
    {
        try (OutputStream out = Files.newOutputStream(configPath))
        {
            props.store(out, "Nonprofit Bookkeeping Preferences");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private static final PreferencesService INSTANCE =
        new PreferencesService();

    private PreferencesService()
    {
    }

    public static PreferencesService getInstance()
    {
        return INSTANCE;
    }
}
