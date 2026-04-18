package nonprofitbookkeeping.persistence.records;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.records.DocumentRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists imported SCLX document records into a concrete staging table.
 */
@ApplicationScoped
public class DocumentRecordRepository
{
    private static final String CREATE_SQL = """
        CREATE TABLE IF NOT EXISTS imported_document_record (
            document_id VARCHAR(255) PRIMARY KEY,
            document_type VARCHAR(128),
            reference_number VARCHAR(255),
            document_date DATE,
            file_name VARCHAR(512),
            notes CLOB,
            extensions_json CLOB
        )
        """;

    private static final String UPSERT_SQL = """
        MERGE INTO imported_document_record(
            document_id, document_type, reference_number, document_date, file_name, notes, extensions_json
        ) KEY(document_id)
        VALUES(?,?,?,?,?,?,?)
        """;
    private static final String LIST_ALL_SQL = """
        SELECT document_id, document_type, reference_number, document_date, file_name, notes
        FROM imported_document_record
        """;

    public void upsert(DocumentRecord row) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
            {
                int i = 0;
                ps.setString(++i, row.documentId());
                ps.setString(++i, row.documentType());
                ps.setString(++i, row.referenceNumber());
                ps.setDate(++i, row.documentDate() == null ? null : Date.valueOf(row.documentDate()));
                ps.setString(++i, row.fileName());
                ps.setString(++i, row.notes());
                ps.setString(++i, JsonColumnCodec.toJson(row.extensions()));
                ps.executeUpdate();
            }
        }
    }

    public List<DocumentRecord> listAll() throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            List<DocumentRecord> rows = new ArrayList<>();
            try (Statement statement = c.createStatement();
                 ResultSet rs = statement.executeQuery(LIST_ALL_SQL))
            {
                while (rs.next())
                {
                    rows.add(new DocumentRecord(
                        rs.getString("document_id"),
                        rs.getString("document_type"),
                        rs.getString("reference_number"),
                        rs.getDate("document_date") == null ? null : rs.getDate("document_date").toLocalDate(),
                        rs.getString("file_name"),
                        rs.getString("notes"),
                        java.util.Map.of()
                    ));
                }
            }
            return rows;
        }
    }

    private void ensureTable(Connection c) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement(CREATE_SQL))
        {
            ps.execute();
        }
    }
}
