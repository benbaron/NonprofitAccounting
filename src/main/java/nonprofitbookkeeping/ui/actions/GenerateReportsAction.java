
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.helpers.AlertBox; // Added

import javafx.event.ActionEvent; // Added
import javafx.event.EventHandler; // Added
import javafx.scene.control.ChoiceDialog; // Added
import javafx.scene.control.TextInputDialog; // Added
import javafx.stage.Window; // Added
// import javax.swing.*; // Removed
// import java.awt.event.ActionEvent; // Removed
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException; // Added
import java.util.Arrays; // Added
import java.util.List; // Added
import java.util.Optional; // Added


/**
 * GenerateReportsAction prompts the user to choose a report type,
 * date range, and output format, then generates the specified report using the ReportService.
 */
public class GenerateReportsAction implements EventHandler<ActionEvent> {
    // private static final long serialVersionUID = 3474943577654997739L; // Removed
    private final ReportService reportService; // Retained

    public GenerateReportsAction(ReportService service) {
        // super("Generate All Ledger Reports"); // Removed
        this.reportService = service;
    }

    @Override
    public void handle(ActionEvent event) {
        Window parentWindow = null;
        if (event.getSource() instanceof javafx.scene.Node) {
            parentWindow = ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        }

        try {
            // Prompt for report type.
            List<String> reportOptions = Arrays.asList(
                    "ledger",
                    "income_statement",
                    "balance_sheet",
                    "trial_balance",
                    "cash_flow",
                    "general_ledger");
            ChoiceDialog<String> reportTypeDialog = new ChoiceDialog<>(reportOptions.get(0), reportOptions);
            reportTypeDialog.initOwner(parentWindow);
            reportTypeDialog.setTitle("Report Type");
            reportTypeDialog.setHeaderText("Select Report Type:");
            reportTypeDialog.setContentText("Report Type:");
            Optional<String> reportTypeOpt = reportTypeDialog.showAndWait();

            if (!reportTypeOpt.isPresent()) {
                return; // User cancelled.
            }
            String reportType = reportTypeOpt.get();

            // Prompt for start and end dates (ISO format yyyy-MM-dd).
            // TODO: For a better user experience, replace with DatePicker controls in a custom dialog.
            TextInputDialog startDateDialog = new TextInputDialog();
            startDateDialog.initOwner(parentWindow);
            startDateDialog.setTitle("Start Date");
            startDateDialog.setHeaderText("Enter start date (yyyy-MM-dd):");
            startDateDialog.setContentText("Start Date:");
            Optional<String> startInputOpt = startDateDialog.showAndWait();

            if (!startInputOpt.isPresent() || startInputOpt.get().trim().isEmpty()) {
                AlertBox.showError(parentWindow, "Start date is required.");
                return;
            }
            String startInput = startInputOpt.get();

            TextInputDialog endDateDialog = new TextInputDialog();
            endDateDialog.initOwner(parentWindow);
            endDateDialog.setTitle("End Date");
            endDateDialog.setHeaderText("Enter end date (yyyy-MM-dd):");
            endDateDialog.setContentText("End Date:");
            Optional<String> endInputOpt = endDateDialog.showAndWait();

            if (!endInputOpt.isPresent() || endInputOpt.get().trim().isEmpty()) {
                AlertBox.showError(parentWindow, "End date is required.");
                return;
            }
            String endInput = endInputOpt.get();

            LocalDate startDate;
            LocalDate endDate;
            try {
                startDate = LocalDate.parse(startInput);
                endDate = LocalDate.parse(endInput);
            } catch (DateTimeParseException ex) {
                AlertBox.showError(parentWindow, "Invalid date format. Please use yyyy-MM-dd.");
                return;
            }
            
            if (endDate.isBefore(startDate)) {
                AlertBox.showError(parentWindow, "End Date cannot be before Start Date.");
                return;
            }

            // Prompt for output format.
            List<String> formatOptions = Arrays.asList("xlsx", "csv", "pdf");
            ChoiceDialog<String> formatDialog = new ChoiceDialog<>(formatOptions.get(0), formatOptions);
            formatDialog.initOwner(parentWindow);
            formatDialog.setTitle("Output Format");
            formatDialog.setHeaderText("Select output format:");
            formatDialog.setContentText("Format:");
            Optional<String> outputFormatOpt = formatDialog.showAndWait();

            if (!outputFormatOpt.isPresent()) {
                return; // User cancelled.
            }
            String outputFormat = outputFormatOpt.get();

            // Create and populate ReportContext.
            ReportContext ctx = new ReportContext();
            ctx.setReportType(reportType);
            ctx.setStartDate(startDate);
            ctx.setEndDate(endDate);
            ctx.setOutputFormat(outputFormat);

            // Generate the report.
            File output = ReportService.generate(ctx); // Assuming this static call is correct
            AlertBox.showInfo(parentWindow, "Report generated at: " + output.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertBox.showError(parentWindow, "Error generating report: " + ex.getMessage());
        }
    }
}
