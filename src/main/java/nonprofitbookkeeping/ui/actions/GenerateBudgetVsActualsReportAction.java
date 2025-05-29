package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.BudgetService;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.helpers.DatePickerDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class GenerateBudgetVsActualsReportAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private final BudgetService budgetService;
    // ReportService is not strictly needed as a field if only static methods are used,
    // but kept for consistency with constructor if other actions use instance methods.
    // private final ReportService reportService; 

    public GenerateBudgetVsActualsReportAction(ReportService reportService, BudgetService budgetService) {
        super("Generate Budget vs. Actuals Report");
        // this.reportService = reportService;
        this.budgetService = budgetService;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Company currentCompany = CurrentCompany.getCompany();
        if (currentCompany == null) {
            JOptionPane.showMessageDialog(null, "No company is currently open.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File companyFile = currentCompany.getCompanyFile();
        if (companyFile == null) {
            JOptionPane.showMessageDialog(null, "Company file information is not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File companyDirectory = companyFile.getParentFile();
        if (companyDirectory == null) {
            JOptionPane.showMessageDialog(null, "Cannot determine company directory.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 1. Load Available Budgets
            List<Budget> availableBudgets = budgetService.loadBudgets(companyDirectory);
            if (availableBudgets == null || availableBudgets.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No budgets found. Please create a budget first.", "No Budgets Available", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Select Budget
            String[] budgetNames = availableBudgets.stream()
                                       .map(Budget::getBudgetName) // Assumes getBudgetName() is unique enough for selection
                                       .toArray(String[]::new);
            
            String selectedBudgetName = (String) JOptionPane.showInputDialog(
                null, // Or determine parent component
                "Select a Budget:",
                "Choose Budget",
                JOptionPane.QUESTION_MESSAGE,
                null,
                budgetNames,
                budgetNames[0] // Default selection
            );

            if (selectedBudgetName == null) {
                return; // User cancelled
            }

            Budget chosenBudget = availableBudgets.stream()
                                    .filter(b -> selectedBudgetName.equals(b.getBudgetName()))
                                    .findFirst()
                                    .orElse(null); // Should not happen if names are from the list

            if (chosenBudget == null) { // Should ideally not occur if selection is from list
                JOptionPane.showMessageDialog(null, "Selected budget could not be found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 3. Select Date Range
            Optional<LocalDate[]> datesOpt = DatePickerDialog.showDateRangeDialog(
                null, 
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
                JOptionPane.showMessageDialog(null, "Start Date is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (endDate == null) {
                JOptionPane.showMessageDialog(null, "End Date is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (endDate.isBefore(startDate)) {
                JOptionPane.showMessageDialog(null, "End Date cannot be before Start Date.", "Input Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(null, "Ledger or Chart of Accounts not available.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File f = ReportService.generate(ctx, ledger, chartOfAccounts);
            
            // 6. Show Success Message
            JOptionPane.showMessageDialog(null, "Budget vs. Actuals report saved to: " + f.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ioe) {
            System.err.println("Error loading budgets: " + ioe.getMessage());
            ioe.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading budgets: " + ioe.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            System.err.println("Error during Budget vs. Actuals report generation: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to generate report: " + ex.getMessage(), "Generation Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
