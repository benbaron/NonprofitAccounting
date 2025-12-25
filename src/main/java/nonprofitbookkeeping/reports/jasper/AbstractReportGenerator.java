package nonprofitbookkeeping.reports.jasper;

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
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for Jasper report generators, providing common wiring for
 * template compilation and data source handling.
 */
public abstract class AbstractReportGenerator
{
	private static final String DEFAULT_FORMAT = "pdf";
	private List<?> reportData;
	
	/**
	 * Returns data beans for the report.
	 *
	 * @return list of beans or {@code null}
	 */
	protected abstract List<?> getReportData();
	
	/**
	 * Returns the parameter map for the report.
	 *
	 * @return parameter map or {@code null}
	 */
	protected abstract Map<String, Object> getReportParameters();
	
	/**
	 * Returns a path or classpath resource to the JRXML template.
	 *
	 * @return JRXML path or resource
	 * @throws ActionCancelledException when the report generation is cancelled
	 * @throws NoFileCreatedException when the JRXML template cannot be resolved
	 */
	protected abstract String getReportPath()
		throws ActionCancelledException, NoFileCreatedException;
	
	/**
	 * Returns the base filename (without extension) for the generated report.
	 *
	 * @return base name for output files
	 */
	public abstract String getBaseName();
	
	/**
	 * Generates the JasperPrint by compiling the JRXML template and filling it
	 * with the resolved data.
	 *
	 * @return generated {@link JasperPrint}
	 * @throws JRException when Jasper fails to compile or fill the report
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
			throw new JRException("Unable to resolve report template path", e);
		}
		
		if (reportPath == null || reportPath.isBlank())
		{
			throw new JRException("Report template path is missing");
		}
		
		JasperReport report = compileReport(reportPath);
		
		Map<String, Object> params = getReportParameters();
		
		if (params == null)
		{
			params = new HashMap<>();
		}
		
		List<?> data = resolveReportData();
		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);
		
		return JasperFillManager.fillReport(report, params, dataSource);
		
	}
	
	/**
	 * Writes the JasperPrint to disk using the requested format.
	 *
	 * @param format output format (pdf, html, xlsx)
	 * @param print generated JasperPrint
	 * @param baseName base filename without extension
	 * @return the written file
	 * @throws JRException when export fails
	 * @throws IOException when output directory cannot be created
	 */
	public File writeJasperOutput(String format, JasperPrint print,
		String baseName) throws JRException, IOException
	{
		String normalized =
			(format == null || format.isBlank()) ? DEFAULT_FORMAT :
				format.trim().toLowerCase();
		String safeBaseName =
			(baseName == null || baseName.isBlank()) ? "report" : baseName;
		
		File directory = new File(System.getProperty("user.home"),
			"NonprofitBookkeepingReports");
		Files.createDirectories(directory.toPath());
		
		File outputFile = new File(directory,
			safeBaseName + "." + normalized);
		
		switch (normalized)
		{
			case "html" -> JasperExportManager.exportReportToHtmlFile(print,
				outputFile.getAbsolutePath());
			case "xlsx" -> exportXlsx(print, outputFile.toPath());
			default -> JasperExportManager.exportReportToPdfFile(print,
				outputFile.getAbsolutePath());
		}
		
		return outputFile;
		
	}
	
	/**
	 * Allows external callers to provide report data beans directly.
	 *
	 * @param reportData list of beans or {@code null} to reset
	 */
	public void setReportData(List<?> reportData)
	{
		this.reportData = (reportData == null) ? null : List.copyOf(reportData);
		
	}
	
	/**
	 * Resolves the report data, preferring injected beans when present.
	 *
	 * @return unmodifiable list of beans
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
		
		return List.copyOf(data);
		
	}
	
	/**
	 * Returns the JRXML path from bundle metadata.
	 *
	 * @return classpath resource to bundled template
	 */
	protected String bundledReportPath()
	{
		return ReportBundles.bundleForGenerator(getClass()).jrxmlResource();
		
	}
	
	private static JasperReport compileReport(String reportPath)
		throws JRException
	{
		String resourcePath = reportPath.startsWith("/") ?
			reportPath.substring(1) : reportPath;
		ClassLoader loader = AbstractReportGenerator.class.getClassLoader();
		
		try (InputStream stream = loader.getResourceAsStream(resourcePath))
		{
			
			if (stream != null)
			{
				return JasperCompileManager.compileReport(stream);
			}
			
		}
		catch (IOException e)
		{
			throw new JRException("Unable to load report template", e);
		}
		
		return JasperCompileManager.compileReport(reportPath);
		
	}
	
	private static void exportXlsx(JasperPrint print, Path output)
		throws JRException
	{
		JRXlsxExporter exporter = new JRXlsxExporter();
		exporter.setExporterInput(new SimpleExporterInput(print));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
			output.toFile()));
		exporter.exportReport();
		
	}
}
