
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDate;

/**
 * GenerateReportsAction prompts the user to choose a report type, 
 * date range, and output format, then generates the specified report using the ReportService.
 */
public class GenerateReportsAction extends AbstractAction
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 3474943577654997739L;
	public GenerateReportsAction(ReportService service)
	{
		super("Generate All Ledger Reports");
	}
	
	/**
	 * 
	 * Override @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override public void actionPerformed(ActionEvent e)
	{
		
		try
		{
			// Prompt for report type.
			String[] reportOptions =
			{
				"ledger",
				"income_statement",
				"balance_sheet",
				"trial_balance",
				"cash_flow",
				"general_ledger"
			};
			String reportType = (String) JOptionPane.showInputDialog(
				null,
				"Select Report Type:",
				"Report Type",
				JOptionPane.QUESTION_MESSAGE,
				null,
				reportOptions,
				reportOptions[0]);
			
			if (reportType == null)
			{
				return; // User cancelled.
			}
			
			// Prompt for start and end dates (ISO format yyyy-MM-dd).
			String startInput = JOptionPane.showInputDialog("Enter start date (yyyy-MM-dd):");
			
			if (startInput == null || startInput.trim().isEmpty())
			{
				JOptionPane.showMessageDialog(null, "Start date is required.");
				return;
			}
			
			String endInput = JOptionPane.showInputDialog("Enter end date (yyyy-MM-dd):");
			
			if (endInput == null || endInput.trim().isEmpty())
			{
				JOptionPane.showMessageDialog(null, "End date is required.");
				return;
			}
			
			LocalDate startDate = LocalDate.parse(startInput);
			LocalDate endDate = LocalDate.parse(endInput);
			
			// Prompt for output format.
			String[] formatOptions = { "xlsx", "csv", "pdf" };
			String outputFormat =
				(String) JOptionPane.showInputDialog(
					null,
					"Select output format:",
					"Output Format",
					JOptionPane.PLAIN_MESSAGE,
					null,
					formatOptions,
					formatOptions[0]);
			
			if (outputFormat == null)
			{
				return; // User cancelled.
			}
			
			// Create and populate ReportContext.
			ReportContext ctx = new ReportContext();
			ctx.setReportType(reportType);
			ctx.setStartDate(startDate);
			ctx.setEndDate(endDate);
			ctx.setOutputFormat(outputFormat);
			
			// Generate the report.
			File output = ReportService.generate(ctx);
			JOptionPane.showMessageDialog(null,
				"Report generated at: " + output.getAbsolutePath());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null,
				"Error generating report: " + ex.getMessage(),
				"Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
}
