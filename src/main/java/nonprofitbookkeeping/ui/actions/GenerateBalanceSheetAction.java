package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.ReportService.ReportType;

/**
 * Generates a balance sheet report.
 */
public class GenerateBalanceSheetAction extends SwingReportAction
{
        private static final long serialVersionUID = 1L;

        public GenerateBalanceSheetAction(ReportService reportService)
        {
                super(reportService, ReportType.BALANCE_SHEET_JASPER, "Balance Sheet");
        }
}
