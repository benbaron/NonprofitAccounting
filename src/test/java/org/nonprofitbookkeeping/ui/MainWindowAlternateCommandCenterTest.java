package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
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
    void scheduledReportEntriesArePrependedInPreferences() throws Exception
    {
        MainWindowAlternate window = new MainWindowAlternate();

        Field prefsField = MainWindowAlternate.class.getDeclaredField("alternatePreferences");
        prefsField.setAccessible(true);
        Preferences prefs = (Preferences) prefsField.get(window);

        Field keyField = MainWindowAlternate.class.getDeclaredField("SCHEDULED_REPORTS_KEY");
        keyField.setAccessible(true);
        String key = (String) keyField.get(null);

        prefs.remove(key);
        Method saveScheduledReport = MainWindowAlternate.class.getDeclaredMethod("saveScheduledReport", String.class);
        saveScheduledReport.setAccessible(true);

        saveScheduledReport.invoke(window, "Income Statement|Monthly|2026-05-09");
        saveScheduledReport.invoke(window, "Balance Sheet|Weekly|2026-05-10");

        assertEquals("Balance Sheet|Weekly|2026-05-10\nIncome Statement|Monthly|2026-05-09",
            prefs.get(key, ""));
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
                Method exportAction = MainWindowAlternate.class
                    .getDeclaredMethod("openReportsWorkspaceWithExportHint");
                exportAction.setAccessible(true);
                exportAction.invoke(window);

                Field statusField = MainWindowAlternate.class.getDeclaredField("alternateStatus");
                statusField.setAccessible(true);
                javafx.scene.control.TextArea status = (javafx.scene.control.TextArea) statusField.get(window);
                assertTrue(status.getText().contains("Export action requires an active window"));
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
