package org.nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.service.DocumentStorageService;
import nonprofitbookkeeping.service.DonorService;
import nonprofitbookkeeping.service.FundAccountingService;
import nonprofitbookkeeping.service.GrantsService;
import nonprofitbookkeeping.service.InventoryService;
import nonprofitbookkeeping.service.LegacyNpbkImportService;
import nonprofitbookkeeping.service.ReconciliationService;
import nonprofitbookkeeping.service.SalesService;
import nonprofitbookkeeping.service.SettingsService;
import nonprofitbookkeeping.service.UndepositedFundsService;
import nonprofitbookkeeping.plugins.scaledger.SCALedgerPlugin;
import nonprofitbookkeeping.tools.H2ScriptCompanyImporter;
import nonprofitbookkeeping.ui.actions.ExcelTemplateReportActionFX;
import nonprofitbookkeeping.ui.actions.ExportCoaXlsxActionFX;
import nonprofitbookkeeping.ui.actions.ExportFileActionFX;
import nonprofitbookkeeping.ui.actions.ImportCoaXlsxActionFX;
import nonprofitbookkeeping.ui.actions.ImportFileActionFX;
import nonprofitbookkeeping.ui.actions.CloseCompanyFileAction;
import nonprofitbookkeeping.ui.actions.CreateOrEditCompanyActionFX;
import nonprofitbookkeeping.ui.actions.OpenCompanyFileActionFX;
import nonprofitbookkeeping.ui.actions.ImportSCALedgerActionFX;
import nonprofitbookkeeping.ui.actions.scaledger.ImportFromOutlandsLedgerActionFX;
import nonprofitbookkeeping.ui.actions.scaledger.SaveModifiedCopyActionFX;
import nonprofitbookkeeping.ui.actions.scaledger.LoadXlsmTableActionFX;
import nonprofitbookkeeping.ui.panels.SqlQueryPanelFX;
import nonprofitbookkeeping.ui.panels.DonationsPanelFX;
import nonprofitbookkeeping.ui.panels.DonorsPanelFX;
import nonprofitbookkeeping.ui.panels.DocumentsPanelFX;
import nonprofitbookkeeping.ui.panels.FundsPanelFX;
import nonprofitbookkeeping.ui.panels.GrantsPanelFX;
import nonprofitbookkeeping.ui.panels.HelpPanelFX;
import nonprofitbookkeeping.ui.panels.InventoryPanelFX;
import nonprofitbookkeeping.ui.panels.LedgerReconcilePanelFX;
import nonprofitbookkeeping.ui.panels.SalesAndCOGPanelFX;
import nonprofitbookkeeping.ui.panels.SettingsPanelFX;
import nonprofitbookkeeping.ui.panels.UndepositedFundsPanelFX;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Represents the MainWindow component in the nonprofit bookkeeping application.
 */
public class MainWindow extends BorderPane
{
    private final PanelHost panelHost = new PanelHost();
    private final InspectorPane inspectorPane = new InspectorPane();
    private final NavigationPane nav = new NavigationPane(this::openPanel, this::openInspectorForSelection);
    private final InventoryService inventoryService = new InventoryService();
    private final DocumentStorageService documentStorageService = new DocumentStorageService();
    private final FundAccountingService fundAccountingService = new FundAccountingService();
    private final DonorService donorService = new DonorService();
    private final GrantsService grantsService = new GrantsService();
    private final UndepositedFundsService undepositedFundsService = new UndepositedFundsService();
    private final SalesService salesService = new SalesService();
    private final SettingsService settingsService = new SettingsService();
    private final SCALedgerPlugin scaLedgerPlugin = new SCALedgerPlugin();
    private final Supplier<Stage> stageSupplier;
    private DateRangeSelector dateRangeSelector;

    public MainWindow()
    {
        this(() -> null);
    }

    MainWindow(Supplier<Stage> stageSupplier)
    {
        this.stageSupplier = (stageSupplier == null) ? () -> null : stageSupplier;
        initialiseScaPlugin();

        setTop(buildTopChrome());
        setLeft(nav);
        setCenter(panelHost);
        setRight(inspectorPane);

        BorderPane.setMargin(panelHost, new Insets(8));
        BorderPane.setMargin(nav, new Insets(8, 4, 8, 8));
        BorderPane.setMargin(inspectorPane, new Insets(8, 8, 8, 4));

        openPanel(AppPanelId.DASHBOARD);
    }

    private VBox buildTopChrome()
    {
        MenuBar menuBar = buildMenuBar();
        ToolBar toolBar = buildToolBar();
        VBox v = new VBox(menuBar, toolBar);
        v.getStyleClass().add("top-chrome");
        return v;
    }

    private MenuBar buildMenuBar()
    {
        Menu file = new Menu("File");
        file.getItems().addAll(
            item("Open Company", "Ctrl+O", this::openCompany),
            item("Close Company", "Ctrl+W", this::closeCompany),
            item("Save Company", "Ctrl+S", this::saveActivePanel),
            new SeparatorMenuItem(),
            item("Import COA (XLSX)", null,
                () -> new ImportCoaXlsxActionFX(getOwningStage()).handle(null)),
            item("Export COA (XLSX)", null,
                () -> new ExportCoaXlsxActionFX(getOwningStage()).handle(null)),
            new SeparatorMenuItem(),
            item("Import Financial File (OFX/QFX)...", null,
                () -> new ImportFileActionFX(getOwningStage()).handle(null)),
            item("Import Outlands Ledger...", null, this::importOutlandsLedger),
            item("Import SCA Ledger...", null, this::importScaLedger),
            new SeparatorMenuItem(),
            item("Save Modified SCA Workbook...", null, this::saveModifiedScaWorkbook),
            item("Export Account Statement (OFX/QFX)...", null,
                () -> new ExportFileActionFX(getOwningStage()).handle(null)),
            new SeparatorMenuItem(),
            item("Exit", null, () -> System.exit(0))
        );

        Menu edit = new Menu("Edit");
        edit.getItems().addAll(
            item("Create or Edit Company", null, this::createOrEditCompany),
            item("Edit Chart of Accounts", null,
                () -> openPanel(AppPanelId.CHART_OF_ACCOUNTS)),
            item("Edit Journal", "Ctrl+J", this::openInspectorJournal)
        );

        Menu run = new Menu("Run");
        run.getItems().addAll(
            item("Reports Workspace", null, () -> openPanel(AppPanelId.REPORTS_WORKSPACE)),
            item("Reconcile Accounts", null,
                () -> showLegacyPanel("Reconciliation", new LedgerReconcilePanelFX(new ReconciliationService()))),
            item("Undeposited Funds", null,
                () -> showLegacyPanel("Undeposited Funds", new UndepositedFundsPanelFX(undepositedFundsService))),
            item("Sales & COGS", null,
                () -> showLegacyPanel("Sales & COGS", new SalesAndCOGPanelFX(salesService, null))),
            new SeparatorMenuItem(),
            item("Documents & Attachments", null,
                () -> showLegacyPanel("Documents", new DocumentsPanelFX(documentStorageService))),
            item("Inventory & Depreciation", null,
                () -> showLegacyPanel("Inventory", new InventoryPanelFX(inventoryService, null))),
            new SeparatorMenuItem(),
            item("Generate Excel Template Report...", null,
                () -> new ExcelTemplateReportActionFX(getOwningStage()).handle(null))
        );

        Menu database = new Menu("Database");
        database.getItems().addAll(
            item("Open/Create H2 DB...", null,
                this::handleOpenOrCreateDatabase),
            item("Import Legacy .npbk Archive...", null,
                this::handleImportLegacyArchive),
            item("Import H2 script into DB...", null,
                this::handleImportH2Script),
            item("Run SQL Query...", null,
                this::openSqlQueryDialog)
        );

        Menu reports = new Menu("Reports");
        reports.getItems().addAll(
            item("Income Statement", null,
                () -> openPanel(AppPanelId.REPORTS_WORKSPACE)),
            item("Balance Sheet", null,
                () -> openPanel(AppPanelId.REPORTS_WORKSPACE)),
            item("Account Details", null,
                () -> openPanel(AppPanelId.REPORTS_WORKSPACE))
        );

        Menu fundraising = new Menu("Fundraising");
        fundraising.getItems().addAll(
            item("Donors", null,
                () -> showLegacyPanel("Donors", new DonorsPanelFX(donorService, null))),
            item("Donations", null,
                () -> showLegacyPanel("Donations", new DonationsPanelFX(getOwningStage()))),
            item("Grants", null,
                () -> showLegacyPanel("Grants", new GrantsPanelFX(grantsService))),
            item("Funds & Fund Accounting", null,
                () -> showLegacyPanel("Funds", new FundsPanelFX(fundAccountingService, null)))
        );

        Menu settings = new Menu("Settings");
        settings.getItems().addAll(
            item("Show Settings", null,
                () -> showLegacyPanel("Settings", new SettingsPanelFX(getOwningStage(), settingsService, () -> {})))
        );

        Menu plugins = new Menu("Plugins");
        MenuItem none = new MenuItem("No plugins available");
        none.setDisable(true);
        plugins.getItems().add(none);

        Menu help = new Menu("Help");
        help.getItems().addAll(
            item("Help", null, () -> showLegacyPanel("Help", new HelpPanelFX(getOwningStage())))
        );

        return new MenuBar(file, edit, run, database, reports, fundraising, settings, plugins, help);
    }

    private ToolBar buildToolBar()
    {
        Button btnNew = new Button("New");
        btnNew.setOnAction(e -> newItemInActivePanel());

        Button btnSave = new Button("Save");
        btnSave.setOnAction(e -> saveActivePanel());

        Button btnFind = new Button("Find");
        btnFind.setOnAction(e -> openSearch());

        Button btnJournal = new Button("Journal");
        btnJournal.setOnAction(e -> openInspectorJournal());

        DateRangeSelector dr = new DateRangeSelector();
        this.dateRangeSelector = dr;

        ToolBar tb = new ToolBar(btnNew, btnSave, new Separator(), btnFind, new Separator(), btnJournal, new Separator(), dr);
        tb.getStyleClass().add("toolbar");
        return tb;
    }

    
    private void focusDateRangeSelector()
    {
        if (dateRangeSelector == null) return;
        dateRangeSelector.presetBox().requestFocus();
        dateRangeSelector.presetBox().show();
    }

    private MenuItem item(String text, String accel, Runnable action)
    {
        MenuItem mi = new MenuItem(text);
        if (accel != null) mi.setAccelerator(KeyCombination.keyCombination(accel));
        mi.setOnAction(e -> action.run());
        return mi;
    }

    // --- hooks ---
    public void openPanel(AppPanelId id)
    {
        panelHost.show(id);
        nav.highlight(id);
    }

    public void openInspectorForSelection(String title, String body)
    {
        inspectorPane.show(title, body);
    }

    public void closeInspector()
    {
        inspectorPane.clear();
    }

    public void saveActivePanel()
    {
        panelHost.saveActive();
        info("Save: " + panelHost.getActiveTitle());
    }

    public void newItemInActivePanel()
    {
        panelHost.newItemActive();
    }

    public void copySelection()
    {
        panelHost.copySelectionActive();
    }

    public void paste()
    {
        panelHost.pasteActive();
    }

    public void openSearch()
    {
        inspectorPane.show("Search", "Search UI placeholder.\n\n(We’ll decide whether this is a modal dialog or a side pane.)");
    }

    public void openInspectorJournal()
    {
        inspectorPane.show("Journal View", "Journal drawer placeholder.\n\nFrom any panel, this should show derived DR/CR lines for the current selection.");
    }

    private void info(String msg)
    {
        inspectorPane.show("Info", msg);
    }

    private void showLegacyPanel(String title, Node content)
    {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(1050, 720);
        dialog.setResizable(true);

        if (getScene() != null && getScene().getWindow() != null)
        {
            dialog.initOwner(getScene().getWindow());
        }

        dialog.showAndWait();
    }

    private void handleOpenOrCreateDatabase()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open or Create H2 Database");
        chooser.getExtensionFilters().setAll(
            new FileChooser.ExtensionFilter("H2 Database (*.mv.db)", "*.mv.db"),
            new FileChooser.ExtensionFilter("All Files", "*.*"));

        ButtonType openExisting = new ButtonType("Open Existing");
        ButtonType createNew = new ButtonType("Create New");
        Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
        choice.setTitle("Select Database Action");
        choice.setHeaderText("Open an existing database or create a new one?");
        choice.getButtonTypes().setAll(openExisting, createNew, ButtonType.CANCEL);

        if (getScene() != null && getScene().getWindow() != null)
        {
            choice.initOwner(getScene().getWindow());
        }

        ButtonType selected = choice.showAndWait().orElse(ButtonType.CANCEL);
        if (selected == ButtonType.CANCEL)
        {
            return;
        }

        File selectedFile = (selected == createNew)
            ? chooser.showSaveDialog(getOwningStage())
            : chooser.showOpenDialog(getOwningStage());

        if (selectedFile == null)
        {
            return;
        }

        Path basePath = normalizeH2Base(selectedFile.toPath());
        try
        {
            Database.init(basePath);
            Database.get().ensureSchema();
            info("Database ready: " + basePath.toAbsolutePath());
        }
        catch (Exception ex)
        {
            info("Failed to open/create DB: " + UiErrors.safeMessage(ex));
        }
    }

    private void handleImportLegacyArchive()
    {
        if (!Database.isInitialized())
        {
            info("Open/Create an H2 database first.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Legacy .npbk Archive");
        chooser.getExtensionFilters().setAll(
            new FileChooser.ExtensionFilter("Legacy archive (*.npbk, *.zip, *.json)",
                "*.npbk", "*.zip", "*.json"),
            new FileChooser.ExtensionFilter("All Files", "*.*"));

        File file = chooser.showOpenDialog(getOwningStage());
        if (file == null)
        {
            return;
        }

        try
        {
            long id = new LegacyNpbkImportService().importArchive(file.toPath());
            info("Legacy archive imported. Company id=" + id);
        }
        catch (Exception ex)
        {
            info("Legacy import failed: " + UiErrors.safeMessage(ex));
        }
    }

    private void handleImportH2Script()
    {
        if (!Database.isInitialized())
        {
            info("Open/Create an H2 database first.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import H2 SQL Script");
        chooser.getExtensionFilters().setAll(
            new FileChooser.ExtensionFilter("SQL script (*.sql)", "*.sql"),
            new FileChooser.ExtensionFilter("All Files", "*.*"));

        File file = chooser.showOpenDialog(getOwningStage());
        if (file == null)
        {
            return;
        }

        try
        {
            H2ScriptCompanyImporter.importScript(file.toPath());
            info("H2 script imported: " + file.getName());
        }
        catch (Exception ex)
        {
            info("H2 script import failed: " + UiErrors.safeMessage(ex));
        }
    }

    private void openSqlQueryDialog()
    {
        if (!Database.isInitialized())
        {
            info("Open/Create an H2 database first.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Run SQL Query");
        dialog.getDialogPane().setContent(new SqlQueryPanelFX());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(960, 700);
        dialog.setResizable(true);

        if (getScene() != null && getScene().getWindow() != null)
        {
            dialog.initOwner(getScene().getWindow());
        }

        dialog.showAndWait();
    }

    private Stage getOwningStage()
    {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage)
        {
            return stage;
        }

        return stageSupplier.get();
    }

    private void initialiseScaPlugin()
    {
        try
        {
            this.scaLedgerPlugin.initialize(null);
        }
        catch (Exception ex)
        {
            info("SCA plugin initialization failed: " + UiErrors.safeMessage(ex));
        }
    }

    private void openCompany()
    {
        new OpenCompanyFileActionFX(getOwningStage(),
            () -> info("Company opened."));
    }

    private void closeCompany()
    {
        CloseCompanyFileAction action = new CloseCompanyFileAction(getOwningStage());
        if (action.isClosed())
        {
            info("Company closed.");
        }
    }

    private void createOrEditCompany()
    {
        new CreateOrEditCompanyActionFX(getOwningStage());
    }

    private void importOutlandsLedger()
    {
        new ImportFromOutlandsLedgerActionFX(getOwningStage(),
            this.scaLedgerPlugin.getPageViewerPanel()).handle(null);
    }

    private void importScaLedger()
    {
        new ImportSCALedgerActionFX(getOwningStage()).handle(null);
    }

    private void saveModifiedScaWorkbook()
    {
        if (!ensureScaWorkbookLoadedForSave())
        {
            return;
        }

        new SaveModifiedCopyActionFX(getOwningStage(), this.scaLedgerPlugin)
            .handle(null);
    }

    private boolean ensureScaWorkbookLoadedForSave()
    {
        if (hasLoadedScaWorkbook())
        {
            return true;
        }

        Alert prompt = new Alert(Alert.AlertType.CONFIRMATION);
        prompt.setTitle("Load SCA Workbook Required");
        prompt.setHeaderText("No SCA workbook is currently loaded.");
        prompt.setContentText("Load an XLSM workbook now so the modified copy can be saved?");

        ButtonType loadNow = new ButtonType("Load XLSM Now");
        prompt.getButtonTypes().setAll(loadNow, ButtonType.CANCEL);

        Stage owner = getOwningStage();
        if (owner != null)
        {
            prompt.initOwner(owner);
        }

        ButtonType decision = prompt.showAndWait().orElse(ButtonType.CANCEL);
        if (decision != loadNow)
        {
            info("Save Modified SCA Workbook canceled (no workbook loaded).");
            return false;
        }

        new LoadXlsmTableActionFX(owner, this.scaLedgerPlugin).handle(null);

        if (!hasLoadedScaWorkbook())
        {
            info("No SCA workbook loaded. Use File → Import Outlands Ledger... or retry with an XLSM file.");
            return false;
        }

        return true;
    }


    boolean hasLoadedScaWorkbook()
    {
        return this.scaLedgerPlugin.getCurrentScaFile() != null;
    }

    SCALedgerPlugin getScaLedgerPluginForTest()
    {
        return this.scaLedgerPlugin;
    }

    private Path normalizeH2Base(Path filePath)
    {
        String path = filePath.toAbsolutePath().toString();
        if (path.endsWith(".mv.db"))
        {
            path = path.substring(0, path.length() - ".mv.db".length());
        }
        return Path.of(path);
    }
}
