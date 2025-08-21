
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
import nonprofitbookkeeping.persistence.DatabaseManager;
import nonprofitbookkeeping.ui.actions.*;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.panels.*;
import nonprofitbookkeeping.ui.javafx.BudgetPanelFX;

/**
 * Main JavaFX application class for Nonprofit Bookkeeping.
 */
public class NonprofitBookkeepingFX extends Application
{
	private Stage primaryStage;
	private BorderPane root;
	private DashboardPanelFX dashboard;
	private CurrentCompany c;
	
	private enum AppState
	{
		NO_COMPANY, CREATING_COMPANY, COMPANY_OPEN
	}
	
	private AppState state = AppState.NO_COMPANY;
	
	private MenuItem miOpen;
	private MenuItem miClose;
	private MenuItem miSave;
	private MenuItem miEditCompany;
	private MenuItem miEditCoa;
	private MenuItem miEditJournal;
	private MenuItem miImportCoaXlsx;
	private MenuItem miExportCoaXlsx;
	
	private Menu run;
	private Menu reports;
	private Menu panels;
	
	private static final Logger LOGGER =
		Logger.getLogger(NonprofitBookkeepingFX.class.getName());
	
	private List<Plugin> loadedPlugins = new ArrayList<>();
	private ApplicationContext applicationContext;
	
	private static final class ServiceContainer
	{
		static InventoryService iss = null;
		static ReportService reportService = null;
		static BudgetService budgetService = null;
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
				iss = new InventoryService();
				reportService = new ReportService();
				budgetService = new BudgetService();
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
	
	public static void main(String[] args)
	{
		DatabaseManager.startServer();
		launch(args);
		
	}
	
	@Override
	public void start(Stage stage)
	{
		Runtime.getRuntime().addShutdownHook(new Thread(this::doSaveCompany));
		
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
		this.dashboard = new DashboardPanelFX();
		MainApplicationView mainView = new MainApplicationView();
		this.root = mainView;
		
		this.applicationContext = new ApplicationContextImpl(
			this.primaryStage,
			ServiceContainer.reportService,
			ServiceContainer.budgetService,
			ServiceContainer.reportConfigurationService,
			ServiceContainer.iss,
			ServiceContainer.dss,
			ServiceContainer.fas
		);
		
		LOGGER.info("Starting plugin discovery...");
		ServiceLoader<Plugin> pluginLoader = ServiceLoader.load(Plugin.class);
		
		for (Plugin plugin : pluginLoader)
		{
			
			try
			{
				LOGGER.info("Initializing plugin: " + plugin.getName() + " - " +
					plugin.getDescription());
				plugin.initialize(this.applicationContext);
				this.loadedPlugins.add(plugin);
				LOGGER.info(
					"Plugin initialized successfully: " + plugin.getName());
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "Failed to initialize plugin: " +
					plugin.getClass().getName() + " - " + e.getMessage(), e);
				AlertBox.showError(this.primaryStage, "Plugin Load Error");
			}
			
		}
		
		LOGGER.info("Plugin discovery complete. Loaded " +
			this.loadedPlugins.size() + " plugins.");
		
		MenuBar menuBar = buildMenuBar();
		mainView.setMenuBar(menuBar);
		
		Scene scene = new Scene(mainView, 1000, 700);
		ThemeManager.applyTheme(scene);
		this.primaryStage.setScene(scene);
		this.primaryStage.setTitle("Nonprofit Bookkeeping");
		
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
		this.miImportCoaXlsx = add(file, "Import COA (XLSX)",
			e -> new ImportCoaXlsxActionFX(this.primaryStage).handle(e));
		this.miExportCoaXlsx = add(file, "Export COA (XLSX)",
			e -> new ExportCoaXlsxActionFX(this.primaryStage).handle(e));
		
		add(file, "Import File",
			e -> new ImportFileActionFX(this.primaryStage).handle(e));
		add(file, "Export File",
			e -> new ExportFileActionFX(this.primaryStage).handle(e));
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
		
		// Plugin menu items
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
					plugin.addMenuItems(bar);
				}
				catch (Exception ex)
				{
					LOGGER.log(Level.WARNING, "Plugin " + plugin.getName() +
						" failed to add its menu items: " + ex.getMessage(),
						ex);
				}
				
			}
			
		}
		
		return bar;
		
	}
	
	private static MenuItem add(Menu menu, String label,
		EventHandler<ActionEvent> handler)
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
		Scene scene = new Scene(wrapper, 900, 600);
		ThemeManager.applyTheme(scene);
		sub.setScene(scene);
		sub.initOwner(this.primaryStage);
		sub.show();
		
	}
	
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
	
	/** Updated to use OpenCompanyFileActionFX.run() and then check CurrentCompany.isOpen(). */
	private void doOpenCompany()
	{
		
		try
		{
			OpenCompanyFileActionFX action =
				new OpenCompanyFileActionFX(this.primaryStage);
			action.run();
			
			if (CurrentCompany.isOpen())
			{
				setState(AppState.COMPANY_OPEN);
			}
			
		}
		catch (Exception e)
		{
			AlertBox.showError(this.primaryStage,
				"Failed to open company: " + e.getMessage());
		}
		
	}
	
	private void doCloseCompany()
	{
		
		try
		{
			CloseCompanyFileAction closeCompanyFileAction =
				new CloseCompanyFileAction(this.primaryStage);
			
			if (closeCompanyFileAction.isClosed())
			{
				setState(AppState.NO_COMPANY);
			}
			else
			{
				return; // user cancelled
			}
			
		}
		catch (Exception e)
		{
			AlertBox.showError(this.primaryStage,
				"Failed to close company: " + e.getMessage());
		}
		
		if (this.root instanceof MainApplicationView)
		{
			((MainApplicationView) this.root)
				.showPanel(MainApplicationView.PanelType.DASHBOARD);
		}
		else
		{
			this.root.setCenter(this.dashboard);
		}
		
	}
	
	private void doSaveCompany()
	{
		
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
	
	private void startCreateWizard()
	{
		AppState saved = getState();
		setState(AppState.CREATING_COMPANY);
		
		try
		{
			CreateOrEditCompanyActionFX createOrEditCompanyActionFX =
				new CreateOrEditCompanyActionFX(this.primaryStage);
			
			if (CurrentCompany.getCompany() != null)
			{
				setState(AppState.COMPANY_OPEN);
			}
			else
			{
				setState(saved);
			}
			
		}
		catch (Exception e)
		{
			setState(saved);
			e.printStackTrace();
			AlertBox.showError(this.primaryStage,
				"Error during company setup: " + e.getMessage());
		}
		
	}
	
	private AppState getState()
	{
		return this.state;
		
	}
	
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
					LOGGER.log(Level.WARNING,
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
