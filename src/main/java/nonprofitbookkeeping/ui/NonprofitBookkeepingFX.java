
package nonprofitbookkeeping.ui;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.bridge.SLF4JBridgeHandler;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import nonprofitbookkeeping.core.ApplicationContext;
import nonprofitbookkeeping.core.ApplicationContextImpl;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.plugin.Plugin;
import nonprofitbookkeeping.preferences.PreferencesManager;
import nonprofitbookkeeping.service.*;
import nonprofitbookkeeping.ui.actions.*;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.panels.*;
import nonprofitbookkeeping.ui.javafx.BudgetPanelFX;
import nonprofitbookkeeping.tools.H2ScriptCompanyImporter;


/**
 * Main JavaFX application class for Nonprofit Bookkeeping.
 * This class initializes the primary stage, user interface (including menus and main content area),
 * loads plugins, manages application state, and handles core application actions like
 * opening, closing, and saving company files.
 */
public class NonprofitBookkeepingFX extends Application
{
	/** The primary stage of the JavaFX application. */
	private Stage primaryStage;
        /** The root layout pane (a {@link MainApplicationView} instance) for the main scene. */
        private BorderPane root; // Should be MainApplicationView
        /** Strongly typed reference to the main workspace view. */
        private MainApplicationView mainView;
        /** Shared company selection panel displayed when no company is open. */
        private CompanySelectionPanelFX companySelectionPanel;
	/** Reference to the dashboard panel, used as a fallback or initial view. */
	private DashboardPanelFX dashboard; // Potentially part of MainApplicationView's default tabs
	/** Instance managing the currently loaded company data. Suppressed unused warning as it's initialized. */
	private CurrentCompany c;
	
	/**
	 * Enum representing the different operational states of the application,
	 * primarily concerning whether a company file is open, being created, or not open.
	 */
	private enum AppState
	{
		/** No company file is currently open. */
		NO_COMPANY,
		/** The application is in the process of creating a new company file. */
		CREATING_COMPANY,
		/** A company file is open and active. */
		COMPANY_OPEN
	}
	
	/** Current operational state of the application. */
	private AppState state = AppState.NO_COMPANY;
	
	// Menu items that need their state managed based on AppState
	/** Menu item for opening a company. */
	private MenuItem miOpen;
	/** Menu item for closing the current company. */
	private MenuItem miClose;
	/** Menu item for saving the current company. */
	private MenuItem miSave;
	/** Menu item for editing company details or creating a new company. */
	private MenuItem miEditCompany;
	/** Menu item for editing the Chart of Accounts. */
	private MenuItem miEditCoa;
	/** Menu item for editing the Journal. */
	private MenuItem miEditJournal;
	/** Menu item for importing COA from XLSX. */
	private MenuItem miImportCoaXlsx;
        /** Menu item for exporting COA to XLSX. */
        private MenuItem miExportCoaXlsx;
        /** Menu item for exporting account statements to OFX/QFX. */
        private MenuItem miExportStatementOfx;
	
	// Menus that need their state managed
	/** Top-level menu for running various tools and plugin features. */
	private Menu run;
	/** Top-level menu for generating and viewing reports. */
	private Menu reports;
	/** Top-level menu for accessing different data panels like Donors, Grants etc. */
	private Menu panels;
	
	/** Logger for this class. */
	private static final Logger LOGGER = Logger.getLogger(NonprofitBookkeepingFX.class.getName());
        /** List to hold all successfully loaded plugins. */
        private List<Plugin> loadedPlugins = new ArrayList<>();
        /** The application context passed to plugins and potentially other components. */
        private ApplicationContext applicationContext;
        /** Service that imports legacy .npbk archives into the active database. */
        private final LegacyNpbkImportService legacyNpbkImportService = new LegacyNpbkImportService();
	
	/**
	 * Static inner class acting as a container for singleton service instances.
	 * This provides a central point of access for various services used throughout the application.
	 */
	private static final class ServiceContainer
	{
		
		static InventoryService iss = null;
		/** Singleton instance of {@link ReportService}. */
		static ReportService reportService = null;
		/** Singleton instance of {@link BudgetService}. */
		static BudgetService budgetService = null;
		/** Singleton instance of {@link ReportConfigurationService}. */
		
		static ReportConfigurationService reportConfigurationService = null;
		static DocumentStorageService dss = null;
		static FundAccountingService fas = null;
		static DonorService donorService = null;
		static GrantsService grantsService = null;
		public static SalesService salesService;
		
		static
		{
			
			try
			{
				/** Singleton instance of {@link InventoryService}. */
				
				iss = new InventoryService();
				/** Singleton instance of {@link ReportService}. */
				reportService = new ReportService();
				/** Singleton instance of {@link BudgetService}. */
				budgetService = new BudgetService();
				/** Singleton instance of {@link ReportConfigurationService}. */
				reportConfigurationService = new ReportConfigurationService();
				dss = new DocumentStorageService();
				fas = new FundAccountingService();
				donorService = new DonorService();
				grantsService = new GrantsService();
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
		}
		
	}
	
	/**
	 * Main entry point for the JavaFX application.
	 * Launches the JavaFX runtime and application.
	 * @param args Command line arguments passed to the application.
	 */
	public static void main(String[] args)
	{
		launch(args);
	}
	
	/**
	 * The main entry point for this JavaFX application, called after the {@code init} method.
	 * This method sets up the primary stage, initializes the main application view,
	 * configures the menu bar, loads plugins, and displays the initial UI.
	 * A shutdown hook is added to attempt saving company data on application exit.
	 * SLF4J logging bridge is installed.
	 *
	 * @param stage The primary {@link Stage} for this application, onto which
	 *              the application scene can be set.
	 */
	@Override public void start(Stage stage)
	{
		// Add a shutdown hook to save company data when the application exits.
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// Save data
			doSaveCompany(); // Attempt to save company data
		}));
		
		// Configure SLF4J logging bridge
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		
		System.setProperty("net.sf.jasperreports.debug", "true");
		System.setProperty("net.sf.jasperreports.compile.class.debug", "true");
		System.setProperty("net.sf.jasperreports.compile.keep.java.file", "true");
		System.setProperty("net.sf.jasperreports.compiler.temp.dir", "C:/Users/benba/eclipse-workspace");
		
		stage.getIcons().addAll(new Image(getClass().getResourceAsStream("../../cg-128px.png")));
		this.primaryStage = stage;
		this.c = new CurrentCompany();
		this.dashboard = new DashboardPanelFX();
                this.mainView = new MainApplicationView();
                this.root = this.mainView; // Assign MainApplicationView to root
                this.companySelectionPanel = this.mainView.getCompanySelectionPanel();
                this.companySelectionPanel.setOnCompanyOpenedHandler(this::handleCompanyOpened);
                this.companySelectionPanel.setOnError(message -> AlertBox.showError(this.primaryStage, message));
		
		// Instantiate ApplicationContextImpl
		// Services are passed from the static ServiceContainer
		this.applicationContext = new ApplicationContextImpl(this.primaryStage,
			ServiceContainer.reportService, ServiceContainer.budgetService,
			ServiceContainer.reportConfigurationService, ServiceContainer.iss, // InventoryService
			ServiceContainer.dss, // DocumentStorageService
			ServiceContainer.fas // FundAccountingService
		);
		
		// Plugin Discovery and Initialization
		LOGGER.info("Starting plugin discovery...");
		ServiceLoader<Plugin> pluginLoader = ServiceLoader.load(Plugin.class);
		
		for (Plugin plugin : pluginLoader)
		{
			
			try
			{
				LOGGER.info(
					"Initializing plugin: " + plugin.getName() + " - " + plugin.getDescription());
				plugin.initialize(this.applicationContext);
				this.loadedPlugins.add(plugin);
				LOGGER.info("Plugin initialized successfully: " + plugin.getName());
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "Failed to initialize plugin: " +
					plugin.getClass().getName() + " - " + e.getMessage(), e);
				AlertBox.showError(this.primaryStage, "Plugin Load Error");
			}
			
		}
		
		LOGGER.info("Plugin discovery complete. Loaded " + this.loadedPlugins.size() + " plugins.");
		
		// MenuBar must be built *after* plugins are loaded so
		// they can add their items.
                MenuBar menuBar = buildMenuBar();
                this.mainView.setMenuBar(menuBar);

                Scene scene = new Scene(this.mainView, 1000, 700); // Use mainView for the scene
		ThemeManager.applyTheme(scene);
		this.primaryStage.setScene(scene);
		this.primaryStage.setTitle("Nonprofit Bookkeeping");
		
		setState(AppState.NO_COMPANY); // Set initial UI state
		this.primaryStage.show();
	}
	
	/**
	 * Builds the main {@link MenuBar} for the application.
	 * This includes standard menus like File, Edit, Run, Reports, Panels, Settings, and Help.
	 * It also iterates through any loaded {@link Plugin}s and calls their
	 * {@link Plugin#addMenuItems(MenuBar)} method to allow them to contribute to the menu bar.
	 *
	 * @return The fully constructed {@link MenuBar}.
	 */
	private MenuBar buildMenuBar()
	{
		MenuBar bar = new MenuBar();
		
		/* FILE */
                Menu companyMenu = new Menu("Company");
                this.miOpen = add(companyMenu, "Open Company", e -> doOpenCompany());
                this.miClose = add(companyMenu, "Close Company", e -> doCloseCompany());
                this.miSave = add(companyMenu, "Save Company", e -> doSaveCompany());
                this.miImportCoaXlsx = add(companyMenu, "Import COA (XLSX)",
                        e -> new ImportCoaXlsxActionFX(this.primaryStage).handle(e));
                this.miExportCoaXlsx = add(companyMenu, "Export COA (XLSX)",
                        e -> new ExportCoaXlsxActionFX(this.primaryStage).handle(e));
                this.miExportStatementOfx = add(companyMenu, "Export Statement (OFX/QFX)",
                        e -> new ExportFileActionFX(this.primaryStage).handle(e));
                bar.getMenus().add(companyMenu);
		
		/* EDIT */
		Menu edit = new Menu("Edit");
		this.miEditCompany = add(edit, "Create or Edit Company", e -> startCreateWizard());
		this.miEditCoa = add(edit, "Edit Chart of Accounts",
			e -> ((MainApplicationView) this.root).showPanel(MainApplicationView.PanelType.COA));
		this.miEditJournal = add(edit, "Edit Journal", e -> ((MainApplicationView) this.root)
			.showPanel(MainApplicationView.PanelType.JOURNAL));
		
                add(edit, "Open Budget Editor", e -> {
                        Company currentCompany = CurrentCompany.getCompany();

                        if (!CurrentCompany.isOpen() || currentCompany == null)
                        {
                                AlertBox.showError(this.primaryStage,
                                        "No company is currently open. Please open or create a company first.");
                                return;
                        }

                        BudgetPanelFX panel = new BudgetPanelFX(ServiceContainer.budgetService,
                                currentCompany.getChartOfAccounts(), new ArrayList<Fund>(), null);
                        showPanel(panel, "Budget Editor");
                });
		bar.getMenus().add(edit);
		
		/* RUN */
		this.run = new Menu("Run");
		add(this.run, "Documents & Attachments",
			e -> showPanel(new DocumentsPanelFX(ServiceContainer.dss), "Documents"));
                add(this.run, "Inventory & Depreciation",
                        e -> showPanel(new InventoryPanelFX(ServiceContainer.iss, null), "Inventory"));
                add(this.run, "Funds & Fund Accounting",
                        e -> showPanel(new FundsPanelFX(ServiceContainer.fas, null), "Funds"));
		add(this.run, "Reconcile",
			e -> showPanel(new LedgerReconcilePanelFX(new ReconciliationService()),
				"Reconciliation"));
		bar.getMenus().add(this.run);
		
//		/* REPORTS */
		this.reports = new Menu("Reports");
//		add(this.reports, "Show Reports", e -> ((MainApplicationView) this.root)
//			.showPanel(MainApplicationView.PanelType.REPORTS));
//		add(this.reports, "Show Accounts",
//			
//			e -> showPanel(new AccountsPanelFX(new AccountService()), "Chart of Accounts"));
//		add(this.reports, "Show Account Activity", e -> {
//			Company currentCompany = CurrentCompany.getCompany();
//			
//			
//			if (currentCompany != null && currentCompany.getLedger() != null)
//			{
//				showPanel(new AccountsActivityPanelFX(currentCompany.getLedger()),
//					"Account Activity");
//			}
//			else
//			{
//				AlertBox.showError(this.primaryStage, "No company or ledger open.");
//			}
//			
//		});
//		add(this.reports, "Generate Reports...",
//			e -> new GenerateReportsAction(ServiceContainer.reportService).handle(e));
//		add(this.reports, "Generate Income Statement",
//			e -> new GenerateIncomeStatementAction(ServiceContainer.reportService)
//				.actionPerformed(null));
//		add(this.reports, "Generate Balance Sheet",
//			e -> new GenerateBalanceSheetAction(ServiceContainer.reportService)
//				.actionPerformed(null));
//		add(this.reports, "Generate Trial Balance",
//			e -> new GenerateTrialBalanceAction(ServiceContainer.reportService)
//				.actionPerformed(null));
//		add(this.reports, "Generate Cash Flow Statement",
//			e -> new GenerateCashFlowStatementAction(ServiceContainer.reportService)
//				.actionPerformed(null));
//		add(this.reports, "Generate Budget vs. Actuals Report",
//			e -> new GenerateBudgetVsActualsReportAction(ServiceContainer.reportService,
//				ServiceContainer.budgetService).actionPerformed(null));
//		add(this.reports, "Manage Saved Reports", e -> {
//			
//			if (!CurrentCompany.isOpen())
//			{
//				AlertBox.showError(this.primaryStage,
//					"No company open. Load or create a company first.");
//				return;
//			}
//			
//			File companyFile = CurrentCompany.getCurrentFile();
//			
//			if (companyFile == null)
//			{
//				Company currentCompany = CurrentCompany.getCompany();
//				companyFile = currentCompany != null ? currentCompany.getCompanyFile() : null;
//			}
//			
//			File companyDir = (companyFile != null) ? companyFile.getParentFile() : null;
//			
//			if (companyDir == null)
//			{
//				AlertBox.showError(this.primaryStage,
//					"Company directory not available. Save the company before managing reports.");
//				return;
//			}
//			
//			new ManageReportConfigurationsDialog(null, ServiceContainer.reportConfigurationService,
//				companyDir, new ArrayList<Fund>(), ServiceContainer.reportService).setVisible(true);
//		});
//		add(this.reports, "Generate Account Activity Detail",
//			e -> new GenerateAccountActivityReportAction(ServiceContainer.reportService)
//				.actionPerformed(null));
//		bar.getMenus().add(this.reports);
		
		/* PANELS */
                this.panels = new Menu("Panels");
                add(this.panels, "Donors",
                        e -> showPanel(new DonorsPanelFX(ServiceContainer.donorService, null), "Donors"));
                add(this.panels, "Donations",
                        e -> showPanel(new DonationsPanelFX(this.primaryStage), "Donations"));
                add(this.panels, "Grants",
                        e -> showPanel(new GrantsPanelFX(ServiceContainer.grantsService), "Grants"));
                add(this.panels, "Sales & COG",
                        e -> showPanel(new SalesAndCOGPanelFX(ServiceContainer.salesService, null), "Sales & COG"));
		bar.getMenus().add(this.panels);
		
                bar.getMenus().add(createDatabaseMenu());

                /* SETTINGS */
                Menu settings = new Menu("Settings");
                add(settings, "Show Settings",
                        e -> showPanel(new SettingsPanelFX(this.primaryStage, new SettingsService()),
                                "Settings"));
		bar.getMenus().add(settings);
		
		/* HELP */
		Menu help = new Menu("Help");
		add(help, "Help", e -> showPanel(new HelpPanelFX(this.primaryStage), "Help"));
		bar.getMenus().add(help);
		
		// Add plugin menu items
		LOGGER.info("Adding plugin menu items. Number of plugins: " +
			(this.loadedPlugins != null ? this.loadedPlugins.size() : 0));
		
		if (this.loadedPlugins != null)
		{
			
			for (Plugin plugin : this.loadedPlugins)
			{
				
				try
				{
					LOGGER.info("Adding menu items for plugin: " + plugin.getName());
					plugin.addMenuItems(bar); // 'bar' is the MenuBar instance
				}
				catch (Exception ex)
				{
					LOGGER.log(Level.WARNING, "Plugin " + plugin.getName() +
						" failed to add its menu items: " + ex.getMessage(), ex);
				}
				
			}
			
		}
		
		return bar;
	}
	
	/**
	 * Helper method to create a {@link MenuItem}, set its label and action handler,
	 * and add it to the specified {@link Menu}.
	 *
	 * @param menu The {@link Menu} to which the new item will be added.
	 * @param label The text label for the menu item.
	 * @param handler The {@link EventHandler} to be called when the menu item is actioned.
	 * @return The created {@link MenuItem}.
	 */
        private static MenuItem add(Menu menu, String label, EventHandler<ActionEvent> handler)
        {
                MenuItem item = new MenuItem(label);
                item.setOnAction(handler);
                menu.getItems().add(item);
                return item;
        }

        private Menu createDatabaseMenu()
        {
                Menu db = new Menu("Database");
                add(db, "Open/Create H2 DB...", e -> handleOpenOrCreateDatabase());
                add(db, "Import Legacy .npbk Archive...", e -> handleImportLegacyArchive());
                add(db, "Import H2 script into DB...", e -> handleImportScriptIntoDatabase());
                return db;
        }

        private void handleOpenOrCreateDatabase()
        {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Open or Create H2 Database");
                chooser.getExtensionFilters().setAll(
                        new FileChooser.ExtensionFilter("H2 Database (*.mv.db)", "*.mv.db"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));

                String lastDatabasePath = PreferencesManager.getLastDatabasePath();
                if (lastDatabasePath != null && !lastDatabasePath.trim().isEmpty())
                {
                        try
                        {
                                Path lastPath = Path.of(lastDatabasePath);
                                Path chooserDir = Files.isDirectory(lastPath) ? lastPath : lastPath.getParent();

                                if (chooserDir != null && Files.isDirectory(chooserDir))
                                {
                                        chooser.setInitialDirectory(chooserDir.toFile());
                                }
                        }
                        catch (InvalidPathException ex)
                        {
                                LOGGER.log(Level.FINE, "Ignoring invalid DB path preference: " + lastDatabasePath, ex);
                        }
                }

                ButtonType openExisting = new ButtonType("Open Existing");
                ButtonType createNew = new ButtonType("Create New");
                Alert choiceDialog = new Alert(Alert.AlertType.CONFIRMATION);
                choiceDialog.setTitle("Select Database Action");
                choiceDialog.setHeaderText("Would you like to open an existing database or create a new one?");
                choiceDialog.getButtonTypes().setAll(openExisting, createNew, ButtonType.CANCEL);

                Optional<ButtonType> selection = choiceDialog.showAndWait();

                if (selection.isEmpty() || selection.get() == ButtonType.CANCEL)
                {
                        return;
                }

                boolean creating = selection.get() == createNew;
                File file = creating ? chooser.showSaveDialog(this.primaryStage)
                        : chooser.showOpenDialog(this.primaryStage);

                if (file == null)
                {
                        return;
                }

                Path base = normalizeH2Base(file.toPath());

                try
                {
                        Path dataFile = resolveH2DataFile(base);

                        if (!creating)
                        {
                                if (Files.notExists(dataFile))
                                {
                                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                                "The selected file does not contain an H2 database: "
                                                        + dataFile.toAbsolutePath());
                                        alert.setHeaderText("Database Not Found");
                                        alert.showAndWait();
                                        return;
                                }
                        }

                        if (base.getParent() != null)
                        {
                                Files.createDirectories(base.getParent());
                        }

                        Database.init(base);
                        Database.get().ensureSchema();
                        PreferencesManager.setLastDatabasePath(dataFile.toAbsolutePath().toString());
                        Alert a = new Alert(Alert.AlertType.INFORMATION,
                                "Database initialized at: " + base.toAbsolutePath());
                        a.setHeaderText("H2 Ready");
                        a.showAndWait();
                        setState(AppState.NO_COMPANY);

                        if (this.companySelectionPanel != null)
                        {
                                this.companySelectionPanel.refreshCompanyList();
                        }

                        if (this.mainView != null)
                        {
                                this.mainView.showCompanySelection();
                        }
                }
                catch (Exception ex)
                {
                        LOGGER.log(Level.SEVERE, "Failed to open DB: " + base, ex);
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open DB: " + ex.getMessage());
                        alert.setHeaderText("Database Error");
                        alert.showAndWait();
                }
        }

        private void handleImportLegacyArchive()
        {
                if (!Database.isInitialized())
                {
                        Alert alert = new Alert(Alert.AlertType.WARNING,
                                "Open/Create an H2 DB first.");
                        alert.setHeaderText("Database Not Ready");
                        alert.showAndWait();
                        return;
                }

                FileChooser chooser = new FileChooser();
                chooser.setTitle("Import Legacy .npbk Archive");
                chooser.getExtensionFilters().setAll(
                        new FileChooser.ExtensionFilter("Legacy Archives (*.npbk, *.json)", "*.npbk", "*.json"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));

                File file = chooser.showOpenDialog(this.primaryStage);

                if (file == null)
                {
                        return;
                }

                try
                {
                        long id = this.legacyNpbkImportService.importArchive(file.toPath());

                        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                "Imported legacy archive into the database.\nCompany record id: " + id);
                        alert.setHeaderText("Legacy Import Complete");
                        alert.showAndWait();

                        if (this.companySelectionPanel != null)
                        {
                                this.companySelectionPanel.refreshCompanyList();
                        }

                        if (this.mainView != null)
                        {
                                this.mainView.showCompanySelection();
                        }
                }
                catch (IllegalArgumentException | IllegalStateException ex)
                {
                        Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
                        alert.setHeaderText("Import Failed");
                        alert.showAndWait();
                }
                catch (Exception ex)
                {
                        LOGGER.log(Level.SEVERE, "Failed to import legacy archive", ex);
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                "Failed to import legacy archive: " + ex.getMessage());
                        alert.setHeaderText("Import Failed");
                        alert.showAndWait();
                }
        }

        private void handleImportScriptIntoDatabase()
        {
                if (!Database.isInitialized())
                {
                        Alert alert = new Alert(Alert.AlertType.WARNING,
                                "Open/Create an H2 DB first.");
                        alert.setHeaderText("Database Not Ready");
                        alert.showAndWait();
                        return;
                }

                FileChooser fc = new FileChooser();
                fc.setTitle("Select company H2 SQL script");
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL scripts", "*.sql"));
                File file = fc.showOpenDialog(this.primaryStage);

                if (file == null)
                        return;

                try
                {
                        H2ScriptCompanyImporter.importScript(file.toPath());
                        Alert a = new Alert(Alert.AlertType.INFORMATION,
                                "Imported company script into DB.");
                        a.setHeaderText("Import complete");
                        a.showAndWait();
                }
                catch (Exception ex)
                {
                        LOGGER.log(Level.SEVERE, "Import failed for file: " + file, ex);
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Import failed: " + ex.getMessage());
                        alert.setHeaderText("Import Error");
                        alert.showAndWait();
                }
        }

        private static Path normalizeH2Base(Path chosen)
        {
                String fileName = chosen.getFileName() != null ? chosen.getFileName().toString() : chosen.toString();

                if (fileName.endsWith(".mv.db"))
                {
                        String trimmed = fileName.substring(0, fileName.length() - ".mv.db".length());
                        Path parent = chosen.getParent();
                        return parent == null ? Path.of(trimmed) : parent.resolve(trimmed);
                }

                return chosen;
        }

        private static Path resolveH2DataFile(Path base)
        {
                String fileName = base.getFileName() != null ? base.getFileName().toString() : base.toString();

                if (fileName.endsWith(".mv.db"))
                {
                        return base;
                }

                Path parent = base.getParent();
                String candidate = fileName + ".mv.db";
                return parent == null ? Path.of(candidate) : parent.resolve(candidate);
        }
	
	/**
	 * Displays a given JavaFX {@link Node} (typically a panel or UI component) in a new,
	 * non-modal {@link Stage} (window).
	 * The new stage is owned by the application's primary stage.
	 *
	 * @param panel The {@link Node} to display in the new window.
	 * @param title The title for the new window.
	 */
	private void showPanel(Node panel, String title)
	{
		Stage sub = new Stage();
		sub.setTitle(title);
		BorderPane wrapper = new BorderPane(panel);
		wrapper.setPadding(new Insets(8));
		Scene scene = new Scene(wrapper, 900, 600);
		ThemeManager.applyTheme(scene);
		sub.setScene(scene);
		sub.initOwner(this.primaryStage);
		sub.show();
	}
	
	
	/**
	 * Sets the application's operational state and updates the enabled/disabled status
	 * of various menu items accordingly.
	 *
	 * @param newState The new {@link AppState} to set for the application.
	 */
        private void setState(AppState newState)
        {
                this.state = newState;
                boolean databaseReady = Database.isInitialized();
                boolean creatingCompany = (newState == AppState.CREATING_COMPANY);
                boolean companyOpen = databaseReady && CurrentCompany.isOpen();

                this.miOpen.setDisable(!databaseReady || creatingCompany || companyOpen);
                this.miClose.setDisable(!companyOpen || creatingCompany);
                this.miSave.setDisable(!companyOpen || creatingCompany);
                this.miEditCompany.setDisable(!databaseReady || creatingCompany);
                this.miEditCoa.setDisable(!companyOpen || creatingCompany);
                this.miEditJournal.setDisable(!companyOpen || creatingCompany);
                this.miImportCoaXlsx.setDisable(!companyOpen || creatingCompany);
                this.miExportCoaXlsx.setDisable(!companyOpen || creatingCompany);
                if (this.miExportStatementOfx != null)
                {
                        this.miExportStatementOfx.setDisable(!companyOpen || creatingCompany);
                }

                this.run.setDisable(!companyOpen || creatingCompany);
                this.panels.setDisable(!companyOpen || creatingCompany);
                this.reports.setDisable(!companyOpen || creatingCompany);

                if (this.mainView != null)
                {
                        this.mainView.updateCompanyOpenState(companyOpen);
                }

        }

        private void handleCompanyOpened(Company company)
        {
                if (company == null)
                {
                        return;
                }

                setState(AppState.COMPANY_OPEN);

                if (this.companySelectionPanel != null)
                {
                        this.companySelectionPanel.refreshCompanyList();
                }

                if (this.mainView != null)
                {
                        this.mainView.showWorkspaceTabs();
                        this.mainView.showPanel(MainApplicationView.PanelType.DASHBOARD);
                }
        }
	
	/**
	 * Handles the action to open a company file.
	 * It instantiates and triggers {@link OpenCompanyFileActionFX}.
	 * If successful, the application state is set to {@link AppState#COMPANY_OPEN}.
	 * Errors are displayed using an {@link AlertBox}.
	 * The {@code @SuppressWarnings("unused")} is present because this method is called via JavaFX action event.
	 */
        private void doOpenCompany()
        {
                if (!Database.isInitialized())
                {
                        Alert alert = new Alert(Alert.AlertType.WARNING,
                                "Initialize an H2 database before opening a company.");
                        alert.initOwner(this.primaryStage);
                        alert.setHeaderText("Database Not Ready");
                        alert.showAndWait();
                        return;
                }

                try
                {
                        OpenCompanyFileActionFX openCompanyFileActionFX = new OpenCompanyFileActionFX(
                                this.primaryStage,
                                () -> handleCompanyOpened(CurrentCompany.getCompany()));
                }
                catch (Exception e)
                {
                        AlertBox.showError(this.primaryStage, "Failed to open company: " + e.getMessage());
                }
		
	}
	
	/**
	 * Handles the action to close the currently open company file.
	 * It instantiates and triggers {@link CloseCompanyFileAction}.
	 * Sets the application state to {@link AppState#NO_COMPANY} and switches
	 * the main view to the dashboard.
	 * Errors are displayed using an {@link AlertBox}.
	 * The {@code @SuppressWarnings("unused")} is present because this method is called via JavaFX action event.
	 */
	private void doCloseCompany()
	{
		
		try
		{
			CloseCompanyFileAction closeCompanyFileAction =
				new CloseCompanyFileAction(this.primaryStage);
			
                        if (closeCompanyFileAction.isClosed())
                        {
                                // After action, set state.
                                setState(AppState.NO_COMPANY);
                                if (this.companySelectionPanel != null)
                                {
                                        this.companySelectionPanel.refreshCompanyList();
                                }
                        }
                        else
                        {
                                return; // user cancelled closing
			}
			
		}
		
		catch (Exception e) // Catch broad exceptions from action
		{
			AlertBox.showError(this.primaryStage, "Failed to close company: " + e.getMessage());
		}
		
		// Switch view back to dashboard
                if (this.mainView != null)
                {
                        this.mainView.showCompanySelection();
                }
		
	}
	
	/**
	 * Handles the action to save the currently open company file.
	 * It instantiates and triggers {@link SaveCompanyFileAction}.
	 * Shows an info message on success or an error message on failure.
	 * The {@code @SuppressWarnings("unused")} is present because this method is called via JavaFX action event.
	 */
        private void doSaveCompany()
        {
                if (!Database.isInitialized() || !CurrentCompany.isOpen())
                {
                        AlertBox.showError(this.primaryStage,
                                "Open a company connected to the database before saving.");
                        return;
                }

                try
                {
                        SaveCompanyFileAction saveCompanyFileAction =
                                new SaveCompanyFileAction(this.primaryStage);
			AlertBox.showInfo(this.primaryStage, "Company saved.");
		}
		catch (Exception ex)
		{
			AlertBox.showError(this.primaryStage, "Failed to save company: " + ex.getMessage());
		}
		
	}
	
	/**
	 * Handles the action to start the company creation/editing wizard.
	 * It changes the application state to {@link AppState#CREATING_COMPANY},
	 * then instantiates and triggers {@link CreateOrEditCompanyActionFX}.
	 * If successful, state changes to {@link AppState#COMPANY_OPEN}. If an error occurs,
	 * state reverts to the previous state, and an error alert is shown.
	 * The {@code @SuppressWarnings("unused")} is present because this method is called via JavaFX action event.
	 */
        private void startCreateWizard()
        {
                if (!Database.isInitialized())
                {
                        Alert alert = new Alert(Alert.AlertType.WARNING,
                                "Initialize an H2 database before creating a company.");
                        alert.initOwner(this.primaryStage);
                        alert.setHeaderText("Database Not Ready");
                        alert.showAndWait();
                        return;
                }

                AppState saved = getState();
                setState(AppState.CREATING_COMPANY);

                try
                {
			CreateOrEditCompanyActionFX createOrEditCompanyActionFX =
				new CreateOrEditCompanyActionFX(this.primaryStage);
			
			// If wizard completes and company is set in CurrentCompany:
                        if (CurrentCompany.getCompany() != null)
                        { // Basic check
                                setState(AppState.COMPANY_OPEN);
                                if (this.companySelectionPanel != null)
                                {
                                        this.companySelectionPanel.refreshCompanyList();
                                }
                        }
                        else
                        {
                                // Wizard was cancelled or failed without setting a company
                                setState(saved); // Revert to previous state
			}
			
		}
		catch (Exception e)
		{
			setState(saved); // Revert to previous state on error
			e.printStackTrace(); // Consider more specific logging
			AlertBox.showError(this.primaryStage, "Error during company setup: " + e.getMessage());
		}
		
	}
	
	/**
	 * Gets the current operational state of the application.
	 * @return The current {@link AppState}.
	 */
	private AppState getState()
	{
		return this.state;
	}
	
	/**
	 * {@inheritDoc}
	 * Called when the application is shutting down.
	 * This implementation iterates through all loaded plugins and calls their {@code shutdown()} method.
	 * It also attempts to save any currently open company data via {@link #doSaveCompany()}.
	 * Errors during plugin shutdown are logged.
	 * @throws Exception if an error occurs during the superclass's stop method or saving company data.
	 */
	@Override public void stop() throws Exception
	{
		LOGGER.info("Application stopping. Shutting down plugins.");
		
		if (this.loadedPlugins != null)
		{
			
			for (Plugin plugin : this.loadedPlugins)
			{
				
				try
				{
					LOGGER.info("Shutting down plugin: " + plugin.getName());
					plugin.shutdown();
				}
				catch (Exception e)
				{
					LOGGER.log(Level.WARNING,
						"Error shutting down plugin: " + plugin.getName() + " - " + e.getMessage(),
						e);
				}
				
			}
			
		}
		
		doSaveCompany();
		super.stop();
	}
	
}
