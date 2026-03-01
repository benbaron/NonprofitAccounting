package org.nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.service.LegacyNpbkImportService;
import nonprofitbookkeeping.tools.H2ScriptCompanyImporter;
import nonprofitbookkeeping.ui.panels.SqlQueryPanelFX;

import java.io.File;
import java.nio.file.Path;

/**
 * Represents the MainWindow component in the nonprofit bookkeeping application.
 */
public class MainWindow extends BorderPane
{
    private final PanelHost panelHost = new PanelHost();
    private final InspectorPane inspectorPane = new InspectorPane();
    private final NavigationPane nav = new NavigationPane(this::openPanel, this::openInspectorForSelection);
    private DateRangeSelector dateRangeSelector;

    public MainWindow()
    {
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
            item("New", "Ctrl+N", this::newItemInActivePanel),
            item("Open…", null, () -> info("Open not wired yet.")),
            new SeparatorMenuItem(),
            item("Save", "Ctrl+S", this::saveActivePanel),
            item("Export…", null, () -> info("Export not wired yet.")),
            new SeparatorMenuItem(),
            item("Exit", null, () -> System.exit(0))
        );

        Menu edit = new Menu("Edit");
        edit.getItems().addAll(
            item("Undo", "Ctrl+Z", () -> info("Undo not wired yet.")),
            item("Redo", "Ctrl+Y", () -> info("Redo not wired yet.")),
            new SeparatorMenuItem(),
            item("Cut", "Ctrl+X", () -> info("Cut not wired yet.")),
            item("Copy", "Ctrl+C", this::copySelection),
            item("Paste", "Ctrl+V", this::paste)
        );

        Menu search = new Menu("Search");
        search.getItems().addAll(
            item("Find…", "Ctrl+F", this::openSearch),
            item("Go to…", "Ctrl+G", () -> info("Go to not wired yet.")),
            new SeparatorMenuItem(),
            item("Date Range…", null, this::focusDateRangeSelector)
        );

        Menu run = new Menu("Run");
        run.getItems().addAll(
            item("Post / Validate", null, () -> info("Posting not wired in UI yet.")),
            item("Recalculate summaries", null, () -> info("Recalculate not wired yet.")),
            new SeparatorMenuItem(),
            item("Inventory & Depreciation", null, () -> openPanel(AppPanelId.INVENTORY)),
            item("Reports Workspace", null, () -> openPanel(AppPanelId.REPORTS_WORKSPACE))
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

        Menu tools = new Menu("Tools");
        tools.getItems().addAll(
            item("Import/Export…", null, () -> info("Tools not wired yet.")),
            item("Preferences…", null, this::openSettingsDialog)
        );

        Menu help = new Menu("Help");
        help.getItems().addAll(
            item("About", null, () -> info("SCA Ledger prototype shell."))
        );

        return new MenuBar(file, edit, search, run, database, tools, help);
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

    private void openSettingsDialog()
    {
        SettingsPanel settingsPanel = new SettingsPanel();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Preferences");
        dialog.getDialogPane().setContent(settingsPanel.root());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.showAndWait();
    }

    private void info(String msg)
    {
        inspectorPane.show("Info", msg);
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
        return (getScene() != null && getScene().getWindow() instanceof Stage stage)
            ? stage
            : null;
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
