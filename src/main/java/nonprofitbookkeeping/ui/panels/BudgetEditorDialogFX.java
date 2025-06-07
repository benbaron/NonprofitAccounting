
package nonprofitbookkeeping.ui.panels;

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

public class BudgetEditorDialogFX extends Dialog<Budget>
{
	
	private Budget currentBudget;
	private List<Fund> availableFunds;
	// UI Fields for Budget Properties
	private TextField budgetNameField;
	private Spinner<Integer> fiscalYearSpinner;
	private TextField descriptionField;
	private ComboBox<Fund> applicableFundComboBox;
	private TextField currencyField;
	
	// UI Fields for Budget Lines (Placeholder for now)
	private TableView<Object> budgetLinesTable;
	private Button addLineButton;
	private Button editLineButton;
	private Button removeLineButton;
	
	public BudgetEditorDialogFX(Window owner, 
	                            ChartOfAccounts chartOfAccounts, 
	                            List<Fund> funds,
	                            BudgetService budgetService, 
	                            File companyDirectory, 
	                            Budget budgetToEdit)
	{
		initOwner(owner);
		setTitle(budgetToEdit == null ? "Create New Budget" : "Edit Budget");
		
		this.availableFunds = (funds != null) ? funds : new ArrayList<>();
		if (budgetToEdit != null)
		{
			// For now, work directly on the passed object. Consider cloning if edits should
			// be cancelable without affecting original.
			this.currentBudget = budgetToEdit;
		}
		else
		{
			this.currentBudget = new Budget("New Budget", LocalDate.now().getYear());
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
		this.fiscalYearSpinner = new Spinner<>(2000, 2100, this.currentBudget.getFiscalYear()); 
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
		linesButtonsBox.getChildren().addAll(this.addLineButton, 
			this.editLineButton, 
			this.removeLineButton);
		
		budgetLinesSection.getChildren().addAll(new Separator(), new Label("Budget Lines:"),
			this.budgetLinesTable, linesButtonsBox);
		mainLayout.setCenter(budgetLinesSection);
		
		dialogPane.setContent(mainLayout);
		
		ButtonType saveButtonType = new ButtonType("Save Budget", ButtonBar.ButtonData.OK_DONE);
		dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
		
		populateFieldsFromBudget();
		
		// TODO: Set result converter for save
		// TODO: Add event handlers for line buttons & save logic
	}
	
	private void populateFieldsFromBudget()
	{
		if (this.currentBudget == null)
			return;
		this.budgetNameField.setText(this.currentBudget.getBudgetName());
		
		// fiscalYearSpinner value is set during initialization
		if (this.currentBudget.getFiscalYear() != this.fiscalYearSpinner.getValueFactory().getValue())
		{
			this.fiscalYearSpinner.getValueFactory().setValue(this.currentBudget.getFiscalYear());
		}
		
		this.descriptionField
			.setText(this.currentBudget.getDescription() != null ? this.currentBudget.getDescription() : "");
		this.currencyField
			.setText(this.currentBudget.getCurrency() != null ? this.currentBudget.getCurrency() : "USD");
		
		if (this.availableFunds != null)
		{
			this.applicableFundComboBox.setItems(FXCollections.observableArrayList(this.availableFunds));
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
