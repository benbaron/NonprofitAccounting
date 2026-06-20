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
    @Override public Node root() { return root; }

    private void build()
    {
        Label title = new Label("Reconciliation & Banking");
        title.getStyleClass().add("alternate-panel-title");
        Label subtitle = new Label("Review statement activity before posting transactions, then reconcile cleared ledger transactions without legacy panel adapters.");
        subtitle.setWrapText(true);
        subtitle.getStyleClass().add("alternate-panel-subtitle");
        root.setPadding(new Insets(12));
        root.getStyleClass().add("alternate-content-card");
        root.getChildren().setAll(title, subtitle, controls(), table(), totals(), new Separator(), importQueue());
    }

    private GridPane controls()
    {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        endingBalance.setPromptText("Statement ending balance");
        accountBox.setOnAction(e -> loadRows());
        beginningBalance.textProperty().addListener((obs, old, val) -> updateSummary());
        endingBalance.textProperty().addListener((obs, old, val) -> updateSummary());
        Button reload = new Button("Reload transactions");
        reload.setOnAction(e -> loadRows());
        grid.addRow(0, new Label("Account"), accountBox, reload);
        grid.addRow(1, new Label("Statement date"), statementDate);
        grid.addRow(2, new Label("Beginning balance"), beginningBalance);
        grid.addRow(3, new Label("Statement ending balance"), endingBalance);
        return grid;
    }

    private TableView<AlternateReconciliationService.ReconciliationRow> table()
    {
        TableView<AlternateReconciliationService.ReconciliationRow> table = new TableView<>(rows);
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
        validation.getStyleClass().add("validation-message");
        HBox box = new HBox(16, new Label("Cleared total:"), clearedTotal, new Label("Difference:"), difference, save, validation);
        updateSummary();
        return box;
    }

    private VBox importQueue()
    {
        Label heading = new Label("Statement import review queue");
        Label desc = new Label("Supported statement import formats discovered in existing services: OFX, QFX, and QIF. Imports are routed through Import/Export or this banking review queue before any posting to the ledger.");
        desc.setWrapText(true);
        importReview.setEditable(false);
        importReview.setPrefRowCount(4);
        importReview.setText("No statement file selected. Review parsed transactions here before posting; direct import-to-ledger is intentionally not exposed in this native panel.");
        Button route = new Button("Open Import/Export Workspace");
        route.setOnAction(e -> importReview.setText("Use Import/Export for file selection and validation; return here to reconcile after reviewed transactions are posted."));
        return new VBox(6, heading, desc, route, importReview);
    }

    private void refreshAccounts()
    {
        accountBox.getItems().setAll(service.listAccounts());
        accountBox.getSelectionModel().selectFirst();
        loadRows();
    }

    private void loadRows()
    {
        rows.clear();
        String account = accountBox.getValue();
        if (account != null) rows.addAll(service.loadRows(account));
        rows.forEach(row -> row.clearedProperty().addListener((obs, old, val) -> updateSummary()));
        updateSummary();
    }

    private BigDecimal parsedBeginning()
    {
        try { return new BigDecimal(beginningBalance.getText().trim()); }
        catch (Exception ex) { return BigDecimal.ZERO; }
    }

    private void updateSummary()
    {
        AlternateReconciliationService.BalanceParseResult parsed = service.parseEndingBalance(endingBalance.getText());
        BigDecimal ending = parsed.valid() ? parsed.balance() : BigDecimal.ZERO;
        AlternateReconciliationService.ReconciliationSummary summary = service.summarize(parsedBeginning(), ending, rows);
        clearedTotal.setText(FormatUtils.formatCurrency(summary.clearedTotal()));
        difference.setText(FormatUtils.formatCurrency(summary.difference()));
        validation.setText(parsed.valid() ? "" : parsed.message());
    }

    @Override
    public SaveResult save()
    {
        SaveResult result = service.save(accountBox.getValue(), statementDate.getValue(), parsedBeginning(), endingBalance.getText(), rows);
        if (result.status() == SaveResult.Status.SAVED) loadRows();
        return result;
    }

    private void showSaveResult(SaveResult result)
    {
        validation.setText(result.message());
    }
}
