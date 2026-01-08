package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Map;

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
		List<?> data = getReportData();
		if (LOGGER.isLoggable(Level.FINE))
		{
			LOGGER.fine("Generated " +
				(data == null ? 0 : data.size()) +
				" report data rows for generator " + getClass().getName() +
				".");
		}
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
	
}
