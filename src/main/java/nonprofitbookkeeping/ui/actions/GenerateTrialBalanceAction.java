package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.ReportService.ReportType;

/**
 * Generates a trial balance report.
 */
public class GenerateTrialBalanceAction extends SwingReportAction
{
        private static final long serialVersionUID = 1L;

        public GenerateTrialBalanceAction(ReportService reportService)
        {
                super(reportService, ReportType.TRIAL_BALANCE_JASPER, "Trial Balance");
        }
}
