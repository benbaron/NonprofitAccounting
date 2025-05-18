package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDate;

public class GenerateIncomeStatementAction extends AbstractAction {
    /**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -6004544138078222664L;

	public GenerateIncomeStatementAction(ReportService reportService) {
        super("Generate Income Statement");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            String start = JOptionPane.showInputDialog("Start Date (yyyy-mm-dd):");
            String end = JOptionPane.showInputDialog("End Date (yyyy-mm-dd):");

            ReportContext ctx = new ReportContext();
            ctx.reportType = "income_statement";
            ctx.startDate = LocalDate.parse(start);
            ctx.endDate = LocalDate.parse(end);
            ctx.outputFormat = "xlsx";

            File f = ReportService.generate(ctx);
            JOptionPane.showMessageDialog(null, "Saved to: " + f.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed: " + ex.getMessage());
        }
    }
}
