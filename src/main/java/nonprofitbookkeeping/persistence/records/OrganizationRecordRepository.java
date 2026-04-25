package nonprofitbookkeeping.persistence.records;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.records.OrganizationRecord;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Persists imported SCLX organization records into a concrete staging table.
 */
@ApplicationScoped
public class OrganizationRecordRepository extends AbstractRepository<OrganizationRecord, String>
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
    private static final String LIST_ALL_SQL = """
        SELECT organization_id, name, parent_organization, base_currency, fiscal_year_start, fiscal_year_end
        FROM imported_organization_record
        """;
    private static final String DELETE_SQL = "DELETE FROM imported_organization_record WHERE organization_id = ?";

    public OrganizationRecordRepository()
    {
        super(CREATE_SQL, UPSERT_SQL, LIST_ALL_SQL, DELETE_SQL);
    }

    @Override
    protected void bindUpsert(PreparedStatement ps, OrganizationRecord row) throws SQLException
    {
        int i = 0;
        ps.setString(++i, row.organizationId());
        ps.setString(++i, row.name());
        ps.setString(++i, row.parentOrganization());
        ps.setString(++i, row.baseCurrency());
        ps.setDate(++i, row.fiscalYearStart() == null ? null : Date.valueOf(row.fiscalYearStart()));
        ps.setDate(++i, row.fiscalYearEnd() == null ? null : Date.valueOf(row.fiscalYearEnd()));
        ps.setString(++i, JsonColumnCodec.toJson(row.extensions()));
    }

    @Override
    protected OrganizationRecord mapRow(ResultSet rs) throws SQLException
    {
        return new OrganizationRecord(
            rs.getString("organization_id"),
            rs.getString("name"),
            rs.getString("parent_organization"),
            rs.getString("base_currency"),
            rs.getDate("fiscal_year_start") == null ? null : rs.getDate("fiscal_year_start").toLocalDate(),
            rs.getDate("fiscal_year_end") == null ? null : rs.getDate("fiscal_year_end").toLocalDate(),
            java.util.Map.of()
        );
    }
}
