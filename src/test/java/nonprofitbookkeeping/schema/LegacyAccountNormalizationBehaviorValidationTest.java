package nonprofitbookkeeping.schema;

import nonprofitbookkeeping.core.Database;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyAccountNormalizationBehaviorValidationTest
{
    @TempDir
    Path tempDir;

    @Test
    void flywayNormalizesLegacyAccountCodeAndNormalBalanceOnlyWhenMissing() throws Exception
    {
        Database.init(tempDir.resolve("legacy-account-normalization-behavior"));
        Database database = Database.get();

        migrateWithFlyway(database, "15");
        seedLegacyAccounts(database);

        migrateWithFlyway(database);
        database.ensureSchema();

        assertAccount(database, "1000", "1000", "DEBIT");
        assertAccount(database, "2000", "2000", "CREDIT");
        assertAccount(database, "3000", "3000", "CREDIT");
        assertAccount(database, "4000", "custom-code", "CREDIT");
    }

    private static void migrateWithFlyway(Database database)
    {
        migrateWithFlyway(database, null);
    }

    private static void migrateWithFlyway(Database database, String target)
    {
        var configuration = Flyway.configure()
            .dataSource(database.getJdbcUrl(), database.getUser(), database.getPass())
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .baselineVersion("0");
        if (target != null)
        {
            configuration.target(target);
        }
        configuration.load().migrate();
    }

    private static void seedLegacyAccounts(Database database) throws SQLException
    {
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass());
             Statement st = connection.createStatement())
        {
            st.execute("INSERT INTO account(account_number, name, increase_side) VALUES ('1000', 'Debit Account', 'DEBIT')");
            st.execute("INSERT INTO account(account_number, name, increase_side) VALUES ('2000', 'Credit Account', 'CREDIT')");
            st.execute("INSERT INTO account(account_number, name, increase_side) VALUES ('3000', 'Short Credit Account', 'CR')");
            st.execute("""
                INSERT INTO account(account_number, name, increase_side, code, normal_balance)
                VALUES ('4000', 'Already Normalized Account', 'DEBIT', 'custom-code', 'CREDIT')
                """);
        }
    }

    private static void assertAccount(Database database, String accountNumber, String expectedCode, String expectedNormalBalance)
        throws SQLException
    {
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass());
             PreparedStatement ps = connection.prepareStatement(
                 "SELECT code, normal_balance FROM account WHERE account_number = ?"))
        {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery())
            {
                rs.next();
                assertEquals(expectedCode, rs.getString(1));
                assertEquals(expectedNormalBalance, rs.getString(2));
            }
        }
    }

}
