package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Base class for Jasper report generators.
 */
public abstract class AbstractReportGenerator
{
	private static final Logger LOGGER =
		Logger.getLogger(AbstractReportGenerator.class.getName());
	private static final String DEFAULT_OUTPUT_DIR =
		"NonprofitBookkeepingReports";
	
	private List<?> reportDataOverride;
	private boolean reportDataExplicit;
	
	/**
	 * Subclasses provide the data beans needed for the report.
	 */
	protected abstract List<?> getReportData();
	
	/**
	 * Subclasses provide any report parameters for the template.
	 */
	protected abstract Map<String, Object> getReportParameters();
	
	/**
	 * Subclasses provide the JRXML path for the report.
	 */
	protected abstract String getReportPath()
		throws ActionCancelledException, NoFileCreatedException;
	
	/**
	 * Base name used when writing output files.
	 */
	public abstract String getBaseName();
	
	/**
	 * Allows callers to provide report data directly rather than generating
	 * it through {@link #getReportData()}.
	 */
	public void setReportData(List<?> reportData)
	{
		
		if (reportData == null)
		{
			this.reportDataOverride = null;
			this.reportDataExplicit = false;
			return;
		}
		
		this.reportDataOverride = List.copyOf(reportData);
		this.reportDataExplicit = true;
		
	}
	
	/**
	 * Resolve the effective report data, preferring explicitly supplied beans.
	 */
	protected List<?> resolveReportData()
	{
		
		if (this.reportDataExplicit)
		{
			return this.reportDataOverride == null ?
				Collections.emptyList() : this.reportDataOverride;
		}
		
		List<?> data = getReportData();
		return data == null ? Collections.emptyList() : List.copyOf(data);
		
	}
	
	/**
	 * Convenience helper for templates packaged alongside metadata bundles.
	 */
	protected String bundledReportPath()
	{
		
		try
		{
			ReportBundles.Bundle bundle =
				ReportBundles.bundleForGenerator(getClass());
			return bundle.jrxmlResource();
		}
		catch (RuntimeException e)
		{
			return "nonprofitbookkeeping/reports/" + getBaseName() + ".jrxml";
		}
		
	}
	
	/**
	 * Compile and fill the Jasper template for this generator.
	 */
	public JasperPrint generatePrint()
		throws JRException, IOException, ActionCancelledException,
		NoFileCreatedException
	{
		String reportPath = getReportPath();
		if (LOGGER.isLoggable(Level.FINE))
		{
			LOGGER.fine("Compiling Jasper report from path " + reportPath +
				" for generator " + getClass().getName());
		}
		
		try (InputStream input = openReportStream(reportPath))
		{
			JasperReport report = JasperCompileManager.compileReport(input);
			Map<String, Object> parameters = getReportParameters();
			
			if (parameters == null)
			{
				parameters = new HashMap<>();
			}
			
			if (LOGGER.isLoggable(Level.FINE))
			{
				LOGGER.fine("Resolved " + parameters.size() +
					" report parameters for generator " + getClass().getName() +
					": " + parameters.keySet());
			}

			List<?> data = resolveReportData();
			JRDataSource dataSource;

			if (data == null || data.isEmpty())
			{
				LOGGER.warning(() ->
					"Report generator " + getClass().getName() +
						" returned no data rows; using empty datasource.");
				dataSource = new JREmptyDataSource(1);
			}
			else
			{
				LOGGER.fine(() -> "Resolved " + data.size() +
					" data rows for generator " +
					getClass().getName() + ".");
				dataSource = new JRBeanCollectionDataSource(data);
			}
			
			if (LOGGER.isLoggable(Level.FINE))
			{
				LOGGER.fine("Filling Jasper report for generator " +
					getClass().getName() + " with " +
					(data == null ? 0 : data.size()) + " data rows");
			}
			return JasperFillManager.fillReport(report, parameters,
				dataSource);
		}
		
	}
	
	/**
	 * Writes the filled Jasper report to disk using the requested format.
	 */
	public File writeJasperOutput(String format, JasperPrint print,
		String baseName) throws JRException, IOException
	{
		String normalized =
			(format == null ? "pdf" : format).trim().toLowerCase();
		File outputDirectory =
			new File(System.getProperty("user.home"), DEFAULT_OUTPUT_DIR);
		
		if (!outputDirectory.exists() && !outputDirectory.mkdirs())
		{
			throw new IOException(
				"Unable to create output directory: " + outputDirectory);
		}
		
		File outputFile = new File(outputDirectory,
			baseName + "." + normalized);
		
		switch (normalized)
		{
			case "pdf":
				JasperExportManager.exportReportToPdfFile(print,
					outputFile.getAbsolutePath());
				break;
			case "html":
				JasperExportManager.exportReportToHtmlFile(print,
					outputFile.getAbsolutePath());
				break;
			case "xml":
				JasperExportManager.exportReportToXmlFile(print,
					outputFile.getAbsolutePath(), true);
				break;
			case "xlsx":
				JRXlsxExporter exporter = new JRXlsxExporter();
				exporter.setExporterInput(new SimpleExporterInput(print));
				exporter.setExporterOutput(
					new SimpleOutputStreamExporterOutput(outputFile));
				SimpleXlsxReportConfiguration config =
					new SimpleXlsxReportConfiguration();
				config.setDetectCellType(true);
				exporter.setConfiguration(config);
				exporter.exportReport();
				break;
			default:
				JasperExportManager.exportReportToPdfFile(print,
					outputFile.getAbsolutePath());
		}
		
		return outputFile;
		
	}
	
	private InputStream openReportStream(String reportPath)
		throws IOException
	{
		
		if (reportPath == null || reportPath.isBlank())
		{
			throw new FileNotFoundException(
				"Report path is not defined for generator " + getClass());
		}
		
		String resourcePath = reportPath.startsWith("/") ?
			reportPath.substring(1) : reportPath;
		InputStream input =
			getClass().getClassLoader().getResourceAsStream(resourcePath);
		
		if (input != null)
		{
			return input;
		}
		
		return new FileInputStream(reportPath);
		
	}
}
