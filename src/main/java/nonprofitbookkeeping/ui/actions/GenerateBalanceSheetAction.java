
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDate;

public class GenerateBalanceSheetAction extends AbstractAction
{
	public GenerateBalanceSheetAction(ReportService service)
	{
		super("Generate Balance Sheet");
	}
	
	@Override public void actionPerformed(ActionEvent e)
	{
		
		try
		{
			String start = JOptionPane.showInputDialog("Start Date (yyyy-mm-dd):");
			String end = JOptionPane.showInputDialog("End Date (yyyy-mm-dd):");
			
			ReportContext ctx = new ReportContext();
			ctx.reportType = "balance_sheet";
			ctx.startDate = LocalDate.parse(start);
			ctx.endDate = LocalDate.parse(end);
			ctx.outputFormat = "xlsx";
			
			File f = ReportService.generate(ctx);
			JOptionPane.showMessageDialog(null, "Saved to: " + f.getAbsolutePath());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed: " + ex.getMessage());
		}
		
	}
	
}
