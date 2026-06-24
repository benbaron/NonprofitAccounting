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
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.ui.panels.SharedDashboardPanelFX;

class AlternateDashboardPanelTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void newShellDashboardUsesSharedReadOnlyResizableSurface()
        throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                UiSessionContext context = new UiSessionContext();
                AlternateDashboardPanel panel = new AlternateDashboardPanel(
                    context, new UiServiceProvider(context));

                SharedDashboardPanelFX dashboard = assertInstanceOf(
                    SharedDashboardPanelFX.class, panel.root());
                SplitPane dashboardSections = assertInstanceOf(
                    SplitPane.class, dashboard.getCenter());
                assertEquals(Orientation.VERTICAL,
                    dashboardSections.getOrientation());
                assertEquals(2, dashboardSections.getItems().size());
                assertEquals(1, dashboardSections.getDividers().size());

                ScrollPane totalsPane = assertInstanceOf(ScrollPane.class,
                    dashboardSections.getItems().get(0));
                TilePane totals = assertInstanceOf(TilePane.class,
                    totalsPane.getContent());
                assertFalse(totals.getChildren().isEmpty());
                assertTrue(totals.getChildren().stream()
                    .allMatch(VBox.class::isInstance));
                assertTrue(totals.getChildren().stream()
                    .noneMatch(Button.class::isInstance));

                SplitPane tables = assertInstanceOf(SplitPane.class,
                    dashboardSections.getItems().get(1));
                assertEquals(Orientation.VERTICAL, tables.getOrientation());
            }
            catch (Throwable throwable)
            {
                error[0] = throwable;
            }
            finally
            {
                latch.countDown();
            }
        });

        if (!latch.await(20, TimeUnit.SECONDS))
        {
            throw new IllegalStateException("Timed out waiting for FX task");
        }
        if (error[0] != null)
        {
            throw new AssertionError(error[0]);
        }
    }
}
