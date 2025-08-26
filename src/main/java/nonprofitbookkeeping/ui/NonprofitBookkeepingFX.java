
package nonprofitbookkeeping.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.slf4j.bridge.SLF4JBridgeHandler;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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
import nonprofitbookkeeping.persistence.DatabaseManager;
import nonprofitbookkeeping.ui.actions.*;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.panels.*;
import nonprofitbookkeeping.ui.javafx.BudgetPanelFX;


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
	private DashboardPanelFX dashboard; // Potentially part of
										// MainApplicationView's default tabs
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
	
	// Menus that need their state managed
	/** Top-level menu for running various tools and plugin features. */
	private Menu run;
	/** Top-level menu for generating and viewing reports. */
	private Menu reports;
	/** Top-level menu for accessing different data panels like Donors, Grants etc. */
	private Menu panels;
	
	/** Logger for this class. */
	
	private static final Logger LOGGER =
		LoggerFactory.getLogger(NonprofitBookkeepingFX.class);
	
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
		DatabaseManager.startServer();
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
		this.c = new CurrentCompany();
		CurrentCompany.loadFromDatabase();
		this.dashboard = new DashboardPanelFX();
		MainApplicationView mainView = new MainApplicationView();
		this.root = mainView; // Assign MainApplicationView to root
		
		if (CurrentCompany.isOpen())
		{
			CurrentCompany.markCompanyOpen();
		}
		
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
					"Initializing plugin: " + plugin.getName() + " - " +
						plugin.getDescription());
				plugin.initialize(this.applicationContext);
				this.loadedPlugins.add(plugin);
				LOGGER.info(
					"Plugin initialized successfully: " + plugin.getName());
			}
			catch (Exception e)
			{
				LOGGER.error("Failed to initialize plugin: " +
					plugin.getClass().getName() + " - " + e.getMessage(), e);
				AlertBox.showError(this.primaryStage, "Plugin Load Error");
			}
			
		}
		
		LOGGER.info("Plugin discovery complete. Loaded " +
			this.loadedPlugins.size() + " plugins.");
		
		// MenuBar must be built *after* plugins are loaded so
		// they can add their items.
		MenuBar menuBar = buildMenuBar();
		mainView.setMenuBar(menuBar);
		
		Scene scene = new Scene(mainView, 1000, 700); // Use mainView for the
														// scene
		ThemeManager.applyTheme(scene);
		this.primaryStage.setScene(scene);
		this.primaryStage.setTitle("Nonprofit Bookkeeping");
		
		if (CurrentCompany.isOpen())
		{
			setState(AppState.COMPANY_OPEN);
		}
		else
		{
			setState(AppState.NO_COMPANY);
		}
		
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
                this.miOpen = add(file, "Open Company", e -> doOpenCompany());
                this.miClose = add(file, "Close Company", e -> doCloseCompany());
                this.miSave = add(file, "Save Company File", e -> doSaveCompany());

                Menu importMenu = new Menu("Import");
                this.miImportCoaXlsx = add(importMenu, "Chart of Accounts (XLSX)",
                        e -> new ImportCoaXlsxActionFX(this.primaryStage).handle(e));
                add(importMenu, "Company (.npbk)", e -> {
                        LOGGER.info("Importing company from .npbk");
                        doImportCompany();
                });
                add(importMenu, "File", e -> new ImportFileActionFX(this.primaryStage).handle(e));

                Menu exportMenu = new Menu("Export");
                this.miExportCoaXlsx = add(exportMenu, "Chart of Accounts (XLSX)",
                        e -> new ExportCoaXlsxActionFX(this.primaryStage).handle(e));
                add(exportMenu, "Company (.npbk)", e -> {
                        LOGGER.info("Exporting company to .npbk");
                        doSaveCompany();
                });
                add(exportMenu, "File", e -> new ExportFileActionFX(this.primaryStage).handle(e));

                file.getItems().addAll(importMenu, exportMenu, new SeparatorMenuItem());
                add(file, "Exit", e -> doExit());
                bar.getMenus().add(file);

		
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
		
		add(edit, "Open Budget Editor", e -> {
			Company currentCompany = CurrentCompany.getCompany();
			
			if (!CurrentCompany.isOpen() || currentCompany == null)
			{
				AlertBox.showError(this.primaryStage,
					"No company is currently open. Please open or create a company first.");
				return;
			}
			
			String companyId = currentCompany.getCompanyId();
			
			if (companyId == null || companyId.isBlank())
			{
				AlertBox.showError(this.primaryStage,
					"The current company does not have a database identifier yet. Please save your company before managing budgets.");
				return;
			}
			
			File companyFile = CurrentCompany.getCurrentFile();
			
			if (companyFile == null)
			{
				AlertBox.showError(this.primaryStage,
					"The current company has not been saved to a file yet. Please save your company before managing budgets.");
				return;
			}
			
			File companyDir = companyFile.getParentFile();
			
			if (companyDir == null)
			{
				AlertBox.showError(this.primaryStage,
					"Could not determine the company's directory from its saved file path. Cannot manage budgets.");
				return;
			}
			
			if (!companyDir.exists() || !companyDir.isDirectory())
			{
				AlertBox.showError(this.primaryStage,
					"The company directory '" + companyDir.getAbsolutePath() +
						"' does not exist or is not a directory. Cannot manage budgets.");
				return;
			}
			
			// If all checks pass, open the JavaFX BudgetPanelFX
			BudgetPanelFX panel =
				new BudgetPanelFX(ServiceContainer.budgetService, companyDir,
					currentCompany.getChartOfAccounts(), new ArrayList<Fund>(),
					null);
			showPanel(panel, "Budget Editor");
		});
		bar.getMenus().add(edit);
		
		/* RUN */
		this.run = new Menu("Run");
		add(this.run, "Documents & Attachments",
			e -> showPanel(new DocumentsPanelFX(ServiceContainer.dss),
				"Documents"));
		add(this.run, "Inventory & Depreciation", e -> {
			File dir = null;
			if (CurrentCompany.getCurrentFile() != null)
				dir = CurrentCompany.getCurrentFile().getParentFile();
			showPanel(new InventoryPanelFX(ServiceContainer.iss, dir),
				"Inventory");
		});
		add(this.run, "Funds & Fund Accounting", e -> {
			File dir = null;
			if (CurrentCompany.getCurrentFile() != null)
				dir = CurrentCompany.getCurrentFile().getParentFile();
			showPanel(new FundsPanelFX(ServiceContainer.fas, dir), "Funds");
		});
		add(this.run, "Reconcile",
			e -> showPanel(
				new LedgerReconcilePanelFX(new ReconciliationService()),
				"Reconciliation"));
		bar.getMenus().add(this.run);
		
// /* REPORTS */
		this.reports = new Menu("Reports");
// add(this.reports, "Show Reports", e -> ((MainApplicationView) this.root)
// .showPanel(MainApplicationView.PanelType.REPORTS));
// add(this.reports, "Show Accounts",
//
// e -> showPanel(new AccountsPanelFX(new AccountService()), "Chart of
// Accounts"));
// add(this.reports, "Show Account Activity", e -> {
// Company currentCompany = CurrentCompany.getCompany();
//
//
// if (currentCompany != null && currentCompany.getLedger() != null)
// {
// showPanel(new AccountsActivityPanelFX(currentCompany.getLedger()),
// "Account Activity");
// }
// else
// {
// AlertBox.showError(this.primaryStage, "No company or ledger open.");
// }
//
// });
// add(this.reports, "Generate Reports...",
// e -> new GenerateReportsAction(ServiceContainer.reportService).handle(e));
// add(this.reports, "Generate Income Statement",
// e -> new GenerateIncomeStatementAction(ServiceContainer.reportService)
// .actionPerformed(null));
// add(this.reports, "Generate Balance Sheet",
// e -> new GenerateBalanceSheetAction(ServiceContainer.reportService)
// .actionPerformed(null));
// add(this.reports, "Generate Trial Balance",
// e -> new GenerateTrialBalanceAction(ServiceContainer.reportService)
// .actionPerformed(null));
// add(this.reports, "Generate Cash Flow Statement",
// e -> new GenerateCashFlowStatementAction(ServiceContainer.reportService)
// .actionPerformed(null));
// add(this.reports, "Generate Budget vs. Actuals Report",
// e -> new GenerateBudgetVsActualsReportAction(ServiceContainer.reportService,
// ServiceContainer.budgetService).actionPerformed(null));
// add(this.reports, "Manage Saved Reports", e -> {
//
// if (!CurrentCompany.isOpen())
// {
// AlertBox.showError(this.primaryStage,
// "No company open. Load or create a company first.");
// return;
// }
//
// File companyFile = CurrentCompany.getCurrentFile();
//
// if (companyFile == null)
// {
// Company currentCompany = CurrentCompany.getCompany();
// companyFile = currentCompany != null ? currentCompany.getCompanyFile() :
// null;
// }
//
// File companyDir = (companyFile != null) ? companyFile.getParentFile() : null;
//
// if (companyDir == null)
// {
// AlertBox.showError(this.primaryStage,
// "Company directory not available. Save the company before managing
// reports.");
// return;
// }
//
// new ManageReportConfigurationsDialog(null,
// ServiceContainer.reportConfigurationService,
// companyDir, new ArrayList<Fund>(),
// ServiceContainer.reportService).setVisible(true);
// });
// add(this.reports, "Generate Account Activity Detail",
// e -> new GenerateAccountActivityReportAction(ServiceContainer.reportService)
// .actionPerformed(null));
// bar.getMenus().add(this.reports);
		
		/* PANELS */
		this.panels = new Menu("Panels");
		add(this.panels, "Donors", e -> {
			File dir = null;
			if (CurrentCompany.getCurrentFile() != null)
				dir = CurrentCompany.getCurrentFile().getParentFile();
			showPanel(new DonorsPanelFX(ServiceContainer.donorService, dir),
				"Donors");
		});
		add(this.panels, "Donations",
			e -> showPanel(new DonationsPanelFX(this.primaryStage),
				"Donations"));
		add(this.panels, "Grants", e -> {
			String companyId = null;
			Company cc = CurrentCompany.getCompany();
			
			if (cc != null)
			{
				companyId = cc.getCompanyId();
			}
			
			showPanel(
				new GrantsPanelFX(ServiceContainer.grantsService, companyId),
				"Grants");
		});
		add(this.panels, "Sales & COG", e -> {
			File dir = null;
			if (CurrentCompany.getCurrentFile() != null)
				dir = CurrentCompany.getCurrentFile().getParentFile();
			showPanel(
				new SalesAndCOGPanelFX(ServiceContainer.salesService, dir),
				"Sales & COG");
		});
		bar.getMenus().add(this.panels);
		
		/* SETTINGS */
		Menu settings = new Menu("Settings");
		add(settings, "Show Settings", e -> {
			File dir = null;
			if (CurrentCompany.getCurrentFile() != null)
				dir = CurrentCompany.getCurrentFile().getParentFile();
			showPanel(
				new SettingsPanelFX(this.primaryStage, new SettingsService(),
					dir),
				"Settings");
		});
		bar.getMenus().add(settings);
		
		/* HELP */
		Menu help = new Menu("Help");
		add(help, "Help",
			e -> showPanel(new HelpPanelFX(this.primaryStage), "Help"));
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
					LOGGER.info(
						"Adding menu items for plugin: " + plugin.getName());
					plugin.addMenuItems(bar); // 'bar' is the MenuBar instance
				}
				catch (Exception ex)
				{
					LOGGER.warn("Plugin " + plugin.getName() +
						" failed to add its menu items: " + ex.getMessage(),
						ex);
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
	private static MenuItem add(Menu menu, String label,
		EventHandler<ActionEvent> handler)
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
		boolean companyOpen = (newState == AppState.COMPANY_OPEN);
		boolean noCompany = (newState == AppState.NO_COMPANY);
		boolean creatingCompany = (newState == AppState.CREATING_COMPANY);
		
		this.miOpen.setDisable(companyOpen || creatingCompany);
		this.miClose.setDisable(noCompany || creatingCompany);
		this.miSave.setDisable(noCompany || creatingCompany);
		this.miEditCompany.setDisable(creatingCompany);
		this.miEditCoa.setDisable(noCompany || creatingCompany);
		this.miEditJournal.setDisable(noCompany || creatingCompany);
		this.miImportCoaXlsx.setDisable(noCompany || creatingCompany);
		this.miExportCoaXlsx.setDisable(noCompany || creatingCompany);
		
		this.run.setDisable(noCompany || creatingCompany);
		this.panels.setDisable(noCompany || creatingCompany);
		this.reports.setDisable(noCompany || creatingCompany);
		
		if (this.root instanceof MainApplicationView)
		{
			((MainApplicationView) this.root)
				.updateCompanyOpenState(companyOpen);
		}
		
	}
	
        /**
         * Handles the action to open the company stored in the database.
         * If a company exists, it is marked open and menu options are enabled via
         * {@link #setState(AppState)}.  If no company is present, a warning alert
         * is shown prompting the user to import or create one.
         */
        private void doOpenCompany()
        {
                logDatabaseState();
                LOGGER.info("Opening company from database");

                nonprofitbookkeeping.persistence.DatabaseService db =
                        new nonprofitbookkeeping.persistence.DatabaseService();
                java.util.List<nonprofitbookkeeping.persistence.entity.CompanyEntity> entities =
                        db.listCompanyEntities();

                if (entities.isEmpty())
                {
                        AlertBox.showWarning(this.primaryStage,
                                "No company found. Please import or create a company first.");
                        return;
                }

                if (entities.size() == 1)
                {
                        long id = entities.get(0).getId();
                        java.util.Optional<nonprofitbookkeeping.model.Company> loaded =
                                db.loadCompany(id);
                        if (loaded.isPresent())
                        {
                                CurrentCompany.forceCompanyLoad(loaded.get());
                                setState(AppState.COMPANY_OPEN);
                                return;
                        }
                }

                AlertBox.showWarning(this.primaryStage,
                        "Database contains multiple or invalid company records.");
                nonprofitbookkeeping.ui.panels.FixDatabaseWizardFX.Result result =
                        nonprofitbookkeeping.ui.panels.FixDatabaseWizardFX.show(this.primaryStage, entities);
                if (result.cancelled)
                {
                        return;
                }
                for (Long id : result.deleteIds)
                {
                        db.delete(id);
                }
                if (result.openId != null)
                {
                        java.util.Optional<nonprofitbookkeeping.model.Company> loaded =
                                db.loadCompany(result.openId);
                        if (loaded.isPresent())
                        {
                                CurrentCompany.forceCompanyLoad(loaded.get());
                                setState(AppState.COMPANY_OPEN);
                        }
                        else
                        {
                                AlertBox.showWarning(this.primaryStage,
                                        "Selected company could not be loaded.");
                        }
                }

        }

        /**
         * Imports a company from a <code>.npbk</code> file.  This retains the
         * previous behaviour of {@link #doOpenCompany()} prior to database-backed
         * companies and is used by the Import menu option.
         */
        private void doImportCompany()
        {
                LOGGER.info("Importing company file");

                try
                {
                        OpenCompanyFileActionFX action =
                                new OpenCompanyFileActionFX(this.primaryStage);
                        action.run();

                        if (CurrentCompany.isOpen())
                        {
                                LOGGER.info("Company imported: "
                                        + CurrentCompany.getCompany().getName());
                                // Persist the imported company into the embedded H2 database
                                // and ensure the database contents are flushed to the on-disk
                                // *.db file so it can be reopened via the "Open" menu.
                                CurrentCompany.flushToDatabase();
                                setState(AppState.COMPANY_OPEN);
                        }
                }
                catch (jakarta.persistence.EntityExistsException e)
                {
                        LOGGER.log(Level.SEVERE,
                                "Import failed: company already exists in database", e);
                        AlertBox.showError(this.primaryStage,
                                "Import failed: company already exists in database.");
                }
                catch (Exception e)
                {
                        LOGGER.log(Level.SEVERE, "Failed to import company", e);
                        AlertBox.showError(this.primaryStage,
                                "Failed to import company: " + e.getMessage());
                }
        }

        private void logDatabaseState()
        {
                java.nio.file.Path dbFile = java.nio.file.Paths.get("./data/nonprofit.mv.db");
                try
                {
                        boolean exists = java.nio.file.Files.exists(dbFile);
                        long size = exists ? java.nio.file.Files.size(dbFile) : 0L;
                        long count = new nonprofitbookkeeping.persistence.DatabaseService()
                                .countCompanies();
                        LOGGER.info(
                                "Database file {} exists: {}, size: {} bytes, stored companies: {}",
                                dbFile.toAbsolutePath(), exists, size, count);
                }
                catch (Exception e)
                {
                        LOGGER.log(Level.WARNING,
                                "Unable to determine database file state", e);
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
		if (this.root instanceof MainApplicationView)
		{
			((MainApplicationView) this.root)
				.showPanel(MainApplicationView.PanelType.DASHBOARD);
		}
		else
		{
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
        private void doSaveCompany()
        {
                LOGGER.info("Saving company file");
                try
                {
                        SaveCompanyFileAction saveCompanyFileAction =
                                new SaveCompanyFileAction(this.primaryStage);
                        AlertBox.showInfo(this.primaryStage, "Company saved.");
                }
                catch (Exception ex)
                {
                        LOGGER.log(Level.SEVERE, "Failed to save company", ex);
                        AlertBox.showError(this.primaryStage,
                                "Failed to save company: " + ex.getMessage());
                }

        }

        /**
         * Handles application exit.  The currently open company is flushed to the
         * database and, if a backup file has been specified, persisted to that
         * file before being marked closed.  Finally the JavaFX platform is
         * exited.
         */
        private void doExit()
        {
                LOGGER.info("Exit menu selected");

                try
                {
                        if (CurrentCompany.getCurrentFile() != null)
                        {
                                CurrentCompany.persist();
                        }
                        else
                        {
                                CurrentCompany.flushToDatabase();
                        }
                }
                catch (Exception e)
                {
                        LOGGER.log(Level.SEVERE, "Failed to save company on exit", e);
                        AlertBox.showError(this.primaryStage,
                                "Failed to save company: " + e.getMessage());
                }
                finally
                {
                        CurrentCompany.close();
                        Platform.exit();
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
					LOGGER.warn(
						"Error shutting down plugin: " + plugin.getName() +
							" - " + e.getMessage(),
						e);
				}
				
			}
			
		}
		
		doSaveCompany();
		DatabaseManager.shutdown();
		super.stop();
		
	}
	
}
