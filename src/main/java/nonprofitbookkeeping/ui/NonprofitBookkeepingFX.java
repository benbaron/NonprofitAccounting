
package nonprofitbookkeeping.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nonprofitbookkeeping.core.ApplicationContext;
import nonprofitbookkeeping.core.ApplicationContextImpl;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.plugin.Plugin;
import nonprofitbookkeeping.service.*;
import nonprofitbookkeeping.ui.actions.*;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.panels.*;


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
	/** Reference to the dashboard panel, used as a fallback or initial view. */
	private DashboardPanelFX dashboard; // Potentially part of MainApplicationView's default tabs
	/** Instance managing the currently loaded company data. Suppressed unused warning as it's initialized. */
	@SuppressWarnings("unused") private CurrentCompany c;
	
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
	/** Menu item for importing COA from JSON. */
	private MenuItem miImportCoaJson;
	/** Menu item for exporting COA from JSON. */
	private MenuItem miExportCoaJson;
	
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
	
	/**
	 * Static inner class acting as a container for singleton service instances.
	 * This provides a central point of access for various services used throughout the application.
	 */
	private static final class ServiceContainer
	{
		/** Singleton instance of {@link InventoryService}. */
		private static final InventoryService iss = new InventoryService();
		/** Singleton instance of {@link ReportService}. */
		private static final ReportService reportService = new ReportService();
		/** Singleton instance of {@link BudgetService}. */
		private static final BudgetService budgetService = new BudgetService();
		/** Singleton instance of {@link ReportConfigurationService}. */
		private static final ReportConfigurationService reportConfigurationService =
			new ReportConfigurationService();
		private static final DocumentStorageService dss = new DocumentStorageService();
		private static final FundAccountingService fas = new FundAccountingService();
		
		// FileImportService and FileExportService are typically used via Actions,
		// and thus not instantiated here as globally managed singletons.
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
		
		stage.getIcons().addAll(new Image(getClass().getResourceAsStream("../../cg-128px.png")));
		this.primaryStage = stage;
		this.c = new CurrentCompany();
		this.dashboard = new DashboardPanelFX();
		// this.root = new BorderPane(); // Old root
		MainApplicationView mainView = new MainApplicationView();
		this.root = mainView; // Assign MainApplicationView to root
		// this.root.setCenter(this.dashboard); // MainApplicationView handles its own center
		
		// Instantiate ApplicationContextImpl
		// Services are passed from the static ServiceContainer
		this.applicationContext = new ApplicationContextImpl(
			this.primaryStage,
			ServiceContainer.reportService,
			ServiceContainer.budgetService,
			ServiceContainer.reportConfigurationService,
			ServiceContainer.iss, // InventoryService
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
		// mainView.setTop(buildMenuBar()); // Old way of setting menu bar directly
		MenuBar menuBar = buildMenuBar();
		mainView.setMenuBar(menuBar); // New way: Pass MenuBar to MainApplicationView
		
		Scene scene = new Scene(mainView, 1000, 700); // Use mainView for the scene
		this.primaryStage.setScene(scene);
		this.primaryStage.setTitle("Nonprofit Bookkeeping (JavaFX)");
		
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
		Menu file = new Menu("File");
		this.miOpen = add(file, "Open Company File", e -> doOpenCompany());
		this.miClose = add(file, "Close Company File", e -> doCloseCompany());
		this.miSave = add(file, "Save Company File", e -> doSaveCompany());
		this.miImportCoaJson = add(file, "Import COA (JSON)", e -> new ImportCoaJsonActionFX(this.primaryStage).handle(e));
		this.miExportCoaJson = add(file, "Export COA (JSON)", e -> new ExportCoaJsonActionFX(this.primaryStage).handle(e));

		add(file, "Import File", e -> new ImportFileActionFX(this.primaryStage).handle(e));
		add(file, "Export File", e -> new ExportFileActionFX(this.primaryStage).handle(e));
		bar.getMenus().add(file);
		
		/* EDIT */
		Menu edit = new Menu("Edit");
		this.miEditCompany = add(edit, "Create or Edit Company", e -> startCreateWizard());
		// this.miEditCoa = add(edit, "Edit Chart of Accounts", e -> showCoaEditor()); // Old
		this.miEditCoa = add(edit, "Edit Chart of Accounts", e -> ((MainApplicationView)this.root).showPanel(MainApplicationView.PanelType.COA));
		// this.miEditJournal = add(edit, "Edit Journal", e -> showPanel(new JournalPanelFX(), "Journal")); // Old
		this.miEditJournal = add(edit, "Edit Journal", e -> ((MainApplicationView)this.root).showPanel(MainApplicationView.PanelType.JOURNAL));
		
		add(edit, "Open Budget Editor", e -> {
			Company currentCompany = CurrentCompany.getCompany();

			if (currentCompany == null) {
				AlertBox.showError(this.primaryStage, "No company is currently open. Please open or create a company first.");
				return;
			}

			File companyFile = currentCompany.getCompanyFile();
			if (companyFile == null) {
				AlertBox.showError(this.primaryStage, "The current company has not been saved to a file yet. Please save your company before managing budgets.");
				return;
			}

			File companyDir = companyFile.getParentFile();
			if (companyDir == null) {
				// This case is less likely if companyFile is not null and is a valid file path,
				// but it's a good safeguard.
				AlertBox.showError(this.primaryStage, "Could not determine the company's directory from its saved file path. Cannot manage budgets.");
				return;
			}
			
			// Check if the directory actually exists, as an additional safeguard,
			// though BudgetService might also handle this.
			if (!companyDir.exists() || !companyDir.isDirectory()) {
				AlertBox.showError(this.primaryStage, "The company directory '" + companyDir.getAbsolutePath() + "' does not exist or is not a directory. Cannot manage budgets.");
				return;
			}

			// If all checks pass, proceed to open the BudgetPanel
			new BudgetPanel(null, currentCompany.getChartOfAccounts(), new ArrayList<Fund>(),
				ServiceContainer.budgetService, companyDir, null).setVisible(true);
		});
		bar.getMenus().add(edit);
		
		/* RUN */
		this.run = new Menu("Run");
		add(this.run, "Documents & Attachments",
			e -> showPanel(new DocumentsPanelFX(ServiceContainer.dss), "Documents"));
		add(this.run, "Inventory & Depreciation",
			e -> showPanel(new InventoryPanelFX(ServiceContainer.iss), "Inventory"));
		add(this.run, "Funds & Fund Accounting",
			e -> showPanel(new FundsPanelFX(ServiceContainer.fas), "Funds"));
		add(this.run, "Reconcile",
			e -> showPanel(new ReconcilePanelFX(new ReconciliationService()), "Reconciliation"));
		// Note: The SCA Ledger submenu was here. It will be added by the
		// SCALedgerPlugin if loaded.
		bar.getMenus().add(this.run);
		
		/* REPORTS */
		this.reports = new Menu("Reports");
		// add(this.reports, "Generate Reports", e -> { /* implement */}); // Old
		// placeholder
		// add(this.reports, "Show Reports", e -> showPanel(new ReportsPanelFX(), "Reports")); // Old
		add(this.reports, "Show Reports", e -> ((MainApplicationView)this.root).showPanel(MainApplicationView.PanelType.REPORTS));
		add(this.reports, "Show Accounts",
			e -> showPanel(new AccountsPanelFX(new AccountService()), "Chart of Accounts")); // Stays as new window for now
		add(this.reports, "Show Account Activity", e -> {
			Company currentCompany = CurrentCompany.getCompany();
			
			if (currentCompany != null && currentCompany.getLedger() != null)
			{
				showPanel(new AccountsActivityPanelFX(currentCompany.getLedger()),
					"Account Activity");
			}
			else
			{
				AlertBox.showError(this.primaryStage, "No company or ledger open.");
			}
			
		});
		add(this.reports, "Generate Income Statement",
			e -> new GenerateIncomeStatementAction(ServiceContainer.reportService)
				.actionPerformed(null));
		add(this.reports, "Generate Balance Sheet",
			e -> new GenerateBalanceSheetAction(ServiceContainer.reportService)
				.actionPerformed(null));
		add(this.reports, "Generate Trial Balance",
			e -> new GenerateTrialBalanceAction(ServiceContainer.reportService)
				.actionPerformed(null));
		add(this.reports, "Generate Cash Flow Statement",
			e -> new GenerateCashFlowStatementAction(ServiceContainer.reportService)
				.actionPerformed(null));
		add(this.reports, "Generate Budget vs. Actuals Report",
			e -> new GenerateBudgetVsActualsReportAction(ServiceContainer.reportService,
				ServiceContainer.budgetService).actionPerformed(null));
		add(this.reports, "Manage Saved Reports", e -> {
			Company currentCompany = CurrentCompany.getCompany();

			if (currentCompany == null || currentCompany.getCompanyFile() == null ||
				currentCompany.getCompanyFile().getParentFile() == null)
			{
				AlertBox.showError(this.primaryStage,
					"Company context not properly set for managing reports.");
				return;
			}

			new ManageReportConfigurationsDialog(null, ServiceContainer.reportConfigurationService,
				currentCompany.getCompanyFile().getParentFile(),
				new ArrayList<Fund>(), ServiceContainer.reportService).setVisible(true);
		});
		add(this.reports, "Generate Account Activity Detail",
			e -> new GenerateAccountActivityReportAction(ServiceContainer.reportService)
				.actionPerformed(null));
		bar.getMenus().add(this.reports);
		
		/* PANELS */
		this.panels = new Menu("Panels");
		add(this.panels, "Donors", e -> showPanel(new DonorsPanelFX(this.primaryStage), "Donors"));
		add(this.panels, "Donations",
			e -> showPanel(new DonationsPanelFX(this.primaryStage), "Donations"));
		add(this.panels, "Grants", e -> showPanel(new GrantsPanelFX(this.primaryStage), "Grants"));
		add(this.panels, "Sales & COG",
			e -> showPanel(new SalesAndCOGPanelFX(this.primaryStage), "Sales & COG"));
		bar.getMenus().add(this.panels);
		
		/* SETTINGS */
		Menu settings = new Menu("Settings");
		add(settings, "Show Settings",
			e -> showPanel(new SettingsPanelFX(this.primaryStage), "Settings"));
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
		sub.setScene(new Scene(wrapper, 900, 600));
		sub.initOwner(this.primaryStage);
		sub.show();
	}
	
	/*
	// Original showCoaEditor method, now replaced by using MainApplicationView.showPanel()
	private void showCoaEditor()
	{
		Node previousView = this.root.getCenter();
		Company activeCompany = CurrentCompany.getCompany();
		
		if (activeCompany == null || activeCompany.getChartOfAccounts() == null)
		{
			AlertBox.showError(this.primaryStage, "No company or Chart of Accounts open.");
			return;
		}
		
		CoaEditorPanelFX editor = new CoaEditorPanelFX(
			activeCompany.getChartOfAccounts(),
			chart ->
			{
				activeCompany.setChartOfAccounts(chart);
				
				try
				{
					CurrentCompany.persist();
				}
				catch (IOException | ActionCancelledException | NoFileCreatedException ex)
				{
					ex.printStackTrace(); // Consider better error handling
				}
				
			},
			() -> this.root.setCenter(previousView));
		this.root.setCenter(editor);
	}
	*/
	
	/**
	 * Sets the application's operational state and updates the enabled/disabled status
	 * of various menu items accordingly.
	 *
	 * @param newState The new {@link AppState} to set for the application.
	 */
	private void setState(AppState newState)
	{
		this.state = newState;
		boolean companyOpen = (newState == AppState.COMPANY_OPEN);
		boolean noCompany = (newState == AppState.NO_COMPANY);
		boolean creatingCompany = (newState == AppState.CREATING_COMPANY);
		
		this.miOpen.setDisable(companyOpen || creatingCompany);
		this.miClose.setDisable(noCompany || creatingCompany);
		this.miSave.setDisable(noCompany || creatingCompany);
		this.miEditCompany.setDisable(creatingCompany); // Can edit if open, can create if no
														// company
		this.miEditCoa.setDisable(noCompany || creatingCompany);
		this.miEditJournal.setDisable(noCompany || creatingCompany);
		this.miImportCoaJson.setDisable(noCompany || creatingCompany);
		this.miExportCoaJson.setDisable(noCompany || creatingCompany);
		
		this.run.setDisable(noCompany || creatingCompany);
		this.panels.setDisable(noCompany || creatingCompany);
		this.reports.setDisable(noCompany || creatingCompany);
		
		// Plugin menus are added by plugins themselves. The core app enables/disables
		// the top-level "SCA Ledger" menu (or other plugin menus) if those plugins make that possible
		// via ApplicationContext. For now, top-level plugin menus added by plugins will be enabled/disabled
		// along with "Run", "Reports", etc.
		
		// A more granular approach would involve plugins registering their top-level
		// menus with the core app, and the core app managing their disable state based on broader context like
		// COMPANY_OPEN.
		
		// Or, plugins can check ApplicationContext.getCurrentCompany() themselves in
		// their actions.
		
		// The SCALedgerPlugin's menu is added directly to the bar, so it won't be
		// covered by this.run.setDisable(). It should ideally also be disabled if no company is open if its actions
		// require one.
		
		// This can be handled by the plugin itself in its addMenuItems or if the main
		// app manages plugin menus. For now, SCALedgerPlugin's menu is always enabled once added.
		// This is a V2 improvement area.
		// A robust solution would involve plugins registering their top-level menus for state management by the core.
	}
	
	/**
	 * Handles the action to open a company file.
	 * It instantiates and triggers {@link OpenCompanyFileActionFX}.
	 * If successful, the application state is set to {@link AppState#COMPANY_OPEN}.
	 * Errors are displayed using an {@link AlertBox}.
	 * The {@code @SuppressWarnings("unused")} is present because this method is called via JavaFX action event.
	 */
	@SuppressWarnings("unused") private void doOpenCompany()
	{
		
		try
		{
			// The OpenCompanyFileActionFX itself should handle the logic of opening
            // and then potentially calling a method here or using a listener to update state.
            // For now, it's assumed the action completes and state is set.
			OpenCompanyFileActionFX openCompanyFileActionFX =
				new OpenCompanyFileActionFX(this.primaryStage);
            // If action is successful and a company is loaded, it should update CurrentCompany.
            // Then, we can set state.
            if (CurrentCompany.getCompany() != null && CurrentCompany.getCompany().getCompanyFile() != null) { // Check if company actually opened
			    setState(AppState.COMPANY_OPEN);
            } else {
                // If action was cancelled or failed, state might not change or revert to NO_COMPANY.
                // This depends on action's internal logic and if it throws exceptions on failure/cancel.
            }
		}
		catch (Exception e) // Catch broad exceptions from action if it throws them directly
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
	@SuppressWarnings("unused") private void doCloseCompany()
	{
		
		try
		{
			// The CloseCompanyFileAction should handle the logic of closing.
            // This includes saving if necessary/prompting, clearing CurrentCompany, etc.
			CloseCompanyFileAction closeCompanyFileAction =
				new CloseCompanyFileAction(this.primaryStage);
			// After action, set state.
			setState(AppState.NO_COMPANY);
		}
		catch (Exception e)  // Catch broad exceptions from action
		{
			AlertBox.showError(this.primaryStage, "Failed to close company: " + e.getMessage());
		}
		
		// Switch view back to dashboard
		if (this.root instanceof MainApplicationView) {
			((MainApplicationView)this.root).showPanel(MainApplicationView.PanelType.DASHBOARD);
		} else {
			// Fallback or error if root is not what we expect
			this.root.setCenter(this.dashboard);
		}
	}
	
	/**
	 * Handles the action to save the currently open company file.
	 * It instantiates and triggers {@link SaveCompanyFileAction}.
	 * Shows an info message on success or an error message on failure.
	 * The {@code @SuppressWarnings("unused")} is present because this method is called via JavaFX action event.
	 */
	@SuppressWarnings("unused") private void doSaveCompany()
	{
		
		try
		{
            // SaveCompanyFileAction should handle the logic of saving CurrentCompany.
			SaveCompanyFileAction saveCompanyFileAction =
				new SaveCompanyFileAction(this.primaryStage);
            // Assuming the action itself shows success/failure messages or throws on failure.
            // If it returns a status, we could use that:
            // boolean saved = saveCompanyFileAction.execute(); // If it had an execute method
            // if (saved) AlertBox.showInfo(this.primaryStage, "Company saved.");
            // For now, just showing a generic message if no exception.
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
	@SuppressWarnings("unused") private void startCreateWizard()
	{
		AppState saved = getState();
		setState(AppState.CREATING_COMPANY);
		
		try
		{
            // CreateOrEditCompanyActionFX should handle the wizard logic.
            // Upon successful completion, it should update CurrentCompany.
			CreateOrEditCompanyActionFX createOrEditCompanyActionFX =
				new CreateOrEditCompanyActionFX(this.primaryStage);
            // If wizard completes and company is set in CurrentCompany:
            if (CurrentCompany.getCompany() != null) { // Basic check
			    setState(AppState.COMPANY_OPEN);
            } else {
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
