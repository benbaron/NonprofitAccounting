
package nonprofitbookkeeping.ui;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import nonprofitbookkeeping.model.CompanyDataFile;
import nonprofitbookkeeping.service.AccountService;
import nonprofitbookkeeping.service.DocumentStorageService;
import nonprofitbookkeeping.service.FundAccountingService;
import nonprofitbookkeeping.service.InventoryService;
import nonprofitbookkeeping.service.ReconciliationService;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.TrialBalanceService;
import nonprofitbookkeeping.ui.actions.CreateOrEditCompanyAction;
import nonprofitbookkeeping.ui.actions.ExportFileAction;
import nonprofitbookkeeping.ui.actions.GenerateBalanceSheetAction;
import nonprofitbookkeeping.ui.actions.GenerateIncomeStatementAction;
import nonprofitbookkeeping.ui.actions.ImportFileAction;
import nonprofitbookkeeping.ui.actions.InputFileAction;
import nonprofitbookkeeping.ui.actions.OpenCompanyFileAction;
import nonprofitbookkeeping.ui.actions.OutputFileAction;
import nonprofitbookkeeping.ui.actions.CloseCompanyFileAction;
import nonprofitbookkeeping.ui.actions.scaledger.ApplyFormulasAction;
import nonprofitbookkeeping.ui.actions.scaledger.ImportFromJsonAction;
import nonprofitbookkeeping.ui.actions.scaledger.LoadXlsmTableAction;
import nonprofitbookkeeping.ui.actions.scaledger.SaveModifiedCopyAction;
import nonprofitbookkeeping.ui.actions.scaledger.UndoEditAction;
import nonprofitbookkeeping.ui.panels.AccountsActivityPanel;
import nonprofitbookkeeping.ui.panels.AccountsPanel;
import nonprofitbookkeeping.ui.panels.DashboardPanel;
import nonprofitbookkeeping.ui.panels.DocumentsPanel;
import nonprofitbookkeeping.ui.panels.DonationsPanel;
import nonprofitbookkeeping.ui.panels.DonorsPanel;
import nonprofitbookkeeping.ui.panels.FundsPanel;
import nonprofitbookkeeping.ui.panels.GrantsPanel;
import nonprofitbookkeeping.ui.panels.HelpPanel;
import nonprofitbookkeeping.ui.panels.InventoryPanel;
import nonprofitbookkeeping.ui.panels.JournalPanel;
import nonprofitbookkeeping.ui.panels.ReconcilePanel;
import nonprofitbookkeeping.ui.panels.ReportsPanel;
import nonprofitbookkeeping.ui.panels.SalesAndCOGPanel;
import nonprofitbookkeeping.ui.panels.SettingsPanel;

public class NonprofitBookkeeping
{
	public static File currentInputFile;
	public static CompanyDataFile companyDataFile;
	
	public static Map<String, Object> beans = new HashMap<>();
	
	static class PanelContainer
	{
		public static JFrame frame;
		public static JPanel topPanel;
		public static JComboBox<String> viewModeSelector;
		public static JComboBox<String> accountSelector;
		public static JTextArea viewerArea;
		public static DashboardPanel dashboardPanel;
		public static JournalPanel journalPanel;
		public static AccountsPanel accountsPanel;
		public static ReportsPanel reportsPanel;
		public static AccountsActivityPanel accountsActivityPanel;
		public static SettingsPanel settingsPanel;
		public static DocumentsPanel documentsPanel;
		public static InventoryPanel inventoryPanel;
		public static FundsPanel fundsPanel;
		public static ReconcilePanel reconciliationPanel;
		public static DonorsPanel donorsPanel;
		public static DonationsPanel donationsPanel;
		public static GrantsPanel grantsPanel;
		public static SalesAndCOGPanel salesAndCOGPanel;
		public static HelpPanel helpPanel;
		
	}
	
	// Main frame getter
	public static JFrame getFrame()
	{
		return PanelContainer.frame;
	}
	
	static class ServiceContainer
	{
		public static InventoryService iss = new InventoryService();
		public static ReportService reportService = new ReportService();
		public static TrialBalanceService trialBalanceService = new TrialBalanceService();
		public static DocumentStorageService dss = new DocumentStorageService();
		public static FundAccountingService fas = new FundAccountingService();
		
	}
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(NonprofitBookkeeping::createAndShowGUI);
	}
	
	private static void createAndShowGUI()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (@SuppressWarnings("unused") Exception ignored)
		{
		}
		
		PanelContainer.frame = new JFrame("Nonprofit Bookkeeping");		
		PanelContainer.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		PanelContainer.frame.setSize(1000, 700);
		
		JMenuBar menuBar = new JMenuBar();
		
		////////////////////
		// File Menu
		JMenu fileMenu = new JMenu("File");
		
		List<MenuItemData> fileMenuItems = Arrays.asList(
			new MenuItemData("Load Company File...", e -> {				
					new OpenCompanyFileAction(currentInputFile).actionPerformed(e);

				
			}),
			new MenuItemData("Store Company File...", e -> new CloseCompanyFileAction().actionPerformed(e)),
			new MenuItemData("Create or Edit Company", e -> new CreateOrEditCompanyAction(PanelContainer.frame).actionPerformed(e)),
			new MenuItemData("Import File", e -> new ImportFileAction().actionPerformed(e)),  // Add Import File
			new MenuItemData("Export File", e -> new ExportFileAction().actionPerformed(e))   // Add Export File
		);

		fileMenuItems.forEach(item -> {
			JMenuItem menuItem = new JMenuItem(item.getLabel());
			menuItem.addActionListener(item.getActionListener());
			fileMenu.add(menuItem);
		});
		
		menuBar.add(fileMenu);

		////////////////////
		// Run Menu
		JMenu runMenu = new JMenu("Run");

		List<MenuItemData> runMenuItems = Arrays.asList(
			new MenuItemData("Show Settings", e -> showPanel(new SettingsPanel(), "Settings")),
			new MenuItemData("Documents & Attachments", e -> showPanel(new DocumentsPanel(ServiceContainer.dss), "Documents")),
			new MenuItemData("Inventory & Depreciation", e -> showPanel(new InventoryPanel(ServiceContainer.iss), "Inventory")),
			new MenuItemData("Funds & Fund Accounting", e -> showPanel(new FundsPanel(ServiceContainer.fas), "Funds")),
			new MenuItemData("Reconcile", e -> showPanel(new ReconcilePanel(new ReconciliationService()), "Reconciliation"))
		);

		runMenuItems.forEach(item -> {
			JMenuItem menuItem = new JMenuItem(item.getLabel());
			menuItem.addActionListener(item.getActionListener());
			runMenu.add(menuItem);
		});
		
		JMenu scaLedger = new JMenu("SCA Ledger");
		runMenu.add(scaLedger);
		
		List<MenuItemData> scaLedgerItems = Arrays.asList(
			new MenuItemData("Input File...", e -> new InputFileAction().actionPerformed(e)),
			new MenuItemData("Output File...", e -> new OutputFileAction().actionPerformed(e)),
			new MenuItemData("Load XLSM Table", e -> new LoadXlsmTableAction().actionPerformed(e)),
			new MenuItemData("Apply Formulas", e -> new ApplyFormulasAction().actionPerformed(e)),
			new MenuItemData("Save Modified Copy", e -> new SaveModifiedCopyAction().actionPerformed(e)),
			new MenuItemData("Import from JSON", e -> new ImportFromJsonAction().actionPerformed(e)),
			new MenuItemData("Undo Last Edit", e -> new UndoEditAction().actionPerformed(e)));

		scaLedgerItems.forEach(item -> {
			JMenuItem menuItem = new JMenuItem(item.getLabel());
			menuItem.addActionListener(item.getActionListener());
			scaLedger.add(menuItem);
		});		

		menuBar.add(runMenu);

		////////////////////
		// Reports Menu
		JMenu reportsMenu = new JMenu("Reports");

		List<MenuItemData> reportMenuItems = Arrays.asList(
			new MenuItemData("Generate Reports", e -> {
				// Implement your generate reports logic here.
			}),
			new MenuItemData("Show Reports", e -> showPanel(new ReportsPanel(), "Reports")),
			new MenuItemData("Show Dashboard", e -> showPanel(new DashboardPanel(companyDataFile), "Dashboard")),
			new MenuItemData("Show Journal", e -> showPanel(new JournalPanel(), "Journal")),
			new MenuItemData("Show Accounts", e -> showPanel(new AccountsPanel(new AccountService()), "Chart of Accounts")),
			new MenuItemData("Show Account Activity", e -> showPanel(new AccountsActivityPanel(companyDataFile.getCompanyDataFile().getLedger()), "Account Activity")),
			new MenuItemData("Generate Income Statement", e -> new GenerateIncomeStatementAction(ServiceContainer.reportService).actionPerformed(e)),
			new MenuItemData("Generate Balance Sheet", e -> new GenerateBalanceSheetAction(ServiceContainer.reportService).actionPerformed(e))
		);

		reportMenuItems.forEach(item -> {
			JMenuItem menuItem = new JMenuItem(item.getLabel());
			menuItem.addActionListener(item.getActionListener());
			reportsMenu.add(menuItem);
		});
		
		menuBar.add(reportsMenu);

		////////////////////
		// Panels Menu
		JMenu panelsMenu = new JMenu("Panels");

		List<MenuItemData> panelMenuItems = Arrays.asList(
			new MenuItemData("Donors", e -> showPanel(new DonorsPanel(), "Donors")),
			new MenuItemData("Donations", e -> showPanel(new DonationsPanel(), "Donations")),
			new MenuItemData("Grants", e -> showPanel(new GrantsPanel(), "Grants")),
			new MenuItemData("Sales & COG", e -> showPanel(new SalesAndCOGPanel(null), "Sales & COG")));

		panelMenuItems.forEach(item -> {
			JMenuItem menuItem = new JMenuItem(item.getLabel());
			menuItem.addActionListener(item.getActionListener());
			panelsMenu.add(menuItem);
		});

		menuBar.add(panelsMenu);

		////////////////////
		// Help Menu
		JMenu helpMenu = new JMenu("Help");
		List<MenuItemData> helpMenuItems = Arrays.asList(
			new MenuItemData("Help", e -> showPanel(new HelpPanel(), "Help")));
			
		helpMenuItems.forEach(item -> {
			JMenuItem menuItem = new JMenuItem(item.getLabel());
			menuItem.addActionListener(item.getActionListener());
			helpMenu.add(menuItem);
		});
		
		menuBar.add(helpMenu);

		////////////////////
		PanelContainer.frame.setJMenuBar(menuBar);
		PanelContainer.frame.setLocationRelativeTo(null);
		PanelContainer.frame.setVisible(true);
	}
	
	/**
	 * showPanel
	 * @param panel
	 * @param title
	 */
	private static void showPanel(JPanel panel, String title)
	{
		JFrame subFrame = new JFrame(title);
		subFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		subFrame.setSize(900, 600);
		subFrame.setLocationRelativeTo(PanelContainer.frame);
		subFrame.setContentPane(panel);
		subFrame.setVisible(true);
	}
	
	/**
	 * Helper class for holding Menu Item Data
	 */
	private static class MenuItemData
	{
		private String label;
		private java.awt.event.ActionListener actionListener;
		
		public MenuItemData(String label, java.awt.event.ActionListener actionListener)
		{
			this.label = label;
			this.actionListener = actionListener;
		}
		
		public String getLabel()
		{
			return this.label;
		}
		
		public java.awt.event.ActionListener getActionListener()
		{
			return this.actionListener;
		}
		
	}
	
	
}
