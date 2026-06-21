package org.nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.util.FormatUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Native alternate banking workspace for statement reconciliation. */
public class AlternateReconciliationPanel implements AppPanel, AppPanel.SaveAware
{
    private final VBox root = new VBox(12);
    private final AlternateReconciliationService service;
    private final ComboBox<String> accountBox = new ComboBox<>();
    private final DatePicker statementDate = new DatePicker(LocalDate.now());
    private final TextField beginningBalance = new TextField("0.00");
    private final TextField endingBalance = new TextField();
    private final ObservableList<AlternateReconciliationService.ReconciliationRow> rows = FXCollections.observableArrayList();
    private final Label clearedTotal = new Label();
    private final Label difference = new Label();
    private final Label validation = new Label();
    private final TextArea importReview = new TextArea();

    public AlternateReconciliationPanel()
    {
        this(new AlternateReconciliationService());
    }

    AlternateReconciliationPanel(AlternateReconciliationService service)
    {
        this.service = service;
        build();
        refreshAccounts();
    }

    @Override public String title() { return "Reconciliation"; }
    @Override public Node root() { return this.root; }

    private void build()
    {
        Label title = new Label("Reconciliation & Banking");
        title.getStyleClass().add("alternate-panel-title");
        Label subtitle = new Label("Review statement activity before posting transactions, then reconcile cleared ledger transactions without legacy panel adapters.");
        subtitle.setWrapText(true);
        subtitle.getStyleClass().add("alternate-panel-subtitle");
        this.root.setPadding(new Insets(12));
        this.root.getStyleClass().add("alternate-content-card");
        this.root.getChildren().setAll(title, subtitle, controls(), table(), totals(), new Separator(), importQueue());
    }

    private GridPane controls()
    {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        this.endingBalance.setPromptText("Statement ending balance");
        this.accountBox.setOnAction(e -> loadRows());
        this.beginningBalance.textProperty().addListener((obs, old, val) -> updateSummary());
        this.endingBalance.textProperty().addListener((obs, old, val) -> updateSummary());
        Button reload = new Button("Reload transactions");
        reload.setOnAction(e -> loadRows());
        grid.addRow(0, new Label("Account"), this.accountBox, reload);
        grid.addRow(1, new Label("Statement date"), this.statementDate);
        grid.addRow(2, new Label("Beginning balance"), this.beginningBalance);
        grid.addRow(3, new Label("Statement ending balance"), this.endingBalance);
        return grid;
    }

    private TableView<AlternateReconciliationService.ReconciliationRow> table()
    {
        TableView<AlternateReconciliationService.ReconciliationRow> table = new TableView<>(this.rows);
        table.setEditable(true);
        TableColumn<AlternateReconciliationService.ReconciliationRow, Boolean> cleared = new TableColumn<>("Cleared/Reconciled");
        cleared.setCellValueFactory(data -> data.getValue().clearedProperty());
        cleared.setCellFactory(CheckBoxTableCell.forTableColumn(cleared));
        cleared.setEditable(true);
        TableColumn<AlternateReconciliationService.ReconciliationRow, String> date = new TableColumn<>("Date");
        date.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().date()));
        TableColumn<AlternateReconciliationService.ReconciliationRow, String> memo = new TableColumn<>("Memo");
        memo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().memo()));
        TableColumn<AlternateReconciliationService.ReconciliationRow, BigDecimal> amount = new TableColumn<>("Amount");
        amount.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().amount()));
        table.getColumns().addAll(cleared, date, memo, amount);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    private HBox totals()
    {
        Button save = new Button("Save Reconciliation");
        save.setOnAction(e -> showSaveResult(save()));
        this.validation.getStyleClass().add("validation-message");
        HBox box = new HBox(16, new Label("Cleared total:"), this.clearedTotal, new Label("Difference:"), this.difference, save, this.validation);
        updateSummary();
        return box;
    }

    private VBox importQueue()
    {
        Label heading = new Label("Statement import review queue");
        Label desc = new Label("Supported statement import formats discovered in existing services: OFX, QFX, and QIF. Imports are routed through Import/Export or this banking review queue before any posting to the ledger.");
        desc.setWrapText(true);
        this.importReview.setEditable(false);
        this.importReview.setPrefRowCount(4);
        this.importReview.setText("No statement file selected. Review parsed transactions here before posting; direct import-to-ledger is intentionally not exposed in this native panel.");
        Button route = new Button("Open Import/Export Workspace");
        route.setOnAction(e -> this.importReview.setText("Use Import/Export for file selection and validation; return here to reconcile after reviewed transactions are posted."));
        return new VBox(6, heading, desc, route, this.importReview);
    }

    private void refreshAccounts()
    {
        this.accountBox.getItems().setAll(this.service.listAccounts());
        this.accountBox.getSelectionModel().selectFirst();
        loadRows();
    }

    private void loadRows()
    {
        this.rows.clear();
        String account = this.accountBox.getValue();
        if (account != null) this.rows.addAll(this.service.loadRows(account));
        this.rows.forEach(row -> row.clearedProperty().addListener((obs, old, val) -> updateSummary()));
        updateSummary();
    }

    private BigDecimal parsedBeginning()
    {
        try { return new BigDecimal(this.beginningBalance.getText().trim()); }
        catch (Exception ex) { return BigDecimal.ZERO; }
    }

    private void updateSummary()
    {
        AlternateReconciliationService.BalanceParseResult parsed = this.service.parseEndingBalance(this.endingBalance.getText());
        BigDecimal ending = parsed.valid() ? parsed.balance() : BigDecimal.ZERO;
        AlternateReconciliationService.ReconciliationSummary summary = this.service.summarize(parsedBeginning(), ending, this.rows);
        this.clearedTotal.setText(FormatUtils.formatCurrency(summary.clearedTotal()));
        this.difference.setText(FormatUtils.formatCurrency(summary.difference()));
        this.validation.setText(parsed.valid() ? "" : parsed.message());
    }

    @Override
    public SaveResult save()
    {
        SaveResult result = this.service.save(this.accountBox.getValue(), this.statementDate.getValue(), parsedBeginning(), this.endingBalance.getText(), this.rows);
        if (result.status() == SaveResult.Status.SAVED) loadRows();
        return result;
    }

    private void showSaveResult(SaveResult result)
    {
        this.validation.setText(result.message());
    }
}
