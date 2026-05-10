package org.nonprofitbookkeeping.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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

import nonprofitbookkeeping.ui.actions.ExcelTemplateReportActionFX;
import nonprofitbookkeeping.ui.actions.GenerateIncomeStatementAction;
import nonprofitbookkeeping.ui.actions.GenerateBalanceSheetAction;
import nonprofitbookkeeping.ui.actions.GenerateTrialBalanceAction;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.panels.HelpPanelFX;
import nonprofitbookkeeping.ui.panels.LedgerReconcilePanelFX;
import nonprofitbookkeeping.service.ReconciliationService;
import nonprofitbookkeeping.service.UndepositedFundsService;
import nonprofitbookkeeping.service.DocumentStorageService;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.ui.panels.UndepositedFundsPanelFX;
import nonprofitbookkeeping.ui.panels.DocumentsPanelFX;
import nonprofitbookkeeping.ui.panels.DonorsPanelFX;
import nonprofitbookkeeping.ui.panels.DonationsPanelFX;
import nonprofitbookkeeping.ui.panels.GrantsPanelFX;
import nonprofitbookkeeping.service.DonorService;
import nonprofitbookkeeping.service.GrantsService;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.nonprofitbookkeeping.ui.routing.WorkspaceRouteDecision;
import org.nonprofitbookkeeping.ui.routing.WorkspaceRouter;

/**
 * Alternate dashboard-first UI shell that preserves current panel APIs.
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
    private final VBox databaseSelectorPane = new VBox();
    private final VBox companySelectorPane = new VBox();
    private final VBox profilePane = new VBox();
    private final VBox searchPane = new VBox();
    private final VBox navButtons = new VBox(6);
    private final TitledPane importToolsPane = new TitledPane();
    private final WorkspaceRouter workspaceRouter = new WorkspaceRouter();
    private final BankingPanelFactory bankingPanelFactory;
    private final AlternateDataContextService contextService = new AlternateDataContextService();
    private final Label headerTitle = new Label("Dashboard");
    private final Label headerSubtitle = new Label("No company open");
    private final AlternateNavigationModel navigationModel = new AlternateNavigationModel();
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
        this.bankingPanelFactory = bankingPanelFactory;
        setTop(buildHeader());
        setCenter(buildWorkspace());
        setLeft(buildIconRail());
        setPadding(new Insets(10));
        setBackground(new Background(new BackgroundFill(SURFACE_COLORS.get("Slate"), CornerRadii.EMPTY, Insets.EMPTY)));
        openPanel(AppPanelId.DASHBOARD);
    }

    private Node buildIconRail()
    {
        VBox rail = new VBox(14,
            iconButton("◉", this::openProfilePage),
            iconButton("⌂", () -> openPanel(AppPanelId.DASHBOARD)),
            iconButton("⌕", this::openSearchPage),
            iconButton("☰", this::openCommandCenter),
            iconButton("⚙", () -> openPanel(AppPanelId.SETTINGS)));
        rail.setPadding(new Insets(14, 8, 14, 8));
        rail.setStyle("-fx-background-color: #1f2431; -fx-background-radius: 14;");
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
        headerTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: 700;");
        headerSubtitle.setStyle("-fx-text-fill: #5c6482;");

        VBox heading = new VBox(2, headerTitle, headerSubtitle);
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

        dashboardCanvas.getChildren().setAll(buildDashboardCards());
        dashboardCanvas.setSpacing(12);

        panelHost.setVisible(false);
        panelHost.setManaged(false);

        alternateSettingsPane.setVisible(false);
        alternateSettingsPane.setManaged(false);
        alternateContentPane.setVisible(false);
        alternateContentPane.setManaged(false);
        workspaceSurface.getChildren().setAll(dashboardCanvas, alternateSettingsPane, alternateContentPane, panelHost);
        StackPane.setMargin(dashboardCanvas, new Insets(8));
        StackPane.setMargin(panelHost, new Insets(8));
        HBox.setHgrow(workspaceSurface, Priority.ALWAYS);
        workspaceSurface.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 18;");

        HBox body = new HBox(12, leftNav, workspaceSurface);
        body.setAlignment(Pos.TOP_LEFT);
        return body;
    }

    private VBox buildLeftNavigation()
    {
        importToolsPane.setText("Import & Tools");
        importToolsPane.setContent(new VBox(6,
            navButton("Assets Register", AppPanelId.ASSETS_REGISTER),
            navButton("Budget vs Actual", AppPanelId.BUDGET_VS_ACTUAL),
            navButton("Depreciation", AppPanelId.DEPRECIATION_RUNS)));
        importToolsPane.setExpanded(false);
        rebuildNavigationButtons();

        VBox wrapper = new VBox(10, new Label("Navigation"), navButtons, importToolsPane);
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
        grid.add(buildCard("Receivables", "$11,230", "+8.2%"), 0, 0);
        grid.add(buildCard("Payables", "$5,830", "-1.1%"), 1, 0);
        grid.add(buildCard("Profit & Loss", "$23,009", "+14.5%"), 2, 0);
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
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Jan", 9));
        series.getData().add(new XYChart.Data<>("Feb", 12));
        series.getData().add(new XYChart.Data<>("Mar", 11));
        series.getData().add(new XYChart.Data<>("Apr", 15));
        chart.getData().add(series);

        VBox box = new VBox(8, new Label("Cash Flow"), chart);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return box;
    }

    private VBox buildBalancesCard()
    {
        VBox list = new VBox(6,
            new Label("Operating  ·  $12,004"),
            new Label("Payroll      ·  $3,420"),
            new Label("Savings      ·  $7,230"),
            new Label("Undeposited  ·  $980"));
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

        alternateStatus.setWrapText(true);

        alternateSettingsPane.getChildren().setAll(
            new Label("Alternate View Settings"),
            new Separator(),
            new Label("Background"),
            backgroundChoice,
            new Label("Card Corners"),
            roundingChoice,
            new Label("Status"),
            alternateStatus,
            new Separator(),
            new Label("Custom Fields / Localization / Expenses / Bank placeholders added in alternate templates."));
        alternateSettingsPane.setSpacing(8);
        alternateSettingsPane.setPadding(new Insets(12));
        alternateSettingsPane.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return alternateSettingsPane;
    }



    private VBox buildDatabaseSelectorPane()
    {
        TextField dbPath = new TextField();
        dbPath.setPromptText("/path/to/file.mv.db");
        Path activeDatabaseBasePath = contextService.activeDatabaseBasePath();
        if (activeDatabaseBasePath != null)
        {
            dbPath.setText(activeDatabaseBasePath.toString());
        }
        ListView<String> recent = new ListView<>(FXCollections.observableArrayList(contextService.recentDatabasePaths()));
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
                alternateStatus.setText("No database selected.");
                state.setText("No database selected.");
                return;
            }
            try
            {
                contextService.openDatabase(Paths.get(value.trim()));
                String openedMessage = "Database opened: " + value.trim();
                alternateStatus.setText(openedMessage);
                state.setText(openedMessage);
                refreshHeaderLabels();
                openPanel(AppPanelId.DASHBOARD);
            }
            catch (Exception ex)
            {
                String failedMessage = "Failed to open database: " + ex.getMessage();
                alternateStatus.setText(failedMessage);
                state.setText(failedMessage);
            }
        });
        databaseSelectorPane.getChildren().setAll(new Label("Open Database (.mv.db/.db)"), new HBox(8, dbPath, browse), new Label("Recent Databases"), recent, open, state);
        databaseSelectorPane.setPadding(new Insets(12));
        databaseSelectorPane.setSpacing(10);
        return databaseSelectorPane;
    }

    private VBox buildCompanySelectorPane()
    {
        Map<String, Long> companiesByName = new LinkedHashMap<>();
        try
        {
            for (var record : contextService.listCompanies())
            {
                companiesByName.put(record.name() + " (ID: " + record.id() + ")", record.id());
            }
        }
        catch (Exception ex)
        {
            alternateStatus.setText("Failed to load companies: " + ex.getMessage());
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
            recent.setItems(FXCollections.observableArrayList(contextService.recentCompanies().stream()
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
                alternateStatus.setText("No company selected.");
                state.setText("No company selected.");
                return;
            }
            try
            {
                contextService.openCompany(companyId, selected);
                String openedMessage = "Company opened: " + selected;
                alternateStatus.setText(openedMessage);
                state.setText(openedMessage);
                refreshHeaderLabels();
                openPanel(AppPanelId.DASHBOARD);
            }
            catch (Exception ex)
            {
                String failedMessage = "Failed to open company: " + ex.getMessage();
                alternateStatus.setText(failedMessage);
                state.setText(failedMessage);
            }
        });
        companySelectorPane.getChildren().setAll(new Label("Open Company"), companies, new Label("Recent Companies"), recent, open, state);
        companySelectorPane.setPadding(new Insets(12));
        companySelectorPane.setSpacing(10);
        return companySelectorPane;
    }

    private VBox buildProfilePane()
    {
        Label title = new Label("User Profile");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700;");
        profilePane.getChildren().setAll(
            title,
            new Separator(),
            new Label("Signed in user"),
            new Label("Role: Accountant"),
            new Label("Preferences and account details will be wired in a later phase."));
        profilePane.setPadding(new Insets(12));
        profilePane.setSpacing(10);
        profilePane.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return profilePane;
    }

    private VBox buildSearchPane()
    {
        TextField query = new TextField();
        query.setPromptText("Search accounts, transactions, reports...");
        Button search = new Button("Search");
        Label status = new Label("Enter a query to search.");
        search.setOnAction(e -> status.setText(executeSearchQuery(query.getText())));
        searchPane.getChildren().setAll(
            new Label("Search"),
            new Separator(),
            query,
            search,
            status);
        searchPane.setPadding(new Insets(12));
        searchPane.setSpacing(10);
        searchPane.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return searchPane;
    }


    private VBox buildCommandCenterPane()
    {
        VBox fileGroup = new VBox(6,
            new Label("File"),
            actionButton("Open Database", this::openDatabaseSelector),
            actionButton("Open Company", this::openCompanySelector));

        VBox runGroup = new VBox(6,
            new Label("Run"),
            actionButton("Chart of Accounts", () -> openPanel(AppPanelId.CHART_OF_ACCOUNTS)),
            actionButton("Journal", () -> openPanel(AppPanelId.LEDGER_REGISTER)),
            actionButton("Inventory", () -> openPanel(AppPanelId.INVENTORY)),
            actionButton("Reports Workspace", () -> openPanel(AppPanelId.REPORTS_WORKSPACE)));

        VBox reportActions = new VBox(6,
            new Label("Reports actions"),
            actionButton("Print Income Statement", this::printIncomeStatementDirect),
            actionButton("Print Balance Sheet", this::printBalanceSheetDirect),
            actionButton("Print Trial Balance", this::printTrialBalanceDirect),
            actionButton("Export", this::openReportsWorkspaceWithExportHint),
            actionButton("Schedule", this::openReportsScheduleDirect));

        Button newButton = actionButton("New", this::runNewAction);
        Button saveButton = actionButton("Save", this::runSaveAction);
        boolean panelCommandAvailable = hasActivePanelCommandTarget();
        newButton.setDisable(!panelCommandAvailable);
        saveButton.setDisable(!panelCommandAvailable);

        VBox quickActions = new VBox(6,
            new Label("Toolbar-style actions"),
            newButton,
            saveButton,
            actionButton("Find", this::openSearchPage),
            actionButton("Journal", () -> openPanel(AppPanelId.LEDGER_REGISTER)));

        VBox fundraisingGroup = new VBox(6,
            new Label("Fundraising"),
            actionButton("Donors", this::openDonorsDirect),
            actionButton("Donations", this::openDonationsDirect),
            actionButton("Grants", this::openGrantsDirect),
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


    private void printIncomeStatementDirect()
    {
        try
        {
            new GenerateIncomeStatementAction(new ReportService()).actionPerformed(null);
            openInspectorForSelection("Reports", "Printed Income Statement.");
        }
        catch (Exception ex)
        {
            openInspectorForSelection("Reports", "Income Statement print failed: " + ex.getMessage());
        }
    }

    private void printBalanceSheetDirect()
    {
        try
        {
            new GenerateBalanceSheetAction(new ReportService()).actionPerformed(null);
            openInspectorForSelection("Reports", "Printed Balance Sheet.");
        }
        catch (Exception ex)
        {
            openInspectorForSelection("Reports", "Balance Sheet print failed: " + ex.getMessage());
        }
    }

    private void printTrialBalanceDirect()
    {
        try
        {
            new GenerateTrialBalanceAction(new ReportService()).actionPerformed(null);
            openInspectorForSelection("Reports", "Printed Trial Balance.");
        }
        catch (Exception ex)
        {
            openInspectorForSelection("Reports", "Trial Balance print failed: " + ex.getMessage());
        }
    }

    private void openReportsWorkspaceWithExportHint()
    {
        Stage owner = getOwningStage();
        if (owner == null)
        {
            openInspectorForSelection("Reports", "Export action requires an active window; open Reports workspace and try again.");
            return;
        }
        try
        {
            new ExcelTemplateReportActionFX(owner).handle(null);
            openInspectorForSelection("Reports", "Export action launched via Excel template report workflow.");
        }
        catch (Exception ex)
        {
            openInspectorForSelection("Reports", "Export action failed: " + ex.getMessage());
        }
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
        String existing = alternatePreferences.get(SCHEDULED_REPORTS_KEY, "");
        String updated = existing == null || existing.isBlank() ? entry : entry + "\n" + existing;
        alternatePreferences.put(SCHEDULED_REPORTS_KEY, updated);
    }

    void saveScheduledReportForTest(String entry)
    {
        saveScheduledReport(entry);
    }

    void clearScheduledReportsForTest()
    {
        alternatePreferences.remove(SCHEDULED_REPORTS_KEY);
    }

    String scheduledReportsSnapshotForTest()
    {
        return alternatePreferences.get(SCHEDULED_REPORTS_KEY, "");
    }

    void triggerReportExportActionForTest()
    {
        openReportsWorkspaceWithExportHint();
    }

    String alternateStatusTextForTest()
    {
        return alternateStatus.getText();
    }

    private void openReconcileAccountsDirect()
    {
        try
        {
            showAlternatePane(bankingPanelFactory.createReconcilePanel());
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
            showAlternatePane(bankingPanelFactory.createUndepositedFundsPanel());
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
            showAlternatePane(bankingPanelFactory.createDocumentsPanel());
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

    private void openDonationsDirect()
    {
        Stage owner = getOwningStage();
        showAlternatePane(new DonationsPanelFX(owner));
        openInspectorForSelection("Fundraising", "Donations panel opened in alternate shell.");
    }

    private void openGrantsDirect()
    {
        showAlternatePane(new GrantsPanelFX(new GrantsService()));
        openInspectorForSelection("Fundraising", "Grants panel opened in alternate shell.");
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
        panelHost.newItemActive();
        openInspectorForSelection("Command", "New action sent to active workspace panel: " + panelHost.getActiveTitle());
    }

    private void runSaveAction()
    {
        if (!hasActivePanelCommandTarget())
        {
            openInspectorForSelection("Command", "Save is unavailable until a panel-host workspace is active.");
            return;
        }
        panelHost.saveActive();
        openInspectorForSelection("Command", "Save action sent to active workspace panel: " + panelHost.getActiveTitle());
    }

    private boolean hasActivePanelCommandTarget()
    {
        return workspaceRouter.decide(activePanelId).isPanelHost();
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

    private void openCompanySelector()
    {
        showAlternatePane(buildCompanySelectorPane());
    }


    private void showAlternatePane(Node content)
    {
        dashboardCanvas.setVisible(false);
        dashboardCanvas.setManaged(false);
        alternateSettingsPane.setVisible(false);
        alternateSettingsPane.setManaged(false);
        panelHost.setVisible(false);
        panelHost.setManaged(false);
        alternateContentPane.setVisible(true);
        alternateContentPane.setManaged(true);
        alternateContentPane.getChildren().setAll(content);
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

    private void openPanel(AppPanelId id)
    {
        if (activePanelId != id)
        {
            String saveMessage = panelHost.isActiveDirty()
                ? "Unsaved changes detected. Saving state before leaving " + panelTitle(activePanelId) + "..."
                : "Saving state before leaving " + panelTitle(activePanelId) + "...";
            alternateStatus.setText(saveMessage);
            if (activeAdaptedPanel != null)
            {
                activeAdaptedPanel.onLeave();
                activeAdaptedPanel.saveContext();
                activeAdaptedPanel = null;
            }
            panelHost.saveActive();
        }
        activePanelId = id;
        refreshHeaderLabels();
        rebuildNavigationButtons();
        WorkspaceRouteDecision decision = workspaceRouter.decide(id);

        boolean dashboard = decision.isDashboard();
        boolean alternateCustomPane = decision.isAlternateCustomPane();
        boolean panelHostBackedPanel = decision.isPanelHost();

        dashboardCanvas.setVisible(dashboard);
        dashboardCanvas.setManaged(dashboard);
        alternateSettingsPane.setVisible(id == AppPanelId.SETTINGS);
        alternateSettingsPane.setManaged(id == AppPanelId.SETTINGS);
        alternateContentPane.setVisible(alternateCustomPane && id != AppPanelId.SETTINGS);
        alternateContentPane.setManaged(alternateCustomPane && id != AppPanelId.SETTINGS);
        panelHost.setVisible(panelHostBackedPanel);
        panelHost.setManaged(panelHostBackedPanel);

        if (id == AppPanelId.SETTINGS)
        {
            buildAlternateSettingsPane();
        }
        else if (alternateCustomPane)
        {
            if (id == AppPanelId.REPORTS_WORKSPACE)
            {
                alternateContentPane.getChildren().setAll(new Label("Reports workspace adapted for alternate shell."));
                openInspectorForSelection("Reports", "Reports workspace opened with adapted navigation context.");
            }
            else
            {
                alternateContentPane.getChildren().setAll(new Label("Template pending"));
            }
        }
        else if (panelHostBackedPanel)
        {
            panelHost.show(id);
            activeAdaptedPanel = null;
            LOGGER.debug("Panel strategy {} ({}) for {}", PanelAdaptationPlan.strategyFor(id), PanelAdaptationPlan.phaseFor(id), id);
        }
        nav.highlight(id);
    }

    private void rebuildNavigationButtons()
    {
        navButtons.getChildren().clear();
        boolean databaseOpen = contextService.activeDatabaseBasePath() != null;
        boolean companyOpen = CurrentCompany.isOpen();

        if (!databaseOpen)
        {
            navButtons.getChildren().addAll(
                navActionButton("🗄  Open Database", this::openDatabaseSelector),
                navButton("⚙  Settings", AppPanelId.SETTINGS));
            importToolsPane.setVisible(false);
            importToolsPane.setManaged(false);
            return;
        }
        if (!companyOpen)
        {
            navButtons.getChildren().addAll(
                navActionButton("🗄  Open Database", this::openDatabaseSelector),
                navActionButton("🏢  Open Company", this::openCompanySelector),
                navButton("⚙  Settings", AppPanelId.SETTINGS));
            importToolsPane.setVisible(false);
            importToolsPane.setManaged(false);
            return;
        }

        AppPanelId parentPanel = navigationModel.parentPanelFor(activePanelId);
        navButtons.getChildren().addAll(
            navButton("⌂  " + panelTitle(parentPanel), parentPanel));

        for (AppPanelId child : navigationModel.subPanelsFor(parentPanel))
        {
            navButtons.getChildren().add(navButton("↳  " + panelTitle(child), child));
        }

        navButtons.getChildren().addAll(
            navButton("⚙  Settings", AppPanelId.SETTINGS),
            navActionButton("🗄  Open Database", this::openDatabaseSelector),
            navActionButton("🏢  Open Company", this::openCompanySelector));
        importToolsPane.setVisible(true);
        importToolsPane.setManaged(true);
    }

    private void refreshHeaderLabels()
    {
        headerTitle.setText(panelTitle(activePanelId));
        headerSubtitle.setText(activeCompanyName());
    }

    private String activeCompanyName()
    {
        Company company = CurrentCompany.getCompany();
        if (!CurrentCompany.isOpen() || company == null || company.getName() == null || company.getName().isBlank())
        {
            return "No company open";
        }
        return company.getName();
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
            case REPORTS_WORKSPACE -> "Reports";
            case SCHEDULES -> "Schedules";
            case BUDGET_EDITOR -> "Budget";
            case SETTINGS -> "Settings";
            default -> panelId.name().replace('_', ' ');
        };
    }

    private void openInspectorForSelection(String title, String body)
    {
        alternateStatus.setText(title + "\n" + body);
    }

    private void dismissActiveContext()
    {
        if (activeAdaptedPanel != null)
        {
            activeAdaptedPanel.onLeave();
            activeAdaptedPanel.saveContext();
            activeAdaptedPanel = null;
        }
        panelHost.saveActive();
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
        activeAdaptedPanel = LegacyPanelAdapter.from(panel);
        openInspectorForSelection(binding.displayName(), panel.title() + " opened in alternate shell.");
        activeAdaptedPanel.onEnter();
        showAlternatePane(activeAdaptedPanel.content());
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
        return alternateStatus.getText();
    }

    void testClearScheduledReports()
    {
        alternatePreferences.remove(SCHEDULED_REPORTS_KEY);
    }

    void testSaveScheduledReport(String entry)
    {
        saveScheduledReport(entry);
    }

    String testScheduledReportsValue()
    {
        return alternatePreferences.get(SCHEDULED_REPORTS_KEY, "");
    }

    void testOpenPanel(AppPanelId id)
    {
        openPanel(id);
    }

    List<String> testNavigationButtonLabels()
    {
        return navButtons.getChildren().stream()
            .filter(Button.class::isInstance)
            .map(Button.class::cast)
            .map(Button::getText)
            .collect(Collectors.toList());
    }

    String testHeaderTitle()
    {
        return headerTitle.getText();
    }

    String testHeaderSubtitle()
    {
        return headerSubtitle.getText();
    }

}
