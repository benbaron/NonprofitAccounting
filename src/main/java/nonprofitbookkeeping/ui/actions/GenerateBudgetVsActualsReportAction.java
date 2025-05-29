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
// import nonprofitbookkeeping.ui.helpers.DatePickerDialog; // Commented out for now

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


public class GenerateBudgetVsActualsReportAction implements EventHandler<ActionEvent> {

    // private static final long serialVersionUID = 1L; // Removed
    private final BudgetService budgetService;
    // private final ReportService reportService; // Retained if needed by other parts or for DI

    public GenerateBudgetVsActualsReportAction(ReportService reportService, BudgetService budgetService) {
        // super("Generate Budget vs. Actuals Report"); // Removed
        // this.reportService = reportService;
        this.budgetService = budgetService;
    }

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
            // TODO: Refactor DatePickerDialog.showDateRangeDialog to JavaFX or use an alternative JavaFX date range picker
            // For now, commenting out this section and subsequent logic.
            AlertBox.showInfo(parentWindow, "Date range selection is temporarily unavailable. Report generation aborted.");
            return; 
            /*
            Optional<LocalDate[]> datesOpt = DatePickerDialog.showDateRangeDialog(
                parentWindow, // Pass Window object
                "Select Report Period for Actuals",
                "Start Date:",
                "End Date:"
            );

            if (!datesOpt.isPresent()) {
                return; // User cancelled
            }

            LocalDate[] dates = datesOpt.get();
            LocalDate startDate = dates[0];
            LocalDate endDate = dates[1];

            if (startDate == null) {
                AlertBox.showError(parentWindow, "Start Date is required.");
                return;
            }
            if (endDate == null) {
                AlertBox.showError(parentWindow, "End Date is required.");
                return;
            }
            if (endDate.isBefore(startDate)) {
                AlertBox.showError(parentWindow, "End Date cannot be before Start Date.");
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
            */

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
}
