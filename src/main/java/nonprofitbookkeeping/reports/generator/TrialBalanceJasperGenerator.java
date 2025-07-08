
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.TrialBalanceRowBean;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;


import java.io.File;
import java.io.FileNotFoundException; // For specific exception
import java.io.InputStream;
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

/**
 * Generates a Trial Balance report using JasperReports.
 * This class extends {@link AbstractReportGenerator} and is responsible for
 * providing the specific data, parameters, and JRXML template path for the
 * Trial Balance report. It utilizes a {@link ReportService} to prepare the data
 * based on the provided {@link ReportContext}.
 */
public class TrialBalanceJasperGenerator extends AbstractReportGenerator
{
	
	private ReportContext reportContext;
	private ReportService reportService;
	
	/**
	 * Constructs a {@code TrialBalanceJasperGenerator}.
	 *
	 * @param reportContext The {@link ReportContext} containing criteria and settings for the report,
	 *                      such as the end date for the trial balance.
	 * @param reportService The {@link ReportService} used to prepare the data (list of {@link TrialBalanceRowBean})
	 *                      for the report.
	 */
	public TrialBalanceJasperGenerator(ReportContext reportContext, ReportService reportService)
	{
		this.reportContext = reportContext;
		this.reportService = reportService;
	}
	
	/**
	 * {@inheritDoc}
	 * @return The classpath resource path "reports/TrialBalanceReport.jrxml" for the Trial Balance template.
	 * @throws ActionCancelledException Not directly thrown by this implementation, but declared due to the interface.
	 * @throws NoFileCreatedException Not directly thrown by this implementation, but declared due to the interface.
	 */
	@Override protected String getReportPath()	throws ActionCancelledException,
												NoFileCreatedException
	{
		// This JRXML already exists in src/main/resources/reports/
		return "TrialBalanceReport.jrxml";
	}
	
	/**
	 * {@inheritDoc}
	 * <p>Prepares and returns the data for the Trial Balance report.
	 * It retrieves the current company's ledger and chart of accounts, then uses the
	 * {@link ReportService} to generate a list of {@link TrialBalanceRowBean} objects
	 * based on the provided {@link ReportContext}.
	 * If essential company data is missing, an error is logged, and an empty list is returned.
	 * </p>
	 * @return A list of {@link TrialBalanceRowBean} objects for the report, or an empty list if data cannot be prepared.
	 */
	@Override protected List<TrialBalanceRowBean> getReportData()
	{
		Company company = CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
		{
			System.err.println( // Consider using a logger
				"TrialBalanceJasperGenerator: Company, Ledger, or COA is null. Cannot generate data.");
			return Collections.emptyList();
		}
		
		Ledger ledger = company.getLedger();
		ChartOfAccounts coa = company.getChartOfAccounts();
		
		return this.reportService.prepareTrialBalanceJasperData(this.reportContext, ledger, coa);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>Provides parameters for the Trial Balance report. This includes:
	 * <ul>
	 *   <li>Standard parameters (prefixed with {@code P_}): {@code P_REPORT_TITLE}, {@code P_COMPANY_NAME},
	 *       {@code P_AS_OF_DATE}, {@code P_GENERATION_DATE}.</li>
	 *   <li>JRXML-specific parameters: {@code reporttitle}, {@code dateToday}, {@code companyname}.</li>
	 * </ul>
	 * The company name and "as of" date are derived from the current company and report context.
	 * </p>
	 * @return A map of parameters for the JasperReport.
	 */
	@Override protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		
		// Standardized Parameters (using P_ convention for clarity)
		params.put("P_REPORT_TITLE", "Trial Balance");
		
		Company company = CurrentCompany.getCompany();
		String companyName = "N/A";
		
		if (company != null && company.getCompanyProfile() != null &&
			company.getCompanyProfile().getCompanyName() != null)
		{
			companyName = company.getCompanyProfile().getCompanyName();
		}
		
		params.put("P_COMPANY_NAME", companyName);
		
		String reportAsOfDate = "N/A";
		
		if (this.reportContext.getEndDate() != null)
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
			reportAsOfDate = "As of " + this.reportContext.getEndDate().format(formatter);
		}
		
		params.put("P_AS_OF_DATE", reportAsOfDate);
		params.put("P_GENERATION_DATE",
			LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
		
		// Parameters matching the existing TrialBalanceReport.jrxml:
		// <parameter name="reporttitle" class="java.lang.String"/>
		// <parameter name="dateToday" class="java.lang.String"/>
		// <parameter name="companyname" class="java.lang.String"/>
		// It seems the JRXML uses lowercase for these specific ones. Let's ensure they
		// are provided.
		params.put("reporttitle", "Trial Balance"); // Specific for JRXML
		params.put("dateToday", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)); // YYYY-MM-DD format, specific for JRXML
		params.put("companyname", companyName); // Specific for JRXML, might be redundant with P_COMPANY_NAME if JRXML is standardized
		
		return params;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>This implementation generates the "Trial Balance Report". It compiles the JRXML template,
	 * fills it with data and parameters, and exports to the specified format (PDF or HTML)
	 * using helper methods from {@link AbstractReportGenerator}.
	 * If an unsupported format is requested, it defaults to PDF.
	 * The output file is named "Trial_Balance_Report_[report_end_date_or_current_date].[format]".
	 * </p>
	 * @param format The desired output format ("pdf" or "html"). Defaults to "pdf" if unsupported.
	 * @return The generated {@link File}.
	 * @throws Exception If any error occurs during report generation, including {@link FileNotFoundException} if the JRXML template is not found.
	 */
	@Override public File generateAndExportReport(String format) throws Exception
	{
		File generatedFile = null;
		String currentDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
		String reportBaseName = "Trial_Balance_Report_" +
			(this.reportContext.getEndDate() != null ? this.reportContext.getEndDate().toString() :
				currentDateStr);
		
		String jrxmlPath = getReportPath();
		
		try (InputStream reportStream = getClass().getClassLoader().getResourceAsStream(jrxmlPath))
		{
			
			if (reportStream == null)
			{
				System.err.println("Cannot find report template: " + jrxmlPath);
				throw new FileNotFoundException("Report template not found: " + jrxmlPath);
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
				System.out.println(
					"Unsupported format for Trial Balance: " + format + ". Defaulting to PDF.");
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
