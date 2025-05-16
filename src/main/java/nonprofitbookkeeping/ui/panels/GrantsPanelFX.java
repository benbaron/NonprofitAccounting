package nonprofitbookkeeping.ui.panels;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import nonprofitbookkeeping.core.FileBasedGrantsService;

/**
 * JavaFX port of {@code GrantsPanel}. Displays grant records in a table with a
 * Refresh button. Uses the same FileBasedGrantsService for data.
 */
public class GrantsPanelFX extends BorderPane {

    private final FileBasedGrantsService grantsService;
    private final ObservableList<GrantRow> rows = FXCollections.observableArrayList();
    private final TableView<GrantRow> table = new TableView<>();

    public GrantsPanelFX() {
        this.grantsService = new FileBasedGrantsService();
        setPadding(new Insets(10));
        buildTable();
        setCenter(new TitledPane("Grant List", this.table) {{ setCollapsible(false); }});
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> loadGrantData());
        setBottom(new ToolBar(refresh));
        loadGrantData();
    }

    /* ------------------------------------------------------------------ */
    @SuppressWarnings({ "unchecked", "deprecation" }) private void buildTable() {
        TableColumn<GrantRow,String> idCol = col("Grant ID", "grantId");
        TableColumn<GrantRow,String> grantorCol = col("Grantor", "grantor");
        TableColumn<GrantRow,String> amtCol = col("Amount", "amount");
        TableColumn<GrantRow,String> dateCol = col("Date Awarded", "dateAwarded");
        TableColumn<GrantRow,String> purpCol = col("Purpose", "purpose");
        TableColumn<GrantRow,String> statusCol = col("Status", "status");
        this.table.getColumns().addAll(idCol, grantorCol, amtCol, dateCol, purpCol, statusCol);
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.table.setItems(this.rows);
    }

    private static TableColumn<GrantRow,String> col(String title, String prop) {
        TableColumn<GrantRow,String> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        return c;
    }

    private void loadGrantData() {
        this.rows.clear();
        List<GrantsPanel.Grant> list = this.grantsService.getAllGrants();
        for(var g:list) this.rows.add(new GrantRow(g));
    }

    /* Row wrapper */
    public static class GrantRow {
        private final String grantId, grantor, amount, dateAwarded, purpose, status;
        GrantRow(GrantsPanel.Grant g) {
            this.grantId=g.getGrantId();
            this.grantor=g.getGrantor();
            this.amount=String.format("$%.2f", g.getAmount());
            this.dateAwarded=g.getDateAwarded();
            this.purpose=g.getPurpose();
            this.status=g.getStatus();
        }
        public String getGrantId() { return this.grantId; }
        public String getGrantor() { return this.grantor; }
        public String getAmount() { return this.amount; }
        public String getDateAwarded() { return this.dateAwarded; }
        public String getPurpose() { return this.purpose; }
        public String getStatus() { return this.status; }
    }

    /* Service interface aligned with original inner interface */
    public interface GrantsService {
        List<GrantsPanel.Grant> getAllGrants();
    }
}
