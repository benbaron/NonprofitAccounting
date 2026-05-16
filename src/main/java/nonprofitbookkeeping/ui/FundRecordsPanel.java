package nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.records.FundRecord;
import nonprofitbookkeeping.service.FundRecordService;
import org.nonprofitbookkeeping.ui.AppPanel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Fund records panel backed by {@link FundRecordService}.
 */
public class FundRecordsPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    private final TableView<FundRow> table = new TableView<>();
    private final Label status = new Label("Ready");
    private final FundRecordService service;

    public FundRecordsPanel()
    {
        this(new FundRecordService());
    }

    public FundRecordsPanel(FundRecordService service)
    {
        this.service = service;

        Label title = new Label("Fund Records");
        title.getStyleClass().add("journal-entry-heading");
        Button add = new Button("+ Add Fund");
        Button refresh = new Button("Refresh");
        Button delete = new Button("Delete Selected");
        Button save = new Button("Save");

        this.root.setTop(new VBox(6, title, new HBox(8, add, refresh, delete, save), new Separator()));
        configureTable();
        this.root.setCenter(this.table);
        this.root.setBottom(new VBox(new Separator(), this.status));

        add.setOnAction(e -> this.table.getItems().add(FundRow.empty()));
        refresh.setOnAction(e -> load());
        save.setOnAction(e -> onSave());
        delete.setOnAction(e -> onDeleteSelected());

        load();
    }

    private void configureTable()
    {
        this.table.setEditable(true);
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        this.table.getColumns().setAll(
            col("Fund ID", FundRow::fundIdProperty, FundRow::setFundId),
            col("Name", FundRow::nameProperty, FundRow::setName),
            restrictedCol("Restricted"),
            col("Description", FundRow::descriptionProperty, FundRow::setDescription)
        );
    }

    private TableColumn<FundRow, String> restrictedCol(String name)
    {
        TableColumn<FundRow, String> col = new TableColumn<>(name);
        col.setCellValueFactory(v -> v.getValue().restrictedProperty());
        col.setCellFactory(column -> {
            ComboBoxTableCell<FundRow, String> cell = new ComboBoxTableCell<>();
            cell.getItems().setAll("true", "false");
            return cell;
        });
        col.setOnEditCommit(event -> event.getRowValue().setRestricted(event.getNewValue()));
        return col;
    }

    private TableColumn<FundRow, String> col(String name,
        java.util.function.Function<FundRow, SimpleStringProperty> getter,
        java.util.function.BiConsumer<FundRow, String> setter)
    {
        TableColumn<FundRow, String> col = new TableColumn<>(name);
        col.setCellValueFactory(v -> getter.apply(v.getValue()));
        col.setCellFactory(c -> new FocusCommitTextFieldTableCell<>());
        col.setOnEditCommit(event -> setter.accept(event.getRowValue(), event.getNewValue()));
        return col;
    }

    private void load()
    {
        try
        {
            List<FundRow> rows = this.service.listAll().stream().map(FundRow::fromRecord).toList();
            this.table.setItems(FXCollections.observableArrayList(rows));
            this.status.setText("Loaded " + rows.size() + " fund row(s).");
        }
        catch (SQLException ex)
        {
            this.table.getItems().clear();
            this.status.setText("Failed to load funds: " + ex.getMessage());
        }
    }

    @Override
    public void onSave()
    {
        try
        {
            List<FundRecord> records = new ArrayList<>();
            for (FundRow row : this.table.getItems())
            {
                records.add(row.toRecord());
            }
            for (FundRecord record : records)
            {
                this.service.save(record);
            }
            this.status.setText("Saved " + records.size() + " fund row(s).");
        }
        catch (SQLException | RuntimeException ex)
        {
            this.status.setText("Failed to save funds: " + ex.getMessage());
        }
    }

    private void onDeleteSelected()
    {
        FundRow selected = this.table.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            this.status.setText("Select a fund row to delete.");
            return;
        }
        try
        {
            int deleted = this.service.delete(selected.fundIdProperty().get());
            this.table.getItems().remove(selected);
            this.status.setText(deleted > 0 ? "Deleted fund " + selected.fundIdProperty().get() : "Removed unsaved row.");
        }
        catch (SQLException ex)
        {
            this.status.setText("Failed to delete fund: " + ex.getMessage());
        }
    }

    @Override
    public String title()
    {
        return "Fund Records";
    }

    @Override
    public Node root()
    {
        return this.root;
    }

    static final class FundRow
    {
        private final SimpleStringProperty fundId;
        private final SimpleStringProperty name;
        private final SimpleStringProperty restricted;
        private final SimpleStringProperty description;

        FundRow(String fundId, String name, String restricted, String description)
        {
            this.fundId = new SimpleStringProperty(fundId);
            this.name = new SimpleStringProperty(name);
            this.restricted = new SimpleStringProperty(restricted);
            this.description = new SimpleStringProperty(description);
        }

        static FundRow empty()
        {
            return new FundRow("fund-" + UUID.randomUUID(), "", "false", "");
        }

        static FundRow fromRecord(FundRecord record)
        {
            return new FundRow(record.fundId(), record.name(), Boolean.toString(record.restricted()), record.description());
        }

        FundRecord toRecord()
        {
            return new FundRecord(
                this.fundId.get(),
                this.name.get() == null ? "" : this.name.get(),
                Boolean.parseBoolean(this.restricted.get()),
                this.description.get(),
                Map.of());
        }

        SimpleStringProperty fundIdProperty(){ return this.fundId; }
        SimpleStringProperty nameProperty(){ return this.name; }
        SimpleStringProperty restrictedProperty(){ return this.restricted; }
        SimpleStringProperty descriptionProperty(){ return this.description; }
        void setFundId(String value){ this.fundId.set(value); }
        void setName(String value){ this.name.set(value); }
        void setRestricted(String value){ this.restricted.set(value); }
        void setDescription(String value){ this.description.set(value); }
    }
}
