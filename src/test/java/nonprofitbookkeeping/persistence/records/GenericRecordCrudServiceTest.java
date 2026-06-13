package nonprofitbookkeeping.persistence.records;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericRecordCrudServiceTest
{
    @TempDir
    Path tempDir;

    private GenericRecordCrudService service;

    @BeforeEach
    void setUp() throws SQLException
    {
        Database.init(tempDir.resolve("generic-record-crud-db"));
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
        Database.get().ensureSchema();
        new FundRecordRepository().listAll();
        this.service = new GenericRecordCrudService(new RecordSchemaService());
    }

    @Test
    void supportsUpsertListAndDeleteForImportedFundRecord() throws SQLException
    {
        int upserted = service.upsert("imported_fund_record", Map.of(
            "fund_id", "fund-001",
            "name", "General Fund",
            "restricted", Boolean.FALSE,
            "description", "Primary operating fund",
            "extensions_json", "{}"
        ));
        assertEquals(1, upserted);

        List<Map<String, Object>> rows = service.listAll("imported_fund_record");
        assertEquals(1, rows.size());
        Map<String, Object> row = rows.get(0);
        assertTrue(row.containsKey("FUND_ID"));
        assertEquals("fund-001", row.get("FUND_ID"));

        int deleted = service.deleteByPrimaryKey("imported_fund_record", Map.of("fund_id", "fund-001"));
        assertEquals(1, deleted);
        assertTrue(service.listAll("imported_fund_record").isEmpty());
    }
}
