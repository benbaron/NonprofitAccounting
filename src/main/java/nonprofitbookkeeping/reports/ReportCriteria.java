package nonprofitbookkeeping.reports;

import java.time.LocalDate;
import java.util.List;
import nonprofitbookkeeping.ui.helpers.DateSelectionMode; // Added import

/**
 * Encapsulates the criteria used for generating a report.
 * This includes date ranges, selected fund IDs, selected account IDs (for detail reports),
 * and the mode of date selection. It also allows for specifying a name if the
 * criteria set is to be saved.
 * Most fields are final and set via the constructor, promoting immutability for core criteria.
 */
public class ReportCriteria {
    /** The start date for the report period. This field is final. */
    private final LocalDate startDate;
    /** The end date for the report period. This field is final. */
    private final LocalDate endDate;
    /**
     * A list of selected fund IDs (typically fund names) to filter the report.
     * This field is final. An empty or null list may imply no fund-specific filtering.
     */
    private final List<String> selectedFundIds; 
    /**
     * An optional name used if these report criteria are to be saved for later use.
     * Defaults to null. This field is mutable.
     */
    private String nameForSaving = null; 
    /**
     * The mode of date selection (e.g., specific range, relative range).
     * See {@link DateSelectionMode}. This field is final.
     */
    private final DateSelectionMode dateSelectionMode; 
    /**
     * A list of selected account IDs, typically used for filtering detail reports.
     * This field is final. Initialized to an empty list if null is passed to the constructor.
     */
    private final List<String> selectedAccountIds; // Added field

    /**
     * Constructs a new ReportCriteria object.
     *
     * @param startDate The start date for the report period. Can be null depending on {@code dateSelectionMode}.
     * @param endDate The end date for the report period. Can be null depending on {@code dateSelectionMode}.
     * @param selectedFundIds A list of fund IDs to filter by. Can be null or empty.
     * @param dateSelectionMode The {@link DateSelectionMode} indicating how dates are interpreted.
     * @param selectedAccountIds A list of account IDs for filtering. If null, an empty list is used.
     */
    public ReportCriteria(LocalDate startDate, LocalDate endDate, List<String> selectedFundIds, 
                          DateSelectionMode dateSelectionMode, List<String> selectedAccountIds) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.selectedFundIds = selectedFundIds;
        this.dateSelectionMode = dateSelectionMode; 
        this.selectedAccountIds = (selectedAccountIds != null) ? selectedAccountIds : new java.util.ArrayList<>(); // Ensure not null
    }

    /**
     * Gets the start date for the report period.
     * @return The start {@link LocalDate}, or null if not applicable for the current date selection mode.
     */
    public LocalDate getStartDate() {
        return this.startDate;
    }

    /**
     * Gets the end date for the report period.
     * @return The end {@link LocalDate}, or null if not applicable for the current date selection mode.
     */
    public LocalDate getEndDate() {
        return this.endDate;
    }

    /**
     * Gets the list of selected fund IDs for filtering.
     * An empty list or null might indicate that all funds should be considered (no specific fund filter).
     * @return A list of fund ID strings. Can be null or empty.
     */
    public List<String> getSelectedFundIds() {
        return this.selectedFundIds;
    }

    /**
     * Gets the name designated for saving this set of report criteria.
     * @return The name for saving, or null if not set.
     */
    public String getNameForSaving() {
        return this.nameForSaving;
    }

    /**
     * Sets the name to be used if these report criteria are saved.
     * @param nameForSaving The name for saving.
     */
    public void setNameForSaving(String nameForSaving) {
        this.nameForSaving = nameForSaving;
    }

    /**
     * Gets the date selection mode.
     * This indicates how the {@code startDate} and {@code endDate} should be interpreted
     * or if a relative date range is used.
     * @return The {@link DateSelectionMode} enum value.
     */
    public DateSelectionMode getDateSelectionMode() { 
        return this.dateSelectionMode;
    }

    /**
     * Gets the list of selected account IDs, typically used for filtering detail reports.
     * @return A list of account ID strings. Will be an empty list if initialized with null.
     */
    public List<String> getSelectedAccountIds() { // Added getter
        return this.selectedAccountIds;
    }
}
