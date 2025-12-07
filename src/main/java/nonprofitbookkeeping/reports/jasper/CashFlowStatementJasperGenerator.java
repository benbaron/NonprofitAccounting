
package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.CashFlowStatementRowBean;
import nonprofitbookkeeping.service.ReportService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Generates a Cash Flow Statement report using JasperReports. This class
 * extends {@link AbstractReportGenerator} and is responsible for providing the
 * specific data, parameters, and JRXML template path for the Cash Flow
 * Statement. It utilizes a {@link ReportService} to prepare the data.
 */
public class CashFlowStatementJasperGenerator extends AbstractReportGenerator
{
	
	private ReportContext reportContext;
	/**
	 * Constructs a {@code CashFlowStatementJasperGenerator}.
	 *
	 * @param reportContext The {@link ReportContext} containing criteria and settings for the report.
	 * @param reportService2 The {@link ReportService} used to prepare the data for the report.
	 */
	public CashFlowStatementJasperGenerator(ReportContext reportContext,
		ReportService reportService2)
	{
		this.reportContext = reportContext;
	}
	
        /** {@inheritDoc} */
        @Override protected String getReportPath()
        {
                return bundledReportPath();
        }
	
	/**
	 * {@inheritDoc}
	 * <p>Prepares and returns the data for the Cash Flow Statement.
	 * It retrieves the current company's ledger and chart of accounts, then uses the
	 * {@link ReportService} to generate a list of {@link CashFlowStatementRowBean} objects.
	 * If essential company data (company, ledger, or chart of accounts) is missing,
	 * an error is logged, and an empty list is returned.
	 * </p>
	 * @return A list of {@link CashFlowStatementRowBean} objects for the report, or an empty list if data cannot be prepared.
	 */
	@Override protected List<CashFlowStatementRowBean> getReportData()
	{
		Company company = CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
		{
			System.err.println(
				"CashFlowStatementJasperGenerator: Company, Ledger, or COA is null. Cannot generate data."); 
			return Collections.emptyList();
		}
		
		Ledger ledger = company.getLedger();
		ChartOfAccounts coa = company.getChartOfAccounts();
		
		return CashFlowStatementJasperGenerator.prepareCashFlowStatementJasperData(this.reportContext, ledger,
			coa);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>Provides parameters for the Cash Flow Statement report. This includes:
	 * <ul>
	 *   <li>{@code P_REPORT_TITLE}: "Cash Flow Statement"</li>
	 *   <li>{@code P_COMPANY_NAME}: The name of the current company, or "N/A".</li>
	 *   <li>{@code P_REPORT_PERIOD}: A formatted string representing the report period (start date - end date), or "N/A".</li>
	 *   <li>{@code P_GENERATION_DATE}: The current date, formatted.</li>
	 * </ul>
	 * </p>
	 * @return A map of parameters for the JasperReport.
	 */
	@Override protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		params.put("P_REPORT_TITLE", "Cash Flow Statement");
		
		Company company = CurrentCompany.getCompany();
		String companyName = "N/A";
		
		if (company != null && company.getCompanyProfile() != null &&
			company.getCompanyProfile().getCompanyName() != null)
		{
			companyName = company.getCompanyProfile().getCompanyName();
		}
		
		params.put("P_COMPANY_NAME", companyName);
		
		String reportPeriod = "N/A";
		
		if (this.reportContext.getStartDate() != null && this.reportContext.getEndDate() != null)
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
			reportPeriod = this.reportContext.getStartDate().format(formatter) + " - " +
				this.reportContext.getEndDate().format(formatter);
		}
		
		params.put("P_REPORT_PERIOD", reportPeriod);
		params.put("P_GENERATION_DATE",
			LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
		
		return params;
	}
	


	/**
	 * @param reportContext
	 * @param ledger
	 * @param coa
	 * @return
	 */
	public static
			List<CashFlowStatementRowBean>
			prepareCashFlowStatementJasperData(	ReportContext reportContext, nonprofitbookkeeping.model.Ledger ledger,
												nonprofitbookkeeping.model.ChartOfAccounts coa)
	{
		return new ArrayList<>();
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getBaseName() 
	 */
	@Override public String getBaseName()
	{
		String currentDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE); // YYYY-MM-DD

		return "Cash_Flow_Statement_Report_" + currentDateStr;
		
	}
	
}
