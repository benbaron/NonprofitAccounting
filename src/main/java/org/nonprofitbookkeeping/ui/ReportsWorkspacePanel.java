package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.reports.ReportGenerationRequest;
import nonprofitbookkeeping.model.reports.ReportFormat;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.reports.ReportMetadata;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Native alternate reports workspace with catalog, parameters, generation history, and export/open actions. */
public class ReportsWorkspacePanel implements AppPanel
{
    private final AlternateReportsWorkspaceService service;
    private final AlternatePanelScaffold scaffold = new AlternatePanelScaffold("Reports Workspace");
    private final ListView<ReportCatalogItem> catalog = new ListView<>();
    private final DatePicker startDate = new DatePicker(LocalDate.now().withDayOfYear(1));
    private final DatePicker endDate = new DatePicker(LocalDate.now());
    private final TextField fund = new TextField();
    private final TextField account = new TextField();
    private final TextField donor = new TextField();
    private final ComboBox<ReportFormat> format = new ComboBox<>(FXCollections.observableArrayList(ReportFormat.values()));
    private final CheckBox optionA = new CheckBox();
    private final CheckBox optionB = new CheckBox();
    private final TableView<HistoryRow> history = new TableView<>();

    public ReportsWorkspacePanel()
    {
        this(new AlternateReportsWorkspaceService());
    }

    public ReportsWorkspacePanel(AlternateReportsWorkspaceService service)
    {
        this.service = service;
        scaffold.setSubtitle("Generate financial and semantic workbook reports. Reports are written to ~/NonprofitBookkeepingReports using <report>_<as-of-date>.<format> naming.");
        Button generate = new Button("Generate");
        generate.setDefaultButton(true);
        generate.setOnAction(e -> generateSelected());
        Button refresh = new Button("Refresh History");
        refresh.setOnAction(e -> refreshHistory());
        scaffold.setPrimaryActions(List.of(generate, refresh));
        scaffold.setContent(buildContent());
        format.getSelectionModel().select(ReportFormat.TEXT);
        refreshCatalog();
        refreshHistory();
    }

    @Override public String title() { return "Reports"; }
    @Override public Node root() { return scaffold; }

    private Node buildContent()
    {
        catalog.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(ReportCatalogItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.displayName() + " — " + item.kind());
            }
        });
        catalog.getSelectionModel().selectedItemProperty().addListener((obs, old, item) -> updateParameterState(item));
        GridPane params = new GridPane();
        params.setHgap(8); params.setVgap(8);
        params.addRow(0, new Label("Start date"), startDate, new Label("End date"), endDate);
        params.addRow(1, new Label("Fund"), fund, new Label("Account"), account);
        params.addRow(2, new Label("Donor"), donor, new Label("Output"), format);
        params.addRow(3, new Label("Options"), new VBox(4, optionA, optionB));
        format.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(ReportFormat item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label() + (item.supported() ? "" : " — unsupported: " + item.explanation()));
                setDisable(item != null && !item.supported());
            }
        });
        format.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(ReportFormat item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });
        buildHistoryTable();
        SplitPane split = new SplitPane(new VBox(8, new Label("Report Catalog"), catalog), new VBox(8, new Label("Parameters"), params, new Label("Generated Report History"), history));
        split.setDividerPositions(0.35);
        return split;
    }

    private void refreshCatalog()
    {
        catalog.setItems(FXCollections.observableArrayList(service.catalog()));
        if (!catalog.getItems().isEmpty()) catalog.getSelectionModel().selectFirst();
    }

    private void updateParameterState(ReportCatalogItem item)
    {
        boolean none = item == null;
        fund.setDisable(none || !item.fundSupported());
        account.setDisable(none || !item.accountSupported());
        donor.setDisable(none || !item.donorSupported());
        List<String> options = none ? List.of() : item.optionNames();
        configureOption(optionA, options, 0);
        configureOption(optionB, options, 1);
        scaffold.setStatus(item == null ? "Select a report." : "Default export name: " + service.exportNamingConvention(item, format.getValue(), endDate.getValue()));
    }

    private static void configureOption(CheckBox box, List<String> options, int index)
    {
        boolean available = index < options.size();
        box.setText(available ? options.get(index) : "No additional option");
        box.setSelected(false);
        box.setDisable(!available);
        box.setVisible(available);
        box.setManaged(available);
    }

    private void generateSelected()
    {
        ReportCatalogItem item = catalog.getSelectionModel().getSelectedItem();
        if (item == null) { scaffold.showError("Select a report before generating."); return; }
        try {
            service.generate(buildRequest(item));
            scaffold.showContent();
            scaffold.setStatus("Generated report in ~/NonprofitBookkeepingReports. Use Open or Export in history.");
            refreshHistory();
        } catch (Exception ex) {
            scaffold.showError(UiErrors.safeMessage(ex));
        }
    }

    ReportGenerationRequest buildRequest(ReportCatalogItem item)
    {
        Map<String, String> options = new LinkedHashMap<>();
        if (optionA.isVisible()) options.put(optionA.getText(), Boolean.toString(optionA.isSelected()));
        if (optionB.isVisible()) options.put(optionB.getText(), Boolean.toString(optionB.isSelected()));
        return new ReportGenerationRequest(item.id(), item.kind(), item.templateId(), startDate.getValue(), endDate.getValue(),
            value(fund), value(account), value(donor), format.getValue(), options);
    }

    private static String value(TextField field) { return field.isDisabled() || field.getText().isBlank() ? null : field.getText().trim(); }

    @SuppressWarnings({"unchecked", "deprecation"})
    private void buildHistoryTable()
    {
        TableColumn<HistoryRow, String> name = col("Report", "name");
        TableColumn<HistoryRow, String> created = col("Created", "created");
        TableColumn<HistoryRow, String> path = col("Location", "path");
        TableColumn<HistoryRow, Void> actions = new TableColumn<>("Actions");
        actions.setCellFactory(tc -> new TableCell<>() {
            private final Button open = new Button("Open");
            private final Button export = new Button("Export");
            private final HBox buttons = new HBox(4, open, export);
            { open.setOnAction(e -> openFile(row().path)); export.setOnAction(e -> openFile(new File(row().path).getParent())); }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : buttons); }
            private HistoryRow row() { return getTableView().getItems().get(getIndex()); }
        });
        history.getColumns().setAll(name, created, path, actions);
        history.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private static TableColumn<HistoryRow, String> col(String title, String property)
    {
        TableColumn<HistoryRow, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private void refreshHistory()
    {
        history.setItems(FXCollections.observableArrayList(service.history().stream().map(HistoryRow::new).toList()));
    }

    private void openFile(String path)
    {
        try { Desktop.getDesktop().open(new File(path)); }
        catch (Exception ex) { scaffold.showError("Cannot open report location: " + UiErrors.safeMessage(ex)); }
    }

    public static class HistoryRow
    {
        private final String name; private final String created; private final String path;
        HistoryRow(ReportMetadata metadata) { name = metadata.getReportName(); created = metadata.getCreated(); path = metadata.getFilePath(); }
        public String getName() { return name; }
        public String getCreated() { return created; }
        public String getPath() { return path; }
    }
}
