package nonprofitbookkeeping.schema;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FundTransferStatusTransitionSeedValidationTest
{
    private static final Map<TransitionKey, TransitionRow> EXPECTED_TRANSITIONS = expectedTransitions();

    @TempDir
    Path tempDir;

    @Test
    void flywaySeedsFundTransferStatusTransitions() throws Exception
    {
        String url = h2Url(tempDir.resolve("fund-transfer-status-transition-seed"));

        Flyway.configure()
            .dataSource(url, "sa", "")
            .locations("classpath:db/migration")
            .load()
            .migrate();

        assertEquals(EXPECTED_TRANSITIONS, readTransitions(url),
            "Flyway must seed the fund-transfer status transition matrix without Database.ensureSchema()");
    }

    private static Map<TransitionKey, TransitionRow> readTransitions(String url) throws SQLException
    {
        Map<TransitionKey, TransitionRow> transitions = new LinkedHashMap<>();
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("""
                 SELECT from_status, to_status, is_allowed, notes
                 FROM fund_transfer_status_transition
                 ORDER BY from_status, to_status
                 """))
        {
            while (rs.next())
            {
                TransitionKey key = new TransitionKey(rs.getString("from_status"), rs.getString("to_status"));
                transitions.put(key, new TransitionRow(rs.getBoolean("is_allowed"), rs.getString("notes")));
            }
        }
        return transitions;
    }

    private static Map<TransitionKey, TransitionRow> expectedTransitions()
    {
        Map<TransitionKey, TransitionRow> transitions = new LinkedHashMap<>();
        transitions.put(new TransitionKey("APPROVED", "POSTING"),
            new TransitionRow(true, "Posting transaction started."));
        transitions.put(new TransitionKey("APPROVED", "VOIDED"),
            new TransitionRow(true, "Cancelled after approval."));
        transitions.put(new TransitionKey("DRAFT", "APPROVED"),
            new TransitionRow(true, "Ready for posting."));
        transitions.put(new TransitionKey("DRAFT", "VOIDED"),
            new TransitionRow(true, "Cancelled before approval."));
        transitions.put(new TransitionKey("FAILED", "APPROVED"),
            new TransitionRow(true, "Retry after remediation."));
        transitions.put(new TransitionKey("FAILED", "VOIDED"),
            new TransitionRow(true, "Abandon failed transfer."));
        transitions.put(new TransitionKey("POSTED", "VOIDED"),
            new TransitionRow(false, "Disallow direct void; require reversing transfer."));
        transitions.put(new TransitionKey("POSTING", "FAILED"),
            new TransitionRow(true, "Posting failed; no posting txn persisted."));
        transitions.put(new TransitionKey("POSTING", "POSTED"),
            new TransitionRow(true, "Atomic post complete."));
        transitions.put(new TransitionKey("VOIDED", "DRAFT"),
            new TransitionRow(false, "No reopen from void."));
        return transitions;
    }

    private static String h2Url(Path databaseBase)
    {
        return "jdbc:h2:file:" + databaseBase.toAbsolutePath() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
    }

    private record TransitionKey(String fromStatus, String toStatus)
    {
    }

    private record TransitionRow(boolean isAllowed, String notes)
    {
    }
}
