
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.CashFlowStatementRowBean;
import nonprofitbookkeeping.service.ReportService;

import java.io.File;
import java.io.InputStream;
import java.io.FileNotFoundException; // Added for explicit exception
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
// Export related imports are not needed if using inherited methods from
// AbstractReportGenerator

/**
 * Generates a Cash Flow Statement report using JasperReports. This class
 * extends {@link AbstractReportGenerator} and is responsible for providing the
 * specific data, parameters, and JRXML template path for the Cash Flow
 * Statement. It utilizes a {@link ReportService} to prepare the data.
 */
public class CashFlowStatementJasperGenerator extends AbstractReportGenerator
{
	
	private ReportContext reportContext;
	private ReportService reportService;
	
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
		this.reportService = reportService2;
	}
	
	/**
	 * {@inheritDoc}
	 * @return The classpath resource path "jrxml/cash_flow_statement.jrxml" for the Cash Flow Statement template.
	 */
	@Override protected String getReportPath()
	{
		return "jrxml/cash_flow_statement.jrxml";
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
		
		return this.reportService.prepareCashFlowStatementJasperData(this.reportContext, ledger,
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
	 * {@inheritDoc}
	 * <p>This implementation generates the "Cash Flow Statement". It compiles the JRXML template,
	 * fills it with data and parameters, and exports to the specified format (PDF or HTML)
	 * using helper methods from {@link AbstractReportGenerator}.
	 * If an unsupported format is requested, it defaults to PDF.
	 * The output file is named "Cash_Flow_Statement_Report_[current_date].[format]".
	 * </p>
	 * @param format The desired output format ("pdf" or "html"). Defaults to "pdf" if unsupported.
	 * @return The generated {@link File}.
	 * @throws Exception If any error occurs during report generation, including {@link FileNotFoundException} if the JRXML template is not found.
	 */
	@Override public File generateAndExportReport(String format) throws Exception
	{
		File generatedFile = null;
		String currentDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE); // YYYY-MM-DD
		String reportBaseName = "Cash_Flow_Statement_Report_" + currentDateStr;
		
		
		try (InputStream reportStream =
			getClass().getClassLoader().getResourceAsStream(getReportPath()))
		{
			
			if (reportStream == null)
			{
				System.err.println("Cannot find report template: " + getReportPath());
				throw new FileNotFoundException("Report template not found: " + getReportPath());
			}
			
			JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
			
			List<?> reportDataList = getReportData();
			JRDataSource dataSource = new JRBeanCollectionDataSource(reportDataList);
			
			Map<String, Object> parameters = getReportParameters();
			
			JasperPrint jasperPrint =
				JasperFillManager.fillReport(jasperReport, parameters, dataSource);
			
			File outputDir = new File(getOutputDirectory());
			
			if (!outputDir.exists())
			{
				outputDir.mkdirs();
			}
			
			String outputFileName = reportBaseName + "." + format.toLowerCase();
			File outputFile = new File(outputDir, outputFileName);
			
			if ("pdf".equalsIgnoreCase(format))
			{
				generatedFile = exportToPDF(jasperPrint, outputFile.getAbsolutePath());
			}
			else if ("html".equalsIgnoreCase(format))
			{
				generatedFile = exportToHTML(jasperPrint, outputFile.getAbsolutePath());
			}
			else
			{
				System.out.println("Unsupported format for Cash Flow Statement: " + format +
					". Defaulting to PDF.");
				File defaultOutputFile = new File(outputDir, reportBaseName + ".pdf");
				generatedFile = exportToPDF(jasperPrint, defaultOutputFile.getAbsolutePath());
			}
			
			if (generatedFile != null && generatedFile.exists())
			{
				System.out.println(reportBaseName + " generated successfully at: " +
					generatedFile.getAbsolutePath());
			}
			else
			{
				String attemptedPath = (generatedFile != null) ? generatedFile.getAbsolutePath() :
					outputFile.getAbsolutePath();
				System.err.println("Report file " + attemptedPath +
					" was not created or found after export attempt.");
				throw new FileNotFoundException(
					"Generated report file could not be confirmed after export: " + attemptedPath);
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		
		return generatedFile;
	}
	
}
