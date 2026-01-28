package nonprofitbookkeeping.reports.jasper.runtime;

import java.io.File;
import java.time.LocalDate;
import java.util.List; // Added import
import nonprofitbookkeeping.model.budget.Budget; // Ensured import for Budget

/**
 * Holds the context or criteria for generating a specific financial report.
 * This includes the type of report, date ranges, output format, and any
 * applicable filters such as selected budget, fund IDs, or account IDs for detail reports.
 */
public class ReportContext
{
	/** The type of report to be generated (e.g., "income_statement", "balance_sheet"). */
	private String reportType;
	/** The start date for the report period. Can be null if not applicable or for certain relative date ranges. */
	private LocalDate startDate;
	/** The end date for the report period. Can be null if not applicable (e.g., for a single date report) or for certain relative date ranges. */
	private LocalDate endDate;
	/** The desired output format for the report (e.g., "xlsx", "pdf"). */
	private String outputFormat;
	/** The selected {@link Budget} to be used for budget vs. actual comparisons in the report. Can be null. */
	private Budget selectedBudget;
	/** A list of fund IDs (typically fund names) to filter the report data. Can be null or empty. */
	private List<String> fundIds;
	/** A list of account IDs used for generating detail-specific sections or filtering in some reports. Can be null or empty. */
	private List<String> accountIdsForDetailReport; // Added new field
	/** Optional transaction type filter (e.g., DEBIT or CREDIT). */
	private String transactionType;
	/** Optional substring filter for transaction memos. */
	private String memoFilter;
	/** If true, all supplied accounts must be present on a transaction. */
	private boolean requireAllAccounts;
	/** Optional pre-built beans to drive Jasper reports that expect their data to be provided externally. */
	private List<?> beans;
	/** Optional placeholder text used when string fields are null in report output. */
	private String nullPlaceholder = "-";
	/** Optional list of field names that should retain null values instead of placeholders. */
	private List<String> nullPlaceholderSkipFields;
	/** Optional Excel template file for direct XLSX output. */
	private File excelTemplateFile;
	
	/**
	 * Default constructor for ReportContext.
	 * Initializes all fields to their default values (null for objects).
	 */
	public ReportContext()
	{
		
	}
	
	/**
	 * Gets the type of the report.
	 * @return The report type string.
	 */
	public String getReportType()
	{
		return this.reportType;
		
	}
	
	/**
	 * Sets the type of the report.
	 * @param reportType The report type string (e.g., "income_statement").
	 */
	public void setReportType(String reportType)
	{
		this.reportType = reportType;
		
	}
	
	/**
	 * Gets the start date for the report period.
	 * @return The start {@link LocalDate}, or null if not set.
	 */
	public LocalDate getStartDate()
	{
		return this.startDate;
		
	}
	
	/**
	 * Sets the start date for the report period.
	 * @param startDate The start {@link LocalDate} to set.
	 */
	public void setStartDate(LocalDate startDate)
	{
		this.startDate = startDate;
		
	}
	
	/**
	 * Gets the end date for the report period.
	 * @return The end {@link LocalDate}, or null if not set.
	 */
	public LocalDate getEndDate()
	{
		return this.endDate;
		
	}
	
	/**
	 * Sets the end date for the report period.
	 * @param endDate The end {@link LocalDate} to set.
	 */
	public void setEndDate(LocalDate endDate)
	{
		this.endDate = endDate;
		
	}
	
	/**
	 * Gets the desired output format for the report.
	 * @return The output format string (e.g., "xlsx").
	 */
	public String getOutputFormat()
	{
		return this.outputFormat;
		
	}
	
	/**
	 * Sets the desired output format for the report.
	 * @param outputFormat The output format string to set (e.g., "xlsx", "pdf").
	 */
	public void setOutputFormat(String outputFormat)
	{
		this.outputFormat = outputFormat;
		
	}

	/**
	 * Gets the Excel template file used for direct XLSX output.
	 *
	 * @return Excel template file or {@code null} if not set
	 */
	public File getExcelTemplateFile()
	{
		return this.excelTemplateFile;
	}

	/**
	 * Sets the Excel template file used for direct XLSX output.
	 *
	 * @param excelTemplateFile template file to populate
	 */
	public void setExcelTemplateFile(File excelTemplateFile)
	{
		this.excelTemplateFile = excelTemplateFile;
	}
	
	
	/**
	 * Gets the list of fund IDs to be used for filtering the report.
	 * @return A list of fund ID strings. Can be null or empty.
	 */
	public List<String> getFundIds()
	{
		return this.fundIds;
		
	}
	
	/**
	 * Sets the list of fund IDs to be used for filtering the report.
	 * @param fundIds A list of fund ID strings to set.
	 */
	public void setFundIds(List<String> fundIds)
	{
		this.fundIds = fundIds;
		
	}
	
	/**
	 * Gets the list of account IDs to be used for detail sections or filtering in some reports.
	 * @return A list of account ID strings. Can be null or empty.
	 */
	public List<String> getAccountIdsForDetailReport()
	{
		return this.accountIdsForDetailReport;
		
	}
	
	/**
	 * Sets the list of account IDs to be used for detail sections or filtering in some reports.
	 * @param accountIdsForDetailReport A list of account ID strings to set.
	 */
	public void setAccountIdsForDetailReport(
		List<String> accountIdsForDetailReport)
	{
		this.accountIdsForDetailReport = accountIdsForDetailReport;
		
	}
	
	/**
	 * Gets the list of beans prepared for Jasper report generation.
	 *
	 * @return List of beans or {@code null} if none were supplied.
	 */
	public List<?> getBeans()
	{
		return this.beans;
		
	}
	
	/**
	 * Sets the list of beans to be used directly by a {@link nonprofitbookkeeping.reports.jasper.AbstractReportGenerator}.
	 *
	 * @param beans data beans for the report
	 */
	public void setBeans(List<?> beans)
	{
		this.beans = beans;
		
	}

	/**
	 * Gets the placeholder used for null string fields in report output.
	 *
	 * @return placeholder string; defaults to "-" when unset, or {@code null}
	 * to disable placeholder substitution
	 */
	public String getNullPlaceholder()
	{
		return this.nullPlaceholder;
		
	}

	/**
	 * Sets the placeholder used for null string fields in report output.
	 *
	 * @param nullPlaceholder placeholder text to substitute for nulls; use
	 * {@code null} to leave nulls as-is, or an empty string for blank output
	 */
	public void setNullPlaceholder(String nullPlaceholder)
	{
		this.nullPlaceholder = nullPlaceholder;
		
	}

	/**
	 * Gets the list of field names that should keep null values in output.
	 *
	 * @return list of field names or {@code null} if none are configured
	 */
	public List<String> getNullPlaceholderSkipFields()
	{
		return this.nullPlaceholderSkipFields;
		
	}

	/**
	 * Sets the list of field names that should keep null values in output.
	 *
	 * @param nullPlaceholderSkipFields field names to exclude from placeholder substitution
	 */
	public void setNullPlaceholderSkipFields(List<String> nullPlaceholderSkipFields)
	{
		this.nullPlaceholderSkipFields = nullPlaceholderSkipFields;
		
	}
	
	/**
	 * Gets the transaction type filter text (e.g., "DEBIT", "CREDIT").
	 * @return transaction type filter or {@code null} if not set
	 */
	public String getTransactionType()
	{
		return this.transactionType;
		
	}
	
	/**
	 * Sets the transaction type filter text.
	 * @param transactionType type text to match against {@link nonprofitbookkeeping.model.AccountSide}
	 */
	public void setTransactionType(String transactionType)
	{
		this.transactionType = transactionType;
		
	}
	
	/**
	 * Gets the memo substring filter for transaction lookups.
	 * @return memo substring or {@code null}
	 */
	public String getMemoFilter()
	{
		return this.memoFilter;
		
	}
	
	/**
	 * Sets the memo substring filter for transaction lookups.
	 * @param memoFilter substring to search for (case-insensitive)
	 */
	public void setMemoFilter(String memoFilter)
	{
		this.memoFilter = memoFilter;
		
	}
	
	/**
	 * Indicates whether all provided accounts must be present on a transaction.
	 * @return true if all accounts are required; false to match any
	 */
	public boolean isRequireAllAccounts()
	{
		return this.requireAllAccounts;
		
	}
	
	/**
	 * Sets whether a transaction must include all provided accounts.
	 * @param requireAllAccounts true to require all accounts, false to match any
	 */
	public void setRequireAllAccounts(boolean requireAllAccounts)
	{
		this.requireAllAccounts = requireAllAccounts;
		
	}
	
}
