package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepreciationRunProcessingServiceTest
{
    @TempDir
    Path tempDir;

    @Test
    void calculateRun_updatesAccumulatedAndCreatesPreviewLines() throws Exception
    {
        initDb();
        seedPostingAccounts();
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
        seedPostingAccounts();
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

    @Test
    void postAndReverseRun_persistsImmutableLineage() throws Exception
    {
        initDb();
        seedPostingAccounts();
        seedAsset("asset-3", new BigDecimal("1200.00"), BigDecimal.ZERO);
        DepreciationRunLifecycleService lifecycle = new DepreciationRunLifecycleService();
        DepreciationRunProcessingService svc = new DepreciationRunProcessingService();
        String runId = "run-post-reverse-001";
        lifecycle.createDraftRun(runId, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), "march close");
        svc.calculateAndMarkCalculated(runId, "tester");
        assertEquals(1, svc.postRun(runId, "poster"));
        assertEquals(1, svc.reversePostedRun(runId, "poster", "void"));
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT posted_journal_txn_id, reversal_journal_txn_id FROM depreciation_record WHERE depreciation_run_id = ?"))
        {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery())
            {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) > 0);
                assertTrue(rs.getInt(2) > 0);
            }
        }
    }

    @Test
    void postRun_missingPostingRoleCode_failsFast() throws Exception
    {
        initDb();
        seedPostingAccounts();
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE account SET account_code = NULL WHERE account_number IN ('6100','1700')"))
        {
            ps.executeUpdate();
        }
        seedAsset("asset-4", new BigDecimal("1200.00"), BigDecimal.ZERO);
        DepreciationRunLifecycleService lifecycle = new DepreciationRunLifecycleService();
        DepreciationRunProcessingService svc = new DepreciationRunProcessingService();
        String runId = "run-post-missing-code-001";
        lifecycle.createDraftRun(runId, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), "apr close");
        svc.calculateAndMarkCalculated(runId, "tester");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> svc.postRun(runId, "poster"));
        assertTrue(ex.getMessage().contains("account_code"));
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
        Path dbPath = this.tempDir.resolve("depreciation-run-processing");
        Database.init(dbPath);
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
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

    private void seedPostingAccounts() throws Exception
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("MERGE INTO account(account_number, name, account_code, account_type, supplemental_kinds, increase_side) KEY(account_number) VALUES (?,?,?,?,?,?)"))
        {
            ps.setString(1, "6100"); ps.setString(2, "Depreciation Expense"); ps.setString(3, "DEPRECIATION_EXPENSE"); ps.setString(4, "EXPENSE"); ps.setString(5, null); ps.setString(6, "DEBIT"); ps.addBatch();
            ps.setString(1, "1700"); ps.setString(2, "Accumulated Depreciation"); ps.setString(3, "ACCUMULATED_DEPRECIATION"); ps.setString(4, "ASSET"); ps.setString(5, "OTHER_ASSET"); ps.setString(6, "CREDIT"); ps.addBatch();
            ps.executeBatch();
        }
    }
}
