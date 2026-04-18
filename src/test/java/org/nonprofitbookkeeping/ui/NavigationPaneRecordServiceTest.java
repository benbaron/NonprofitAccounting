package org.nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import nonprofitbookkeeping.ui.RecordServicePanelRegistry;
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

class NavigationPaneRecordServiceTest
{
    @BeforeAll
    static void initFx()
    {
        new JFXPanel();
    }

    @Test
    void registryEntriesAreDiscoverableInNavigationModel() throws Exception
    {
        AtomicReference<TreeView<NavigationPane.NavItem>> treeRef = new AtomicReference<>();
        runFx(() -> {
            NavigationPane nav = new NavigationPane(id -> {
            }, (title, body) -> {
            }, binding -> {
            });
            @SuppressWarnings("unchecked")
            TreeView<NavigationPane.NavItem> tree = (TreeView<NavigationPane.NavItem>) nav.getChildren().get(0);
            treeRef.set(tree);
        });

        TreeView<NavigationPane.NavItem> tree = treeRef.get();
        assertNotNull(tree);
        TreeItem<NavigationPane.NavItem> recordServices = findByLabel(tree.getRoot(), "Record Services");
        assertNotNull(recordServices);
        assertEquals(RecordServicePanelRegistry.all().size(), recordServices.getChildren().size());

        TreeItem<NavigationPane.NavItem> budgetService =
            findByLabel(tree.getRoot(), "Budget · Budget Records (Workspace)");
        TreeItem<NavigationPane.NavItem> documentService =
            findByLabel(tree.getRoot(), "Admin · Document Records (Proposed)");

        assertNotNull(budgetService);
        assertNotNull(documentService);
        assertTrue(budgetService.getValue().onOpen() != null);
        assertTrue(documentService.getValue().onOpen() != null);
    }

    @Test
    void recordServiceCallbacksPreserveProposedVsExistingBehavior() throws Exception
    {
        List<RecordServicePanelRegistry.PanelBinding> opened = new ArrayList<>();
        AtomicReference<TreeView<NavigationPane.NavItem>> treeRef = new AtomicReference<>();

        runFx(() -> {
            NavigationPane nav = new NavigationPane(id -> {
            }, (title, body) -> {
            }, opened::add);
            @SuppressWarnings("unchecked")
            TreeView<NavigationPane.NavItem> tree = (TreeView<NavigationPane.NavItem>) nav.getChildren().get(0);
            treeRef.set(tree);
        });

        TreeItem<NavigationPane.NavItem> budgetService =
            findByLabel(treeRef.get().getRoot(), "Budget · Budget Records (Workspace)");
        TreeItem<NavigationPane.NavItem> documentService =
            findByLabel(treeRef.get().getRoot(), "Admin · Document Records (Proposed)");

        runFx(() -> {
            budgetService.getValue().onOpen().run();
            documentService.getValue().onOpen().run();
        });

        assertEquals(2, opened.size());
        assertTrue(opened.stream().anyMatch(binding ->
            "Budget Records".equals(binding.displayName()) && !binding.proposedPanel()));
        assertTrue(opened.stream().anyMatch(binding ->
            "Document Records".equals(binding.displayName()) && binding.proposedPanel()));
    }

    private static TreeItem<NavigationPane.NavItem> findByLabel(TreeItem<NavigationPane.NavItem> root, String label)
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

    private static void runFx(Runnable runnable) throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try
            {
                runnable.run();
            }
            catch (Throwable throwable)
            {
                error.set(throwable);
            }
            finally
            {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out waiting for JavaFX thread");
        if (error.get() != null)
        {
            throw new AssertionError("JavaFX execution failed", error.get());
        }
    }
}
