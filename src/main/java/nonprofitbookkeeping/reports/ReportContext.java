
package nonprofitbookkeeping.reports;

import java.time.LocalDate;
import java.util.List; // Added import
import nonprofitbookkeeping.model.budget.Budget; // Ensured import for Budget

/**
 * ReportContext
 */
public class ReportContext
{
	private String reportType;
	private LocalDate startDate;
	private LocalDate endDate;
	private String outputFormat;
    private Budget selectedBudget; 
    private List<String> fundIds; 
    private List<String> accountIdsForDetailReport; // Added new field

	// Constructors (optional, but good practice)
    public ReportContext() {}

    // Getters and Setters for all fields

	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public LocalDate getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public String getOutputFormat() {
		return outputFormat;
	}
	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

    public Budget getSelectedBudget() {
        return selectedBudget;
    }
    public void setSelectedBudget(Budget selectedBudget) {
        this.selectedBudget = selectedBudget;
    }

    public List<String> getFundIds() {
        return fundIds;
    }
    public void setFundIds(List<String> fundIds) {
        this.fundIds = fundIds;
    }

    // Getter and Setter for accountIdsForDetailReport
    public List<String> getAccountIdsForDetailReport() {
        return accountIdsForDetailReport;
    }
    public void setAccountIdsForDetailReport(List<String> accountIdsForDetailReport) {
        this.accountIdsForDetailReport = accountIdsForDetailReport;
    }
}
