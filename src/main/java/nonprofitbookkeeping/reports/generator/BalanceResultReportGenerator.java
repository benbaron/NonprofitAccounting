
package nonprofitbookkeeping.reports.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException; // For exportToHTML
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections; // For Collections.emptyList()

import nonprofitbookkeeping.service.AccountService;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
// Assuming AccountService.AccountBalance or similar class that
// getBalanceResults returns
// import nonprofitbookkeeping.model.Account; // If
// AccountService.AccountBalance is actually Account

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
// JasperExportManager and HtmlExporter are used in inherited methods

public class BalanceResultReportGenerator extends AbstractReportGenerator
{
	
	/**
	 * Constructor BalanceResultReportGenerator
	 * @param accountService (currently unused as getReportData calls a static method)
	 */
	public BalanceResultReportGenerator(AccountService accountService)
	{
		// accountService parameter is not currently used by this generator's methods
		// as getReportData() calls AccountService.getBalanceResults() statically.
		// If AccountService were to become an instance service, this would need to
		// change.
	}
	
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
		// parameters.put("P_COMPANY_DETAILS", companyDetailsText); // If JRXML uses
		// this
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
	
	@Override protected String getReportPath()	throws ActionCancelledException,
												NoFileCreatedException
	{
		// Path relative to the root of the classpath (e.g., src/main/resources)
		return "reports/BalanceResultReport.jrxml";
	}
	
	@Override protected List<?> getReportData()
	{
		// This method fetches data using a static call.
		// The data structure returned by AccountService.getBalanceResults()
		// must be a List of JavaBeans compatible with the fields defined in
		// BalanceResultReport.jrxml.
		// Fields in BalanceResultReport.jrxml: name, number, type,
		// balance_currency_string
		List<AccountService.AccountBalance> balanceResults = AccountService.getBalanceResults();
		return balanceResults != null ? balanceResults : Collections.emptyList();
	}
	
	@Override public File generateAndExportReport(String format) throws Exception
	{
		File generatedFile = null;
		String currentDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
		String reportBaseName = "Balance_Result_Report_" + currentDateStr;
		
		String jrxmlPath = getReportPath();
		
		if (jrxmlPath == null || jrxmlPath.trim().isEmpty())
		{
			throw new NoFileCreatedException(
				"JRXML path not specified in BalanceResultReportGenerator.");
		}
		
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
				System.out.println("Unsupported format for Balance Result Report: " + format +
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
