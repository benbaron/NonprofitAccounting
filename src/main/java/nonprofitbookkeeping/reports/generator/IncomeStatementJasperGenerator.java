
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.IncomeStatementRowBean;
import nonprofitbookkeeping.service.ReportService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.io.File; // Added import
import java.io.InputStream;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
// JasperExportManager and HtmlExporter related imports are not needed here
// if exportToPDF and exportToHTML are properly inherited from
// AbstractReportGenerator.

/**
 * Generates an Income Statement (Profit & Loss) report using JasperReports.
 * This class extends {@link AbstractReportGenerator} and is responsible for
 * providing the specific data, parameters, and JRXML template path for the
 * Income Statement. It utilizes a {@link ReportService} to prepare the data and
 * a {@link ReportContext} for report criteria.
 */
public class IncomeStatementJasperGenerator extends AbstractReportGenerator
{
	
	private ReportContext reportContext;
	private ReportService reportService;
	
	/**
	 * Constructs an {@code IncomeStatementJasperGenerator}.
	 *
	 * @param reportContext The {@link ReportContext} containing criteria and settings for the report.
	 * @param reportService The {@link ReportService} used to prepare the data for the report.
	 */
	public IncomeStatementJasperGenerator(ReportContext reportContext, ReportService reportService)
	{
		this.reportContext = reportContext;
		this.reportService = reportService;
	}
	
	/**
	 * {@inheritDoc}
	 * @return The classpath resource path "jrxml/income_statement.jrxml" for the Income Statement template.
	 */
	@Override protected String getReportPath()
	{
		// Path relative to the resources directory
		return "jrxml/income_statement.jrxml";
	}
	
	/**
	 * {@inheritDoc}
	 * <p>Prepares and returns the data for the Income Statement.
	 * It retrieves the current company's ledger and chart of accounts, then uses the
	 * {@link ReportService} to generate a list of {@link IncomeStatementRowBean} objects
	 * based on the provided {@link ReportContext}.
	 * If essential company data is missing, an error is logged, and an empty list is returned.
	 * </p>
	 * @return A list of {@link IncomeStatementRowBean} objects for the report, or an empty list if data cannot be prepared.
	 */
	@Override protected List<IncomeStatementRowBean> getReportData()
	{
		Company company = CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
		{
			System.err.println(
				"IncomeStatementJasperGenerator: Company, Ledger, or COA is null. Cannot generate data.");
			return Collections.emptyList();
		}
		
		Ledger ledger = company.getLedger();
		ChartOfAccounts coa = company.getChartOfAccounts();
		
		return this.reportService.prepareIncomeStatementJasperData(this.reportContext, ledger, coa);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>Provides parameters for the Income Statement report. This includes:
	 * <ul>
	 *   <li>{@code P_REPORT_TITLE}: "Income Statement"</li>
	 *   <li>{@code P_COMPANY_NAME}: The name of the current company, or "N/A".</li>
	 *   <li>{@code P_REPORT_PERIOD}: A formatted string representing the report period (start date - end date), or "N/A".</li>
	 *   <li>{@code P_GENERATION_DATE}: The current date, formatted.</li>
	 * </ul>
	 * Parameters for Net Income are assumed to be calculated within the JRXML or are part of the bean list.
	 * </p>
	 * @return A map of parameters for the JasperReport.
	 */
	@Override protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		params.put("P_REPORT_TITLE", "Income Statement");
		
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
		
		// Net Income parameter calculation can be added here if needed by JRXML
		// List<IncomeStatementRowBean> data = getReportData(); // This might be
		// inefficient if called again
		// Consider calculating sums from the data if not done by Jasper itself.
		// For now, assuming JRXML handles summary or it's part of the bean list.
		
		return params;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>This implementation generates the "Income Statement". It compiles the JRXML template,
	 * fills it with data and parameters, and exports to the specified format (PDF or HTML)
	 * using helper methods from {@link AbstractReportGenerator}.
	 * If an unsupported format is requested, it defaults to PDF.
	 * The output file is named "Income_Statement_[report_end_date_or_current_date].[format]".
	 * </p>
	 * @param format The desired output format ("pdf" or "html"). Defaults to "pdf" if unsupported.
	 * @return The generated {@link File}.
	 * @throws Exception If any error occurs during report generation, including {@link java.io.FileNotFoundException} if the JRXML template is not found.
	 */
	@Override public File generateAndExportReport(String format) throws Exception
	{
		File generatedFile = null;
		String reportBaseName = "Income_Statement_" + (this.reportContext.getEndDate() != null ?
			this.reportContext.getEndDate().toString() : LocalDate.now().toString());
		
		try
		{
			Class<? extends AbstractReportGenerator> clazz = getClass();
			ClassLoader clazzloader = clazz.getClassLoader();
			InputStream reportStream = clazzloader.getResourceAsStream(getReportPath());
			
			if (reportStream == null)
			{
				System.err.println("Cannot find report template: " + getReportPath());
				
				throw new java.io.FileNotFoundException(
					"Report template not found: " + getReportPath());
			}
			
			JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
			
			List<?> reportDataList = getReportData();
			JRDataSource dataSource = new JRBeanCollectionDataSource(reportDataList);
			
			Map<String, Object> parameters = getReportParameters();
			
			JasperPrint jasperPrint =
				JasperFillManager.fillReport(jasperReport, parameters, dataSource);
			
			File outputDir = new File(getOutputDirectory()); // Method from AbstractReportGenerator
			
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
				System.out.println(
					"Unsupported format for Income Statement: " + format + ". Defaulting to PDF.");
				File defaultOutputFile = new File(outputDir, reportBaseName + ".pdf");
				generatedFile = exportToPDF(jasperPrint, defaultOutputFile.getAbsolutePath());
			}
			
			if (generatedFile != null && generatedFile.exists())
			{ // Check existence after export
				System.out.println(reportBaseName + " generated successfully at: " +
					generatedFile.getAbsolutePath());
			}
			else
			{
				System.err
					.println("Report generation failed or file not found for: " + reportBaseName);
				// If generatedFile is null from export methods (if they can return null on
				// failure) or if the file doesn't exist after export call.
				throw new Exception(
					"Report file was not created or found: " + outputFile.getAbsolutePath());
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
