
package nonprofitbookkeeping.ui.javafx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window; // Required for initOwner
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.service.BudgetService;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * A JavaFX {@link Dialog} for creating a new budget or editing an existing one.
 * This dialog allows users to define budget properties such as name, fiscal year,
 * description, applicable fund, and currency. It also includes a section for
 * managing budget lines (though the detailed implementation for budget lines is
 * marked as TODO).
 * <p>
 * The dialog interacts with {@link BudgetService} (though currently unused) and
 * uses {@link ChartOfAccounts} and a list of {@link Fund}s for context, such as populating
 * a ComboBox for fund selection.
 * </p>
 * <p>
 * Upon successful completion (e.g., clicking a "Save Budget" button, which is not fully
 * implemented with a result converter yet), it would typically return the created or
 * modified {@link Budget} object.
 * </p>
 */
public class BudgetEditorDialogFX extends Dialog<Budget>
{
	
	/** The {@link Budget} object being created or edited. */
	private Budget currentBudget;
	/**
	 * The {@link ChartOfAccounts} for the current company.
	 * Currently unused in this dialog's direct logic but kept for potential future context.
	 */
	private ChartOfAccounts chartOfAccounts;
	/** List of available {@link Fund}s to populate the fund selector ComboBox. */
	private List<Fund> availableFunds;
	/**
	 * The {@link BudgetService} for budget-related operations.
	 * Currently unused in this dialog's direct logic but kept for potential future context.
	 */
	private BudgetService budgetService;
	/**
	 * The company directory, potentially used for saving/loading budget-related files.
	 * Currently unused in this dialog's direct logic but kept for potential future context.
	 */
	private File companyDirectory;
	
	// UI Fields for Budget Properties
	/** TextField for entering or displaying the budget name. */
	private TextField budgetNameField;
	/** Spinner for selecting or displaying the fiscal year of the budget. */
	private Spinner<Integer> fiscalYearSpinner;
	/** TextField for entering or displaying an optional description for the budget. */
	private TextField descriptionField;
	/** ComboBox for selecting an applicable fund for the budget. Can be "All Funds". */
	private ComboBox<Fund> applicableFundComboBox;
	/** TextField for displaying the budget's currency (typically non-editable, derived from company settings). */
	private TextField currencyField;
	
	// UI Fields for Budget Lines (Placeholder for detailed implementation)
	/** TableView intended to display budget lines. Currently shows a placeholder. */
	private TableView<Object> budgetLinesTable; // Should be TableView<BudgetLineFX> or similar
	/** Button to trigger adding a new budget line. Functionality is TODO. */
	private Button addLineButton;
	/** Button to trigger editing an existing budget line. Functionality is TODO. */
	private Button editLineButton;
	/** Button to trigger removing an existing budget line. Functionality is TODO. */
	private Button removeLineButton;
	
	/**
     * Constructs a new {@code BudgetEditorDialogFX}.
     * Initializes the UI components for editing budget properties and managing budget lines.
     * If {@code budgetToEdit} is provided, the dialog populates its fields with the data from this budget
     * (edit mode). Otherwise, it initializes fields for creating a new budget (create mode),
     * defaulting the fiscal year to the current year and currency from {@link CurrentCompany} if available.
     * <p>
     * Note: The parameters {@code chartOfAccounts}, {@code budgetService}, and {@code companyDirectory}
     * are stored but not actively used in the current UI logic of this dialog, suggesting they
     * are for future enhancements or were part of a previous design.
     * The management of budget lines (add, edit, remove) and the dialog's result conversion
     * (saving the budget) are marked as TODO.
     * </p>
     *
     * @param owner The parent {@link Window} for this dialog, used for proper modality.
     * @param chartOfAccounts The {@link ChartOfAccounts} of the current company (currently unused).
     * @param funds A list of available {@link Fund}s to populate the fund selector. Can be null or empty.
     * @param budgetService The {@link BudgetService} (currently unused).
     * @param companyDirectory The company's data directory (currently unused).
     * @param budgetToEdit The {@link Budget} to edit. If null, the dialog enters "create new budget" mode.
     */
	public BudgetEditorDialogFX(Window owner, ChartOfAccounts chartOfAccounts, List<Fund> funds,
		BudgetService budgetService, File companyDirectory, Budget budgetToEdit)
	{
		initOwner(owner);
		setTitle(budgetToEdit == null ? "Create New Budget" : "Edit Budget");
		
		this.chartOfAccounts = chartOfAccounts;
		this.availableFunds = (funds != null) ? funds : new ArrayList<>();
		this.budgetService = budgetService;
		this.companyDirectory = companyDirectory;
		
		if (budgetToEdit != null)
		{
			// For now, work directly on the passed object. Consider cloning if edits should
			// be cancelable without affecting original.
			this.currentBudget = budgetToEdit;
		}
		else
		{
			this.currentBudget = new Budget("New Budget", LocalDate.now().getYear());
			// Default currency setting (example, might need adjustment based on actual app
			// context)
			String defaultCurrency = "USD";
			
			if (nonprofitbookkeeping.model.CurrentCompany.getCompany() != null &&
				nonprofitbookkeeping.model.CurrentCompany.getCompany().getCompanyProfile() !=
					null &&
				nonprofitbookkeeping.model.CurrentCompany.getCompany().getCompanyProfile()
					.getBaseCurrency() != null &&
				!nonprofitbookkeeping.model.CurrentCompany.getCompany().getCompanyProfile()
					.getBaseCurrency().isEmpty())
			{
				defaultCurrency = nonprofitbookkeeping.model.CurrentCompany.getCompany()
					.getCompanyProfile().getBaseCurrency();
			}
			
			this.currentBudget.setCurrency(defaultCurrency);
		}
		
		DialogPane dialogPane = getDialogPane();
		dialogPane.setPrefWidth(700);
		
		BorderPane mainLayout = new BorderPane();
		mainLayout.setPadding(new Insets(10));
		
		GridPane propertiesGrid = new GridPane();
		propertiesGrid.setHgap(10);
		propertiesGrid.setVgap(8);
		propertiesGrid.setPadding(new Insets(5));
		
		this.budgetNameField = new TextField();
		this.fiscalYearSpinner = new Spinner<>(2000, 2100, this.currentBudget.getFiscalYear()); // Use
																								// year
																								// from
																								// currentBudget
		this.fiscalYearSpinner.setEditable(true);
		this.descriptionField = new TextField();
		this.applicableFundComboBox = new ComboBox<>();
		this.currencyField = new TextField();
		this.currencyField.setEditable(false);
		
		propertiesGrid.add(new Label("Budget Name:"), 0, 0);
		propertiesGrid.add(this.budgetNameField, 1, 0);
		propertiesGrid.add(new Label("Fiscal Year:"), 0, 1);
		propertiesGrid.add(this.fiscalYearSpinner, 1, 1);
		propertiesGrid.add(new Label("Description:"), 0, 2);
		propertiesGrid.add(this.descriptionField, 1, 2);
		propertiesGrid.add(new Label("Applicable Fund:"), 0, 3);
		propertiesGrid.add(this.applicableFundComboBox, 1, 3);
		propertiesGrid.add(new Label("Currency:"), 0, 4);
		propertiesGrid.add(this.currencyField, 1, 4);
		
		mainLayout.setTop(propertiesGrid);
		
		VBox budgetLinesSection = new VBox(10);
		budgetLinesSection.setPadding(new Insets(10, 0, 10, 0));
		this.budgetLinesTable = new TableView<>();
		this.budgetLinesTable.setPlaceholder(new Label("No budget lines added yet."));
		
		HBox linesButtonsBox = new HBox(10);
		this.addLineButton = new Button("Add Line");
		this.editLineButton = new Button("Edit Line");
		this.removeLineButton = new Button("Remove Line");
		linesButtonsBox.getChildren().addAll(this.addLineButton, this.editLineButton,
			this.removeLineButton);
		
		budgetLinesSection.getChildren().addAll(new Separator(), new Label("Budget Lines:"),
			this.budgetLinesTable, linesButtonsBox);
		mainLayout.setCenter(budgetLinesSection);
		
		dialogPane.setContent(mainLayout);
		
		ButtonType saveButtonType = new ButtonType("Save Budget", ButtonBar.ButtonData.OK_DONE);
		dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
		
		populateFieldsFromBudget();
		
		// TODO: Set result converter for the dialog to return the currentBudget upon save.
		// TODO: Add event handlers for addLineButton, editLineButton, removeLineButton.
		// TODO: Implement logic for collecting data from UI fields back into currentBudget upon save (collectFieldsToBudget method).
		// TODO: Consider creating a BudgetLineFX class for TableView and a BudgetLineDialogFX for line editing.
	}
	
	/**
     * Populates the dialog's UI fields with data from the {@link #currentBudget} object.
     * This method is called during dialog initialization to display an existing budget's
     * details or to set defaults for a new budget.
     * It handles setting the budget name, fiscal year, description, currency,
     * and the selected fund in the ComboBox.
     */
	private void populateFieldsFromBudget()
	{
		if (this.currentBudget == null) {
			// Should not happen if constructor logic for new budget is correct
			this.budgetNameField.setText("");
            this.fiscalYearSpinner.getValueFactory().setValue(LocalDate.now().getYear());
            this.descriptionField.setText("");
            this.currencyField.setText("USD"); // Default fallback
            this.applicableFundComboBox.getSelectionModel().clearSelection();
            this.applicableFundComboBox.setPromptText("Select Fund");
			return;
        }
		this.budgetNameField.setText(this.currentBudget.getBudgetName());
		
		// fiscalYearSpinner value is set during initialization
		if (this.currentBudget.getFiscalYear() !=
			this.fiscalYearSpinner.getValueFactory().getValue())
		{
			this.fiscalYearSpinner.getValueFactory().setValue(this.currentBudget.getFiscalYear());
		}
		
		this.descriptionField.setText(
			this.currentBudget.getDescription() != null ? this.currentBudget.getDescription() : "");
		this.currencyField.setText(
			this.currentBudget.getCurrency() != null ? this.currentBudget.getCurrency() : "USD");
		
		if (this.availableFunds != null)
		{
			this.applicableFundComboBox
				.setItems(FXCollections.observableArrayList(this.availableFunds));
			this.applicableFundComboBox.setCellFactory(lv -> new ListCell<Fund>()
			{
				@Override protected void updateItem(Fund fund, boolean empty)
				{
					super.updateItem(fund, empty);
					setText(empty || fund == null ? null : fund.getName());
				}
				
			});
			this.applicableFundComboBox.setButtonCell(new ListCell<Fund>()
			{
				@Override protected void updateItem(Fund fund, boolean empty)
				{
					super.updateItem(fund, empty);
					setText(empty || fund == null ? null : fund.getName());
				}
				
			});
			
			if (this.currentBudget.getApplicableFundId() != null)
			{
				this.availableFunds.stream()
					.filter(f -> this.currentBudget.getApplicableFundId().equals(f.getFundId()))
					.findFirst()
					.ifPresent(this.applicableFundComboBox::setValue);
			}
			else
			{
				// Optionally, add a "None" or "All Funds" option to ComboBox and select it here
				this.applicableFundComboBox.getSelectionModel().clearSelection();
				this.applicableFundComboBox.setPromptText("All Funds (default)");
			}
			
		}
		else
		{
			this.applicableFundComboBox.setPlaceholder(new Label("No funds available"));
		}
		
	}
	
	// TODO: collectFieldsToBudget() method
	// TODO: Result converter
	// TODO: BudgetLineFX class and BudgetLineDialogFX for adding/editing lines
}
