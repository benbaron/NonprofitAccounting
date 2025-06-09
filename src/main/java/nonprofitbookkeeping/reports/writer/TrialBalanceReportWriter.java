/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * TrialBalanceReportWriter.java
 * TrialBalanceReportWriter
 */
package nonprofitbookkeeping.reports.writer;

import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.service.TrialBalanceService;
// nonprofitbookkeeping.model.Ledger is already imported above

/**
 * Responsible for orchestrating the generation and writing of trial balance reports.
 * This class currently holds references to a {@link TrialBalanceService} and a {@link Ledger},
 * which are intended to be used for computing and formatting the trial balance data.
 * Note: This class does not currently implement a report writing method (e.g., from {@code ReportWriterIntf}).
 * Its primary role as defined here is to be instantiated with the necessary service and data.
 */
public class TrialBalanceReportWriter
{
	private final TrialBalanceService trialBalanceService;
	private final Ledger ledger;

	/**
	 * Constructs a new {@code TrialBalanceReportWriter}.
	 *
	 * @param trialBalanceService The {@link TrialBalanceService} used to compute trial balance data from the ledger. Must not be null.
	 * @param ledger The {@link Ledger} containing the financial data from which the trial balance will be derived. Must not be null.
	 * @throws IllegalArgumentException if either {@code trialBalanceService} or {@code ledger} is null.
	 */
	public TrialBalanceReportWriter(TrialBalanceService trialBalanceService, Ledger ledger)
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

}
