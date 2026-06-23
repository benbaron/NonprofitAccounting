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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
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
import nonprofitbookkeeping.service.UndepositedFundsService;
import nonprofitbookkeeping.service.DocumentStorageService;
import nonprofitbookkeeping.ui.panels.UndepositedFundsPanelFX;
import nonprofitbookkeeping.ui.panels.DocumentsPanelFX;
import nonprofitbookkeeping.ui.panels.JournalEntryWorkspaceFX;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
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
    static final String NO_SERVICE_DATA_MESSAGE =
        "No service-backed data source is wired for this panel yet.";
    private static final Map<String, Color> SURFACE_COLORS = Map.of(
        "Slate", Color.web("#eef1f8"),
        "Warm", Color.web("#f7f2ee"),
        "Cool", Color.web("#edf5f9"));

    private final PanelHost panelHost;
    private final Label alternateStatus = new Label("Select an item to see context details.");
    private final AlternateDashboardPanel alternateDashboardPanel;
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
    private final SplitPane workspaceSplitPane = new SplitPane();
    private final TitledPane importToolsPane = new TitledPane();
    private final WorkspaceRouter workspaceRouter = new WorkspaceRouter();
    private final BankingPanelFactory bankingPanelFactory;
    private final AlternateDataContextService contextService;
    private final UiSessionContext sessionContext;
    private final Label headerTitle = new Label("Dashboard");
    private final Label headerSubtitle = new Label("No company open");
    private final AlternateNavigationModel navigationModel = new AlternateNavigationModel();
    private final AlternateUiCommandCatalog commandCatalog;
    private final UiServiceProvider uiServices;
    private LegacyPanelAdapter.AdaptedPanel activeAdaptedPanel;
    private AppPanelId activePanelId = AppPanelId.DASHBOARD;
    private static final String SCHEDULED_REPORTS_KEY = "alternate.scheduled.reports";
    private final Preferences alternatePreferences = Preferences.userNodeForPackage(MainWindowAlternate.class);


    interface BankingPanelFactory
    {
        Node createUndepositedFundsPanel();

        Node createDocumentsPanel();
    }

    static class DefaultBankingPanelFactory implements BankingPanelFactory
    {
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
        this.uiServices = new UiServiceProvider(contextService);
        this.panelHost = new PanelHost(this.uiServices);
        this.alternateDashboardPanel = new AlternateDashboardPanel(this.sessionContext, this.uiServices);
        this.commandCatalog = new AlternateUiCommandCatalog(this.sessionContext);
        this.sessionContext.companyOpenProperty().addListener((obs, oldValue, newValue) -> rebuildNavigationButtons());
        this.sessionContext.databaseOpenProperty().addListener((obs, oldValue, newValue) -> rebuildNavigationButtons());
        setTop(buildHeader());
        setCenter(buildWorkspace());
        setPadding(new Insets(10));
        getStyleClass().add("alternate-shell-root");
        setBackground(new Background(new BackgroundFill(SURFACE_COLORS.get("Slate"), CornerRadii.EMPTY, Insets.EMPTY)));
        openPanel(AppPanelId.DASHBOARD);
    }

    private Node buildHeader()
    {
        this.headerTitle.getStyleClass().add("alternate-shell-title");
        this.headerSubtitle.getStyleClass().add("alternate-shell-subtitle");
        this.headerSubtitle.textProperty().bind(this.sessionContext.sessionDisplayLabelProperty());

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

        this.panelHost.setVisible(false);
        this.panelHost.setManaged(false);

        this.alternateSettingsPane.setVisible(false);
        this.alternateSettingsPane.setManaged(false);
        this.alternateContentPane.setVisible(false);
        this.alternateContentPane.setManaged(false);
        this.workspaceSurface.getChildren().setAll(this.alternateDashboardPanel.root(), this.alternateSettingsPane, this.alternateContentPane, this.panelHost);
        StackPane.setMargin(this.alternateDashboardPanel.root(), new Insets(8));
        StackPane.setMargin(this.panelHost, new Insets(8));
        HBox.setHgrow(this.workspaceSurface, Priority.ALWAYS);
        this.workspaceSurface.getStyleClass().add("alternate-workspace-surface");

        this.workspaceSplitPane.getItems().setAll(leftNav, this.workspaceSurface);
        this.workspaceSplitPane.setDividerPositions(0.25);
        this.workspaceSplitPane.getStyleClass().add("alternate-workspace-split");
        return this.workspaceSplitPane;
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

        VBox quickActions = new VBox(6,
            navActionButton("◉  Profile", this::openProfilePage),
            navButton("⌂  Dashboard", AppPanelId.DASHBOARD),
            navActionButton("⌕  Search", this::openSearchPage),
            navActionButton("☰  Command Center", this::openCommandCenter));
        quickActions.getStyleClass().add("alternate-left-navigation-actions");

        VBox wrapper = new VBox(10, new Label("Navigation"), quickActions, new Separator(), this.navButtons, this.importToolsPane);
        wrapper.setPadding(new Insets(12));
        wrapper.setMinWidth(240);
        wrapper.getStyleClass().add("alternate-left-navigation");
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
            new Label("Custom fields, localization, expenses, and bank settings are not wired yet."));
        this.alternateSettingsPane.setSpacing(8);
        this.alternateSettingsPane.setPadding(new Insets(12));
        this.alternateSettingsPane.getStyleClass().add("alternate-content-card");
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
        title.getStyleClass().add("alternate-panel-title");
        this.profilePane.getChildren().setAll(
            title,
            new Separator(),
            new Label("No signed-in user service is wired yet."),
            new Label("No role or permissions service is wired yet."),
            new Label("Preferences and account details will be wired in a later phase."));
        this.profilePane.setPadding(new Insets(12));
        this.profilePane.setSpacing(10);
        this.profilePane.getStyleClass().add("alternate-content-card");
        return this.profilePane;
    }

    private VBox buildSearchPane()
    {
        TextField query = new TextField();
        query.setPromptText("Search accounts, transactions, funds, donors, reports, companies...");
        Button search = new Button("Search");
        Button openSelected = new Button("Open Selected Result");
        Label status = new Label("Enter a query to search.");
        ListView<GlobalSearchResult> results = new ListView<>();
        results.setPlaceholder(new Label("No search results."));
        results.setCellFactory(list -> new ListCell<>()
        {
            @Override
            protected void updateItem(GlobalSearchResult item, boolean empty)
            {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.type() + " • " + item.title()
                    + (item.subtitle().isBlank() ? "" : "\n" + item.subtitle()));
            }
        });
        Runnable execute = () -> executeSearchQuery(query.getText(), results, status);
        search.setOnAction(e -> execute.run());
        openSelected.setOnAction(e -> openSearchResult(results.getSelectionModel().getSelectedItem()));
        query.setOnAction(e -> execute.run());
        results.setOnMouseClicked(e -> {
            if (e.getClickCount() >= 2)
            {
                openSearchResult(results.getSelectionModel().getSelectedItem());
            }
        });
        results.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER)
            {
                openSearchResult(results.getSelectionModel().getSelectedItem());
            }
        });
        this.searchPane.getChildren().setAll(
            new Label("Search"),
            new Separator(),
            query,
            search,
            openSelected,
            status,
            results);
        VBox.setVgrow(results, Priority.ALWAYS);
        this.searchPane.setPadding(new Insets(12));
        this.searchPane.setSpacing(10);
        this.searchPane.getStyleClass().add("alternate-content-card");
        return this.searchPane;
    }



    private AlternateUiCommandActions commandActions()
    {
        Runnable openDatabaseAdministration = () -> openPanel(AppPanelId.DATABASE_ADMIN);
        return new AlternateUiCommandActions(
            openDatabaseAdministration,
            openDatabaseAdministration,
            openDatabaseAdministration,
            openDatabaseAdministration,
            notImplementedAction("Create Company"),
            notImplementedAction("Destroy/Delete Company"),
            notImplementedAction("Populate Company"),
            notImplementedAction("Create Sample Company"),
            this::openCompanySelector,
            () -> openPanel(AppPanelId.IMPORT_EXPORT),
            () -> openPanel(AppPanelId.IMPORT_EXPORT),
            () -> openPanel(AppPanelId.IMPORT_EXPORT),
            () -> openPanel(AppPanelId.MONTHLY_CLOSE),
            () -> openPanel(AppPanelId.CHART_OF_ACCOUNTS),
            () -> openPanel(AppPanelId.LEDGER_REGISTER),
            () -> openPanel(AppPanelId.INVENTORY),
            () -> openPanel(AppPanelId.REPORTS_WORKSPACE),
            this::runNewAction,
            this::runSaveAction,
            this::runDeleteAction,
            this::runCancelAction,
            this::openSearchPage,
            this::openReportsScheduleDirect,
            this::openDonorsDirect,
            () -> openPanel(AppPanelId.FUNDS),
            () -> openPanel(AppPanelId.RECONCILIATION),
            this::openUndepositedFundsDirect,
            this::openDocumentsDirect,
            this::openHelpHint);
    }

    private VBox buildCommandCenterPane()
    {
        VBox pane = new VBox(10, new Label("Command Center"), new Separator());
        String currentCategory = null;
        VBox group = null;
        for (CommandDescriptor command : this.commandCatalog.commands(commandActions()))
        {
            if (!command.category().equals(currentCategory))
            {
                if (group != null)
                {
                    pane.getChildren().add(group);
                    pane.getChildren().add(new Separator());
                }
                currentCategory = command.category();
                group = new VBox(6, new Label(currentCategory));
            }
            group.getChildren().add(actionButton(command));
        }
        if (group != null)
        {
            pane.getChildren().add(group);
        }
        pane.setPadding(new Insets(12));
        pane.setSpacing(10);
        pane.getStyleClass().add("alternate-content-card");
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


    void openReportsScheduleDirect()
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




    Runnable notImplementedAction(String label)
    {
        return () -> openInspectorForSelection("Not implemented", label + " is not implemented in the alternate UI yet.");
    }

    List<CommandDescriptor> commandDescriptorsForTest()
    {
        return this.commandCatalog.commands(commandActions());
    }

    String alternateStatusTextForTest()
    {
        return this.alternateStatus.getText();
    }

    void openReconcileAccountsDirect()
    {
        openPanel(AppPanelId.RECONCILIATION);
        openInspectorForSelection("Banking", "Native reconciliation workspace opened in alternate shell.");
    }

    void openUndepositedFundsDirect()
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

    void openDocumentsDirect()
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

    void openHelpHint()
    {
        Stage owner = getOwningStage();
        showAlternatePane(new HelpPanelFX(owner));
        openInspectorForSelection("Help", "Help content opened in alternate shell.");
    }

    void openDonorsDirect()
    {
        openPanel(AppPanelId.DONORS);
        openInspectorForSelection("Fundraising", "Native donors workspace opened in alternate shell.");
    }

    private Stage getOwningStage()
    {
        return getScene() != null && getScene().getWindow() instanceof Stage stage ? stage : null;
    }

    void runNewAction()
    {
        if (!hasActivePanelCommandTarget())
        {
            openInspectorForSelection("Command", "New is unavailable until a panel-host workspace is active.");
            return;
        }
        this.panelHost.newItemActive();
        openInspectorForSelection("Command", "New action sent to active workspace panel: " + this.panelHost.getActiveTitle());
    }

    void runSaveAction()
    {
        if (!hasActivePanelCommandTarget())
        {
            openInspectorForSelection("Command", "Save is unavailable until a panel-host workspace is active.");
            return;
        }
        SaveResult result = this.panelHost.saveActive();
        openInspectorForSelection("Command", saveMessageFor(this.panelHost.getActiveTitle(), result));
    }

    void runDeleteAction()
    {
        if (!hasActivePanelCommandTarget())
        {
            openInspectorForSelection("Command", "Delete is unavailable until a panel-host workspace is active.");
            return;
        }
        this.panelHost.deleteActive();
        openInspectorForSelection("Command", "Delete action sent to active workspace panel: " + this.panelHost.getActiveTitle());
    }

    void runCancelAction()
    {
        if (!hasActivePanelCommandTarget())
        {
            openInspectorForSelection("Command", "Cancel is unavailable until a panel-host workspace is active.");
            return;
        }
        this.panelHost.cancelActive();
        openInspectorForSelection("Command", "Cancel action sent to active workspace panel: " + this.panelHost.getActiveTitle());
    }

    private String saveMessageFor(String title, SaveResult result)
    {
        return switch (result.status())
        {
            case SAVED -> "Saved active workspace panel: " + title;
            case NO_CHANGES -> "No changes to save for active workspace panel: " + title;
            case UNSUPPORTED -> "Save is not supported for active workspace panel: " + title;
            case FAILED -> result.message();
        };
    }

    private String navigationSaveMessage(String title, SaveResult result)
    {
        return switch (result.status())
        {
            case SAVED -> "Saved changes before leaving " + title + ".";
            case NO_CHANGES -> "No changes to save before leaving " + title + ".";
            case UNSUPPORTED -> "No save performed before leaving " + title + ": " + result.message();
            case FAILED -> "Cannot leave " + title + ": " + result.message();
        };
    }

    private boolean hasActivePanelCommandTarget()
    {
        return this.workspaceRouter.decide(this.activePanelId).isPanelHost();
    }

    private Button actionButton(String label, Runnable action)
    {
        return actionButton(new CommandDescriptor(label, "Ad hoc", action, CommandAvailability.AVAILABLE, "", null));
    }

    private Button actionButton(CommandDescriptor command)
    {
        Button button = new Button(command.label());
        button.setMaxWidth(Double.MAX_VALUE);
        button.setDisable(!command.executable());
        if (!command.disabledReason().isBlank())
        {
            button.setTooltip(new Tooltip(command.disabledReason()));
            button.setAccessibleHelp(command.disabledReason());
        }
        button.setOnAction(e -> {
            if (command.executable())
            {
                command.action().run();
            }
            else
            {
                openInspectorForSelection(command.category(), command.disabledReason());
            }
        });
        return button;
    }

    private void openProfilePage()
    {
        showAlternatePane(buildProfilePane());
    }

    void openSearchPage()
    {
        showAlternatePane(buildSearchPane());
    }

    private void openCommandCenter()
    {
        showAlternatePane(buildCommandCenterPane());
    }

    void openDatabaseSelector()
    {
        showAlternatePane(buildDatabaseSelectorPane());
    }

    void openH2RecoveryTool()
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

    void openCompanySelector()
    {
        showAlternatePane(buildCompanySelectorPane());
    }


    private void showAlternatePane(Node content)
    {
        this.alternateDashboardPanel.root().setVisible(false);
        this.alternateDashboardPanel.root().setManaged(false);
        this.alternateSettingsPane.setVisible(false);
        this.alternateSettingsPane.setManaged(false);
        this.panelHost.setVisible(false);
        this.panelHost.setManaged(false);
        this.alternateContentPane.setVisible(true);
        this.alternateContentPane.setManaged(true);
        this.alternateContentPane.getChildren().setAll(content);
    }

    private void executeSearchQuery(String value, ListView<GlobalSearchResult> results, Label status)
    {
        results.getItems().clear();
        if (value == null || value.isBlank())
        {
            status.setText("Enter a query to search.");
            return;
        }
        List<GlobalSearchResult> hits = this.uiServices.globalSearch().search(value);
        results.getItems().setAll(hits);
        String message = hits.isEmpty() ? "No results for: " + value.trim() : hits.size() + " result(s) for: " + value.trim();
        status.setText(message);
        openInspectorForSelection("Search", message);
    }

    private void openSearchResult(GlobalSearchResult result)
    {
        if (result == null)
        {
            return;
        }
        if (result.hasRoute())
        {
            openPanel(result.targetPanelId());
        }
        openInspectorForSelection(result.type() + ": " + result.title(), result.targetDescription());
    }

    /**
     * Open panel.
     *
     * @param id the id
     */
    void openPanel(AppPanelId id)
    {
        if (id == AppPanelId.REPORT_LIBRARY)
        {
            id = AppPanelId.REPORTS_WORKSPACE;
        }
        if (this.activePanelId != id)
        {
            SaveResult result = dismissActiveContext();
            this.alternateStatus.setText(navigationSaveMessage(panelTitle(this.activePanelId), result));
            if (result.failed())
            {
                return;
            }
        }
        this.activePanelId = id;
        refreshHeaderLabels();
        rebuildNavigationButtons();
        WorkspaceRouteDecision decision = this.workspaceRouter.decide(id);

        boolean dashboard = decision.isDashboard();
        boolean alternateCustomPane = decision.isAlternateCustomPane();
        boolean panelHostBackedPanel = decision.isPanelHost();

        this.alternateDashboardPanel.root().setVisible(dashboard);
        this.alternateDashboardPanel.root().setManaged(dashboard);
        if (dashboard)
        {
            this.alternateDashboardPanel.refresh();
        }
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
        else if (id == AppPanelId.COMPANY_ADMIN)
        {
            this.alternateContentPane.getChildren().setAll(buildCompanySelectorPane());
        }
        else if (alternateCustomPane)
        {
            this.alternateContentPane.getChildren().setAll(new Label("Template pending"));
        }
        else if (panelHostBackedPanel)
        {
            SaveResult switchResult = this.panelHost.show(id);
            if (switchResult.failed())
            {
                this.alternateStatus.setText(navigationSaveMessage(panelTitle(this.activePanelId), switchResult));
                return;
            }
            this.activeAdaptedPanel = null;
            LOGGER.debug("Panel strategy {} ({}) for {}", PanelAdaptationPlan.strategyFor(id), PanelAdaptationPlan.phaseFor(id), id);
        }
    }

    private void buildAlternateBudgetEditorPane()
    {
        Label title = new Label("Budget Editor");
        title.getStyleClass().add("alternate-panel-title");
        Label subtitle = new Label("Native-first budget workspace using journal-style entry flow.");
        subtitle.getStyleClass().add("alternate-panel-subtitle");
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
            contextRequired.getStyleClass().addAll("alternate-panel-banner", "alternate-panel-warning-banner");
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
        title.getStyleClass().add("alternate-panel-title");
        Label subtitle = new Label("Scheduled reports and recurring tasks.");
        subtitle.getStyleClass().add("alternate-panel-subtitle");
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
            case DONORS -> "Donors";
            case DATABASE_ADMIN -> "Database Administration";
            case COMPANY_ADMIN -> "Company Administration";
            case IMPORT_EXPORT -> "Import/Export";
            case RECONCILIATION -> "Reconciliation";
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

    private SaveResult dismissActiveContext()
    {
        if (this.activeAdaptedPanel != null)
        {
            this.activeAdaptedPanel.onLeave();
            SaveResult result = this.activeAdaptedPanel.saveContext();
            if (!result.failed())
            {
                this.activeAdaptedPanel = null;
            }
            return result;
        }
        return this.panelHost.prepareActiveForNavigation();
    }

    private void openRecordServicePanel(nonprofitbookkeeping.ui.RecordServicePanelRegistry.PanelBinding binding)
    {
        SaveResult dismissResult = dismissActiveContext();
        if (dismissResult.failed())
        {
            this.alternateStatus.setText(navigationSaveMessage(panelTitle(this.activePanelId), dismissResult));
            return;
        }
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

    List<String> testShellSurfaceStyleClasses()
    {
        return List.of(
            getStyleClass().contains("alternate-shell-root") ? "alternate-shell-root" : "",
            this.workspaceSurface.getStyleClass().contains("alternate-workspace-surface") ? "alternate-workspace-surface" : "",
            this.headerTitle.getStyleClass().contains("alternate-shell-title") ? "alternate-shell-title" : "",
            this.headerSubtitle.getStyleClass().contains("alternate-shell-subtitle") ? "alternate-shell-subtitle" : "");
    }


    boolean testHasIconRail()
    {
        return getLeft() != null;
    }

    boolean testWorkspaceUsesSplitPane()
    {
        return getCenter() == this.workspaceSplitPane
            && this.workspaceSplitPane.getItems().size() == 2
            && this.workspaceSplitPane.getItems().get(1) == this.workspaceSurface;
    }

}
