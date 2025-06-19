package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for managing {@link Budget} data.
 * This class provides functionalities to persist budgets using an embedded SQL
 * database via {@link DatabaseManager}. Previous JSON-based persistence has
 * been replaced with SQL operations.
 */
public class BudgetService {

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(BudgetService.class.getName());
    // No longer used: budgets were previously stored in a JSON file

    /**
     * Persists a list of budgets to the database. Existing rows with the same
     * budget ID are replaced.
     *
     * @param budgets The list of budgets to save.
     * @param companyDirectory Unused but kept for API compatibility.
     */
    public void saveBudgets(List<Budget> budgets, File companyDirectory) throws IOException {
        if (budgets == null) {
            LOGGER.warning("Budget list provided is null. Nothing to save.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement upsert = conn.prepareStatement(
                     "MERGE INTO budget(budget_id,budget_name,fiscal_year,description,currency,applicable_fund_id) KEY(budget_id) VALUES(?,?,?,?,?,?)");
             PreparedStatement deleteLines = conn.prepareStatement("DELETE FROM budget_line WHERE budget_id=?");
             PreparedStatement insertLine = conn.prepareStatement(
                     "INSERT INTO budget_line(budget_id,account_id,account_name,total_amount,periodicity,fund_id) VALUES(?,?,?,?,?,?)")) {
            conn.setAutoCommit(false);
            for (Budget b : budgets) {
                upsert.setString(1, b.getBudgetId());
                upsert.setString(2, b.getBudgetName());
                upsert.setInt(3, b.getFiscalYear());
                upsert.setString(4, b.getDescription());
                upsert.setString(5, b.getCurrency());
                upsert.setString(6, b.getApplicableFundId());
                upsert.executeUpdate();

                deleteLines.setString(1, b.getBudgetId());
                deleteLines.executeUpdate();

                if (b.getBudgetLines() != null) {
                    for (BudgetLine bl : b.getBudgetLines()) {
                        insertLine.setString(1, b.getBudgetId());
                        insertLine.setString(2, bl.getAccountId());
                        insertLine.setString(3, bl.getAccountName());
                        insertLine.setBigDecimal(4, bl.getTotalAmount());
                        insertLine.setString(5, bl.getPeriodicity() == null ? null : bl.getPeriodicity().name());
                        insertLine.setString(6, bl.getFundId());
                        insertLine.addBatch();
                    }
                    insertLine.executeBatch();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving budgets", e);
        }
    }

    /**
     * Loads all budgets from the database.
     *
     * @param companyDirectory Unused but kept for API compatibility.
     * @return list of budgets from the database.
     */
    public List<Budget> loadBudgets(File companyDirectory) throws IOException {
        List<Budget> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT budget_id,budget_name,fiscal_year,description,currency,applicable_fund_id FROM budget");
             PreparedStatement psLine = conn.prepareStatement(
                     "SELECT account_id,account_name,total_amount,periodicity,fund_id FROM budget_line WHERE budget_id=?")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Budget b = new Budget();
                b.setBudgetId(rs.getString(1));
                b.setBudgetName(rs.getString(2));
                b.setFiscalYear(rs.getInt(3));
                b.setDescription(rs.getString(4));
                b.setCurrency(rs.getString(5));
                b.setApplicableFundId(rs.getString(6));

                psLine.setString(1, b.getBudgetId());
                ResultSet rsLine = psLine.executeQuery();
                List<BudgetLine> lines = new ArrayList<>();
                while (rsLine.next()) {
                    BudgetLine bl = new BudgetLine();
                    bl.setAccountId(rsLine.getString(1));
                    bl.setAccountName(rsLine.getString(2));
                    bl.setTotalAmount(rsLine.getBigDecimal(3));
                    String per = rsLine.getString(4);
                    if (per != null) bl.setPeriodicity(Periodicity.valueOf(per));
                    bl.setFundId(rsLine.getString(5));
                    lines.add(bl);
                }
                b.setBudgetLines(lines);
                list.add(b);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading budgets", e);
        }
        return list;
    }
}
