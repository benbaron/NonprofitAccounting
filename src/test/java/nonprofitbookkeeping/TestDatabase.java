package nonprofitbookkeeping;

import nonprofitbookkeeping.core.Database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Utility to initialize and reset the shared H2 database for tests. */
public final class TestDatabase {
    private TestDatabase() {
    }

    public static void reset(Path tempDir) throws SQLException {
        Path dbFile = tempDir.resolve("test-db");
        Database.init(dbFile);
        Database.get().ensureSchema();
        try (Connection connection = Database.get().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE json_storage");
            statement.execute("DELETE FROM document");
            statement.execute("DELETE FROM donor");
        }
    }
}
