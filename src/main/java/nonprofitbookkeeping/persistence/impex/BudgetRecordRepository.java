package nonprofitbookkeeping.persistence.impex;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.BudgetRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Persists imported SCLX budget records into a concrete staging table.
 */
@ApplicationScoped
public class BudgetRecordRepository
{
    private static final String CREATE_SQL = """
        CREATE TABLE IF NOT EXISTS imported_budget_record (
            budget_id VARCHAR(255) PRIMARY KEY,
            name VARCHAR(500) NOT NULL,
            fiscal_year INT NOT NULL,
            fund_id VARCHAR(255) NOT NULL,
            is_active BOOLEAN NOT NULL,
            description CLOB,
            lines_json CLOB,
            extensions_json CLOB
        )
        """;

    private static final String UPSERT_SQL = """
        MERGE INTO imported_budget_record(
            budget_id, name, fiscal_year, fund_id, is_active, description, lines_json, extensions_json
        ) KEY(budget_id)
        VALUES(?,?,?,?,?,?,?,?)
        """;

    public void upsert(BudgetRecord row) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            ensureTable(c);
            try (PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
            {
                int i = 0;
                ps.setString(++i, row.budgetId());
                ps.setString(++i, row.name());
                ps.setInt(++i, row.fiscalYear());
                ps.setString(++i, row.fundId());
                ps.setBoolean(++i, row.active());
                ps.setString(++i, row.description());
                ps.setString(++i, JsonColumnCodec.toJson(row.lines()));
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
