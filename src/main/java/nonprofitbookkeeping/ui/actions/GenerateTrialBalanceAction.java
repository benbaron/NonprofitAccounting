
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.ReportService.ReportType;


/**
 * Generates a trial balance report.
 */
public class GenerateTrialBalanceAction extends SwingReportAction
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instantiates a new generate trial balance action.
	 *
	 * @param reportService the report service
	 */
	public GenerateTrialBalanceAction(ReportService reportService)
	{
		super(reportService, ReportType.TRIAL_BALANCE_JASPER, "Trial Balance");
		
	}
	
}
