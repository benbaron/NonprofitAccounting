package nonprofitbookkeeping.persistence.impex;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.DocumentRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    private void ensureTable(Connection c) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement(CREATE_SQL))
        {
            ps.execute();
        }
    }
}
