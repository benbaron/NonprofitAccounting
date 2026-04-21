package nonprofitbookkeeping.persistence.records;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.records.FundRecord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Persists imported SCLX fund records into a concrete staging table.
 */
@ApplicationScoped
public class FundRecordRepository extends AbstractRepository<FundRecord, String>
{
    private static final String CREATE_SQL = """
        CREATE TABLE IF NOT EXISTS imported_fund_record (
            fund_id VARCHAR(255) PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            restricted BOOLEAN NOT NULL,
            description CLOB,
            extensions_json CLOB
        )
        """;

    private static final String UPSERT_SQL = """
        MERGE INTO imported_fund_record(
            fund_id, name, restricted, description, extensions_json
        ) KEY(fund_id)
        VALUES(?,?,?,?,?)
        """;
    private static final String LIST_ALL_SQL = """
        SELECT fund_id, name, restricted, description
        FROM imported_fund_record
        """;
    private static final String DELETE_SQL = "DELETE FROM imported_fund_record WHERE fund_id = ?";

    public FundRecordRepository()
    {
        super(CREATE_SQL, UPSERT_SQL, LIST_ALL_SQL, DELETE_SQL);
    }

    @Override
    protected void bindUpsert(PreparedStatement ps, FundRecord row) throws SQLException
    {
        int i = 0;
        ps.setString(++i, row.fundId());
        ps.setString(++i, row.name());
        ps.setBoolean(++i, row.restricted());
        ps.setString(++i, row.description());
        ps.setString(++i, JsonColumnCodec.toJson(row.extensions()));
    }

    @Override
    protected FundRecord mapRow(ResultSet rs) throws SQLException
    {
        return new FundRecord(
            rs.getString("fund_id"),
            rs.getString("name"),
            rs.getBoolean("restricted"),
            rs.getString("description"),
            java.util.Map.of()
        );
    }
}
