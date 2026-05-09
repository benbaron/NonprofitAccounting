package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

class MainWindowAlternateCommandCenterTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void commandCenterIncludesClassicBankingWorkflowLabels() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindowAlternate window = new MainWindowAlternate();

                Method buildCommandCenterPane = MainWindowAlternate.class
                    .getDeclaredMethod("buildCommandCenterPane");
                buildCommandCenterPane.setAccessible(true);
                VBox pane = (VBox) buildCommandCenterPane.invoke(window);

                List<String> buttonLabels = pane.getChildren().stream()
                    .filter(VBox.class::isInstance)
                    .flatMap(group -> ((VBox) group).getChildren().stream())
                    .filter(Button.class::isInstance)
                    .map(node -> ((Button) node).getText())
                    .toList();

                assertTrue(buttonLabels.contains("Reconcile Accounts"));
                assertTrue(buttonLabels.contains("Undeposited Funds"));
                assertTrue(buttonLabels.contains("Documents & Attachments"));
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

        assertTrue(latch.await(20, TimeUnit.SECONDS));
        if (error[0] != null)
        {
            throw new AssertionError("MainWindowAlternate command center banking labels test failed", error[0]);
        }
    }

    @Test
    void bankingActionFailuresSurfaceInspectorMessages() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindowAlternate.BankingPanelFactory failingFactory = new MainWindowAlternate.BankingPanelFactory()
                {
                    public Node createReconcilePanel()
                    {
                        throw new IllegalStateException("reconcile exploded");
                    }

                    public Node createUndepositedFundsPanel()
                    {
                        throw new IllegalStateException("undeposited exploded");
                    }

                    public Node createDocumentsPanel()
                    {
                        throw new IllegalStateException("documents exploded");
                    }
                };

                MainWindowAlternate window = new MainWindowAlternate(failingFactory);
                window.testOpenReconcileAccountsDirect();
                assertTrue(window.testAlternateStatusText().contains("Reconcile Accounts failed: reconcile exploded"));

                window.testOpenUndepositedFundsDirect();
                assertTrue(window.testAlternateStatusText().contains("Undeposited Funds failed: undeposited exploded"));

                window.testOpenDocumentsDirect();
                assertTrue(window.testAlternateStatusText().contains("Documents & Attachments failed: documents exploded"));
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

        assertTrue(latch.await(20, TimeUnit.SECONDS));
        if (error[0] != null)
        {
            throw new AssertionError("MainWindowAlternate banking failure feedback test failed", error[0]);
        }
    }

    @Test
    void scheduledReportEntriesArePrependedInPreferences()
    {
        MainWindowAlternate window = new MainWindowAlternate();
        window.testClearScheduledReports();
        window.testSaveScheduledReport("Income Statement|Monthly|2026-05-09");
        window.testSaveScheduledReport("Balance Sheet|Weekly|2026-05-10");

        assertEquals("Balance Sheet|Weekly|2026-05-10\nIncome Statement|Monthly|2026-05-09",
            window.testScheduledReportsValue());
    }
}
