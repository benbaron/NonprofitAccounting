package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

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
    void commandCenterIncludesClassicBankingWorkflowLabelsInClassicOrder() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindowAlternate window = new MainWindowAlternate();
                var buttonLabels = window.commandCenterActionLabelsForTest();
                var bankingLabels = new ArrayList<String>();
                for (String label : buttonLabels)
                {
                    if (label.equals("Reconcile Accounts")
                        || label.equals("Undeposited Funds")
                        || label.equals("Documents & Attachments"))
                    {
                        bankingLabels.add(label);
                    }
                }

                assertEquals(List.of("Reconcile Accounts", "Undeposited Funds", "Documents & Attachments"), bankingLabels);
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
    void commandCenterExposesCompleteToolbarActions() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindowAlternate window = new MainWindowAlternate();
                List<String> buttonLabels = window.commandCenterActionLabelsForTest();

                assertTrue(buttonLabels.contains("New"));
                assertTrue(buttonLabels.contains("Save"));
                assertTrue(buttonLabels.contains("Delete"));
                assertTrue(buttonLabels.contains("Cancel"));
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
            throw new AssertionError("MainWindowAlternate complete toolbar action test failed", error[0]);
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
                assertTrue(window.testAlternateStatusText().contains("Native reconciliation workspace opened"));

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
            throw new AssertionError("MainWindowAlternate banking feedback test failed", error[0]);
        }
    }

    @Test
    void scheduledReportEntriesArePrependedInPreferences()
    {
        MainWindowAlternate writer = new MainWindowAlternate();
        writer.testClearScheduledReports();
        writer.testSaveScheduledReport("Income Statement|Monthly|2026-05-09");
        writer.testSaveScheduledReport("Balance Sheet|Weekly|2026-05-10");

        MainWindowAlternate reader = new MainWindowAlternate();

        assertEquals("Balance Sheet|Weekly|2026-05-10\nIncome Statement|Monthly|2026-05-09",
            reader.testScheduledReportsValue());

        reader.testClearScheduledReports();
    }

    @Test
    void reportsFundsInventoryNavigationRendersSubpanelButtons() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindowAlternate window = new MainWindowAlternate();
                window.testOpenPanel(AppPanelId.REPORTS_WORKSPACE);
                List<String> reportsLabels = window.testNavigationButtonLabels();
                assertTrue(reportsLabels.contains("⌂  Reports"));

                window.testOpenPanel(AppPanelId.FUNDS);
                List<String> fundsLabels = window.testNavigationButtonLabels();
                assertTrue(fundsLabels.contains("⌂  Funds"));

                window.testOpenPanel(AppPanelId.INVENTORY);
                List<String> inventoryLabels = window.testNavigationButtonLabels();
                assertTrue(inventoryLabels.contains("⌂  Inventory"));
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
            throw new AssertionError("MainWindowAlternate reports/funds/inventory nav rendering test failed", error[0]);
        }
    }

    @Test
    void headerContextRemainsConsistentAcrossNativePhaseThreePanels() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindowAlternate window = new MainWindowAlternate();
                window.testOpenPanel(AppPanelId.BUDGET_EDITOR);
                assertEquals("Budget", window.testHeaderTitle());
                assertEquals("No company open", window.testHeaderSubtitle());

                window.testOpenPanel(AppPanelId.SCHEDULES);
                assertEquals("Schedules", window.testHeaderTitle());
                assertEquals("No company open", window.testHeaderSubtitle());

                window.testOpenPanel(AppPanelId.DASHBOARD);
                assertEquals("Dashboard", window.testHeaderTitle());
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
            throw new AssertionError("MainWindowAlternate header continuity regression test failed", error[0]);
        }
    }

    @Test
    void iconRailUsesStyleClassesTooltipsAndAccessibleLabels() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindowAlternate window = new MainWindowAlternate();

                assertEquals(List.of("Profile", "Dashboard", "Search", "Command Center", "Settings"),
                    window.testIconRailAccessibleLabels());
                assertEquals(List.of("Profile", "Dashboard", "Search", "Command Center", "Settings"),
                    window.testIconRailTooltipLabels());
                assertTrue(window.testIconRailButtonStyleClasses().contains("alternate-icon-rail-button"));
                assertTrue(window.testShellSurfaceStyleClasses().contains("alternate-shell-root"));
                assertTrue(window.testShellSurfaceStyleClasses().contains("alternate-workspace-surface"));
                assertTrue(window.testShellSurfaceStyleClasses().contains("alternate-shell-title"));
                assertTrue(window.testShellSurfaceStyleClasses().contains("alternate-shell-subtitle"));
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
            throw new AssertionError("MainWindowAlternate icon rail styling/accessibility test failed", error[0]);
        }
    }

}
