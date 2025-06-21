package nonprofitbookkeeping.service;


import nonprofitbookkeeping.dao.BudgetDao;
import java.sql.SQLException;

import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.db.DatabaseManager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for managing {@link Budget} data.

 * This class provides functionalities to persist budgets using a database
 * located inside the company's directory.

 */
public class BudgetService {

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(BudgetService.class.getName());

    /** Data access object used for persistence. */
    private final BudgetDao budgetDao = new BudgetDao();

    /**
     * Saves a list of {@link Budget} objects to the database located in the
     * provided company directory. Existing budgets are replaced.
     *
     * @param budgets The list of {@link Budget} objects to save. Can be null or empty.
     * @param companyDirectory 
     * @param companyDirectory The {@link File} object representing the directory where the
     *                         company's database is stored.
     *                         Must not be null and must be a valid directory.
     * @throws IOException If persistence fails or the directory is invalid.

     */
    public void saveBudgets(List<Budget> budgets, File companyDirectory) throws IOException {
        if (budgets == null) {
            LOGGER.warning("Budget list provided is null. Nothing to save.");
            return;

        }
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            throw new IOException("Company directory is invalid or not provided.");
        }

        try {
            this.budgetDao.clearBudgets(companyDirectory);
            for (Budget budget : budgets) {
                this.budgetDao.saveBudget(budget, companyDirectory);
            }
            LOGGER.info("Budgets saved successfully to database in " + companyDirectory.getAbsolutePath());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save budgets to database", e);
            throw new IOException("Database error while saving budgets", e);
        }
    }

    /**
     * Loads all {@link Budget} objects from the database located in the
     * specified company directory. If the directory is invalid or no database
     * exists, an empty list is returned.
     *
     * @param companyDirectory The {@link File} object representing the directory where the
     *                         company's database is located. Must not be null and
     *                         must be a valid directory.
     * @return A {@code List<Budget>} loaded from the database. Returns an empty
     *         list on error or if none exist.
     * @throws IOException If a database error occurs.
     */
    public List<Budget> loadBudgets(File companyDirectory) throws IOException {
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            LOGGER.warning("Company directory is invalid or not provided for loading budgets.");
            return new ArrayList<>();
        }

        try {
            return this.budgetDao.getAllBudgets(companyDirectory);
        } catch (SQLException | IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load budgets from database", e);
            return new ArrayList<>();

        }
    }
}
