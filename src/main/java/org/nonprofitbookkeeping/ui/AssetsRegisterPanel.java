package org.nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.ui.FocusCommitTextFieldTableCell;
import nonprofitbookkeeping.model.records.AssetItemType;
import nonprofitbookkeeping.service.AssetRecordService;
import nonprofitbookkeeping.service.AssetRecordService.AssetRegisterRow;
import nonprofitbookkeeping.service.AssetRecordService.AssetRegisterSaveRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Service-backed native alternate fixed asset register panel. */
public class AssetsRegisterPanel implements AppPanel, AppPanel.SaveAware
{
    static final String NO_SERVICE_DATA_MESSAGE = "Open a company database to load fixed assets.";
    static final String EMPTY_MESSAGE = NO_SERVICE_DATA_MESSAGE;
    private static final Logger LOG = LoggerFactory.getLogger(AssetsRegisterPanel.class);

    private final BorderPane root = new BorderPane();
    private final TableView<AssetRow> table = new TableView<>();
    private final Label status = new Label(EMPTY_MESSAGE);
    private final AssetRecordService assetRecordService;
    private final String panelTitle;

    public AssetsRegisterPanel()
    {
        this("Asset Register", new AssetRecordService());
    }

    public AssetsRegisterPanel(AssetRecordService assetRecordService)
    {
        this("Asset Register", assetRecordService);
    }

    public AssetsRegisterPanel(String panelTitle)
    {
        this(panelTitle, new AssetRecordService());
    }

    public AssetsRegisterPanel(String panelTitle, AssetRecordService assetRecordService)
    {
        this.panelTitle = panelTitle == null || panelTitle.isBlank() ? "Asset Register" : panelTitle;
        this.assetRecordService = Objects.requireNonNull(assetRecordService, "assetRecordService");
        this.root.setPadding(new Insets(8));
        Label title = new Label(this.panelTitle);
        title.getStyleClass().add("panel-title");
        Button add = new Button("+ Add Asset");
        Button save = new Button("Save");
        Button refresh = new Button("Refresh");
        Button deactivate = new Button("Deactivate Selected");
        Button dispose = new Button("Dispose Selected");
        HBox actions = new HBox(8, add, save, refresh, deactivate, dispose);
        this.root.setTop(new VBox(6, title, actions, new Separator()));
        configureTable();
        this.root.setCenter(this.table);
        this.root.setBottom(new VBox(new Separator(), this.status));

        add.setOnAction(e -> {
            if (!Database.isInitialized())
            {
                this.status.setText(EMPTY_MESSAGE);
                return;
            }
            this.table.getItems().add(AssetRow.newRow());
        });
        save.setOnAction(e -> onSave());
        refresh.setOnAction(e -> loadAssets());
        deactivate.setOnAction(e -> deactivateSelected());
        dispose.setOnAction(e -> disposeSelected());
        loadAssets();
    }

    private void configureTable()
    {
        this.table.setEditable(true);
        this.table.setPlaceholder(new Label(EMPTY_MESSAGE));
        this.table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        this.table.getColumns().setAll(
            textCol("Asset ID", AssetRow::assetIdProperty, AssetRow::setAssetId),
            textCol("Acquired (YYYY-MM-DD)", AssetRow::dateAcquiredProperty, AssetRow::setDateAcquired),
            textCol("Description", AssetRow::descriptionProperty, AssetRow::setDescription),
            textCol("Count", AssetRow::itemCountProperty, AssetRow::setItemCount),
            textCol("Cost (plain number)", AssetRow::costProperty, AssetRow::setCost),
            textCol("Accum. Depreciation", AssetRow::accumulatedDepreciationProperty, AssetRow::setAccumulatedDepreciation),
            textCol("Method", AssetRow::depreciationMethodProperty, AssetRow::setDepreciationMethod),
            textCol("Useful Life Months", AssetRow::usefulLifeMonthsProperty, AssetRow::setUsefulLifeMonths),
            itemTypeCol(),
            readonlyCol("State", AssetRow::assetStateProperty),
            readonlyCol("Net Book Value", AssetRow::netBookValueProperty),
            readonlyCol("Depreciation Runs", AssetRow::depreciationRunLinksProperty));
    }

    private TableColumn<AssetRow, String> textCol(String name,
        java.util.function.Function<AssetRow, SimpleStringProperty> getter,
        java.util.function.BiConsumer<AssetRow, String> setter)
    {
        TableColumn<AssetRow, String> col = readonlyCol(name, getter);
        col.setCellFactory(c -> new FocusCommitTextFieldTableCell<>());
        col.setOnEditCommit(e -> setter.accept(e.getRowValue(), e.getNewValue()));
        return col;
    }

    private TableColumn<AssetRow, String> readonlyCol(String name,
        java.util.function.Function<AssetRow, SimpleStringProperty> getter)
    {
        TableColumn<AssetRow, String> col = new TableColumn<>(name);
        col.setPrefWidth(name.length() > 16 ? 190 : 140);
        col.setCellValueFactory(v -> getter.apply(v.getValue()));
        return col;
    }

    private TableColumn<AssetRow, AssetItemType> itemTypeCol()
    {
        TableColumn<AssetRow, AssetItemType> col = new TableColumn<>("Item Type");
        col.setPrefWidth(170);
        col.setCellValueFactory(v -> v.getValue().itemTypeProperty());
        col.setCellFactory(ComboBoxTableCell.forTableColumn(AssetItemType.values()));
        col.setOnEditCommit(e -> e.getRowValue().setItemType(e.getNewValue()));
        return col;
    }

    private void loadAssets()
    {
        if (!Database.isInitialized())
        {
            this.table.getItems().clear();
            this.status.setText(EMPTY_MESSAGE);
            return;
        }
        try
        {
            this.table.setItems(FXCollections.observableArrayList(
                this.assetRecordService.listRegisterRows().stream().map(AssetRow::fromRegisterRow).toList()));
            this.status.setText("Loaded " + this.table.getItems().size() + " fixed asset(s). Inventory document items remain a separate legacy model.");
        }
        catch (SQLException | RuntimeException ex)
        {
            LOG.warn("Could not load assets", ex);
            this.status.setText("Could not load assets: " + ex.getMessage());
        }
    }

    @Override public String title() { return this.panelTitle; }
    @Override public Node root() { return this.root; }

    @Override
    public void onSave()
    {
        saveAssets();
    }

    private SaveResult saveAssets()
    {
        try
        {
            for (int i = 0; i < this.table.getItems().size(); i++)
            {
                this.assetRecordService.saveRegisterRow(this.table.getItems().get(i).toRequest(i + 1));
            }
            loadAssets();
            this.status.setText("Saved asset register.");
            return SaveResult.saved("Asset register saved.");
        }
        catch (SQLException | RuntimeException ex)
        {
            LOG.warn("Could not save assets", ex);
            String message = Database.isInitialized() ? "Could not save assets: " + ex.getMessage() : EMPTY_MESSAGE;
            this.status.setText(message);
            return SaveResult.failed(message, ex);
        }
    }

    @Override public SaveResult save() { return saveAssets(); }
    @Override public void onDelete() { deactivateSelected(); }
    @Override public void onCancel() { loadAssets(); }

    private void deactivateSelected()
    {
        AssetRow row = selectedRow();
        if (row == null) return;
        try { this.assetRecordService.deactivate(row.getAssetId()); loadAssets(); }
        catch (SQLException | RuntimeException ex) { this.status.setText("Could not deactivate asset: " + ex.getMessage()); }
    }

    private void disposeSelected()
    {
        AssetRow row = selectedRow();
        if (row == null) return;
        DatePicker picker = new DatePicker(LocalDate.now());
        javafx.scene.control.Dialog<LocalDate> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Dispose Asset");
        dialog.getDialogPane().setContent(new VBox(6, new Label("Disposal date"), picker));
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
        dialog.setResultConverter(button -> button == javafx.scene.control.ButtonType.OK ? picker.getValue() : null);
        LocalDate disposalDate = dialog.showAndWait().orElse(null);
        if (disposalDate == null) return;
        try { this.assetRecordService.dispose(row.getAssetId(), disposalDate); loadAssets(); }
        catch (SQLException | RuntimeException ex) { this.status.setText("Could not dispose asset: " + ex.getMessage()); }
    }

    private AssetRow selectedRow()
    {
        AssetRow row = this.table.getSelectionModel().getSelectedItem();
        if (row == null) this.status.setText("Select an asset first.");
        return row;
    }

    public static final class AssetRow
    {
        private final SimpleStringProperty assetId = new SimpleStringProperty("");
        private final SimpleStringProperty dateAcquired = new SimpleStringProperty("");
        private final SimpleStringProperty description = new SimpleStringProperty("");
        private final SimpleStringProperty itemCount = new SimpleStringProperty("");
        private final SimpleStringProperty cost = new SimpleStringProperty("");
        private final SimpleStringProperty accumulatedDepreciation = new SimpleStringProperty("0.00");
        private final SimpleStringProperty depreciationMethod = new SimpleStringProperty(AssetRecordService.STRAIGHT_LINE);
        private final SimpleStringProperty usefulLifeMonths = new SimpleStringProperty("60");
        private final SimpleObjectProperty<AssetItemType> itemType = new SimpleObjectProperty<>();
        private final SimpleStringProperty assetState = new SimpleStringProperty("ACTIVE");
        private final SimpleStringProperty netBookValue = new SimpleStringProperty("0.00");
        private final SimpleStringProperty depreciationRunLinks = new SimpleStringProperty("");

        static AssetRow newRow()
        {
            AssetRow row = new AssetRow();
            row.setAssetId("asset-" + UUID.randomUUID());
            return row;
        }

        static AssetRow fromRegisterRow(AssetRegisterRow source)
        {
            AssetRow row = new AssetRow();
            row.setAssetId(source.record().assetId());
            row.setDateAcquired(source.record().dateAcquired() == null ? "" : source.record().dateAcquired().toString());
            row.setDescription(source.record().description());
            row.setItemCount(source.record().itemCount() == null ? "" : source.record().itemCount().toString());
            row.setCost(source.record().approxValueTotal() == null ? "" : source.record().approxValueTotal().toPlainString());
            row.setAccumulatedDepreciation(source.accumulatedDepreciation() == null ? "0.00" : source.accumulatedDepreciation().toPlainString());
            row.setDepreciationMethod(source.depreciationMethod());
            row.setUsefulLifeMonths(source.usefulLifeMonths() == null ? "" : source.usefulLifeMonths().toString());
            row.setItemType(source.record().itemType());
            row.assetState.set(source.assetState() == null ? "" : source.assetState());
            row.netBookValue.set(source.netBookValue() == null ? "0.00" : source.netBookValue().toPlainString());
            row.depreciationRunLinks.set(String.join(", ", source.depreciationRunLinks()));
            return row;
        }

        AssetRegisterSaveRequest toRequest(int rowNumber)
        {
            return new AssetRegisterSaveRequest(trim(this.assetId.get()), parseDate(this.dateAcquired.get(), rowNumber), trim(this.description.get()),
                parseInteger(this.itemCount.get(), rowNumber), AssetRecordService.parseMoneyInput(this.cost.get(), "Row " + rowNumber + " cost"),
                AssetRecordService.parseMoneyInput(this.accumulatedDepreciation.get(), "Row " + rowNumber + " accumulated depreciation"),
                this.itemType.get(), trim(this.depreciationMethod.get()), parseInteger(this.usefulLifeMonths.get(), rowNumber));
        }

        private static LocalDate parseDate(String raw, int rowNumber) { return raw == null || raw.isBlank() ? null : LocalDate.parse(raw.trim()); }
        private static Integer parseInteger(String raw, int rowNumber) { return raw == null || raw.isBlank() ? null : Integer.parseInt(raw.trim()); }
        private static String trim(String raw) { return raw == null || raw.isBlank() ? null : raw.trim(); }

        public String getAssetId() { return this.assetId.get(); }
        public SimpleStringProperty assetIdProperty() { return this.assetId; }
        public SimpleStringProperty dateAcquiredProperty() { return this.dateAcquired; }
        public SimpleStringProperty descriptionProperty() { return this.description; }
        public SimpleStringProperty itemCountProperty() { return this.itemCount; }
        public SimpleStringProperty costProperty() { return this.cost; }
        public SimpleStringProperty accumulatedDepreciationProperty() { return this.accumulatedDepreciation; }
        public SimpleStringProperty depreciationMethodProperty() { return this.depreciationMethod; }
        public SimpleStringProperty usefulLifeMonthsProperty() { return this.usefulLifeMonths; }
        public SimpleObjectProperty<AssetItemType> itemTypeProperty() { return this.itemType; }
        public SimpleStringProperty assetStateProperty() { return this.assetState; }
        public SimpleStringProperty netBookValueProperty() { return this.netBookValue; }
        public SimpleStringProperty depreciationRunLinksProperty() { return this.depreciationRunLinks; }
        public void setAssetId(String v) { this.assetId.set(v == null ? "" : v); }
        public void setDateAcquired(String v) { this.dateAcquired.set(v == null ? "" : v); }
        public void setDescription(String v) { this.description.set(v == null ? "" : v); }
        public void setItemCount(String v) { this.itemCount.set(v == null ? "" : v); }
        public void setCost(String v) { this.cost.set(v == null ? "" : v); }
        public void setAccumulatedDepreciation(String v) { this.accumulatedDepreciation.set(v == null ? "" : v); }
        public void setDepreciationMethod(String v) { this.depreciationMethod.set(v == null || v.isBlank() ? AssetRecordService.STRAIGHT_LINE : v); }
        public void setUsefulLifeMonths(String v) { this.usefulLifeMonths.set(v == null ? "" : v); }
        public void setItemType(AssetItemType v) { this.itemType.set(v); }
    }
}
