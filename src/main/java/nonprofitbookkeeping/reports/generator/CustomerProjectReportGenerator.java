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
import nonprofitbookkeeping.model.Customer;
import nonprofitbookkeeping.service.CustomerService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class CustomerProjectReportGenerator extends ReportGenerator
{
	/**  
	 * Constructor CustomerProjectReportGenerator
	 * @param accountService
	 */
	
	public CustomerProjectReportGenerator(CustomerService customerService)
	{
		super(customerService);
	}
	
	public void generateAndExportReport(String format)
	{
		
		try
		{
			// Compile JRXML into JasperReport
			String reportPath = "path/to/CustomerProjectReport.jrxml"; // Replace with the actual
																		// path
			JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);
			
			// Fetch data for the report
			List<Customer> customerData = CustomerService.getCustomerProjectData();
			
			// Fill the report with the data
			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(customerData);
			Map<String, Object> parameters = getReportParameters();
			
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
		catch (JRException e)
		{
			e.printStackTrace();
		}
		
	}
	
	private Map<String, Object> getReportParameters()
	{
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("reporttitle", "Customer/Project Report");
		parameters.put("dateToday", "2023-04-15"); // Example static date, replace as needed
		parameters.put("company", "Your Company Name");
		parameters.put("companytext", "Company Details");
		return parameters;
	}
	
	public void exportToPDF(JasperPrint jasperPrint, String outputFilePath)
	{
		
		try
		{
			JasperExportManager.exportReportToPdfFile(jasperPrint, outputFilePath);
			System.out.println("Report exported to PDF: " + outputFilePath);
		}
		catch (JRException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void exportToHTML(JasperPrint jasperPrint, String outputFilePath)
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
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args)
	{
		CustomerService customerService = new CustomerService();
		ReportGenerator reportGenerator = new ReportGenerator(customerService);
		
		// Generate and export the report to HTML or PDF
		reportGenerator.generateAndExportReport("html"); // Use "pdf" for PDF export
	}
	
}
