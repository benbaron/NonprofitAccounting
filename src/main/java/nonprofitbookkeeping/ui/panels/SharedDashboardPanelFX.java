package nonprofitbookkeeping.ui.panels;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Journal;
import nonprofitbookkeeping.service.DashboardService;
import nonprofitbookkeeping.service.DashboardSnapshot;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.util.FormatUtils;
import org.nonprofitbookkeeping.service.FundBalanceRow;

/**
 * Shared dashboard surface used by both the classic and new UI shells.
 *
 * <p>The panel owns presentation and calculations while shell-specific
 * navigation is supplied through {@link DashboardNavigation}.</p>
 */
public class SharedDashboardPanelFX extends BorderPane
{
    private final DashboardNavigation navigation;
    private final DashboardService dashboardService;
    private final DatePicker asOfDate = new DatePicker(LocalDate.now());
    private final Spinner<Integer> recentLimit = new Spinner<>();
    private final Label companyName = new Label("No company open");
    private final Label periodLabel = new Label();
    private final Label status = new Label();
    private final TilePane cards = new TilePane();
    private final TableView<FundBalanceRow> fundTable = new TableView<>();
    private final TableView<AccountingTransaction> recentTable =
        new TableView<>();

    /** Creates a dashboard with no-op shell navigation. */
    public SharedDashboardPanelFX()
    {
        this(new DashboardNavigation() { }, new DashboardService());
    }

    /** Creates a dashboard with shell-specific navigation. */
    public SharedDashboardPanelFX(DashboardNavigation navigation)
    {
        this(navigation, new DashboardService());
    }

    SharedDashboardPanelFX(DashboardNavigation navigation,
        DashboardService dashboardService)
    {
        this.navigation = navigation == null ? new DashboardNavigation() { } :
            navigation;
        this.dashboardService = dashboardService;
        build();
        CurrentCompany.CompanyListener.addCompanyListener(
            isOpen -> reloadData());
        reloadData();
    }

    /** Reloads all dashboard metrics and tables. */
    public final void reloadData()
    {
        DashboardSnapshot snapshot = this.dashboardService.load(
            this.asOfDate.getValue(), this.recentLimit.getValue());
        this.companyName.setText(snapshot.companyName());
        this.periodLabel.setText("Fiscal period " +
            snapshot.fiscalPeriodStart() + " through " + snapshot.asOfDate());
        this.status.setText(snapshot.status());
        populateCards(snapshot);
        this.fundTable.getItems().setAll(snapshot.fundBalances());
        this.recentTable.getItems().setAll(snapshot.recentTransactions());
    }

    private void build()
    {
        setPadding(PanelChrome.PANEL_PADDING);

        this.companyName.getStyleClass().add("company-indicator");
        this.companyName.setStyle("-fx-font-size: 1.4em; -fx-font-weight: bold;");
        Button refresh = new Button("Reload");
        refresh.setOnAction(event -> reloadData());

        this.recentLimit.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, 100,
                PreferencesService.getDashboardRecentTransactionLimit()));
        this.recentLimit.setEditable(true);
        this.recentLimit.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null)
            {
                PreferencesService.setDashboardRecentTransactionLimit(newValue);
                reloadData();
            }
        });
        this.asOfDate.valueProperty().addListener(
            (obs, oldValue, newValue) -> reloadData());

        HBox controls = new HBox(10,
            new Label("As of"), this.asOfDate,
            new Label("Recent transactions"), this.recentLimit,
            refresh);
        controls.setAlignment(Pos.CENTER_LEFT);
        VBox header = PanelChrome.topSection("Dashboard",
            this.companyName, this.periodLabel, controls, this.status);
        setTop(header);

        this.cards.setHgap(10);
        this.cards.setVgap(10);
        this.cards.setPrefColumns(4);
        this.cards.setTileAlignment(Pos.TOP_LEFT);
        ScrollPane cardScroll = new ScrollPane(this.cards);
        cardScroll.setFitToWidth(true);
        cardScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cardScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        cardScroll.setPrefHeight(270);

        buildFundTable();
        buildRecentTable();

        TitledPane funds = new TitledPane("Fund Balances",
            this.fundTable);
        funds.setCollapsible(false);
        TitledPane recent = new TitledPane("Recent Transactions",
            this.recentTable);
        recent.setCollapsible(false);

        SplitPane tables = new SplitPane(funds, recent);
        tables.setOrientation(Orientation.VERTICAL);
        tables.setDividerPositions(0.43);
        VBox center = new VBox(10, cardScroll, tables);
        VBox.setVgrow(tables, Priority.ALWAYS);
        setCenter(center);
    }

    private void populateCards(DashboardSnapshot snapshot)
    {
        this.cards.getChildren().setAll(
            card("Cash and Bank", snapshot.cashAndBank(),
                this.navigation::openCashAndBank),
            card("Total Assets", snapshot.totalAssets(),
                this.navigation::openLedger),
            card("Total Liabilities", snapshot.totalLiabilities(),
                this.navigation::openLedger),
            card("Unrestricted Net Assets",
                snapshot.unrestrictedNetAssets(), this.navigation::openFunds),
            card("Restricted Net Assets", snapshot.restrictedNetAssets(),
                this.navigation::openFunds),
            card("Current-Period Income", snapshot.periodIncome(),
                this.navigation::openReports),
            card("Current-Period Expenses", snapshot.periodExpenses(),
                this.navigation::openReports),
            card("Current-Period Surplus/Deficit",
                snapshot.periodSurplus(), this.navigation::openReports),
            card("Unreconciled",
                snapshot.unreconciledAmount(),
                snapshot.unreconciledCount() + " transaction(s)",
                this.navigation::openReconciliation),
            card("Undeposited Funds",
                BigDecimal.valueOf(snapshot.undepositedCount()),
                snapshot.undepositedCount() + " item(s)",
                this.navigation::openUndepositedFunds));
    }

    private Button card(String title, BigDecimal value, Runnable action)
    {
        return card(title, value, "", action);
    }

    private Button card(String title, BigDecimal value, String detail,
        Runnable action)
    {
        String text = title + "\n" + FormatUtils.formatCurrency(value);
        if (detail != null && !detail.isBlank())
        {
            text += "\n" + detail;
        }
        Button button = new Button(text);
        button.setWrapText(true);
        button.setAlignment(Pos.TOP_LEFT);
        button.setMinSize(230, 88);
        button.setPrefSize(250, 96);
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button.getStyleClass().add("dashboard-metric-card");
        button.setOnAction(event -> action.run());
        return button;
    }

    private void buildFundTable()
    {
        TableColumn<FundBalanceRow, String> code =
            new TableColumn<>("Fund");
        code.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            safe(value.getValue().getFundCode())));
        code.setPrefWidth(150);

        TableColumn<FundBalanceRow, String> name =
            new TableColumn<>("Name");
        name.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            safe(value.getValue().getFundName())));
        name.setPrefWidth(280);

        TableColumn<FundBalanceRow, String> type =
            new TableColumn<>("Type");
        type.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            value.getValue().getFundType().toString()));
        type.setPrefWidth(170);

        TableColumn<FundBalanceRow, BigDecimal> balance =
            new TableColumn<>("Balance");
        balance.setCellValueFactory(value -> new ReadOnlyObjectWrapper<>(
            value.getValue().getBalance()));
        balance.setCellFactory(column -> currencyCell());
        balance.setPrefWidth(150);

        this.fundTable.getColumns().setAll(code, name, type, balance);
        this.fundTable.setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        this.fundTable.setPlaceholder(new Label(
            "No fund balances are available for the selected date."));
        this.fundTable.setRowFactory(table -> {
            TableRow<FundBalanceRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty())
                {
                    this.navigation.openFunds();
                }
            });
            return row;
        });
    }

    private void buildRecentTable()
    {
        TableColumn<AccountingTransaction, String> date =
            new TableColumn<>("Date");
        date.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            safe(value.getValue().getDate())));
        date.setPrefWidth(120);

        TableColumn<AccountingTransaction, String> description =
            new TableColumn<>("Payee / Description");
        description.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            transactionDescription(value.getValue())));
        description.setPrefWidth(330);

        TableColumn<AccountingTransaction, String> accounts =
            new TableColumn<>("Accounts");
        accounts.setCellValueFactory(value -> new ReadOnlyStringWrapper(
            accountSummary(value.getValue())));
        accounts.setPrefWidth(360);

        TableColumn<AccountingTransaction, BigDecimal> amount =
            new TableColumn<>("Debit Total");
        amount.setCellValueFactory(value -> new ReadOnlyObjectWrapper<>(
            value.getValue().getTotalAmount()));
        amount.setCellFactory(column -> currencyCell());
        amount.setPrefWidth(140);

        TableColumn<AccountingTransaction, Integer> id =
            new TableColumn<>("Transaction ID");
        id.setCellValueFactory(value -> new ReadOnlyObjectWrapper<>(
            value.getValue().getId()));
        id.setCellFactory(column -> new TableCell<>()
        {
            private final Hyperlink link = new Hyperlink();

            {
                this.link.setOnAction(event -> {
                    Integer transactionId = getItem();
                    if (transactionId != null)
                    {
                        SharedDashboardPanelFX.this.navigation
                            .openTransactionInLedger(transactionId);
                    }
                });
            }

            @Override
            protected void updateItem(Integer transactionId, boolean empty)
            {
                super.updateItem(transactionId, empty);
                if (empty || transactionId == null)
                {
                    setGraphic(null);
                }
                else
                {
                    this.link.setText(transactionId.toString());
                    setGraphic(this.link);
                }
            }
        });
        id.setPrefWidth(130);

        this.recentTable.getColumns().setAll(
            date, description, accounts, amount, id);
        this.recentTable.setColumnResizePolicy(
            TableView.UNCONSTRAINED_RESIZE_POLICY);
        this.recentTable.setPlaceholder(new Label(
            "No recent transactions are available."));
        this.recentTable.setRowFactory(table -> {
            TableRow<AccountingTransaction> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty())
                {
                    openEditor(row.getItem());
                }
            });
            return row;
        });
    }

    private TableCell<FundBalanceRow, BigDecimal> currencyCell()
    {
        return new TableCell<>()
        {
            @Override
            protected void updateItem(BigDecimal value, boolean empty)
            {
                super.updateItem(value, empty);
                setText(empty || value == null ? null :
                    FormatUtils.formatCurrency(value));
                setAlignment(Pos.CENTER_RIGHT);
            }
        };
    }

    private TableCell<AccountingTransaction, BigDecimal>
        transactionCurrencyCell()
    {
        return new TableCell<>()
        {
            @Override
            protected void updateItem(BigDecimal value, boolean empty)
            {
                super.updateItem(value, empty);
                setText(empty || value == null ? null :
                    FormatUtils.formatCurrency(value));
                setAlignment(Pos.CENTER_RIGHT);
            }
        };
    }

    private <S> TableCell<S, BigDecimal> currencyCellGeneric()
    {
        return new TableCell<>()
        {
            @Override
            protected void updateItem(BigDecimal value, boolean empty)
            {
                super.updateItem(value, empty);
                setText(empty || value == null ? null :
                    FormatUtils.formatCurrency(value));
                setAlignment(Pos.CENTER_RIGHT);
            }
        };
    }

    private String transactionDescription(AccountingTransaction transaction)
    {
        String payee = safe(transaction.getToFrom());
        String memo = safe(transaction.getMemo());
        if (!payee.isBlank() && !memo.isBlank())
        {
            return payee + " — " + memo;
        }
        return payee.isBlank() ? memo : payee;
    }

    private String accountSummary(AccountingTransaction transaction)
    {
        if (transaction.getEntries() == null)
        {
            return "";
        }
        Set<String> names = new LinkedHashSet<>();
        for (AccountingEntry entry : transaction.getEntries())
        {
            String name = safe(entry.getAccountName());
            names.add(name.isBlank() ? safe(entry.getAccountNumber()) : name);
        }
        return names.stream().filter(value -> !value.isBlank())
            .collect(Collectors.joining("; "));
    }

    private void openEditor(AccountingTransaction existing)
    {
        if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null ||
            CurrentCompany.getCompany().getLedger() == null)
        {
            return;
        }
        Journal journal = CurrentCompany.getCompany().getLedger().getJournal();
        if (journal == null)
        {
            return;
        }

        GeneralJournalEntryPanelFX editor = new GeneralJournalEntryPanelFX(
            existing, transaction -> {
                journal.updateTransaction(transaction);
                try
                {
                    CurrentCompany.persist();
                    reloadData();
                }
                catch (IOException ex)
                {
                    AlertBox.showError(getScene() == null ? null :
                        getScene().getWindow(),
                        "Unable to save the transaction. Please try again.");
                }
            });

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Transaction " + existing.getId());
        dialog.getDialogPane().setContent(editor);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(1100, 760);
        dialog.setResizable(true);
        if (getScene() != null && getScene().getWindow() != null)
        {
            dialog.initOwner(getScene().getWindow());
        }
        dialog.showAndWait();
    }

    private <S> TableCell<S, BigDecimal> currencyCell()
    {
        return new TableCell<>()
        {
            @Override
            protected void updateItem(BigDecimal value, boolean empty)
            {
                super.updateItem(value, empty);
                setText(empty || value == null ? null :
                    FormatUtils.formatCurrency(value));
                setAlignment(Pos.CENTER_RIGHT);
            }
        };
    }

    private String safe(String value)
    {
        return value == null ? "" : value;
    }
}
