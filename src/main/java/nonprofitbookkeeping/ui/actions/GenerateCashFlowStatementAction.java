
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.ReportService.ReportType;


/**
 * Generates a cash flow statement.
 */
public class GenerateCashFlowStatementAction extends SwingReportAction
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instantiates a new generate cash flow statement action.
	 *
	 * @param reportService the report service
	 */
	public GenerateCashFlowStatementAction(ReportService reportService)
	{
		super(reportService, ReportType.CASH_FLOW_STATEMENT_JASPER,
			"Cash Flow Statement");
		
	}
	
}
