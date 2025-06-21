package nonprofitbookkeeping.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import nonprofitbookkeeping.db.DatabaseManager;
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object handling persistence of {@link Budget} and
 * {@link BudgetLine} records using SQLite via {@link DatabaseManager}.
 */
public class BudgetDao {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Inserts or updates the given budget and its lines.
     */
    public void saveBudget(Budget budget, File companyDirectory) throws SQLException, IOException {
        try (Connection conn = DatabaseManager.getConnection(companyDirectory)) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT OR REPLACE INTO budgets (budget_id,budget_name,fiscal_year,description,currency,applicable_fund_id)" +
                        " VALUES(?,?,?,?,?,?)")) {
                    ps.setString(1, budget.getBudgetId());
                    ps.setString(2, budget.getBudgetName());
                    ps.setInt(3, budget.getFiscalYear());
                    ps.setString(4, budget.getDescription());
                    ps.setString(5, budget.getCurrency());
                    ps.setString(6, budget.getApplicableFundId());
                    ps.executeUpdate();
                }
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM budget_lines WHERE budget_id = ?")) {
                    del.setString(1, budget.getBudgetId());
                    del.executeUpdate();
                }
                if (budget.getBudgetLines() != null) {
                    for (BudgetLine line : budget.getBudgetLines()) {
                        insertBudgetLine(conn, budget.getBudgetId(), line);
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Deletes all budgets and lines. */
    public void clearBudgets(File companyDirectory) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(companyDirectory);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM budget_lines");
            stmt.executeUpdate("DELETE FROM budgets");
        }
    }

    /** Retrieves all budgets with their lines. */
    public List<Budget> getAllBudgets(File companyDirectory) throws SQLException, IOException {
        List<Budget> budgets = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(companyDirectory);
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM budgets")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    budgets.add(loadBudget(conn, rs.getString("budget_id"), rs));
                }
            }
        }
        return budgets;
    }

    /** Retrieves a single budget by ID. */
    public Budget getBudget(String budgetId, File companyDirectory) throws SQLException, IOException {
        try (Connection conn = DatabaseManager.getConnection(companyDirectory)) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM budgets WHERE budget_id=?")) {
                ps.setString(1, budgetId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return loadBudget(conn, budgetId, rs);
                    }
                }
        }
        return null;
        }
    }

    /** Deletes a budget by ID. */
    public void deleteBudget(String budgetId, File companyDirectory) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(companyDirectory)) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM budget_lines WHERE budget_id=?")) {
                    ps.setString(1, budgetId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM budgets WHERE budget_id=?")) {
                    ps.setString(1, budgetId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private Budget loadBudget(Connection conn, String budgetId, ResultSet rs) throws SQLException, IOException {
        Budget budget = new Budget(budgetId, 0);
        budget.setBudgetId(budgetId);
        budget.setBudgetName(rs.getString("budget_name"));
        budget.setFiscalYear(rs.getInt("fiscal_year"));
        budget.setDescription(rs.getString("description"));
        budget.setCurrency(rs.getString("currency"));
        budget.setApplicableFundId(rs.getString("applicable_fund_id"));
        budget.setBudgetLines(loadLines(conn, budgetId));
        return budget;
    }

    private List<BudgetLine> loadLines(Connection conn, String budgetId) throws SQLException, IOException {
        List<BudgetLine> lines = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM budget_lines WHERE budget_id=?")) {
            ps.setString(1, budgetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BudgetLine line = new BudgetLine();
                    line.setAccountId(rs.getString("account_id"));
                    line.setAccountName(rs.getString("account_name"));
                    String amtStr = rs.getString("total_budgeted_amount");
                    line.setTotalBudgetedAmount(amtStr == null ? null : new BigDecimal(amtStr));
                    line.setPeriodicity(nonNullEnum(rs.getString("periodicity")));
                    line.setFundId(rs.getString("fund_id"));
                    String pa = rs.getString("periodic_amounts");
                    if (pa != null && !pa.isEmpty()) {
                        BigDecimal[] arr = mapper.readValue(pa, BigDecimal[].class);
                        List<BigDecimal> list = new ArrayList<>();
                        for (BigDecimal bd : arr) list.add(bd);
                        line.setPeriodicAmounts(list);
                    }
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private void insertBudgetLine(Connection conn, String budgetId, BudgetLine line) throws SQLException, IOException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO budget_lines (budget_id,account_id,account_name,total_budgeted_amount,periodicity,periodic_amounts,fund_id)" +
                " VALUES(?,?,?,?,?,?,?)")) {
            ps.setString(1, budgetId);
            ps.setString(2, line.getAccountId());
            ps.setString(3, line.getAccountName());
            ps.setString(4, line.getTotalBudgetedAmount() == null ? null : line.getTotalBudgetedAmount().toPlainString());
            ps.setString(5, line.getPeriodicity() == null ? null : line.getPeriodicity().name());
            ps.setString(6, line.getPeriodicAmounts() == null || line.getPeriodicAmounts().isEmpty() ? null : mapper.writeValueAsString(line.getPeriodicAmounts()));
            ps.setString(7, line.getFundId());
            ps.executeUpdate();
        }
    }

    private Periodicity nonNullEnum(String name) {
        if (name == null) return null;
        try {
            return Periodicity.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
