package nonprofitbookkeeping.service;

import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.prefs.Preferences;

import nonprofitbookkeeping.preferences.PreferencesManager;

/**
 * Stores UI lifecycle metadata that is not part of the accounting aggregate.
 * Values are scoped by database path and company id.
 */
public final class CompanyUiMetadataStore
{
    private static final Preferences PREFS = Preferences.userNodeForPackage(
        CompanyUiMetadataStore.class);

    private CompanyUiMetadataStore()
    {
    }

    public static Instant getLastOpened(long companyId)
    {
        String value = PREFS.get(key(companyId, "lastOpened"), "");
        if (value.isBlank())
        {
            return null;
        }
        try
        {
            return Instant.parse(value);
        }
        catch (DateTimeParseException ex)
        {
            return null;
        }
    }

    public static void setLastOpened(long companyId, Instant instant)
    {
        if (instant == null)
        {
            PREFS.remove(key(companyId, "lastOpened"));
        }
        else
        {
            PREFS.put(key(companyId, "lastOpened"), instant.toString());
        }
    }

    public static boolean isArchived(long companyId)
    {
        return PREFS.getBoolean(key(companyId, "archived"), false);
    }

    public static void setArchived(long companyId, boolean archived)
    {
        PREFS.putBoolean(key(companyId, "archived"), archived);
    }

    public static void remove(long companyId)
    {
        PREFS.remove(key(companyId, "lastOpened"));
        PREFS.remove(key(companyId, "archived"));
    }

    private static String key(long companyId, String field)
    {
        String database = PreferencesManager.getLastDatabasePath();
        String normalized = database == null ? "default" :
            Path.of(database).toAbsolutePath().normalize().toString();
        return Integer.toHexString(normalized.hashCode()) + ".company." +
            companyId + "." + field;
    }
}
