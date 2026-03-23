package nonprofitbookkeeping.persistence.impex;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.EventRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Persists imported SCLX event records into a concrete staging table.
 */
@ApplicationScoped
public class EventRecordRepository
{
    private static final String CREATE_SQL = """
        CREATE TABLE IF NOT EXISTS imported_event_record (
            event_id VARCHAR(255) PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            start_date DATE,
            end_date DATE,
            hosting_organization_id VARCHAR(255),
            extensions_json CLOB
        )
        """;

    private static final String UPSERT_SQL = """
        MERGE INTO imported_event_record(
            event_id, name, start_date, end_date, hosting_organization_id, extensions_json
        ) KEY(event_id)
        VALUES(?,?,?,?,?,?)
        """;

    public void upsert(EventRecord row) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
            {
                int i = 0;
                ps.setString(++i, row.eventId());
                ps.setString(++i, row.name());
                ps.setDate(++i, row.startDate() == null ? null : Date.valueOf(row.startDate()));
                ps.setDate(++i, row.endDate() == null ? null : Date.valueOf(row.endDate()));
                ps.setString(++i, row.hostingOrganizationId());
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
