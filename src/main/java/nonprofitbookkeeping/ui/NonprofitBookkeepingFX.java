
package nonprofitbookkeeping.ui;

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
import nonprofitbookkeeping.core.JacksonDataStore;
import nonprofitbookkeeping.model.CompanyDataFile;
import nonprofitbookkeeping.model.CurrentInputFile;
import nonprofitbookkeeping.service.*;
import nonprofitbookkeeping.ui.panels.*;
import nonprofitbookkeeping.ui.actions.*;
import nonprofitbookkeeping.ui.actions.scaledger.*;

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
	private final DashboardPanelFX dashboard = new DashboardPanelFX();
	// The company file and its object wrapper
	private static CompanyDataFile companyDataFile;

	/** Container for singletons we re-use across panels. */
	private static final class ServiceContainer
	{
		private static final InventoryService iss = new InventoryService();
		private static final ReportService reportService = new ReportService();
		private static final DocumentStorageService dss = new DocumentStorageService();
		private static final FundAccountingService fas = new FundAccountingService();
		
	}
	

	/**
	 * main
	 * @param args
	 */
	public static void main(String[] args)
	{
		launch(args);
	}
	
	/**
	 * 
	 * Override @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override public void start(Stage stage)
	{
		stage.getIcons().addAll(
			new Image(getClass().getResourceAsStream("../../cg-128px.png")));
		
		this.primaryStage = stage;
		
		this.root = new BorderPane();
		this.root.setCenter(this.dashboard); 
		this.root.setTop(buildMenuBar());
		
		Scene scene = new Scene(this.root, 1000, 700);
		this.primaryStage.setScene(scene);
		this.primaryStage.setTitle("Nonprofit Bookkeeping (JavaFX)");
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
		add(file, "Open Company File…",
			e -> new OpenCompanyFileActionFX(this.primaryStage, JacksonDataStore.dataStore).handle(e));
		add(file, "Close Company File…",
			e -> new CloseCompanyFileAction());
		add(file, "Save Company File…",
			e -> new SaveCompanyFileAction(CurrentInputFile.currentInputFile, JacksonDataStore.dataStore, CompanyDataFile.getCdf()));
		add(file, "Create or Edit Company",
			e -> new CreateOrEditCompanyActionFX(this.primaryStage).handle(e));
		add(file, "Import File", e -> new ImportFileActionFX(this.primaryStage).handle(e));
		add(file, "Export File", e -> new ExportFileActionFX(this.primaryStage).handle(e));
		bar.getMenus().add(file);
		
		/* RUN */
		Menu run = new Menu("Run");
		add(run, "Show Settings", e -> showPanel(new SettingsPanelFX(), "Settings"));
		add(run, "Documents & Attachments",
			e -> showPanel(new DocumentsPanelFX(ServiceContainer.dss), "Documents"));
		add(run, "Inventory & Depreciation",
			e -> showPanel(new InventoryPanelFX(ServiceContainer.iss), "Inventory"));
		add(run, "Funds & Fund Accounting",
			e -> showPanel(new FundsPanelFX(ServiceContainer.fas), "Funds"));
		add(run, "Reconcile",
			e -> showPanel(new ReconcilePanelFX(new ReconciliationService()), "Reconciliation"));
		add(run, "Edit Chart of Accounts",
			e -> showPanel(new CoaEditorPanelFX(CompanyDataFile.getCdf().getCoA()), "Chart of Accounts"));
		
		/* SCA Ledger submenu */
		Menu sca = new Menu("SCA Ledger");
		add(sca, "Input File…", e -> new InputFileActionFX(this.primaryStage).handle(e));
		add(sca, "Output File…", e -> new OutputFileActionFX(this.primaryStage).handle(e));
		add(sca, "Load XLSM Table", e -> new LoadXlsmTableActionFX(this.primaryStage).handle(e));
		add(sca, "Apply Formulas", e -> new ApplyFormulasActionFX(this.primaryStage).handle(e));
		add(sca, "Save Modified Copy",
			e -> new SaveModifiedCopyActionFX(this.primaryStage).handle(e));
		add(sca, "Import from JSON", e -> new ImportFromJsonActionFX(this.primaryStage).handle(e));
		add(sca, "Undo Last Edit", e -> new UndoEditAction().actionPerformed(null));
		run.getItems().add(sca);
		bar.getMenus().add(run);
		
		/* REPORTS */
		Menu reports = new Menu("Reports");
		add(reports, "Generate Reports", e -> {
			/* implement */});
		add(reports, "Show Reports", e -> showPanel(new ReportsPanelFX(), "Reports"));
		add(reports, "Show Journal", e -> showPanel(new JournalPanelFX(), "Journal"));
		add(reports, "Show Accounts",
			e -> showPanel(new AccountsPanelFX(new AccountService()), "Chart of Accounts"));
		add(reports, "Show Account Activity",
			e -> showPanel(new AccountsActivityPanelFX(CompanyDataFile.getCdf().getLedger()),
				"Account Activity"));
		add(reports, "Generate Income Statement",
			e -> new GenerateIncomeStatementAction(ServiceContainer.reportService)
				.actionPerformed(null));
		add(reports, "Generate Balance Sheet",
			e -> new GenerateBalanceSheetAction(ServiceContainer.reportService)
				.actionPerformed(null));
		bar.getMenus().add(reports);
		
		/* PANELS */
		Menu panels = new Menu("Panels");
		add(panels, "Donors", e -> showPanel(new DonorsPanelFX(), "Donors"));
		add(panels, "Donations", e -> showPanel(new DonationsPanelFX(), "Donations"));
		add(panels, "Grants", e -> showPanel(new GrantsPanelFX(), "Grants"));
		add(panels, "Sales & COG", e -> showPanel(new SalesAndCOGPanelFX(null), "Sales & COG"));
		bar.getMenus().add(panels);
		
		/* HELP */
		Menu help = new Menu("Help");
		add(help, "Help", e -> showPanel(new HelpPanelFX(), "Help"));
		bar.getMenus().add(help);
		
		return bar;
	}
	
	/**
	 * 
	 * @param menu
	 * @param label
	 * @param handler
	 */
	private static void add(Menu menu,
							String label,
							EventHandler<ActionEvent> handler)
	{
		MenuItem item = new MenuItem(label);
		item.setOnAction(handler);
		menu.getItems().add(item);
	}
	
	/* ───────────────────────────────────────────────────────────────────────── */
	/* Helper to open subpanels in their own Stage */
	/* ───────────────────────────────────────────────────────────────────────── */
	
	/**
	 * 
	 * @param panel
	 * @param title
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
	 * @return the primaryStage
	 */
	public Stage getPrimaryStage()
	{
		return this.primaryStage;
	}

	/**
	 * @param primaryStage the primaryStage to set
	 */
	public void setPrimaryStage(Stage primaryStage)
	{
		this.primaryStage = primaryStage;
	}

	/**
	 * @return the root
	 */
	public BorderPane getRoot()
	{
		return this.root;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(BorderPane root)
	{
		this.root = root;
	}

	/**
	 * @return the dashboard
	 */
	public DashboardPanelFX getDashboard()
	{
		return this.dashboard;
	}
	
}
