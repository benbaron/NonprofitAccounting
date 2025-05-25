
package nonprofitbookkeeping.ui;

import java.io.IOException;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
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
	private DashboardPanelFX dashboard;
	@SuppressWarnings("unused") private Company c;
	private ReadOnlyObjectProperty<Company> prop;
	
	/** Container for singletons we re-use across panels. */
	private static final class ServiceContainer
	{
		private static final InventoryService iss = new InventoryService();
		private static final ReportService reportService = new ReportService();
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
		this.c = Company.getCompany();
		// pass its observer property into the dashboard panel
		this.prop = Company.getCompany().getCompanyObserver();
		this.dashboard = new DashboardPanelFX(this.prop);
		
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
		add(file, "Open Company File",
			e -> new OpenCompanyFileActionFX(this.primaryStage));
		add(file, "Close Company File",
			e -> new CloseCompanyFileAction(this.primaryStage));
		add(file, "Save Company File",
			e -> new SaveCompanyFileAction(this.primaryStage));
		add(file, "Create or Edit Company",
			e -> new CreateOrEditCompanyActionFX(this.primaryStage));
		
		add(file, "Import File", e -> new ImportFileActionFX(this.primaryStage).handle(e));
		add(file, "Export File", e -> new ExportFileActionFX(this.primaryStage).handle(e));
		bar.getMenus().add(file);
		
		/* RUN */
		Menu run = new Menu("Run");
		add(run, "Show Settings",
			e -> showPanel(new SettingsPanelFX(this.primaryStage), "Settings"));
		add(run, "Documents & Attachments",
			e -> showPanel(new DocumentsPanelFX(ServiceContainer.dss), "Documents"));
		add(run, "Inventory & Depreciation",
			e -> showPanel(new InventoryPanelFX(ServiceContainer.iss), "Inventory"));
		add(run, "Funds & Fund Accounting",
			e -> showPanel(new FundsPanelFX(ServiceContainer.fas), "Funds"));
		add(run, "Reconcile",
			e -> showPanel(new ReconcilePanelFX(new ReconciliationService()), "Reconciliation"));
		Company.getCompany();
		add(run, "Edit Chart of Accounts",
			e -> showCoaEditor());
		
		
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
		Company.getCompany();
		add(reports, "Show Account Activity",
			e -> showPanel(
				new AccountsActivityPanelFX(Company.getCompany().getLedger()),
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
		add(panels, "Donors", e -> showPanel(new DonorsPanelFX(this.primaryStage), "Donors"));
		add(panels, "Donations",
			e -> showPanel(new DonationsPanelFX(this.primaryStage), "Donations"));
		add(panels, "Grants", e -> showPanel(new GrantsPanelFX(this.primaryStage), "Grants"));
		add(panels, "Sales & COG",
			e -> showPanel(new SalesAndCOGPanelFX(this.primaryStage), "Sales & COG"));
		bar.getMenus().add(panels);
		
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
		
		Company activeCompany = Company.getCompany();
		CoaEditorPanelFX editor = new CoaEditorPanelFX(
			activeCompany.getChartOfAccounts(),
			
			/* onSave */
			new Consumer<ChartOfAccounts>()
			{
				@Override public void accept(ChartOfAccounts chart)
				{
					activeCompany.setChartOfAccounts(chart);
					try
					{
						activeCompany.persist();
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
	
}
