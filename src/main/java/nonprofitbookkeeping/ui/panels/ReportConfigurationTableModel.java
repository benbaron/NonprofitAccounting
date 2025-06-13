
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.reports.ReportConfiguration;
import nonprofitbookkeeping.ui.helpers.DateSelectionMode; // Ensure this is the correct path

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Swing {@link AbstractTableModel} for displaying a list of {@link ReportConfiguration} objects.
 * This model defines columns for Name, Report Type, Date Range, and Funds.
 * It provides methods to format date ranges and fund lists for display.
 */
public class ReportConfigurationTableModel extends AbstractTableModel
{
	
	/** The list of {@link ReportConfiguration} objects this table model represents. */
	private final List<ReportConfiguration> configurations;
	/** The names of the columns to be displayed in the table header. */
	private final String[] columnNames =
	{ "Name", "Report Type", "Date Range", "Funds" };
	/** Formatter for displaying dates in "MM/dd/yyyy" format. */
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
	
	
	/**
	 * Constructs a new {@code ReportConfigurationTableModel}.
	 *
	 * @param configurations The initial list of {@link ReportConfiguration}s to display.
	 *                       If null, an empty list is used.
	 */
	public ReportConfigurationTableModel(List<ReportConfiguration> configurations)
	{
		this.configurations = (configurations != null) ? configurations : new ArrayList<>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override public int getRowCount()
	{
		return this.configurations.size();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override public int getColumnCount()
	{
		return this.columnNames.length;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override public String getColumnName(int columnIndex)
	{
		return this.columnNames[columnIndex];
	}
	
	/**
	 * {@inheritDoc}
	 * Retrieves the value for a cell in the table.
	 * <ul>
	 *   <li>Column 0 (Name): {@link ReportConfiguration#getUserGivenName()}</li>
	 *   <li>Column 1 (Report Type): {@link ReportConfiguration#getReportType()} (could be mapped to user-friendly names)</li>
	 *   <li>Column 2 (Date Range): Formatted date range string via {@link #formatDateRange(ReportConfiguration)}</li>
	 *   <li>Column 3 (Funds): Formatted list of fund IDs/names via {@link #formatFundIds(List)}</li>
	 * </ul>
	 * @param rowIndex The row whose value is to be queried.
	 * @param columnIndex The column whose value is to be queried.
	 * @return The value at the specified cell, or null for an invalid column index.
	 */
	@Override public Object getValueAt(int rowIndex, int columnIndex)
	{
		ReportConfiguration config = this.configurations.get(rowIndex);
		
		switch(columnIndex)
		{
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
	
	/**
	 * Formats the date range of a {@link ReportConfiguration} into a displayable string.
	 * It handles relative date ranges (if present) or formats specific start/end dates
	 * based on the {@link DateSelectionMode}.
	 *
	 * @param config The {@link ReportConfiguration} whose date range is to be formatted. Must not be null.
	 * @return A string representation of the date range (e.g., "01/01/2023 - 12/31/2023", "Last Month", "N/A").
	 */
	private String formatDateRange(ReportConfiguration config)
	{
		
		if (config.getRelativeDateRange() != null && !config.getRelativeDateRange().isEmpty())
		{
			// V2: Handle relative date ranges like "Last Month", "Year to Date"
			// For now, returning the stored string if it exists.
			return config.getRelativeDateRange();
		}
		
		DateSelectionMode mode = config.getDateSelectionMode();
		LocalDate startDate = config.getSpecificStartDate();
		LocalDate endDate = config.getSpecificEndDate();
		
		if (mode == null)
			return "N/A"; // Should not happen if data is saved correctly
			
		switch(mode)
		{
			case SINGLE_DATE:
				return (endDate != null) ? endDate.format(this.dateFormatter) : "N/A";
				
			case DATE_RANGE_MANDATORY_START:
			case DATE_RANGE_OPTIONAL_START:
				String startStr =
					(startDate != null) ? startDate.format(this.dateFormatter) : "Beginning";
				String endStr = (endDate != null) ? endDate.format(this.dateFormatter) : "N/A";
				return startStr + " - " + endStr;
				
			default:
				return "Invalid Date Mode";
		}
		
	}
	
	/**
	 * Formats a list of fund IDs into a displayable string.
	 * If the list is null or empty, it returns "All Funds". Otherwise, it returns a comma-separated
	 * string of the fund IDs (or names, if they were resolved prior to being stored as strings).
	 *
	 * @param fundIds A list of strings representing fund identifiers.
	 * @return A comma-separated string of fund IDs, or "All Funds" if the list is empty/null.
	 */
	private String formatFundIds(List<String> fundIds)
	{
		
		if (fundIds == null || fundIds.isEmpty())
		{
			return "All Funds";
		}
		
		// Assuming fundIds are fund names for display, as Fund objects aren't stored in
		// ReportConfiguration
		return fundIds.stream().collect(Collectors.joining(", "));
	}
	
	/**
	 * Retrieves the {@link ReportConfiguration} object at the specified row index.
	 *
	 * @param rowIndex The zero-based index of the row.
	 * @return The {@link ReportConfiguration} at the specified row, or {@code null} if the index is out of bounds.
	 */
	public ReportConfiguration getConfigurationAt(int rowIndex)
	{
		
		if (rowIndex >= 0 && rowIndex < this.configurations.size())
		{
			return this.configurations.get(rowIndex);
		}
		
		return null;
	}
	
	/**
	 * Removes the {@link ReportConfiguration} at the specified row index from the table model.
	 * Notifies listeners that a row has been deleted.
	 *
	 * @param rowIndex The zero-based index of the row to remove. Does nothing if the index is out of bounds.
	 */
	public void removeConfiguration(int rowIndex)
	{
		
		if (rowIndex >= 0 && rowIndex < this.configurations.size())
		{
			this.configurations.remove(rowIndex);
			fireTableRowsDeleted(rowIndex, rowIndex);
		}
		
	}
	
	/**
	 * Sets the list of {@link ReportConfiguration}s to be displayed by this table model.
	 * Clears any existing configurations and adds all from the new list.
	 * Notifies listeners that the table data has changed.
	 *
	 * @param newConfigurations The new list of {@link ReportConfiguration}s. If null, the table will be empty.
	 */
	public void setConfigurations(List<ReportConfiguration> newConfigurations)
	{
		this.configurations.clear();
		
		if (newConfigurations != null)
		{
			this.configurations.addAll(newConfigurations);
		}
		
		fireTableDataChanged();
	}
	
}
