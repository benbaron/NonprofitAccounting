package nonprofitbookkeeping.model;

/**
 * Represents the default date range selection presented to a user when opening
 * report dialogs. This information lives in {@link SettingsModel} so the UI can
 * remember the user's preference across sessions.
 */
public enum DefaultReportPeriod {
    /** Use the current calendar month as the default reporting range. */
    CURRENT_MONTH,
    /** Use the previous calendar month as the default reporting range. */
    PREVIOUS_MONTH,
    /** Report on the current fiscal or calendar year-to-date. */
    YEAR_TO_DATE,
    /** Use the most recent completed fiscal year. */
    LAST_YEAR,
    /** Leave the date fields empty and allow the user to choose every time. */
    CUSTOM
}
