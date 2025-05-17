
package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.*;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;

import java.util.List;
import java.util.Map;

public abstract class AbstractReportGenerator
{	
	// Abstract method to get data specific to the report
	protected abstract List<?> getReportData();
	
	// Abstract method to set report parameters
	protected abstract Map<String, Object> getReportParameters();
	
	// Method to generate the report and export to a given format (e.g., PDF, HTML)
	public void generateAndExportReport(String format)
	{
		
		try
		{
			// Step 1: Compile the JRXML file into a JasperReport
			String reportPath = getReportPath(); // This will be overridden by the child class
			JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);
			
			// Step 2: Get the data specific to this report
			List<?> reportData = getReportData();
			
			// Step 3: Use JRBeanCollectionDataSource to pass the data for the report
			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);
			
			// Step 4: Set report parameters (specific to the report)
			Map<String, Object> parameters = getReportParameters();
			
			// Step 5: Fill the report with data and parameters
			JasperPrint jasperPrint =
				JasperFillManager.fillReport(jasperReport, parameters, dataSource);
			
			// Step 6: Export the filled report
			if ("pdf".equalsIgnoreCase(format))
			{
				exportToPDF(jasperPrint, "Report.pdf");
			}
			else if ("html".equalsIgnoreCase(format))
			{
				exportToHTML(jasperPrint, "Report.html");
			}
			
		}
		catch (JRException e)
		{
			e.printStackTrace();
		}
		catch (ActionCancelledException e)
		{
			e.printStackTrace();
		}
		catch (NoFileCreatedException e)
		{
			e.printStackTrace();
		}
		
	}
	
	// Method to export to PDF
	protected static void exportToPDF(JasperPrint jasperPrint, String outputFilePath)
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
	
	// Method to export to HTML
	protected static void exportToHTML(JasperPrint jasperPrint, String outputFilePath)
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
	
	// Abstract method to get the path to the JRXML file
	protected abstract String getReportPath() throws ActionCancelledException, NoFileCreatedException;
	
}
