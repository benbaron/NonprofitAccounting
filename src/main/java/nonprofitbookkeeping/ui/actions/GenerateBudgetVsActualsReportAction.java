
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.service.BudgetService;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.helpers.DateRangePickerDialog;

import javafx.event.ActionEvent; // Added
import javafx.event.EventHandler; // Added
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Window;
// import javax.swing.*; // Removed
// import java.awt.event.ActionEvent; // Removed
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.ChartOfAccounts;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;


public class GenerateBudgetVsActualsReportAction extends AbstractAction
	implements EventHandler<ActionEvent>
{
	
	// private static final long serialVersionUID = 1L; // Not needed as this is
		// primarily a JavaFX EventHandler
	/** The {@link BudgetService} used to load available budgets. */
	private final BudgetService budgetService;
	// private final ReportService reportService; // Field is present but not used
	// in current constructor/handle logic
	
	/**
	 * Constructs a new {@code GenerateBudgetVsActualsReportAction}.
	 *
	 * @param reportService The {@link ReportService}. This parameter is currently not stored or used
	 *                      within this action's primary logic but is accepted by the constructor.
	 * @param budgetService The {@link BudgetService} used to load available budgets for selection.
	 *                      This service is stored and used by the action.
	 */
	public GenerateBudgetVsActualsReportAction(ReportService reportService, // Parameter currently
																			// not assigned to a
																			// field
		BudgetService budgetService)
	{
		// super("Generate Budget vs. Actuals Report"); // Constructor for
		// AbstractAction, not directly used for JavaFX
		// this.reportService = reportService; // This line was commented out in the
		// original code
		this.budgetService = budgetService;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Handles the JavaFX action event for generating a Budget vs. Actuals report. It performs these steps:
	 * <ol>
	 *   <li>Checks if a company is open and company file information is available; shows errors and returns if not.</li>
	 *   <li>Loads available budgets using {@link BudgetService#loadBudgets(File)}. If none, informs user and returns.</li>
	 *   <li>Prompts the user to select a budget using a {@link ChoiceDialog}. If cancelled, returns.</li>
         *   <li>Prompts the user for a date range using a JavaFX dialog with {@link javafx.scene.control.DatePicker}s.</li>
         *   <li>Sets up a {@link ReportContext} with the chosen budget and dates.</li>
         *   <li>Calls {@link ReportService#generate(ReportContext, Ledger, ChartOfAccounts)} to produce the report.</li>
         *   <li>Shows success or error alerts.</li>
	 * </ol>
	 * Catches {@link IOException} from loading budgets and general {@link Exception} during the process.
	 * </p>
	 * @param event The {@link ActionEvent} that triggered this handler (e.g., a menu item click).
	 */
	@Override public void handle(ActionEvent event)
	{
		Window parentWindow = null;
		
		if (event.getSource() instanceof javafx.scene.Node)
		{
			parentWindow = ((javafx.scene.Node) event.getSource()).getScene().getWindow();
		}
		
		Company currentCompany = CurrentCompany.getCompany();
		
		if (currentCompany == null)
		{
			AlertBox.showError(parentWindow, "No company is currently open.");
			return;
		}
		
		File companyFile = currentCompany.getCompanyFile();
		
		if (companyFile == null)
		{
			AlertBox.showError(parentWindow, "Company file information is not available.");
			return;
		}
		
		File companyDirectory = companyFile.getParentFile();
		
		if (companyDirectory == null)
		{
			AlertBox.showError(parentWindow, "Cannot determine company directory.");
			return;
		}
		
		try
		{
			// 1. Load Available Budgets
			List<Budget> availableBudgets = this.budgetService.loadBudgets(companyDirectory);
			
			if (availableBudgets == null || availableBudgets.isEmpty())
			{
				AlertBox.showInfo(parentWindow, "No budgets found. Please create a budget first."); // Changed
																									// //
																									// Info
				return;
			}
			
			// 2. Select Budget
			List<String> budgetNamesList = availableBudgets.stream()
				.map(Budget::getBudgetName)
				.collect(Collectors.toList());
			
			ChoiceDialog<String> budgetDialog =
				new ChoiceDialog<>(budgetNamesList.get(0), budgetNamesList);
			budgetDialog.initOwner(parentWindow);
			budgetDialog.setTitle("Choose Budget");
			budgetDialog.setHeaderText("Select a Budget:");
			budgetDialog.setContentText("Budget:");
			
			Optional<String> selectedBudgetNameOpt = budgetDialog.showAndWait();
			
			if (!selectedBudgetNameOpt.isPresent())
			{
				return; // User cancelled
			}
			
			String selectedBudgetName = selectedBudgetNameOpt.get();
			
			Budget chosenBudget = availableBudgets.stream()
				.filter(b -> selectedBudgetName.equals(b.getBudgetName()))
				.findFirst()
				.orElse(null);
			
			if (chosenBudget == null)
			{
				AlertBox.showError(parentWindow, "Selected budget could not be found.");
				return;
			}
			
                        // 3. Select Date Range using JavaFX DatePickers
                        Optional<LocalDate[]> rangeOpt = DateRangePickerDialog.show(parentWindow,
                                "Select Report Period for Actuals", "Start Date:", "End Date:");

                        if (!rangeOpt.isPresent())
                        {
                                return; // User cancelled
                        }

                        LocalDate[] range = rangeOpt.get();
                        LocalDate startDate = range[0];
                        LocalDate endDate = range[1];

                        if (startDate == null)
                        {
                                AlertBox.showError(parentWindow, "Start Date is required.");
                                return;
                        }

                        if (endDate == null)
                        {
                                AlertBox.showError(parentWindow, "End Date is required.");
                                return;
                        }

                        if (endDate.isBefore(startDate))
                        {
                                AlertBox.showError(parentWindow, "End Date cannot be before Start Date.");
                                return;
                        }

                        // 4. Setup ReportContext
                        ReportContext ctx = new ReportContext();
                        ctx.setReportType("budget_vs_actuals");
                        ctx.setStartDate(startDate);
                        ctx.setEndDate(endDate);
                        ctx.setSelectedBudget(chosenBudget);
                        ctx.setOutputFormat("xlsx");

                        // 5. Retrieve Company Data & Generate Report
                        Ledger ledger = currentCompany.getLedger();
                        ChartOfAccounts chartOfAccounts = currentCompany.getChartOfAccounts();

                        if (ledger == null || chartOfAccounts == null)
                        {
                                AlertBox.showError(parentWindow, "Ledger or Chart of Accounts not available.");
                                return;
                        }

                        File f = ReportService.generate(ctx, ledger, chartOfAccounts);
                        // 6. Show Success Message
                        AlertBox.showInfo(parentWindow,
                                "Budget vs. Actuals report saved to: " + f.getAbsolutePath());
			
		}
		catch (IOException ioe)
		{
			System.err.println("Error loading budgets: " + ioe.getMessage());
			ioe.printStackTrace();
			AlertBox.showError(parentWindow, "Error loading budgets: " + ioe.getMessage());
		}
		catch (Exception ex)
		{
			System.err
				.println("Error during Budget vs. Actuals report generation: " + ex.getMessage());
			ex.printStackTrace();
			AlertBox.showError(parentWindow, "Failed to generate report: " + ex.getMessage());
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Invoked when a Swing action occurs. This method is part of the {@link java.awt.event.ActionListener} interface,
	 * implemented via {@link javax.swing.AbstractAction}.
	 * Note: This is a stub implementation and currently does nothing.
	 * For JavaFX, the {@link #handle(ActionEvent)} method is the primary event handler.
	 * </p>
	 * @param e The {@link java.awt.event.ActionEvent} that occurred.
	 */
        @Override public void actionPerformed(java.awt.event.ActionEvent e)
        {
                handle(new ActionEvent());
        }
	
}
