package nonprofitbookkeeping.model.reports;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import nonprofitbookkeeping.ui.helpers.DateSelectionMode; // Import the top-level enum

@Data
@NoArgsConstructor
public class ReportConfiguration {

    private String configurationId;
    private String userGivenName; // e.g., "Q1 Income Statement - Operations Fund"
    private String reportType;    // e.g., "income_statement", "balance_sheet" 
    
    // Date related fields
    private DateSelectionMode dateSelectionMode; // The mode used (SINGLE_DATE, DATE_RANGE_MANDATORY_START etc.)
    private String relativeDateRange; // Optional: e.g., "LAST_MONTH", "YEAR_TO_DATE" (for V2, can be null for V1)
    private LocalDate specificStartDate;   // Nullable
    private LocalDate specificEndDate;     // Nullable, but usually present if specific dates are used

    // Filter related fields
    private List<String> fundIds; // Stores fund names for filtering

    private String outputFormat = "xlsx"; // Default or saved format

    // Constructor to ensure ID is generated and essential fields are set
    public ReportConfiguration(String userGivenName, String reportType, DateSelectionMode dateSelectionMode, 
                               LocalDate specificStartDate, LocalDate specificEndDate, List<String> fundIds) {
        this.configurationId = UUID.randomUUID().toString();
        this.userGivenName = userGivenName;
        this.reportType = reportType;
        this.dateSelectionMode = dateSelectionMode;
        this.specificStartDate = specificStartDate;
        this.specificEndDate = specificEndDate;
        this.fundIds = fundIds;
        // this.relativeDateRange can be set separately if/when implemented
        // this.outputFormat defaults to "xlsx"
    }



	/**
	 * @return the reportType
	 */
	public String getReportType()
	{
		return this.reportType;
	}

	/**
	 * @param reportType the reportType to set
	 */
	public void setReportType(String reportType)
	{
		this.reportType = reportType;
	}

	/**
	 * @return the dateSelectionMode
	 */
	public DateSelectionMode getDateSelectionMode()
	{
		return this.dateSelectionMode;
	}

	/**
	 * @param dateSelectionMode the dateSelectionMode to set
	 */
	public void setDateSelectionMode(DateSelectionMode dateSelectionMode)
	{
		this.dateSelectionMode = dateSelectionMode;
	}

	/**
	 * @return the relativeDateRange
	 */
	public String getRelativeDateRange()
	{
		return this.relativeDateRange;
	}

	/**
	 * @param relativeDateRange the relativeDateRange to set
	 */
	public void setRelativeDateRange(String relativeDateRange)
	{
		this.relativeDateRange = relativeDateRange;
	}

	/**
	 * @return the specificStartDate
	 */
	public LocalDate getSpecificStartDate()
	{
		return this.specificStartDate;
	}

	/**
	 * @param specificStartDate the specificStartDate to set
	 */
	public void setSpecificStartDate(LocalDate specificStartDate)
	{
		this.specificStartDate = specificStartDate;
	}

	/**
	 * @return the specificEndDate
	 */
	public LocalDate getSpecificEndDate()
	{
		return this.specificEndDate;
	}

	/**
	 * @param specificEndDate the specificEndDate to set
	 */
	public void setSpecificEndDate(LocalDate specificEndDate)
	{
		this.specificEndDate = specificEndDate;
	}

	/**
	 * @return the fundIds
	 */
	public List<String> getFundIds()
	{
		return this.fundIds;
	}

	/**
	 * @param fundIds the fundIds to set
	 */
	public void setFundIds(List<String> fundIds)
	{
		this.fundIds = fundIds;
	}

	/**
	 * @return the outputFormat
	 */
	public String getOutputFormat()
	{
		return this.outputFormat;
	}

	/**
	 * @param configurationId the configurationId to set
	 */
	public void setConfigurationId(String configurationId)
	{
		this.configurationId = configurationId;
	}

	/**
	 * @param userGivenName the userGivenName to set
	 */
	public void setUserGivenName(String userGivenName)
	{
		this.userGivenName = userGivenName;
	}



	/**
	 * @return the configurationId
	 */
	public String getConfigurationId()
	{
		return this.configurationId;
	}



	/**
	 * @return the userGivenName
	 */
	public String getUserGivenName()
	{
		return this.userGivenName;
	}



	/**
	 * @param outputFormat the outputFormat to set
	 */
	public void setOutputFormat(String outputFormat)
	{
		this.outputFormat = outputFormat;
	}



	/**
	 * @param selectedAccountIds
	 */
	public void setAccountIdsForDetailReport(List<String> selectedAccountIds)
	{
		// TODO Auto-generated method stub
		
	}


	// FIXME 
	/**
	 * @return
	 */
	public List<String> getAccountIdsForDetailReport()
	{
		// TODO Auto-generated method stub
		return null;
	}

    // Lombok's @Data will generate:
    // - Getters for all fields
    // - Setters for all non-final fields
    // - toString(), equals(), hashCode()
    // - A constructor requiring all final fields (if any, none here)

    // Lombok's @NoArgsConstructor generates the public no-args constructor.
    // If configurationId needs to be absolutely guaranteed even with a no-args constructor path (e.g. by Jackson),
    // a custom getter like in the Budget class could be used:
    // public String getConfigurationId() {
    //     if (this.configurationId == null) {
    //         this.configurationId = UUID.randomUUID().toString();
    //     }
    //     return this.configurationId;
    // }
    // However, for new instances, the parameterized constructor is preferred.
    // Jackson typically handles ID fields correctly if they are present in JSON or set via setters.
}
