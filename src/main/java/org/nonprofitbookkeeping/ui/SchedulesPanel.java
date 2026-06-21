package org.nonprofitbookkeeping.ui;

import org.nonprofitbookkeeping.model.Account;
import org.nonprofitbookkeeping.service.AccountLookupService;
import org.nonprofitbookkeeping.service.ScheduleEligibilityService;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One schedules panel with tabs. Tabs are enabled/disabled based on the selected account's subtype
 * (plus any per-account overrides).
 *
 * This is an intentionally light UI skeleton; actual schedule grids will be added later.
 */
public class SchedulesPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    static final String NO_SERVICE_DATA_MESSAGE = "No service-backed data source is wired for this panel yet.";

    private final ComboBox<Account> accountSelect = new ComboBox<>();
    private final TabPane tabs = new TabPane();
    private final Label status = new Label();

    private final Map<String, Tab> tabIndex = new LinkedHashMap<>();

    private final UiServiceProvider services;
    private final ScheduleEligibilityService eligibility;

    public SchedulesPanel()
    {
        this(UiServiceRegistry.provider());
    }

    SchedulesPanel(UiServiceProvider services)
    {
        this.services = services;
        this.eligibility = services.sessionContext().isDatabaseOpen() ? services.scheduleEligibility() : null;

        Label title = new Label("Schedules / Outstanding Details");
        title.getStyleClass().add("h1");

        Label help = new Label("Select an account; the applicable schedule tabs will enable (Excel-like gating).");
        help.setWrapText(true);

        configureAccountCombo();

        buildTabs();

        HBox top = new HBox(10, new Label("Account:"), this.accountSelect);
        top.setPadding(new Insets(6, 6, 6, 6));

        VBox header = new VBox(6, title, help, top, this.status, new Separator());
        header.setPadding(new Insets(6, 6, 6, 6));

        this.root.setTop(header);
        this.root.setCenter(this.tabs);

        loadAccounts();

        this.accountSelect.valueProperty().addListener((obs, oldV, newV) -> applyGating(newV));
        if (!this.accountSelect.getItems().isEmpty())
        {
            this.accountSelect.getSelectionModel().select(0);
        }
    }

    @Override public String title() { return "Schedules"; }

    public Node node() { return this.root; }

    private void configureAccountCombo()
    {
        this.accountSelect.setPrefWidth(560);
        this.accountSelect.setCellFactory(cb -> new ListCell<>()
        {
            @Override protected void updateItem(Account a, boolean empty)
            {
                super.updateItem(a, empty);
                if (empty || a == null) { setText(null); return; }
                String sub = a.getSubtype() == null ? "" : ("  [" + a.getSubtype().name() + "]");
                setText(a.getCode() + " — " + a.getName() + sub);
            }
        });
        this.accountSelect.setButtonCell(this.accountSelect.getCellFactory().call(null));
    }

    private void buildTabs()
    {
        addTab("RECEIVABLE", "Receivables");
        addTab("PAYABLE", "Payables");
        addTab("PREPAID", "Prepaids");
        addTab("DEFERRED_REVENUE", "Deferred Revenue");
        addTab("OTHER_ASSET", "Other Assets / Deposits");
        addTab("OTHER_LIABILITY", "Other Liabilities");
        addTab("INVENTORY", "Inventory");
        addTab("FIXED_ASSET", "Fixed Assets");

        // Default: disabled until account selection happens
        for (Tab t : this.tabs.getTabs()) t.setDisable(true);
    }

    private void addTab(String scheduleCode, String label)
    {
        Tab t = new Tab(label);
        t.setClosable(false);
        t.setContent(new Label(label + " schedule UI not wired yet."));
        this.tabs.getTabs().add(t);
        this.tabIndex.put(scheduleCode, t);
    }

    private void loadAccounts()
    {
        if (!this.services.sessionContext().isDatabaseOpen())
        {
            showNoAccountsMessage("Open a database to load schedule account eligibility.");
            return;
        }

        this.status.setText("Loading accounts...");

        UiAsync.run("schedule-accounts-load",
            this::loadDbAccounts,
            accounts -> {
                if (!accounts.isEmpty())
                {
                    this.accountSelect.getItems().setAll(accounts);
                    this.status.setText("Loaded " + accounts.size() + " account(s).");
                    this.accountSelect.getSelectionModel().select(0);
                    return;
                }
                showNoAccountsMessage("No service-backed accounts are available.");
            },
            ex -> showNoAccountsMessage("Unable to load accounts: " + UiErrors.safeMessage(ex)));
    }

    private List<Account> loadDbAccounts()
    {
        AccountLookupService lookup = this.services.accountLookup();
        return lookup.listActivePostingAccounts();
    }

    private void showNoAccountsMessage(String message)
    {
        this.accountSelect.getItems().clear();
        this.accountSelect.getSelectionModel().clearSelection();
        this.status.setText(message + " " + NO_SERVICE_DATA_MESSAGE);
    }

    private void applyGating(Account account)
    {
        for (Tab t : this.tabs.getTabs()) t.setDisable(true);
        if (account == null) return;

        Set<String> allowed = this.eligibility == null ? Set.of() : this.eligibility.allowedScheduleKindCodes(account);
        for (String code : allowed)
        {
            Tab t = this.tabIndex.get(code);
            if (t != null) t.setDisable(false);
        }
    }

    @Override public Node root() { return this.root; }
}
