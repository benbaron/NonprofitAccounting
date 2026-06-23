package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import nonprofitbookkeeping.ui.panels.SharedDashboardPanelFX;

class AlternateDashboardPanelTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void newShellDashboardUsesSharedDashboardSurface() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                UiSessionContext context = new UiSessionContext();
                AlternateDashboardPanel panel = new AlternateDashboardPanel(
                    context, new UiServiceProvider(context));

                assertInstanceOf(SharedDashboardPanelFX.class, panel.root());
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
