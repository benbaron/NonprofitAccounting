package nonprofitbookkeeping.persistence.records;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.records.BudgetRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists imported budget records as staged impex rows.
 */
public class BudgetRecordRepository
{
    private static final String DDL_BUDGET = """
        CREATE TABLE IF NOT EXISTS imported_budget (
            budget_id VARCHAR(255) PRIMARY KEY,
            name VARCHAR(255),
            fiscal_year INT,
            fund_id VARCHAR(255),
            active BOOLEAN,
            description CLOB
        )
        """;

    private static final String DDL_LINE = """
        CREATE TABLE IF NOT EXISTS imported_budget_line (
            budget_id VARCHAR(255),
            line_ordinal INT,
            event_name VARCHAR(255),
            budgeted_amount DECIMAL(19,2),
            revenue_category VARCHAR(128),
            expense_category VARCHAR(128),
            account_id VARCHAR(255),
            notes CLOB,
            PRIMARY KEY (budget_id, line_ordinal)
        )
        """;

    private static final String UPSERT_BUDGET = """
        MERGE INTO imported_budget(
            budget_id, name, fiscal_year, fund_id, active, description
        ) KEY(budget_id)
        VALUES(?,?,?,?,?,?)
        """;

    private static final String DELETE_LINES = "DELETE FROM imported_budget_line WHERE budget_id = ?";
    private static final String INSERT_LINE = """
        INSERT INTO imported_budget_line(
            budget_id, line_ordinal, event_name, budgeted_amount, revenue_category,
            expense_category, account_id, notes
        ) VALUES(?,?,?,?,?,?,?,?)
        """;
    private static final String LIST_ALL_BUDGETS = """
        SELECT budget_id, name, fiscal_year, fund_id, active, description
        FROM imported_budget
        """;
    private static final String LIST_LINES_FOR_BUDGET = """
        SELECT event_name, budgeted_amount, revenue_category, expense_category, account_id, notes
        FROM imported_budget_line
        WHERE budget_id = ?
        ORDER BY line_ordinal
        """;

    public void upsert(BudgetRecord record) throws SQLException
    {
        ensureSchema();
        try (Connection c = Database.get().getConnection())
        {
            boolean auto = c.getAutoCommit();
            c.setAutoCommit(false);
            try
            {
                try (PreparedStatement ps = c.prepareStatement(UPSERT_BUDGET))
                {
                    int i = 0;
                    ps.setString(++i, record.budgetId());
                    ps.setString(++i, record.name());
                    if (record.fiscalYear() == null) ps.setNull(++i, java.sql.Types.INTEGER); else ps.setInt(++i, record.fiscalYear());
                    ps.setString(++i, record.fundId());
                    if (record.active() == null) ps.setNull(++i, java.sql.Types.BOOLEAN); else ps.setBoolean(++i, record.active());
                    ps.setString(++i, record.description());
                    ps.executeUpdate();
                }

                try (PreparedStatement del = c.prepareStatement(DELETE_LINES))
                {
                    del.setString(1, record.budgetId());
                    del.executeUpdate();
                }

                if (record.lines() != null && !record.lines().isEmpty())
                {
                    try (PreparedStatement ins = c.prepareStatement(INSERT_LINE))
                    {
                        int ordinal = 0;
                        for (BudgetRecord.BudgetLineRecord line : record.lines())
                        {
                            ins.setString(1, record.budgetId());
                            ins.setInt(2, ordinal++);
                            ins.setString(3, line.eventName());
                            ins.setBigDecimal(4, line.budgetedAmount());
                            ins.setString(5, line.revenueCategory());
                            ins.setString(6, line.expenseCategory());
                            ins.setString(7, line.accountId());
                            ins.setString(8, line.notes());
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }
                c.commit();
            }
            catch (SQLException ex)
            {
                c.rollback();
                throw ex;
            }
            finally
            {
                c.setAutoCommit(auto);
            }
        }
    }

    public List<BudgetRecord> listAll() throws SQLException
    {
        ensureSchema();
        List<BudgetRecord> rows = new ArrayList<>();
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(LIST_ALL_BUDGETS);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                String budgetId = rs.getString("budget_id");
                List<BudgetRecord.BudgetLineRecord> lines = listLines(c, budgetId);
                rows.add(new BudgetRecord(
                    budgetId,
                    rs.getString("name"),
                    rs.getObject("fiscal_year", Integer.class),
                    rs.getString("fund_id"),
                    rs.getObject("active", Boolean.class),
                    rs.getString("description"),
                    lines,
                    java.util.Map.of(),
                    null
                ));
            }
        }
        return rows;
    }

    private List<BudgetRecord.BudgetLineRecord> listLines(Connection c, String budgetId) throws SQLException
    {
        List<BudgetRecord.BudgetLineRecord> lines = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(LIST_LINES_FOR_BUDGET))
        {
            ps.setString(1, budgetId);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    lines.add(new BudgetRecord.BudgetLineRecord(
                        rs.getString("event_name"),
                        rs.getBigDecimal("budgeted_amount"),
                        rs.getString("revenue_category"),
                        rs.getString("expense_category"),
                        rs.getString("account_id"),
                        rs.getString("notes"),
                        java.util.Map.of()
                    ));
                }
            }
        }
        return lines;
    }

    private void ensureSchema() throws SQLException
    {
        try (Connection c = Database.get().getConnection();
             Statement st = c.createStatement())
        {
            st.execute(DDL_BUDGET);
            st.execute(DDL_LINE);
        }
    }
}
