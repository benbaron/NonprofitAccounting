package org.nonprofitbookkeeping.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;

import org.nonprofitbookkeeping.model.Account;

import java.util.List;

/**
 * Represents the ChartOfAccountsPanel component in the nonprofit bookkeeping application.
 */
public class ChartOfAccountsPanel implements AppPanel
{
    private final AlternatePanelScaffold root = new AlternatePanelScaffold("Chart of Accounts");
    private final TableView<Account> table = new TableView<>();
    private final Label status = new Label();
    private Button refresh;

    public ChartOfAccountsPanel()
    {
        this.root.setSubtitle("Review active posting accounts for the current company context.");

        Button add = new Button("+ Add");
        add.setOnAction(e -> onNew());

        this.refresh = new Button("Refresh");
        this.refresh.setOnAction(e -> reload());

        this.root.setPrimaryActions(List.of(add));
        this.root.setSecondaryActions(List.of(this.refresh));
        this.root.setFooter(this.status);

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
        this.root.setContent(this.table);

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
        this.root.showLoading("Loading accounts...");
        this.status.setText("Loading accounts...");

        UiAsync.run("coa-load",
            () -> UiServiceRegistry.accountLookup().listActivePostingAccounts(),
            rows -> {
            this.table.getItems().setAll(rows);
            if (rows.isEmpty())
            {
                this.root.showEmpty("No service-backed accounts are available for the active context.");
            }
            else
            {
                this.root.showContent();
            }
            this.status.setText("Loaded " + rows.size() + " posting account(s).");
            this.refresh.setDisable(false);
            },
            ex -> {
            String message = "Failed to load accounts: " + UiErrors.safeMessage(ex);
            this.root.showError(message);
            this.status.setText(message);
            this.refresh.setDisable(false);
            });
    }
}
