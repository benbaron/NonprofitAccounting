package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.TestDatabase;
import nonprofitbookkeeping.core.Database;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentRepositoryRawSclxTest
{
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception
    {
        TestDatabase.reset(tempDir);
    }

    @Test
    void rawSclxPayloadIsNotStoredAndLegacyCopiesAreRemoved() throws Exception
    {
        try (Connection connection = Database.get().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                 "INSERT INTO document(name, content) VALUES (?, ?)"))
        {
            ps.setString(1, "sclx.raw.old-run");
            ps.setString(2, "large legacy payload");
            ps.executeUpdate();
        }

        DocumentRepository repository = new DocumentRepository();
        repository.upsert("sclx.raw.new-run", "new large payload");

        assertTrue(repository.find("sclx.raw.new-run").isEmpty());
        assertEquals(0, countRawDocuments());
    }

    private static int countRawDocuments() throws Exception
    {
        try (Connection connection = Database.get().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                 "SELECT COUNT(*) FROM document WHERE name LIKE 'sclx.raw.%'");
             ResultSet rs = ps.executeQuery())
        {
            rs.next();
            return rs.getInt(1);
        }
    }
}
