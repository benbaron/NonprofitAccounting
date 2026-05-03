package org.nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;

import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class ReadModelMaintenanceService {

    public void refreshForTxn(long txnId) {
        try (Connection c = Database.get().getConnection()) {
            c.setAutoCommit(false);
            upsertDonationReadModel(c, txnId);
            upsertGrantReadModel(c, txnId);
            upsertFundReadModel(c, txnId);
            upsertReconciliationReadModel(c, txnId);
            c.commit();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to refresh read models for txnId=" + txnId, e);
        }
    }

    public void rebuildAll() {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id FROM txn")) {
            c.setAutoCommit(false);
            c.createStatement().executeUpdate("DELETE FROM rm_donation_summary");
            c.createStatement().executeUpdate("DELETE FROM rm_grant_summary");
            c.createStatement().executeUpdate("DELETE FROM rm_fund_summary");
            c.createStatement().executeUpdate("DELETE FROM rm_reconciliation_summary");
            c.createStatement().executeUpdate("DELETE FROM rm_depreciation_summary");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long txnId = rs.getLong(1);
                    upsertDonationReadModel(c, txnId);
                    upsertGrantReadModel(c, txnId);
                    upsertFundReadModel(c, txnId);
                    upsertReconciliationReadModel(c, txnId);
                }
            }
            rebuildDepreciationSummary(c);
            c.commit();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to rebuild read models", e);
        }
    }

    public Map<String, BigDecimal> detectDrift() {
        Map<String, BigDecimal> drift = new LinkedHashMap<>();
        try (Connection c = Database.get().getConnection()) {
            drift.put("donation", compare(c,
                "SELECT COALESCE(SUM(total_amount),0) FROM rm_donation_summary",
                "SELECT COALESCE(SUM(CASE WHEN ts.amount_signed>0 THEN ts.amount_signed ELSE 0 END),0) FROM txn_split ts JOIN account a ON a.id=ts.account_id WHERE UPPER(a.account_type) IN ('INCOME','REVENUE')"));
            drift.put("fund", compare(c,
                "SELECT COALESCE(SUM(net_amount),0) FROM rm_fund_summary",
                "SELECT COALESCE(SUM(amount_signed),0) FROM txn_split"));
            drift.put("reconciliation", compare(c,
                "SELECT COALESCE(SUM(absolute_amount),0) FROM rm_reconciliation_summary",
                "SELECT COALESCE(SUM(ABS(amount_signed)),0) FROM txn_split"));
            drift.put("depreciation", compare(c,
                "SELECT COALESCE(SUM(net_depreciation_total),0) FROM rm_depreciation_summary",
                "SELECT COALESCE(SUM(net_depreciation),0) FROM depreciation_record"));
        } catch (Exception e) {
            throw new IllegalStateException("Failed drift detection", e);
        }
        return drift;
    }

    private BigDecimal compare(Connection c, String left, String right) throws SQLException {
        BigDecimal l = scalar(c, left);
        BigDecimal r = scalar(c, right);
        return l.subtract(r);
    }

    private BigDecimal scalar(Connection c, String sql) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            BigDecimal v = rs.getBigDecimal(1);
            return v == null ? BigDecimal.ZERO : v;
        }
    }

    private void upsertDonationReadModel(Connection c, long txnId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
            MERGE INTO rm_donation_summary(txn_id, total_amount, line_count, refreshed_at)
            KEY(txn_id)
            SELECT ts.txn_id,
                   COALESCE(SUM(CASE WHEN ts.amount_signed > 0 THEN ts.amount_signed ELSE 0 END),0),
                   COUNT(*),
                   ?
            FROM txn_split ts
            JOIN account a ON a.id = ts.account_id
            WHERE ts.txn_id = ? AND UPPER(a.account_type) IN ('INCOME','REVENUE')
            GROUP BY ts.txn_id
            """)) {
            ps.setTimestamp(1, java.sql.Timestamp.from(Instant.now()));
            ps.setLong(2, txnId);
            ps.executeUpdate();
        }
    }

    private void upsertGrantReadModel(Connection c, long txnId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
            MERGE INTO rm_grant_summary(txn_id, grant_link_count, refreshed_at)
            KEY(txn_id)
            SELECT t.id, COALESCE(COUNT(gpl.id),0), ?
            FROM txn t
            LEFT JOIN txn_split ts ON ts.txn_id = t.id
            LEFT JOIN grant_posting_link gpl ON gpl.txn_split_id = ts.id
            WHERE t.id = ?
            GROUP BY t.id
            """)) {
            ps.setTimestamp(1, java.sql.Timestamp.from(Instant.now()));
            ps.setLong(2, txnId);
            ps.executeUpdate();
        }
    }

    private void upsertFundReadModel(Connection c, long txnId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
            MERGE INTO rm_fund_summary(txn_id, primary_fund_code, net_amount, refreshed_at)
            KEY(txn_id)
            SELECT ts.txn_id, MIN(f.code), COALESCE(SUM(ts.amount_signed),0), ?
            FROM txn_split ts
            JOIN fund f ON f.id = ts.fund_id
            WHERE ts.txn_id = ?
            GROUP BY ts.txn_id
            """)) {
            ps.setTimestamp(1, java.sql.Timestamp.from(Instant.now()));
            ps.setLong(2, txnId);
            ps.executeUpdate();
        }
    }

    private void upsertReconciliationReadModel(Connection c, long txnId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
            MERGE INTO rm_reconciliation_summary(txn_id, absolute_amount, split_count, refreshed_at)
            KEY(txn_id)
            SELECT ts.txn_id, COALESCE(SUM(ABS(ts.amount_signed)),0), COUNT(*), ?
            FROM txn_split ts
            WHERE ts.txn_id = ?
            GROUP BY ts.txn_id
            """)) {
            ps.setTimestamp(1, java.sql.Timestamp.from(Instant.now()));
            ps.setLong(2, txnId);
            ps.executeUpdate();
        }
    }

    private void rebuildDepreciationSummary(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
            MERGE INTO rm_depreciation_summary(depreciation_run_id, record_count, net_depreciation_total, refreshed_at)
            KEY(depreciation_run_id)
            SELECT dr.depreciation_run_id, COUNT(drec.depreciation_record_id), COALESCE(SUM(drec.net_depreciation),0), ?
            FROM depreciation_run dr
            LEFT JOIN depreciation_record drec ON drec.depreciation_run_id = dr.depreciation_run_id
            GROUP BY dr.depreciation_run_id
            """)) {
            ps.setTimestamp(1, java.sql.Timestamp.from(Instant.now()));
            ps.executeUpdate();
        }
    }
}
