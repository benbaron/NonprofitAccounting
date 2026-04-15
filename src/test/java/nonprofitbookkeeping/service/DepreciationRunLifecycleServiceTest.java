package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepreciationRunLifecycleServiceTest
{
    @TempDir
    Path tempDir;

    @Test
    void transitionAndLock_runPersistsStatusAndEvents() throws Exception
    {
        initDb();
        DepreciationRunLifecycleService svc = new DepreciationRunLifecycleService();

        String runId = "run-001";
        svc.createDraftRun(runId, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), "March close");
        svc.transitionStatus(runId, "CALCULATED", null, "analyst", "calculated run");
        svc.lockRun(runId, "reviewer", "approved for posting");

        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 SELECT run_status, is_locked, locked_by
                 FROM depreciation_run
                 WHERE depreciation_run_id = ?
             """))
        {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery())
            {
                assertTrue(rs.next());
                assertEquals("CALCULATED", rs.getString(1));
                assertTrue(rs.getBoolean(2));
                assertEquals("reviewer", rs.getString(3));
            }
        }

        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 SELECT COUNT(*)
                 FROM depreciation_run_event
                 WHERE depreciation_run_id = ?
             """))
        {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery())
            {
                rs.next();
                assertEquals(2, rs.getInt(1));
            }
        }
    }

    @Test
    void transitionStatus_postedRequiresPostedTxnId() throws Exception
    {
        initDb();
        DepreciationRunLifecycleService svc = new DepreciationRunLifecycleService();
        String runId = "run-002";
        svc.createDraftRun(runId, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), "March close");
        svc.transitionStatus(runId, "CALCULATED", null, "analyst", "calculated run");

        assertThrows(IllegalArgumentException.class,
            () -> svc.transitionStatus(runId, "POSTED", null, "poster", "missing posted txn"));
    }

    @Test
    void transitionStatus_lockedRunRejectsFurtherChanges() throws Exception
    {
        initDb();
        DepreciationRunLifecycleService svc = new DepreciationRunLifecycleService();
        String runId = "run-003";
        svc.createDraftRun(runId, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), "March close");
        svc.transitionStatus(runId, "CALCULATED", null, "analyst", "calculated run");
        svc.lockRun(runId, "reviewer", "approved");

        assertThrows(IllegalStateException.class,
            () -> svc.transitionStatus(runId, "VOIDED", null, "reviewer", "should fail after lock"));
    }

    private void initDb() throws Exception
    {
        Path dbPath = tempDir.resolve("depreciation-run-lifecycle");
        Database.init(dbPath);
        Database.get().ensureSchema();
    }
}
