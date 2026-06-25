package nonprofitbookkeeping.service;

import java.util.prefs.Preferences;

/** Stores the user's company-selection behavior at application startup. */
public final class CompanyStartupPreferenceStore
{
    public enum StartupBehavior
    {
        PRESELECT_LAST("Preselect last company"),
        AUTO_OPEN_LAST("Automatically open last company"),
        ALWAYS_ASK("Always ask");

        private final String label;

        StartupBehavior(String label)
        {
            this.label = label;
        }

        @Override
        public String toString()
        {
            return this.label;
        }
    }

    private static final Preferences PREFS = Preferences.userNodeForPackage(
        CompanyStartupPreferenceStore.class);
    private static final String KEY = "companyStartupBehavior";

    private CompanyStartupPreferenceStore()
    {
    }

    public static StartupBehavior get()
    {
        try
        {
            return StartupBehavior.valueOf(PREFS.get(KEY,
                StartupBehavior.PRESELECT_LAST.name()));
        }
        catch (IllegalArgumentException ex)
        {
            return StartupBehavior.PRESELECT_LAST;
        }
    }

    public static void set(StartupBehavior behavior)
    {
        PREFS.put(KEY, behavior == null ?
            StartupBehavior.PRESELECT_LAST.name() : behavior.name());
    }
}
