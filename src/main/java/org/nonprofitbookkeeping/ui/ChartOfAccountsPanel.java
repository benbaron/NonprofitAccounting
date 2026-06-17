package org.nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;

import org.nonprofitbookkeeping.model.Account;

import java.util.List;

/**
 * Represents the ChartOfAccountsPanel component in the nonprofit bookkeeping application.
 */
public class ChartOfAccountsPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    private final TableView<Account> table = new TableView<>();
    private final Label status = new Label();
    private Button refresh;

    public ChartOfAccountsPanel()
    {
        this.root.setPadding(new Insets(8));

        Label title = new Label("Chart of Accounts");
        title.getStyleClass().add("panel-title");

        Button add = new Button("+ Add");
        add.setOnAction(e -> onNew());

        this.refresh = new Button("Refresh");
        this.refresh.setOnAction(e -> reload());

        HBox actions = new HBox(8, add, this.refresh);
        VBox header = new VBox(6, title, actions, this.status, new Separator());

        this.root.setTop(header);

        TableColumn<Account, String> code = new TableColumn<>("Code");
        code.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getCode()));

        TableColumn<Account, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getName()));

        TableColumn<Account, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(v -> new SimpleStringProperty(String.valueOf(v.getValue().getAccountType())));

        TableColumn<Account, String> subtype = new TableColumn<>("Subtype");
        subtype.setCellValueFactory(v -> new SimpleStringProperty(String.valueOf(v.getValue().getSubtype())));

        this.table.getColumns().addAll(code, name, type, subtype);
        this.table.setPlaceholder(new Label("No service-backed accounts are available for the active context."));
        this.root.setCenter(this.table);

        reload();
    }

    @Override public String title() { return "Chart of Accounts"; }
    @Override public Node root() { return this.root; }

    @Override public void onNew() {
        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION,
            "Create account UI is not built yet. This panel is now connected to AccountLookupService for read-only listing.").showAndWait();
    }

    private void reload()
    {
        this.refresh.setDisable(true);
        this.status.setText("Loading accounts...");

        UiAsync.run("coa-load",
            () -> UiServiceRegistry.accountLookup().listActivePostingAccounts(),
            rows -> {
            this.table.getItems().setAll(rows);
            this.status.setText("Loaded " + rows.size() + " posting account(s).");
            this.refresh.setDisable(false);
            },
            ex -> {
            this.status.setText("Failed to load accounts: " + UiErrors.safeMessage(ex));
            this.refresh.setDisable(false);
            });
    }
}
