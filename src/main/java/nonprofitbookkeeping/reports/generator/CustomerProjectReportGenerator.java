/**
 * nonprofit-scaledger-ribbon.zip_expanded CustomerProjectReportGenerator.java
 * CustomerProjectReportGenerator
 */

package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.Customer;
import nonprofitbookkeeping.service.CustomerService;

import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


/**
 * Generates a "Customer/Project Report" using JasperReports.
 * This class extends {@link ReportGenerator} and is responsible for compiling a JRXML template,
 * fetching customer project data using {@link CustomerService}, filling the report,
 * and exporting it to PDF or HTML format.
 */
public class CustomerProjectReportGenerator extends ReportGenerator
{
	/**
	 * Constructs a {@code CustomerProjectReportGenerator}.
	 *
	 * @param customerService The {@link CustomerService} instance used to fetch data for the report.
	 *                        It is passed to the superclass constructor.
	 */
	public CustomerProjectReportGenerator(CustomerService customerService)
	{
		super(customerService);
	}
	
	/**
	 * Generates and exports the Customer/Project report to the specified format.
	 * This method compiles the "CustomerProjectReport.jrxml" template (path needs to be correctly configured),
	 * fetches customer data using {@link CustomerService#getCustomerProjectData()},
	 * fills the report, and then exports it to either PDF ("CustomerProjectReport.pdf")
	 * or HTML ("CustomerProjectReport.html") in the application's root directory.
	 * Errors during the process are printed to the standard error stream.
	 *
	 * @param format A string indicating the desired output format. Accepts "pdf" or "html" (case-insensitive).
	 *               If another format is provided, no export will occur.
	 */
	@Override public void generateAndExportReport(String format)
	{
		
		try
		{
			
			// Compile JRXML into JasperReport from the classpath
			try (var in =
				CustomerProjectReportGenerator.class.getResourceAsStream("/CustomerReport.jrxml"))
			{
				
				if (in == null)
				{
					System.err.println("CustomerReport.jrxml not found in resources");
					return;
				}
				
				JasperReport jasperReport = JasperCompileManager.compileReport(in);
			}
			catch (IOException e)
			{
				System.err.println("Failed to read CustomerReport.jrxml: " + e.getMessage());
				e.printStackTrace();
				return; // Abort generation if template cannot be read
			}
			
		}
		catch (JRException e)
		{
			e.printStackTrace(); // Consider more robust error handling/logging
		}
		
		// Fetch data for the report
		List<Customer> customerData = CustomerService.getCustomerProjectData();
		
		// Fill the report with the data
		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(customerData);
		Map<String, Object> parameters = getReportParameters();
		
		try (InputStream reportStream =
			getClass().getClassLoader().getResourceAsStream(getReportPath()))
		{
			JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
			
			JasperPrint jasperPrint =
				JasperFillManager.fillReport(jasperReport, parameters, dataSource);
			
			// Export report to the desired format (PDF, HTML)
			if ("pdf".equalsIgnoreCase(format))
			{
				exportToPDF(jasperPrint, "CustomerProjectReport.pdf");
			}
			else if ("html".equalsIgnoreCase(format))
			{
				exportToHTML(jasperPrint, "CustomerProjectReport.html");
			}
			
		}
		catch (Exception e)
		{
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 * @return The classpath resource path "reports/TrialBalanceReport.jrxml" for the Trial Balance template.
	 * @throws ActionCancelledException Not directly thrown by this implementation, but declared due to the interface.
	 * @throws NoFileCreatedException Not directly thrown by this implementation, but declared due to the interface.
	 */
	protected static String getReportPath() throws ActionCancelledException,
											NoFileCreatedException
	{
		return "reports/CustomerProjectReport.jrxml";
	}
	
	/**
	 * Retrieves the parameters required for the Customer/Project report.
	 * This includes a report title, a static date, and placeholder company information.
	 *
	 * @return A {@link Map} containing parameter names as keys and their corresponding values.
	 */
	private static Map<String, Object> getReportParameters()
	{
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("reporttitle", "Customer/Project Report");
		parameters.put("dateToday", "2023-04-15"); // Example static date, replace as needed or make
													// dynamic
		parameters.put("company", "Your Company Name"); // Placeholder, should be dynamic
		parameters.put("companytext", "Company Details"); // Placeholder, should be dynamic
		return parameters;
	}
	
	/**
	 * Exports the provided {@link JasperPrint} object to a PDF file.
	 * The output file path is specified, and a success message or stack trace is printed to standard output/error.
	 *
	 * @param jasperPrint The compiled and filled {@link JasperPrint} object to export.
	 * @param outputFilePath The path (including filename) where the PDF report will be saved.
	 */
	public static void exportToPDF(JasperPrint jasperPrint, String outputFilePath)
	{
		
		try
		{
			JasperExportManager.exportReportToPdfFile(jasperPrint, outputFilePath);
			System.out.println("Report exported to PDF: " + outputFilePath);
		}
		catch (JRException e)
		{
			e.printStackTrace(); // Consider more robust error handling/logging
		}
		
	}
	
	/**
	 * Exports the provided {@link JasperPrint} object to an HTML file.
	 * The output file path is specified, and a success message or stack trace is printed to standard output/error.
	 *
	 * @param jasperPrint The compiled and filled {@link JasperPrint} object to export.
	 * @param outputFilePath The path (including filename) where the HTML report will be saved.
	 */
	public static void exportToHTML(JasperPrint jasperPrint, String outputFilePath)
	{
		
		try
		{
			HtmlExporter exporter = new HtmlExporter();
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputFilePath));
			exporter.exportReport();
			System.out.println("Report exported to HTML: " + outputFilePath);
		}
		catch (JRException e)
		{
			e.printStackTrace(); // Consider more robust error handling/logging
		}
		
	}
	
	/**
	 * Main method for demonstrating or testing the generation of the Customer/Project report.
	 * It instantiates a {@link CustomerService} and {@link ReportGenerator} (which seems to be the base class here,
	 * though {@code CustomerProjectReportGenerator} itself is a more specific generator),
	 * then calls {@code generateAndExportReport} to produce an HTML report by default.
	 *
	 * @param args Command line arguments (not used).
	 */
	public static void main(String[] args)
	{
		CustomerService customerService = new CustomerService();
		ReportGenerator reportGenerator = new ReportGenerator(customerService);
		
		// Generate and export the report to HTML or PDF
		reportGenerator.generateAndExportReport("html"); // Use "pdf" for PDF export
	}
	
}
