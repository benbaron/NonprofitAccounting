
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.ReportService.ReportType;

// TODO: Auto-generated Javadoc
/**
 * Generates a transaction detail report showing account activity.
 */
public class GenerateAccountActivityReportAction extends SwingReportAction
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instantiates a new generate account activity report action.
	 *
	 * @param reportService the report service
	 */
	public GenerateAccountActivityReportAction(ReportService reportService)
	{
		super(reportService, ReportType.TRANSACTION_REPORT_JASPER,
			"Account Activity Detail");
		
	}
	
}
