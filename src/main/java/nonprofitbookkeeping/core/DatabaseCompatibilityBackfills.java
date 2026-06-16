package nonprofitbookkeeping.core;

import java.sql.Connection;
import java.sql.SQLException;

final class DatabaseCompatibilityBackfills
{
    private final LegacyTransactionMapCompatibilityBackfill legacyTransactionMapBackfill =
        new LegacyTransactionMapCompatibilityBackfill();
    private final ReconciledDataCompatibilityBackfill reconciledDataBackfill =
        new ReconciledDataCompatibilityBackfill();

    void run(Connection c) throws SQLException
    {
        legacyTransactionMapBackfill.run(c);
        reconciledDataBackfill.run(c);
    }
}
