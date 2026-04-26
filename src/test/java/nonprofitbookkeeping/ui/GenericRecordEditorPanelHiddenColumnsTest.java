package nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import nonprofitbookkeeping.persistence.records.GenericRecordCrudService;
import nonprofitbookkeeping.persistence.records.RecordSchemaService;
import nonprofitbookkeeping.persistence.records.TableColumnMetadata;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericRecordEditorPanelHiddenColumnsTest
{
    @BeforeAll
    static void initToolkit()
    {
        new JFXPanel();
    }

    @Test
    void hidesConfiguredColumnsButStillSavesHiddenValues() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try
            {
                FakeSchemaService schemaService = new FakeSchemaService();
                FakeCrudService crudService = new FakeCrudService(schemaService);
                GenericRecordEditorPanel panel = new GenericRecordEditorPanel(
                    "Asset Register",
                    "imported_asset_record",
                    "asset_id",
                    () -> "asset-1",
                    Set.of("extensions_json"),
                    schemaService,
                    crudService
                );

                if (!(panel.root() instanceof javafx.scene.layout.BorderPane pane)
                    || !(pane.getCenter() instanceof ScrollPane container)
                    || !(container.getContent() instanceof TableView<?> centerTable))
                {
                    throw new AssertionError("Expected generic editor center to be a ScrollPane wrapping a TableView.");
                }

                List<String> headers = centerTable.getColumns().stream().map(TableColumn::getText).toList();
                assertTrue(headers.contains("Asset Id *"));
                assertTrue(headers.contains("Description"));
                assertFalse(headers.contains("Extensions Json"));
                assertFalse(headers.contains("Extensions Json *"));

                panel.onSave();

                assertNotNull(crudService.lastUpsertValues);
                assertEquals("{}", crudService.lastUpsertValues.get("extensions_json"));
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
            throw new AssertionError("Hidden column behavior assertions failed.", error.get());
        }
    }

    private static final class FakeSchemaService extends RecordSchemaService
    {
        @Override
        public List<TableColumnMetadata> columnsForTable(String tableName)
        {
            return List.of(
                new TableColumnMetadata(tableName, "asset_id", Types.VARCHAR, "VARCHAR", 255, 0, false, true, 1),
                new TableColumnMetadata(tableName, "description", Types.VARCHAR, "VARCHAR", 255, 0, true, false, 2),
                new TableColumnMetadata(tableName, "extensions_json", Types.CLOB, "CLOB", 0, 0, true, false, 3)
            );
        }
    }

    private static final class FakeCrudService extends GenericRecordCrudService
    {
        private Map<String, Object> lastUpsertValues;

        private FakeCrudService(RecordSchemaService schemaService)
        {
            super(schemaService);
        }

        @Override
        public List<Map<String, Object>> listAll(String tableName)
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("asset_id", "asset-1");
            row.put("description", "Desk");
            row.put("extensions_json", "{}");
            return List.of(row);
        }

        @Override
        public int upsert(String tableName, Map<String, Object> rowValues) throws SQLException
        {
            this.lastUpsertValues = new LinkedHashMap<>(rowValues);
            return 1;
        }
    }
}
