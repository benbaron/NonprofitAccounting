
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.writer.LedgerReportWriter;
import nonprofitbookkeeping.ui.panels.ReportMetadata;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ReportService
{
	
	/**
	 * Generates a report based on the provided ReportContext.
	 * 
	 * @param context The context containing report parameters (type, date range, format).
	 * @return The file where the report is saved.
	 * @throws IOException If an error occurs while generating or saving the report.
	 */
	public static File generate(ReportContext context) throws IOException
	{
		// Example logic for report generation, replace with actual logic to generate
		// reports
		System.out.println("Generating report: " + context.reportType);
		// Generate the report and save it to a file
		File outputFile = new File("generated_report_" + context.reportType + ".pdf");
		
		// Simulating report generation by creating an empty file
		if (!outputFile.exists())
		{
			outputFile.createNewFile();
		}
		
		return outputFile; // Return the file where the report is saved
	}

	/**
	 * @param string
	 * @param ledgerReportWriter
	 */
	public void registerWriter(String string, LedgerReportWriter ledgerReportWriter)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return
	 */
	public List<ReportMetadata> listGeneratedReports()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
