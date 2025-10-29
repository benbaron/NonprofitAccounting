package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.ReportService.ReportType;

/**
 * Generates a transaction detail report showing account activity.
 */
public class GenerateAccountActivityReportAction extends SwingReportAction
{
        private static final long serialVersionUID = 1L;

        public GenerateAccountActivityReportAction(ReportService reportService)
        {
                super(reportService, ReportType.TRANSACTION_REPORT_JASPER, "Account Activity Detail");
        }
}
