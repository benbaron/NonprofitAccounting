
package nonprofitbookkeeping.reports.generator;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nonprofitbookkeeping.service.AccountService;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.Company;

public class BalanceResultReportGenerator extends AbstractReportGenerator
{
	
	/**
	 * Constructor BalanceResultReportGenerator
	 * @param accountService
	 */
	public BalanceResultReportGenerator(AccountService accountService)
	{
	}
	
	// Override method to set parameters specific to this report
	@Override protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> parameters = new HashMap<>();
		
		// Set the report title
		parameters.put("reporttitle", "Balance Result Report");
		
		// Set the current date dynamically
		parameters.put("dateToday", LocalDate.now().toString()); // Gets the current date (e.g.,
																// "2025-04-15")
		
		// Fetch real company details from the Company model/service
		Company company = getCompanyDetails(); // You need to implement this method
		parameters.put("company", Company.getCompany().getCompanyProfile().getCompanyName()); // Assuming Company class has a getName()
														// method
		parameters.put("companytext", company.getCompanyProfile().toString()); // Assuming Company class has a
																// getDetails() method
		
		return parameters;
	}
	
	// Override method to provide the path to the JRXML file
	@Override protected String getReportPath() throws ActionCancelledException, NoFileCreatedException
	{
		return null;
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
	@Override protected List<?> getReportData()
	{
		// Fetch real data for the report
		// Assuming AccountService has a method that returns a list of balances or
		// transaction data for the report
		return AccountService.getBalanceResults(); // Modify based on your service's actual method
	}
	
	// This is just an example method for fetching company details
	private static Company getCompanyDetails()
	{
		// Assuming a CompanyService or similar exists to fetch company details
		// You can replace this with real logic to fetch company data, maybe from a
		// database or config
		return null;
	}
	
}
