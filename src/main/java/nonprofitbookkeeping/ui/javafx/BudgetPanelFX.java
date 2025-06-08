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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Added import
import java.util.Optional;

/**
 * JavaFX panel for creating and editing Budget objects.
 * This corresponds to the Swing BudgetPanel.
 */
public class BudgetPanelFX extends VBox {

    // Data model fields
    private Budget currentBudget;
    private ChartOfAccounts chartOfAccounts;
    private List<Fund> availableFunds;

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

    public BudgetPanelFX() {
        ALL_FUNDS_SENTINEL.setName("All Funds"); // Initialize sentinel display name

        initializeComponents();
        layoutComponents();
        attachListeners();
    }

    private void initializeComponents() {
        txtBudgetName = new TextField();
        txtBudgetName.setPromptText("Enter budget name");
        spnFiscalYear = new Spinner<>();
        spnFiscalYear.setEditable(true);
        txtDescription = new TextField();
        txtDescription.setPromptText("Enter description");

        cmbApplicableFund = new ComboBox<>(); // Now ComboBox<Fund>
        setupFundComboBox(); // Setup converter and cell factory

        txtCurrency = new TextField();
        txtCurrency.setPromptText("USD");
        txtCurrency.setEditable(false);

        tblBudgetLines = new TableView<>();
        setupTableColumns();
        tblBudgetLines.setPlaceholder(new Label("No budget lines added."));

        btnAddLine = new Button("Add Line");
        btnEditLine = new Button("Edit Line");
        btnRemoveLine = new Button("Remove Line");
        btnSaveBudget = new Button("Save Budget");
        btnClose = new Button("Close");
    }

    private void setupFundComboBox() {
        cmbApplicableFund.setConverter(new StringConverter<Fund>() {
            @Override
            public String toString(Fund fund) {
                if (fund == null || fund == ALL_FUNDS_SENTINEL) {
                    return "All Funds"; // Display text for null or sentinel
                }
                return fund.getName();
            }

            @Override
            public Fund fromString(String string) {
                // Not generally needed for ComboBox if items are set programmatically
                return null;
            }
        });

        cmbApplicableFund.setCellFactory(listView -> new ListCell<Fund>() {
            @Override
            protected void updateItem(Fund fund, boolean empty) {
                super.updateItem(fund, empty);
                if (empty || fund == null) {
                    setText(null);
                } else if (fund == ALL_FUNDS_SENTINEL) {
                    setText("All Funds");
                } else {
                    setText(fund.getName());
                }
            }
        });
    }


    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
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
        tblBudgetLines.getColumns().addAll(accountCol, periodicityCol, fundCol, amountCol, notesCol);

        accountCol.prefWidthProperty().bind(tblBudgetLines.widthProperty().multiply(0.25));
        periodicityCol.prefWidthProperty().bind(tblBudgetLines.widthProperty().multiply(0.15));
        fundCol.prefWidthProperty().bind(tblBudgetLines.widthProperty().multiply(0.15));
        amountCol.prefWidthProperty().bind(tblBudgetLines.widthProperty().multiply(0.15));
        notesCol.prefWidthProperty().bind(tblBudgetLines.widthProperty().multiply(0.25));
    }

    private void layoutComponents() {
        setSpacing(10);
        setPadding(new Insets(10));

        GridPane pnlProperties = new GridPane();
        pnlProperties.setHgap(10);
        pnlProperties.setVgap(8);
        pnlProperties.add(new Label("Budget Name:"), 0, 0);
        pnlProperties.add(txtBudgetName, 1, 0);
        GridPane.setHgrow(txtBudgetName, Priority.ALWAYS);
        pnlProperties.add(new Label("Fiscal Year:"), 0, 1);
        pnlProperties.add(spnFiscalYear, 1, 1);
        spnFiscalYear.setPrefWidth(100);
        pnlProperties.add(new Label("Description:"), 0, 2);
        pnlProperties.add(txtDescription, 1, 2);
        GridPane.setHgrow(txtDescription, Priority.ALWAYS);
        pnlProperties.add(new Label("Applicable Fund:"), 0, 3);
        pnlProperties.add(cmbApplicableFund, 1, 3);
        GridPane.setHgrow(cmbApplicableFund, Priority.ALWAYS);
        pnlProperties.add(new Label("Currency:"), 0, 4);
        pnlProperties.add(txtCurrency, 1, 4);
        txtCurrency.setMaxWidth(100);

        HBox pnlTableButtons = new HBox(5, btnAddLine, btnEditLine, btnRemoveLine);
        pnlTableButtons.setAlignment(Pos.CENTER_LEFT);
        pnlTableButtons.setPadding(new Insets(5,0,5,0));

        VBox tableSection = new VBox(5, tblBudgetLines, pnlTableButtons);
        VBox.setVgrow(tblBudgetLines, Priority.ALWAYS);

        HBox pnlActions = new HBox(10, btnSaveBudget, btnClose);
        pnlActions.setAlignment(Pos.CENTER_RIGHT);
        pnlActions.setPadding(new Insets(5,0,0,0));

        getChildren().addAll(pnlProperties, tableSection, pnlActions);
    }

    public void loadBudget(Budget budgetToEdit, ChartOfAccounts coa, List<Fund> funds) {
        this.chartOfAccounts = coa;
        // Ensure availableFunds is a modifiable list if we plan to add sentinels, though here we add to ComboBox items
        this.availableFunds = (funds != null) ? new ArrayList<>(funds) : new ArrayList<>();

        if (budgetToEdit != null) {
            this.currentBudget = budgetToEdit;
            if (this.currentBudget.getBudgetLines() == null) {
                this.currentBudget.setBudgetLines(new ArrayList<>());
            }
        } else {
            this.currentBudget = new Budget("New Budget", LocalDate.now().getYear());
            this.currentBudget.setBudgetLines(new ArrayList<>());
            this.currentBudget.setCurrency("USD");
        }
        populateUIFromCurrentBudget();
    }

    private void populateUIFromCurrentBudget() {
        if (currentBudget == null) return;

        txtBudgetName.setText(currentBudget.getBudgetName());
        SpinnerValueFactory<Integer> yearFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
            2000, 2100, currentBudget.getFiscalYear()
        );
        spnFiscalYear.setValueFactory(yearFactory);
        txtDescription.setText(currentBudget.getDescription() != null ? currentBudget.getDescription() : "");
        txtCurrency.setText(currentBudget.getCurrency());

        // Populate Applicable Fund ComboBox
        List<Fund> fundComboItems = new ArrayList<>();
        fundComboItems.add(ALL_FUNDS_SENTINEL); // Represents "All Funds"
        if (this.availableFunds != null) {
            fundComboItems.addAll(this.availableFunds.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        cmbApplicableFund.setItems(FXCollections.observableArrayList(fundComboItems));

        if (currentBudget.getApplicableFundId() == null) {
            cmbApplicableFund.setValue(ALL_FUNDS_SENTINEL);
        } else {
            this.availableFunds.stream() // Search in original availableFunds
                .filter(f -> currentBudget.getApplicableFundId().equals(f.getFundId()))
                .findFirst()
                .ifPresentOrElse(
                    cmbApplicableFund::setValue,
                    () -> cmbApplicableFund.setValue(ALL_FUNDS_SENTINEL) // Fallback
                );
        }
        refreshTableLines();
    }

    private void refreshTableLines() {
        if (currentBudget != null && currentBudget.getBudgetLines() != null) {
            tblBudgetLines.setItems(FXCollections.observableArrayList(currentBudget.getBudgetLines()));
        } else {
            tblBudgetLines.setItems(FXCollections.observableArrayList());
        }
        tblBudgetLines.refresh();
    }

    private void attachListeners() {
        txtBudgetName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (currentBudget != null) currentBudget.setBudgetName(newVal);
        });
        txtDescription.textProperty().addListener((obs, oldVal, newVal) -> {
            if (currentBudget != null) currentBudget.setDescription(newVal);
        });
        spnFiscalYear.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentBudget != null && newVal != null) currentBudget.setFiscalYear(newVal);
        });

        cmbApplicableFund.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentBudget != null) {
                if (newVal == null || newVal == ALL_FUNDS_SENTINEL) {
                    currentBudget.setApplicableFundId(null);
                } else {
                    currentBudget.setApplicableFundId(newVal.getFundId());
                }
            }
        });

        btnSaveBudget.setOnAction(e -> {
            System.out.println("Save Budget clicked.");
            if (currentBudget != null) {
                System.out.println("Budget Name: " + currentBudget.getBudgetName());
                System.out.println("Fiscal Year: " + currentBudget.getFiscalYear());
                System.out.println("Applicable Fund ID: " + currentBudget.getApplicableFundId());
            }
        });
        btnClose.setOnAction(e -> System.out.println("Close clicked."));

        btnAddLine.setOnAction(e -> actionAddLine());
        btnEditLine.setOnAction(e -> actionEditLine());
        btnRemoveLine.setOnAction(e -> actionRemoveLine());
    }

    private void actionAddLine() {
        if (currentBudget == null || chartOfAccounts == null || availableFunds == null) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Budget data not fully loaded. Cannot add line.");
            errorAlert.setHeaderText(null);
            errorAlert.showAndWait();
            return;
        }

        BudgetLineDialogFX dialog = new BudgetLineDialogFX("Add Budget Line", null, chartOfAccounts, availableFunds);
        Optional<BudgetLine> result = dialog.showAndWait();

        result.ifPresent(newLine -> {
            if (currentBudget.getBudgetLines() == null) {
                currentBudget.setBudgetLines(new ArrayList<>());
            }
            currentBudget.getBudgetLines().add(newLine);
            refreshTableLines();
        });
    }

    private void actionEditLine() {
        BudgetLine selectedLine = tblBudgetLines.getSelectionModel().getSelectedItem();
        if (selectedLine != null) {
            if (chartOfAccounts == null || availableFunds == null) {
                 Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Chart of Accounts or Funds not available for editing.");
                 errorAlert.setHeaderText(null);
                 errorAlert.showAndWait();
                 return;
            }
            BudgetLineDialogFX dialog = new BudgetLineDialogFX("Edit Budget Line", selectedLine, chartOfAccounts, availableFunds);
            Optional<BudgetLine> result = dialog.showAndWait();
            result.ifPresent(editedLine -> {
                refreshTableLines();
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No budget line selected to edit.");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    private void actionRemoveLine() {
        BudgetLine selectedLine = tblBudgetLines.getSelectionModel().getSelectedItem();
        if (selectedLine != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to remove the selected budget line?",
                ButtonType.YES, ButtonType.NO);
            confirmAlert.setHeaderText("Confirm Removal");
            Optional<ButtonType> result = confirmAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.YES) {
                if (currentBudget != null && currentBudget.getBudgetLines() != null) {
                    currentBudget.getBudgetLines().remove(selectedLine);
                    refreshTableLines();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No budget line selected to remove.");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }
}
