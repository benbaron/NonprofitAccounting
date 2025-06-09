
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
 * This implementation currently writes a basic text file with placeholder content.
 */
public class LedgerReportWriter implements ReportWriterIntf
{
	private final TrialBalanceService trialBalanceService;
	private final Ledger ledger;

	/**
	 * Constructs a new {@code LedgerReportWriter}.
	 *
	 * @param trialBalanceService The {@link TrialBalanceService} that might be used for related financial computations
	 *                            or summary data relevant to the ledger report. Must not be null.
	 * @param ledger The {@link Ledger} containing the financial transaction data to be reported. Must not be null.
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
	 * {@inheritDoc}
	 * <p>
	 * This implementation generates a ledger report as a temporary text file.
	 * The report includes a title, the date range from the {@link ReportContext},
	 * the specified output format, and placeholder text for the actual ledger content.
	 * </p>
	 * @param context The {@link ReportContext} providing details such as date range and output format for the report.
	 * @return A temporary {@link File} containing the generated text-based ledger report.
	 * @throws IOException If an error occurs during file creation or writing.
	 */
	@Override public File writeReport(ReportContext context) throws IOException
	{
		File file = File.createTempFile("ledger_report_", ".txt");

		try (FileWriter writer = new FileWriter(file))
		{
			writer.write("Ledger Report\n");
			writer.write("From: " + context.getStartDate() + " To: " + context.getEndDate() + "\n");
			writer.write("Format: " + context.getOutputFormat() + "\n");
			writer.write("...ledger report content here..."); // Placeholder for actual report data
		}

		return file;
	}

}
