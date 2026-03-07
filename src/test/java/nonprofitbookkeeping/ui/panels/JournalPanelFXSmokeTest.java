package nonprofitbookkeeping.ui.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TableView;

class JournalPanelFXSmokeTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void journalPanelBuildsExpectedTransactionColumns() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                JournalPanelFX panel = new JournalPanelFX();
                TableView<?> table = assertInstanceOf(TableView.class, panel.getCenter());
                assertEquals(11, table.getColumns().size());
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
            throw new IllegalStateException("Timed out waiting for FX task");
        }
        if (error[0] != null)
        {
            throw new AssertionError(error[0]);
        }
    }
}
