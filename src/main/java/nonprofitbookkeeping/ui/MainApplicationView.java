
package nonprofitbookkeeping.ui;

import javafx.scene.control.MenuBar;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import nonprofitbookkeeping.ui.panels.BalanceSheetPanelFX;
import nonprofitbookkeeping.ui.panels.CoaEditorPanelFX;
import nonprofitbookkeeping.ui.panels.CompanySelectionPanelFX;
import nonprofitbookkeeping.ui.panels.BankReconciliationPanelFX;
import nonprofitbookkeeping.ui.panels.IncomeStatementPanelFX;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonDashboardPanel;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonJournalPanel;
import nonprofitbookkeeping.ui.panels.AccountTransactionDetailsPanelFX; 
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.ReportPeriodPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.MonthDay;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tabbed workspace for the {@link NonprofitBookkeepingFX} UI system.
 *
 * <p>Status: live when {@link NonprofitBookkeepingFX} is launched directly. It
 * uses a {@link TabPane} in the center to display Dashboard, Journal, Chart of
 * Accounts, Reports, and Account Details. The top area is reserved for a
 * {@link MenuBar} supplied by {@link NonprofitBookkeepingFX}.</p>
 */
public class MainApplicationView extends BorderPane
{
	/** Logger for shell-level UI diagnostics. */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(MainApplicationView.class);

	/** Semantic group for shell hierarchy highlighting. */
	private enum ShellGroup
	{
		REVIEW,
		WORKFLOW,
		REPORTING
	}
	
	/**
	 * Enum defining the different types of panels/tabs that can be displayed
	 * in the main application view.
	 */
	public enum PanelType
	{
		/** Represents the Dashboard panel. */
		DASHBOARD,
		/** Represents the Journal panel. */
		JOURNAL,
		/** Represents the Chart of Accounts panel. */
		COA,
		/** Represents the Reports panel. */
		REPORTS,
		/** Represents the Income Statement panel. */
		INCOME_STATEMENT,
		/** Represents the Balance Sheet panel. */
		BALANCE_SHEET,
		/** Represents the Chart of Accounts table panel. */
		COA_TABLE,
		/** Represents the Account Transaction Details panel. */
		ACCOUNT_DETAILS,
		/** Represents the Budget workspace panel. */
		BUDGET,
		/** Represents the Ledger workspace panel. */
		LEDGER,
		/** Represents the Assets workspace panel. */
		ASSETS,
		/** Represents the bank reconciliation panel. */
		BANK_RECONCILIATION
	}
	
	/** The TabPane used to display different application sections. */
	private TabPane tabPane;
	/** Shell container that hosts hierarchy legend and top-level tabs. */
	private VBox workspaceShell;
	/** The main MenuBar for the application, set externally. */
	private MenuBar menuBar;
	
	/** Tab for displaying the Dashboard. */
	private Tab dashboardTab;
	/** Tab for displaying the Journal. */
	private Tab journalTab;
	/** Tab for displaying the Chart of Accounts. */
	private Tab coaTab;
	/** Tab for displaying Reports. */
	private Tab reportsTab;
	/** Tab for displaying Income Statement. */
	private Tab incomeStatementTab;
	/** Tab for displaying Balance Sheet. */
	private Tab balanceSheetTab;
	/** Tab for displaying Account Transaction Details. */
	private Tab accountDetailsTab;
	/** Tab for displaying Budget workspace. */
	private Tab budgetTab;
	/** Tab for displaying Ledger workspace. */
	private Tab ledgerTab;
	/** Tab for displaying Assets workspace. */
	private Tab assetsTab;
	/** Tab for bank reconciliation. */
	private Tab bankReconciliationTab;
	/** Shell legend label for review surfaces. */
	private Label reviewGroupLabel;
	/** Shell legend label for operational workflow surfaces. */
	private Label workflowGroupLabel;
	/** Shell legend label for reporting surfaces. */
	private Label reportingGroupLabel;
	/** Explicit shell group mapping to avoid style-class inference drift. */
	private final Map<Tab, ShellGroup> tabGroups;
	/** Tracks which tabs have already emitted unmapped-group warnings. */
	private final Set<Tab> warnedUnmappedTabs;
	/** Embedded Chart of Accounts editor panel. */
	private CoaEditorPanelFX coaEditorPanel;
	/** Embedded Chart of Accounts tabular panel shown with the editor. */
	/** Panel used to select or create companies when none are open. */
	private final CompanySelectionPanelFX companySelectionPanel;
	/** Journal panel instance to expose search helpers. */
	private final SkeletonJournalPanel journalPanel;
	/** Account details panel for report defaults. */
	private final AccountTransactionDetailsPanelFX accountDetailsPanel;
	
	
	/**
	 * Constructs a new {@code MainApplicationView}.
	 * Initializes the {@link TabPane} and creates non-closable tabs for Dashboard,
	 * Journal, Chart of Accounts, Reports, and Account Details, each populated
	 * with their respective panels (currently skeleton or placeholder panels).
	 * The TabPane is set as the center content of this BorderPane.
	 * The MenuBar is initialized to null and is expected to be set via {@link #setMenuBar(MenuBar)}.
	 */
	public MainApplicationView()
	{
		this.menuBar = null; // Initialize menuBar, will be set via setter
		
		this.tabPane = new TabPane();
		this.tabPane.getStyleClass().add("main-shell-tabs");
		this.workspaceShell = new VBox();
		this.workspaceShell.getStyleClass().add("workspace-shell");
		this.companySelectionPanel = new CompanySelectionPanelFX();
		this.tabGroups = new HashMap<>();
		this.warnedUnmappedTabs = new HashSet<>();
		
		// Create Tab instances
		this.dashboardTab = new Tab("Dashboard", new SkeletonDashboardPanel());
		this.journalPanel = new SkeletonJournalPanel();
		this.journalTab = new Tab("Journal", this.journalPanel);
		
		Company company = CurrentCompany.getCompany();
		ChartOfAccounts coa =
			company != null ? company.getChartOfAccounts() :
				new ChartOfAccounts();
		this.coaEditorPanel = new CoaEditorPanelFX(coa, c -> {
			
			if (company != null)
			{
				company.setChartOfAccounts(c);
			}
			
		}, () -> {});
		this.coaTab = new Tab("Chart of Accounts",
			createMergedCoaPanel(this.coaEditorPanel));
		
		this.incomeStatementTab = new Tab("Income Statement",
			new IncomeStatementPanelFX());
		this.balanceSheetTab = new Tab("Balance Sheet",
			new BalanceSheetPanelFX());
		this.budgetTab = new Tab("Budget", new BudgetPanel());
		this.ledgerTab = new Tab("Ledger", new LedgerPanel());
		this.assetsTab = new Tab("Assets", new AssetsPanel());
		this.bankReconciliationTab = new Tab("Bank Reconciliation", new BankReconciliationPanelFX());

		registerShellTab(this.dashboardTab, ShellGroup.REVIEW, "tab-review",
			"tab-readonly");
		registerShellTab(this.journalTab, ShellGroup.WORKFLOW,
			"tab-operational", "tab-workspace");
		registerShellTab(this.coaTab, ShellGroup.WORKFLOW, "tab-operational",
			"tab-workspace");
		registerShellTab(this.budgetTab, ShellGroup.WORKFLOW,
			"tab-operational", "tab-workspace");
		registerShellTab(this.ledgerTab, ShellGroup.WORKFLOW,
			"tab-operational", "tab-workspace");
		registerShellTab(this.assetsTab, ShellGroup.WORKFLOW,
			"tab-operational", "tab-workspace");
		registerShellTab(this.bankReconciliationTab, ShellGroup.WORKFLOW,
			"tab-operational", "tab-workspace");
		registerShellTab(this.reportsTab, ShellGroup.REPORTING, "tab-reporting",
			"tab-readonly");
		registerShellTab(this.incomeStatementTab, ShellGroup.REPORTING,
			"tab-reporting", "tab-readonly");
		registerShellTab(this.balanceSheetTab, ShellGroup.REPORTING,
			"tab-reporting", "tab-readonly");

		// Set tabs to be non-closable
		this.dashboardTab.setClosable(false);
		this.journalTab.setClosable(false);
		this.coaTab.setClosable(false);
		this.reportsTab.setClosable(false);
		this.incomeStatementTab.setClosable(false);
		this.balanceSheetTab.setClosable(false);
		this.budgetTab.setClosable(false);
		this.ledgerTab.setClosable(false);
		this.assetsTab.setClosable(false);
		this.bankReconciliationTab.setClosable(false);
		
		// Add new tab for Account Details
		this.accountDetailsPanel = new AccountTransactionDetailsPanelFX();
		this.accountDetailsTab =
			new Tab("Account Details", this.accountDetailsPanel);
		this.accountDetailsTab.setClosable(false);
		registerShellTab(this.accountDetailsTab, ShellGroup.REVIEW, "tab-review",
			"tab-readonly");
		this.journalTab.getStyleClass().add("tab-operational-start");
		this.reportsTab.getStyleClass().add("tab-reporting-start");
		
		// Add tabs to the tabPane
		this.tabPane.getTabs()
			.addAll(this.dashboardTab,
				this.accountDetailsTab,
				this.journalTab,
				this.coaTab,
				this.budgetTab,
				this.ledgerTab,
				this.assetsTab,
				this.bankReconciliationTab,
				this.reportsTab,
				this.incomeStatementTab,
				this.balanceSheetTab
			);
		this.reviewGroupLabel =
			createShellGroupLabel("Review", "shell-group-review");
		this.workflowGroupLabel =
			createShellGroupLabel("Workflows", "shell-group-operational");
		this.reportingGroupLabel =
			createShellGroupLabel("Reporting", "shell-group-reporting");
		HBox shellGroups = new HBox(12, this.reviewGroupLabel,
			this.workflowGroupLabel, this.reportingGroupLabel);
		shellGroups.getStyleClass().add("shell-nav-groups");
		VBox.setVgrow(this.tabPane, Priority.ALWAYS);
		this.workspaceShell.getChildren().setAll(shellGroups, this.tabPane);
		this.tabPane.getSelectionModel().selectedItemProperty()
			.addListener((obs, oldTab, newTab) -> updateShellGroupHighlight(newTab));
		updateShellGroupHighlight(this.tabPane.getSelectionModel().getSelectedItem());
		
		// Default to the company selection view until a company is opened.
		setCenter(this.companySelectionPanel);
		
	}

	/**
	 * Creates a shell-level hierarchy legend label.
	 *
	 * @param title legend text
	 * @param styleClass semantic style class for legend color/weight
	 * @return configured legend label
	 */
	private Label createShellGroupLabel(String title, String styleClass)
	{
		Label label = new Label(title);
		label.getStyleClass().addAll("shell-nav-group-label", styleClass);
		return label;
	}

	/**
	 * Applies style semantics and explicit group mapping for one shell tab.
	 *
	 * @param tab shell tab
	 * @param group explicit semantic group
	 * @param tabClass tab style class
	 * @param surfaceClass content-surface class
	 */
	private void registerShellTab(Tab tab, ShellGroup group, String tabClass,
		String surfaceClass)
	{
		applyTabSemantics(tab, tabClass, surfaceClass);
		this.tabGroups.put(tab, group);
	}

	/**
	 * Highlights the active shell legend group based on the selected top-level tab.
	 *
	 * @param selectedTab currently selected tab
	 */
	private void updateShellGroupHighlight(Tab selectedTab)
	{
		this.reviewGroupLabel.getStyleClass().remove("shell-group-active");
		this.workflowGroupLabel.getStyleClass().remove("shell-group-active");
		this.reportingGroupLabel.getStyleClass().remove("shell-group-active");
		if (selectedTab == null)
		{
			return;
		}
		ShellGroup group = this.tabGroups.get(selectedTab);
		if (group == null)
		{
			group = ShellGroup.WORKFLOW;
			if (this.warnedUnmappedTabs.add(selectedTab))
			{
				LOGGER.warn(
					"Unmapped shell tab '{}' encountered; defaulting highlight group to WORKFLOW.",
					selectedTab.getText());
			}
		}
		switch (group)
		{
			case REVIEW:
				this.reviewGroupLabel.getStyleClass().add("shell-group-active");
				break;
			case REPORTING:
				this.reportingGroupLabel.getStyleClass().add("shell-group-active");
				break;
			case WORKFLOW:
			default:
				this.workflowGroupLabel.getStyleClass().add("shell-group-active");
				break;
		}
	}

	/**
	 * Applies semantic style classes to a shell tab and its content surface.
	 *
	 * @param tab target shell tab
	 * @param groupClass group classification (for example operational/reporting)
	 * @param surfaceClass content surface classification (workspace/read-only)
	 */
	private void applyTabSemantics(Tab tab, String groupClass, String surfaceClass)
	{
		tab.getStyleClass().add(groupClass);
		if (tab.getContent() != null)
		{
			tab.getContent().getStyleClass().add(surfaceClass);
		}
	}

	/**
	 * Returns the unified Chart of Accounts workspace.
	 *
	 * @param editor the ladder/tree editor panel
	 * @return unified COA workspace container
	 */
	private CoaEditorPanelFX createMergedCoaPanel(CoaEditorPanelFX editor)
	{
		return editor;
	}
	
	/**
	 * Exposes the company selection panel for additional configuration.
	 *
	 * @return the company selection panel
	 */
	public CompanySelectionPanelFX getCompanySelectionPanel()
	{
		return this.companySelectionPanel;
		
	}
	
	/** Displays the company selection panel in the main content area. */
	public void showCompanySelection()
	{
		setCenter(this.companySelectionPanel);
		
	}
	
	/** Restores the workspace tab pane to the main content area. */
	public void showWorkspaceTabs()
	{
		setCenter(this.workspaceShell);
		
	}
	
	/**
	 * Sets the main {@link MenuBar} for the application view.
	 * The provided MenuBar will be placed in the top region of this BorderPane.
	 *
	 * @param menuBar The {@link MenuBar} to be displayed at the top of the application.
	 */
	public void setMenuBar(MenuBar menuBar)
	{
		this.menuBar = menuBar;
		setTop(this.menuBar); // Directly set the MenuBar to the top
		
	}
	
	/**
	 * Switches the visible tab in the central {@link TabPane} to the one
	 * corresponding to the specified {@link PanelType}.
	 * If an unknown panel type is provided, an error message is printed to standard error,
	 * and no tab selection change occurs (unless a fallback is implemented).
	 *
	 * @param panelType The {@link PanelType} indicating which tab/panel to display.
	 */
	public void showPanel(PanelType panelType)
	{
		
		if (getCenter() != this.tabPane)
		{
			showWorkspaceTabs();
		}
		
		switch(panelType)
		{
			case DASHBOARD:
				this.tabPane.getSelectionModel().select(this.dashboardTab);
				break;
			
			case JOURNAL:
				this.tabPane.getSelectionModel().select(this.journalTab);
				break;
			
			case COA:
				this.tabPane.getSelectionModel().select(this.coaTab);
				break;
			
			case REPORTS:
				this.tabPane.getSelectionModel().select(this.reportsTab);
				break;
			
			case INCOME_STATEMENT:
				this.tabPane.getSelectionModel()
					.select(this.incomeStatementTab);
				break;
			
			case BALANCE_SHEET:
				this.tabPane.getSelectionModel().select(this.balanceSheetTab);
				break;
			
			case COA_TABLE:
				this.tabPane.getSelectionModel().select(this.coaTab);
				break;
			
			case ACCOUNT_DETAILS:
				this.tabPane.getSelectionModel().select(this.accountDetailsTab);
				break;

			case BUDGET:
				this.tabPane.getSelectionModel().select(this.budgetTab);
				break;

			case LEDGER:
				this.tabPane.getSelectionModel().select(this.ledgerTab);
				break;

			case ASSETS:
				this.tabPane.getSelectionModel().select(this.assetsTab);
				break;

			case BANK_RECONCILIATION:
				this.tabPane.getSelectionModel().select(this.bankReconciliationTab);
				break;
			
			default:
				// Optionally, log an error or select a default tab
				System.err.println("Unknown panel type: " + panelType); // Consider
																		// using
																		// a
																		// logger
				break;
		}
		
	}
	
	/**
	 * Enables or disables primary tabs based on whether a company is open
	 * and shows or hides the company selection tab accordingly.
	 *
	 * @param companyOpen {@code true} if a company is currently open.
	 */
	public void updateCompanyOpenState(boolean companyOpen)
	{
		this.dashboardTab.setDisable(!companyOpen);
		this.journalTab.setDisable(!companyOpen);
		this.coaTab.setDisable(!companyOpen);
		this.budgetTab.setDisable(!companyOpen);
		this.ledgerTab.setDisable(!companyOpen);
		this.assetsTab.setDisable(!companyOpen);
		this.bankReconciliationTab.setDisable(!companyOpen);
		this.reportsTab.setDisable(!companyOpen);
		this.incomeStatementTab.setDisable(!companyOpen);
		this.balanceSheetTab.setDisable(!companyOpen);
		this.accountDetailsTab.setDisable(!companyOpen);
		
		if (companyOpen)
		{
			showWorkspaceTabs();
			Company company = CurrentCompany.getCompany();
			ChartOfAccounts coa =
				company != null ? company.getChartOfAccounts() :
					new ChartOfAccounts();
			
			if (this.coaEditorPanel == null)
			{
				this.coaEditorPanel = new CoaEditorPanelFX(coa, c -> {
					
					if (company != null)
					{
						company.setChartOfAccounts(c);
					}
					
				}, () -> {});
				this.coaTab.setContent(
					createMergedCoaPanel(this.coaEditorPanel));
			}
			else
			{
				this.coaEditorPanel.setChartOfAccounts(coa);
			}
			
		}
		else
		{
			showCompanySelection();
		}
		
	}
	
	/**
	 * Provides access to the shared journal panel instance.
	 *
	 * @return the journal panel
	 */
	public SkeletonJournalPanel getJournalPanel()
	{
		return this.journalPanel;
		
	}
	
	/**
	 * Exposes the account details panel instance.
	 *
	 * @return the account details panel
	 */
	public AccountTransactionDetailsPanelFX getAccountDetailsPanel()
	{
		return this.accountDetailsPanel;
		
	}
	
	/**
	 * Applies the configured default report period to the account details view.
	 *
	 * @param preset the preset
	 * @param fiscalYearStart the fiscal year start
	 * @param showYearToDate the show year to date
	 * @param showFullYear the show full year
	 * @param showLastMonth the show last month
	 */
	public void applyAccountDetailsDefaults(ReportPeriodPreset preset,
		MonthDay fiscalYearStart,
		boolean showYearToDate, boolean showFullYear, boolean showLastMonth)
	{
		
		if (preset == null || this.accountDetailsPanel == null)
		{
			return;
		}
		
		this.accountDetailsPanel.applyDefaultPeriod(preset, fiscalYearStart);
		this.accountDetailsPanel.configureQuickRanges(showYearToDate,
			showFullYear, showLastMonth,
			fiscalYearStart);
		
	}
	
}
