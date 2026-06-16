package nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.Grant;
import nonprofitbookkeeping.service.GrantRecordService;
import org.nonprofitbookkeeping.ui.AppPanel;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Grant records panel backed by {@link GrantRecordService}.
 */
public class GrantRecordsPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    private final TableView<GrantRow> table = new TableView<>();
    private final Label status = new Label("Ready");
    private final GrantRecordService service;

    public GrantRecordsPanel()
    {
        this(new GrantRecordService());
    }

    public GrantRecordsPanel(GrantRecordService service)
    {
        this.service = service;
        Label title = new Label("Grant Records");
        title.getStyleClass().add("journal-entry-heading");

        Button add = new Button("+ Add Grant");
        Button refresh = new Button("Refresh");
        Button delete = new Button("Delete Selected");
        Button save = new Button("Save");
        HBox actions = new HBox(8, add, refresh, delete, save);

        this.root.setTop(new VBox(6, title, actions, new Separator()));
        configureTable();
        this.root.setCenter(this.table);
        this.root.setBottom(new VBox(new Separator(), this.status));

        add.setOnAction(e -> this.table.getItems().add(GrantRow.empty()));
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
            col("Grant ID", GrantRow::grantIdProperty, GrantRow::setGrantId),
            col("Grantor", GrantRow::grantorProperty, GrantRow::setGrantor),
            col("Amount", GrantRow::amountProperty, GrantRow::setAmount),
            col("Date Awarded", GrantRow::dateAwardedProperty, GrantRow::setDateAwarded),
            col("Purpose", GrantRow::purposeProperty, GrantRow::setPurpose),
            col("Status", GrantRow::statusProperty, GrantRow::setStatus)
        );
    }

    private TableColumn<GrantRow, String> col(String name,
        java.util.function.Function<GrantRow, SimpleStringProperty> getter,
        java.util.function.BiConsumer<GrantRow, String> setter)
    {
        TableColumn<GrantRow, String> col = new TableColumn<>(name);
        col.setCellValueFactory(v -> getter.apply(v.getValue()));
        col.setCellFactory(c -> new FocusCommitTextFieldTableCell<>());
        col.setOnEditCommit(event -> setter.accept(event.getRowValue(), event.getNewValue()));
        return col;
    }

    private void load()
    {
        try
        {
            List<GrantRow> rows = this.service.listAll().stream().map(GrantRow::fromGrant).toList();
            this.table.setItems(FXCollections.observableArrayList(rows));
            this.status.setText("Loaded " + rows.size() + " grant row(s).");
        }
        catch (SQLException ex)
        {
            this.table.getItems().clear();
            this.status.setText("Failed to load grants: " + ex.getMessage());
        }
    }

    @Override
    public void onSave()
    {
        try
        {
            List<Grant> records = new ArrayList<>();
            for (GrantRow row : this.table.getItems())
            {
                records.add(row.toGrant());
            }
            this.service.saveAll(records);
            this.status.setText("Saved " + records.size() + " grant row(s).");
        }
        catch (SQLException | RuntimeException ex)
        {
            this.status.setText("Failed to save grants: " + ex.getMessage());
        }
    }

    @Override
    public void onDelete()
    {
        onDeleteSelected();
    }

    @Override
    public void onCancel()
    {
        load();
    }

    private void onDeleteSelected()
    {
        GrantRow selected = this.table.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            this.status.setText("Select a grant row to delete.");
            return;
        }
        try
        {
            boolean deleted = this.service.deleteByGrantId(selected.grantIdProperty().get());
            this.table.getItems().remove(selected);
            this.status.setText(deleted ? "Deleted grant " + selected.grantIdProperty().get() : "Removed unsaved row.");
        }
        catch (SQLException ex)
        {
            this.status.setText("Failed to delete grant: " + ex.getMessage());
        }
    }

    @Override
    public String title()
    {
        return "Grant Records";
    }

    @Override
    public Node root()
    {
        return this.root;
    }

    static final class GrantRow
    {
        private final SimpleStringProperty grantId;
        private final SimpleStringProperty grantor;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty dateAwarded;
        private final SimpleStringProperty purpose;
        private final SimpleStringProperty status;

        private GrantRow(String grantId, String grantor, String amount,
            String dateAwarded, String purpose, String status)
        {
            this.grantId = new SimpleStringProperty(grantId);
            this.grantor = new SimpleStringProperty(grantor);
            this.amount = new SimpleStringProperty(amount);
            this.dateAwarded = new SimpleStringProperty(dateAwarded);
            this.purpose = new SimpleStringProperty(purpose);
            this.status = new SimpleStringProperty(status);
        }

        static GrantRow empty()
        {
            return new GrantRow("grant-" + UUID.randomUUID(), "", "0.00", "", "", "DRAFT");
        }

        static GrantRow fromGrant(Grant grant)
        {
            return new GrantRow(
                grant.getGrantId(),
                grant.getGrantor(),
                grant.getAmount() == null ? "0.00" : grant.getAmount().toPlainString(),
                grant.getDateAwarded(),
                grant.getPurpose(),
                grant.getStatus());
        }

        Grant toGrant()
        {
            Grant grant = new Grant();
            grant.setGrantId(this.grantId.get());
            grant.setGrantor(this.grantor.get());
            grant.setAmount(new BigDecimal(this.amount.get() == null || this.amount.get().isBlank() ? "0" : this.amount.get()));
            grant.setDateAwarded(this.dateAwarded.get());
            grant.setPurpose(this.purpose.get());
            grant.setStatus(this.status.get());
            return grant;
        }

        SimpleStringProperty grantIdProperty(){ return this.grantId; }
        SimpleStringProperty grantorProperty(){ return this.grantor; }
        SimpleStringProperty amountProperty(){ return this.amount; }
        SimpleStringProperty dateAwardedProperty(){ return this.dateAwarded; }
        SimpleStringProperty purposeProperty(){ return this.purpose; }
        SimpleStringProperty statusProperty(){ return this.status; }

        void setGrantId(String value){ this.grantId.set(value); }
        void setGrantor(String value){ this.grantor.set(value); }
        void setAmount(String value){ this.amount.set(value); }
        void setDateAwarded(String value){ this.dateAwarded.set(value); }
        void setPurpose(String value){ this.purpose.set(value); }
        void setStatus(String value){ this.status.set(value); }
    }
}
