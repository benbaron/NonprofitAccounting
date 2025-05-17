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

public class ReportGenerator
{
	
	
	/**  
	 * Constructor ReportGenerator
	 * @param customerService
	 */
	public ReportGenerator(CustomerService customerService)
	{
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * 
	 * @param format
	 */
	public void generateAndExportReport(String format)
	{
		// Fetch data and generate the JasperPrint object
		JasperPrint jasperPrint = generateReport(); // Your method to generate the report
		
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
	 * @return
	 */
	private static JasperPrint generateReport()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 
	 * @param jasperPrint
	 * @param outputFilePath
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
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param jasperPrint
	 * @param outputFilePath
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
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param jasperPrint
	 * @param outputFilePath
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
			e.printStackTrace();
		}
		
	}
	
	
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
			e.printStackTrace();
		}
		
	}
	
	// Method to export JasperPrint to HTML
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
