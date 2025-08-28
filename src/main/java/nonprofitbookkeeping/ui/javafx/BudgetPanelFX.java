
package nonprofitbookkeeping.ui.javafx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter; // Added import

import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;
import nonprofitbookkeeping.ui.javafx.dialogs.BudgetLineDialogFX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nonprofitbookkeeping.service.BudgetService;

import java.io.File;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Added import
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JavaFX panel for creating and editing Budget objects.
 * This corresponds to the Swing BudgetPanel.
 */
public class BudgetPanelFX extends VBox
{
        private static final Logger LOGGER = LoggerFactory.getLogger(BudgetPanelFX.class);
	
	// Data model fields
	private Budget currentBudget;
	private ChartOfAccounts chartOfAccounts;
	private List<Fund> availableFunds;
	private BudgetService budgetService;
	private File companyDirectory;
	
	// UI Components for Budget Properties
	private TextField txtBudgetName;
	private Spinner<Integer> spnFiscalYear;
	private TextField txtDescription;
	private ComboBox<Fund> cmbApplicableFund; // Changed from ComboBox<String>
	private TextField txtCurrency;
	
	// UI Components for Budget Lines
	private TableView<BudgetLine> tblBudgetLines;
	private Button btnAddLine;
	private Button btnEditLine;
	private Button btnRemoveLine;
	
	// UI Components for Actions
	private Button btnSaveBudget;
	private Button btnClose;
	
	// Sentinel object to represent "All Funds" or "None" in ComboBox<Fund>
	// This allows null to be a distinct value if needed, or provides a named "None"
	private static final Fund ALL_FUNDS_SENTINEL = new Fund();
	// Needs a way to set its display name if not null ID
	
	public BudgetPanelFX()
	{
		ALL_FUNDS_SENTINEL.setName("All Funds"); // Initialize sentinel display name
		
		initializeComponents();
		layoutComponents();
		attachListeners();
	}
	
	public BudgetPanelFX(BudgetService budgetService, File companyDirectory, ChartOfAccounts coa,
		List<Fund> funds, Budget budgetToEdit)
	{
		this();
		this.budgetService = Objects.requireNonNull(budgetService, "BudgetService cannot be null");
		this.companyDirectory =
			Objects.requireNonNull(companyDirectory, "Company directory cannot be null");
		loadBudget(budgetToEdit, coa, funds);
	}
	
	private void initializeComponents()
	{
		this.txtBudgetName = new TextField();
		this.txtBudgetName.setPromptText("Enter budget name");
		this.spnFiscalYear = new Spinner<>();
		this.spnFiscalYear.setEditable(true);
		this.txtDescription = new TextField();
		this.txtDescription.setPromptText("Enter description");
		
		this.cmbApplicableFund = new ComboBox<>(); // Now ComboBox<Fund>
		setupFundComboBox(); // Setup converter and cell factory
		
		this.txtCurrency = new TextField();
		this.txtCurrency.setPromptText("USD");
		this.txtCurrency.setEditable(false);
		
		this.tblBudgetLines = new TableView<>();
		setupTableColumns();
		this.tblBudgetLines.setPlaceholder(new Label("No budget lines added."));
		
		this.btnAddLine = new Button("Add Line");
		this.btnEditLine = new Button("Edit Line");
		this.btnRemoveLine = new Button("Remove Line");
		this.btnSaveBudget = new Button("Save Budget");
		this.btnClose = new Button("Close");
	}
	
	private void setupFundComboBox()
	{
		this.cmbApplicableFund.setConverter(new StringConverter<Fund>()
		{
			@Override public String toString(Fund fund)
			{
				
				if (fund == null || fund == ALL_FUNDS_SENTINEL)
				{
					return "All Funds"; // Display text for null or sentinel
				}
				
				return fund.getName();
			}
			
			@Override public Fund fromString(String string)
			{
				// Not generally needed for ComboBox if items are set programmatically
				return null;
			}
			
		});
		
		this.cmbApplicableFund.setCellFactory(listView -> new ListCell<Fund>()
		{
			@Override protected void updateItem(Fund fund, boolean empty)
			{
				super.updateItem(fund, empty);
				
				if (empty || fund == null)
				{
					setText(null);
				}
				else if (fund == ALL_FUNDS_SENTINEL)
				{
					setText("All Funds");
				}
				else
				{
					setText(fund.getName());
				}
				
			}
			
		});
	}
	
	
	@SuppressWarnings("unchecked") private void setupTableColumns()
	{
		TableColumn<BudgetLine, String> accountCol = new TableColumn<>("Account");
		accountCol.setCellValueFactory(new PropertyValueFactory<>("accountName"));
		TableColumn<BudgetLine, Periodicity> periodicityCol = new TableColumn<>("Periodicity");
		periodicityCol.setCellValueFactory(new PropertyValueFactory<>("periodicity"));
		TableColumn<BudgetLine, String> fundCol = new TableColumn<>("Line Fund");
		fundCol.setCellValueFactory(new PropertyValueFactory<>("fundName"));
		TableColumn<BudgetLine, BigDecimal> amountCol = new TableColumn<>("Amount");
		amountCol.setCellValueFactory(new PropertyValueFactory<>("totalBudgetedAmount"));
		TableColumn<BudgetLine, String> notesCol = new TableColumn<>("Notes");
		notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
		this.tblBudgetLines.getColumns().addAll(accountCol, periodicityCol, fundCol, amountCol,
			notesCol);
		
		accountCol.prefWidthProperty().bind(this.tblBudgetLines.widthProperty().multiply(0.25));
		periodicityCol.prefWidthProperty().bind(this.tblBudgetLines.widthProperty().multiply(0.15));
		fundCol.prefWidthProperty().bind(this.tblBudgetLines.widthProperty().multiply(0.15));
		amountCol.prefWidthProperty().bind(this.tblBudgetLines.widthProperty().multiply(0.15));
		notesCol.prefWidthProperty().bind(this.tblBudgetLines.widthProperty().multiply(0.25));
	}
	
	private void layoutComponents()
	{
		setSpacing(10);
		setPadding(new Insets(10));
		
		GridPane pnlProperties = new GridPane();
		pnlProperties.setHgap(10);
		pnlProperties.setVgap(8);
		pnlProperties.add(new Label("Budget Name:"), 0, 0);
		pnlProperties.add(this.txtBudgetName, 1, 0);
		GridPane.setHgrow(this.txtBudgetName, Priority.ALWAYS);
		pnlProperties.add(new Label("Fiscal Year:"), 0, 1);
		pnlProperties.add(this.spnFiscalYear, 1, 1);
		this.spnFiscalYear.setPrefWidth(100);
		pnlProperties.add(new Label("Description:"), 0, 2);
		pnlProperties.add(this.txtDescription, 1, 2);
		GridPane.setHgrow(this.txtDescription, Priority.ALWAYS);
		pnlProperties.add(new Label("Applicable Fund:"), 0, 3);
		pnlProperties.add(this.cmbApplicableFund, 1, 3);
		GridPane.setHgrow(this.cmbApplicableFund, Priority.ALWAYS);
		pnlProperties.add(new Label("Currency:"), 0, 4);
		pnlProperties.add(this.txtCurrency, 1, 4);
		this.txtCurrency.setMaxWidth(100);
		
		HBox pnlTableButtons = new HBox(5, this.btnAddLine, this.btnEditLine, this.btnRemoveLine);
		pnlTableButtons.setAlignment(Pos.CENTER_LEFT);
		pnlTableButtons.setPadding(new Insets(5, 0, 5, 0));
		
		VBox tableSection = new VBox(5, this.tblBudgetLines, pnlTableButtons);
		VBox.setVgrow(this.tblBudgetLines, Priority.ALWAYS);
		
		HBox pnlActions = new HBox(10, this.btnSaveBudget, this.btnClose);
		pnlActions.setAlignment(Pos.CENTER_RIGHT);
		pnlActions.setPadding(new Insets(5, 0, 0, 0));
		
		getChildren().addAll(pnlProperties, tableSection, pnlActions);
	}
	
	public void loadBudget(Budget budgetToEdit, ChartOfAccounts coa, List<Fund> funds)
	{
		this.chartOfAccounts = coa;
		// Ensure availableFunds is a modifiable list if we plan to add sentinels,
		// though here we add to ComboBox items
		this.availableFunds = (funds != null) ? new ArrayList<>(funds) : new ArrayList<>();
		
		if (budgetToEdit != null)
		{
			this.currentBudget = budgetToEdit;
			
			if (this.currentBudget.getBudgetLines() == null)
			{
				this.currentBudget.setBudgetLines(new ArrayList<>());
			}
			
		}
		else
		{
			this.currentBudget = new Budget("New Budget", LocalDate.now().getYear());
			this.currentBudget.setBudgetLines(new ArrayList<>());
			this.currentBudget.setCurrency("USD");
		}
		
		populateUIFromCurrentBudget();
	}
	
	private void populateUIFromCurrentBudget()
	{
		if (this.currentBudget == null)
			return;
		
		this.txtBudgetName.setText(this.currentBudget.getBudgetName());
		SpinnerValueFactory<Integer> yearFactory =
			new SpinnerValueFactory.IntegerSpinnerValueFactory(2000, 2100,
				this.currentBudget.getFiscalYear());
		this.spnFiscalYear.setValueFactory(yearFactory);
		this.txtDescription.setText(
			this.currentBudget.getDescription() != null ? this.currentBudget.getDescription() : "");
		this.txtCurrency.setText(this.currentBudget.getCurrency());
		
		// Populate Applicable Fund ComboBox
		List<Fund> fundComboItems = new ArrayList<>();
		fundComboItems.add(ALL_FUNDS_SENTINEL); // Represents "All Funds"
		
		if (this.availableFunds != null)
		{
			fundComboItems.addAll(
				this.availableFunds.stream().filter(Objects::nonNull).collect(Collectors.toList()));
		}
		
		this.cmbApplicableFund.setItems(FXCollections.observableArrayList(fundComboItems));
		
		if (this.currentBudget.getApplicableFundId() == null)
		{
			this.cmbApplicableFund.setValue(ALL_FUNDS_SENTINEL);
		}
		else
		{
			this.availableFunds.stream() // Search in original availableFunds
				.filter(f -> this.currentBudget.getApplicableFundId().equals(f.getFundId()))
				.findFirst().ifPresentOrElse(this.cmbApplicableFund::setValue,
					() -> this.cmbApplicableFund.setValue(ALL_FUNDS_SENTINEL) // Fallback
				);
		}
		
		refreshTableLines();
	}
	
	private void refreshTableLines()
	{
		
		if (this.currentBudget != null && this.currentBudget.getBudgetLines() != null)
		{
			this.tblBudgetLines
				.setItems(FXCollections.observableArrayList(this.currentBudget.getBudgetLines()));
		}
		else
		{
			this.tblBudgetLines.setItems(FXCollections.observableArrayList());
		}
		
		this.tblBudgetLines.refresh();
	}
	
	private void attachListeners()
	{
		this.txtBudgetName.textProperty().addListener((obs, oldVal, newVal) -> {
			if (this.currentBudget != null)
				this.currentBudget.setBudgetName(newVal);
		});
		this.txtDescription.textProperty().addListener((obs, oldVal, newVal) -> {
			if (this.currentBudget != null)
				this.currentBudget.setDescription(newVal);
		});
		this.spnFiscalYear.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (this.currentBudget != null && newVal != null)
				this.currentBudget.setFiscalYear(newVal);
		});
		
		this.cmbApplicableFund.valueProperty().addListener((obs, oldVal, newVal) -> {
			
			if (this.currentBudget != null)
			{
				
				if (newVal == null || newVal == ALL_FUNDS_SENTINEL)
				{
					this.currentBudget.setApplicableFundId(null);
				}
				else
				{
					this.currentBudget.setApplicableFundId(newVal.getFundId());
				}
				
			}
			
		});
		
                this.btnSaveBudget.setOnAction(e -> actionSaveBudget());
                this.btnClose.setOnAction(e -> LOGGER.debug("Close clicked."));
		
		this.btnAddLine.setOnAction(e -> actionAddLine());
		this.btnEditLine.setOnAction(e -> actionEditLine());
		this.btnRemoveLine.setOnAction(e -> actionRemoveLine());
	}
	
	private void actionSaveBudget()
	{
		
		if (this.currentBudget == null || this.budgetService == null ||
			this.companyDirectory == null)
		{
			Alert alert =
				new Alert(Alert.AlertType.ERROR, "Budget data not fully loaded. Cannot save.");
			alert.setHeaderText(null);
			alert.showAndWait();
			return;
		}
		
		try
		{
			List<Budget> allBudgets = this.budgetService.loadBudgets(this.companyDirectory);
			
			if (allBudgets == null)
			{
				allBudgets = new ArrayList<>();
			}
			
			String currentId = this.currentBudget.getBudgetId();
			boolean replaced = false;
			
			if (currentId != null)
			{
				
				for (int i = 0; i < allBudgets.size(); i++)
				{
					Budget b = allBudgets.get(i);
					
					if (b != null && currentId.equals(b.getBudgetId()))
					{
						allBudgets.set(i, this.currentBudget);
						replaced = true;
						break;
					}
					
				}
				
			}
			
			if (!replaced)
			{
				allBudgets.add(this.currentBudget);
			}
			
			BudgetService.saveBudgets(allBudgets, this.companyDirectory);
			
			Alert alert = new Alert(Alert.AlertType.INFORMATION, "Budget saved successfully.");
			alert.setHeaderText(null);
			alert.showAndWait();
		}
		catch (Exception ex)
		{
			Alert alert =
				new Alert(Alert.AlertType.ERROR, "Error saving budget: " + ex.getMessage());
			alert.setHeaderText("Save Error");
			alert.showAndWait();
		}
		
	}
	
	private void actionAddLine()
	{
		
		if (this.currentBudget == null || this.chartOfAccounts == null ||
			this.availableFunds == null)
		{
			Alert errorAlert =
				new Alert(Alert.AlertType.ERROR, "Budget data not fully loaded. Cannot add line.");
			errorAlert.setHeaderText(null);
			errorAlert.showAndWait();
			return;
		}
		
		BudgetLineDialogFX dialog = new BudgetLineDialogFX("Add Budget Line", null,
			this.chartOfAccounts, this.availableFunds);
		Optional<BudgetLine> result = dialog.showAndWait();
		
		result.ifPresent(newLine -> {
			
			if (this.currentBudget.getBudgetLines() == null)
			{
				this.currentBudget.setBudgetLines(new ArrayList<>());
			}
			
			this.currentBudget.getBudgetLines().add(newLine);
			refreshTableLines();
		});
	}
	
	private void actionEditLine()
	{
		BudgetLine selectedLine = this.tblBudgetLines.getSelectionModel().getSelectedItem();
		
		if (selectedLine != null)
		{
			
			if (this.chartOfAccounts == null || this.availableFunds == null)
			{
				Alert errorAlert = new Alert(Alert.AlertType.ERROR,
					"Chart of Accounts or Funds not available for editing.");
				errorAlert.setHeaderText(null);
				errorAlert.showAndWait();
				return;
			}
			
			BudgetLineDialogFX dialog = new BudgetLineDialogFX("Edit Budget Line", selectedLine,
				this.chartOfAccounts, this.availableFunds);
			Optional<BudgetLine> result = dialog.showAndWait();
			result.ifPresent(editedLine -> {
				refreshTableLines();
			});
		}
		else
		{
			Alert alert =
				new Alert(Alert.AlertType.INFORMATION, "No budget line selected to edit.");
			alert.setHeaderText(null);
			alert.showAndWait();
		}
		
	}
	
	private void actionRemoveLine()
	{
		BudgetLine selectedLine = this.tblBudgetLines.getSelectionModel().getSelectedItem();
		
		if (selectedLine != null)
		{
			Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
				"Are you sure you want to remove the selected budget line?", ButtonType.YES,
				ButtonType.NO);
			confirmAlert.setHeaderText("Confirm Removal");
			Optional<ButtonType> result = confirmAlert.showAndWait();
			
			if (result.isPresent() && result.get() == ButtonType.YES)
			{
				
				if (this.currentBudget != null && this.currentBudget.getBudgetLines() != null)
				{
					this.currentBudget.getBudgetLines().remove(selectedLine);
					refreshTableLines();
				}
				
			}
			
		}
		else
		{
			Alert alert =
				new Alert(Alert.AlertType.INFORMATION, "No budget line selected to remove.");
			alert.setHeaderText(null);
			alert.showAndWait();
		}
		
	}
	
}
