package nonprofitbookkeeping.model;

/**
 * Represents the default time window a user prefers when generating reports.
 * The values intentionally cover the most common reporting presets used in the
 * application. Additional values can be introduced later without impacting
 * serialization because the enum name is what is stored.
 */
public enum DefaultReportPeriod
{
        /** Use the current calendar month to date. */
        MONTH_TO_DATE,
        /** Use the previous full calendar month. */
        LAST_MONTH,
        /** Use the current quarter to date. */
        QUARTER_TO_DATE,
        /** Use the current fiscal or calendar year to date. */
        YEAR_TO_DATE,
        /** Allow the user to provide custom start/end dates each time. */
        CUSTOM
}
