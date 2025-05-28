
package nonprofitbookkeeping.ui;

import java.io.IOException;
import java.util.function.Consumer;

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
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.service.*;
import nonprofitbookkeeping.ui.panels.*;
import nonprofitbookkeeping.ui.actions.GenerateBalanceSheetAction;
import nonprofitbookkeeping.ui.actions.GenerateCashFlowStatementAction; // Added
import nonprofitbookkeeping.ui.actions.GenerateBalanceSheetAction;
import nonprofitbookkeeping.ui.actions.GenerateBudgetVsActualsReportAction; // Added
import nonprofitbookkeeping.ui.actions.GenerateCashFlowStatementAction; 
import nonprofitbookkeeping.ui.actions.GenerateAccountActivityReportAction; // Added
import nonprofitbookkeeping.ui.actions.GenerateBalanceSheetAction;
import nonprofitbookkeeping.ui.actions.GenerateBudgetVsActualsReportAction; 
import nonprofitbookkeeping.ui.actions.GenerateCashFlowStatementAction; 
import nonprofitbookkeeping.ui.actions.GenerateIncomeStatementAction;
import nonprofitbookkeeping.ui.actions.GenerateTrialBalanceAction;
import nonprofitbookkeeping.ui.actions.*;
import nonprofitbookkeeping.ui.actions.scaledger.*;
import nonprofitbookkeeping.service.BudgetService; 
import nonprofitbookkeeping.ui.panels.BudgetPanel; 
import nonprofitbookkeeping.service.ReportConfigurationService; 
import nonprofitbookkeeping.ui.panels.ManageReportConfigurationsDialog; 
import java.io.File; 
import java.util.ArrayList; 
import nonprofitbookkeeping.model.Fund; 
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.model.CurrentCompany;

/**
 * JavaFX rewrite of the original Swing based {@code NonprofitBookkeeping} launcher.
 * <p>
 * Each Swing {@code JPanel} has a JavaFX counterpart that extends {@code javafx.scene.Node}.
 * Those panels are opened in independent secondary {@link Stage}s exactly like the old
 * implementation opened new {@code JFrame}s.
 */
public class NonprofitBookkeepingFX extends Application
{
	private Stage primaryStage;
	private BorderPane root;
	private DashboardPanelFX dashboard;
	@SuppressWarnings("unused") private CurrentCompany c;
	

	
	private enum AppState { NO_COMPANY, CREATING_COMPANY, COMPANY_OPEN }
	private AppState state = AppState.NO_COMPANY;      // current mode
	

	
	/* menu item refs we enable/disable */
	private MenuItem miOpen, miClose, miSave;
	private MenuItem miEditCompany, miEditCoa, miEditJournal;
	
	private Menu run, reports, panels;
	
	/** Container for singletons we re-use across panels. */
	private static final class ServiceContainer
	{
		private static final InventoryService iss = new InventoryService();
		private static final ReportService reportService = new ReportService();
		private static final BudgetService budgetService = new BudgetService(); 
		private static final ReportConfigurationService reportConfigurationService = new ReportConfigurationService(); // Added
		private static final DocumentStorageService dss = new DocumentStorageService();
		private static final FundAccountingService fas = new FundAccountingService();
		
	}
	
	
	/**
	 * Main 
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		launch(args);
	}
	
	/**
	 * start()
	 * 
	 * Override @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override public void start(Stage stage)
	{
		stage.getIcons().addAll(
			new Image(getClass().getResourceAsStream("../../cg-128px.png")));
		
		this.primaryStage = stage;
		
		// Get and hold the company singleton
		this.c = new CurrentCompany();
		
		// pass its observer property into the dashboard panel
		this.dashboard = new DashboardPanelFX();
		
		this.root = new BorderPane();
		this.root.setCenter(this.dashboard);
		this.root.setTop(buildMenuBar());
		
		Scene scene = new Scene(this.root, 1000, 700);
		this.primaryStage.setScene(scene);
		this.primaryStage.setTitle("Nonprofit Bookkeeping (JavaFX)");
		
		// set after creating the menu items
		setState(AppState.NO_COMPANY);
		this.primaryStage.show();
	}
	
	/* ───────────────────────────────────────────────────────────────────────── */
	/* Menu construction */
	/* ───────────────────────────────────────────────────────────────────────── */
	
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
		this.miEditCoa = add(edit, "Edit Chart of Accounts", e -> showCoaEditor());
		this.miEditJournal = add(edit, "Edit Journal", e -> showPanel(new JournalPanelFX(), "Journal"));
		
		add(edit, "Open Budget Editor", e -> {
            Company currentCompany = CurrentCompany.getCompany();
            if (currentCompany == null) {
                AlertBox.showError(this.primaryStage, "No company open.");
                return;
            }
            File companyDir = null;
            if (currentCompany.getCompanyFile() != null) {
                companyDir = currentCompany.getCompanyFile().getParentFile();
            }
            if (companyDir == null) {
                 AlertBox.showError(this.primaryStage, "Company directory not found. Cannot manage budgets.");
                 return;
            }

            // For V1, always open to create a new budget.
            // Company.getFunds() does not exist, so pass an empty list for now.
            List<Fund> funds = new ArrayList<>(); 
            BudgetPanel budgetPanel = new BudgetPanel(
                null, // Owner Frame for Swing JDialog
                currentCompany.getChartOfAccounts(),
                funds, 
                ServiceContainer.budgetService,
                companyDir,
                null // Passing null for budgetToEdit to create a new budget
            );
            budgetPanel.setVisible(true); 
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
		CurrentCompany.getCompany();
		
		/* SCA Ledger submenu */
		Menu sca = new Menu("SCA Ledger");
		add(sca, "Input File", e -> new InputFileActionFX(this.primaryStage).handle(e));
		add(sca, "Output File", e -> new OutputFileActionFX(this.primaryStage).handle(e));
		add(sca, "Load XLSM Table", e -> new LoadXlsmTableActionFX(this.primaryStage).handle(e));
		add(sca, "Apply Formulas", e -> new ApplyFormulasActionFX(this.primaryStage).handle(e));
		add(sca, "Save Modified Copy",
			e -> new SaveModifiedCopyActionFX(this.primaryStage).handle(e));
		add(sca, "Import from JSON", e -> new ImportFromJsonActionFX(this.primaryStage).handle(e));
		add(sca, "Undo Last Edit", e -> new UndoEditAction().actionPerformed(null));
		this.run.getItems().add(sca);
		
		bar.getMenus().add(this.run);
		
		/* REPORTS */
		this.reports = new Menu("Reports");
		add(this.reports, "Generate Reports", e -> {
			/* implement */});
		add(this.reports, "Show Reports", e -> showPanel(new ReportsPanelFX(), "Reports"));
		add(this.reports, "Show Accounts",
			e -> showPanel(new AccountsPanelFX(new AccountService()), "Chart of Accounts"));
		CurrentCompany.getCompany();
		add(this.reports, "Show Account Activity",
			e -> showPanel(new AccountsActivityPanelFX(CurrentCompany.getCompany().getLedger()),
				"Account Activity"));
		GenerateIncomeStatementAction incomeStatementAction = new GenerateIncomeStatementAction(ServiceContainer.reportService);
		add(this.reports, "Generate Income Statement", incomeStatementAction::actionPerformed);
		
		GenerateBalanceSheetAction balanceSheetAction = new GenerateBalanceSheetAction(ServiceContainer.reportService);
		add(this.reports, "Generate Balance Sheet", balanceSheetAction::actionPerformed);

		GenerateTrialBalanceAction trialBalanceAction = new GenerateTrialBalanceAction(ServiceContainer.reportService);
		add(this.reports, "Generate Trial Balance", trialBalanceAction::actionPerformed);
		
		GenerateCashFlowStatementAction cashFlowStatementAction = new GenerateCashFlowStatementAction(ServiceContainer.reportService);
		add(this.reports, "Generate Cash Flow Statement", cashFlowStatementAction::actionPerformed);
		
		GenerateBudgetVsActualsReportAction bvaAction = new GenerateBudgetVsActualsReportAction(ServiceContainer.reportService, ServiceContainer.budgetService);
        add(this.reports, "Generate Budget vs. Actuals Report", bvaAction::actionPerformed);
		
        add(this.reports, "Manage Saved Reports", e -> {
            Company currentCompany = CurrentCompany.getCompany();
            if (currentCompany == null) { // Check currentCompany first
                AlertBox.showError(this.primaryStage, "No company open.");
                return;
            }
            File companyFile = currentCompany.getCompanyFile();
            if (companyFile == null) {
                 AlertBox.showError(this.primaryStage, "Company file path not set. Cannot manage saved reports.");
                 return;
            }
            File companyDir = companyFile.getParentFile();
            if (companyDir == null) {
                 AlertBox.showError(this.primaryStage, "Company directory not found. Cannot manage saved reports.");
                 return;
            }

            ManageReportConfigurationsDialog manageDialog = new ManageReportConfigurationsDialog(
                null, // Swing owner Frame
                ServiceContainer.reportConfigurationService,
                companyDir,
                new ArrayList<Fund>(), // availableFunds placeholder
                ServiceContainer.reportService 
            );
            manageDialog.setVisible(true);
        });

        GenerateAccountActivityReportAction aaAction = new GenerateAccountActivityReportAction(ServiceContainer.reportService);
        add(this.reports, "Generate Account Activity Detail", aaAction::actionPerformed);

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
		
		return bar;
	}
	
	/**
	 * Add a menu item
	 * 
	 * @param menu The item
	 * @param label its label
	 * @param handler Its handler
	 */
	/** helper now returns the created MenuItem */
	private static MenuItem add(Menu menu, 
	                            String label, 
	                            EventHandler<ActionEvent> handler)
	{
		MenuItem item = new MenuItem(label);
		item.setOnAction(handler);
		menu.getItems().add(item);
		return item;
	}
	
	/* ───────────────────────────────────────────────────────────────────────── */
	/* Helper to open subpanels in their own Stage */
	/* ───────────────────────────────────────────────────────────────────────── */
	
	/**
	 * Show a panel
	 * 
	 * @param panel the panel to show
	 * @param title title on the panel
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
	
	/**
	 * 
	 */
	private void showCoaEditor()
	{
		Node previousView = this.root.getCenter();
		
		Company activeCompany = CurrentCompany.getCompany();
		
		CoaEditorPanelFX editor = new CoaEditorPanelFX(
			CurrentCompany.getCompany().getChartOfAccounts(),
			
			/* onSave */
			new Consumer<ChartOfAccounts>()
			{
				@Override public void accept(ChartOfAccounts chart)
				{
					activeCompany.setChartOfAccounts(chart);
					
					try
					{
						CurrentCompany.persist();
					}
					catch (IOException | ActionCancelledException | NoFileCreatedException e)
					{
						e.printStackTrace();
					}
					
				}
				
			},
			
			/* onClose */
			new Runnable()
			{
				@Override public void run()
				{
					NonprofitBookkeepingFX.this.root.setCenter(previousView);
				}
				
			});
		
		this.root.setCenter(editor);
		
	}
	
	/* ====== state machine wiring ===================================== */
	private void setState(AppState s) {
	    this.state = s;            // store new mode

	    switch (s) {
	        case NO_COMPANY -> {
	            this.miOpen.setDisable(false);
	            this.miClose.setDisable(true);
	            this.miSave.setDisable(true);
	            this.miEditCompany.setDisable(false);
	            this.miEditCoa.setDisable(true);
	            this.miEditJournal.setDisable(true);
	            
	            this.run.setDisable(true);
	            this.panels.setDisable(true);
	            this.reports.setDisable(true);
	        }
	        case CREATING_COMPANY -> {
	            this.miOpen.setDisable(true);
	            this.miClose.setDisable(true);
	            this.miSave.setDisable(true);
	            this.miEditCompany.setDisable(true);
	            this.miEditCoa.setDisable(true);
	            this.miEditJournal.setDisable(true);
	            
	            this.run.setDisable(true);
	            this.panels.setDisable(true);
	            this.reports.setDisable(true);
	        }
	        case COMPANY_OPEN -> {
	            this.miOpen.setDisable(true);
	            this.miClose.setDisable(false);
	            this.miSave.setDisable(false);
	            this.miEditCompany.setDisable(false);
	            this.miEditCoa.setDisable(false);
	            this.miEditJournal.setDisable(false);
	            
	            this.run.setDisable(false);
	            this.panels.setDisable(false);
	            this.reports.setDisable(false);
	        }
			default -> throw new IllegalArgumentException("Unexpected value: " + s);
	    }
	}

	
	/* ====== workflow methods that flip the state ===================== */
	
	/**
	 * doOpenCompany
	 */
	@SuppressWarnings("unused") private void doOpenCompany()
	{
		try
		{
			OpenCompanyFileActionFX openCompanyFileActionFX = new OpenCompanyFileActionFX(this.primaryStage);
			setState(AppState.COMPANY_OPEN);
		}
		catch (Exception e)
		{
			AlertBox.showError(null, e.getMessage());
		}
	}
	
	/**
	 * doCloseCompany
	 */
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
		
		this.root.setCenter(this.dashboard);
	}
	
	/**
	 * doSaveCompany
	 */
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
	
	/**
	 * startCreateWizard
	 */
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
			// roll back state if create failed.
			setState(saved);
			e.printStackTrace();
			AlertBox.showError(null, e.getMessage());
		}
		
		
	}

	/**
	 * @return
	 */
	private AppState getState()
	{
		return this.state;
	}
		
	
}
