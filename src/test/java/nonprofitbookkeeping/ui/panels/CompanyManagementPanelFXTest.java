package nonprofitbookkeeping.ui.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.service.CompanyManagementService.CompanySummary;
import nonprofitbookkeeping.service.CompanyStartupPreferenceStore.StartupBehavior;

class CompanyManagementPanelFXTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void sharedPanelProvidesSortableCompanyTableAndPreview() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try
            {
                CompanyManagementPanelFX panel =
                    new CompanyManagementPanelFX();

                SplitPane split = assertInstanceOf(SplitPane.class,
                    panel.getCenter());
                assertEquals(2, split.getItems().size());
                TableView<CompanySummary> table = assertInstanceOf(
                    TableView.class, split.getItems().get(0));
                assertEquals(5, table.getColumns().size());
                assertTrue(table.getColumns().stream()
                    .allMatch(column -> column.isSortable()));
                assertInstanceOf(TextArea.class, split.getItems().get(1));

                VBox top = assertInstanceOf(VBox.class, panel.getTop());
                assertNotNull(find(top, TextField.class));
                ComboBox<?> startup = find(top, ComboBox.class);
                assertNotNull(startup);
                assertTrue(startup.getItems().contains(
                    StartupBehavior.PRESELECT_LAST));

                assertNotNull(table.getRowFactory());
                assertNotNull(table.getOnKeyPressed());
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

    private static <T extends Node> T find(Node root, Class<T> type)
    {
        if (type.isInstance(root))
        {
            return type.cast(root);
        }
        if (root instanceof javafx.scene.Parent parent)
        {
            for (Node child : parent.getChildrenUnmodifiable())
            {
                T found = find(child, type);
                if (found != null)
                {
                    return found;
                }
            }
        }
        return null;
    }
}
