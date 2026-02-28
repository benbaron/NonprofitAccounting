package org.nonprofitbookkeeping.map;

import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import org.nonprofitbookkeeping.ui.AppPanelId;
import org.nonprofitbookkeeping.ui.MainWindow;
import org.nonprofitbookkeeping.ui.NavigationPane;
import org.nonprofitbookkeeping.ui.PanelHost;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapDrivenRoutingTest
{
    @BeforeAll
    static void initFx()
    {
        CountDownLatch latch = new CountDownLatch(1);

        try
        {
            Platform.startup(latch::countDown);
        }
        catch (IllegalStateException alreadyStarted)
        {
            latch.countDown();
        }

        await(latch);
    }

    @Test
    void panelHostRoutesInventoryAndReportsWorkspace()
    {
        runOnFxAndWait(() -> {
            PanelHost host = new PanelHost();

            host.show(AppPanelId.INVENTORY);
            assertEquals("Inventory", host.getActiveTitle());

            host.show(AppPanelId.REPORTS_WORKSPACE);
            assertEquals("Reports Library", host.getActiveTitle());
        });
    }

    @Test
    void navigationContainsMapDrivenNodes()
    {
        runOnFxAndWait(() -> {
            NavigationPane nav = new NavigationPane(id -> { }, (title, body) -> { });
            @SuppressWarnings("unchecked")
            TreeView<NavigationPane.NavItem> tree = (TreeView<NavigationPane.NavItem>) nav.getChildren().get(0);

            List<String> labels = new ArrayList<>();
            collectLabels(tree.getRoot(), labels);

            assertTrue(labels.contains("Dashboard"));
            assertTrue(labels.contains("Inventory"));
            assertTrue(labels.contains("Reports Workspace"));
        });
    }

    @Test
    void mainWindowStartsOnDashboardAndRunMenuRoutesToMapTargets()
    {
        runOnFxAndWait(() -> {
            MainWindow window = new MainWindow();
            PanelHost host = (PanelHost) window.getCenter();
            assertEquals("Dashboard", host.getActiveTitle());

            VBox top = (VBox) window.getTop();
            MenuBar menuBar = (MenuBar) top.getChildren().get(0);
            Menu runMenu = menuBar.getMenus().stream()
                .filter(m -> "Run".equals(m.getText()))
                .findFirst()
                .orElseThrow();

            fireMenu(runMenu, "Inventory & Depreciation");
            assertEquals("Inventory", host.getActiveTitle());

            fireMenu(runMenu, "Reports Workspace");
            assertEquals("Reports Library", host.getActiveTitle());
        });
    }

    private static void fireMenu(Menu menu, String itemText)
    {
        MenuItem item = menu.getItems().stream()
            .filter(i -> itemText.equals(i.getText()))
            .findFirst()
            .orElse(null);

        assertNotNull(item, "Expected menu item not found: " + itemText);
        item.fire();
    }

    private static void collectLabels(TreeItem<NavigationPane.NavItem> item, List<String> labels)
    {
        if (item == null)
        {
            return;
        }

        NavigationPane.NavItem value = item.getValue();
        if (value != null && value.label() != null)
        {
            labels.add(value.label());
        }

        for (TreeItem<NavigationPane.NavItem> child : item.getChildren())
        {
            collectLabels(child, labels);
        }
    }

    private static void runOnFxAndWait(Runnable work)
    {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try
            {
                work.run();
            }
            catch (Throwable t)
            {
                error.set(t);
            }
            finally
            {
                latch.countDown();
            }
        });

        await(latch);

        if (error.get() != null)
        {
            throw new AssertionError(error.get());
        }
    }

    private static void await(CountDownLatch latch)
    {
        try
        {
            assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out waiting for JavaFX thread.");
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for JavaFX thread.", e);
        }
    }
}
