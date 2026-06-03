package nonprofitbookkeeping.ui;

import java.time.MonthDay;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.ReportPeriodPreset;
import nonprofitbookkeeping.ui.panels.AccountTransactionDetailsPanelFX;
import nonprofitbookkeeping.ui.panels.BalanceSheetPanelFX;
import nonprofitbookkeeping.ui.panels.BankReconciliationPanelFX;
import nonprofitbookkeeping.ui.panels.CoaEditorPanelFX;
import nonprofitbookkeeping.ui.panels.CompanySelectionPanelFX;
import nonprofitbookkeeping.ui.panels.IncomeStatementPanelFX;
import nonprofitbookkeeping.ui.panels.ReportsPanelFX;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonDashboardPanel;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonJournalPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApplicationView extends BorderPane
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplicationView.class);

    private enum ShellGroup { REVIEW, WORKFLOW, REPORTING }

    public enum PanelType
    {
        DASHBOARD,
        JOURNAL,
        COA,
        REPORTS,
        INCOME_STATEMENT,
        BALANCE_SHEET,
        COA_TABLE,
        ACCOUNT_DETAILS,
        BUDGET,
        LEDGER,
        ASSETS,
        BANK_RECONCILIATION
    }

    private final TabPane tabPane = new TabPane();
    private final VBox workspaceShell = new VBox();
    private MenuBar menuBar;

    private final Tab dashboardTab;
    private final Tab journalTab;
    private final Tab coaTab;
    private final Tab reportsTab;
    private final Tab incomeStatementTab;
    private final Tab balanceSheetTab;
    private final Tab accountDetailsTab;
    private final Tab budgetTab;
    private final Tab ledgerTab;
    private final Tab assetsTab;
    private final Tab bankReconciliationTab;

    private Label reviewGroupLabel;
    private Label workflowGroupLabel;
    private Label reportingGroupLabel;
    private final Map<Tab, ShellGroup> tabGroups = new HashMap<>();
    private final Set<Tab> warnedUnmappedTabs = new HashSet<>();

    private CoaEditorPanelFX coaEditorPanel;
    private final CompanySelectionPanelFX companySelectionPanel;
    private final SkeletonJournalPanel journalPanel;
    private final AccountTransactionDetailsPanelFX accountDetailsPanel;

    public MainApplicationView()
    {
        this.tabPane.getStyleClass().add("main-shell-tabs");
        this.workspaceShell.getStyleClass().add("workspace-shell");
        this.companySelectionPanel = new CompanySelectionPanelFX();

        this.dashboardTab = new Tab("Dashboard", new SkeletonDashboardPanel());
        this.journalPanel = new SkeletonJournalPanel();
        this.journalTab = new Tab("Journal", this.journalPanel);

        Company company = CurrentCompany.getCompany();
        ChartOfAccounts coa = company != null ? company.getChartOfAccounts() : new ChartOfAccounts();
        this.coaEditorPanel = new CoaEditorPanelFX(coa, c -> {
            if (company != null)
            {
                company.setChartOfAccounts(c);
            }
        }, () -> {});
        this.coaTab = new Tab("Chart of Accounts", createMergedCoaPanel(this.coaEditorPanel));

        this.incomeStatementTab = new Tab("Income Statement", new IncomeStatementPanelFX());
        this.balanceSheetTab = new Tab("Balance Sheet", new BalanceSheetPanelFX());
        this.reportsTab = new Tab("Reports", new ReportsPanelFX());
        this.budgetTab = new Tab("Budget", new BudgetPanel());
        this.ledgerTab = new Tab("Ledger", new LedgerPanel());
        this.assetsTab = new Tab("Assets", new AssetsPanel());
        this.bankReconciliationTab = new Tab("Bank Reconciliation", new BankReconciliationPanelFX());
        this.accountDetailsPanel = new AccountTransactionDetailsPanelFX();
        this.accountDetailsTab = new Tab("Account Details", this.accountDetailsPanel);

        registerShellTab(this.dashboardTab, ShellGroup.REVIEW, "tab-review", "tab-readonly");
        registerShellTab(this.accountDetailsTab, ShellGroup.REVIEW, "tab-review", "tab-readonly");
        registerShellTab(this.journalTab, ShellGroup.WORKFLOW, "tab-operational", "tab-workspace");
        registerShellTab(this.coaTab, ShellGroup.WORKFLOW, "tab-operational", "tab-workspace");
        registerShellTab(this.budgetTab, ShellGroup.WORKFLOW, "tab-operational", "tab-workspace");
        registerShellTab(this.ledgerTab, ShellGroup.WORKFLOW, "tab-operational", "tab-workspace");
        registerShellTab(this.assetsTab, ShellGroup.WORKFLOW, "tab-operational", "tab-workspace");
        registerShellTab(this.bankReconciliationTab, ShellGroup.WORKFLOW, "tab-operational", "tab-workspace");
        registerShellTab(this.reportsTab, ShellGroup.REPORTING, "tab-reporting", "tab-readonly");
        registerShellTab(this.incomeStatementTab, ShellGroup.REPORTING, "tab-reporting", "tab-readonly");
        registerShellTab(this.balanceSheetTab, ShellGroup.REPORTING, "tab-reporting", "tab-readonly");

        for (Tab tab : this.tabGroups.keySet())
        {
            tab.setClosable(false);
        }
        this.journalTab.getStyleClass().add("tab-operational-start");
        this.reportsTab.getStyleClass().add("tab-reporting-start");

        this.tabPane.getTabs().addAll(
            this.dashboardTab,
            this.accountDetailsTab,
            this.journalTab,
            this.coaTab,
            this.budgetTab,
            this.ledgerTab,
            this.assetsTab,
            this.bankReconciliationTab,
            this.reportsTab,
            this.incomeStatementTab,
            this.balanceSheetTab);

        this.reviewGroupLabel = createShellGroupLabel("Review", "shell-group-review");
        this.workflowGroupLabel = createShellGroupLabel("Workflows", "shell-group-operational");
        this.reportingGroupLabel = createShellGroupLabel("Reporting", "shell-group-reporting");
        HBox shellGroups = new HBox(12, this.reviewGroupLabel, this.workflowGroupLabel, this.reportingGroupLabel);
        shellGroups.getStyleClass().add("shell-nav-groups");
        VBox.setVgrow(this.tabPane, Priority.ALWAYS);
        this.workspaceShell.getChildren().setAll(shellGroups, this.tabPane);
        this.tabPane.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldTab, newTab) -> updateShellGroupHighlight(newTab));
        updateShellGroupHighlight(this.tabPane.getSelectionModel().getSelectedItem());
        setCenter(this.companySelectionPanel);
    }

    private Label createShellGroupLabel(String title, String styleClass)
    {
        Label label = new Label(title);
        label.getStyleClass().addAll("shell-nav-group-label", styleClass);
        return label;
    }

    private void registerShellTab(Tab tab, ShellGroup group, String tabClass, String surfaceClass)
    {
        applyTabSemantics(tab, tabClass, surfaceClass);
        this.tabGroups.put(tab, group);
    }

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
                LOGGER.warn("Unmapped shell tab '{}' encountered; defaulting highlight group to WORKFLOW.", selectedTab.getText());
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

    private void applyTabSemantics(Tab tab, String groupClass, String surfaceClass)
    {
        tab.getStyleClass().add(groupClass);
        if (tab.getContent() != null)
        {
            tab.getContent().getStyleClass().add(surfaceClass);
        }
    }

    private CoaEditorPanelFX createMergedCoaPanel(CoaEditorPanelFX editor)
    {
        return editor;
    }

    public CompanySelectionPanelFX getCompanySelectionPanel()
    {
        return this.companySelectionPanel;
    }

    public void showCompanySelection()
    {
        setCenter(this.companySelectionPanel);
    }

    public void showWorkspaceTabs()
    {
        setCenter(this.workspaceShell);
    }

    public void setMenuBar(MenuBar menuBar)
    {
        this.menuBar = menuBar;
        setTop(this.menuBar);
    }

    public void showPanel(PanelType panelType)
    {
        if (getCenter() != this.workspaceShell)
        {
            showWorkspaceTabs();
        }

        switch (panelType)
        {
            case DASHBOARD:
                this.tabPane.getSelectionModel().select(this.dashboardTab);
                break;
            case JOURNAL:
                this.tabPane.getSelectionModel().select(this.journalTab);
                break;
            case COA:
            case COA_TABLE:
                this.tabPane.getSelectionModel().select(this.coaTab);
                break;
            case REPORTS:
                this.tabPane.getSelectionModel().select(this.reportsTab);
                break;
            case INCOME_STATEMENT:
                this.tabPane.getSelectionModel().select(this.incomeStatementTab);
                break;
            case BALANCE_SHEET:
                this.tabPane.getSelectionModel().select(this.balanceSheetTab);
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
                LOGGER.warn("Unknown panel type: {}", panelType);
                break;
        }
    }

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
            ChartOfAccounts coa = company != null ? company.getChartOfAccounts() : new ChartOfAccounts();
            if (this.coaEditorPanel == null)
            {
                this.coaEditorPanel = new CoaEditorPanelFX(coa, c -> {
                    if (company != null)
                    {
                        company.setChartOfAccounts(c);
                    }
                }, () -> {});
                this.coaTab.setContent(createMergedCoaPanel(this.coaEditorPanel));
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

    public SkeletonJournalPanel getJournalPanel()
    {
        return this.journalPanel;
    }

    public AccountTransactionDetailsPanelFX getAccountDetailsPanel()
    {
        return this.accountDetailsPanel;
    }

    public void applyAccountDetailsDefaults(ReportPeriodPreset preset, MonthDay fiscalYearStart,
        boolean showYearToDate, boolean showFullYear, boolean showLastMonth)
    {
        if (preset == null || this.accountDetailsPanel == null)
        {
            return;
        }
        this.accountDetailsPanel.applyDefaultPeriod(preset, fiscalYearStart);
        this.accountDetailsPanel.configureQuickRanges(showYearToDate, showFullYear, showLastMonth, fiscalYearStart);
    }
}
