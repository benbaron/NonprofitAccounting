
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.ReportService.ReportType;

// TODO: Auto-generated Javadoc
/**
 * Generates an income statement using the {@link ReportService}.
 */
public class GenerateIncomeStatementAction extends SwingReportAction
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instantiates a new generate income statement action.
	 *
	 * @param reportService the report service
	 */
	public GenerateIncomeStatementAction(ReportService reportService)
	{
		super(reportService, ReportType.INCOME_STATEMENT_JASPER,
			"Income Statement");
		
	}
	
}
