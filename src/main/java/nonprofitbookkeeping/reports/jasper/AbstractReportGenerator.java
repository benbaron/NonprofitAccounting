package nonprofitbookkeeping.reports.jasper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

/**
 * Base class for Jasper report generators.
 */
public abstract class AbstractReportGenerator
{
	private List<?> reportData;
	
	/**
	 * Generate the Jasper report output.
	 *
	 * @return JasperPrint instance
	 * @throws JRException if Jasper fails to compile or fill the report
	 * @throws IOException if the report template cannot be read
	 */
	public JasperPrint generatePrint() throws JRException, IOException
	{
		String reportPath;
		
		try
		{
			reportPath = getReportPath();
		}
		catch (ActionCancelledException | NoFileCreatedException ex)
		{
			throw new JRException("Unable to resolve report path", ex);
		}
		
		if (reportPath == null || reportPath.isBlank())
		{
			throw new JRException("Report path is required");
		}
		
		JasperReport report;
		
		try (InputStream stream = loadReportStream(reportPath))
		{
			
			if (stream != null)
			{
				report = JasperCompileManager.compileReport(stream);
			}
			else
			{
				report = JasperCompileManager.compileReport(reportPath);
			}
			
		}
		
		Map<String, Object> parameters = getReportParameters();
		
		if (parameters == null)
		{
			parameters = Collections.emptyMap();
		}
		
		List<?> data = resolveReportData();
		JRBeanCollectionDataSource dataSource =
			new JRBeanCollectionDataSource(data);
		
		return JasperFillManager.fillReport(report, parameters, dataSource);
		
	}
	
	/**
	 * Writes the JasperPrint output to disk using the specified format.
	 *
	 * @param format output format, e.g. pdf or xlsx
	 * @param print JasperPrint instance
	 * @param baseName base file name without extension
	 * @return created file
	 * @throws JRException if export fails
	 * @throws IOException if file output fails
	 */
	public File writeJasperOutput(String format, JasperPrint print,
		String baseName) throws JRException, IOException
	{
		String normalized =
			format == null ? "pdf" : format.trim().toLowerCase();
		
		File outputDirectory = new File(System.getProperty("user.home"),
			"NonprofitBookkeepingReports");
		
		if (!outputDirectory.exists() && !outputDirectory.mkdirs())
		{
			throw new IOException(
				"Unable to create report output directory: " +
					outputDirectory);
		}
		
		String safeBaseName =
			baseName == null || baseName.isBlank() ? "report" : baseName;
		File output =
			new File(outputDirectory, safeBaseName + "." + normalized);
		
		switch (normalized)
		{
			case "html" ->
				JasperExportManager.exportReportToHtmlFile(print,
					output.getAbsolutePath());
			case "xlsx" ->
				exportXlsx(print, output);
			case "pdf" ->
				JasperExportManager.exportReportToPdfFile(print,
					output.getAbsolutePath());
			default ->
				throw new JRException("Unsupported Jasper output format: " +
					normalized);
		}
		
		return output;
		
	}
	
	public void setReportData(List<?> reportData)
	{
		
		if (reportData == null)
		{
			this.reportData = null;
			return;
		}
		
		this.reportData =
			Collections.unmodifiableList(new ArrayList<>(reportData));
		
	}
	
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
	
	protected String bundledReportPath()
	{
		return ReportBundles.bundleForGenerator(getClass()).jrxmlResource();
		
	}
	
	protected abstract List<?> getReportData();
	
	protected abstract Map<String, Object> getReportParameters();
	
	protected abstract String getReportPath()
		throws ActionCancelledException, NoFileCreatedException;
	
	public abstract String getBaseName();
	
	private InputStream loadReportStream(String reportPath)
	{
		ClassLoader loader = getClass().getClassLoader();
		return loader.getResourceAsStream(reportPath);
		
	}
	
	private void exportXlsx(JasperPrint print, File output) throws JRException
	{
		JRXlsxExporter exporter = new JRXlsxExporter();
		exporter.setExporterInput(new SimpleExporterInput(print));
		exporter.setExporterOutput(
			new SimpleOutputStreamExporterOutput(output));
		
		SimpleXlsxReportConfiguration configuration =
			new SimpleXlsxReportConfiguration();
		configuration.setDetectCellType(true);
		configuration.setCollapseRowSpan(false);
		exporter.setConfiguration(configuration);
		
		exporter.exportReport();
		
	}
	
}
