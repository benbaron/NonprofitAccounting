package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import nonprofitbookkeeping.model.SaleRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SalesServiceTest {

    @TempDir
    Path tempDir;

    private SalesService service;

    @BeforeEach
    void setUp() throws Exception {
        Path dbFile = this.tempDir.resolve("sales-db");
        Database.init(dbFile);
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
        Database.get().ensureSchema();
        this.service = new SalesService();
    }

    @Test
    void saveAndLoad() throws IOException {
        File dir = this.tempDir.toFile();
        this.service.addSale(new SaleRecord("1", "2024-01-01", "Item", 2, BigDecimal.TEN, BigDecimal.ONE));
        this.service.saveSales(dir);

        SalesService load = new SalesService();
        load.loadSales(dir);
        List<SaleRecord> sales = load.listSales();
        assertEquals(1, sales.size());
        assertEquals("1", sales.get(0).getId());
    }
}
