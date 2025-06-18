package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.*;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for report generators using JasperReports.
 * This class provides a template and common functionalities for generating various types of reports.
 * Subclasses must implement methods to provide report-specific data, parameters, and the JRXML template path.
 * It includes helper methods for exporting reports to PDF and HTML formats.
 */
public abstract class AbstractReportGenerator {

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
    protected abstract String getReportPath() throws ActionCancelledException, NoFileCreatedException;

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
    public abstract File generateAndExportReport(String format) throws Exception;

    /**
     * Gets the base directory where generated reports should be saved.
     * This implementation defaults to a "NonprofitBookkeepingReports" subdirectory within the user's home directory.
     * Subclasses can override this method to specify a different output directory.
     * The directory will be created if it does not already exist.
     *
     * @return A string representing the absolute path to the output directory.
     */
    protected String getOutputDirectory() {
        // Could be configurable via a properties file or system property
        String userHome = System.getProperty("user.home");
        File outputDir = new File(userHome, "NonprofitBookkeepingReports");
        if (!outputDir.exists()) {
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
    protected File exportToPDF(JasperPrint jasperPrint, String outputFilePath) throws JRException {
        File outputFile = new File(outputFilePath);
        // Ensure parent directory exists
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputFile.getAbsolutePath());
        System.out.println("Report exported to PDF: " + outputFile.getAbsolutePath()); // Consider using a logger
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
    protected File exportToHTML(JasperPrint jasperPrint, String outputFilePath) throws JRException, IOException {
        File outputFile = new File(outputFilePath);
        // Ensure parent directory exists
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        HtmlExporter exporter = new HtmlExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleHtmlExporterOutput(new FileOutputStream(outputFile)));
        // exporter.setConfiguration(new SimpleHtmlExporterConfiguration()); // Optional config
        exporter.exportReport();
        System.out.println("Report exported to HTML: " + outputFile.getAbsolutePath()); // Consider using a logger
        return outputFile;
    }
}
