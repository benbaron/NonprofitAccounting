package nonprofitbookkeeping.persistence.records;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.records.EventRecord;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Persists imported SCLX event records into a concrete staging table.
 */
@ApplicationScoped
public class EventRecordRepository extends AbstractRepository<EventRecord, String>
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
    private static final String LIST_ALL_SQL = """
        SELECT event_id, name, start_date, end_date, hosting_organization_id
        FROM imported_event_record
        """;
    private static final String DELETE_SQL = "DELETE FROM imported_event_record WHERE event_id = ?";

    public EventRecordRepository()
    {
        super(CREATE_SQL, UPSERT_SQL, LIST_ALL_SQL, DELETE_SQL);
    }

    @Override
    protected void bindUpsert(PreparedStatement ps, EventRecord row) throws SQLException
    {
        int i = 0;
        ps.setString(++i, row.eventId());
        ps.setString(++i, row.name());
        ps.setDate(++i, row.startDate() == null ? null : Date.valueOf(row.startDate()));
        ps.setDate(++i, row.endDate() == null ? null : Date.valueOf(row.endDate()));
        ps.setString(++i, row.hostingOrganizationId());
        ps.setString(++i, JsonColumnCodec.toJson(row.extensions()));
    }

    @Override
    protected EventRecord mapRow(ResultSet rs) throws SQLException
    {
        return new EventRecord(
            rs.getString("event_id"),
            rs.getString("name"),
            rs.getDate("start_date") == null ? null : rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date") == null ? null : rs.getDate("end_date").toLocalDate(),
            rs.getString("hosting_organization_id"),
            java.util.Map.of()
        );
    }
}
