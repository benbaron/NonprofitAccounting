package nonprofitbookkeeping.ui;

import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
// import javafx.scene.layout.VBox; // No longer needed for sidebar
// import javafx.scene.Node; // No longer needed for setContent
// import javafx.geometry.Insets; // No longer needed for sidebar
// import javafx.scene.control.Button; // No longer needed for sidebar
// import javafx.scene.layout.StackPane; // No longer needed for contentArea

import nonprofitbookkeeping.ui.panels.skeletons.SkeletonCoaPanel;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonDashboardPanel;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonJournalPanel;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonReportsPanel;
import nonprofitbookkeeping.ui.panels.AccountTransactionDetailsPanelFX; // Added import

public class MainApplicationView extends BorderPane {

    public enum PanelType {
        DASHBOARD, JOURNAL, COA, REPORTS, ACCOUNT_DETAILS // Added ACCOUNT_DETAILS
    }

    private TabPane tabPane;
    private MenuBar menuBar; // To be set from NonprofitBookkeepingFX

    // Tab instances as fields
    private Tab dashboardTab;
    private Tab journalTab;
    private Tab coaTab;
    private Tab reportsTab;
    private Tab accountDetailsTab; // Added field for the new tab

    public MainApplicationView() {
        this.menuBar = null; // Initialize menuBar, will be set via setter

        tabPane = new TabPane();

        // Create Tab instances
        dashboardTab = new Tab("Dashboard", new SkeletonDashboardPanel());
        journalTab = new Tab("Journal", new SkeletonJournalPanel());
        coaTab = new Tab("Chart of Accounts", new SkeletonCoaPanel());
        reportsTab = new Tab("Reports", new SkeletonReportsPanel());

        // Set tabs to be non-closable
        dashboardTab.setClosable(false);
        journalTab.setClosable(false);
        coaTab.setClosable(false);
        reportsTab.setClosable(false);

        // Add new tab for Account Details
        accountDetailsTab = new Tab("Account Details", new AccountTransactionDetailsPanelFX());
        accountDetailsTab.setClosable(false);

        // Add tabs to the tabPane
        tabPane.getTabs().addAll(dashboardTab, journalTab, coaTab, reportsTab, accountDetailsTab);

        // Set the TabPane as the center of the BorderPane
        setCenter(tabPane);

        // The TOP will be set via setMenuBar()
    }

    public void setMenuBar(MenuBar menuBar) {
        this.menuBar = menuBar;
        setTop(this.menuBar); // Directly set the MenuBar to the top
    }

    public void showPanel(PanelType panelType) {
        switch (panelType) {
            case DASHBOARD:
                tabPane.getSelectionModel().select(dashboardTab);
                break;
            case JOURNAL:
                tabPane.getSelectionModel().select(journalTab);
                break;
            case COA:
                tabPane.getSelectionModel().select(coaTab);
                break;
            case REPORTS:
                tabPane.getSelectionModel().select(reportsTab);
                break;
            case ACCOUNT_DETAILS: // Added case for ACCOUNT_DETAILS
                tabPane.getSelectionModel().select(accountDetailsTab);
                break;
            default:
                // Optionally, log an error or select a default tab
                System.err.println("Unknown panel type: " + panelType);
                // tabPane.getSelectionModel().select(dashboardTab); // Fallback to dashboard
                break;
        }
    }

    // setContent(Node node) method is removed as TabPane handles content directly.
}
