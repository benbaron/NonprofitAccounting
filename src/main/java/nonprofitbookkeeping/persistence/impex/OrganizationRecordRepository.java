package nonprofitbookkeeping.persistence.impex;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.OrganizationRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Persists imported SCLX organization records into a concrete staging table.
 */
@ApplicationScoped
public class OrganizationRecordRepository
{
    private static final String CREATE_SQL = """
        CREATE TABLE IF NOT EXISTS imported_organization_record (
            organization_id VARCHAR(255) PRIMARY KEY,
            name VARCHAR(512) NOT NULL,
            parent_organization VARCHAR(255),
            base_currency VARCHAR(32),
            fiscal_year_start DATE,
            fiscal_year_end DATE,
            extensions_json CLOB
        )
        """;

    private static final String UPSERT_SQL = """
        MERGE INTO imported_organization_record(
            organization_id, name, parent_organization, base_currency,
            fiscal_year_start, fiscal_year_end, extensions_json
        ) KEY(organization_id)
        VALUES(?,?,?,?,?,?,?)
        """;

    public void upsert(OrganizationRecord row) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
            {
                int i = 0;
                ps.setString(++i, row.organizationId());
                ps.setString(++i, row.name());
                ps.setString(++i, row.parentOrganization());
                ps.setString(++i, row.baseCurrency());
                ps.setDate(++i, row.fiscalYearStart() == null ? null : Date.valueOf(row.fiscalYearStart()));
                ps.setDate(++i, row.fiscalYearEnd() == null ? null : Date.valueOf(row.fiscalYearEnd()));
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
