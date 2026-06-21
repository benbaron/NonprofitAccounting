package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Executes depreciation run calculations and destructive lifecycle actions.
 */
public final class DepreciationRunProcessingService
{
    private static final BigDecimal DEFAULT_SALVAGE = BigDecimal.ZERO;
    private static final int DEFAULT_USEFUL_LIFE_MONTHS = 60;
    private final PostingFacade postingFacade;

    public DepreciationRunProcessingService()
    {
        this(new DefaultPostingFacade());
    }

    DepreciationRunProcessingService(PostingFacade postingFacade)
    {
        this.postingFacade = postingFacade;
    }

    public enum DeleteMode
    {
        DELETE_LINKED_JOURNALS,
        REVERSE_LINKED_JOURNALS
    }

    public record PreviewLine(String assetId, BigDecimal amount, String debitAccount, String creditAccount)
    {
    }

    public List<PreviewLine> calculateAndMarkCalculated(String runId, String actor) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            boolean autoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try
            {
                RunMeta run = loadRun(c, runId);
                if (run == null)
                {
                    throw new IllegalArgumentException("Depreciation run not found: " + runId);
                }
                if (run.isLocked())
                {
                    throw new IllegalStateException("Run is locked: " + runId);
                }
                if (!"DRAFT".equals(run.status()))
                {
                    throw new IllegalStateException("Run must be DRAFT before calculation.");
                }

                deleteExistingRecords(c, runId);
                List<PreviewLine> preview = calculate(c, run);
                appendRunEvent(c, runId, "RUN_CALCULATED",
                    "assets=" + preview.size() + ", actor=" + safe(actor), actor);
                c.commit();
                c.setAutoCommit(autoCommit);
                new DepreciationRunLifecycleService().transitionStatus(runId, "CALCULATED", null, actor,
                    "Calculated " + preview.size() + " depreciation rows");
                return preview;
            }
            catch (Exception ex)
            {
                c.rollback();
                if (ex instanceof SQLException sql)
                {
                    throw sql;
                }
                throw ex;
            }
            finally
            {
                c.setAutoCommit(autoCommit);
            }
        }
    }

    public List<PreviewLine> journalPreview(String runId) throws SQLException
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 SELECT dr.asset_record_id, dr.net_depreciation
                 FROM depreciation_record dr
                 WHERE dr.depreciation_run_id = ?
                 ORDER BY dr.sequence_in_run
             """))
        {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery())
            {
                List<PreviewLine> lines = new ArrayList<>();
                while (rs.next())
                {
                    lines.add(new PreviewLine(
                        rs.getString(1),
                        rs.getBigDecimal(2),
                        "Depreciation Expense",
                        "Accumulated Depreciation"));
                }
                return lines;
            }
        }
    }

    public void unlockRun(String runId, String actor, String detail) throws SQLException
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 UPDATE depreciation_run
                 SET is_locked = FALSE, locked_at = NULL, locked_by = NULL
                 WHERE depreciation_run_id = ?
             """))
        {
            ps.setString(1, runId);
            int changed = ps.executeUpdate();
            if (changed == 0)
            {
                throw new IllegalArgumentException("Depreciation run not found: " + runId);
            }
        }
        try (Connection c = Database.get().getConnection())
        {
            appendRunEvent(c, runId, "RUN_UNLOCKED", "actor=" + safe(actor) + ", detail=" + safe(detail), actor);
        }
    }

    public void deleteRun(String runId, DeleteMode mode, String actor) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            boolean autoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try
            {
                Set<Integer> linkedJournalIds = new LinkedHashSet<>();
                try (PreparedStatement ps = c.prepareStatement("""
                    SELECT asset_record_id, net_depreciation, posted_journal_txn_id, reversal_journal_txn_id
                    FROM depreciation_record
                    WHERE depreciation_run_id = ?
                """))
                {
                    ps.setString(1, runId);
                    try (ResultSet rs = ps.executeQuery())
                    {
                        while (rs.next())
                        {
                            rollbackAccumulatedDepreciation(c, rs.getString(1), rs.getBigDecimal(2));
                            Integer posted = (Integer) rs.getObject(3);
                            Integer reversal = (Integer) rs.getObject(4);
                            if (posted != null)
                            {
                                linkedJournalIds.add(posted);
                            }
                            if (reversal != null)
                            {
                                linkedJournalIds.add(reversal);
                            }
                        }
                    }
                }

                if (mode == DeleteMode.REVERSE_LINKED_JOURNALS)
                {
                    for (Integer journalId : linkedJournalIds)
                    {
                        reverseJournal(c, journalId, runId);
                    }
                }
                else
                {
                    for (Integer journalId : linkedJournalIds)
                    {
                        deleteJournal(c, journalId);
                    }
                }

                try (PreparedStatement deleteRecords = c.prepareStatement(
                    "DELETE FROM depreciation_record WHERE depreciation_run_id = ?"))
                {
                    deleteRecords.setString(1, runId);
                    deleteRecords.executeUpdate();
                }
                try (PreparedStatement deleteEvents = c.prepareStatement(
                    "DELETE FROM depreciation_run_event WHERE depreciation_run_id = ?"))
                {
                    deleteEvents.setString(1, runId);
                    deleteEvents.executeUpdate();
                }
                try (PreparedStatement deleteRun = c.prepareStatement(
                    "DELETE FROM depreciation_run WHERE depreciation_run_id = ?"))
                {
                    deleteRun.setString(1, runId);
                    deleteRun.executeUpdate();
                }
                c.commit();
            }
            catch (Exception ex)
            {
                c.rollback();
                if (ex instanceof SQLException sql)
                {
                    throw sql;
                }
                throw ex;
            }
            finally
            {
                c.setAutoCommit(autoCommit);
            }
        }
    }

    public int postRun(String runId, String actor) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            RunMeta run = loadRun(c, runId);
            if (run == null) throw new IllegalArgumentException("Depreciation run not found: " + runId);
            if (run.isLocked()) throw new IllegalStateException("Run is locked: " + runId);
            if (!"CALCULATED".equals(run.status())) throw new IllegalStateException("Run must be CALCULATED before posting");
        }
        int posted = 0;
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 SELECT depreciation_record_id, net_depreciation, posted_journal_txn_id, period_start, period_end
                 FROM depreciation_record
                 WHERE depreciation_run_id = ?
                 ORDER BY sequence_in_run
             """))
        {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    String recordId = rs.getString(1);
                    BigDecimal amount = rs.getBigDecimal(2);
                    Integer postedTxn = (Integer) rs.getObject(3);
                    LocalDate periodStart = rs.getDate(4).toLocalDate();
                    LocalDate periodEnd = rs.getDate(5).toLocalDate();
                    if (postedTxn != null)
                    {
                        continue;
                    }
                    if (periodEnd.isBefore(periodStart))
                    {
                        throw new IllegalStateException("Invalid depreciation period for record " + recordId);
                    }
                    PostingReference ref = this.postingFacade.post(
                        DepreciationPostingFactory.build(recordId, amount, periodEnd));
                    try (PreparedStatement up = c.prepareStatement("""
                        UPDATE depreciation_record
                        SET posted_journal_txn_id = ?
                        WHERE depreciation_record_id = ? AND posted_journal_txn_id IS NULL
                    """))
                    {
                        up.setInt(1, ref.journalTxnId());
                        up.setString(2, recordId);
                        posted += up.executeUpdate();
                    }
                }
            }
            appendRunEvent(c, runId, "RUN_POSTED", "count=" + posted + ", actor=" + safe(actor), actor);
        }
        return posted;
    }

    public int reversePostedRun(String runId, String actor, String reason) throws SQLException
    {
        int reversed = 0;
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 SELECT depreciation_record_id, posted_journal_txn_id, reversal_journal_txn_id
                 FROM depreciation_record
                 WHERE depreciation_run_id = ?
                 ORDER BY sequence_in_run
             """))
        {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    String recordId = rs.getString(1);
                    Integer postedTxn = (Integer) rs.getObject(2);
                    Integer reversalTxn = (Integer) rs.getObject(3);
                    if (postedTxn == null || reversalTxn != null)
                    {
                        continue;
                    }
                    PostingReference ref = this.postingFacade.reverse(postedTxn,
                        reason == null ? "Depreciation run reversal " + runId : reason);
                    try (PreparedStatement up = c.prepareStatement("""
                        UPDATE depreciation_record
                        SET reversal_journal_txn_id = ?
                        WHERE depreciation_record_id = ? AND reversal_journal_txn_id IS NULL
                    """))
                    {
                        up.setInt(1, ref.journalTxnId());
                        up.setString(2, recordId);
                        reversed += up.executeUpdate();
                    }
                }
            }
            appendRunEvent(c, runId, "RUN_REVERSED", "count=" + reversed + ", actor=" + safe(actor), actor);
        }
        return reversed;
    }

    private List<PreviewLine> calculate(Connection c, RunMeta run) throws SQLException
    {
        List<PreviewLine> lines = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("""
            SELECT a.asset_id,
                   COALESCE(d.depreciable_basis, a.approx_value_total, 0),
                   COALESCE(a.accumulated_depreciation, 0),
                   COALESCE(d.salvage_value, 0),
                   COALESCE(d.useful_life_months, ?)
            FROM imported_asset_record a
            LEFT JOIN asset_record_detail d ON d.asset_record_id = a.asset_id
            WHERE COALESCE(a.approx_value_total, 0) > 0
        """))
        {
            ps.setInt(1, DEFAULT_USEFUL_LIFE_MONTHS);
            try (ResultSet rs = ps.executeQuery())
            {
                int sequence = 0;
                int periodMonths = Math.max(1, (int) ChronoUnit.MONTHS.between(
                    run.periodStart().withDayOfMonth(1), run.periodEnd().withDayOfMonth(1)) + 1);
                while (rs.next())
                {
                    String assetId = rs.getString(1);
                    BigDecimal basis = rs.getBigDecimal(2);
                    BigDecimal accumulated = rs.getBigDecimal(3);
                    BigDecimal salvage = rs.getBigDecimal(4) == null ? DEFAULT_SALVAGE : rs.getBigDecimal(4);
                    int usefulLifeMonths = rs.getInt(5);
                    if (usefulLifeMonths <= 0)
                    {
                        usefulLifeMonths = DEFAULT_USEFUL_LIFE_MONTHS;
                    }

                    BigDecimal depreciableTotal = basis.subtract(salvage);
                    BigDecimal remaining = depreciableTotal.subtract(accumulated);
                    if (remaining.compareTo(BigDecimal.ZERO) <= 0)
                    {
                        continue;
                    }
                    BigDecimal monthly = depreciableTotal
                        .divide(BigDecimal.valueOf(usefulLifeMonths), 6, RoundingMode.HALF_UP);
                    BigDecimal proposed = monthly.multiply(BigDecimal.valueOf(periodMonths));
                    BigDecimal amount = proposed.min(remaining).setScale(2, RoundingMode.HALF_UP);
                    if (amount.compareTo(BigDecimal.ZERO) <= 0)
                    {
                        continue;
                    }

                    sequence++;
                    ensureAssetDetailExists(c, assetId, run.periodStart());
                    persistDepreciationRecord(c, run, sequence, assetId, amount, basis);
                    updateAccumulated(c, assetId, amount);
                    lines.add(new PreviewLine(assetId, amount, "Depreciation Expense", "Accumulated Depreciation"));
                }
            }
        }
        return lines;
    }

    private void persistDepreciationRecord(Connection c, RunMeta run, int sequence, String assetId,
                                           BigDecimal amount, BigDecimal basis) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement("""
            INSERT INTO depreciation_record(
                depreciation_record_id, depreciation_run_id, asset_record_id, net_depreciation, depreciation_date,
                depreciation_percentage, amortization_schedule, period_start, period_end, sequence_in_run
            ) VALUES (?,?,?,?,?,?,?,?,?,?)
        """))
        {
            ps.setString(1, "depr-" + UUID.randomUUID());
            ps.setString(2, run.runId());
            ps.setString(3, assetId);
            ps.setBigDecimal(4, amount);
            ps.setDate(5, Date.valueOf(run.periodEnd()));
            BigDecimal pct = basis.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : amount.divide(basis, 4, RoundingMode.HALF_UP);
            ps.setBigDecimal(6, pct);
            ps.setString(7, "STRAIGHT_LINE");
            ps.setDate(8, Date.valueOf(run.periodStart()));
            ps.setDate(9, Date.valueOf(run.periodEnd()));
            ps.setInt(10, sequence);
            ps.executeUpdate();
        }
    }

    private void ensureAssetDetailExists(Connection c, String assetId, LocalDate defaultDateAcquired) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement("""
            MERGE INTO asset_record_detail(asset_record_id, date_acquired, asset_state, in_service_date, updated_at)
            KEY(asset_record_id)
            VALUES (?, ?, 'ACTIVE', ?, CURRENT_TIMESTAMP)
        """))
        {
            Date acquired = Date.valueOf(defaultDateAcquired == null ? LocalDate.now() : defaultDateAcquired);
            ps.setString(1, assetId);
            ps.setDate(2, acquired);
            ps.setDate(3, acquired);
            ps.executeUpdate();
        }
    }

    private void updateAccumulated(Connection c, String assetId, BigDecimal amount) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement("""
            UPDATE imported_asset_record
            SET accumulated_depreciation = COALESCE(accumulated_depreciation, 0) + ?
            WHERE asset_id = ?
        """))
        {
            ps.setBigDecimal(1, amount);
            ps.setString(2, assetId);
            ps.executeUpdate();
        }
    }

    private void rollbackAccumulatedDepreciation(Connection c, String assetId, BigDecimal amount) throws SQLException
    {
        if (assetId == null || amount == null)
        {
            return;
        }
        try (PreparedStatement ps = c.prepareStatement("""
            UPDATE imported_asset_record
            SET accumulated_depreciation = CASE
                WHEN COALESCE(accumulated_depreciation, 0) - ? < 0 THEN 0
                ELSE COALESCE(accumulated_depreciation, 0) - ?
            END
            WHERE asset_id = ?
        """))
        {
            ps.setBigDecimal(1, amount);
            ps.setBigDecimal(2, amount);
            ps.setString(3, assetId);
            ps.executeUpdate();
        }
    }

    private void reverseJournal(Connection c, int sourceTxnId, String runId) throws SQLException
    {
        int newTxnId;
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM journal_transaction"))
        {
            rs.next();
            newTxnId = rs.getInt(1);
        }
        try (PreparedStatement tx = c.prepareStatement("""
            INSERT INTO journal_transaction(id, booking_ts, date_text, memo, to_from, check_number, clear_bank,
                                            bank_name, reconciled, budget_tracking, associated_fund_name)
            SELECT ?, booking_ts, ?, CONCAT('REVERSAL(', ?, '): ', COALESCE(memo, 'Depreciation')),
                   to_from, check_number, clear_bank, bank_name, FALSE, budget_tracking, associated_fund_name
            FROM journal_transaction WHERE id = ?
        """))
        {
            tx.setInt(1, newTxnId);
            tx.setString(2, LocalDate.now().toString());
            tx.setString(3, runId);
            tx.setInt(4, sourceTxnId);
            tx.executeUpdate();
        }
        try (PreparedStatement entries = c.prepareStatement("""
            INSERT INTO journal_entry(txn_id, amount, account_number, account_side, account_name, fund_number)
            SELECT ?, amount, account_number,
                   CASE WHEN UPPER(COALESCE(account_side, 'DEBIT')) IN ('DEBIT','DR') THEN 'CREDIT' ELSE 'DEBIT' END,
                   account_name, fund_number
            FROM journal_entry WHERE txn_id = ?
        """))
        {
            entries.setInt(1, newTxnId);
            entries.setInt(2, sourceTxnId);
            entries.executeUpdate();
        }
    }

    private void deleteJournal(Connection c, int txnId) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM journal_transaction WHERE id = ?"))
        {
            ps.setInt(1, txnId);
            ps.executeUpdate();
        }
    }

    private void deleteExistingRecords(Connection c, String runId) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM depreciation_record WHERE depreciation_run_id = ?"))
        {
            ps.setString(1, runId);
            ps.executeUpdate();
        }
    }

    private RunMeta loadRun(Connection c, String runId) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement("""
            SELECT depreciation_run_id, period_start, period_end, run_status, is_locked
            FROM depreciation_run WHERE depreciation_run_id = ?
        """))
        {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next())
                {
                    return null;
                }
                return new RunMeta(
                    rs.getString(1),
                    rs.getDate(2).toLocalDate(),
                    rs.getDate(3).toLocalDate(),
                    rs.getString(4),
                    rs.getBoolean(5));
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

    private String safe(String value)
    {
        return value == null ? "" : value;
    }

    private record RunMeta(String runId, LocalDate periodStart, LocalDate periodEnd, String status, boolean isLocked)
    {
    }
}
