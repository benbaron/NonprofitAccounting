
package nonprofitbookkeeping.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import nonprofitbookkeeping.core.ApplicationContext;
import nonprofitbookkeeping.core.ApplicationContextImpl;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.plugin.Plugin;
import nonprofitbookkeeping.preferences.PreferencesManager;
import nonprofitbookkeeping.service.*;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.util.FormatUtils;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonJournalPanel;
import nonprofitbookkeeping.plugins.scaledger.SCALedgerPlugin;
import nonprofitbookkeeping.ui.actions.*;
import nonprofitbookkeeping.ui.actions.scaledger.ImportFromOutlandsLedgerActionFX;
import nonprofitbookkeeping.ui.actions.scaledger.LoadXlsmTableActionFX;
import nonprofitbookkeeping.ui.actions.scaledger.SaveModifiedCopyActionFX;
import nonprofitbookkeeping.plugins.scaledger.ui.PageViewerPanel;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.panels.*;
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
	/** Menu item for importing financial files directly into the data model. */
	private MenuItem miImportFile;
	/** Menu item for loading an SCA XLSM workbook via the plugin. */
	private MenuItem miLoadScaXlsm;
	/** Menu item for importing an SCA Excel ledger via the plugin. */
	private MenuItem miImportScaExcel;
	/** Menu item for persisting an SCA ledger into the journal tables. */
	private MenuItem miPersistScaLedger;
	/** Menu item for saving a modified copy of an SCA workbook. */
	private MenuItem miSaveScaModifiedCopy;
	
	// Menus that need their state managed
	/** Top-level menu for running various tools and plugin features. */
	private Menu run;
	/** Top-level menu for generating and viewing reports. */
	private Menu reports;
	/** Top-level menu for accessing different data panels like Donors, Grants etc. */
	private Menu panels;
	/** Top-level menu dedicated to import workflows. */
	private Menu importMenu;
	/** Top-level menu dedicated to export workflows. */
	private Menu exportMenu;
	/** Top-level menu that lists available plugins. */
	private Menu pluginsMenu;
	
	/** Reference to the loaded SCA Ledger plugin, if available. */
	private SCALedgerPlugin scaLedgerPlugin;
	/** Shared viewer used for SCA Excel imports. */
	private final PageViewerPanel scaExcelViewerPanel = new PageViewerPanel();
	
	/** Logger for this class. */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(NonprofitBookkeepingFX.class);
	/** List to hold all successfully loaded plugins. */
	private List<Plugin> loadedPlugins = new ArrayList<>();
	/** The application context passed to plugins and potentially other components. */
	private ApplicationContext applicationContext;
	/** Service responsible for persisting application settings. */
	private final SettingsService settingsService = new SettingsService();
	/** Tracks whether settings have been loaded from persistent storage. */
	private boolean settingsLoaded;
	/** Executor managing background autosave tasks. */
	private ScheduledExecutorService autosaveExecutor;
	/** Handle to the currently scheduled autosave task. */
	private ScheduledFuture<?> autosaveFuture;
	/** Service that imports legacy .npbk archives into the active database. */
	private final LegacyNpbkImportService legacyNpbkImportService =
		new LegacyNpbkImportService();
	
	/** Cached settings to avoid redundant database reads. */
	private SettingsModel cachedSettings = new SettingsModel();
	
	
	/**
	 * Static inner class acting as a container for singleton service instances.
	 * This provides a central point of access for various services used throughout the application.
	 */
	private static final class ServiceContainer
	{
		
		static InventoryService iss = null;
		/** Singleton instance of {@link ReportService}. */
		static ReportService reportService = null;
		/** Singleton instance of {@link ReportConfigurationService}. */
		
		static ReportConfigurationService reportConfigurationService = null;
		static DocumentStorageService dss = null;
		static FundAccountingService fas = null;
		static DonorService donorService = null;
		static GrantsService grantsService = null;
		static UndepositedFundsService undepositedFundsService = null;
		public static SalesService salesService;
		
		static
		{
			
			try
			{
				/** Singleton instance of {@link InventoryService}. */
				
				iss = new InventoryService();
				/** Singleton instance of {@link ReportService}. */
				reportService = new ReportService();
				/** Singleton instance of {@link ReportConfigurationService}. */
				reportConfigurationService = new ReportConfigurationService();
				dss = new DocumentStorageService();
				fas = new FundAccountingService();
				donorService = new DonorService();
				grantsService = new GrantsService();
				undepositedFundsService = new UndepositedFundsService();
				
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
	@Override
	public void start(Stage stage)
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
		System.setProperty("net.sf.jasperreports.compile.keep.java.file",
			"true");
		System.setProperty("net.sf.jasperreports.compiler.temp.dir",
			"C:/Users/benba/eclipse-workspace");
		
		stage.getIcons().addAll(
			new Image(getClass().getResourceAsStream("../../cg-128px.png")));
		this.primaryStage = stage;
		
		
		this.mainView = new MainApplicationView();
		this.root = this.mainView; // Assign MainApplicationView to root
		this.companySelectionPanel = this.mainView.getCompanySelectionPanel();
		this.companySelectionPanel
			.setOnCompanyOpenedHandler(this::handleCompanyOpened);
		this.companySelectionPanel.setOnError(
			message -> AlertBox.showError(this.primaryStage, message));
		
		// Instantiate ApplicationContextImpl
		// Services are passed from the static ServiceContainer
		MenuBar contextMenuBar = new MenuBar();
		this.applicationContext = new ApplicationContextImpl(this.primaryStage,
			contextMenuBar, ServiceContainer.reportService,
			ServiceContainer.reportConfigurationService, ServiceContainer.iss, // InventoryService
			ServiceContainer.dss, // DocumentStorageService
			ServiceContainer.fas // FundAccountingService
		);
		
		// Plugin Discovery and Initialization
		LOGGER.info("Starting plugin initialization...");
		List<Plugin> pluginsToLoad = new ArrayList<>();
		pluginsToLoad
			.add(new nonprofitbookkeeping.plugins.scaledger.SCALedgerPlugin());
		pluginsToLoad
			.add(new nonprofitbookkeeping.plugins.sample.SamplePlugin());
		
		for (Plugin plugin : pluginsToLoad)
		{
			
			try
			{
				LOGGER.info("Initializing plugin: {} - {}", plugin.getName(),
					plugin.getDescription());
				plugin.initialize(this.applicationContext);
				
				if (plugin instanceof SCALedgerPlugin scaPlugin)
				{
					this.scaLedgerPlugin = scaPlugin;
				}
				
				this.loadedPlugins.add(plugin);
				LOGGER.info("Plugin initialized successfully: {}",
					plugin.getName());
			}
			catch (Exception e)
			{
				LOGGER.error("Failed to initialize plugin: {} - {}",
					plugin.getClass().getName(), e.getMessage(), e);
				AlertBox.showError(this.primaryStage, "Plugin Load Error");
			}
			
		}
		
		LOGGER.info("Plugin discovery complete. Loaded {} plugins.",
			this.loadedPlugins.size());
		
		// MenuBar must be built *after* plugins are loaded so
		// they can add their items.
		MenuBar menuBar = buildMenuBar();
		this.mainView.setMenuBar(menuBar);
		
		Scene scene = new Scene(this.mainView, 1000, 700); // Use mainView for
															// the scene
		ThemeManager.applyTheme(scene);
		scene.getAccelerators().put(
			new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN),
			() ->
			{
				
				if (this.mainView != null)
				{
					this.mainView
						.showPanel(MainApplicationView.PanelType.JOURNAL);
					SkeletonJournalPanel panel =
						this.mainView.getJournalPanel();
					
					if (panel != null)
					{
						panel.focusSearchField();
					}
					
				}
				
			});
		this.primaryStage.setScene(scene);
		applyGlobalSettings();
		
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
		MenuBar bar = (this.applicationContext != null &&
			this.applicationContext.getMenuBar() != null) ?
				this.applicationContext.getMenuBar() : new MenuBar();
		bar.getMenus().clear();
		
		/* FILE */
		Menu companyMenu = new Menu("Company");
		this.miOpen = add(companyMenu, "Open Company", e -> doOpenCompany());
		this.miOpen.setAccelerator(
			new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		this.miClose = add(companyMenu, "Close Company", e -> doCloseCompany());
		this.miClose.setAccelerator(
			new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
		this.miSave = add(companyMenu, "Save Company", e -> doSaveCompany());
		this.miSave.setAccelerator(
			new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		
		this.miImportCoaXlsx = add(companyMenu, "Import COA (XLSX)",
			e -> new ImportCoaXlsxActionFX(this.primaryStage).handle(e));
		this.miExportCoaXlsx = add(companyMenu, "Export COA (XLSX)",
			e -> new ExportCoaXlsxActionFX(this.primaryStage).handle(e));
		bar.getMenus().add(companyMenu);
		
		/* IMPORT */
		this.importMenu = new Menu("Import");
		this.miImportFile =
			add(this.importMenu, "Import Financial (OFX, QFX) File...",
				e -> new ImportFileActionFX(this.primaryStage).handle(e));
		
		this.miImportScaExcel = new MenuItem("Import Outlands Ledger...");
		this.miImportScaExcel.setOnAction(
			e -> new ImportFromOutlandsLedgerActionFX(this.primaryStage,
				this.scaExcelViewerPanel).handle(e));
		this.importMenu.getItems().add(this.miImportScaExcel);
		
		this.miPersistScaLedger = add(this.importMenu,
			"Import SCA Ledger...",
			e -> new ImportSCALedgerActionFX(this.primaryStage)
				.handle(e));
		bar.getMenus().add(this.importMenu);
		
		/* EXPORT */
		this.exportMenu = new Menu("Export");
		this.miSaveScaModifiedCopy =
			new MenuItem("Save Modified SCA Workbook...");
		
		if (this.scaLedgerPlugin != null)
		{
			this.miSaveScaModifiedCopy.setOnAction(
				e -> new SaveModifiedCopyActionFX(this.primaryStage,
					this.scaLedgerPlugin).handle(e));
		}
		else
		{
			this.miSaveScaModifiedCopy.setDisable(true);
		}
		
		this.exportMenu.getItems().add(this.miSaveScaModifiedCopy);
		
		this.miExportStatementOfx = add(this.exportMenu,
			"Export Account Statement (OFX/QFX)...",
			e -> new ExportFileActionFX(this.primaryStage).handle(e));
		bar.getMenus().add(this.exportMenu);
		
		/* EDIT */
		Menu edit = new Menu("Edit");
		this.miEditCompany =
			add(edit, "Create or Edit Company", e -> startCreateWizard());
		this.miEditCoa = add(edit, "Edit Chart of Accounts",
			e -> ((MainApplicationView) this.root)
				.showPanel(MainApplicationView.PanelType.COA));
		this.miEditJournal =
			add(edit, "Edit Journal", e -> ((MainApplicationView) this.root)
				.showPanel(MainApplicationView.PanelType.JOURNAL));
		this.miEditJournal.setAccelerator(
			new KeyCodeCombination(KeyCode.J, KeyCombination.CONTROL_DOWN));
		
		
		bar.getMenus().add(edit);
		
		/* RUN */
		this.run = new Menu("Run");
		add(this.run, "Documents & Attachments",
			e -> showPanel(new DocumentsPanelFX(ServiceContainer.dss),
				"Documents"));
		add(this.run, "Inventory & Depreciation",
			e -> showPanel(new InventoryPanelFX(ServiceContainer.iss, null),
				"Inventory"));
		add(this.run, "Funds & Fund Accounting",
			e -> showPanel(new FundsPanelFX(ServiceContainer.fas, null),
				"Funds"));
		add(this.run, "Excel Template Report...",
			e -> new ExcelTemplateReportActionFX(this.primaryStage).handle(e));
		add(this.run, "Reconcile",
			e -> showPanel(
				new LedgerReconcilePanelFX(new ReconciliationService()),
				"Reconciliation"));
		bar.getMenus().add(this.run);
		
		
		/* PANELS */
		this.panels = new Menu("Panels");
		add(this.panels, "Donors",
			e -> showPanel(
				new DonorsPanelFX(ServiceContainer.donorService, null),
				"Donors"));
		add(this.panels, "Donations",
			e -> showPanel(new DonationsPanelFX(this.primaryStage),
				"Donations"));
		add(this.panels, "Grants",
			e -> showPanel(new GrantsPanelFX(ServiceContainer.grantsService),
				"Grants"));
		add(this.panels, "Undeposited Funds",
			e -> showPanel(
				new UndepositedFundsPanelFX(
					ServiceContainer.undepositedFundsService),
				"Undeposited Funds"));
		add(this.panels, "Sales & COG",
			e -> showPanel(
				new SalesAndCOGPanelFX(ServiceContainer.salesService, null),
				"Sales & COG"));
		bar.getMenus().add(this.panels);
		
		bar.getMenus().add(createDatabaseMenu());
		
		/* SETTINGS */
		Menu settings = new Menu("Settings");
		add(settings, "Show Settings", e -> {
			ensureSettingsLoaded();
			showPanel(new SettingsPanelFX(this.primaryStage,
				this.settingsService, () ->
				{
					applyGlobalSettings();
					
					if (CurrentCompany.isOpen())
					{
						scheduleAutosave();
					}
					
				}),
				"Settings");
		});
		bar.getMenus().add(settings);
		
		/* HELP */
		Menu help = new Menu("Help");
		add(help, "Help",
			e -> showPanel(new HelpPanelFX(this.primaryStage), "Help"));
		bar.getMenus().add(help);
		
		/* PLUGINS */
		this.pluginsMenu = new Menu("Plugins");
		
		if (this.loadedPlugins == null || this.loadedPlugins.isEmpty())
		{
			MenuItem none = new MenuItem("No plugins available");
			none.setDisable(true);
			this.pluginsMenu.getItems().add(none);
		}
		
		bar.getMenus().add(this.pluginsMenu);
		
		// Add plugin menu items
		LOGGER.info("Adding plugin menu items. Number of plugins: {}",
			(this.loadedPlugins != null ? this.loadedPlugins.size() : 0));
		
		if (this.loadedPlugins != null)
		{
			
			for (Plugin plugin : this.loadedPlugins)
			{
				
				try
				{
					LOGGER.info("Adding menu items for plugin: {}",
						plugin.getName());
					addPluginInfoMenuItem(plugin);
					plugin.addMenuItems(bar); // 'bar' is the MenuBar instance
				}
				catch (Exception ex)
				{
					LOGGER.warn("Plugin {} failed to add its menu items: {}",
						plugin.getName(), ex.getMessage(), ex);
				}
				
			}
			
		}
		
		return bar;
		
	}
	
	/**
	 * Adds the plugin info menu item.
	 *
	 * @param plugin the plugin
	 */
	private void addPluginInfoMenuItem(Plugin plugin)
	{
		
		if (this.pluginsMenu == null || plugin == null)
		{
			return;
		}
		
		MenuItem item = new MenuItem(plugin.getName());
		item.setOnAction(e -> showPluginDetails(plugin));
		this.pluginsMenu.getItems().add(item);
		
	}
	
	/**
	 * Show plugin details.
	 *
	 * @param plugin the plugin
	 */
	private void showPluginDetails(Plugin plugin)
	{
		
		if (plugin == null)
		{
			return;
		}
		
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		
		if (this.primaryStage != null)
		{
			alert.initOwner(this.primaryStage);
		}
		
		alert.setTitle("Plugin Information");
		alert.setHeaderText(plugin.getName());
		String description = Optional.ofNullable(plugin.getDescription())
			.filter(desc -> !desc.isBlank())
			.orElse("No description available.");
		alert.setContentText(description);
		alert.showAndWait();
		
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
	private static MenuItem add(Menu menu, String label,
		EventHandler<ActionEvent> handler)
	{
		MenuItem item = new MenuItem(label);
		item.setOnAction(handler);
		menu.getItems().add(item);
		return item;
		
	}
	
	/**
	 * Creates the database menu.
	 *
	 * @return the menu
	 */
	private Menu createDatabaseMenu()
	{
		Menu db = new Menu("Database");
		add(db, "Open/Create H2 DB...", e -> handleOpenOrCreateDatabase());
		add(db, "Import Legacy .npbk Archive...",
			e -> handleImportLegacyArchive());
		add(db, "Import H2 script into DB...",
			e -> handleImportScriptIntoDatabase());
		add(db, "Run SQL Query...", e -> showPanel(new SqlQueryPanelFX(),
			"SQL Query"));
		return db;
		
	}
	
	/**
	 * Handle open or create database.
	 */
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
				Path chooserDir = Files.isDirectory(lastPath) ? lastPath :
					lastPath.getParent();
				
				if (chooserDir != null && Files.isDirectory(chooserDir))
				{
					chooser.setInitialDirectory(chooserDir.toFile());
				}
				
			}
			catch (InvalidPathException ex)
			{
				LOGGER.debug("Ignoring invalid DB path preference: {}",
					lastDatabasePath, ex);
			}
			
		}
		
		ButtonType openExisting = new ButtonType("Open Existing");
		ButtonType createNew = new ButtonType("Create New");
		Alert choiceDialog = new Alert(Alert.AlertType.CONFIRMATION);
		choiceDialog.setTitle("Select Database Action");
		choiceDialog.setHeaderText(
			"Would you like to open an existing database or create a new one?");
		choiceDialog.getButtonTypes().setAll(openExisting, createNew,
			ButtonType.CANCEL);
		
		Optional<ButtonType> selection = choiceDialog.showAndWait();
		
		if (selection.isEmpty() || selection.get() == ButtonType.CANCEL)
		{
			return;
		}
		
		boolean creating = selection.get() == createNew;
		File file = creating ? chooser.showSaveDialog(this.primaryStage) :
			chooser.showOpenDialog(this.primaryStage);
		
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
						"The selected file does not contain an H2 database: " +
							dataFile.toAbsolutePath());
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
			PreferencesManager
				.setLastDatabasePath(dataFile.toAbsolutePath().toString());
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
			LOGGER.error("Failed to open DB: {}", base, ex);
			Alert alert = new Alert(Alert.AlertType.ERROR,
				"Failed to open DB: " + ex.getMessage());
			alert.setHeaderText("Database Error");
			alert.showAndWait();
		}
		
	}
	
	/**
	 * Handle import legacy archive.
	 */
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
			new FileChooser.ExtensionFilter("Legacy Archives (*.npbk, *.json)",
				"*.npbk", "*.json"),
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
				"Imported legacy archive into the database.\nCompany record id: " +
					id);
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
			LOGGER.error("Failed to import legacy archive", ex);
			Alert alert = new Alert(Alert.AlertType.ERROR,
				"Failed to import legacy archive: " + ex.getMessage());
			alert.setHeaderText("Import Failed");
			alert.showAndWait();
		}
		
	}
	
	/**
	 * Handle import script into database.
	 */
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
		fc.getExtensionFilters()
			.add(new FileChooser.ExtensionFilter("SQL scripts", "*.sql"));
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
			LOGGER.error("Import failed for file: {}", file, ex);
			Alert alert = new Alert(Alert.AlertType.ERROR,
				"Import failed: " + ex.getMessage());
			alert.setHeaderText("Import Error");
			alert.showAndWait();
		}
		
	}
	
	private static Path normalizeH2Base(Path chosen)
	{
		String fileName = chosen.getFileName() != null ?
			chosen.getFileName().toString() : chosen.toString();
		
		if (fileName.endsWith(".mv.db"))
		{
			String trimmed =
				fileName.substring(0, fileName.length() - ".mv.db".length());
			Path parent = chosen.getParent();
			return parent == null ? Path.of(trimmed) : parent.resolve(trimmed);
		}
		
		return chosen;
		
	}
	
	/**
	 * Resolve H 2 data file.
	 *
	 * @param base the base
	 * @return the path
	 */
	private static Path resolveH2DataFile(Path base)
	{
		String fileName = base.getFileName() != null ?
			base.getFileName().toString() : base.toString();
		
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
	
	private void ensureSettingsLoaded()
	{
		
		if (this.settingsLoaded || !Database.isInitialized())
		{
			return;
		}
		
		try
		{
			this.settingsService.loadSettings(null);
			this.settingsLoaded = true;
			applyGlobalSettings();
		}
		catch (IOException ex)
		{
			LOGGER.debug("Unable to load settings", ex);
		}
		
	}
	
	private void applyGlobalSettings()
	{
		SettingsModel settings = this.settingsService.getSettings();
		Locale locale = settings.getLanguage() != null &&
			!settings.getLanguage().isBlank() ?
				Locale.forLanguageTag(settings.getLanguage()) :
				Locale.getDefault();
		FormatUtils.configureLocale(locale, settings.getDefaultCurrency());
		FormatUtils.setCurrencyFormat(settings.getCurrencyFormat());
		
		if (this.primaryStage != null)
		{
			String title = settings.getOrganizationName();
			this.primaryStage.setTitle(title != null && !title.isBlank() ?
				title + " - Nonprofit Bookkeeping" : "Nonprofit Bookkeeping");
			
			if (this.primaryStage.getScene() != null)
			{
				ThemeManager.applyTheme(this.primaryStage.getScene(),
					settings.getTheme());
			}
			
		}
		
		if (this.mainView != null)
		{
			this.mainView.applyAccountDetailsDefaults(
				this.settingsService.resolveDefaultReportPeriod(),
				this.settingsService.resolveFiscalYearStart(),
				settings.isEnableYearToDateOption(),
				settings.isEnableFullYearOption(),
				settings.isEnableLastMonthOption());
		}
		
	}
	
	/**
	 * Schedule autosave.
	 */
	private void scheduleAutosave()
	{
		cancelAutosave();
		
		if (!CurrentCompany.isOpen())
		{
			return;
		}
		
		SettingsModel settings = this.settingsService.getSettings();
		
		if (!settings.isAutosaveEnabled() ||
			settings.getAutosaveIntervalMinutes() <= 0)
		{
			return;
		}
		
		if (this.autosaveExecutor == null)
		{
			this.autosaveExecutor =
				Executors.newSingleThreadScheduledExecutor(r ->
				{
					Thread t = new Thread(r, "autosave-worker");
					t.setDaemon(true);
					return t;
				});
		}
		
		long interval = settings.getAutosaveIntervalMinutes();
		this.autosaveFuture =
			this.autosaveExecutor.scheduleAtFixedRate(this::performAutosave,
				interval, interval, TimeUnit.MINUTES);
		
	}
	
	/**
	 * Cancel autosave.
	 */
	private void cancelAutosave()
	{
		
		if (this.autosaveFuture != null)
		{
			this.autosaveFuture.cancel(false);
			this.autosaveFuture = null;
		}
		
	}
	
	/**
	 * Perform autosave.
	 */
	private void performAutosave()
	{
		
		if (!Database.isInitialized() || !CurrentCompany.isOpen())
		{
			return;
		}
		
		try
		{
			CurrentCompany.persist();
			LOGGER.debug("Background autosave completed");
		}
		catch (IOException ex)
		{
			LOGGER.warn("Autosave failed", ex);
		}
		
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
		
		if (databaseReady)
		{
			ensureSettingsLoaded();
		}
		
		boolean creatingCompany = (newState == AppState.CREATING_COMPANY);
		boolean companyOpen = databaseReady && CurrentCompany.isOpen();
		
		if (!companyOpen)
		{
			cancelAutosave();
		}
		
		this.miOpen
			.setDisable(!databaseReady || creatingCompany || companyOpen);
		this.miClose.setDisable(!companyOpen || creatingCompany);
		this.miSave.setDisable(!companyOpen || creatingCompany);
		this.miEditCompany.setDisable(!databaseReady || creatingCompany);
		this.miEditCoa.setDisable(!companyOpen || creatingCompany);
		
		this.miEditJournal.setDisable(!companyOpen || creatingCompany);
		this.miImportCoaXlsx.setDisable(!companyOpen || creatingCompany);
		this.miExportCoaXlsx.setDisable(!companyOpen || creatingCompany);
		
		if (this.miImportFile != null)
		{
			this.miImportFile.setDisable(!companyOpen || creatingCompany);
		}
		
		if (this.miPersistScaLedger != null)
		{
			this.miPersistScaLedger
				.setDisable(!companyOpen || creatingCompany);
		}
		
		if (this.miExportStatementOfx != null)
		{
			this.miExportStatementOfx
				.setDisable(!companyOpen || creatingCompany);
		}
		
		if (this.miLoadScaXlsm != null)
		{
			this.miLoadScaXlsm
				.setDisable(this.scaLedgerPlugin == null || creatingCompany);
		}
		
		if (this.miImportScaExcel != null)
		{
			this.miImportScaExcel
				.setDisable(creatingCompany);
		}
		
		if (this.miSaveScaModifiedCopy != null)
		{
			this.miSaveScaModifiedCopy
				.setDisable(this.scaLedgerPlugin == null || creatingCompany);
		}
		
		this.run.setDisable(!companyOpen || creatingCompany);
		this.panels.setDisable(!companyOpen || creatingCompany);
		
		if (this.mainView != null)
		{
			this.mainView.updateCompanyOpenState(companyOpen);
		}
		
	}
	
	/**
	 * Reload settings.
	 */
	private void reloadSettings()
	{
		
		if (!Database.isInitialized())
		{
			return;
		}
		
		try
		{
			this.settingsService.loadSettings(null);
			this.cachedSettings = this.settingsService.getSettings();
			applySettings(this.cachedSettings);
		}
		catch (IOException ex)
		{
			LOGGER.debug("Unable to load settings", ex);
		}
		
	}
	
	/**
	 * Apply settings.
	 *
	 * @param settings the settings
	 */
	private void applySettings(SettingsModel settings)
	{
		
		if (settings == null)
		{
			return;
		}
		
		Locale locale = settings.getCurrencyLocale();
		FormatUtils.setCurrencyLocale(locale);
		FormatUtils.setCurrencyFormat(settings.getCurrencyFormat());
		
		if (this.primaryStage != null && this.primaryStage.getScene() != null)
		{
			ThemeManager.applyTheme(this.primaryStage.getScene(),
				settings.getTheme());
		}
		
	}
	
	/**
	 * Configure autosave.
	 */
	private void configureAutosave()
	{
		cancelAutosave();
		
		if (this.cachedSettings == null)
		{
			return;
		}
		
		int interval =
			Math.max(0, this.cachedSettings.getAutosaveIntervalMinutes());
		
		if (interval <= 0)
		{
			return;
		}
		
		if (this.autosaveExecutor == null || this.autosaveExecutor.isShutdown())
		{
			this.autosaveExecutor =
				Executors.newSingleThreadScheduledExecutor(r ->
				{
					Thread t = new Thread(r, "npbk-autosave");
					t.setDaemon(true);
					return t;
				});
		}
		
		this.autosaveFuture = this.autosaveExecutor.scheduleAtFixedRate(() -> {			
			if (!CurrentCompany.isOpen())
			{
				return;
			}
			
			try
			{
				CurrentCompany.persist();
			}
			catch (IOException ex)
			{
				LOGGER.warn("Autosave failed", ex);
			}
			
		}, interval, interval, TimeUnit.MINUTES);
		
	}
	
	
	/**
	 * Handle company opened.
	 *
	 * @param company the company
	 */
	private void handleCompanyOpened(Company company)
	{
		
		if (company == null)
		{
			return;
		}
		
		setState(AppState.COMPANY_OPEN);
		ensureSettingsLoaded();
		applyGlobalSettings();
		scheduleAutosave();
		
		reloadSettings();
		configureAutosave();
		
		if (company.getCompanyFile() != null && Database.isInitialized())
		{
			String path = company.getCompanyFile().getAbsolutePath();
			this.cachedSettings.setLastOpenedFile(path);
			PreferencesManager.setLastDatabasePath(path);
			
			File parent = company.getCompanyFile().getParentFile();
			
			if (parent != null)
			{
				String dir = parent.getAbsolutePath();
				this.cachedSettings.setDefaultDirectory(dir);
				PreferencesManager.setLastDirectory(dir);
				PreferencesManager.setLastWriteDirectory(dir);
			}
			
			try
			{
				this.settingsService.saveSettings(null);
			}
			catch (IOException ex)
			{
				LOGGER.debug("Unable to persist updated file preferences", ex);
			}
			
		}
		
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
			OpenCompanyFileActionFX openCompanyFileActionFX =
				new OpenCompanyFileActionFX(
					this.primaryStage,
					() -> handleCompanyOpened(CurrentCompany.getCompany()));
		}
		catch (Exception e)
		{
			AlertBox.showError(this.primaryStage,
				"Failed to open company: " + e.getMessage());
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
				cancelAutosave();
				
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
			AlertBox.showError(this.primaryStage,
				"Failed to close company: " + e.getMessage());
		}
		
		// Switch view back to dashboard
		if (this.mainView != null)
		{
			this.mainView.showCompanySelection();
		}
		
		cancelAutosave();
		
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
			AlertBox.showError(this.primaryStage,
				"Failed to save company: " + ex.getMessage());
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
			AlertBox.showError(this.primaryStage,
				"Error during company setup: " + e.getMessage());
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
	@Override
	public void stop() throws Exception
	{
		LOGGER.info("Application stopping. Shutting down plugins.");
		
		cancelAutosave();
		
		if (this.autosaveExecutor != null)
		{
			this.autosaveExecutor.shutdownNow();
		}
		
		if (this.loadedPlugins != null)
		{
			
			for (Plugin plugin : this.loadedPlugins)
			{
				
				try
				{
					LOGGER.info("Shutting down plugin: {}", plugin.getName());
					plugin.shutdown();
				}
				catch (Exception e)
				{
					LOGGER.warn("Error shutting down plugin: {} - {}",
						plugin.getName(), e.getMessage(), e);
				}
				
			}
			
		}
		
		doSaveCompany();
		super.stop();
		
	}
	
}
