package nonprofitbookkeeping.ui.helpers;

/**
 * Defines different modes for selecting dates or date ranges, typically used in report criteria dialogs.
 * This enum helps determine which date fields (start date, end date) are relevant or mandatory
 * for a particular report or operation.
 */
public enum DateSelectionMode {
    /**
     * Represents a single date selection.
     * In this mode, typically only an "end date" or "as of date" is relevant.
     * For example, a Balance Sheet is often for a single point in time.
     */
    SINGLE_DATE,
    /**
     * Represents a date range where both the start date and end date are mandatory.
     * For example, an Income Statement usually requires a defined period.
     */
    DATE_RANGE_MANDATORY_START,
    /**
     * Represents a date range where the end date is mandatory, but the start date is optional.
     * If the start date is not provided, it might default to the beginning of records or a fiscal year start.
     * For example, a Trial Balance might be "as of" an end date, but could optionally show activity from a start date.
     */
    DATE_RANGE_OPTIONAL_START
}
