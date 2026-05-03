
package nonprofitbookkeeping.plugins.scaledger;

import nonprofitbookkeeping.core.ApplicationContext;
import nonprofitbookkeeping.plugin.Plugin;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage; // For action constructors

// Import SCA Action classes
import nonprofitbookkeeping.plugins.scaledger.ui.PageViewerPanel; // Added
import nonprofitbookkeeping.ui.actions.InputFileActionFX;
import nonprofitbookkeeping.ui.actions.OutputFileActionFX;
import nonprofitbookkeeping.ui.actions.scaledger.ImportFromOutlandsLedgerActionFX;
import nonprofitbookkeeping.ui.actions.scaledger.LoadXlsmTableActionFX;
import nonprofitbookkeeping.ui.actions.scaledger.SaveModifiedCopyActionFX;

import java.io.File;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Plugin for handling SCA Ledger functionalities.
 * This plugin provides UI elements and actions to load, process, and save
 * SCA ledger data from XLSM and JSON formats. It manages its own state
 * for SCA-specific beans and the currently active SCA file, replacing
 * previous static access patterns.
 */
public class SCALedgerPlugin implements Plugin
{
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(SCALedgerPlugin.class);
	
	/** The Constant PLUGIN_NAME. */
	public static final String PLUGIN_NAME = "SCA Ledger Tools";
	
	/** The application context. */
	private ApplicationContext applicationContext;
	
	/** The sca beans. */
	// To replace static state from BeanShell and NonCompanyFile
	private Map<String, Object> scaBeans; // Formerly BeanShell.beans
	
	/** The current sca file. */
	private File currentScaFile; // Formerly NonCompanyFile.currentFile
	
	/** The page viewer panel. */
	private PageViewerPanel pageViewerPanel; // Added field
	
	// Potentially an instance of PageViewer (once it's not a stub)
	// private PageViewerPanel pageViewerPanel;
	
	/**
	 * {@inheritDoc}
	 * @return The name of this plugin: "SCA Ledger Tools".
	 */
	@Override
	public String getName()
	{
		return PLUGIN_NAME;
		
	}
	
	/**
	 * {@inheritDoc}
	 * @return A description of the plugin's purpose, focusing on SCA ledger processing.
	 */
	@Override
	public String getDescription()
	{
		return "Provides tools for loading, viewing, and processing specialized SCA (Standard Chart of Accounts) formatted ledgers from XLSM, XLSX, and JSON files.";
		
	}
	
	/**
	 * {@inheritDoc}
	 * Initializes the plugin by storing the application context and preparing
	 * internal components like the {@link PageViewerPanel}.
	 * @param context The application context provided by the main application.
	 * @throws Exception If any error occurs during initialization (not thrown by this implementation).
	 */
	@Override
	public void initialize(ApplicationContext context) throws Exception
	{
		this.applicationContext = context;
		this.pageViewerPanel = new PageViewerPanel(); // Added initialization
		LOGGER.info("{} initialized.", PLUGIN_NAME);
		
		// Any other one-time setup for the plugin.
		// For example, if PageViewer was a real component managed by the
		// plugin:
		// this.pageViewerPanel = new PageViewerPanel(); // Assuming PageViewer
		// becomes a real UI panel
	}
	
	/**
	 * {@inheritDoc}
	 * Adds a new "SCA Ledger" top-level menu to the application's main menu bar.
	 * This menu includes items for loading XLSM tables, importing JSON, saving modified copies,
	 * generic file input/output actions (contextualized for SCA), and an undo edit action.
	 * Actions are instantiated and linked to their respective menu items.
	 * @param mainMenuBar The main menu bar of the application.
	 */
	@Override
	public void addMenuItems(MenuBar mainMenuBar)
	{
		LOGGER.info("Adding menu items for {}", PLUGIN_NAME);
		
		Menu scaMenu = new Menu("SCA Ledger");
		// Add this new top-level menu to the main menubar.
		// A more refined approach might add it to a "Tools" or "Plugins" menu
		// if one exists, or the core app could provide a dedicated menu for plugins.
		// For now, adding as a new top-level menu for visibility.
		mainMenuBar.getMenus().add(scaMenu);
		
		Stage primaryStage = (this.applicationContext != null) ?
			this.applicationContext.getPrimaryStage() : null;
		
		// Wire up actions - these actions will need refactoring to use plugin
		// state and potentially the applicationContext. For now, just instantiating
		// them.
		
		// SCA Specific (from nonprofitbookkeeping.ui.actions.scaledger package)
		MenuItem loadXlsmItem = new MenuItem("Load XLSM Table (SCA)");
		loadXlsmItem.setOnAction(
			e -> new LoadXlsmTableActionFX(primaryStage, this).handle(e)); // Pass
																			// plugin
																			// instance
		scaMenu.getItems().add(loadXlsmItem);
		
		MenuItem importExcelItem = new MenuItem("Import from Excel (SCA)");
		importExcelItem.setOnAction(e -> new ImportFromOutlandsLedgerActionFX(primaryStage,
			this.pageViewerPanel).handle(e));
		scaMenu.getItems().add(importExcelItem);
		
		MenuItem saveModifiedItem = new MenuItem("Save Modified Copy (SCA)");
		saveModifiedItem.setOnAction(
			e -> new SaveModifiedCopyActionFX(primaryStage, this).handle(e)); // Pass
																				// plugin
																				// instance
		scaMenu.getItems().add(saveModifiedItem);
		
		// Generic actions - these might be general utilities or need context
		// For now, assuming they are part of the desired SCA menu as per
		// original structure
		MenuItem inputFileItem =
			new MenuItem("Input File (Generic - SCA context)");
		inputFileItem
			.setOnAction(e -> new InputFileActionFX(primaryStage).handle(e));
		scaMenu.getItems().add(inputFileItem);
		
		MenuItem outputFileItem =
			new MenuItem("Output File (Generic - SCA context)");
		outputFileItem
			.setOnAction(e -> new OutputFileActionFX(primaryStage).handle(e));
		scaMenu.getItems().add(outputFileItem);
		
		MenuItem undoEditItem = new MenuItem("Undo Last Edit (SCA)"); // Clarified
																		// name
		// UndoEditAction is Swing-based and its actionPerformed is called with
		// null. This will need refactoring for JavaFX and plugin state.
		undoEditItem.setOnAction(
			e -> new nonprofitbookkeeping.ui.actions.scaledger.UndoEditAction()
				.actionPerformed(null));
		scaMenu.getItems().add(undoEditItem);
		
		// Initial state of the menu (enabled/disabled)
		// This should ideally be managed by the core application's state
		// machine that enables/disables menus based on whether a company is open, etc.
		// The plugin itself should not assume it controls this beyond its own
		// items if needed.
		// If applicationContext is null or no company, the core app should
		// disable this scaMenu.
		// However, if the plugin *must* manage its own menu's top-level disable
		// state:
		if (this.applicationContext == null ||
			this.applicationContext.getCurrentCompany() == null)
		{
			// scaMenu.setDisable(true); // Example: disable if no company
			// context
		}
		
		// The subtask asked to recreate menu as it was. The original "Run" menu
		// which contained SCA
		// was disabled/enabled by the core app's setState. This new top-level
		// "SCA Ledger" menu
		// will also be subject to that if we modify NonprofitBookkeepingFX to
		// manage it,
		// or it will be always enabled unless we add explicit disable logic
		// here based on context.
		// For now, let's assume it's enabled by default and core app state
		// handles broader context.
	}
	
	/**
	 * Override @see nonprofitbookkeeping.plugin.Plugin#shutdown() 
	 */
	@Override
	public void shutdown()
	{
		LOGGER.info("{} shutting down.", PLUGIN_NAME);
		// Release any plugin-specific resources
		this.scaBeans = null;
		this.currentScaFile = null;
		
		if (this.applicationContext != null &&
			this.applicationContext.getMenuBar() != null)
		{
			this.applicationContext.getMenuBar().getMenus()
				.removeIf(menu -> PLUGIN_NAME.equals(menu.getText()) ||
					"SCA Ledger".equals(menu.getText()));
		}
		
	}
	
	/**
	 * Gets the map of SCA-specific beans (objects) managed by this plugin.
	 * This replaces the static `BeanShell.beans`.
	 * @return The map of SCA beans.
	 */
	public Map<String, Object> getScaBeans()
	{
		return this.scaBeans;
		
	}
	
	/**
	 * Sets the map of SCA-specific beans for this plugin.
	 * @param scaBeans The map of SCA beans to set.
	 */
	public void setScaBeans(Map<String, Object> scaBeans)
	{
		this.scaBeans = scaBeans;
		
	}
	
	/**
	 * Gets the currently active SCA file being processed by this plugin.
	 * This replaces the static `NonCompanyFile.currentFile`.
	 * @return The current SCA {@link File}, or null if none is active.
	 */
	public File getCurrentScaFile()
	{
		return this.currentScaFile;
		
	}
	
	/**
	 * Sets the currently active SCA file for this plugin.
	 * @param currentScaFile The SCA {@link File} to set as current.
	 */
	public void setCurrentScaFile(File currentScaFile)
	{
		this.currentScaFile = currentScaFile;
		
	}
	
	/**
	 * Gets the {@link PageViewerPanel} instance associated with this plugin.
	 * This panel is used for displaying SCA ledger pages.
	 * @return The {@link PageViewerPanel} instance.
	 */
	public PageViewerPanel getPageViewerPanel()
	{ // Added getter
		return this.pageViewerPanel;
		
	}
	
}
