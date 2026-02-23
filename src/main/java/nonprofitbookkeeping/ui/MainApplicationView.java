
package nonprofitbookkeeping.ui;

import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import nonprofitbookkeeping.ui.panels.BalanceSheetPanelFX;
import nonprofitbookkeeping.ui.panels.ChartOfAccountsTablePanelFX;
import nonprofitbookkeeping.ui.panels.CoaEditorPanelFX;
import nonprofitbookkeeping.ui.panels.CompanySelectionPanelFX;
import nonprofitbookkeeping.ui.panels.IncomeStatementPanelFX;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonDashboardPanel;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonJournalPanel;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonReportsPanel;
import nonprofitbookkeeping.ui.panels.AccountTransactionDetailsPanelFX; // Added import
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.ReportPeriodPreset;

import java.time.MonthDay;

// TODO: Auto-generated Javadoc
/**
 * Represents the main application view, structured as a {@link BorderPane}.
 * It uses a {@link TabPane} in the center to display different sections of the application
 * like Dashboard, Journal, Chart of Accounts, Reports, and Account Details.
 * The top area is reserved for a {@link MenuBar}.
 */
public class MainApplicationView extends BorderPane
{
	
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
		ACCOUNT_DETAILS
	}
	
	/** The TabPane used to display different application sections. */
	private TabPane tabPane;
	/** The main MenuBar for the application, set externally. */
	private MenuBar menuBar;
	
	// Tab instances as fields for easy reference
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
        /** Tab for displaying Chart of Accounts table. */
        private Tab coaTableTab;
        /** Tab for displaying Account Transaction Details. */
        private Tab accountDetailsTab;
        /** Embedded Chart of Accounts editor panel. */
        private CoaEditorPanelFX coaEditorPanel;
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
                this.companySelectionPanel = new CompanySelectionPanelFX();
		
		// Create Tab instances
                this.dashboardTab = new Tab("Dashboard", new SkeletonDashboardPanel());
                this.journalPanel = new SkeletonJournalPanel();
                this.journalTab = new Tab("Journal", this.journalPanel);
		
		Company company = CurrentCompany.getCompany();
		ChartOfAccounts coa =
				company != null ? company.getChartOfAccounts() : new ChartOfAccounts();
		this.coaEditorPanel = new CoaEditorPanelFX(coa, c -> {
			
			if (company != null)
			{
				company.setChartOfAccounts(c);
			}
			
		}, () -> {
		});
		this.coaTab = new Tab("Chart of Accounts", this.coaEditorPanel);
		
		this.reportsTab = new Tab("Reports", new SkeletonReportsPanel());
		this.incomeStatementTab = new Tab("Income Statement",
			new IncomeStatementPanelFX());
		this.balanceSheetTab = new Tab("Balance Sheet",
			new BalanceSheetPanelFX());
		this.coaTableTab = new Tab("Chart of Accounts Table",
			new ChartOfAccountsTablePanelFX());
		
		// Set tabs to be non-closable
		this.dashboardTab.setClosable(false);
		this.journalTab.setClosable(false);
		this.coaTab.setClosable(false);
		this.reportsTab.setClosable(false);
		this.incomeStatementTab.setClosable(false);
		this.balanceSheetTab.setClosable(false);
		this.coaTableTab.setClosable(false);
		
		// Add new tab for Account Details
                this.accountDetailsPanel = new AccountTransactionDetailsPanelFX();
                this.accountDetailsTab = new Tab("Account Details", this.accountDetailsPanel);
                this.accountDetailsTab.setClosable(false);
	
		
                // Add tabs to the tabPane
                this.tabPane.getTabs()
                                .addAll(this.dashboardTab,
                                                this.journalTab,
                                                this.coaTab,
                                                this.coaTableTab,
                                                this.reportsTab,
                                                this.incomeStatementTab,
                                                this.balanceSheetTab,
                                                this.accountDetailsTab
                                );

                // Default to the company selection view until a company is opened.
                setCenter(this.companySelectionPanel);

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
                setCenter(this.tabPane);
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
				this.tabPane.getSelectionModel().select(this.coaTableTab);
				break;
			
			case ACCOUNT_DETAILS:
				this.tabPane.getSelectionModel().select(this.accountDetailsTab);
				break;
			
			default:
				// Optionally, log an error or select a default tab
				System.err.println("Unknown panel type: " + panelType); // Consider using a logger
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
                this.coaTableTab.setDisable(!companyOpen);
                this.reportsTab.setDisable(!companyOpen);
                this.incomeStatementTab.setDisable(!companyOpen);
                this.balanceSheetTab.setDisable(!companyOpen);
                this.accountDetailsTab.setDisable(!companyOpen);

                if (companyOpen)
                {
                        showWorkspaceTabs();
                        Company company = CurrentCompany.getCompany();
                        ChartOfAccounts coa =
                                        company != null ? company.getChartOfAccounts() : new ChartOfAccounts();

                        if (this.coaEditorPanel == null)
			{
				this.coaEditorPanel = new CoaEditorPanelFX(coa, c -> {
					
					if (company != null)
					{
						company.setChartOfAccounts(c);
					}
					
				}, () -> {
				});
				this.coaTab.setContent(this.coaEditorPanel);
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
        public void applyAccountDetailsDefaults(ReportPeriodPreset preset, MonthDay fiscalYearStart,
                boolean showYearToDate, boolean showFullYear, boolean showLastMonth)
        {
                if (preset == null || this.accountDetailsPanel == null)
                {
                        return;
                }

                this.accountDetailsPanel.applyDefaultPeriod(preset, fiscalYearStart);
                this.accountDetailsPanel.configureQuickRanges(showYearToDate, showFullYear, showLastMonth,
                        fiscalYearStart);
        }

}
