package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.ReportService.ReportType;

/**
 * Generates an income statement using the {@link ReportService}.
 */
public class GenerateIncomeStatementAction extends SwingReportAction
{
        private static final long serialVersionUID = 1L;

        public GenerateIncomeStatementAction(ReportService reportService)
        {
                super(reportService, ReportType.INCOME_STATEMENT_JASPER, "Income Statement");
        }
}
