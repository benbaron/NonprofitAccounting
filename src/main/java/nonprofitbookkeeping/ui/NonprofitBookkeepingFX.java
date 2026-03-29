
package nonprofitbookkeeping.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.sql.SQLException;
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
import javafx.scene.control.SeparatorMenuItem;
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
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonJournalPanel;
import nonprofitbookkeeping.plugins.scaledger.SCALedgerPlugin;
import nonprofitbookkeeping.ui.actions.*;
import nonprofitbookkeeping.ui.adapters.LegacyWorkspaceNavigator;
import nonprofitbookkeeping.ui.adapters.MainApplicationViewNavigatorAdapter;
import nonprofitbookkeeping.ui.bootstrap.PluginInitializationService;
import nonprofitbookkeeping.ui.bootstrap.SettingsInitializationService;
import nonprofitbookkeeping.ui.bootstrap.SettingsStartupCoordinator;
import nonprofitbookkeeping.ui.bootstrap.StageDecoratorService;
import nonprofitbookkeeping.ui.commands.LegacyCommandRegistry;
import nonprofitbookkeeping.ui.actions.scaledger.ImportFromOutlandsLedgerActionFX;
import nonprofitbookkeeping.ui.actions.ImportSclxActionFX;
import nonprofitbookkeeping.ui.actions.scaledger.SaveModifiedCopyActionFX;
import nonprofitbookkeeping.plugins.scaledger.ui.PageViewerPanel;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.panels.*;
import nonprofitbookkeeping.tools.H2ScriptCompanyExporter;
import nonprofitbookkeeping.tools.H2ScriptCompanyImporter;


/**
 * Main JavaFX application class for Nonprofit Bookkeeping.
 * <p>
 * Legacy compatibility launcher retained during A/B migration.
 * Runtime startup ownership now belongs to {@code org.nonprofitbookkeeping.ui.MainApp};
 * this class delegates entrypoints to the B-shell.
 * </p>
 */
@Deprecated(forRemoval = false)
public class NonprofitBookkeepingFX extends Application
{
	/** The primary stage of the JavaFX application. */
	private Stage primaryStage;
	/** The root layout pane (a {@link MainApplicationView} instance) for the main scene. */
	private BorderPane root; // Should be MainApplicationView
	/** Strongly typed reference to the main workspace view. */
	private MainApplicationView mainView;
	/** Adapter abstraction over main workspace navigation. */
	private LegacyWorkspaceNavigator workspaceNavigator;
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
	/** Menu item for importing an SCLX file into the data model. */
	private MenuItem miImportSclx;
	/** Menu item for loading an SCA XLSM workbook via the plugin. */
	private MenuItem miLoadScaXlsm;
	/** Menu item for importing an SCA Excel ledger via the plugin. */
	private MenuItem miImportScaExcel;
	/** Menu item for persisting an SCA ledger into the journal tables. */
	private MenuItem miPersistScaLedger;
	/** Menu item for saving a modified copy of an SCA workbook. */
	private MenuItem miSaveScaModifiedCopy;
	
	// Menus that need their state managed
	
	/** Top-level menu for runnable workflows and operational workspaces. */
	private Menu runMenu;
	/** Top-level menu for report navigation shortcuts. */
	private Menu reports;
	/** Top-level menu for donor, grant, and fund-development features. */
	private Menu fundraisingMenu;
	/** Top-level menu that lists available plugins. */
	private Menu pluginsMenu;
	/** Top-level database menu, exposed directly for startup workflows. */
	private Menu databaseMenu;
	
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
	private final StageDecoratorService stageDecoratorService = new StageDecoratorService();
	private final PluginInitializationService pluginInitializationService = new PluginInitializationService();
	private final SettingsInitializationService settingsInitializationService = new SettingsInitializationService();
	private final SettingsStartupCoordinator settingsStartupCoordinator =
		new SettingsStartupCoordinator(this.settingsService,
			this.settingsInitializationService);
	private final LegacyCommandRegistry commandRegistry = new LegacyCommandRegistry();
	private boolean menuCommandsRegistered;
	/** Executor managing background autosave tasks. */
	private ScheduledExecutorService autosaveExecutor;
	/** Handle to the currently scheduled autosave task. */
	private ScheduledFuture<?> autosaveFuture;
	/** Lifecycle events that may require autosave schedule reconciliation. */
	private enum AutosaveLifecycleEvent
	{
		STARTUP,
		COMPANY_OPENED,
		COMPANY_CLOSED,
		SETTINGS_SAVED,
		SHUTDOWN
	}
	/** Service that imports legacy .npbk archives into the active database. */
	private final LegacyNpbkImportService legacyNpbkImportService =
		new LegacyNpbkImportService();
	
	/** Cached settings to avoid redundant database reads. */
	private SettingsModel cachedSettings = new SettingsModel();
	
	
	/**
	 * Static inner class acting as a container for singleton service instances.
	 * This provides a central point of access for various services 
	 * used throughout the application.
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
		org.nonprofitbookkeeping.ui.FxMain.main(args);
	}
	
	/**
	 * Delegates legacy start invocations to the authoritative B-shell startup.
	 *
	 * @param stage The primary {@link Stage} for this application.
	 */
	@Override
	public void start(Stage stage)
	{
		// Legacy shell startup now delegates to the B-shell entrypoint.
		new org.nonprofitbookkeeping.ui.MainApp().start(stage);
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
			
			// present company selection
			if (this.mainView != null)
			{
				this.workspaceNavigator.showCompanySelection();
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
			
			// present company selection panel
			if (this.mainView != null)
			{
				this.workspaceNavigator.showCompanySelection();
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

	/**
	 * Handle export script from database.
	 */
	private void handleExportScriptFromDatabase()
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
		chooser.setTitle("Export H2 SQL Script");
		chooser.getExtensionFilters().setAll(
			new FileChooser.ExtensionFilter("SQL scripts", "*.sql"),
			new FileChooser.ExtensionFilter("All Files", "*.*"));
		File file = chooser.showSaveDialog(this.primaryStage);

		if (file == null)
		{
			return;
		}

		Path outputFile = file.toPath();
		if (!outputFile.getFileName().toString().toLowerCase(Locale.ROOT)
			.endsWith(".sql"))
		{
			outputFile = outputFile.resolveSibling(outputFile.getFileName() + ".sql");
		}

		try
		{
			H2ScriptCompanyExporter.exportScript(outputFile);
			Alert alert = new Alert(Alert.AlertType.INFORMATION,
				"Exported database script to:\n" + outputFile.toAbsolutePath());
			alert.setHeaderText("Export Complete");
			alert.showAndWait();
		}
		catch (IOException | SQLException ex)
		{
			LOGGER.error("Failed to export H2 script: {}", outputFile, ex);
			Alert alert = new Alert(Alert.AlertType.ERROR,
				"Export failed: " + ex.getMessage());
			alert.setHeaderText("Export Error");
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
		try
		{
			settingsStartupCoordinator.ensureSettingsLoaded(this.primaryStage,
				this.mainView);
		}
		catch (IOException ex)
		{
			LOGGER.debug("Unable to load settings", ex);
		}
	}

	private void applyGlobalSettings()
	{
		settingsStartupCoordinator.applyGlobalSettings(this.primaryStage,
			this.mainView);
	}
	
	/**
	 * Schedule autosave.
	 */
	private void scheduleAutosave()
	{
		cancelAutosave();
		
		if (!Database.isInitialized() || !CurrentCompany.isOpen())
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

	private void onAutosaveLifecycleEvent(AutosaveLifecycleEvent event)
	{
		if (event == AutosaveLifecycleEvent.SHUTDOWN)
		{
			cancelAutosave();
			if (this.autosaveExecutor != null)
			{
				this.autosaveExecutor.shutdownNow();
			}
			return;
		}
		scheduleAutosave();
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
		
		if (this.miImportSclx != null)
		{
			this.miImportSclx.setDisable(!companyOpen || creatingCompany);
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
		
		if (this.runMenu != null)
		{
			this.runMenu.setDisable(!companyOpen || creatingCompany);
		}
		
		if (this.fundraisingMenu != null)
		{
			this.fundraisingMenu.setDisable(!companyOpen || creatingCompany);
		}
		
		if (this.databaseMenu != null)
		{
			this.databaseMenu.setDisable(creatingCompany);
		}
		
		// present company open state
		if (this.mainView != null)
		{
			this.workspaceNavigator.updateCompanyOpenState(companyOpen);
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
			settingsStartupCoordinator.applySettings(this.primaryStage,
				this.cachedSettings);
		}
		catch (IOException ex)
		{
			LOGGER.debug("Unable to load settings", ex);
		}
		
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
			
			reloadSettings();
			onAutosaveLifecycleEvent(AutosaveLifecycleEvent.COMPANY_OPENED);
		
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
		
		// present main view panel
		if (this.mainView != null)
		{
			this.workspaceNavigator.showWorkspaceTabs();
			this.workspaceNavigator.showPanel(MainApplicationView.PanelType.DASHBOARD);
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
		CloseCompanyFileAction closeCompanyFileAction;
		try
		{
			closeCompanyFileAction = new CloseCompanyFileAction(this.primaryStage);
		}
		catch (Exception e) // Catch broad exceptions from action
		{
			AlertBox.showError(this.primaryStage,
				"Failed to close company: " + e.getMessage());
			return;
		}

		if (!closeCompanyFileAction.isClosed())
		{
			return; // user cancelled closing
		}

		setState(AppState.NO_COMPANY);
		onAutosaveLifecycleEvent(AutosaveLifecycleEvent.COMPANY_CLOSED);

		if (this.companySelectionPanel != null)
		{
			this.companySelectionPanel.refreshCompanyList();
		}
		
		// Switch view back to dashboard
		if (this.mainView != null)
		{
			this.workspaceNavigator.showCompanySelection();
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
			onAutosaveLifecycleEvent(AutosaveLifecycleEvent.SHUTDOWN);
		
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
