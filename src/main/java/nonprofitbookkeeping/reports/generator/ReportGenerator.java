/**
 * nonprofit-scaledger-ribbon.zip_expanded ReportGenerator.java ReportGenerator
 */

package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.export.*;
import nonprofitbookkeeping.service.CustomerService;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * A general-purpose report generator class that utilizes JasperReports to create reports
 * in various formats (PDF, XLSX, CSV, TXT, HTML).
 * It relies on a stubbed {@link #generateReport()} method to produce a {@link JasperPrint} object,
 * which is then exported using format-specific helper methods.
 * This class can be extended by more specific report generators that provide concrete
 * implementations for report data and parameters.
 */
public class ReportGenerator
{
	
	/** Service used to retrieve customer information for reports. */
	protected final CustomerService customerService;
	
	
	/**  
	 * Constructs a {@code ReportGenerator}.
	 * The provided {@code customerService} parameter is not used in the current
	 * stub implementation of this constructor.
	 *
	 * @param customerService A {@link CustomerService} instance. Currently unused.
	 */
	public ReportGenerator(CustomerService customerService)
	{
		this.customerService = customerService;
	}
	
	
	/**
	 * Generates a report (currently using a stub {@link #generateReport()} method)
	 * and exports it to the specified format.
	 * Supported formats are "pdf", "xlsx", "csv", "txt", and "html" (case-insensitive).
	 * If an unsupported format is provided, a message is printed to standard output.
	 * 
	 * @param format The desired output format string.
	 */
	public void generateAndExportReport(String format)
	{
		// Fetch data and generate the JasperPrint object
		JasperPrint jasperPrint = generateReport(); // Your method to generate the report
		
		if (jasperPrint == null)
		{
			System.out.println(
				"JasperPrint object is null, cannot generate report for format: " + format);
			return;
		}
		
		switch(format.toLowerCase())
		{
			case "pdf":
				exportToPDF(jasperPrint, "report.pdf");
				break;
			
			case "xlsx":
				exportToXLSX(jasperPrint, "report.xlsx");
				break;
			
			case "csv":
				exportToCSV(jasperPrint, "report.csv");
				break;
			
			case "txt":
				exportToTXT(jasperPrint, "report.txt");
				break;
			
			case "html":
				exportToHTML(jasperPrint, "report.html");
				break;
			
			default:
				System.out.println("Unsupported format: " + format);
				break;
		}
		
	}
	
	/**
	 * Generates the {@link JasperPrint} for a simple report using the
	 * {@code AccountSummaryReport.jrxml} template bundled with the
	 * application. The method compiles the template from the classpath,
	 * fills it using an empty data source, and returns the resulting
	 * {@link JasperPrint} instance. If any step fails, {@code null} is
	 * returned and the exception is printed to the console.
	 *
	 * <p>
	 * This implementation is intentionally generic so subclasses can either
	 * reuse it or provide their own more elaborate logic.
	 * </p>
	 *
	 * @return the filled {@link JasperPrint}, or {@code null} on error.
	 */
	private static JasperPrint generateReport()
	{
		
		try
		{
			
			try (var in = ReportGenerator.class
				.getResourceAsStream("/jrxml/AccountSummary.jrxml"))
			{
				
				if (in == null)
				{
					System.err.println("Report template not found");
					return null;
				}
				
				JasperReport jasperReport = JasperCompileManager.compileReport(in);
				JRDataSource dataSource = new JREmptyDataSource();
				return JasperFillManager.fillReport(jasperReport, Map.of(), dataSource);
			}
			
		}
		catch (JRException | IOException e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * Exports the given {@link JasperPrint} object to a PDF file.
	 * A success message or stack trace is printed to standard output/error.
	 * 
	 * @param jasperPrint The compiled and filled {@link JasperPrint} object to export.
	 * @param outputFilePath The path (including filename) where the PDF report will be saved.
	 */
	public static void exportToPDF(JasperPrint jasperPrint, String outputFilePath)
	{
		
		try
		{
			// Export the JasperPrint object to a PDF file
			JasperExportManager.exportReportToPdfFile(jasperPrint, outputFilePath);
			System.out.println("Report exported to PDF: " + outputFilePath);
		}
		catch (JRException e)
		{
			e.printStackTrace(); // Consider more robust error handling
		}
		
	}
	
	/**
	 * Exports the given {@link JasperPrint} object to an XLSX (Excel) file.
	 * A success message or stack trace is printed to standard output/error.
	 * 
	 * @param jasperPrint The compiled and filled {@link JasperPrint} object to export.
	 * @param outputFilePath The path (including filename) where the XLSX report will be saved.
	 */
	public static void exportToXLSX(JasperPrint jasperPrint, String outputFilePath)
	{
		
		try
		{
			// Create an exporter for Excel
			JRXlsExporter exporter = new JRXlsExporter();
			
			// Set the JasperPrint object
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			
			// Set the output file path
			exporter.setExporterOutput(
				new SimpleOutputStreamExporterOutput(new FileOutputStream(outputFilePath)));
			
			// Set the configuration for XLSX export
			XlsxExporterConfiguration configuration = new SimpleXlsxExporterConfiguration();
			exporter.setConfiguration(configuration);
			
			// Export the report
			exporter.exportReport();
			System.out.println("Report exported to XLSX: " + outputFilePath);
		}
		catch (Exception e) // Catches JRException and IOException
		{
			e.printStackTrace(); // Consider more robust error handling
		}
		
	}
	
	/**
	 * Exports the given {@link JasperPrint} object to a CSV file.
	 * A success message or stack trace is printed to standard output/error.
	 * 
	 * @param jasperPrint The compiled and filled {@link JasperPrint} object to export.
	 * @param outputFilePath The path (including filename) where the CSV report will be saved.
	 */
	public static void exportToCSV(JasperPrint jasperPrint, String outputFilePath)
	{
		
		try
		{
			// Create an exporter for CSV
			JRCsvExporter exporter = new JRCsvExporter();
			
			// Set the JasperPrint object
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			
			// Set the output file path
			exporter
				.setExporterOutput(new SimpleWriterExporterOutput(new FileWriter(outputFilePath)));
			
			// Export the report to CSV
			exporter.exportReport();
			System.out.println("Report exported to CSV: " + outputFilePath);
		}
		catch (JRException | IOException e)
		{
			e.printStackTrace(); // Consider more robust error handling
		}
		
	}
	
	/**
	 * Exports the given {@link JasperPrint} object to a plain text (TXT) file.
	 * A success message or stack trace is printed to standard output/error.
	 *
	 * @param jasperPrint The compiled and filled {@link JasperPrint} object to export.
	 * @param outputFilePath The path (including filename) where the TXT report will be saved.
	 */
	public static void exportToTXT(JasperPrint jasperPrint, String outputFilePath)
	{
		
		try
		{
			// Create an exporter for Text
			JRTextExporter exporter = new JRTextExporter();
			
			// Set the JasperPrint object
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			
			// Set the output file path
			exporter
				.setExporterOutput(new SimpleWriterExporterOutput(new FileWriter(outputFilePath)));
			
			// Export the report to a text file
			exporter.exportReport();
			System.out.println("Report exported to TXT: " + outputFilePath);
		}
		catch (JRException | IOException e)
		{
			e.printStackTrace(); // Consider more robust error handling
		}
		
	}
	
	/**
	 * Exports the given {@link JasperPrint} object to an HTML file.
	 * A success message or stack trace is printed to standard output/error.
	 *
	 * @param jasperPrint The compiled and filled {@link JasperPrint} object to export.
	 * @param outputFilePath The path (including filename) where the HTML report will be saved.
	 */
	public static void exportToHTML(JasperPrint jasperPrint, String outputFilePath)
	{
		
		try
		{
			// Create an exporter for HTML
			HtmlExporter exporter = new HtmlExporter();
			
			// Set the JasperPrint object as the input for the exporter
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			
			// Set the output file path for the HTML export
			exporter.setExporterOutput(
				new SimpleHtmlExporterOutput(new FileOutputStream(outputFilePath)));
			
			// Configure exporter if necessary (e.g., to improve formatting or control page
			// sizes)
			exporter.setConfiguration(new SimpleHtmlExporterConfiguration());
			
			// Export the report to HTML
			exporter.exportReport();
			
			System.out.println("Report successfully exported to HTML: " + outputFilePath);
		}
		catch (JRException | IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	
}
