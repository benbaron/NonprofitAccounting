package org.nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.nonprofitbookkeeping.persistence.Jpa;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventAccountingServiceTest
{
    @TempDir Path tempDir;
    private Jpa jpa;

    @AfterEach
    void tearDown()
    {
        if (this.jpa != null) this.jpa.close();
    }

    @Test
    void summaryUsesActivityLinkedIncomeExpenseAndDepositRefundSplits() throws Exception
    {
        seedEventAccountingData();
        this.jpa = new Jpa();
        EventAccountingService.EventAccountingWorkspace workspace = new EventAccountingService(this.jpa).getWorkspace(10L);

        assertEquals("GALA", workspace.code());
        assertAmount("1500", workspace.summary().income());
        assertAmount("250", workspace.summary().expenses());
        assertAmount("1250", workspace.summary().net());
        assertAmount("1500", workspace.summary().deposits());
        assertAmount("100", workspace.summary().refunds());
        assertEquals(5, workspace.summary().linkedTransactionCount());
    }

    @Test
    void eventToTransactionLinkingRequiresMatchingActivitySplit() throws Exception
    {
        seedEventAccountingData();
        this.jpa = new Jpa();
        var workspaces = new EventAccountingService(this.jpa).listWorkspaces();

        var gala = workspaces.stream().filter(w -> w.code().equals("GALA")).findFirst().orElseThrow();
        assertEquals(5, gala.linkedTransactions().size());
        assertTrue(gala.linkedTransactions().stream().noneMatch(row -> row.transactionId() == 99L));
    }

    private static void assertAmount(String expected, BigDecimal actual)
    {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    private void seedEventAccountingData() throws Exception
    {
        Database.init(this.tempDir.resolve("event-accounting"));
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
        Database.get().ensureSchema();
        try (Connection c = Database.get().getConnection(); Statement st = c.createStatement())
        {
            st.execute("INSERT INTO activity(id, code, name, is_active) VALUES (10, 'GALA', 'Annual Gala', TRUE)");
            st.execute("INSERT INTO activity(id, code, name, is_active) VALUES (20, 'PICNIC', 'Picnic', TRUE)");
            st.execute("INSERT INTO account(id, account_number, chart_id, code, name, account_type, opening_balance, normal_balance, is_posting, is_active) VALUES " +
                "(101, '1000', 1, '1000', 'Bank', 'ASSET', 0, 'DEBIT', TRUE, TRUE)," +
                "(102, '4000', 1, '4000', 'Event Income', 'INCOME', 0, 'CREDIT', TRUE, TRUE)," +
                "(103, '5000', 1, '5000', 'Event Expense', 'EXPENSE', 0, 'DEBIT', TRUE, TRUE)");
            st.execute("INSERT INTO txn(id, txn_date, memo) VALUES " +
                "(1, DATE '2026-05-01', 'ticket deposit')," +
                "(2, DATE '2026-05-02', 'venue expense')," +
                "(3, DATE '2026-05-03', 'refund')," +
                "(99, DATE '2026-05-04', 'unrelated')");
            st.execute("INSERT INTO txn_split(txn_id, account_id, fund_id, activity_id, amount_signed, nmr_flag) VALUES " +
                "(1, 101, 1, 10, 1500.00, FALSE)," +
                "(1, 102, 1, 10, 1500.00, FALSE)," +
                "(2, 103, 1, 10, 250.00, FALSE)," +
                "(2, 101, 1, 10, -250.00, FALSE)," +
                "(3, 101, 1, 10, -100.00, FALSE)," +
                "(99, 102, 1, 20, 999.00, FALSE)");
        }
    }
}
