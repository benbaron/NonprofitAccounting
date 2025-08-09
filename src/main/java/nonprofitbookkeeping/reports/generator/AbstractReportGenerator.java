
package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;


import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Collections;
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
         * Data beans supplied to populate the report. Subclasses may override
         * {@link #getReportData()} to compute data dynamically, but in cases where
         * the data is prepared externally it can be injected here via
         * {@link #setReportData(List)}.
         */
        private List<?> reportData = Collections.emptyList();

        /**
         * Retrieves the collection of data beans that will populate the report.
         * The default implementation returns the list provided through
         * {@link #setReportData(List)}. Subclasses may override to compute the
         * dataset on demand.
         *
         * @return A {@link List} of objects (JavaBeans) to be used as the report's
         *         data source. The exact type depends on the specific report.
         */
        protected List<?> getReportData()
        {
                return this.reportData;
        }

        /**
         * Allows external callers (typically the {@link ReportService}) to supply
         * the data beans that will back the report.
         *
         * @param data list of beans specific to the report type
         */
        public void setReportData(List<?> data)
        {
                this.reportData = (data == null) ? Collections.emptyList() : data;
        }
	
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
	protected abstract String getReportPath() throws ActionCancelledException,
		NoFileCreatedException;
	
	
	/**
	 * Gets the output file base name
	 *  
	 * @return base name
	 */
	public abstract String getBaseName();
	
	
	/**
	 * Wraps an immutable map so it is suitable for jasper (which needs a 
	 * writeable one to operate)
	 * 
	 * @param original : report
	 * @return mutable reports
	 */

        public static Map<String, Object>
                        ensureMutableParameters(Map<String, Object> original)
        {
                return (original instanceof HashMap) ?
                                original : new HashMap<>(original);

        }

        /**
         * Compiles the JRXML template, fills it with data and parameters, and returns
         * a populated {@link JasperPrint} ready for export.
         *
         * @return filled {@link JasperPrint}
         * @throws JRException if compilation or filling fails
         */
        public JasperPrint generatePrint() throws JRException
        {
                String jrxmlPath;
                try
                {
                        jrxmlPath = getReportPath();
                }
                catch (ActionCancelledException | NoFileCreatedException e)
                {
                        throw new JRException("Unable to resolve report path", e);
                }

                JasperReport report = JasperCompileManager.compileReport(jrxmlPath);
                JRBeanCollectionDataSource dataSource =
                                new JRBeanCollectionDataSource(getReportData());
                Map<String, Object> params = ensureMutableParameters(getReportParameters());
                return JasperFillManager.fillReport(report, params, dataSource);
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
	public File writeJasperOutput(String format,
	                              JasperPrint print,
	                              String baseName) throws JRException, IOException
	{
		File outDir = new File(getOutputDirectory());

		if (!outDir.exists())
		{
			outDir.mkdirs();
		}


                File outFile =
                                new File(outDir,
                                        baseName + ("html".equalsIgnoreCase(format) ? ".html"
                                                       : "xlsx".equalsIgnoreCase(format) ? ".xlsx" : ".pdf"));

                if ("html".equalsIgnoreCase(format))
                {
                        return exportToHTML(print, outFile.getAbsolutePath());
                }

                if ("xlsx".equalsIgnoreCase(format))
                {
                        JRXlsxExporter xlsx = new JRXlsxExporter();
                        xlsx.setExporterInput(new SimpleExporterInput(print));
                        xlsx.setExporterOutput(new SimpleOutputStreamExporterOutput(outFile));
                        xlsx.exportReport();
                        return outFile;
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
	protected static File exportToPDF(JasperPrint jasperPrint,
		String outputFilePath) throws JRException
	{
		File outputFile = new File(outputFilePath);
		// Ensure parent directory exists
		File parentDir = outputFile.getParentFile();
		
		if (parentDir != null && !parentDir.exists())
		{
			parentDir.mkdirs();
		}
		
		JasperExportManager.exportReportToPdfFile(jasperPrint,
			outputFile.getAbsolutePath());
		System.out
			.println("Report exported to PDF: " + outputFile.getAbsolutePath()); // Consider
																					// using
																					// a
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
	protected static File exportToHTML(JasperPrint jasperPrint,
		String outputFilePath) throws JRException,
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
		exporter.setExporterOutput(
			new SimpleHtmlExporterOutput(new FileOutputStream(outputFile)));
		
		exporter.exportReport();
		System.out.println(
			"Report exported to HTML: " + outputFile.getAbsolutePath());
		return outputFile;
		
	}
	
}
