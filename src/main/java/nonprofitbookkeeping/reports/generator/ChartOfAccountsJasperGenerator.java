
package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

/**
 * Generator for the Chart of Accounts report.
 */
public class ChartOfAccountsJasperGenerator extends AbstractReportGenerator
{
	
	@Override protected List<?> getReportData()
	{
		return Collections.emptyList(); // FIXME
	}
	
	@Override protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		params.put("P_REPORT_TITLE", "Chart of Accounts");
		params.put("P_GENERATION_DATE", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
		return params;
	}
	
	@Override protected String getReportPath()
	{
		return "jrxml/ChartOfAccountsAlt.jrxml";
	}
	
	@Override public File generateAndExportReport(String format) throws Exception
	{
		String baseName = "Chart_of_Accounts_" + LocalDate.now();
		
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(getReportPath()))
		{
			
			if (in == null)
			{
				throw new FileNotFoundException("JRXML not found: " + getReportPath());
			}
			
			JasperReport jasperReport = JasperCompileManager.compileReport(in);
			JRDataSource dataSource = new JRBeanCollectionDataSource(getReportData());
			JasperPrint print =
				JasperFillManager.fillReport(jasperReport, getReportParameters(), dataSource);
			File outDir = new File(getOutputDirectory());
			
			if (!outDir.exists())
			{
				outDir.mkdirs();
			}
			
			File outFile =
				new File(outDir, baseName + ("html".equalsIgnoreCase(format) ? ".html" : ".pdf"));
			
			if ("html".equalsIgnoreCase(format))
			{
				return exportToHTML(print, outFile.getAbsolutePath());
			}
			
			return exportToPDF(print, outFile.getAbsolutePath());
		}
		
	}
	
}
