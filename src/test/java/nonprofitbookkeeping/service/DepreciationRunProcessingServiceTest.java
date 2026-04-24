package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepreciationRunProcessingServiceTest
{
    @TempDir
    Path tempDir;

    @Test
    void calculateRun_updatesAccumulatedAndCreatesPreviewLines() throws Exception
    {
        initDb();
        seedAsset("asset-1", new BigDecimal("1200.00"), BigDecimal.ZERO);
        DepreciationRunLifecycleService lifecycle = new DepreciationRunLifecycleService();
        DepreciationRunProcessingService svc = new DepreciationRunProcessingService();
        String runId = "run-calc-001";
        lifecycle.createDraftRun(runId, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "jan close");

        List<DepreciationRunProcessingService.PreviewLine> lines =
            svc.calculateAndMarkCalculated(runId, "tester");

        assertEquals(1, lines.size());
        assertEquals("asset-1", lines.get(0).assetId());

        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT accumulated_depreciation FROM imported_asset_record WHERE asset_id = ?"))
        {
            ps.setString(1, "asset-1");
            try (ResultSet rs = ps.executeQuery())
            {
                assertTrue(rs.next());
                assertEquals(0, new BigDecimal("20.00").compareTo(rs.getBigDecimal(1)));
            }
        }

        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT run_status FROM depreciation_run WHERE depreciation_run_id = ?"))
        {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery())
            {
                assertTrue(rs.next());
                assertEquals("CALCULATED", rs.getString(1));
            }
        }
    }

    @Test
    void unlockAndDeleteRun_rollsBackAccumulated() throws Exception
    {
        initDb();
        seedAsset("asset-2", new BigDecimal("600.00"), new BigDecimal("50.00"));
        DepreciationRunLifecycleService lifecycle = new DepreciationRunLifecycleService();
        DepreciationRunProcessingService svc = new DepreciationRunProcessingService();
        String runId = "run-delete-001";
        lifecycle.createDraftRun(runId, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), "feb close");
        svc.calculateAndMarkCalculated(runId, "tester");
        lifecycle.lockRun(runId, "reviewer", "locked");

        svc.unlockRun(runId, "reviewer", "unlock for corrections");
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT is_locked FROM depreciation_run WHERE depreciation_run_id = ?"))
        {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery())
            {
                assertTrue(rs.next());
                assertFalse(rs.getBoolean(1));
            }
        }

        svc.deleteRun(runId, DepreciationRunProcessingService.DeleteMode.DELETE_LINKED_JOURNALS, "tester");
        try (Connection c = Database.get().getConnection();
             PreparedStatement runPs = c.prepareStatement(
                 "SELECT COUNT(*) FROM depreciation_run WHERE depreciation_run_id = ?");
             PreparedStatement accPs = c.prepareStatement(
                 "SELECT accumulated_depreciation FROM imported_asset_record WHERE asset_id = ?"))
        {
            runPs.setString(1, runId);
            try (ResultSet rs = runPs.executeQuery())
            {
                rs.next();
                assertEquals(0, rs.getInt(1));
            }
            accPs.setString(1, "asset-2");
            try (ResultSet rs = accPs.executeQuery())
            {
                assertTrue(rs.next());
                assertEquals(0, new BigDecimal("50.00").compareTo(rs.getBigDecimal(1)));
            }
        }
    }

    private void seedAsset(String assetId, BigDecimal approxValue, BigDecimal accumulated) throws Exception
    {
        ensureImportedAssetTable();
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 INSERT INTO imported_asset_record(asset_id, approx_value_total, accumulated_depreciation)
                 VALUES (?, ?, ?)
             """))
        {
            ps.setString(1, assetId);
            ps.setBigDecimal(2, approxValue);
            ps.setBigDecimal(3, accumulated);
            ps.executeUpdate();
        }
    }

    private void initDb() throws Exception
    {
        Path dbPath = tempDir.resolve("depreciation-run-processing");
        Database.init(dbPath);
        Database.get().ensureSchema();
    }

    private void ensureImportedAssetTable() throws Exception
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 CREATE TABLE IF NOT EXISTS imported_asset_record (
                     asset_id VARCHAR(255) PRIMARY KEY,
                     approx_value_total DECIMAL(19,2),
                     accumulated_depreciation DECIMAL(19,2)
                 )
             """))
        {
            ps.execute();
        }
    }
}
