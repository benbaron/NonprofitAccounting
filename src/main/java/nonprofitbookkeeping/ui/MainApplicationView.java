package nonprofitbookkeeping.ui;

import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;


import nonprofitbookkeeping.ui.panels.skeletons.SkeletonCoaPanel;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonDashboardPanel;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonJournalPanel;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonReportsPanel;
import nonprofitbookkeeping.ui.panels.AccountTransactionDetailsPanelFX; // Added import

/**
 * Represents the main application view, structured as a {@link BorderPane}.
 * It uses a {@link TabPane} in the center to display different sections of the application
 * like Dashboard, Journal, Chart of Accounts, Reports, and Account Details.
 * The top area is reserved for a {@link MenuBar}.
 */
public class MainApplicationView extends BorderPane {

    /**
     * Enum defining the different types of panels/tabs that can be displayed
     * in the main application view.
     */
    public enum PanelType {
        /** Represents the Dashboard panel. */
        DASHBOARD,
        /** Represents the Journal panel. */
        JOURNAL,
        /** Represents the Chart of Accounts panel. */
        COA,
        /** Represents the Reports panel. */
        REPORTS,
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
    /** Tab for displaying Account Transaction Details. */
    private Tab accountDetailsTab;

    /**
     * Constructs a new {@code MainApplicationView}.
     * Initializes the {@link TabPane} and creates non-closable tabs for Dashboard,
     * Journal, Chart of Accounts, Reports, and Account Details, each populated
     * with their respective panels (currently skeleton or placeholder panels).
     * The TabPane is set as the center content of this BorderPane.
     * The MenuBar is initialized to null and is expected to be set via {@link #setMenuBar(MenuBar)}.
     */
    public MainApplicationView() {
        this.menuBar = null; // Initialize menuBar, will be set via setter

        this.tabPane = new TabPane();

        // Create Tab instances
        this.dashboardTab = new Tab("Dashboard", new SkeletonDashboardPanel());
        this.journalTab = new Tab("Journal", new SkeletonJournalPanel());
        this.coaTab = new Tab("Chart of Accounts", new SkeletonCoaPanel());
        this.reportsTab = new Tab("Reports", new SkeletonReportsPanel());

        // Set tabs to be non-closable
        this.dashboardTab.setClosable(false);
        this.journalTab.setClosable(false);
        this.coaTab.setClosable(false);
        this.reportsTab.setClosable(false);

        // Add new tab for Account Details
        this.accountDetailsTab = new Tab("Account Details", new AccountTransactionDetailsPanelFX());
        this.accountDetailsTab.setClosable(false);

        // Add tabs to the tabPane
        this.tabPane.getTabs().addAll(this.dashboardTab, this.journalTab, this.coaTab, this.reportsTab, this.accountDetailsTab);

        // Set the TabPane as the center of the BorderPane
        setCenter(this.tabPane);

        // The TOP will be set via setMenuBar()
    }

    /**
     * Sets the main {@link MenuBar} for the application view.
     * The provided MenuBar will be placed in the top region of this BorderPane.
     *
     * @param menuBar The {@link MenuBar} to be displayed at the top of the application.
     */
    public void setMenuBar(MenuBar menuBar) {
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
    public void showPanel(PanelType panelType) {
        switch (panelType) {
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
            case ACCOUNT_DETAILS:
                this.tabPane.getSelectionModel().select(this.accountDetailsTab);
                break;
            default:
                // Optionally, log an error or select a default tab
                System.err.println("Unknown panel type: " + panelType); // Consider using a logger
                // tabPane.getSelectionModel().select(dashboardTab); // Fallback to dashboard
                break;
        }
    }

    // setContent(Node node) method is removed as TabPane handles content directly.
}
