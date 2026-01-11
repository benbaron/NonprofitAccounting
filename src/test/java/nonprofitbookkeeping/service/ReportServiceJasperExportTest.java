
package nonprofitbookkeeping.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Focused tests for {@link ReportService#generateJasperReport(ReportContext, String)}
 * that verify alternate export formats such as XLSX are correctly routed through
 * the generator bridge.
 */
class ReportServiceJasperExportTest
{
	@TempDir File tempDir;
	
	@Test
	void generateJasperReportExportsXlsxViaGenerator() throws Exception
	{
		assumeTrue(isAwtAvailable(),
			"Skipping Jasper export test: AWT native libraries unavailable.");
		StubGenerator.reset(this.tempDir);
		Map<ReportService.ReportType,
			String> registry = Collections.singletonMap(
				ReportService.ReportType.BALANCE_SHEET_JASPER,
				StubGenerator.class.getName());
		ReportService service = new ReportService(registry);
		
		ReportContext context = new ReportContext();
		context
			.setReportType(ReportService.ReportType.BALANCE_SHEET_JASPER.id());
		context.setOutputFormat("xlsx");
		
		File output =
			service.generateJasperReport(context, context.getOutputFormat());
		
		assertNotNull(output,
			"Service should return the generated file instance.");
		assertTrue(output.exists(),
			"Stub generator should create the XLSX file on disk.");
		assertEquals("stub-balance.xlsx", output.getName(),
			"File name should use the generator's base name and format extension.");
		assertEquals(this.tempDir.toPath(), output.getParentFile().toPath(),
			"Output directory should be the injected temporary location.");
		assertEquals("xlsx", StubGenerator.lastFormat(),
			"The generator should receive the XLSX format flag.");
		
	}

	private static boolean isAwtAvailable()
	{
		try
		{
			Toolkit.getDefaultToolkit();
			return true;
		}
		catch (UnsatisfiedLinkError | Exception ex)
		{
			return false;
		}
		
	}
	
	/** Simple stub generator that records the format passed in. */
	public static final class StubGenerator extends AbstractReportGenerator
	{
		private static File outputDirectory;
		private static String lastFormat;
		
		static void reset(File directory)
		{
			outputDirectory = directory;
			lastFormat = null;
			
		}
		
		static String lastFormat()
		{
			return lastFormat;
			
		}
				
		@Override
		protected List<?> getReportData()
		{
			return Collections.emptyList();
			
		}
		
		@Override
		protected Map<String, Object> getReportParameters()
		{
			return Collections.emptyMap();
			
		}
		
		@Override
		protected String getReportPath()
		{
			return "nonprofitbookkeeping/reports/COMMENTS.jrxml";
			
		}

		@Override
		protected File getOutputDirectory()
		{
			return outputDirectory;
			
		}

		@Override
		public File writeJasperOutput(String format, JasperPrint print,
			String baseName) throws JRException, IOException
		{
			lastFormat = format;
			return super.writeJasperOutput(format, print, baseName);
			
		}
		
		@Override
		public String getBaseName()
		{
			return "stub-balance";
			
		}
		
	}
	
}
