package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

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
                var buttonLabels = window.commandCenterActionLabelsForTest();

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
    void scheduledReportEntriesArePrependedInPreferences() throws Exception
    {
        MainWindowAlternate window = new MainWindowAlternate();
        window.clearScheduledReportsForTest();
        window.saveScheduledReportForTest("Income Statement|Monthly|2026-05-09");
        window.saveScheduledReportForTest("Balance Sheet|Weekly|2026-05-10");

        assertEquals("Balance Sheet|Weekly|2026-05-10\nIncome Statement|Monthly|2026-05-09",
            window.scheduledReportsSnapshotForTest());
    }

    @Test
    void exportActionReportsGuidanceWhenNoOwningStageIsAvailable() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                MainWindowAlternate window = new MainWindowAlternate();
                window.triggerReportExportActionForTest();
                assertTrue(window.alternateStatusTextForTest().contains("Export action requires an active window"));
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
            throw new AssertionError("MainWindowAlternate export guidance test failed", error[0]);
        }
    }
}
