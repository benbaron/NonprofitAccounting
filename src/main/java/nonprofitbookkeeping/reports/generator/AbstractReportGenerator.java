package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource; // Though not used directly in this abstract class after change
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.*;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbstractReportGenerator {

    // Abstract methods to be implemented by subclasses
    protected abstract List<?> getReportData();
    protected abstract Map<String, Object> getReportParameters();
    protected abstract String getReportPath() throws ActionCancelledException, NoFileCreatedException;

    /**
     * Subclasses must implement this method to define the complete report generation
     * workflow, including loading the JRXML (likely from classpath), compiling it,
     * filling it with data and parameters, exporting it to the specified format,
     * and returning the generated File object.
     *
     * @param format The desired output format (e.g., "pdf", "html").
     * @return The generated File object.
     * @throws Exception If any error occurs during report generation.
     */
    public abstract File generateAndExportReport(String format) throws Exception;

    /**
     * Gets the base directory where reports should be saved.
     * Subclasses can override this. Default is current working directory.
     * @return String path to the output directory.
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


    // Helper export methods, modified to return File and be non-static protected
    protected File exportToPDF(JasperPrint jasperPrint, String outputFilePath) throws JRException {
        File outputFile = new File(outputFilePath);
        // Ensure parent directory exists
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputFile.getAbsolutePath());
        System.out.println("Report exported to PDF: " + outputFile.getAbsolutePath());
        return outputFile;
    }

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
        System.out.println("Report exported to HTML: " + outputFile.getAbsolutePath());
        return outputFile;
    }
}
