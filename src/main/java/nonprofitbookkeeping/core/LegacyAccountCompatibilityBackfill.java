package nonprofitbookkeeping.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

final class LegacyAccountCompatibilityBackfill
{
    void run(Connection c) throws SQLException
    {
        try (Statement st = c.createStatement())
        {
            st.execute(
                "UPDATE account SET code = account_number WHERE code IS NULL;");
            st.execute(
                "UPDATE account SET normal_balance = CASE WHEN UPPER(COALESCE(increase_side, 'DEBIT')) IN ('CREDIT','CR') THEN 'CREDIT' ELSE 'DEBIT' END WHERE normal_balance IS NULL;");
        }
    }
}
