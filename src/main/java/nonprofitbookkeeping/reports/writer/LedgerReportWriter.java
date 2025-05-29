
package nonprofitbookkeeping.reports.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import nonprofitbookkeeping.api.ReportWriterIntf;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.TrialBalanceService;

/**
 * 
 */
public class LedgerReportWriter implements ReportWriterIntf
{
	/**  
	 * Constructor LedgerReportWriter
	 * @param trialBalanceService
	 * @param ledger
	 */
	public LedgerReportWriter(TrialBalanceService trialBalanceService, Ledger ledger)
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * Override @see nonprofitbookkeeping.reports.ReportWriter#writeReport(nonprofitbookkeeping.reports.ReportContext)
	 */
	@Override public File writeReport(ReportContext context) throws IOException
	{
		File file = File.createTempFile("ledger_report_", ".txt");
		
		try (FileWriter writer = new FileWriter(file))
		{
			writer.write("Ledger Report\n");
			writer.write("From: " + context.getStartDate() + " To: " + context.getEndDate() + "\n");
			writer.write("Format: " + context.getOutputFormat() + "\n");
			writer.write("...ledger report content here...");
		}
		
		return file;
	}

}
