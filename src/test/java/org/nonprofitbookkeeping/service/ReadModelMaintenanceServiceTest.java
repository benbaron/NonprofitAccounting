package org.nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadModelMaintenanceServiceTest {
    @TempDir Path tempDir;

    @Test
    void rebuildAndDriftDetection_workFromCanonical() throws Exception {
        Database.init(tempDir.resolve("rm-maint"));
        Database.get().ensureSchema();
        seedCanonical();

        ReadModelMaintenanceService service = new ReadModelMaintenanceService();
        service.rebuildAll();

        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM rm_fund_summary");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            assertEquals(1, rs.getInt(1));
        }

        Map<String, BigDecimal> drift = service.detectDrift();
        assertTrue(drift.containsKey("fund"));
        assertTrue(drift.containsKey("depreciation"));
        assertEquals(0, drift.get("fund").compareTo(BigDecimal.ZERO));
        assertEquals(0, drift.get("reconciliation").compareTo(BigDecimal.ZERO));
        assertEquals(0, drift.get("depreciation").compareTo(BigDecimal.ZERO));
    }

    private void seedCanonical() throws Exception {
        try (Connection c = Database.get().getConnection()) {
            c.createStatement().executeUpdate("MERGE INTO fund(id, code, name, fund_type, is_active) KEY(id) VALUES (1, 'GEN', 'General', 'UNRESTRICTED', TRUE)");
            c.createStatement().executeUpdate("MERGE INTO account(id, chart_id, code, account_number, name, account_type, subtype, normal_balance, increase_side, is_posting, is_active) KEY(id) VALUES (10, 1, '4000', '4000', 'Revenue', 'INCOME', 'DONATION', 'CREDIT', 'CREDIT', TRUE, TRUE)");
            c.createStatement().executeUpdate("INSERT INTO txn(id, txn_date, memo, created_at, updated_at) VALUES (100, DATE '2026-05-01', 'seed', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            c.createStatement().executeUpdate("INSERT INTO depreciation_run(depreciation_run_id, run_date, notes) VALUES ('run-1', DATE '2026-05-01', 'seed')");
            c.createStatement().executeUpdate("INSERT INTO depreciation_record(depreciation_record_id, depreciation_run_id, net_depreciation, depreciation_date) VALUES ('rec-1', 'run-1', 5.00, DATE '2026-05-01')");
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO txn_split(txn_id, account_id, fund_id, amount_signed, nmr_flag) VALUES (?,?,?,?,FALSE)")) {
                ps.setLong(1, 100);
                ps.setLong(2, 10);
                ps.setLong(3, 1);
                ps.setBigDecimal(4, new BigDecimal("50.00"));
                ps.executeUpdate();
            }
        }
    }
}
