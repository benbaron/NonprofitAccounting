package org.nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.nonprofitbookkeeping.service.EventAccountingService;
import org.nonprofitbookkeeping.service.EventAccountingService.EventAccountingWorkspace;
import org.nonprofitbookkeeping.service.EventAccountingService.EventChecklistItem;
import org.nonprofitbookkeeping.service.EventAccountingService.EventTransactionRow;

import java.text.NumberFormat;
import java.util.List;

/** Native alternate-style Event Accounting workspace backed by Activity-linked transactions. */
public class EventAccountingPanel implements AppPanel
{
    private final AlternatePanelScaffold root = new AlternatePanelScaffold("Event Accounting");
    private final EventAccountingService service;
    private final UiSessionContext sessionContext;
    private final TableView<EventAccountingWorkspace> events = new TableView<>();
    private final TableView<EventTransactionRow> transactions = new TableView<>();
    private final TableView<EventTransactionRow> deposits = new TableView<>();
    private final ListView<String> checklist = new ListView<>();
    private final Label income = new Label("—");
    private final Label expenses = new Label("—");
    private final Label net = new Label("—");
    private final Label linked = new Label("—");
    private final Label status = new Label();
    private final NumberFormat money = NumberFormat.getCurrencyInstance();

    public EventAccountingPanel()
    {
        this(new EventAccountingService(), null);
    }

    EventAccountingPanel(UiServiceProvider services)
    {
        this(services.sessionContext().isDatabaseOpen() ? new EventAccountingService() : null, services.sessionContext());
    }

    EventAccountingPanel(EventAccountingService service)
    {
        this(service, null);
    }

    private EventAccountingPanel(EventAccountingService service, UiSessionContext sessionContext)
    {
        this.service = service;
        this.sessionContext = sessionContext;
        root.setSubtitle("Review event/activity income, expenses, linked journal transactions, deposits/refunds, and closeout readiness from posted service data.");
        root.setWarningBanner("Read-only workspace: accounting postings are created only through posting services, not in this UI panel.");
        root.setSecondaryActions(List.of(refreshButton()));
        root.setFooter(status);
        buildEvents();
        buildTransactionTables();
        root.setContent(new HBox(12, events, new VBox(10, summaryGrid(), new Label("Linked Journal Transactions"), transactions,
            new Label("Deposits / Refunds"), deposits, new Label("Event Close Checklist"), checklist)));
        load();
    }

    @Override public String title() { return "Event Accounting"; }
    @Override public Node root() { return root; }

    private Button refreshButton()
    {
        Button refresh = new Button("Refresh");
        refresh.setOnAction(event -> load());
        return refresh;
    }

    private void buildEvents()
    {
        events.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<EventAccountingWorkspace, String> code = new TableColumn<>("Code");
        code.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().code()));
        TableColumn<EventAccountingWorkspace, String> name = new TableColumn<>("Event / Activity");
        name.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        TableColumn<EventAccountingWorkspace, String> active = new TableColumn<>("Status");
        active.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().active() ? "Active" : "Inactive"));
        events.getColumns().setAll(code, name, active);
        events.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> show(value));
    }

    private void buildTransactionTables()
    {
        configureTransactionTable(transactions);
        configureTransactionTable(deposits);
    }

    private void configureTransactionTable(TableView<EventTransactionRow> table)
    {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<EventTransactionRow, String> date = new TableColumn<>("Date");
        date.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().date())));
        TableColumn<EventTransactionRow, String> id = new TableColumn<>("Journal Txn");
        id.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().transactionId())));
        TableColumn<EventTransactionRow, String> memo = new TableColumn<>("Memo / Payee");
        memo.setCellValueFactory(c -> new SimpleStringProperty(displayMemo(c.getValue())));
        TableColumn<EventTransactionRow, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().accountType().name()));
        TableColumn<EventTransactionRow, String> amount = new TableColumn<>("Amount");
        amount.setCellValueFactory(c -> new SimpleStringProperty(money.format(c.getValue().amount())));
        table.getColumns().setAll(date, id, memo, type, amount);
    }

    private GridPane summaryGrid()
    {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(6);
        grid.addRow(0, new Label("Income"), income, new Label("Expenses"), expenses);
        grid.addRow(1, new Label("Net"), net, new Label("Linked rows"), linked);
        return grid;
    }

    private void load()
    {
        if (sessionContext != null && !sessionContext.isDatabaseOpen())
        {
            root.showEmpty("Open a database to load service-backed event/activity accounting workspaces.");
            status.setText("No database open.");
            show(null);
            return;
        }
        try
        {
            if (service == null)
            {
                root.showEmpty("Open a database to load service-backed event/activity accounting workspaces.");
                status.setText("No database open.");
                show(null);
                return;
            }
            List<EventAccountingWorkspace> rows = service.listWorkspaces();
            events.setItems(FXCollections.observableArrayList(rows));
            if (rows.isEmpty())
            {
                root.showEmpty("No event/activity records exist yet. Create activities in reference data and post transactions through accounting services.");
                status.setText("No events loaded.");
                show(null);
            }
            else
            {
                root.showContent();
                events.getSelectionModel().selectFirst();
                status.setText("Loaded " + rows.size() + " event/activity workspaces.");
            }
        }
        catch (RuntimeException ex)
        {
            root.showError("Event accounting data could not be loaded: " + ex.getMessage());
            status.setText("Load failed.");
        }
    }

    private void show(EventAccountingWorkspace workspace)
    {
        if (workspace == null)
        {
            income.setText("—"); expenses.setText("—"); net.setText("—"); linked.setText("—");
            transactions.getItems().clear(); deposits.getItems().clear(); checklist.getItems().clear();
            return;
        }
        income.setText(money.format(workspace.summary().income()));
        expenses.setText(money.format(workspace.summary().expenses()));
        net.setText(money.format(workspace.summary().net()));
        linked.setText(String.valueOf(workspace.summary().linkedTransactionCount()));
        transactions.setItems(FXCollections.observableArrayList(workspace.linkedTransactions()));
        deposits.setItems(FXCollections.observableArrayList(workspace.depositsAndRefunds()));
        checklist.setItems(FXCollections.observableArrayList(workspace.closeChecklist().stream().map(this::checklistText).toList()));
    }

    private String checklistText(EventChecklistItem item)
    {
        return (item.complete() ? "✓ " : "○ ") + item.label() + " — " + item.detail();
    }

    private static String displayMemo(EventTransactionRow row)
    {
        String payee = row.payee() == null || row.payee().isBlank() ? "" : row.payee() + " — ";
        return payee + (row.memo() == null ? "" : row.memo());
    }
}
