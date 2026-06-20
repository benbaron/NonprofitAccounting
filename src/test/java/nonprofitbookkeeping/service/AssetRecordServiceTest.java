package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import nonprofitbookkeeping.model.records.AssetItemType;
import nonprofitbookkeeping.service.AssetRecordService.AssetRegisterSaveRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetRecordServiceTest
{
    @TempDir
    Path tempDir;

    @Test
    void saveRegisterRowValidatesAmountsAndUsefulLife()
    {
        assertThrows(IllegalArgumentException.class, () -> AssetRecordService.validate(
            new AssetRegisterSaveRequest("asset-1", LocalDate.now(), "", 1,
                new BigDecimal("100.00"), new BigDecimal("150.00"), AssetItemType.DEPRECIATION_5_YEAR,
                AssetRecordService.STRAIGHT_LINE, 60)));
        assertThrows(IllegalArgumentException.class, () -> AssetRecordService.validate(
            new AssetRegisterSaveRequest("asset-1", LocalDate.now(), "", 1,
                new BigDecimal("100.00"), BigDecimal.ZERO, AssetItemType.DEPRECIATION_5_YEAR,
                AssetRecordService.STRAIGHT_LINE, 0)));
    }

    @Test
    void parseMoneyInputRejectsFormattedCurrencyDisplayStrings()
    {
        assertEquals(new BigDecimal("1234.50"), AssetRecordService.parseMoneyInput("1234.5", "cost"));
        assertThrows(IllegalArgumentException.class, () -> AssetRecordService.parseMoneyInput("$1,234.50", "cost"));
        assertThrows(IllegalArgumentException.class, () -> AssetRecordService.parseMoneyInput("1,234.50", "cost"));
    }

    @Test
    void listRegisterRowsIncludesDepreciationRunLinksAndNetBookValue() throws Exception
    {
        initDb();
        AssetRecordService service = new AssetRecordService();
        service.saveRegisterRow(new AssetRegisterSaveRequest("asset-link-1", LocalDate.of(2026, 1, 1),
            "Linkable asset", 1, new BigDecimal("1200.00"), new BigDecimal("20.00"),
            AssetItemType.DEPRECIATION_5_YEAR, AssetRecordService.STRAIGHT_LINE, 60));
        seedRunLink("asset-link-1");

        List<AssetRecordService.AssetRegisterRow> rows = service.listRegisterRows();

        AssetRecordService.AssetRegisterRow row = rows.stream()
            .filter(r -> "asset-link-1".equals(r.record().assetId()))
            .findFirst()
            .orElseThrow();
        assertEquals(new BigDecimal("1180.00"), row.netBookValue());
        assertEquals(60, row.usefulLifeMonths());
        assertTrue(row.depreciationRunLinks().get(0).contains("run-link-1"));
    }

    private void initDb() throws Exception
    {
        Database.init(tempDir.resolve("asset-record-service"));
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
        Database.get().ensureSchema();
    }

    private void seedRunLink(String assetId) throws Exception
    {
        try (Connection c = Database.get().getConnection())
        {
            try (PreparedStatement ps = c.prepareStatement("""
                MERGE INTO depreciation_run(depreciation_run_id, run_date, notes, period_start, period_end, run_status)
                KEY(depreciation_run_id) VALUES ('run-link-1', DATE '2026-01-31', 'link test', DATE '2026-01-01', DATE '2026-01-31', 'CALCULATED')
                """)) { ps.executeUpdate(); }
            try (PreparedStatement ps = c.prepareStatement("""
                MERGE INTO depreciation_record(depreciation_record_id, depreciation_run_id, asset_record_id, net_depreciation,
                  depreciation_date, period_start, period_end, sequence_in_run)
                KEY(depreciation_record_id) VALUES ('rec-link-1', 'run-link-1', ?, 20.00, DATE '2026-01-31', DATE '2026-01-01', DATE '2026-01-31', 1)
                """))
            {
                ps.setString(1, assetId);
                ps.executeUpdate();
            }
        }
    }
}
