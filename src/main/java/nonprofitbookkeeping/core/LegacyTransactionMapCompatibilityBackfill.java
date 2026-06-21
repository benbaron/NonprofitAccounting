package nonprofitbookkeeping.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

final class LegacyTransactionMapCompatibilityBackfill
{
    void run(Connection c) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO legacy_txn_map(legacy_txn_id, canonical_txn_id, checksum)
                SELECT jt.id, t.id, NULL
                FROM journal_transaction jt
                JOIN txn t ON t.id = jt.id
                LEFT JOIN legacy_txn_map m ON m.legacy_txn_id = jt.id
                WHERE m.legacy_txn_id IS NULL
            """))
        {
            ps.executeUpdate();
        }
    }
}
