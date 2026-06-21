package org.nonprofitbookkeeping.ui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.nonprofitbookkeeping.bridge.dashboard.DashboardDataBridge;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.ReconciliationService;
import nonprofitbookkeeping.service.UndepositedFundsService;

/** Native service-backed alternate dashboard with honest unsupported states. */
public class AlternateDashboardPanel implements AppPanel
{
    static final String EMPTY_STATE = AlternateDashboardModel.EMPTY_STATE;
    static final String NOT_WIRED_STATE = AlternateDashboardModel.NOT_WIRED_STATE;

    private final UiSessionContext sessionContext;
    private final DashboardDataLoader dashboardLoader;
    private final UndepositedFundsMetric undepositedFundsMetric;
    private final DatePicker asOfDate = new DatePicker(LocalDate.now());
    private final GridPane grid = new GridPane();
    private final VBox root = new VBox(10);
    private final AlternateDashboardModel model = new AlternateDashboardModel();

    public AlternateDashboardPanel(UiSessionContext sessionContext, UiServiceProvider services)
    {
        this(sessionContext,
            asOf -> services.dashboardData().load(asOf),
            () -> new UndepositedFundsService().listItems().size());
    }

    AlternateDashboardPanel(UiSessionContext sessionContext, DashboardDataLoader dashboardLoader,
        UndepositedFundsMetric undepositedFundsMetric)
    {
        this.sessionContext = Objects.requireNonNull(sessionContext, "sessionContext");
        this.dashboardLoader = Objects.requireNonNull(dashboardLoader, "dashboardLoader");
        this.undepositedFundsMetric = Objects.requireNonNull(undepositedFundsMetric, "undepositedFundsMetric");
        build();
        refresh();
    }

    @Override
    public String title()
    {
        return "Dashboard";
    }

    @Override
    public Node root()
    {
        return this.root;
    }

    void refresh()
    {
        this.grid.getChildren().clear();
        DashboardDataBridge.DashboardSnapshot snapshot = null;
        String dataStatus = null;
        if (this.sessionContext.isDatabaseOpen())
        {
            try
            {
                snapshot = this.dashboardLoader.load(this.asOfDate.getValue() == null ? LocalDate.now() : this.asOfDate.getValue());
            }
            catch (RuntimeException ex)
            {
                dataStatus = "Dashboard services could not load: " + UiErrors.safeMessage(ex);
            }
        }
        else
        {
            dataStatus = "Open a database to load service-backed accounting metrics.";
        }

        Integer unreconciled = CurrentCompany.isOpen() ? unreconciledCount() : null;
        Integer undeposited = undepositedCount();
        List<String> recent = recentTransactions().stream()
            .map(tx -> safe(tx.getDate()) + " — " + safe(tx.getMemo()))
            .toList();
        List<AlternateDashboardModel.Card> cards = this.model.cards(this.sessionContext, snapshot, dataStatus, unreconciled, undeposited, recent);
        int index = 0;
        for (AlternateDashboardModel.Card card : cards)
        {
            this.grid.add(card(card.title(), card.value(), card.detail()), index % 3, index / 3);
            index++;
        }
    }

    private void build()
    {
        Label title = new Label("Dashboard");
        title.getStyleClass().add("alternate-panel-title");
        Button reload = new Button("Reload");
        reload.setOnAction(e -> refresh());
        VBox header = new VBox(6, title, new javafx.scene.layout.HBox(8, new Label("As of"), this.asOfDate, reload));
        this.grid.setHgap(12);
        this.grid.setVgap(12);
        this.root.setPadding(new Insets(12));
        this.root.getChildren().setAll(header, new ScrollPane(this.grid));
    }

    private VBox card(String title, String value, String detail)
    {
        Label t = new Label(title);
        Label v = new Label(value);
        v.getStyleClass().add("alternate-dashboard-card-value");
        Label d = new Label(detail);
        d.setWrapText(true);
        d.getStyleClass().add("alternate-dashboard-card-status");
        VBox box = new VBox(8, t, v, new Separator(), d);
        box.setPadding(new Insets(12));
        box.setMinWidth(220);
        box.getStyleClass().add("alternate-dashboard-card");
        return box;
    }

    private Integer unreconciledCount()
    {
        return ReconciliationService.listReconcilableAccounts().stream()
            .mapToInt(account -> ReconciliationService.getUnreconciled(account).size()).sum();
    }

    private Integer undepositedCount()
    {
        try
        {
            return this.undepositedFundsMetric.count();
        }
        catch (RuntimeException ex)
        {
            return null;
        }
    }

    private List<AccountingTransaction> recentTransactions()
    {
        if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null || CurrentCompany.getCompany().getLedger() == null)
        {
            return List.of();
        }
        List<AccountingTransaction> all = new ArrayList<>(CurrentCompany.getCompany().getLedger().getJournal().getJournalTransactions());
        int from = Math.max(0, all.size() - 5);
        return all.subList(from, all.size());
    }

    private String safe(String value)
    {
        return value == null || value.isBlank() ? "(blank)" : value;
    }

    interface DashboardDataLoader
    {
        DashboardDataBridge.DashboardSnapshot load(LocalDate asOf);
    }

    interface UndepositedFundsMetric
    {
        int count();
    }
}
