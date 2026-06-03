package nonprofitbookkeeping.core;

import org.flywaydb.core.Flyway;

import java.sql.SQLException;
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
            Flyway.configure()
                .dataSource(jdbcUrl, user, password)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load()
                .migrate();
        }
        catch (RuntimeException ex)
        {
            MIGRATED_URLS.remove(jdbcUrl);
            throw new SQLException("Flyway migration failed for " + jdbcUrl, ex);
        }
    }

    static void resetForTest()
    {
        MIGRATED_URLS.clear();
    }
}
