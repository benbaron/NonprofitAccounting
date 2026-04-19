package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

class WorkspacePanelInteractionTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void assetsRegisterSupportsAddAndSaveFeedback() throws Exception
    {
        runOnFxThread(() -> {
            AssetsRegisterPanel panel = new AssetsRegisterPanel();
            BorderPane root = (BorderPane) panel.root();

            TableView<?> table = assertInstanceOf(TableView.class, root.getCenter());
            int initial = table.getItems().size();

            VBox top = assertInstanceOf(VBox.class, root.getTop());
            HBox actions = assertInstanceOf(HBox.class, top.getChildren().get(1));
            Button add = assertInstanceOf(Button.class, actions.getChildren().get(0));
            add.fire();

            assertEquals(initial + 1, table.getItems().size());
            panel.onSave();

            VBox bottom = assertInstanceOf(VBox.class, root.getBottom());
            Label status = assertInstanceOf(Label.class, bottom.getChildren().get(1));
            assertTrue(status.getText().startsWith("Saved"));
        });
    }

    @Test
    void budgetEditorSupportsAddAndSaveFeedback() throws Exception
    {
        runOnFxThread(() -> {
            BudgetEditorPanel panel = new BudgetEditorPanel();
            BorderPane root = (BorderPane) panel.root();

            TableView<?> table = assertInstanceOf(TableView.class, root.getCenter());
            int initial = table.getItems().size();

            VBox top = assertInstanceOf(VBox.class, root.getTop());
            HBox actions = assertInstanceOf(HBox.class, top.getChildren().get(1));
            Button add = assertInstanceOf(Button.class, actions.getChildren().get(0));
            add.fire();

            assertEquals(initial + 1, table.getItems().size());
            panel.onSave();

            VBox bottom = assertInstanceOf(VBox.class, root.getBottom());
            Label status = assertInstanceOf(Label.class, bottom.getChildren().get(1));
            assertTrue(status.getText().startsWith("Saved"));
        });
    }

    @Test
    void budgetVsActualBuildsGroupedRowsAndExpandCollapseWorks() throws Exception
    {
        runOnFxThread(() -> {
            BudgetVsActualPanel panel = new BudgetVsActualPanel();
            BorderPane root = (BorderPane) panel.root();

            TreeTableView<?> tree = assertInstanceOf(TreeTableView.class, root.getCenter());
            assertEquals(2, tree.getRoot().getChildren().size());

            VBox top = assertInstanceOf(VBox.class, root.getTop());
            HBox actions = assertInstanceOf(HBox.class, top.getChildren().get(1));
            Button collapseAll = assertInstanceOf(Button.class, actions.getChildren().get(2));
            Button expandAll = assertInstanceOf(Button.class, actions.getChildren().get(1));

            collapseAll.fire();
            assertFalse(tree.getRoot().getChildren().get(0).isExpanded());
            expandAll.fire();
            assertTrue(tree.getRoot().getChildren().get(0).isExpanded());
        });
    }

    @Test
    void ledgerRegisterRendersRowsAndReadOnlyActions() throws Exception
    {
        runOnFxThread(() -> {
            LedgerRegisterPanel panel = new LedgerRegisterPanel();
            BorderPane root = (BorderPane) panel.root();

            TableView<?> table = assertInstanceOf(TableView.class, root.getCenter());
            assertEquals(2, table.getItems().size());
            assertEquals(5, table.getColumns().size());

            VBox top = assertInstanceOf(VBox.class, root.getTop());
            HBox actions = assertInstanceOf(HBox.class, top.getChildren().get(2));
            assertEquals(1, actions.getChildren().size());
        });
    }

    private static void runOnFxThread(FxRunnable runnable) throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                runnable.run();
            }
            catch (Throwable t)
            {
                error[0] = t;
            }
            finally
            {
                latch.countDown();
            }
        });

        if (!latch.await(20, TimeUnit.SECONDS))
        {
            throw new IllegalStateException("Timed out waiting for JavaFX work");
        }
        if (error[0] != null)
        {
            throw new AssertionError(error[0]);
        }
    }

    @FunctionalInterface
    private interface FxRunnable
    {
        void run() throws Exception;
    }
}
