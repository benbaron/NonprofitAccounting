package nonprofitbookkeeping.persistence.records;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordSchemaServiceTest
{
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws SQLException
    {
        Database.init(this.tempDir.resolve("record-schema-db"));
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
        Database.get().ensureSchema();
        new DocumentRecordRepository().listAll();
    }

    @Test
    void returnsColumnMetadataForImportedDocumentRecordTable() throws SQLException
    {
        RecordSchemaService schemaService = new RecordSchemaService();
        List<TableColumnMetadata> columns = schemaService.columnsForTable("imported_document_record");

        assertFalse(columns.isEmpty());
        assertTrue(columns.stream().anyMatch(column -> column.columnName().equalsIgnoreCase("document_id") && column.primaryKey()));
        assertTrue(columns.stream().anyMatch(column -> column.columnName().equalsIgnoreCase("notes")));
    }
}
