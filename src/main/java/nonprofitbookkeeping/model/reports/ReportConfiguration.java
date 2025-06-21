
package nonprofitbookkeeping.model.reports;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import nonprofitbookkeeping.ui.helpers.DateSelectionMode; // Import the top-level enum

/**
 * Represents the configuration for generating a specific report.
 * This includes details about the report type, date ranges, filters (like fund IDs),
 * and output format. It is used to save and recall user-defined report settings.
 * Lombok's {@code @Data} and {@code @NoArgsConstructor} are used for boilerplate code generation.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "report_configuration")
public class ReportConfiguration
{
	
        /** A unique identifier for this report configuration, typically a UUID. */
        @Id
        @Column(name = "configuration_id")
        private String configurationId;
	/** A user-friendly name for this saved report configuration (e.g., "Q1 Income Statement - Operations Fund"). */
        @Column(name = "user_given_name")
        private String userGivenName;
	/** The type of report to generate (e.g., "income_statement", "balance_sheet"). */
        @Column(name = "report_type")
        private String reportType;
	
	// Date related fields
	/** The mode used for date selection (e.g., SINGLE_DATE, DATE_RANGE_MANDATORY_START). See {@link DateSelectionMode}. */
        @Enumerated(EnumType.STRING)
        @Column(name = "date_selection_mode")
        private DateSelectionMode dateSelectionMode;
	/**
	 * An optional string representing a relative date range (e.g., "LAST_MONTH", "YEAR_TO_DATE").
	 * Can be null, especially if specific start/end dates are used.
	 */
        @Column(name = "relative_date_range")
        private String relativeDateRange;
	/** The specific start date for the report period. Nullable if not applicable for the selected {@code dateSelectionMode}. */
        @Column(name = "specific_start_date")
        private LocalDate specificStartDate;
	/** The specific end date for the report period. Nullable, but usually present if specific dates are used or implied by the mode. */
        @Column(name = "specific_end_date")
        private LocalDate specificEndDate;
	
	// Filter related fields
	/** A list of fund IDs (typically fund names) to filter the report by. Can be null or empty for no fund filtering. */
        @Column(name = "fund_ids")
        private List<String> fundIds;
	
	/** The desired output format for the report (e.g., "xlsx", "pdf"). Defaults to "xlsx". */
        @Column(name = "output_format")
        private String outputFormat = "xlsx";
	
	/** A list of account IDs to filter a detail report by. Can be null or empty. */
        @Column(name = "account_ids")
        private List<String> accountIdsForDetailReport;
	
	/**
	 * Constructs a new ReportConfiguration with essential details.
	 * A unique {@code configurationId} is automatically generated.
	 * {@code relativeDateRange} is not set by this constructor and can be set separately.
	 * {@code outputFormat} defaults to "xlsx".
	 *
	 * @param userGivenName A user-friendly name for this configuration.
	 * @param reportType The type of report (e.g., "income_statement").
	 * @param dateSelectionMode The mode for date selection.
	 * @param specificStartDate The specific start date, if applicable. Can be null.
	 * @param specificEndDate The specific end date, if applicable. Can be null.
	 * @param fundIds A list of fund IDs to filter by. Can be null or empty.
	 */
	public ReportConfiguration(String userGivenName, String reportType,
		DateSelectionMode dateSelectionMode,
		LocalDate specificStartDate, LocalDate specificEndDate, List<String> fundIds)
	{
		this.configurationId = UUID.randomUUID().toString();
		this.userGivenName = userGivenName;
		this.reportType = reportType;
		this.dateSelectionMode = dateSelectionMode;
		this.specificStartDate = specificStartDate;
		this.specificEndDate = specificEndDate;
		this.fundIds = fundIds;
		// this.relativeDateRange can be set separately if/when implemented
		// this.outputFormat defaults to "xlsx" as per field initializer
	}
	
	// Explicit getters and setters below are mostly redundant due to Lombok @Data,
	// but are documented as they exist.
	
	/**
	 * Gets the type of the report.
	 * @return The report type string (e.g., "income_statement").
	 */
	public String getReportType()
	{
		return this.reportType;
	}
	
	/**
	 * Sets the type of the report.
	 * @param reportType The report type string to set.
	 */
	public void setReportType(String reportType)
	{
		this.reportType = reportType;
	}
	
	/**
	 * Gets the date selection mode for the report.
	 * @return The {@link DateSelectionMode} enum value.
	 */
	public DateSelectionMode getDateSelectionMode()
	{
		return this.dateSelectionMode;
	}
	
	/**
	 * Sets the date selection mode for the report.
	 * @param dateSelectionMode The {@link DateSelectionMode} to set.
	 */
	public void setDateSelectionMode(DateSelectionMode dateSelectionMode)
	{
		this.dateSelectionMode = dateSelectionMode;
	}
	
	/**
	 * Gets the relative date range string (e.g., "LAST_MONTH").
	 * @return The relative date range string, or null if not set.
	 */
	public String getRelativeDateRange()
	{
		return this.relativeDateRange;
	}
	
	/**
	 * Sets the relative date range string.
	 * @param relativeDateRange The relative date range string to set.
	 */
	public void setRelativeDateRange(String relativeDateRange)
	{
		this.relativeDateRange = relativeDateRange;
	}
	
	/**
	 * Gets the specific start date for the report period.
	 * @return The specific start {@link LocalDate}, or null if not set.
	 */
	public LocalDate getSpecificStartDate()
	{
		return this.specificStartDate;
	}
	
	/**
	 * Sets the specific start date for the report period.
	 * @param specificStartDate The specific start {@link LocalDate} to set.
	 */
	public void setSpecificStartDate(LocalDate specificStartDate)
	{
		this.specificStartDate = specificStartDate;
	}
	
	/**
	 * Gets the specific end date for the report period.
	 * @return The specific end {@link LocalDate}, or null if not set.
	 */
	public LocalDate getSpecificEndDate()
	{
		return this.specificEndDate;
	}
	
	/**
	 * Sets the specific end date for the report period.
	 * @param specificEndDate The specific end {@link LocalDate} to set.
	 */
	public void setSpecificEndDate(LocalDate specificEndDate)
	{
		this.specificEndDate = specificEndDate;
	}
	
	/**
	 * Gets the list of fund IDs to filter the report by.
	 * @return A list of fund ID strings. Can be null or empty.
	 */
	public List<String> getFundIds()
	{
		return this.fundIds;
	}
	
	/**
	 * Sets the list of fund IDs to filter the report by.
	 * @param fundIds A list of fund ID strings to set.
	 */
	public void setFundIds(List<String> fundIds)
	{
		this.fundIds = fundIds;
	}
	
	/**
	 * Gets the output format for the report.
	 * @return The output format string (e.g., "xlsx").
	 */
	public String getOutputFormat()
	{
		return this.outputFormat;
	}
	
	/**
	 * Sets the unique identifier for this report configuration.
	 * While typically auto-generated, this allows setting it if needed (e.g., when loading from persistence).
	 * @param configurationId The configuration ID to set.
	 */
	public void setConfigurationId(String configurationId)
	{
		this.configurationId = configurationId;
	}
	
	/**
	 * Sets the user-given name for this report configuration.
	 * @param userGivenName The user-friendly name to set.
	 */
	public void setUserGivenName(String userGivenName)
	{
		this.userGivenName = userGivenName;
	}
	
	/**
	 * Gets the unique identifier for this report configuration.
	 * @return The configuration ID string.
	 */
	public String getConfigurationId()
	{
		return this.configurationId;
	}
	
	/**
	 * Gets the user-given name for this report configuration.
	 * @return The user-friendly name.
	 */
	public String getUserGivenName()
	{
		return this.userGivenName;
	}
	
	/**
	 * Sets the output format for the report.
	 * @param outputFormat The output format string to set (e.g., "xlsx", "pdf").
	 */
	public void setOutputFormat(String outputFormat)
	{
		this.outputFormat = outputFormat;
	}
		
	/**
	 * Sets the account IDs to be used for filtering a detail report.
	 * Note: This is a stub method and needs to be implemented.
	 * The actual storage for these account IDs is not yet defined in this class.
	 * @param selectedAccountIds A list of account ID strings.
	 */
	public void setAccountIdsForDetailReport(List<String> selectedAccountIds)
	{
		this.accountIdsForDetailReport = selectedAccountIds;
	}
	
	/**
	 * Gets the account IDs used for filtering a detail report.
	 * @return A list of account ID strings, or null if not set.
	 */
	public List<String> getAccountIdsForDetailReport()
	{
		return this.accountIdsForDetailReport;
	}
	
	
}
