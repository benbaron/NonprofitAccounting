package nonprofitbookkeeping.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/** Stores application preferences in the user's configuration directory. */
public class PreferencesService
{
    public static final String COMPANY_STARTUP_PRESELECT = "Preselect last company";
    public static final String COMPANY_STARTUP_OPEN = "Open last company automatically";
    public static final String COMPANY_STARTUP_NONE = "No automatic selection";

    private static final String PREFS_FILE_NAME = "preferences.properties";
    private static final String DEFAULT_DIR_KEY = "defaultCompanyDir";
    private static final String LAST_FILE_KEY = "lastUsedCompanyFile";
    private static final String LAST_COMPANY_ID_KEY = "lastUsedCompanyId";
    private static final String THEME_KEY = "uiTheme";
    private static final String PENDING_ROW_TEXT_COLOR_KEY =
        "pendingRowTextColor";
    private static final String JOURNAL_STORED_LINE_ORDER_KEY =
        "journalPreserveStoredLineOrder";
    private static final String DASHBOARD_RECENT_LIMIT_KEY =
        "dashboardRecentTransactionLimit";
    private static final String COMPANY_STARTUP_BEHAVIOR_KEY =
        "companyStartupBehavior";

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

    public static String getCompanyStartupBehavior()
    {
        String value = props.getProperty(COMPANY_STARTUP_BEHAVIOR_KEY,
            COMPANY_STARTUP_PRESELECT);
        if (!COMPANY_STARTUP_OPEN.equals(value) &&
            !COMPANY_STARTUP_NONE.equals(value))
        {
            return COMPANY_STARTUP_PRESELECT;
        }
        return value;
    }

    public static void setCompanyStartupBehavior(String value)
    {
        String normalized = value;
        if (!COMPANY_STARTUP_OPEN.equals(normalized) &&
            !COMPANY_STARTUP_NONE.equals(normalized))
        {
            normalized = COMPANY_STARTUP_PRESELECT;
        }
        props.setProperty(COMPANY_STARTUP_BEHAVIOR_KEY, normalized);
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

    public static boolean isJournalStoredLineOrderPreserved()
    {
        return Boolean.parseBoolean(
            props.getProperty(JOURNAL_STORED_LINE_ORDER_KEY, "true"));
    }

    public static void setJournalStoredLineOrderPreserved(boolean preserved)
    {
        props.setProperty(JOURNAL_STORED_LINE_ORDER_KEY,
            Boolean.toString(preserved));
        save();
    }

    public static int getDashboardRecentTransactionLimit()
    {
        String value = props.getProperty(DASHBOARD_RECENT_LIMIT_KEY, "10");
        try
        {
            return Math.max(1, Math.min(100, Integer.parseInt(value)));
        }
        catch (NumberFormatException ex)
        {
            return 10;
        }
    }

    public static void setDashboardRecentTransactionLimit(int limit)
    {
        int normalized = Math.max(1, Math.min(100, limit));
        props.setProperty(DASHBOARD_RECENT_LIMIT_KEY,
            Integer.toString(normalized));
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
