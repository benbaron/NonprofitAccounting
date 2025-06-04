package nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonDashboardPanel;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonJournalPanel;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonCoaPanel;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonReportsPanel;

public class MainApplicationView extends BorderPane {

    public enum PanelType {
        DASHBOARD, JOURNAL, COA, REPORTS
    }

    private StackPane contentArea;

    public MainApplicationView() {
        // Sidebar
        VBox sidebar = new VBox();
        sidebar.setPadding(new Insets(10));
        sidebar.setSpacing(10);

        Button dashboardButton = new Button("Dashboard");
        Button journalButton = new Button("Journal");
        Button chartOfAccountsButton = new Button("Chart of Accounts");
        Button reportsButton = new Button("Reports");

        sidebar.getChildren().addAll(dashboardButton, journalButton, chartOfAccountsButton, reportsButton);
        this.setLeft(sidebar);

        // Content Area
        contentArea = new StackPane();
        this.setCenter(contentArea);

        // Button Actions
        dashboardButton.setOnAction(e -> setContent(new SkeletonDashboardPanel()));
        journalButton.setOnAction(e -> setContent(new SkeletonJournalPanel()));
        chartOfAccountsButton.setOnAction(e -> setContent(new SkeletonCoaPanel()));
        reportsButton.setOnAction(e -> setContent(new SkeletonReportsPanel()));

        // Set initial content
        setContent(new SkeletonDashboardPanel());
    }

    private void setContent(Node node) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
    }

    public void showPanel(PanelType panelType) {
        switch (panelType) {
            case DASHBOARD:
                setContent(new SkeletonDashboardPanel());
                break;
            case JOURNAL:
                setContent(new SkeletonJournalPanel());
                break;
            case COA:
                setContent(new SkeletonCoaPanel());
                break;
            case REPORTS:
                setContent(new SkeletonReportsPanel());
                break;
            default:
                // Optionally, show a default panel or log an error
                setContent(new Label("Unknown panel type: " + panelType));
                break;
        }
    }
}
