package nonprofitbookkeeping.ui.helpers;

public enum DateSelectionMode {
    SINGLE_DATE, // Only end date
    DATE_RANGE_MANDATORY_START, // Start and End date, both mandatory
    DATE_RANGE_OPTIONAL_START // Start date optional, End date mandatory
}
