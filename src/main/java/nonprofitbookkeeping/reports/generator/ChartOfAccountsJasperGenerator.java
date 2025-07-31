
package nonprofitbookkeeping.reports.generator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.reports.datasource.ChartOfAccountsRowBean;
import nonprofitbookkeeping.service.ReportService;

/**
 * Generator for the Chart of Accounts report.
 */
public class ChartOfAccountsJasperGenerator extends AbstractReportGenerator
{
	
	public static final Logger LOGGER = Logger.getLogger(ReportService.class.getName());

	/**
	 * Constructs the generator with the required {@link ReportService}.
	 *
	 * @param reportService service used to prepare report data
	 */
	public ChartOfAccountsJasperGenerator(ReportService reportService)
	{
		
	}
	
	/**
	 * 
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData()
	 */
	@Override protected List<ChartOfAccountsRowBean> getReportData()
	{
		Company company = CurrentCompany.getCompany();
		
		if (company == null || company.getChartOfAccounts() == null)
		{
			System.err.println(
				"ChartOfAccountsJasperGenerator: Company or COA is null. Cannot generate data.");
			return Collections.emptyList();
		}
		
		ChartOfAccounts coa = company.getChartOfAccounts();
		return prepareChartOfAccountsJasperData(coa);
	}
	
	@Override protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		params.put("P_REPORT_TITLE", "Chart of Accounts");
		Company company = CurrentCompany.getCompany();
		String companyName = "N/A";
		
		if (company != null && company.getCompanyProfile() != null &&
			company.getCompanyProfile().getCompanyName() != null)
		{
			companyName = company.getCompanyProfile().getCompanyName();
		}
		
		params.put("P_COMPANY_NAME", companyName);
		params.put("P_GENERATION_DATE", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
		return params;
	}
	
	@Override protected String getReportPath()
	{
		return "jrxml/ChartOfAccountsAlt.jrxml";
	}
	
	/**
	 * Prepares a list of {@link ChartOfAccountsRowBean} objects for the
	 * Chart of Accounts Jasper report.
	 *
	 * @param chartOfAccounts The company's {@link ChartOfAccounts}.
	 * @return List of beans representing each account. Returns an empty list if
	 *         the chart is null or contains no accounts.
	 */
	public
			List<ChartOfAccountsRowBean>
			prepareChartOfAccountsJasperData(ChartOfAccounts chartOfAccounts)
	{
		List<ChartOfAccountsRowBean> data = new ArrayList<>();
		
		if (chartOfAccounts == null)
		{
			LOGGER.warning("ChartOfAccounts is null - cannot prepare COA report data.");
			return data;
		}
		
		List<Account> accounts = chartOfAccounts.getAccounts();
		
		if (accounts == null)
		{
			return data;
		}
		
		accounts.sort(Comparator.comparing(Account::getAccountNumber,
			Comparator.nullsLast(String::compareTo)));
		
		for (Account acct : accounts)
		{
			if (acct == null)
				continue;
			
			String type = (acct.getAccountType() != null) ? acct.getAccountType().name() : "";
			data.add(new ChartOfAccountsRowBean(acct.getAccountNumber(), acct.getName(), type));
		}
		
		return data;
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getBaseName() 
	 */
	@Override protected String getBaseName()
	{
		return "Chart_of_Accounts_" + LocalDate.now();
	}
	
}
