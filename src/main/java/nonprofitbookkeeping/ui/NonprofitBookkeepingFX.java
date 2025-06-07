
package nonprofitbookkeeping.ui;

import java.io.File;
import java.io.IOException;
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
import nonprofitbookkeeping.ui.MainApplicationView;
import nonprofitbookkeeping.core.ApplicationContext;
import nonprofitbookkeeping.core.ApplicationContextImpl;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.plugin.Plugin;
import nonprofitbookkeeping.service.*;
import nonprofitbookkeeping.ui.actions.*;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.panels.*;


public class NonprofitBookkeepingFX extends Application
{
	private Stage primaryStage;
	private BorderPane root;
	private DashboardPanelFX dashboard;
	@SuppressWarnings("unused") private CurrentCompany c;
	
	private enum AppState
	{
		NO_COMPANY, CREATING_COMPANY, COMPANY_OPEN
	}
	
	private AppState state = AppState.NO_COMPANY;
	
	private MenuItem miOpen, miClose, miSave;
	private MenuItem miEditCompany, miEditCoa, miEditJournal;
	private Menu run, reports, panels;
	
	private static final Logger LOGGER = Logger.getLogger(NonprofitBookkeepingFX.class.getName());
	private List<Plugin> loadedPlugins = new ArrayList<>();
	private ApplicationContext applicationContext;
	
	private static final class ServiceContainer
	{
		private static final InventoryService iss = new InventoryService();
		private static final ReportService reportService = new ReportService();
		private static final BudgetService budgetService = new BudgetService();
		private static final ReportConfigurationService reportConfigurationService =
			new ReportConfigurationService();
		private static final DocumentStorageService dss = new DocumentStorageService();
		private static final FundAccountingService fas = new FundAccountingService();
		
		// FileImportService and FileExportService are typically used via Actions, not
		// directly managed as singletons here.
	}
	
	public static void main(String[] args)
	{
		launch(args);
	}
	
	@Override public void start(Stage stage)
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// Save data
			doSaveCompany();
		}));
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
		
		setState(AppState.NO_COMPANY);
		this.primaryStage.show();
	}
	
	private MenuBar buildMenuBar()
	{
		MenuBar bar = new MenuBar();
		
		/* FILE */
		Menu file = new Menu("File");
		this.miOpen = add(file, "Open Company File", e -> doOpenCompany());
		this.miClose = add(file, "Close Company File", e -> doCloseCompany());
		this.miSave = add(file, "Save Company File", e -> doSaveCompany());

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
	
	private static MenuItem add(Menu menu, String label, EventHandler<ActionEvent> handler)
	{
		MenuItem item = new MenuItem(label);
		item.setOnAction(handler);
		menu.getItems().add(item);
		return item;
	}
	
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
	private void showCoaEditor() // Replaced by menu action calling mainView.showPanel()
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
					ex.printStackTrace();
				}
				
			},
			() -> this.root.setCenter(previousView));
		this.root.setCenter(editor);
	}
	*/
	
	private void setState(AppState s)
	{
		this.state = s;
		boolean companyOpen = (s == AppState.COMPANY_OPEN);
		boolean noCompany = (s == AppState.NO_COMPANY);
		boolean creatingCompany = (s == AppState.CREATING_COMPANY);
		
		this.miOpen.setDisable(companyOpen || creatingCompany);
		this.miClose.setDisable(noCompany || creatingCompany);
		this.miSave.setDisable(noCompany || creatingCompany);
		this.miEditCompany.setDisable(creatingCompany); // Can edit if open, can create if no
														// company
		this.miEditCoa.setDisable(noCompany || creatingCompany);
		this.miEditJournal.setDisable(noCompany || creatingCompany);
		
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
		// app manages plugin menus. For now, SCALedgerPlugin's menu is always enabled once added. This is a V2
		// improvement area.
	}
	
	@SuppressWarnings("unused") private void doOpenCompany()
	{
		
		try
		{
			OpenCompanyFileActionFX openCompanyFileActionFX =
				new OpenCompanyFileActionFX(this.primaryStage);
			setState(AppState.COMPANY_OPEN);
		}
		catch (Exception e)
		{
			AlertBox.showError(null, e.getMessage());
		}
		
	}
	
	@SuppressWarnings("unused") private void doCloseCompany()
	{
		
		try
		{
			CloseCompanyFileAction closeCompanyFileAction =
				new CloseCompanyFileAction(this.primaryStage);
			setState(AppState.NO_COMPANY);
		}
		catch (Exception e)
		{
			AlertBox.showError(null, e.getMessage());
		}
		
		// this.root.setCenter(this.dashboard); // Old way
		if (this.root instanceof MainApplicationView) {
			((MainApplicationView)this.root).showPanel(MainApplicationView.PanelType.DASHBOARD);
		} else {
			// Fallback or error if root is not what we expect, though it should be.
			this.root.setCenter(this.dashboard);
		}
	}
	
	@SuppressWarnings("unused") private void doSaveCompany()
	{
		
		try
		{
			SaveCompanyFileAction saveCompanyFileAction =
				new SaveCompanyFileAction(this.primaryStage);
			AlertBox.showInfo(null, "Company saved.");
		}
		catch (Exception ex)
		{
			AlertBox.showError(null, ex.getMessage());
		}
		
	}
	
	@SuppressWarnings("unused") private void startCreateWizard()
	{
		AppState saved = getState();
		setState(AppState.CREATING_COMPANY);
		
		try
		{
			CreateOrEditCompanyActionFX createOrEditCompanyActionFX =
				new CreateOrEditCompanyActionFX(this.primaryStage);
			setState(AppState.COMPANY_OPEN);
		}
		catch (Exception e)
		{
			setState(saved);
			e.printStackTrace();
			AlertBox.showError(null, e.getMessage());
		}
		
	}
	
	private AppState getState()
	{
		return this.state;
	}
	
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
