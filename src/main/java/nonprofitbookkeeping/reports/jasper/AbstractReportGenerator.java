package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

/**
 * Base class for Jasper report generators.
 */
public abstract class AbstractReportGenerator
{
    private static final String OUTPUT_DIRECTORY = "NonprofitBookkeepingReports";

    private List<?> reportData;

    protected abstract List<?> getReportData();

    protected abstract Map<String, Object> getReportParameters();

    protected abstract String getReportPath()
        throws ActionCancelledException, NoFileCreatedException;

    public abstract String getBaseName();

    /**
     * Supplies report data directly for Jasper rendering.
     *
     * @param beans the data beans to use, or {@code null} to reset
     */
    public void setReportData(List<?> beans)
    {
        if (beans == null)
        {
            this.reportData = null;
            return;
        }

        this.reportData = Collections.unmodifiableList(new ArrayList<>(beans));
    }

    /**
     * Resolves the report data, preferring explicitly supplied beans.
     *
     * @return an unmodifiable list of report data
     */
    protected List<?> resolveReportData()
    {
        if (this.reportData != null)
        {
            return this.reportData;
        }

        List<?> data = getReportData();
        if (data == null)
        {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(new ArrayList<>(data));
    }

    /**
     * Resolves the JRXML template path from the report bundle registry.
     *
     * @return classpath resource path for the JRXML template
     */
    protected String bundledReportPath()
    {
        String className = getClass().getName();
        ReportBundles.Bundle bundle = null;

        try
        {
            bundle = ReportBundles.bundleForGenerator(className);
        }
        catch (IllegalArgumentException ex)
        {
            String altName = className.replace(".generator.", ".");

            if (!altName.equals(className))
            {
                try
                {
                    bundle = ReportBundles.bundleForGenerator(altName);
                }
                catch (IllegalArgumentException ignored)
                {
                    bundle = null;
                }
            }
        }

        if (bundle == null)
        {
            throw new IllegalStateException(
                "No bundled JRXML template registered for " + className);
        }

        return bundle.jrxmlResource();
    }

    /**
     * Generates a filled JasperPrint using the provided report data.
     *
     * @return filled JasperPrint
     * @throws JRException if Jasper compilation or fill fails
     */
    public JasperPrint generatePrint() throws JRException
    {
        String reportPath;

        try
        {
            reportPath = getReportPath();
        }
        catch (ActionCancelledException | NoFileCreatedException ex)
        {
            throw new JRException("Failed to resolve report path", ex);
        }

        JasperReport report = loadReport(reportPath);
        Map<String, Object> parameters = getReportParameters();

        if (parameters == null)
        {
            parameters = Collections.emptyMap();
        }

        List<?> data = resolveReportData();
        if (data == null || data.isEmpty())
        {
            return JasperFillManager.fillReport(report, parameters,
                new JREmptyDataSource());
        }

        return JasperFillManager.fillReport(report, parameters,
            new JRBeanCollectionDataSource(data));
    }

    /**
     * Writes the JasperPrint to disk in the requested format.
     *
     * @param format output format (pdf, html, xlsx)
     * @param print filled report
     * @param baseName base filename without extension
     * @return generated file
     * @throws JRException if export fails
     * @throws IOException if file IO fails
     */
    public File writeJasperOutput(String format, JasperPrint print,
        String baseName) throws JRException, IOException
    {
        String normalized = format == null ? "pdf" : format.trim()
            .toLowerCase();
        if (normalized.isEmpty())
        {
            normalized = "pdf";
        }

        Path outputDir = outputDirectory();
        Files.createDirectories(outputDir);
        Path output = outputDir.resolve(baseName + "." + normalized);

        if ("html".equals(normalized))
        {
            JasperExportManager.exportReportToHtmlFile(print,
                output.toString());
        }
        else if ("xlsx".equals(normalized))
        {
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(
                new SimpleOutputStreamExporterOutput(output.toFile()));
            SimpleXlsxReportConfiguration config =
                new SimpleXlsxReportConfiguration();
            config.setDetectCellType(true);
            exporter.setConfiguration(config);
            exporter.exportReport();
        }
        else
        {
            JasperExportManager.exportReportToPdfFile(print, output.toString());
        }

        return output.toFile();
    }

    private static Path outputDirectory()
    {
        return Path.of(System.getProperty("user.home"), OUTPUT_DIRECTORY);
    }

    private static JasperReport loadReport(String reportPath) throws JRException
    {
        if (reportPath == null || reportPath.isBlank())
        {
            throw new JRException("Report path is required");
        }

        try (InputStream in = resolveReportStream(reportPath))
        {
            if (in == null)
            {
                throw new JRException("Unable to locate report template: "
                    + reportPath);
            }

            if (reportPath.endsWith(".jasper"))
            {
                Object loaded = JRLoader.loadObject(in);
                if (loaded instanceof JasperReport jasperReport)
                {
                    return jasperReport;
                }
                throw new JRException("Invalid Jasper template: " + reportPath);
            }

            return JasperCompileManager.compileReport(in);
        }
        catch (IOException ex)
        {
            throw new JRException("Unable to read report template: "
                + reportPath, ex);
        }
    }

    private static InputStream resolveReportStream(String reportPath)
        throws IOException
    {
        Path path = Path.of(reportPath);
        if (Files.exists(path))
        {
            return Files.newInputStream(path);
        }

        ClassLoader loader = AbstractReportGenerator.class.getClassLoader();
        return loader.getResourceAsStream(reportPath);
    }
}
