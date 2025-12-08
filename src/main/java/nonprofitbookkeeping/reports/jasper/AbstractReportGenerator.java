
package nonprofitbookkeeping.reports.jasper;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;


import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.ReportBundles;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
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
	private static final java.util.regex.Pattern SCHEMA_LOCATION_ATTRIBUTE_PATTERN =
		java.util.regex.Pattern.compile("\\s+xsi:schemaLocation=\"[^\"]*\"");
	private static final java.util.Map<String,
		String> LEGACY_STYLE_ATTRIBUTE_ALIASES =
			java.util.Map.ofEntries(
				java.util.Map.entry("isBold", "bold"),
				java.util.Map.entry("isItalic", "italic"),
				java.util.Map.entry("isUnderline", "underline"),
				java.util.Map.entry("isStrikethrough", "strikeThrough"),
				java.util.Map.entry("hAlign", "hTextAlign"));
	/**
	 * Data beans supplied to populate the report. Subclasses may override
	 * {@link #getReportData()} to compute data dynamically, but in cases where
	 * the data is prepared externally it can be injected here via
	 * {@link #setReportData(List)}.
	 */
	private List<?> reportData = Collections.emptyList();
	private boolean reportDataProvided;
	
	/**
	 * Retrieves the collection of data beans that will populate the report.
	 * 
	 * @return A {@link List} of objects (JavaBeans) to be used as the report's
	 *         data source. The exact type depends on the specific report.
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
	protected abstract String getReportPath() throws ActionCancelledException,
		NoFileCreatedException;
	
	/**
	 * Resolves the JRXML location from the co-located bundle metadata for this
	 * generator. Subclasses that simply rely on the bundled template can return
	 * this value from {@link #getReportPath()}.
	 *
	 * @return classpath path to the JRXML template
	 */
	protected final String bundledReportPath()
	{
		return ReportBundles.bundleForGenerator(getClass()).jrxmlResource();
		
	}
	
	
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
	
	public static
		Map<String, Object>
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
		
		JasperReport report;
		
		try
		{
			report = compileReportSanitizingSchemaLocation(jrxmlPath);
		}
		catch (JRException e)
		{
			Throwable t = e;
			
			while (t != null)
			{
				System.err.println("Cause: " + t.getClass().getName() + " - " +
					t.getMessage());
				t = t.getCause();
			}
			
			throw e;
		}
		
		List<?> data = resolveReportData();
		JRBeanCollectionDataSource dataSource =
			new JRBeanCollectionDataSource(data);
		Map<String, Object> params =
			ensureMutableParameters(getReportParameters());
		return JasperFillManager.fillReport(report, params, dataSource);
		
	}
	
	private JasperReport compileReportSanitizingSchemaLocation(String jrxmlPath)
		throws JRException
	{
		byte[] jrxmlBytes;
		
		try
		{
			jrxmlBytes = readJrxmlBytes(jrxmlPath);
		}
		catch (IOException e)
		{
			throw new JRException("Unable to read JRXML template: " + jrxmlPath,
				e);
		}
		
		byte[] sanitized = removeSchemaLocationAttribute(jrxmlBytes);
		byte[] normalized = normalizeBooleanStyleAttributes(sanitized);
		
		try (java.io.ByteArrayInputStream input =
			new java.io.ByteArrayInputStream(normalized))
		{
			return JasperCompileManager.compileReport(input);
		}
		catch (IOException e)
		{
			throw new JRException("Failed to close JRXML stream", e);
		}
		
	}
	
	/**
	 * @param sanitized
	 * @return
	 */
	private static byte[] normalizeBooleanStyleAttributes(byte[] sanitized)
	{
		// TODO Auto-generated method stub
		return sanitized;
		
	}
	
	
	private static byte[] readJrxmlBytes(String jrxmlPath) throws IOException
	{
		java.nio.file.Path path = java.nio.file.Paths.get(jrxmlPath);
		
		if (java.nio.file.Files.exists(path))
		{
			return java.nio.file.Files.readAllBytes(path);
		}
		
		String normalized = jrxmlPath.startsWith("/") ?
			jrxmlPath.substring(1) : jrxmlPath;
		
		try (java.io.InputStream resource =
			AbstractReportGenerator.class.getClassLoader()
				.getResourceAsStream(normalized))
		{
			
			if (resource == null)
			{
				throw new IOException("JRXML template not found: " + jrxmlPath);
			}
			
			return resource.readAllBytes();
		}
		
	}
	
	private static final java.util.Map<String, String> STYLE_ATTRIBUTE_RENAMES =
		java.util.Map.of(
			"isBold",
			"bold",
			"isItalic",
			"italic",
			"isUnderline",
			"underline",
			"isStrikethrough",
			"strikeThrough",
			"hAlign",
			"hTextAlign");
	
	private static byte[] removeSchemaLocationAttribute(byte[] xmlBytes)
	{
		String xml =
			new String(xmlBytes, java.nio.charset.StandardCharsets.UTF_8);
		String sanitized =
			SCHEMA_LOCATION_ATTRIBUTE_PATTERN.matcher(xml).replaceAll("");
		sanitized = replaceLegacyStyleAttributes(sanitized);
		return sanitized.getBytes(java.nio.charset.StandardCharsets.UTF_8);
		
	}
	
	private static String replaceLegacyStyleAttributes(String xml)
	{
		String sanitized = xml;
		
		for (java.util.Map.Entry<String, String> entry : STYLE_ATTRIBUTE_RENAMES
			.entrySet())
		{
			java.util.regex.Pattern pattern =
				java.util.regex.Pattern.compile("(?<=\\s)" +
					java.util.regex.Pattern.quote(entry.getKey()) + "=");
			sanitized = pattern.matcher(sanitized)
				.replaceAll(entry.getValue() + "=");
		}
		
		return sanitized;
		
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
				baseName + ("html".equalsIgnoreCase(format) ? ".html" :
					"xlsx".equalsIgnoreCase(format) ? ".xlsx" : ".pdf"));
		
		if ("html".equalsIgnoreCase(format))
		{
			return exportToHTML(print, outFile.getAbsolutePath());
		}
		
		if ("xlsx".equalsIgnoreCase(format))
		{
			JRXlsxExporter xlsx = new JRXlsxExporter();
			xlsx.setExporterInput(new SimpleExporterInput(print));
			xlsx.setExporterOutput(
				new SimpleOutputStreamExporterOutput(outFile));
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
	
	
	/**
	 * @param beans
	 */
	public void setReportData(List<?> beans)
	{
		
		if (beans == null)
		{
			this.reportData = Collections.emptyList();
			this.reportDataProvided = false;
			return;
		}
		
		this.reportData = Collections.unmodifiableList(new ArrayList<>(beans));
		this.reportDataProvided = true;
		
	}
	
	/**
	 * Resolves the data beans that should populate the report.
	 *
	 * @return unmodifiable list of beans, never {@code null}.
	 */
	protected final List<?> resolveReportData()
	{
		
		if (this.reportDataProvided)
		{
			return this.reportData;
		}
		
		List<?> generated = getReportData();
		
		if (generated == null)
		{
			return Collections.emptyList();
		}
		
		return Collections.unmodifiableList(new ArrayList<>(generated));
		
	}
	
}
