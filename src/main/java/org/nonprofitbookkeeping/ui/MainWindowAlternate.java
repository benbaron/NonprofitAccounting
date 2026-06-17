package org.nonprofitbookkeeping.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import nonprofitbookkeeping.tools.H2SchemaMigrator;
import nonprofitbookkeeping.ui.panels.HelpPanelFX;
import nonprofitbookkeeping.ui.panels.LedgerReconcilePanelFX;
import nonprofitbookkeeping.service.ReconciliationService;
import nonprofitbookkeeping.service.UndepositedFundsService;
import nonprofitbookkeeping.service.DocumentStorageService;
import nonprofitbookkeeping.ui.panels.UndepositedFundsPanelFX;
import nonprofitbookkeeping.ui.panels.DocumentsPanelFX;
import nonprofitbookkeeping.ui.panels.DonorsPanelFX;
import nonprofitbookkeeping.ui.panels.JournalEntryWorkspaceFX;
import nonprofitbookkeeping.service.DonorService;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.nonprofitbookkeeping.ui.routing.WorkspaceRouteDecision;
import org.nonprofitbookkeeping.ui.routing.WorkspaceRouter;

/**
 * Alternate dashboard-first UI system that preserves current panel APIs.
 *
 * <p>Status: live default for {@link MainApp}. This shell is selected when
 * {@code npbk.ui.variant} is absent or set to {@code alternate}; it renders
 * navigation and commands through dashboard, icon-rail, and command-center
 * controls rather than a traditional top menu bar.</p>
 */
public class MainWindowAlternate extends BorderPane
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MainWindowAlternate.class);
    private static final Map<String, Color> SURFACE_COLORS = Map.of(
        "Slate", Color.web("#eef1f8"),
        "Warm", Color.web("#f7f2ee"),
        "Cool", Color.web("#edf5f9"));

    private final PanelHost panelHost = new PanelHost();
    private final NavigationPane nav =
        new NavigationPane(this::openPanel, this::openInspectorForSelection, this::openRecordServicePanel);
    private final Label alternateStatus = new Label("Select an item to see context details.");
    private final VBox dashboardCanvas = new VBox();
    private final StackPane workspaceSurface = new StackPane();
    private final VBox alternateSettingsPane = new VBox();
    private final StackPane alternateContentPane = new StackPane();
    private final BorderPane alternateBudgetPane = new BorderPane();
    private final VBox alternateSchedulesPane = new VBox(10);
    private final VBox databaseSelectorPane = new VBox();
    private final VBox companySelectorPane = new VBox();
    private final VBox profilePane = new VBox();
    private final VBox searchPane = new VBox();
    private final VBox navButtons = new VBox(6);
    private final TitledPane importToolsPane = new TitledPane();
    private final WorkspaceRouter workspaceRouter = new WorkspaceRouter();
    private final BankingPanelFactory bankingPanelFactory;
    private final AlternateDataContextService contextService;
    private final UiSessionContext sessionContext;
    private final Label headerTitle = new Label("Dashboard");
    private final Label headerSubtitle = new Label("No company open");
    private final AlternateNavigationModel navigationModel = new AlternateNavigationModel();
    private final List<Button> iconRailButtons = new ArrayList<>();
    private LegacyPanelAdapter.AdaptedPanel activeAdaptedPanel;
    private AppPanelId activePanelId = AppPanelId.DASHBOARD;
    private static final String SCHEDULED_REPORTS_KEY = "alternate.scheduled.reports";
    private final Preferences alternatePreferences = Preferences.userNodeForPackage(MainWindowAlternate.class);


    interface BankingPanelFactory
    {
        Node createReconcilePanel();

        Node createUndepositedFundsPanel();

        Node createDocumentsPanel();
    }

    static class DefaultBankingPanelFactory implements BankingPanelFactory
    {
        public Node createReconcilePanel()
        {
            return new LedgerReconcilePanelFX(new ReconciliationService());
        }

        public Node createUndepositedFundsPanel()
        {
            return new UndepositedFundsPanelFX(new UndepositedFundsService());
        }

        public Node createDocumentsPanel()
        {
            return new DocumentsPanelFX(new DocumentStorageService());
        }
    }

    public MainWindowAlternate()
    {
        this(new DefaultBankingPanelFactory());
    }

    MainWindowAlternate(BankingPanelFactory bankingPanelFactory)
    {
        this(bankingPanelFactory, new AlternateDataContextService());
    }

    MainWindowAlternate(BankingPanelFactory bankingPanelFactory, AlternateDataContextService contextService)
    {
        this.bankingPanelFactory = bankingPanelFactory;
        this.contextService = contextService;
        this.sessionContext = contextService.sessionContext();
        this.sessionContext.companyOpenProperty().addListener((obs, oldValue, newValue) -> {
            refreshIconBarState();
            rebuildNavigationButtons();
        });
        this.sessionContext.databaseOpenProperty().addListener((obs, oldValue, newValue) -> rebuildNavigationButtons());
        setTop(buildHeader());
        setCenter(buildWorkspace());
        setLeft(buildIconRail());
        setPadding(new Insets(10));
        setBackground(new Background(new BackgroundFill(SURFACE_COLORS.get("Slate"), CornerRadii.EMPTY, Insets.EMPTY)));
        openPanel(AppPanelId.DASHBOARD);
    }

    private Node buildIconRail()
    {
        this.iconRailButtons.clear();
        this.iconRailButtons.add(iconButton("◉", this::openProfilePage));
        this.iconRailButtons.add(iconButton("⌂", () -> openPanel(AppPanelId.DASHBOARD)));
        this.iconRailButtons.add(iconButton("⌕", this::openSearchPage));
        this.iconRailButtons.add(iconButton("☰", this::openCommandCenter));
        this.iconRailButtons.add(iconButton("⚙", () -> openPanel(AppPanelId.SETTINGS)));
        VBox rail = new VBox(14, this.iconRailButtons.toArray(Button[]::new));
        rail.setPadding(new Insets(14, 8, 14, 8));
        rail.setStyle("-fx-background-color: #1f2431; -fx-background-radius: 14;");
        refreshIconBarState();
        return rail;
    }

    private Button iconButton(String text, Runnable action)
    {
        Button button = new Button(text);
        button.setMinSize(46, 46);
        button.setOnAction(e -> action.run());
        button.setStyle("-fx-background-color: #2c3347; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-size: 20px;");
        return button;
    }

    private Node buildHeader()
    {
        this.headerTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: 700;");
        this.headerSubtitle.setStyle("-fx-text-fill: #5c6482;");
        this.headerSubtitle.textProperty().bind(this.sessionContext.activeCompanyDisplayLabelProperty());

        VBox heading = new VBox(2, this.headerTitle, this.headerSubtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ChoiceBox<String> period = new ChoiceBox<>(FXCollections.observableArrayList("This Month", "Quarter", "Year"));
        period.setValue("This Month");
        DateRangeSelector rangeSelector = new DateRangeSelector();

        HBox header = new HBox(12, heading, spacer, new Label("Range"), period, rangeSelector);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        return header;
    }

    private Node buildWorkspace()
    {
        VBox leftNav = buildLeftNavigation();

        this.dashboardCanvas.getChildren().setAll(buildDashboardCards());
        this.dashboardCanvas.setSpacing(12);

        this.panelHost.setVisible(false);
        this.panelHost.setManaged(false);

        this.alternateSettingsPane.setVisible(false);
        this.alternateSettingsPane.setManaged(false);
        this.alternateContentPane.setVisible(false);
        this.alternateContentPane.setManaged(false);
        this.workspaceSurface.getChildren().setAll(this.dashboardCanvas, this.alternateSettingsPane, this.alternateContentPane, this.panelHost);
        StackPane.setMargin(this.dashboardCanvas, new Insets(8));
        StackPane.setMargin(this.panelHost, new Insets(8));
        HBox.setHgrow(this.workspaceSurface, Priority.ALWAYS);
        this.workspaceSurface.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 18;");

        HBox body = new HBox(12, leftNav, this.workspaceSurface);
        body.setAlignment(Pos.TOP_LEFT);
        return body;
    }

    private VBox buildLeftNavigation()
    {
        this.importToolsPane.setText("Import & Tools");
        this.importToolsPane.setContent(new VBox(6,
            navButton("Assets Register", AppPanelId.ASSETS_REGISTER),
            navButton("Budget vs Actual", AppPanelId.BUDGET_VS_ACTUAL),
            navButton("Depreciation", AppPanelId.DEPRECIATION_RUNS)));
        this.importToolsPane.setExpanded(false);
        rebuildNavigationButtons();

        VBox wrapper = new VBox(10, new Label("Navigation"), this.navButtons, this.importToolsPane);
        wrapper.setPadding(new Insets(12));
        wrapper.setMinWidth(240);
        wrapper.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16;");
        return wrapper;
    }

    private Button navButton(String label, AppPanelId id)
    {
        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> openPanel(id));
        return button;
    }


    private Button navActionButton(String label, Runnable action)
    {
        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> action.run());
        return button;
    }

    private Node buildDashboardCards()
    {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.add(buildCard("Receivables", "No data", "No service-backed data source is wired for this panel yet."), 0, 0);
        grid.add(buildCard("Payables", "No data", "No service-backed data source is wired for this panel yet."), 1, 0);
        grid.add(buildCard("Profit & Loss", "No data", "No service-backed data source is wired for this panel yet."), 2, 0);
        grid.add(buildChartCard(), 0, 1, 2, 1);
        grid.add(buildBalancesCard(), 2, 1);
        return new ScrollPane(grid);
    }

    private VBox buildCard(String title, String value, String delta)
    {
        Label t = new Label(title);
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 20px; -fx-font-weight: 700;");
        Label d = new Label(delta);
        d.setStyle("-fx-text-fill: #5962cc;");
        VBox box = new VBox(8, t, v, d);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return box;
    }

    private VBox buildChartCard()
    {
        VBox box = new VBox(8, new Label("Cash Flow"),
            new Label("No service-backed data source is wired for this panel yet."));
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return box;
    }

    private VBox buildBalancesCard()
    {
        VBox list = new VBox(6,
            new Label("No service-backed data source is wired for this panel yet."));
        VBox box = new VBox(8, new Label("Account Balances"), new Separator(), list);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return box;
    }

    private VBox buildAlternateSettingsPane()
    {
        ChoiceBox<String> backgroundChoice = new ChoiceBox<>(FXCollections.observableArrayList(SURFACE_COLORS.keySet()));
        backgroundChoice.setValue("Slate");
        backgroundChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            Color selected = SURFACE_COLORS.getOrDefault(newVal, SURFACE_COLORS.get("Slate"));
            setBackground(new Background(new BackgroundFill(selected, CornerRadii.EMPTY, Insets.EMPTY)));
        });

        ChoiceBox<String> roundingChoice = new ChoiceBox<>(FXCollections.observableArrayList("Soft", "Rounded", "Sharp"));
        roundingChoice.setValue("Rounded");

        this.alternateStatus.setWrapText(true);

        this.alternateSettingsPane.getChildren().setAll(
            new Label("Alternate View Settings"),
            new Separator(),
            new Label("Background"),
            backgroundChoice,
            new Label("Card Corners"),
            roundingChoice,
            new Label("Status"),
            this.alternateStatus,
            new Separator(),
            new Label("Custom Fields / Localization / Expenses / Bank placeholders added in alternate templates."));
        this.alternateSettingsPane.setSpacing(8);
        this.alternateSettingsPane.setPadding(new Insets(12));
        this.alternateSettingsPane.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return this.alternateSettingsPane;
    }



    private VBox buildDatabaseSelectorPane()
    {
        TextField dbPath = new TextField();
        dbPath.setPromptText("/path/to/file.mv.db");
        Path activeDatabaseBasePath = this.contextService.activeDatabaseBasePath();
        if (activeDatabaseBasePath != null)
        {
            dbPath.setText(activeDatabaseBasePath.toString());
        }
        ListView<String> recent = new ListView<>(FXCollections.observableArrayList(this.contextService.recentDatabasePaths()));
        recent.setPrefHeight(120);
        Button browse = new Button("Browse...");
        Button open = new Button("Open Database");
        Label state = new Label();
        state.setText(dbPath.getText().isBlank() ? "No file selected." : "Selected: " + Path.of(dbPath.getText().trim()).getFileName());

        recent.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
            {
                dbPath.setText(newVal);
                state.setText("Selected: " + Path.of(newVal).getFileName());
            }
        });
        browse.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select database file");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database files", "*.mv.db", "*.db", "*.h2.db"));
            File selected = chooser.showOpenDialog(getScene() == null ? null : getScene().getWindow());
            if (selected != null)
            {
                dbPath.setText(selected.getAbsolutePath());
                state.setText("Selected: " + selected.getName());
            }
        });
        open.setOnAction(e -> {
            String value = dbPath.getText();
            if (value == null || value.isBlank())
            {
                this.alternateStatus.setText("No database selected.");
                state.setText("No database selected.");
                return;
            }
            try
            {
                this.contextService.openDatabase(Paths.get(value.trim()));
                String openedMessage = "Database opened: " + value.trim();
                this.alternateStatus.setText(openedMessage);
                state.setText(openedMessage);
                refreshHeaderLabels();
                rebuildNavigationButtons();
            }
            catch (Exception ex)
            {
                String failedMessage = "Failed to open database: " + ex.getMessage();
                this.alternateStatus.setText(failedMessage);
                state.setText(failedMessage);
            }
        });
        this.databaseSelectorPane.getChildren().setAll(new Label("Open Database (.mv.db/.db)"), new HBox(8, dbPath, browse), new Label("Recent Databases"), recent, open, state);
        this.databaseSelectorPane.setPadding(new Insets(12));
        this.databaseSelectorPane.setSpacing(10);
        return this.databaseSelectorPane;
    }

    private VBox buildCompanySelectorPane()
    {
        Map<String, Long> companiesByName = new LinkedHashMap<>();
        try
        {
            for (var record : this.contextService.listCompanies())
            {
                companiesByName.put(record.name() + " (ID: " + record.id() + ")", record.id());
            }
        }
        catch (Exception ex)
        {
            this.alternateStatus.setText("Failed to load companies: " + ex.getMessage());
        }

        ComboBox<String> companies = new ComboBox<>(FXCollections.observableArrayList(companiesByName.keySet()));
        companies.setPromptText("Select company");
        Label state = new Label("No company selected.");

        ListView<String> recent = new ListView<>();
        recent.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        recent.setPrefHeight(140);
        try
        {
            Map<Long, String> labelsById = companiesByName.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
            recent.setItems(FXCollections.observableArrayList(this.contextService.recentCompanies().stream()
                .map(choice -> labelsById.getOrDefault(choice.id(), choice.label()))
                .toList()));
        }
        catch (Exception ignored)
        {
            recent.setItems(FXCollections.observableArrayList());
        }

        recent.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            companies.setValue(newVal);
            if (newVal != null && !newVal.isBlank())
            {
                state.setText("Selected: " + newVal);
            }
        });

        companies.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank())
            {
                state.setText("Selected: " + newVal);
            }
        });

        Button open = new Button("Open Company");
        open.setOnAction(e -> {
            String selected = companies.getValue();
            Long companyId = companiesByName.get(selected);
            if (companyId == null)
            {
                this.alternateStatus.setText("No company selected.");
                state.setText("No company selected.");
                return;
            }
            try
            {
                this.contextService.openCompany(companyId, selected);
                String openedMessage = "Company opened: " + selected;
                this.alternateStatus.setText(openedMessage);
                state.setText(openedMessage);
                refreshHeaderLabels();
                rebuildNavigationButtons();
            }
            catch (Exception ex)
            {
                String failedMessage = "Failed to open company: " + ex.getMessage();
                this.alternateStatus.setText(failedMessage);
                state.setText(failedMessage);
            }
        });
        this.companySelectorPane.getChildren().setAll(new Label("Open Company"), companies, new Label("Recent Companies"), recent, open, state);
        this.companySelectorPane.setPadding(new Insets(12));
        this.companySelectorPane.setSpacing(10);
        return this.companySelectorPane;
    }

    private VBox buildProfilePane()
    {
        Label title = new Label("User Profile");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700;");
        this.profilePane.getChildren().setAll(
            title,
            new Separator(),
            new Label("Signed in user"),
            new Label("Role: Accountant"),
            new Label("Preferences and account details will be wired in a later phase."));
        this.profilePane.setPadding(new Insets(12));
        this.profilePane.setSpacing(10);
        this.profilePane.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return this.profilePane;
    }

    private VBox buildSearchPane()
    {
        TextField query = new TextField();
        query.setPromptText("Search accounts, transactions, reports...");
        Button search = new Button("Search");
        Label status = new Label("Enter a query to search.");
        search.setOnAction(e -> status.setText(executeSearchQuery(query.getText())));
        this.searchPane.getChildren().setAll(
            new Label("Search"),
            new Separator(),
            query,
            search,
            status);
        this.searchPane.setPadding(new Insets(12));
        this.searchPane.setSpacing(10);
        this.searchPane.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return this.searchPane;
    }


    private VBox buildCommandCenterPane()
    {
        VBox fileGroup = new VBox(6,
            new Label("File"),
            actionButton("Open Database", this::openDatabaseSelector),
            actionButton("H2 Recovery Tool", this::openH2RecoveryTool),
            actionButton("Open Company", this::openCompanySelector));

        VBox runGroup = new VBox(6,
            new Label("Run"),
            actionButton("Chart of Accounts", () -> openPanel(AppPanelId.CHART_OF_ACCOUNTS)),
            actionButton("Journal", () -> openPanel(AppPanelId.LEDGER_REGISTER)),
            actionButton("Inventory", () -> openPanel(AppPanelId.INVENTORY)),
            actionButton("Reports Workspace", () -> openPanel(AppPanelId.REPORTS_WORKSPACE)));

        VBox reportActions = new VBox(6,
            new Label("Reports actions"),
            actionButton("Schedule", this::openReportsScheduleDirect));

        Button newButton = actionButton("New", this::runNewAction);
        Button saveButton = actionButton("Save", this::runSaveAction);
        Button deleteButton = actionButton("Delete", this::runDeleteAction);
        Button cancelButton = actionButton("Cancel", this::runCancelAction);
        boolean panelCommandAvailable = hasActivePanelCommandTarget();
        newButton.setDisable(!panelCommandAvailable);
        saveButton.setDisable(!panelCommandAvailable);
        deleteButton.setDisable(!panelCommandAvailable);
        cancelButton.setDisable(!panelCommandAvailable);

        VBox quickActions = new VBox(6,
            new Label("Toolbar-style actions"),
            newButton,
            saveButton,
            deleteButton,
            cancelButton,
            actionButton("Find", this::openSearchPage),
            actionButton("Journal", () -> openPanel(AppPanelId.LEDGER_REGISTER)));

        VBox fundraisingGroup = new VBox(6,
            new Label("Fundraising"),
            actionButton("Donors", this::openDonorsDirect),
            actionButton("Funds", () -> openPanel(AppPanelId.FUNDS)));

        VBox bankingGroup = new VBox(6,
            new Label("Banking"),
            actionButton("Reconcile Accounts", this::openReconcileAccountsDirect),
            actionButton("Undeposited Funds", this::openUndepositedFundsDirect),
            actionButton("Documents & Attachments", this::openDocumentsDirect),
            actionButton("Account Activity", () -> openPanel(AppPanelId.LEDGER_REGISTER)),
            actionButton("Transactions", () -> openPanel(AppPanelId.LEDGER_REGISTER)));

        VBox helpGroup = new VBox(6,
            new Label("Help"),
            actionButton("Help Center", this::openHelpHint));

        VBox pane = new VBox(10, new Label("Command Center"), new Separator(), fileGroup, new Separator(), runGroup, new Separator(), reportActions, new Separator(), quickActions, new Separator(), fundraisingGroup, new Separator(), bankingGroup, new Separator(), helpGroup);
        pane.setPadding(new Insets(12));
        pane.setSpacing(10);
        pane.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return pane;
    }

    List<String> commandCenterActionLabelsForTest()
    {
        return buildCommandCenterPane().getChildren().stream()
            .filter(VBox.class::isInstance)
            .flatMap(group -> ((VBox) group).getChildren().stream())
            .filter(Button.class::isInstance)
            .map(node -> ((Button) node).getText())
            .toList();
    }


    private void openReportsScheduleDirect()
    {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Schedule Report");
        dialog.setHeaderText("Create scheduled report run");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> reportType = new ComboBox<>(FXCollections.observableArrayList(
            "Income Statement", "Balance Sheet", "Trial Balance"));
        reportType.setValue("Income Statement");
        ComboBox<String> frequency = new ComboBox<>(FXCollections.observableArrayList(
            "Daily", "Weekly", "Monthly", "Quarterly"));
        frequency.setValue("Monthly");
        DatePicker nextRun = new DatePicker(java.time.LocalDate.now().plusDays(1));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Report"), reportType);
        grid.addRow(1, new Label("Frequency"), frequency);
        grid.addRow(2, new Label("Next run"), nextRun);
        dialog.getDialogPane().setContent(grid);

        ButtonType selected = dialog.showAndWait().orElse(ButtonType.CANCEL);
        if (selected == ButtonType.OK)
        {
            String entry = reportType.getValue() + "|" + frequency.getValue() + "|" + nextRun.getValue();
            saveScheduledReport(entry);
            openInspectorForSelection("Reports", "Scheduled " + reportType.getValue() + " (" + frequency.getValue() + ") starting " + nextRun.getValue() + ".");
        }
        else
        {
            openInspectorForSelection("Reports", "Schedule action cancelled.");
        }
    }

    private void saveScheduledReport(String entry)
    {
        String existing = this.alternatePreferences.get(SCHEDULED_REPORTS_KEY, "");
        String updated = existing == null || existing.isBlank() ? entry : entry + "\n" + existing;
        this.alternatePreferences.put(SCHEDULED_REPORTS_KEY, updated);
    }

    void saveScheduledReportForTest(String entry)
    {
        saveScheduledReport(entry);
    }

    void clearScheduledReportsForTest()
    {
        this.alternatePreferences.remove(SCHEDULED_REPORTS_KEY);
    }

    String scheduledReportsSnapshotForTest()
    {
        return this.alternatePreferences.get(SCHEDULED_REPORTS_KEY, "");
    }



    String alternateStatusTextForTest()
    {
        return this.alternateStatus.getText();
    }

    private void openReconcileAccountsDirect()
    {
        try
        {
            showAlternatePane(this.bankingPanelFactory.createReconcilePanel());
            openInspectorForSelection("Banking", "Reconcile Accounts opened in alternate shell.");
        }
        catch (Exception ex)
        {
            openInspectorForSelection("Banking", "Reconcile Accounts failed: " + ex.getMessage());
        }
    }

    private void openUndepositedFundsDirect()
    {
        try
        {
            showAlternatePane(this.bankingPanelFactory.createUndepositedFundsPanel());
            openInspectorForSelection("Banking", "Undeposited Funds opened in alternate shell.");
        }
        catch (Exception ex)
        {
            openInspectorForSelection("Banking", "Undeposited Funds failed: " + ex.getMessage());
        }
    }

    private void openDocumentsDirect()
    {
        try
        {
            showAlternatePane(this.bankingPanelFactory.createDocumentsPanel());
            openInspectorForSelection("Banking", "Documents & Attachments opened in alternate shell.");
        }
        catch (Exception ex)
        {
            openInspectorForSelection("Banking", "Documents & Attachments failed: " + ex.getMessage());
        }
    }

    private void openHelpHint()
    {
        Stage owner = getOwningStage();
        showAlternatePane(new HelpPanelFX(owner));
        openInspectorForSelection("Help", "Help content opened in alternate shell.");
    }

    private void openDonorsDirect()
    {
        showAlternatePane(new DonorsPanelFX(new DonorService(), null));
        openInspectorForSelection("Fundraising", "Donors panel opened in alternate shell.");
    }

    private Stage getOwningStage()
    {
        return getScene() != null && getScene().getWindow() instanceof Stage stage ? stage : null;
    }

    private void runNewAction()
    {
        if (!hasActivePanelCommandTarget())
        {
            openInspectorForSelection("Command", "New is unavailable until a panel-host workspace is active.");
            return;
        }
        this.panelHost.newItemActive();
        openInspectorForSelection("Command", "New action sent to active workspace panel: " + this.panelHost.getActiveTitle());
    }

    private void runSaveAction()
    {
        if (!hasActivePanelCommandTarget())
        {
            openInspectorForSelection("Command", "Save is unavailable until a panel-host workspace is active.");
            return;
        }
        this.panelHost.saveActive();
        openInspectorForSelection("Command", "Save action sent to active workspace panel: " + this.panelHost.getActiveTitle());
    }

    private void runDeleteAction()
    {
        if (!hasActivePanelCommandTarget())
        {
            openInspectorForSelection("Command", "Delete is unavailable until a panel-host workspace is active.");
            return;
        }
        this.panelHost.deleteActive();
        openInspectorForSelection("Command", "Delete action sent to active workspace panel: " + this.panelHost.getActiveTitle());
    }

    private void runCancelAction()
    {
        if (!hasActivePanelCommandTarget())
        {
            openInspectorForSelection("Command", "Cancel is unavailable until a panel-host workspace is active.");
            return;
        }
        this.panelHost.cancelActive();
        openInspectorForSelection("Command", "Cancel action sent to active workspace panel: " + this.panelHost.getActiveTitle());
    }

    private boolean hasActivePanelCommandTarget()
    {
        return this.workspaceRouter.decide(this.activePanelId).isPanelHost();
    }

    private Button actionButton(String label, Runnable action)
    {
        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> action.run());
        return button;
    }

    private void openProfilePage()
    {
        showAlternatePane(buildProfilePane());
    }

    private void openSearchPage()
    {
        showAlternatePane(buildSearchPane());
    }

    private void openCommandCenter()
    {
        showAlternatePane(buildCommandCenterPane());
    }

    private void openDatabaseSelector()
    {
        showAlternatePane(buildDatabaseSelectorPane());
    }

    private void openH2RecoveryTool()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Corrupted H2 Database to Recover");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("H2 Database (*.mv.db)", "*.mv.db"),
            new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selected = chooser.showOpenDialog(getOwningStage());
        if (selected == null)
        {
            return;
        }
        Path basePath = this.contextService.normalizeH2Base(selected.toPath());
        try
        {
            H2SchemaMigrator.RepairResult repairResult = H2SchemaMigrator.repairCorruptedDatabase(basePath);
            String message = "H2 recovery completed: " + basePath.toAbsolutePath();
            if (repairResult != null)
            {
                message += "\nRecovery SQL: " + repairResult.recoveryScript().toAbsolutePath();
            }
            openInspectorForSelection("Database", message);
        }
        catch (Exception ex)
        {
            openInspectorForSelection("Database", "H2 recovery failed: " + ex.getMessage());
        }
    }

    private void openCompanySelector()
    {
        showAlternatePane(buildCompanySelectorPane());
    }


    private void showAlternatePane(Node content)
    {
        this.dashboardCanvas.setVisible(false);
        this.dashboardCanvas.setManaged(false);
        this.alternateSettingsPane.setVisible(false);
        this.alternateSettingsPane.setManaged(false);
        this.panelHost.setVisible(false);
        this.panelHost.setManaged(false);
        this.alternateContentPane.setVisible(true);
        this.alternateContentPane.setManaged(true);
        this.alternateContentPane.getChildren().setAll(content);
    }

    private String executeSearchQuery(String value)
    {
        if (value == null || value.isBlank())
        {
            return "Enter a query to search.";
        }
        String query = value.trim();
        openInspectorForSelection("Search", "Query staged for shared command surface:\n" + query);
        return "Query staged for shared command surface: " + query;
    }

    /**
     * Open panel.
     *
     * @param id the id
     */
    private void openPanel(AppPanelId id)
    {
        if (id == AppPanelId.REPORT_LIBRARY)
        {
            id = AppPanelId.REPORTS_WORKSPACE;
        }
        if (this.activePanelId != id)
        {
            String saveMessage = this.panelHost.isActiveDirty()
                ? "Unsaved changes detected. Saving state before leaving " + panelTitle(this.activePanelId) + "..."
                : "Saving state before leaving " + panelTitle(this.activePanelId) + "...";
            this.alternateStatus.setText(saveMessage);
            dismissActiveContext();
        }
        this.activePanelId = id;
        refreshHeaderLabels();
        rebuildNavigationButtons();
        WorkspaceRouteDecision decision = this.workspaceRouter.decide(id);

        boolean dashboard = decision.isDashboard();
        boolean alternateCustomPane = decision.isAlternateCustomPane();
        boolean panelHostBackedPanel = decision.isPanelHost();

        this.dashboardCanvas.setVisible(dashboard);
        this.dashboardCanvas.setManaged(dashboard);
        this.alternateSettingsPane.setVisible(id == AppPanelId.SETTINGS);
        this.alternateSettingsPane.setManaged(id == AppPanelId.SETTINGS);
        this.alternateContentPane.setVisible(alternateCustomPane && id != AppPanelId.SETTINGS);
        this.alternateContentPane.setManaged(alternateCustomPane && id != AppPanelId.SETTINGS);
        this.panelHost.setVisible(panelHostBackedPanel);
        this.panelHost.setManaged(panelHostBackedPanel);

        if (id == AppPanelId.SETTINGS)
        {
            buildAlternateSettingsPane();
        }
        else if (id == AppPanelId.BUDGET_EDITOR)
        {
            buildAlternateBudgetEditorPane();
        }
        else if (id == AppPanelId.SCHEDULES)
        {
            buildAlternateSchedulesPane();
        }
        else if (alternateCustomPane)
        {
            this.alternateContentPane.getChildren().setAll(new Label("Template pending"));
        }
        else if (panelHostBackedPanel)
        {
            this.panelHost.show(id);
            this.activeAdaptedPanel = null;
            LOGGER.debug("Panel strategy {} ({}) for {}", PanelAdaptationPlan.strategyFor(id), PanelAdaptationPlan.phaseFor(id), id);
        }
        this.nav.highlight(id);
    }

    private void buildAlternateBudgetEditorPane()
    {
        Label title = new Label("Budget Editor");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700;");
        Label subtitle = new Label("Native-first budget workspace using journal-style entry flow.");
        subtitle.setStyle("-fx-text-fill: #5c6482;");
        Node body;
        try
        {
            JournalEntryWorkspaceFX budgetWorkspace = new JournalEntryWorkspaceFX();
            VBox.setVgrow(budgetWorkspace, Priority.ALWAYS);
            body = budgetWorkspace;
        }
        catch (IllegalStateException missingContext)
        {
            Label contextRequired = new Label("Open a company to use the budget editor.");
            contextRequired.setStyle("-fx-text-fill: #8a4f4f;");
            body = contextRequired;
        }
        VBox content = new VBox(8, title, subtitle, body);
        content.setPadding(new Insets(8));
        this.alternateBudgetPane.setCenter(content);
        this.alternateContentPane.getChildren().setAll(this.alternateBudgetPane);
    }

    private void buildAlternateSchedulesPane()
    {
        Label title = new Label("Schedules");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700;");
        Label subtitle = new Label("Scheduled reports and recurring tasks.");
        subtitle.setStyle("-fx-text-fill: #5c6482;");
        ListView<String> schedules = new ListView<>();
        schedules.getItems().setAll(this.alternatePreferences.get(SCHEDULED_REPORTS_KEY, "")
            .lines()
            .filter(line -> line != null && !line.isBlank())
            .toList());
        Button createSchedule = new Button("Create Schedule");
        createSchedule.setOnAction(e -> openReportsScheduleDirect());
        this.alternateSchedulesPane.getChildren().setAll(title, subtitle, createSchedule, schedules);
        this.alternateSchedulesPane.setPadding(new Insets(8));
        VBox.setVgrow(schedules, Priority.ALWAYS);
        this.alternateContentPane.getChildren().setAll(this.alternateSchedulesPane);
    }

    private void refreshIconBarState()
    {
        boolean companyLoaded = this.sessionContext.isCompanyOpen();
        for (Button iconButton : this.iconRailButtons)
        {
            iconButton.setDisable(!companyLoaded);
            iconButton.setOpacity(companyLoaded ? 1.0 : 0.55);
        }
    }

    private void rebuildNavigationButtons()
    {
        this.navButtons.getChildren().clear();
        boolean databaseOpen = this.sessionContext.isDatabaseOpen();
        boolean companyOpen = this.sessionContext.isCompanyOpen();

        if (!databaseOpen)
        {
            this.navButtons.getChildren().addAll(
                navActionButton("🗄  Open Database", this::openDatabaseSelector),
                navButton("⚙  Settings", AppPanelId.SETTINGS));
            this.importToolsPane.setVisible(false);
            this.importToolsPane.setManaged(false);
            return;
        }
        if (!companyOpen)
        {
            this.navButtons.getChildren().addAll(
                navActionButton("🗄  Open Database", this::openDatabaseSelector),
                navActionButton("🏢  Open Company", this::openCompanySelector),
                navButton("⚙  Settings", AppPanelId.SETTINGS));
            this.importToolsPane.setVisible(false);
            this.importToolsPane.setManaged(false);
            return;
        }

        AppPanelId parentPanel = this.navigationModel.parentPanelFor(this.activePanelId);
        this.navButtons.getChildren().addAll(
            navButton("⌂  " + panelTitle(parentPanel), parentPanel));

        for (AppPanelId child : this.navigationModel.subPanelsFor(parentPanel))
        {
            this.navButtons.getChildren().add(navButton("↳  " + panelTitle(child), child));
        }

        this.navButtons.getChildren().addAll(
            navButton("⚙  Settings", AppPanelId.SETTINGS),
            navActionButton("🗄  Open Database", this::openDatabaseSelector),
            navActionButton("🏢  Open Company", this::openCompanySelector));
        this.importToolsPane.setVisible(true);
        this.importToolsPane.setManaged(true);
        activateLeftToolbarButtons();
    }


    private void activateLeftToolbarButtons()
    {
        this.navButtons.getChildren().stream()
            .filter(Button.class::isInstance)
            .map(Button.class::cast)
            .forEach(button ->
            {
                button.setDisable(false);
                button.setOpacity(1.0);
            });

        if (this.importToolsPane.getContent() instanceof VBox toolsContent)
        {
            toolsContent.getChildren().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .forEach(button ->
                {
                    button.setDisable(false);
                    button.setOpacity(1.0);
                });
        }
    }

    private void refreshHeaderLabels()
    {
        this.headerTitle.setText(panelTitle(this.activePanelId));
        refreshIconBarState();
    }

    private String panelTitle(AppPanelId panelId)
    {
        return switch (panelId)
        {
            case DASHBOARD -> "Dashboard";
            case CHART_OF_ACCOUNTS -> "Chart of Accounts";
            case LEDGER_REGISTER -> "Journal";
            case INVENTORY -> "Inventory";
            case FUNDS -> "Funds";
            case REPORTS_WORKSPACE -> "Reports Library";
            case SCHEDULES -> "Schedules";
            case BUDGET_EDITOR -> "Budget";
            case SETTINGS -> "Settings";
            default -> panelId.name().replace('_', ' ');
        };
    }

    private void openInspectorForSelection(String title, String body)
    {
        this.alternateStatus.setText(title + "\n" + body);
    }

    private void dismissActiveContext()
    {
        if (this.activeAdaptedPanel != null)
        {
            this.activeAdaptedPanel.onLeave();
            this.activeAdaptedPanel.saveContext();
            this.activeAdaptedPanel = null;
        }
        this.panelHost.saveActive();
    }

    private void openRecordServicePanel(nonprofitbookkeeping.ui.RecordServicePanelRegistry.PanelBinding binding)
    {
        dismissActiveContext();
        if (binding.workspacePanelId() != null)
        {
            openPanel(binding.workspacePanelId());
            return;
        }
        AppPanel panel = binding.panelFactory().get();
        this.activeAdaptedPanel = LegacyPanelAdapter.from(panel);
        openInspectorForSelection(binding.displayName(), panel.title() + " opened in alternate shell.");
        this.activeAdaptedPanel.onEnter();
        showAlternatePane(this.activeAdaptedPanel.content());
    }

    void testOpenReconcileAccountsDirect()
    {
        openReconcileAccountsDirect();
    }

    void testOpenUndepositedFundsDirect()
    {
        openUndepositedFundsDirect();
    }

    void testOpenDocumentsDirect()
    {
        openDocumentsDirect();
    }


    String testAlternateStatusText()
    {
        return this.alternateStatus.getText();
    }

    void testClearScheduledReports()
    {
        this.alternatePreferences.remove(SCHEDULED_REPORTS_KEY);
    }

    void testSaveScheduledReport(String entry)
    {
        saveScheduledReport(entry);
    }

    String testScheduledReportsValue()
    {
        return this.alternatePreferences.get(SCHEDULED_REPORTS_KEY, "");
    }

    void testOpenPanel(AppPanelId id)
    {
        openPanel(id);
    }

    List<String> testNavigationButtonLabels()
    {
        return this.navButtons.getChildren().stream()
            .filter(Button.class::isInstance)
            .map(Button.class::cast)
            .map(Button::getText)
            .collect(Collectors.toList());
    }

    String testHeaderTitle()
    {
        return this.headerTitle.getText();
    }

    String testHeaderSubtitle()
    {
        return this.headerSubtitle.getText();
    }

    List<Boolean> testIconRailDisabledStates()
    {
        return this.iconRailButtons.stream()
            .map(Button::isDisable)
            .toList();
    }

}
