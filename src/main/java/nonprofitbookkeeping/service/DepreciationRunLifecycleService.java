package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * Enforces lifecycle and immutability rules for depreciation runs.
 */
public final class DepreciationRunLifecycleService
{
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
        "DRAFT", Set.of("CALCULATED", "VOIDED"),
        "CALCULATED", Set.of("POSTED", "VOIDED"),
        "POSTED", Set.of(),
        "VOIDED", Set.of()
    );

    public void transitionStatus(String runId, String toStatus, Integer postedTxnId, String actor, String detail)
        throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            boolean autoCommit = c.getAutoCommit();
            boolean txCommitted = false;
            c.setAutoCommit(false);
            try
            {
                RunState current = fetchRunState(c, runId);
                if (current == null)
                {
                    throw new IllegalArgumentException("Depreciation run not found: " + runId);
                }
                if (current.isLocked() && !current.status().equals(toStatus))
                {
                    throw new IllegalStateException("Depreciation run is locked: " + runId);
                }
                validateTransition(current.status(), toStatus);
                validatePostedLink(toStatus, postedTxnId);

                try (PreparedStatement ps = c.prepareStatement("""
                    UPDATE depreciation_run
                    SET run_status = ?, posted_txn_id = ?
                    WHERE depreciation_run_id = ?
                """))
                {
                    ps.setString(1, toStatus);
                    if (postedTxnId == null)
                    {
                        ps.setNull(2, java.sql.Types.INTEGER);
                    }
                    else
                    {
                        ps.setInt(2, postedTxnId);
                    }
                    ps.setString(3, runId);
                    ps.executeUpdate();
                }

                appendRunEvent(c, runId, "STATUS_TRANSITION",
                    "from=" + current.status() + ", to=" + toStatus + ", actor=" + safe(actor) + ", detail=" + safe(detail),
                    actor);

                c.commit();
                txCommitted = true;
            }
            catch (Exception e)
            {
                if (!txCommitted)
                {
                    c.rollback();
                }
                if (e instanceof SQLException sql)
                {
                    throw sql;
                }
                throw e;
            }
            finally
            {
                c.setAutoCommit(autoCommit);
            }
        }
    }

    public void lockRun(String runId, String actor, String detail) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            boolean autoCommit = c.getAutoCommit();
            boolean txCommitted = false;
            c.setAutoCommit(false);
            try
            {
                RunState current = fetchRunState(c, runId);
                if (current == null)
                {
                    throw new IllegalArgumentException("Depreciation run not found: " + runId);
                }
                if ("DRAFT".equals(current.status()))
                {
                    throw new IllegalStateException("Depreciation run must be CALCULATED or POSTED before lock.");
                }

                try (PreparedStatement ps = c.prepareStatement("""
                    UPDATE depreciation_run
                    SET is_locked = TRUE,
                        locked_at = CURRENT_TIMESTAMP,
                        locked_by = COALESCE(?, locked_by)
                    WHERE depreciation_run_id = ?
                """))
                {
                    ps.setString(1, actor);
                    ps.setString(2, runId);
                    ps.executeUpdate();
                }

                appendRunEvent(c, runId, "RUN_LOCKED",
                    "status=" + current.status() + ", actor=" + safe(actor) + ", detail=" + safe(detail),
                    actor);

                c.commit();
                txCommitted = true;
            }
            catch (Exception e)
            {
                if (!txCommitted)
                {
                    c.rollback();
                }
                if (e instanceof SQLException sql)
                {
                    throw sql;
                }
                throw e;
            }
            finally
            {
                c.setAutoCommit(autoCommit);
            }
        }
    }

    public String createDraftRun(String runId, LocalDate periodStart, LocalDate periodEnd, String notes) throws SQLException
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 MERGE INTO depreciation_run(depreciation_run_id, run_date, notes, period_start, period_end, run_status, is_locked)
                 KEY(depreciation_run_id)
                 VALUES (?, ?, ?, ?, ?, 'DRAFT', FALSE)
             """))
        {
            ps.setString(1, runId);
            ps.setDate(2, Date.valueOf(periodEnd == null ? LocalDate.now() : periodEnd));
            ps.setString(3, notes);
            ps.setDate(4, periodStart == null ? null : Date.valueOf(periodStart));
            ps.setDate(5, periodEnd == null ? null : Date.valueOf(periodEnd));
            ps.executeUpdate();
        }
        return runId;
    }

    private RunState fetchRunState(Connection c, String runId) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement("""
            SELECT run_status, is_locked
            FROM depreciation_run
            WHERE depreciation_run_id = ?
        """))
        {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next() ? new RunState(rs.getString(1), rs.getBoolean(2)) : null;
            }
        }
    }

    private void appendRunEvent(Connection c, String runId, String eventType, String detail, String actor)
        throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement("""
            INSERT INTO depreciation_run_event(depreciation_run_id, event_type, event_detail, actor)
            VALUES (?, ?, ?, ?)
        """))
        {
            ps.setString(1, runId);
            ps.setString(2, eventType);
            ps.setString(3, detail);
            ps.setString(4, actor);
            ps.executeUpdate();
        }
    }

    private void validateTransition(String fromStatus, String toStatus)
    {
        Set<String> allowed = ALLOWED_TRANSITIONS.get(fromStatus);
        if (allowed == null || !allowed.contains(toStatus))
        {
            throw new IllegalStateException("Illegal depreciation run transition: " + fromStatus + " -> " + toStatus);
        }
    }

    private void validatePostedLink(String toStatus, Integer postedTxnId)
    {
        if ("POSTED".equals(toStatus) && postedTxnId == null)
        {
            throw new IllegalArgumentException("POSTED transition requires posted transaction ID.");
        }
        if (!"POSTED".equals(toStatus) && postedTxnId != null)
        {
            throw new IllegalArgumentException("Only POSTED transition may include posted transaction ID.");
        }
    }

    private String safe(String value)
    {
        return value == null ? "" : value;
    }

    private record RunState(String status, boolean isLocked)
    {
    }
}
