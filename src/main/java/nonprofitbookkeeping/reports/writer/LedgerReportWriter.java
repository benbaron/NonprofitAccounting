
package nonprofitbookkeeping.reports.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import nonprofitbookkeeping.api.ReportWriterIntf;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.TrialBalanceService;

/**
 * Responsible for generating and writing ledger reports.
 * This class utilizes a {@link TrialBalanceService} (potentially for summary data)
 * and a {@link Ledger} to compile and format the ledger report.
 */
public class LedgerReportWriter implements ReportWriterIntf
{
	private final TrialBalanceService trialBalanceService;
	private final Ledger ledger;

	/**  
	 * Constructs a new LedgerReportWriter.
	 *
	 * @param trialBalanceService The service that might be used for related financial computations
	 *                            or summary data for the ledger report. Must not be null.
	 * @param ledger The ledger containing the financial data to be reported. Must not be null.
	 * @throws IllegalArgumentException if either {@code trialBalanceService} or {@code ledger} is null.
	 */
	public LedgerReportWriter(TrialBalanceService trialBalanceService, Ledger ledger)
	{
		if (trialBalanceService == null) {
            throw new IllegalArgumentException("TrialBalanceService cannot be null.");
        }
        if (ledger == null) {
            throw new IllegalArgumentException("Ledger cannot be null.");
        }
		this.trialBalanceService = trialBalanceService;
		this.ledger = ledger;
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
