package nonprofitbookkeeping.ui.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import nonprofitbookkeeping.model.AccountingTransaction;

class JournalPanelFXSmokeTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void journalPanelBuildsSortableTransactionGroups() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                JournalPanelFX panel = new JournalPanelFX();
                TableView<AccountingTransaction> table =
                    assertInstanceOf(TableView.class, panel.getCenter());

                assertEquals(1, table.getColumns().size());
                TableColumn<?, ?> blockColumn = table.getColumns().get(0);
                GridPane header = assertInstanceOf(GridPane.class,
                    blockColumn.getGraphic());
                assertEquals(8, header.getChildren().size());
                assertEquals(SelectionMode.MULTIPLE,
                    table.getSelectionModel().getSelectionMode());
                assertEquals(-1.0, table.getFixedCellSize());
                assertTrue(blockColumn.getMinWidth() > 1600,
                    "The journal should overflow narrow windows and expose a horizontal scrollbar");

                AccountingTransaction later = transaction(2, "2026-02-01");
                AccountingTransaction earlier = transaction(1, "2026-01-01");
                table.getItems().setAll(later, earlier);

                Label dateHeader = header.getChildren().stream()
                    .filter(Label.class::isInstance)
                    .map(Label.class::cast)
                    .filter(label -> label.getText().startsWith("Date"))
                    .findFirst()
                    .orElseThrow();

                dateHeader.getOnMouseClicked().handle(null);
                assertEquals(1, table.getItems().get(0).getId());
                assertTrue(dateHeader.getText().contains("▲"));

                dateHeader.getOnMouseClicked().handle(null);
                assertEquals(2, table.getItems().get(0).getId());
                assertTrue(dateHeader.getText().contains("▼"));
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

    private static AccountingTransaction transaction(int id, String date)
    {
        AccountingTransaction transaction = new AccountingTransaction();
        transaction.setId(id);
        transaction.setDate(date);
        return transaction;
    }
}
