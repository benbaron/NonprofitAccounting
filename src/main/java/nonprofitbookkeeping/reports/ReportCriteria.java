package nonprofitbookkeeping.reports;

import java.time.LocalDate;
import java.util.List;
import nonprofitbookkeeping.ui.helpers.DateSelectionMode; // Added import

public class ReportCriteria {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<String> selectedFundIds; 
    private String nameForSaving = null; 
    private final DateSelectionMode dateSelectionMode; 
    private final List<String> selectedAccountIds; // Added field

    public ReportCriteria(LocalDate startDate, LocalDate endDate, List<String> selectedFundIds, 
                          DateSelectionMode dateSelectionMode, List<String> selectedAccountIds) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.selectedFundIds = selectedFundIds;
        this.dateSelectionMode = dateSelectionMode; 
        this.selectedAccountIds = (selectedAccountIds != null) ? selectedAccountIds : new java.util.ArrayList<>(); // Ensure not null
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    /**
     * Gets the list of selected fund IDs.
     * An empty list or null might indicate that all funds should be considered (no specific fund filter).
     * @return A list of fund IDs, or null/empty.
     */
    public List<String> getSelectedFundIds() {
        return this.selectedFundIds;
    }

    public String getNameForSaving() {
        return this.nameForSaving;
    }

    public void setNameForSaving(String nameForSaving) {
        this.nameForSaving = nameForSaving;
    }

    public DateSelectionMode getDateSelectionMode() { 
        return this.dateSelectionMode;
    }

    public List<String> getSelectedAccountIds() { // Added getter
        return this.selectedAccountIds;
    }
}
