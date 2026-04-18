package nonprofitbookkeeping.persistence.records;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.records.ReportingPeriodRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists imported SCLX reporting period records into a concrete staging table.
 */
@ApplicationScoped
public class ReportingPeriodRecordRepository
{
    private static final String CREATE_SQL = """
        CREATE TABLE IF NOT EXISTS imported_reporting_period_record (
            period_key VARCHAR(255) PRIMARY KEY,
            start_date DATE NOT NULL,
            end_date DATE NOT NULL,
            label VARCHAR(255),
            fiscal_year INTEGER,
            period_type VARCHAR(64),
            extensions_json CLOB
        )
        """;

    private static final String UPSERT_SQL = """
        MERGE INTO imported_reporting_period_record(
            period_key, start_date, end_date, label, fiscal_year, period_type, extensions_json
        ) KEY(period_key)
        VALUES(?,?,?,?,?,?,?)
        """;
    private static final String LIST_ALL_SQL = """
        SELECT start_date, end_date, label, fiscal_year, period_type
        FROM imported_reporting_period_record
        """;
    private static final String DELETE_SQL = "DELETE FROM imported_reporting_period_record WHERE period_key = ?";

    public void upsert(ReportingPeriodRecord row) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
            {
                int i = 0;
                String periodKey = row.startDate() + ":" + row.endDate() + ":" + String.valueOf(row.label());
                ps.setString(++i, periodKey);
                ps.setDate(++i, Date.valueOf(row.startDate()));
                ps.setDate(++i, Date.valueOf(row.endDate()));
                ps.setString(++i, row.label());
                if (row.fiscalYear() == null) {
                    ps.setObject(++i, null);
                } else {
                    ps.setInt(++i, row.fiscalYear());
                }
                ps.setString(++i, row.periodType());
                ps.setString(++i, JsonColumnCodec.toJson(row.extensions()));
                ps.executeUpdate();
            }
        }
    }

    public List<ReportingPeriodRecord> listAll() throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            List<ReportingPeriodRecord> rows = new ArrayList<>();
            try (Statement statement = c.createStatement();
                 ResultSet rs = statement.executeQuery(LIST_ALL_SQL))
            {
                while (rs.next())
                {
                    rows.add(new ReportingPeriodRecord(
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getString("label"),
                        rs.getObject("fiscal_year", Integer.class),
                        rs.getString("period_type"),
                        java.util.Map.of()
                    ));
                }
            }
            return rows;
        }
    }

    public int delete(ReportingPeriodRecord row) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(DELETE_SQL))
            {
                String periodKey = row.startDate() + ":" + row.endDate() + ":" + String.valueOf(row.label());
                ps.setString(1, periodKey);
                return ps.executeUpdate();
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
