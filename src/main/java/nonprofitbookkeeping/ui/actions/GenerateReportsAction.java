
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.helpers.AlertBox; 
import nonprofitbookkeeping.ui.helpers.DateRangePickerDialog; 

import javafx.event.ActionEvent; 
import javafx.event.EventHandler; 
import javafx.scene.control.ChoiceDialog; 
import javafx.stage.Window; 
import java.io.File;
import java.time.LocalDate;
import java.util.Arrays; 
import java.util.List; 
import java.util.Optional; 


/**
 * GenerateReportsAction prompts the user to choose a report type, date range,
 * and output format, then generates the specified report using the
 * ReportService.
 */
/**
 * JavaFX action handler for generating various types of reports. This action
 * prompts the user to select a report type (e.g., ledger, income statement), a
 * date range (start and end dates), and an output format (e.g., xlsx, csv,
 * pdf). It then utilizes the {@link ReportService} to generate the specified
 * report. User input is gathered through a series of {@link ChoiceDialog} and
 * {@link TextInputDialog} instances.
 */
public class GenerateReportsAction implements EventHandler<ActionEvent>
{
	/** Backing service used to dispatch report generation. */
	private final ReportService service;
	
	/**
	 * Constructs a new {@code GenerateReportsAction}.
	 *
	 * @param service The {@link ReportService} to be used for generating reports.
	 */
	public GenerateReportsAction(ReportService service)
	{
		this.service = service;
		
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Handles the JavaFX action event to orchestrate the report generation process:
	 * <ol>
	 *   <li>Prompts the user to select a report type from a predefined list
	 *       (ledger, income_statement, balance_sheet, trial_balance, cash_flow, general_ledger)
	 *       using a {@link ChoiceDialog}.</li>
	 *   <li>Prompts for start and end dates using a custom dialog with {@link javafx.scene.control.DatePicker}s.</li>
	 *   <li>Validates that both dates are provided and that the end date is not before the start date.</li>
	 *   <li>Prompts for the output format (xlsx, csv, pdf) using a {@link ChoiceDialog}.</li>
	 *   <li>If any dialog is cancelled by the user, the action terminates.</li>
	     *   <li>Constructs a {@link ReportContext} with the selected criteria.</li>
	     *   <li>Calls the injected {@link ReportService} to produce the report file.</li>
	 *   <li>Shows an information alert with the path to the generated report, or an error alert if generation fails.</li>
	 * </ol>
	 * Displays general {@link Exception} messages if errors occur during report generation.
	 * </p>
	 * @param event The {@link ActionEvent} that triggered this handler (e.g., a menu item click).
	 */
	@Override
	public void handle(ActionEvent event)
	{
		Window parentWindow = null;
		
		if (event.getSource() instanceof javafx.scene.Node)
		{
			parentWindow =
				((javafx.scene.Node) event.getSource()).getScene().getWindow();
		}
		
		try
		{
			// Prompt for report type.
			List<String> reportOptions =
				Arrays.asList("ledger", "income_statement", "balance_sheet",
					"trial_balance",
					"cash_flow", "budget_vs_actuals", "account_activity_detail",
					"general_ledger");
			ChoiceDialog<String> reportTypeDialog =
				new ChoiceDialog<>(reportOptions.get(0), reportOptions);
			reportTypeDialog.initOwner(parentWindow);
			reportTypeDialog.setTitle("Report Type");
			reportTypeDialog.setHeaderText("Select Report Type:");
			reportTypeDialog.setContentText("Report Type:");
			Optional<String> reportTypeOpt = reportTypeDialog.showAndWait();
			
			if (!reportTypeOpt.isPresent())
			{
				return; // User cancelled.
			}
			
			String reportType = reportTypeOpt.get();
			
			// Prompt for start and end dates using a JavaFX DatePicker dialog.
			Optional<LocalDate[]> rangeOpt =
				DateRangePickerDialog.show(parentWindow,
					"Select Report Period", "Start Date:", "End Date:");
			
			if (!rangeOpt.isPresent())
			{
				return; // User cancelled
			}
			
			LocalDate[] range = rangeOpt.get();
			LocalDate startDate = range[0];
			LocalDate endDate = range[1];
			
			if (startDate == null)
			{
				AlertBox.showError(parentWindow, "Start date is required.");
				return;
			}
			
			if (endDate == null)
			{
				AlertBox.showError(parentWindow, "End date is required.");
				return;
			}
			
			if (endDate.isBefore(startDate))
			{
				AlertBox.showError(parentWindow,
					"End Date cannot be before Start Date.");
				return;
			}
			
			// Prompt for output format.
			List<String> formatOptions = Arrays.asList("xlsx", "csv", "pdf");
			ChoiceDialog<String> formatDialog =
				new ChoiceDialog<>(formatOptions.get(0), formatOptions);
			formatDialog.initOwner(parentWindow);
			formatDialog.setTitle("Output Format");
			formatDialog.setHeaderText("Select output format:");
			formatDialog.setContentText("Format:");
			Optional<String> outputFormatOpt = formatDialog.showAndWait();
			
			if (!outputFormatOpt.isPresent())
			{
				return; // User cancelled.
			}
			
			String outputFormat = outputFormatOpt.get();
			
			// Create and populate ReportContext.
			ReportContext ctx = new ReportContext();
			ctx.setReportType(reportType);
			ctx.setStartDate(startDate);
			ctx.setEndDate(endDate);
			ctx.setOutputFormat(outputFormat);
			
			// Generate the report using the injected service instance.
			File output = this.service.generateJasperReport(ctx, outputFormat);
			AlertBox.showInfo(parentWindow,
				"Report generated at: " + output.getAbsolutePath());
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			AlertBox.showError(parentWindow,
				"Error generating report: " + ex.getMessage());
		}
		
	}
	
	/**
	 * Placeholder method, potentially from a previous Swing context or an unimplemented interface.
	 * This method is not part of the JavaFX {@link EventHandler} interface and is currently a stub.
	 *
	 * @param object The event object (type is generic Object, specific context unknown).
	 */
	public void actionPerformed(Object object)
	{
		handle(new ActionEvent());
		
	}
	
}
