package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.reports.ReportConfiguration;
import nonprofitbookkeeping.ui.helpers.DateSelectionMode; // Ensure this is the correct path

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportConfigurationTableModel extends AbstractTableModel {

    private final List<ReportConfiguration> configurations;
    private final String[] columnNames = {"Name", "Report Type", "Date Range", "Funds"};
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");


    public ReportConfigurationTableModel(List<ReportConfiguration> configurations) {
        this.configurations = (configurations != null) ? configurations : new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return configurations.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ReportConfiguration config = configurations.get(rowIndex);
        switch (columnIndex) {
            case 0: // Name
                return config.getUserGivenName();
            case 1: // Report Type
                return config.getReportType(); // Could map to more user-friendly names if needed
            case 2: // Date Range
                return formatDateRange(config);
            case 3: // Funds
                return formatFundIds(config.getFundIds());
            default:
                return null;
        }
    }

    private String formatDateRange(ReportConfiguration config) {
        if (config.getRelativeDateRange() != null && !config.getRelativeDateRange().isEmpty()) {
            // V2: Handle relative date ranges like "Last Month", "Year to Date"
            // For now, returning the stored string if it exists.
            return config.getRelativeDateRange(); 
        }

        DateSelectionMode mode = config.getDateSelectionMode();
        LocalDate startDate = config.getSpecificStartDate();
        LocalDate endDate = config.getSpecificEndDate();

        if (mode == null) return "N/A"; // Should not happen if data is saved correctly

        switch (mode) {
            case SINGLE_DATE:
                return (endDate != null) ? endDate.format(dateFormatter) : "N/A";
            case DATE_RANGE_MANDATORY_START:
            case DATE_RANGE_OPTIONAL_START:
                String startStr = (startDate != null) ? startDate.format(dateFormatter) : "Beginning";
                String endStr = (endDate != null) ? endDate.format(dateFormatter) : "N/A";
                return startStr + " - " + endStr;
            default:
                return "Invalid Date Mode";
        }
    }

    private String formatFundIds(List<String> fundIds) {
        if (fundIds == null || fundIds.isEmpty()) {
            return "All Funds";
        }
        // Assuming fundIds are fund names for display, as Fund objects aren't stored in ReportConfiguration
        return fundIds.stream().collect(Collectors.joining(", "));
    }

    public ReportConfiguration getConfigurationAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < configurations.size()) {
            return configurations.get(rowIndex);
        }
        return null;
    }

    public void removeConfiguration(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < configurations.size()) {
            configurations.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }
    
    public void setConfigurations(List<ReportConfiguration> newConfigurations) {
        this.configurations.clear();
        if (newConfigurations != null) {
            this.configurations.addAll(newConfigurations);
        }
        fireTableDataChanged();
    }
}
