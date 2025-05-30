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
 * Responsible for generating and writing trial balance reports.
 * This class utilizes a {@link TrialBalanceService} and a {@link Ledger}
 * to compute and format the trial balance data.
 */
public class TrialBalanceReportWriter
{
	private final TrialBalanceService trialBalanceService;
	private final Ledger ledger;

	/**  
	 * Constructs a new TrialBalanceReportWriter.
	 * 
	 * @param trialBalanceService The service used to compute trial balance data. Must not be null.
	 * @param ledger The ledger containing the financial data. Must not be null.
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
