package nonprofitbookkeeping.persistence.impex;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.impex.BudgetRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BudgetRecordRepository
{
    public BudgetRecordRepository()
    {
        try
        {
            ensureSchema();
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Failed to initialize budget_record table", ex);
        }
    }

    public void upsert(BudgetRecord row) throws SQLException
    {
        String sql =
            "MERGE INTO budget_record(budget_id, name, fiscal_year, fund_id, active, description, raw_json) " +
            "KEY(budget_id) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            int i = 0;
            ps.setString(++i, row.budgetId());
            ps.setString(++i, row.name());
            if (row.fiscalYear() == null) ps.setNull(++i, java.sql.Types.INTEGER); else ps.setInt(++i, row.fiscalYear());
            ps.setString(++i, row.fundId());
            if (row.active() == null) ps.setNull(++i, java.sql.Types.BOOLEAN); else ps.setBoolean(++i, row.active());
            ps.setString(++i, row.description());
            ps.setString(++i, row.rawJson());
            ps.executeUpdate();
        }
    }

    public List<BudgetRecord> listAll() throws SQLException
    {
        List<BudgetRecord> rows = new ArrayList<>();
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM budget_record ORDER BY fiscal_year, name, budget_id");
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                Integer fiscalYear = rs.getInt("fiscal_year");
                if (rs.wasNull()) fiscalYear = null;
                Boolean active = rs.getBoolean("active");
                if (rs.wasNull()) active = null;
                rows.add(new BudgetRecord(
                    rs.getString("budget_id"),
                    rs.getString("name"),
                    fiscalYear,
                    rs.getString("fund_id"),
                    active,
                    rs.getString("description"),
                    java.util.List.of(),
                    java.util.Map.of(),
                    rs.getString("raw_json")
                ));
            }
        }
        return rows;
    }

    private void ensureSchema() throws SQLException
    {
        String sql = """
            CREATE TABLE IF NOT EXISTS budget_record(
                budget_id VARCHAR(255) PRIMARY KEY,
                name VARCHAR(255),
                fiscal_year INTEGER,
                fund_id VARCHAR(255),
                active BOOLEAN,
                description CLOB,
                raw_json CLOB
            )
            """;
        try (Connection c = Database.get().getConnection();
             Statement st = c.createStatement())
        {
            st.execute(sql);
        }
    }
}
