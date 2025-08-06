
package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.*;


import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.datasource.scareports.RowReportBinder;
import nonprofitbookkeeping.reports.datasource.scareports.RowReportBinder.TableSpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for report generators using JasperReports.
 * This class provides a template and common functionalities for generating various types of reports.
 * Subclasses must implement methods to provide report-specific data, parameters, and the JRXML template path.
 * It includes helper methods for exporting reports to PDF and HTML formats.
 */
public abstract class AbstractReportGenerator
{
	
	/**
	 * Retrieves the collection of data beans that will populate the report.
	 * Subclasses must implement this to provide the specific dataset for their report.
	 *
	 * @return A {@link List} of objects (JavaBeans) to be used as the report's data source.
	 *         The exact type of objects in the list depends on the specific report.
	 */
	protected abstract List<?> getReportData();
	
	/**
	 * Retrieves the parameters to be passed to the report during filling.
	 * These parameters can be used within the JRXML template to customize the report's appearance or data.
	 *
	 * @return A {@link Map} where keys are parameter names (String) and values are parameter objects.
	 */
	protected abstract Map<String, Object> getReportParameters();
	
	/**
	 * Gets the classpath resource path to the JRXML template file for the report.
	 * Subclasses must implement this to specify the design file for their report.
	 *
	 * @return A string representing the classpath path to the JRXML file (e.g., "/reports/MyReport.jrxml").
	 * @throws ActionCancelledException If determining the report path involves an action that is cancelled.
	 * @throws NoFileCreatedException If the report template file cannot be found or accessed.
	 */
	protected abstract String getReportPath()	throws ActionCancelledException,
												NoFileCreatedException;
	
	
	/**
	 * Gets the output file base name
	 *  
	 * @return base name
	 */
	protected abstract String getBaseName();
	
	/**
	 * Generates the report by compiling the JRXML template, filling it with data and parameters,
	 * and then exports it to the specified output format.
	 * Subclasses must implement this method to define the complete report generation workflow.
	 *
	 * @param format The desired output format for the report (e.g., "pdf", "html").
	 *               Supported formats depend on the export helper methods used.
	 * @return The generated {@link File} object representing the exported report.
	 * @throws Exception If any error occurs during report compilation, filling, or exporting.
	 */
	public File generateAndExportReport(String format) throws Exception
	{
		// get the input
		File jrxmlFile = getJasperFilePath();
		
		// compile the input
		JasperPrint print = compileJasperInput(jrxmlFile, null, null, null, null);
		
		return writeJasperOutput(format, print, getBaseName());
		
	}
	
	/**
	 * Gets the file path to a jasper report
	 * 
	 * @return : the path
	 * @throws RuntimeException
	 */
	File getJasperFilePath() throws RuntimeException
	{
		System.out.println("Working dir: " + new File(".").getAbsolutePath());
		Path baseDir = Paths.get(System.getProperty("user.dir")); // runtime working dir
		System.out.println("base dir:" + baseDir);
		File jrxmlFile = null;
		
		try
		{
			jrxmlFile = new File(getReportPath());
		}
		catch (ActionCancelledException | NoFileCreatedException e)
		{
			e.printStackTrace();
		}
		
		if (!jrxmlFile.exists())
		{
			throw new RuntimeException("JRXML file not found: " + jrxmlFile.getAbsolutePath());
		}
		
		return jrxmlFile;
		
	}
	
	/**
	 * Wraps an immutable map so it is suitable for jasper (which needs a 
	 * writeable one to operate)
	 * 
	 * @param original : report
	 * @return mutable reports
	 */
	public static	Map<String, Object>
			ensureMutableParameters(Map<String, Object> original)
	{
		return (original instanceof HashMap) ?
				original : new HashMap<>(original);
		
	}
	
	/**
	 * Compiles a report from the given jrxml file template
	 * 
	 * @param jrxmlFile : template
	 * @return printed report
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws JRException 
	 */
	
	
	JasperPrint compileJasperInput(File jrxmlFile,
	                               String orgName,
	                               String reportTitle,
	                               List<TableSpec<?>> tableSpecs,
	                               Map<String,Object> extraParams)
	        throws IOException, JRException {

	    /* RowReportBinder does everything:
	       – compiles the template
	       – builds the parameter map (rows, totals, org, title, extras)
	       – fills with a 1-row empty data-source                                  */
	    return RowReportBinder.fill(jrxmlFile, orgName, reportTitle, tableSpecs, extraParams);
	}

	/**
	 * Writes the output report to the requested directory
	 * 
	 * @param format 	  - report format
	 * @param print       - printed output
	 * @param baseName    - base name of the report
	 * 
	 * @return output file
	 * @throws JRException
	 * @throws IOException
	 */
	@SuppressWarnings("static-method")
		File
		writeJasperOutput(String format, JasperPrint print, String baseName)	throws JRException,
																				IOException
	{
		File outDir = new File(getOutputDirectory());
		
		if (!outDir.exists())
		{
			outDir.mkdirs();
		}
		
		File outFile =
				new File(	outDir,
							baseName + ("html".equalsIgnoreCase(format) ? ".html" : ".pdf"));
		
		if ("html".equalsIgnoreCase(format))
		{
			return exportToHTML(print, outFile.getAbsolutePath());
		}
		
		return exportToPDF(print, outFile.getAbsolutePath());
		
	}
	
	/**
	 * Gets the base directory where generated reports should be saved.
	 * This implementation defaults to a "NonprofitBookkeepingReports" subdirectory within the user's home directory.
	 * Subclasses can override this method to specify a different output directory.
	 * The directory will be created if it does not already exist.
	 *
	 * @return A string representing the absolute path to the output directory.
	 */
	protected static String getOutputDirectory()
	{
		// Could be configurable via a properties file or system property
		String userHome = System.getProperty("user.home");
		File outputDir = new File(userHome, "NonprofitBookkeepingReports");
		
		if (!outputDir.exists())
		{
			outputDir.mkdirs();
		}
		
		return outputDir.getAbsolutePath();
		
	}
	
	
	/**
	 * Exports a filled {@link JasperPrint} object to a PDF file at the specified path.
	 * This method ensures that the parent directory for the output file exists before exporting.
	 *
	 * @param jasperPrint The {@link JasperPrint} object containing the compiled and filled report.
	 * @param outputFilePath The absolute path (including filename) where the PDF report should be saved.
	 * @return The {@link File} object representing the exported PDF report.
	 * @throws JRException If an error occurs during the PDF export process.
	 */
	protected static File exportToPDF(	JasperPrint jasperPrint,
										String outputFilePath) throws JRException
	{
		File outputFile = new File(outputFilePath);
		// Ensure parent directory exists
		File parentDir = outputFile.getParentFile();
		
		if (parentDir != null && !parentDir.exists())
		{
			parentDir.mkdirs();
		}
		
		JasperExportManager.exportReportToPdfFile(jasperPrint, outputFile.getAbsolutePath());
		System.out.println("Report exported to PDF: " + outputFile.getAbsolutePath()); // Consider
																						// using a
																						// logger
		return outputFile;
		
	}
	
	/**
	 * Exports a filled {@link JasperPrint} object to an HTML file at the specified path.
	 * This method ensures that the parent directory for the output file exists before exporting.
	 *
	 * @param jasperPrint The {@link JasperPrint} object containing the compiled and filled report.
	 * @param outputFilePath The absolute path (including filename) where the HTML report should be saved.
	 * @return The {@link File} object representing the exported HTML report.
	 * @throws JRException If an error occurs during the HTML export process setup.
	 * @throws IOException If an error occurs during file writing.
	 */
	protected static File exportToHTML(	JasperPrint jasperPrint,
										String outputFilePath)	throws JRException,
																IOException
	{
		File outputFile = new File(outputFilePath);
		// Ensure parent directory exists
		File parentDir = outputFile.getParentFile();
		
		if (parentDir != null && !parentDir.exists())
		{
			parentDir.mkdirs();
		}
		
		HtmlExporter exporter = new HtmlExporter();
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleHtmlExporterOutput(new FileOutputStream(outputFile)));
		
		exporter.exportReport();
		System.out.println("Report exported to HTML: " + outputFile.getAbsolutePath());
		return outputFile;
		
	}
	
}
