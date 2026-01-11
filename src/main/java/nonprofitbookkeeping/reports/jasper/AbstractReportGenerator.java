package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContextAware;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContextHolder;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Map;

/**
 * Base class for Jasper report generators.
 */
public abstract class AbstractReportGenerator
	implements ReportContextAware
{
	private static final Logger LOGGER =
		Logger.getLogger(AbstractReportGenerator.class.getName());
	private static final String DEFAULT_OUTPUT_DIR =
		"NonprofitBookkeepingReports";
	
	private List<?> reportDataOverride;
	private boolean reportDataExplicit;
	private ReportContext reportContext;
	
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
	 * Builds the {@link JasperPrint} using the JRXML template, parameters, and
	 * bean data supplied by the generator.
	 *
	 * @return rendered JasperPrint
	 * @throws JRException if Jasper fails to compile or fill the template
	 */
	public JasperPrint generatePrint() throws JRException
	{
		String reportPath;
		try
		{
			reportPath = getReportPath();
		}
		catch (ActionCancelledException | NoFileCreatedException e)
		{
			throw new JRException("Unable to resolve report path", e);
		}

		if (reportPath == null || reportPath.isBlank())
		{
			throw new JRException("Report path was not provided.");
		}

		JasperReport report = loadReport(reportPath);
		Map<String, Object> params =
			getReportParameters() == null ? new HashMap<>() :
				new HashMap<>(getReportParameters());
		List<?> data = resolveReportData();
		JRBeanCollectionDataSource dataSource =
			new JRBeanCollectionDataSource(data, false);
		return JasperFillManager.fillReport(report, params, dataSource);
	}

	/**
	 * Export the provided {@link JasperPrint} to disk.
	 *
	 * @param format output format ("pdf", "html", or "xlsx")
	 * @param print rendered JasperPrint
	 * @param baseName base output filename
	 * @return the generated file
	 * @throws JRException when Jasper export fails
	 * @throws IOException when file operations fail
	 */
	public File writeJasperOutput(String format, JasperPrint print,
		String baseName) throws JRException, IOException
	{
		String normalized =
			(format == null || format.isBlank()) ? "pdf" :
				format.trim().toLowerCase();
		String resolvedBase =
			(baseName == null || baseName.isBlank()) ? getBaseName() : baseName;

		File outputDirectory = getOutputDirectory();
		if (outputDirectory == null)
		{
			throw new IOException("Output directory was not provided.");
		}
		if (!outputDirectory.exists() && !outputDirectory.mkdirs())
		{
			throw new IOException("Unable to create output directory: " +
				outputDirectory.getAbsolutePath());
		}

		File outFile = new File(outputDirectory,
			resolvedBase + "." + normalized);

		switch (normalized)
		{
			case "pdf" ->
				JasperExportManager.exportReportToPdfFile(print,
					outFile.getAbsolutePath());
			case "html" ->
				JasperExportManager.exportReportToHtmlFile(print,
					outFile.getAbsolutePath());
			case "xlsx" ->
				exportXlsx(print, outFile);
			default ->
				throw new JRException("Unsupported output format: " +
					normalized);
		}

		return outFile;
	}

	private void exportXlsx(JasperPrint print, File outFile)
		throws JRException
	{
		JRXlsxExporter exporter = new JRXlsxExporter();
		exporter.setExporterInput(new SimpleExporterInput(print));
		exporter.setExporterOutput(
			new SimpleOutputStreamExporterOutput(outFile));
		SimpleXlsxReportConfiguration configuration =
			new SimpleXlsxReportConfiguration();
		configuration.setDetectCellType(true);
		exporter.setConfiguration(configuration);
		exporter.exportReport();
	}

	/**
	 * Resolve the output directory for generated reports.
	 *
	 * @return directory to write report exports
	 */
	protected File getOutputDirectory()
	{
		return new File(System.getProperty("user.home"), DEFAULT_OUTPUT_DIR);
	}
	
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
			if (LOGGER.isLoggable(Level.FINE))
			{
				LOGGER.fine("Using explicit report data for generator " +
					getClass().getName() + " with " +
					(this.reportDataOverride == null ?
						0 : this.reportDataOverride.size()) +
					" rows.");
			}
			return this.reportDataOverride == null ?
				Collections.emptyList() : this.reportDataOverride;
		}
		
		if (LOGGER.isLoggable(Level.FINE))
		{
			LOGGER.fine("Generating report data via getReportData() for " +
				"generator " + getClass().getName() + ".");
		}
		List<?> data = ReportContextHolder.withContext(
			this.reportContext,
			this::getReportData
		);
		if (LOGGER.isLoggable(Level.FINE))
		{
			LOGGER.fine("Generated " +
				(data == null ? 0 : data.size()) +
				" report data rows for generator " + getClass().getName() +
				".");
		}
		List<?> resolvedData =
			data == null ? Collections.emptyList() : List.copyOf(data);
		return normalizeReportData(resolvedData);
		
	}

	@Override
	public void setReportContext(ReportContext context)
	{
		this.reportContext = context;
		
	}

	protected ReportContext getReportContext()
	{
		return this.reportContext;
		
	}

	/**
	 * Indicates whether this report should be driven by a single bean that
	 * represents the entire report output.
	 *
	 * @return {@code true} when the report should render once per bean
	 */
	protected boolean isSingleBeanReport()
	{
		return true;
	}

	private List<?> normalizeReportData(List<?> data)
	{
		if (!isSingleBeanReport() || data == null || data.size() <= 1)
		{
			return data == null ? Collections.emptyList() : data;
		}

		LOGGER.warning("Multiple data rows were provided for generator " +
			getClass().getName() + "; rendering only the first row. " +
			"Consider consolidating data into a single bean and using report " +
			"bands for detail sections.");
		return Collections.singletonList(data.get(0));
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

	private JasperReport loadReport(String reportPath) throws JRException
	{
		String normalized = reportPath.startsWith("/") ?
			reportPath.substring(1) : reportPath;
		InputStream classpathStream = openClasspathReport(normalized);
		if (classpathStream != null)
		{
			try (InputStream stream = classpathStream)
			{
				if (normalized.endsWith(".jasper"))
				{
					return (JasperReport) JRLoader.loadObject(stream);
				}
				return JasperCompileManager.compileReport(stream);
			}
			catch (IOException e)
			{
				throw new JRException("Failed to read report template", e);
			}
		}

		File file = new File(reportPath);
		if (file.exists())
		{
			try (InputStream stream = new FileInputStream(file))
			{
				if (reportPath.endsWith(".jasper"))
				{
					return (JasperReport) JRLoader.loadObject(stream);
				}
				return JasperCompileManager.compileReport(stream);
			}
			catch (IOException e)
			{
				throw new JRException("Failed to read report template file", e);
			}
		}

		throw new JRException("Report template not found: " + reportPath);
	}

	private InputStream openClasspathReport(String reportPath)
	{
		ClassLoader loader =
			Thread.currentThread().getContextClassLoader();
		InputStream stream =
			loader == null ? null : loader.getResourceAsStream(reportPath);
		if (stream != null)
		{
			return stream;
		}
		return getClass().getClassLoader().getResourceAsStream(reportPath);
	}
	
}
