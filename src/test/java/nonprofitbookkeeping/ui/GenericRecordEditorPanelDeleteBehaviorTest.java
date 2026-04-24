package nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.persistence.records.GenericRecordCrudService;
import nonprofitbookkeeping.persistence.records.RecordSchemaService;
import nonprofitbookkeeping.persistence.records.TableColumnMetadata;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericRecordEditorPanelDeleteBehaviorTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void allowsDeletingPendingRowEvenWhenRowDataIsInvalid() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try
            {
                FakeSchemaService schemaService = new FakeSchemaService();
                RecordingCrudService crudService = new RecordingCrudService(schemaService);
                GenericRecordEditorPanel panel = new GenericRecordEditorPanel(
                    "Asset Register",
                    "imported_asset_record",
                    "asset_id",
                    () -> "asset-added",
                    Set.of(),
                    schemaService,
                    crudService
                );

                BorderPane pane = (BorderPane) panel.root();
                ScrollPane scrollPane = (ScrollPane) pane.getCenter();
                @SuppressWarnings("unchecked")
                TableView<Map<String, Object>> table = (TableView<Map<String, Object>>) scrollPane.getContent();

                HBox actions = (HBox) ((VBox) pane.getTop()).getChildren().get(1);
                Button add = (Button) actions.getChildren().get(0);
                Button delete = (Button) actions.getChildren().get(1);

                int initialRows = table.getItems().size();
                add.fire();

                Map<String, Object> added = table.getSelectionModel().getSelectedItem();
                assertNotNull(added);
                added.put("asset_id", "");
                added.put("date_acquired", "not-a-date");

                delete.fire();

                assertEquals(initialRows, table.getItems().size());
                assertEquals(0, crudService.deleteCalls);
            }
            catch (Throwable t)
            {
                error.set(t);
            }
            finally
            {
                latch.countDown();
            }
        });

        assertTrue(latch.await(20, TimeUnit.SECONDS), "Timed out waiting for JavaFX assertions.");
        if (error.get() != null)
        {
            throw new AssertionError("Delete behavior assertions failed.", error.get());
        }
    }

    @Test
    void wrapsTableInScrollPaneWithAlwaysVisibleVerticalBar() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try
            {
                GenericRecordEditorPanel panel = new GenericRecordEditorPanel(
                    "Asset Register",
                    "imported_asset_record",
                    "asset_id",
                    () -> "asset-added",
                    Set.of(),
                    new FakeSchemaService(),
                    new RecordingCrudService(new FakeSchemaService())
                );

                BorderPane pane = (BorderPane) panel.root();
                assertTrue(pane.getCenter() instanceof ScrollPane);
                ScrollPane scrollPane = (ScrollPane) pane.getCenter();
                assertEquals(ScrollPane.ScrollBarPolicy.ALWAYS, scrollPane.getVbarPolicy());
            }
            catch (Throwable t)
            {
                error.set(t);
            }
            finally
            {
                latch.countDown();
            }
        });

        assertTrue(latch.await(20, TimeUnit.SECONDS), "Timed out waiting for JavaFX assertions.");
        if (error.get() != null)
        {
            throw new AssertionError("Scroll pane assertions failed.", error.get());
        }
    }

    private static final class FakeSchemaService extends RecordSchemaService
    {
        @Override
        public List<TableColumnMetadata> columnsForTable(String tableName)
        {
            return List.of(
                new TableColumnMetadata(tableName, "asset_id", Types.VARCHAR, "VARCHAR", 255, 0, false, true, 1),
                new TableColumnMetadata(tableName, "date_acquired", Types.DATE, "DATE", 0, 0, false, false, 2),
                new TableColumnMetadata(tableName, "description", Types.VARCHAR, "VARCHAR", 255, 0, true, false, 3)
            );
        }
    }

    private static final class RecordingCrudService extends GenericRecordCrudService
    {
        private int deleteCalls;

        private RecordingCrudService(RecordSchemaService schemaService)
        {
            super(schemaService);
        }

        @Override
        public List<Map<String, Object>> listAll(String tableName)
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("asset_id", "asset-1");
            row.put("date_acquired", "2026-01-01");
            row.put("description", "Desk");
            return List.of(row);
        }

        @Override
        public int deleteByPrimaryKey(String tableName, Map<String, Object> primaryKey)
        {
            deleteCalls++;
            return 1;
        }
    }
}
