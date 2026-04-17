package nonprofitbookkeeping.persistence.impex;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.FundRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists imported SCLX fund records into a concrete staging table.
 */
@ApplicationScoped
public class FundRecordRepository
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

    public void upsert(FundRecord row) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
            {
                int i = 0;
                ps.setString(++i, row.fundId());
                ps.setString(++i, row.name());
                ps.setBoolean(++i, row.restricted());
                ps.setString(++i, row.description());
                ps.setString(++i, JsonColumnCodec.toJson(row.extensions()));
                ps.executeUpdate();
            }
        }
    }

    public List<FundRecord> listAll() throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            List<FundRecord> rows = new ArrayList<>();
            try (Statement statement = c.createStatement();
                 ResultSet rs = statement.executeQuery(LIST_ALL_SQL))
            {
                while (rs.next())
                {
                    rows.add(new FundRecord(
                        rs.getString("fund_id"),
                        rs.getString("name"),
                        rs.getBoolean("restricted"),
                        rs.getString("description"),
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
