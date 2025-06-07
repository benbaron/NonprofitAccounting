package nonprofitbookkeeping.ui.javafx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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

public class BudgetEditorDialogFX extends Dialog<Budget> {

    private Budget currentBudget;
    private ChartOfAccounts chartOfAccounts; // Unused in this initial structure but kept for context
    private List<Fund> availableFunds;
    private BudgetService budgetService; // Unused in this initial structure but kept for context
    private File companyDirectory; // Unused in this initial structure but kept for context

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

    public BudgetEditorDialogFX(Window owner, ChartOfAccounts chartOfAccounts, List<Fund> funds,
                                BudgetService budgetService, File companyDirectory, Budget budgetToEdit) {
        initOwner(owner);
        setTitle(budgetToEdit == null ? "Create New Budget" : "Edit Budget");

        this.chartOfAccounts = chartOfAccounts;
        this.availableFunds = (funds != null) ? funds : new ArrayList<>();
        this.budgetService = budgetService;
        this.companyDirectory = companyDirectory;

        if (budgetToEdit != null) {
            // For now, work directly on the passed object. Consider cloning if edits should be cancelable without affecting original.
            this.currentBudget = budgetToEdit;
        } else {
            this.currentBudget = new Budget("New Budget", LocalDate.now().getYear());
            // Default currency setting (example, might need adjustment based on actual app context)
            String defaultCurrency = "USD";
            if (nonprofitbookkeeping.model.CurrentCompany.getCompany() != null &&
                nonprofitbookkeeping.model.CurrentCompany.getCompany().getCompanyProfile() != null &&
                nonprofitbookkeeping.model.CurrentCompany.getCompany().getCompanyProfile().getBaseCurrency() != null &&
                !nonprofitbookkeeping.model.CurrentCompany.getCompany().getCompanyProfile().getBaseCurrency().isEmpty() ) {
                defaultCurrency = nonprofitbookkeeping.model.CurrentCompany.getCompany().getCompanyProfile().getBaseCurrency();
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

        budgetNameField = new TextField();
        fiscalYearSpinner = new Spinner<>(2000, 2100, currentBudget.getFiscalYear()); // Use year from currentBudget
        fiscalYearSpinner.setEditable(true);
        descriptionField = new TextField();
        applicableFundComboBox = new ComboBox<>();
        currencyField = new TextField();
        currencyField.setEditable(false);

        propertiesGrid.add(new Label("Budget Name:"), 0, 0);
        propertiesGrid.add(budgetNameField, 1, 0);
        propertiesGrid.add(new Label("Fiscal Year:"), 0, 1);
        propertiesGrid.add(fiscalYearSpinner, 1, 1);
        propertiesGrid.add(new Label("Description:"), 0, 2);
        propertiesGrid.add(descriptionField, 1, 2);
        propertiesGrid.add(new Label("Applicable Fund:"), 0, 3);
        propertiesGrid.add(applicableFundComboBox, 1, 3);
        propertiesGrid.add(new Label("Currency:"), 0, 4);
        propertiesGrid.add(currencyField, 1, 4);

        mainLayout.setTop(propertiesGrid);

        VBox budgetLinesSection = new VBox(10);
        budgetLinesSection.setPadding(new Insets(10, 0, 10, 0));
        budgetLinesTable = new TableView<>();
        budgetLinesTable.setPlaceholder(new Label("No budget lines added yet."));

        HBox linesButtonsBox = new HBox(10);
        addLineButton = new Button("Add Line");
        editLineButton = new Button("Edit Line");
        removeLineButton = new Button("Remove Line");
        linesButtonsBox.getChildren().addAll(addLineButton, editLineButton, removeLineButton);

        budgetLinesSection.getChildren().addAll(new Separator(), new Label("Budget Lines:"), budgetLinesTable, linesButtonsBox);
        mainLayout.setCenter(budgetLinesSection);

        dialogPane.setContent(mainLayout);

        ButtonType saveButtonType = new ButtonType("Save Budget", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        populateFieldsFromBudget();

        // TODO: Set result converter for save
        // TODO: Add event handlers for line buttons & save logic
    }

    private void populateFieldsFromBudget() {
        if (currentBudget == null) return;
        budgetNameField.setText(currentBudget.getBudgetName());
        // fiscalYearSpinner value is set during initialization
        if (currentBudget.getFiscalYear() != fiscalYearSpinner.getValueFactory().getValue()) {
             fiscalYearSpinner.getValueFactory().setValue(currentBudget.getFiscalYear());
        }
        descriptionField.setText(currentBudget.getDescription() != null ? currentBudget.getDescription() : "");
        currencyField.setText(currentBudget.getCurrency() != null ? currentBudget.getCurrency() : "USD");

        if (availableFunds != null) {
            applicableFundComboBox.setItems(FXCollections.observableArrayList(availableFunds));
            applicableFundComboBox.setCellFactory(lv -> new ListCell<Fund>() {
                @Override
                protected void updateItem(Fund fund, boolean empty) {
                    super.updateItem(fund, empty);
                    setText(empty || fund == null ? null : fund.getName());
                }
            });
            applicableFundComboBox.setButtonCell(new ListCell<Fund>() {
                @Override
                protected void updateItem(Fund fund, boolean empty) {
                    super.updateItem(fund, empty);
                    setText(empty || fund == null ? null : fund.getName());
                }
            });

            if (currentBudget.getApplicableFundId() != null) {
                availableFunds.stream()
                    .filter(f -> currentBudget.getApplicableFundId().equals(f.getFundId()))
                    .findFirst()
                    .ifPresent(applicableFundComboBox::setValue);
            } else {
                // Optionally, add a "None" or "All Funds" option to ComboBox and select it here
                 applicableFundComboBox.getSelectionModel().clearSelection();
                 applicableFundComboBox.setPromptText("All Funds (default)");
            }
        } else {
            applicableFundComboBox.setPlaceholder(new Label("No funds available"));
        }
    }

    // TODO: collectFieldsToBudget() method
    // TODO: Result converter
    // TODO: BudgetLineFX class and BudgetLineDialogFX for adding/editing lines
}
