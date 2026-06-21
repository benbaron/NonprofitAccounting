package nonprofitbookkeeping.ui.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

class JournalPanelFXSmokeTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void journalPanelBuildsTransactionBlockColumn() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                JournalPanelFX panel = new JournalPanelFX();
                TableView<?> table =
                    assertInstanceOf(TableView.class, panel.getCenter());
                assertEquals(1, table.getColumns().size());
                TableColumn<?, ?> blockColumn = table.getColumns().get(0);
                assertInstanceOf(GridPane.class, blockColumn.getGraphic());
                assertEquals(SelectionMode.MULTIPLE,
                    table.getSelectionModel().getSelectionMode());
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
