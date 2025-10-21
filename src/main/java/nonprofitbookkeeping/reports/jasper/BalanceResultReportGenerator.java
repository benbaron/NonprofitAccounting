
package nonprofitbookkeeping.reports.jasper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections; // For Collections.emptyList()

import nonprofitbookkeeping.service.AccountService;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;

/**
 * Report generator for creating a "Balance Result Report". This report
 * typically shows account balances. It extends {@link AbstractReportGenerator}
 * to leverage common report generation and export functionalities.
 */
public class BalanceResultReportGenerator extends AbstractReportGenerator
{
	
	/**
	 * Constructs a {@code BalanceResultReportGenerator}.
	 *
	 * @param accountService The account service, which is currently not directly used by this generator's
	 *                       methods as {@link #getReportData()} makes a static call to
	 *                       {@code AccountService.getBalanceResults(Ledger)}. This parameter might be used
	 *                       if {@code AccountService} were refactored to be an instance service.
	 */
	public BalanceResultReportGenerator(AccountService accountService)
	{
		// accountService parameter is not currently used by this generator's methods
		// as getReportData() calls AccountService.getBalanceResults(Ledger) statically.
		// If AccountService were to become an instance service, this would need to
		// change.
	}
	
	/**
	 * {@inheritDoc}
	 * <p>Provides parameters specific to the Balance Result Report. This includes:
	 * <ul>
	 *   <li>{@code reporttitle}: "Balance Result Report"</li>
	 *   <li>{@code dateToday}: Current date as a string (e.g., "YYYY-MM-DD")</li>
	 *   <li>{@code company}: Name of the current company, or "N/A"</li>
	 *   <li>{@code companytext}: Formatted string "Report for: [CompanyName]"</li>
	 *   <li>Other general parameters like {@code P_REPORT_TITLE}, {@code P_GENERATION_DATE},
	 *       {@code P_COMPANY_NAME}, and {@code P_REPORT_PERIOD} are also set.</li>
	 * </ul>
	 * </p>
	 * @return A map of parameters for the JasperReport.
	 */
	@Override protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("P_REPORT_TITLE", "Balance Result Report"); // Match typical JRXML param
																	// names
		parameters.put("P_GENERATION_DATE",
			LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
		
		Company currentCompany = CurrentCompany.getCompany();
		String companyName = "N/A";
		String companyDetailsText = "Company details not available."; // Default text
		
		if (currentCompany != null && currentCompany.getCompanyProfile() != null)
		{
			companyName = currentCompany.getCompanyProfile().getCompanyName() != null ?
				currentCompany.getCompanyProfile().getCompanyName() : "N/A";
			// For companytext, using a simpler approach.
			// If CompanyProfileModel had a formatted address or details string, that could
			// be used.
			companyDetailsText = "Report for: " + companyName;
		}
		
		parameters.put("P_COMPANY_NAME", companyName);
		// The JRXML for BalanceResultReport might not have P_COMPANY_DETAILS or
		// P_REPORT_PERIOD.
		// Adjust parameters based on actual JRXML. For now, providing common ones.

		// parameters.put("P_COMPANY_DETAILS", companyDetailsText);

		// If JRXML uses this
		parameters.put("P_REPORT_PERIOD",
			"As of " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
		
		
		// Parameters from original BalanceResultReport.jrxml:
		// <parameter name="company" class="java.lang.String"/>
		// <parameter name="companytext" class="java.lang.String"/>
		// <parameter name="dateToday" class="java.lang.String"/>
		// <parameter name="reporttitle" class="java.lang.String"/>
		// Mapping to these specific names based on the provided JRXML structure:
		parameters.put("reporttitle", "Balance Result Report");
		parameters.put("dateToday", LocalDate.now().toString()); // e.g., "2024-01-15"
		parameters.put("company", companyName);
		parameters.put("companytext", companyDetailsText);
		
		
		return parameters;
	}
	
	/**
	 * {@inheritDoc}
	 * @return The classpath resource path "reports/BalanceResultReport.jrxml".
	 * @throws ActionCancelledException Not directly thrown by this implementation, but declared due to the interface.
	 * @throws NoFileCreatedException Not directly thrown by this implementation, but declared due to the interface.
	 */
	@Override protected String getReportPath()	throws ActionCancelledException,
												NoFileCreatedException
	{
		// Path relative to the resources directory
		// Updated to match the current location of the JRXML template.
		return "jrxml/balanceReport.jrxml";
	}
	
	/**
	 * {@inheritDoc}
	 * <p>Fetches data for the Balance Result Report by calling the static method
	 * {@code AccountService.getBalanceResults(Ledger)}. The returned list should contain
	 * {@link AccountService.AccountBalance} objects or a compatible JavaBean structure
	 * that matches the fields defined in the {@code BalanceResultReport.jrxml} template
	 * (e.g., name, number, type, balance_currency_string).
	 * </p>
	 * @return A list of {@link AccountService.AccountBalance} objects, or an empty list if no data is available.
	 */
	@Override protected List<?> getReportData()
	{
		// Fetch the ledger from the currently loaded company if available.
		Ledger ledger = null;
		Company currentCompany = CurrentCompany.getCompany();
		
		if (currentCompany != null)
		{
			ledger = currentCompany.getLedger();
		}
		
		// The data structure returned by AccountService.getBalanceResults(ledger)
		// must be a List of JavaBeans compatible with the fields defined in
		// BalanceResultReport.jrxml (name, number, type, balance_currency_string).
		List<AccountService.AccountBalance> balanceResults =
			AccountService.getBalanceResults(ledger);
		
		return balanceResults != null ? balanceResults : Collections.emptyList();
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getBaseName() 
	 */
	@Override public String getBaseName()
	{
		String currentDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
		String reportBaseName = "Balance_Result_Report_" + currentDateStr;
		return reportBaseName;
		
	}
	
	
}
