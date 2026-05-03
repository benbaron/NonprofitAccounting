
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.ReportService.ReportType;


/**
 * Generates a balance sheet report.
 */
public class GenerateBalanceSheetAction extends SwingReportAction
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instantiates a new generate balance sheet action.
	 *
	 * @param reportService the report service
	 */
	public GenerateBalanceSheetAction(ReportService reportService)
	{
		super(reportService, ReportType.BALANCE_SHEET_JASPER, "Balance Sheet");
		
	}
	
}
