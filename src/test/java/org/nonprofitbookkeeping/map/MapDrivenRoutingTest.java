package org.nonprofitbookkeeping.map;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.nonprofitbookkeeping.ui.AppPanelId;
import org.nonprofitbookkeeping.ui.NavigationPane;
import org.nonprofitbookkeeping.ui.PanelHost;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MapDrivenRoutingTest extends ApplicationTest
{
    private PanelHost host;
    private TreeView<NavigationPane.NavItem> tree;

    @Override
    public void start(Stage stage)
    {
        this.host = new PanelHost();

        NavigationPane nav = new NavigationPane(this.host::show, (title, body) -> { });
        @SuppressWarnings("unchecked")
        TreeView<NavigationPane.NavItem> navTree = (TreeView<NavigationPane.NavItem>) nav.getChildren().get(0);
        this.tree = navTree;

        Button inventoryRun = new Button("Inventory & Depreciation");
        inventoryRun.setId("inventoryRunBtn");
        inventoryRun.setOnAction(e -> this.host.show(AppPanelId.INVENTORY));

        Button reportsRun = new Button("Reports Workspace");
        reportsRun.setId("reportsRunBtn");
        reportsRun.setOnAction(e -> this.host.show(AppPanelId.REPORTS_WORKSPACE));

        BorderPane root = new BorderPane();
        root.setTop(new HBox(8, inventoryRun, reportsRun));
        root.setLeft(nav);
        root.setCenter(this.host);

        this.host.show(AppPanelId.LEDGER_REGISTER);

        stage.setScene(new Scene(root, 1200, 800));
        stage.show();
    }


    @Test
    void startsOnLedgerRegisterInTestHarness()
    {
        assertEquals("Ledger Register", this.host.getActiveTitle());
    }

    @Test
    void dashboardAndReportsAliasRoutesAreDeterministic()
    {
        TreeItem<NavigationPane.NavItem> dashboard = findByLabel(this.tree.getRoot(), "Dashboard");
        assertNotNull(dashboard);
        assertEquals(AppPanelId.DASHBOARD, dashboard.getValue().panelId());

        interact(() -> this.host.show(dashboard.getValue().panelId()));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Dashboard", this.host.getActiveTitle());

        interact(() -> this.host.show(AppPanelId.REPORT_LIBRARY));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Reports", this.host.getActiveTitle());

        interact(() -> this.host.show(AppPanelId.REPORTS_WORKSPACE));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Reports", this.host.getActiveTitle());
    }

    @Test
    void runActionsRouteToMapTargets()
    {
        interact(() -> ((Button) lookup("#inventoryRunBtn").query()).fire());
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Inventory", this.host.getActiveTitle());

        interact(() -> ((Button) lookup("#reportsRunBtn").query()).fire());
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Reports", this.host.getActiveTitle());

        interact(() -> this.host.show(AppPanelId.REPORT_LIBRARY));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Reports", this.host.getActiveTitle());
    }

    @Test
    void navigationNodesMapToExpectedPanelIds()
    {
        TreeItem<NavigationPane.NavItem> inventory = findByLabel(this.tree.getRoot(), "Inventory");
        TreeItem<NavigationPane.NavItem> reportsWorkspace = findByLabel(this.tree.getRoot(), "Reports Workspace");

        assertNotNull(inventory);
        assertNotNull(reportsWorkspace);
        assertEquals(AppPanelId.INVENTORY, inventory.getValue().panelId());
        assertEquals(AppPanelId.REPORTS_WORKSPACE, reportsWorkspace.getValue().panelId());

        interact(() -> this.host.show(inventory.getValue().panelId()));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Inventory", this.host.getActiveTitle());

        interact(() -> this.host.show(reportsWorkspace.getValue().panelId()));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Reports", this.host.getActiveTitle());
    }

    private static TreeItem<NavigationPane.NavItem> findByLabel(TreeItem<NavigationPane.NavItem> root,
        String label)
    {
        if (root == null)
        {
            return null;
        }

        NavigationPane.NavItem value = root.getValue();
        if (value != null && label.equals(value.label()))
        {
            return root;
        }

        for (TreeItem<NavigationPane.NavItem> child : root.getChildren())
        {
            TreeItem<NavigationPane.NavItem> found = findByLabel(child, label);
            if (found != null)
            {
                return found;
            }
        }

        return null;
    }
}
