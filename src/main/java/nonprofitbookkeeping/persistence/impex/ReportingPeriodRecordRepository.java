package nonprofitbookkeeping.persistence.impex;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.ReportingPeriodRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    private void ensureTable(Connection c) throws SQLException
    {
        try (PreparedStatement ps = c.prepareStatement(CREATE_SQL))
        {
            ps.execute();
        }
    }
}
