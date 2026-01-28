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
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Base class for Jasper report generators.
 */
public abstract class AbstractReportGenerator
	implements ReportContextAware
{
	private static final Logger LOGGER =
		LoggerFactory.getLogger(AbstractReportGenerator.class);
	private static final String DEFAULT_OUTPUT_DIR =
		"NonprofitBookkeepingReports";
	/**
	 * Parameter key exposing the full report row list to Jasper templates.
	 */
	private static final String REPORT_DATA_ROWS_PARAM = "REPORT_DATA_ROWS";
	/**
	 * Field name on the single-row data-source envelope that holds the row
	 * list for band-driven templates.
	 */
	private static final String REPORT_DATA_ROWS_FIELD = "rows";
	
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
		List<?> safeData = data == null ? Collections.emptyList() : data;
		params.put(REPORT_DATA_ROWS_PARAM, List.copyOf(safeData));
		JRDataSource dataSource = buildDataSource(safeData);
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
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("Using explicit report data for generator {} with {} rows.",
					getClass().getName(),
					(this.reportDataOverride == null ? 0
						: this.reportDataOverride.size()));
			}
			return normalizeReportData(this.reportDataOverride);
		}
		
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Generating report data via getReportData() for generator {}.",
				getClass().getName());
		}
		List<?> data = ReportContextHolder.withContext(
			this.reportContext,
			this::getReportData
		);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Generated {} report data rows for generator {}.",
				(data == null ? 0 : data.size()),
				getClass().getName());
		}
		return normalizeReportData(data);
		
	}

	/**
	 * Exposes resolved report data for non-Jasper export paths (e.g. XLSX templates).
	 *
	 * @return resolved report data list
	 */
	public List<?> getResolvedReportData()
	{
		return resolveReportData();
	}

	/**
	 * Normalize report data.
	 *
	 * @param data the data
	 * @return the list
	 */
	private List<?> normalizeReportData(List<?> data)
	{
		if (data == null || data.isEmpty())
		{
			return Collections.emptyList();
		}
		if (data.size() == 1)
		{
			return List.copyOf(data);
		}
		try
		{
			return List.of(
				nonprofitbookkeeping.reports.jasper.runtime.ReportDataBundle
					.fromRows(data)
			);
		}
		catch (IntrospectionException ex)
		{
			throw new IllegalStateException(
				"Failed to normalize report data for " +
					getClass().getName(),
				ex
			);
		}
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.runtime.ReportContextAware#setReportContext(nonprofitbookkeeping.reports.jasper.runtime.ReportContext) 
	 */
	@Override
	public void setReportContext(ReportContext context)
	{
		this.reportContext = context;
		
	}

	/**
	 * Gets the report context.
	 *
	 * @return the report context
	 */
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
	 * Load report.
	 *
	 * @param reportPath the report path
	 * @return the jasper report
	 * @throws JRException the JR exception
	 */
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

	/**
	 * Open classpath report.
	 *
	 * @param reportPath the report path
	 * @return the input stream
	 */
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

	/**
	 * Builds the data source.
	 *
	 * @param data the data
	 * @return the JR data source
	 */
	private JRDataSource buildDataSource(List<?> data)
	{
		if (data == null || data.isEmpty())
		{
			return new JRBeanCollectionDataSource(Collections.emptyList(), false);
		}
		if (data.size() == 1)
		{
			return new JRBeanCollectionDataSource(data, false);
		}
		Map<String, Object> envelope = new LinkedHashMap<>();
		Object first = data.get(0);
		if (first instanceof Map<?, ?> map)
		{
			map.forEach((key, value) ->
			{
				if (key != null)
				{
					envelope.put(key.toString(), value);
				}
			});
		}
		else if (first != null)
		{
			envelope.putAll(extractBeanProperties(first));
		}
		envelope.put(REPORT_DATA_ROWS_FIELD, List.copyOf(data));
		return new JRMapCollectionDataSource(List.of(envelope));
	}

	/**
	 * Extract bean properties.
	 *
	 * @param bean the bean
	 * @return the map
	 */
	private Map<String, Object> extractBeanProperties(Object bean)
	{
		Map<String, Object> properties = new LinkedHashMap<>();
		BeanInfo info;
		try
		{
			info = Introspector.getBeanInfo(bean.getClass(), Object.class);
		}
		catch (IntrospectionException e)
		{
			throw new IllegalStateException(
				"Unable to introspect report bean " + bean.getClass().getName(),
				e
			);
		}
		for (PropertyDescriptor descriptor : info.getPropertyDescriptors())
		{
			Method readMethod = descriptor.getReadMethod();
			if (readMethod == null)
			{
				continue;
			}
			try
			{
				properties.put(descriptor.getName(),
					readMethod.invoke(bean));
			}
			catch (ReflectiveOperationException e)
			{
				throw new IllegalStateException(
					"Unable to read property " + descriptor.getName() +
						" from " + bean.getClass().getName(),
					e
				);
			}
		}
		return properties;
	}
	
}
