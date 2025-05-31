package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.BudgetService;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.helpers.AlertBox; // Added

import javafx.event.ActionEvent; // Added
import javafx.event.EventHandler; // Added
import javafx.scene.control.ChoiceDialog; // Added
import javafx.stage.Window; // Added
// import javax.swing.*; // Removed
// import java.awt.event.ActionEvent; // Removed
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// JavaFX imports for the new dialog
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.Node;


public class GenerateBudgetVsActualsReportAction implements EventHandler<ActionEvent> {

    // private static final long serialVersionUID = 1L; // Removed
    private final BudgetService budgetService;
    // private final ReportService reportService; // Retained if needed by other parts or for DI

    public GenerateBudgetVsActualsReportAction(ReportService reportService, BudgetService budgetService) {
        // super("Generate Budget vs. Actuals Report"); // Removed
        // this.reportService = reportService;
        this.budgetService = budgetService;
    }

    /**
     * Handles the action event for generating a Budget vs. Actuals report.
     * This multi-step process involves:
     * <ol>
     *   <li>Verifying that a company is open and its directory is accessible.</li>
     *   <li>Loading available budgets using {@link BudgetService}. If none, an info alert is shown.</li>
     *   <li>Prompting the user to select a budget via a {@link ChoiceDialog}.</li>
     *   <li>Prompting the user to select a date range for the 'Actuals' data using a custom JavaFX dialog
     *       ( {@link #showJavaFXDateRangeDialog(Window, String, String, String, String)} ) which uses two {@link DatePicker} controls.</li>
     *   <li>Validating the selected dates (start date, end date, and ensure end date is not before start date).
     *       The custom dialog also performs some of this validation.</li>
     *   <li>Setting up a {@link ReportContext} with the chosen budget, date range, and report type ("budget_vs_actuals").</li>
     *   <li>Retrieving the {@link Ledger} and {@link ChartOfAccounts} from the {@link CurrentCompany}.</li>
     *   <li>Calling {@link ReportService#generate(ReportContext, Ledger, ChartOfAccounts)} to produce the report file.</li>
     *   <li>Displaying a success message with the report file path using {@link AlertBox}, or an error message
     *       if any step fails (e.g., I/O errors loading budgets, missing company data, report generation failure).</li>
     * </ol>
     * The action may be aborted if the user cancels any dialog, or if essential data is missing or invalid at any stage.
     *
     * @param event The {@link ActionEvent} that triggered this handler.
     */
    @Override
    public void handle(ActionEvent event) {
        Window parentWindow = null;
        if (event.getSource() instanceof javafx.scene.Node) {
            parentWindow = ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        }

        Company currentCompany = CurrentCompany.getCompany();
        if (currentCompany == null) {
            AlertBox.showError(parentWindow, "No company is currently open.");
            return;
        }

        File companyFile = currentCompany.getCompanyFile();
        if (companyFile == null) {
            AlertBox.showError(parentWindow, "Company file information is not available.");
            return;
        }
        File companyDirectory = companyFile.getParentFile();
        if (companyDirectory == null) {
            AlertBox.showError(parentWindow, "Cannot determine company directory.");
            return;
        }

        try {
            // 1. Load Available Budgets
            List<Budget> availableBudgets = budgetService.loadBudgets(companyDirectory);
            if (availableBudgets == null || availableBudgets.isEmpty()) {
                AlertBox.showInfo(parentWindow, "No budgets found. Please create a budget first."); // Changed to Info
                return;
            }

            // 2. Select Budget
            List<String> budgetNamesList = availableBudgets.stream()
                                       .map(Budget::getBudgetName)
                                       .collect(Collectors.toList());
            
            ChoiceDialog<String> budgetDialog = new ChoiceDialog<>(budgetNamesList.get(0), budgetNamesList);
            budgetDialog.initOwner(parentWindow);
            budgetDialog.setTitle("Choose Budget");
            budgetDialog.setHeaderText("Select a Budget:");
            budgetDialog.setContentText("Budget:");

            Optional<String> selectedBudgetNameOpt = budgetDialog.showAndWait();

            if (!selectedBudgetNameOpt.isPresent()) {
                return; // User cancelled
            }
            String selectedBudgetName = selectedBudgetNameOpt.get();

            Budget chosenBudget = availableBudgets.stream()
                                    .filter(b -> selectedBudgetName.equals(b.getBudgetName()))
                                    .findFirst()
                                    .orElse(null);

            if (chosenBudget == null) {
                AlertBox.showError(parentWindow, "Selected budget could not be found.");
                return;
            }
            
            // 3. Select Date Range
            Optional<LocalDate[]> datesOpt = showJavaFXDateRangeDialog(
                parentWindow,
                "Select Report Period for Actuals",
                "Please select the start and end dates for the report period.",
                "Start Date:",
                "End Date:"
            );

            if (!datesOpt.isPresent()) {
                // User cancelled, or dates were invalid (e.g., end before start, handled in dialog)
                // Optional: show a message that action was cancelled or dates were invalid.
                // For now, just return as the dialog itself would have shown an error if necessary.
                return;
            }

            LocalDate[] dates = datesOpt.get();
            LocalDate startDate = dates[0]; // Guaranteed non-null if datesOpt.isPresent()
            LocalDate endDate = dates[1];   // Guaranteed non-null if datesOpt.isPresent()

            // The check 'endDate.isBefore(startDate)' is now handled within showJavaFXDateRangeDialog's
            // result converter. If it was true, datesOpt would be empty.
            // So, this explicit check here is likely redundant but harmless as a defensive check.
            if (endDate.isBefore(startDate)) {
                // This block should ideally not be reached if dialog logic is correct.
                AlertBox.showError(parentWindow, "Date Selection Error", "End Date cannot be before Start Date. Please try again.");
                return;
            }

            // 4. Setup ReportContext
            ReportContext ctx = new ReportContext();
            ctx.setReportType("budget_vs_actuals");
            ctx.setStartDate(startDate);
            ctx.setEndDate(endDate);
            ctx.setSelectedBudget(chosenBudget); // Set the chosen budget
            ctx.setOutputFormat("xlsx");

            // 5. Retrieve Company Data & Generate Report
            Ledger ledger = currentCompany.getLedger();
            ChartOfAccounts chartOfAccounts = currentCompany.getChartOfAccounts();

            if (ledger == null || chartOfAccounts == null) {
                AlertBox.showError(parentWindow, "Ledger or Chart of Accounts not available.");
                return;
            }

            File f = ReportService.generate(ctx, ledger, chartOfAccounts);
            
            // 6. Show Success Message
            AlertBox.showInfo(parentWindow, "Budget vs. Actuals report saved to: " + f.getAbsolutePath());

        } catch (IOException ioe) {
            System.err.println("Error loading budgets: " + ioe.getMessage());
            ioe.printStackTrace();
            AlertBox.showError(parentWindow, "Error loading budgets: " + ioe.getMessage());
        } catch (Exception ex) {
            System.err.println("Error during Budget vs. Actuals report generation: " + ex.getMessage());
            ex.printStackTrace();
            AlertBox.showError(parentWindow, "Failed to generate report: " + ex.getMessage());
        }
    }

    /**
     * Displays a JavaFX dialog for selecting a date range.
     *
     * @param owner The parent window for this dialog.
     * @param title The title of the dialog window.
     * @param headerText The header text to display within the dialog.
     * @param startDateLabelText The label text for the start date picker.
     * @param endDateLabelText The label text for the end date picker.
     * @return An {@code Optional<LocalDate[]>} containing an array with two {@link LocalDate} objects
     *         (start date at index 0, end date at index 1) if the user confirms the selection
     *         and both dates are selected. Returns {@code Optional.empty()} if the dialog is
     *         cancelled or closed.
     */
    private static Optional<LocalDate[]> showJavaFXDateRangeDialog(
            Window owner, String title, String headerText,
            String startDateLabelText, String endDateLabelText) {

        Dialog<LocalDate[]> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");

        grid.add(new Label(startDateLabelText), 0, 0);
        grid.add(startDatePicker, 1, 0);
        grid.add(new Label(endDateLabelText), 0, 1);
        grid.add(endDatePicker, 1, 1);

        dialogPane.setContent(grid);

        Node okButton = dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true); // Disable OK button initially

        // Add listeners to enable OK button only if both dates are selected
        Runnable updateOkButtonState = () -> {
            okButton.setDisable(startDatePicker.getValue() == null || endDatePicker.getValue() == null);
        };
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateOkButtonState.run());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateOkButtonState.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                // Additional validation can be added here, e.g., startDate <= endDate
                if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
                     if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                        AlertBox.showError(owner, "Invalid Date Range", "End date cannot be before start date.");
                        return null; // Keep dialog open or indicate error
                    }
                    return new LocalDate[]{startDatePicker.getValue(), endDatePicker.getValue()};
                }
            }
            return null; // No valid result or cancelled
        });

        return dialog.showAndWait();
    }
}
