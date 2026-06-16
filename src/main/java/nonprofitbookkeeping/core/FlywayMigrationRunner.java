package nonprofitbookkeeping.core;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.exception.FlywayValidateException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runs versioned Flyway migrations for an H2 database before legacy schema
 * compatibility/backfill code is invoked.
 *
 * <p>This class is intentionally conservative during the transition. It runs
 * once per JDBC URL, leaves {@link Database#ensureSchema()} in place, and can be
 * disabled with {@value #DISABLE_PROPERTY} while troubleshooting.</p>
 */
public final class FlywayMigrationRunner
{
    public static final String DISABLE_PROPERTY = "nonprofitbookkeeping.flyway.disabled";

    private static final Set<String> MIGRATED_URLS = ConcurrentHashMap.newKeySet();

    private FlywayMigrationRunner()
    {
        // utility class
    }

    public static void migrateCurrentDatabaseIfEnabled() throws SQLException
    {
        Database database = Database.get();
        migrateIfEnabled(database.getJdbcUrl(), database.getUser(), database.getPass());
    }

    public static void migrateIfEnabled(String jdbcUrl, String user, String password) throws SQLException
    {
        if (Boolean.getBoolean(DISABLE_PROPERTY))
        {
            return;
        }
        if (jdbcUrl == null || jdbcUrl.isBlank())
        {
            throw new SQLException("JDBC URL is required for Flyway migration");
        }
        if (!MIGRATED_URLS.add(jdbcUrl))
        {
            return;
        }

        try
        {
            prepareLegacyScheduleDefaultCompatibility(jdbcUrl, user, password);
            Flyway flyway = configuredFlyway(jdbcUrl, user, password);
            try
            {
                flyway.migrate();
            }
            catch (FlywayValidateException ex)
            {
                if (!hasFailedMigration(ex))
                {
                    throw ex;
                }
                flyway.repair();
                flyway.migrate();
            }
        }
        catch (RuntimeException ex)
        {
            MIGRATED_URLS.remove(jdbcUrl);
            throw new SQLException("Flyway migration failed for " + jdbcUrl, ex);
        }
    }

    private static void prepareLegacyScheduleDefaultCompatibility(String jdbcUrl, String user, String password) throws SQLException
    {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, user, password))
        {
            if (!tableExists(connection, "ACCOUNT_SUBTYPE_SCHEDULE_DEFAULT") ||
                columnExists(connection, "ACCOUNT_SUBTYPE_SCHEDULE_DEFAULT", "IS_REQUIRED"))
            {
                return;
            }
            try (Statement st = connection.createStatement())
            {
                st.execute("""
                    ALTER TABLE account_subtype_schedule_default
                    ADD COLUMN IF NOT EXISTS is_required BOOLEAN DEFAULT TRUE
                    """);
            }
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException
    {
        try (ResultSet rs = connection.getMetaData().getTables(null, "PUBLIC", tableName, new String[] {"TABLE"}))
        {
            return rs.next();
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException
    {
        try (ResultSet rs = connection.getMetaData().getColumns(null, "PUBLIC", tableName, columnName))
        {
            return rs.next();
        }
    }

    private static Flyway configuredFlyway(String jdbcUrl, String user, String password)
    {
        return Flyway.configure()
            .dataSource(jdbcUrl, user, password)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .load();
    }

    private static boolean hasFailedMigration(FlywayValidateException ex)
    {
        String message = ex.getMessage();
        return message != null && message.contains("Detected failed migration");
    }

    static void resetForTest()
    {
        MIGRATED_URLS.clear();
    }
}
