
package nonprofitbookkeeping.service;

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
import nonprofitbookkeeping.dao.BudgetDao;
import java.sql.SQLException;

=======
>>>>>>> 61e85fc Implement JPA persistence for budgets
=======
>>>>>>> b1f07f2 Extend SQL support
import nonprofitbookkeeping.model.budget.Budget;
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
import nonprofitbookkeeping.db.DatabaseManager;
=======
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
>>>>>>> b1f07f2 Extend SQL support

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for managing {@link Budget} data.
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
 * This class provides functionalities to persist budgets using a database
 * located inside the company's directory.
=======
 * This class persists {@link Budget} instances using JPA via {@link DatabaseManager}.
 * Legacy references to JSON files remain for backward compatibility but are no longer used.
>>>>>>> 61e85fc Implement JPA persistence for budgets
=======
 * This class provides functionalities to persist budgets using an embedded SQL
 * database via {@link DatabaseManager}. Previous JSON-based persistence has
 * been replaced with SQL operations.
>>>>>>> b1f07f2 Extend SQL support
 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
public class BudgetService
{
	
	/** Logger for this class. */
	private static final Logger LOGGER = Logger.getLogger(BudgetService.class.getName());
	/** The standard filename used for storing budget data in JSON format. */
	private static final String BUDGETS_FILENAME = "budgets.json";
	
	/**
	 * Saves a list of {@link Budget} objects to a JSON file named "budgets.json"
	 * within the specified company directory.
	 * If the provided list of budgets is null, no file will be written.
	 * If the list is empty, an empty JSON array will be saved.
	 * The JSON output is pretty-printed for readability.
	 *
	 * @param budgets The list of {@link Budget} objects to save. Can be null or empty.
	 * @param companyDirectory The {@link File} object representing the directory where the
	 *                         company's data (including the budgets file) should be stored.
	 *                         Must not be null and must be a valid directory.
	 * @throws IOException If the {@code companyDirectory} is invalid, or if an error occurs
	 *                     during file writing or JSON serialization.
	 */
	public static void saveBudgets(List<Budget> budgets, File companyDirectory) throws IOException
	{
		
		if (budgets == null)
		{
			LOGGER.warning("Budget list provided is null. Nothing to save.");
			// Optionally, save an empty list or throw IllegalArgumentException
			// For now, let's just not write the file if the list is null.
			// If an empty list is provided, an empty JSON array will be saved.
			return;
		}
		
		if (companyDirectory == null || !companyDirectory.isDirectory())
		{
			throw new IOException("Company directory is invalid or not provided.");
		}
		
		File budgetsFile = new File(companyDirectory, BUDGETS_FILENAME);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // For pretty printing
		objectMapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
		
		
		try
		{
			// Ensure parent directory exists (though companyDirectory should already exist)
			// Files.createDirectories(budgetsFile.getParentFile().toPath());
			objectMapper.writeValue(budgetsFile, budgets);
			LOGGER.info("Budgets saved successfully to: " + budgetsFile.getAbsolutePath());
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE, "Failed to save budgets to " + budgetsFile.getAbsolutePath(),
				e);
			throw e; // Re-throw to allow caller to handle
		}
		
	}
	
	/**
	 * Loads a list of {@link Budget} objects from a JSON file named "budgets.json"
	 * located within the specified company directory.
	 * <p>
	 * If the {@code companyDirectory} is invalid, or if the "budgets.json" file does not exist,
	 * is not a file, or is empty, an empty list is returned and appropriate messages are logged.
	 * If an error occurs during JSON deserialization, an error is logged, and an empty list is returned.
	 * </p>
	 *
	 * @param companyDirectory The {@link File} object representing the directory where the
	 *                         company's "budgets.json" file is located. Must not be null and
	 *                         must be a valid directory.
	 * @return A {@code List<Budget>} objects. Returns an empty list if the file doesn't exist,
	 *         is empty, or if there's an error during deserialization (after logging the error).
	 * @throws IOException If a critical I/O error occurs that prevents determining the file status
	 *                     (other than file not found or parse error, which result in an empty list).
	 *                     Note: The current implementation catches most IOExceptions from Jackson and returns
	 *                     an empty list, so this specific throws declaration might only cover edge cases
	 *                     related to {@code companyDirectory} validation if it were to throw IOException directly.
	 */
	public List<Budget> loadBudgets(File companyDirectory) throws IOException
	{
		
		if (companyDirectory == null || !companyDirectory.isDirectory())
		{
			LOGGER.warning("Company directory is invalid or not provided for loading budgets.");
			return new ArrayList<>(); // Or throw new IOException("Company directory is invalid.");
		}
		
		File budgetsFile = new File(companyDirectory, BUDGETS_FILENAME);
		
		if (!budgetsFile.exists() || !budgetsFile.isFile())
		{
			LOGGER.info("Budgets file not found at: " + budgetsFile.getAbsolutePath() +
				". Returning empty list.");
			return new ArrayList<>();
		}
		
		if (budgetsFile.length() == 0)
		{
			LOGGER.info("Budgets file is empty at: " + budgetsFile.getAbsolutePath() +
				". Returning empty list.");
			return new ArrayList<>();
		}
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		try
		{
			CollectionType listType =
				objectMapper.getTypeFactory().constructCollectionType(List.class, Budget.class);
			List<Budget> loadedBudgets = objectMapper.readValue(budgetsFile, listType);
			LOGGER.info("Budgets loaded successfully from: " + budgetsFile.getAbsolutePath());
			// The getBudgetId() method in Budget model ensures ID is generated if null
			// after deserialization.
			return loadedBudgets != null ? loadedBudgets : new ArrayList<>();
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE, "Failed to load or parse budgets from " +
				budgetsFile.getAbsolutePath() + ". Returning empty list.", e);
			// Depending on policy, might re-throw for certain IOExceptions vs returning
			// empty for parse errors
			return new ArrayList<>();
		}
		
	}
	
=======
public class BudgetService {

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(BudgetService.class.getName());
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
    /** Data access object used for persistence. */
    private final BudgetDao budgetDao = new BudgetDao();
=======
    /** No-op filename constant retained for backward compatibility. */
    private static final String BUDGETS_FILENAME = "budgets.json";
>>>>>>> 61e85fc Implement JPA persistence for budgets
=======
    // No longer used: budgets were previously stored in a JSON file
>>>>>>> b1f07f2 Extend SQL support

    /**
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
     * Saves a list of {@link Budget} objects to the database located in the
     * provided company directory. Existing budgets are replaced.
=======
     * Persists the provided budgets using JPA. Existing records are updated and new
     * ones are inserted.
>>>>>>> 61e85fc Implement JPA persistence for budgets
=======
     * Persists a list of budgets to the database. Existing rows with the same
     * budget ID are replaced.
>>>>>>> b1f07f2 Extend SQL support
     *
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
     * @param budgets The list of {@link Budget} objects to save. Can be null or empty.
     * @param companyDirectory The {@link File} object representing the directory where the
     *                         company's database is stored.
     *                         Must not be null and must be a valid directory.
     * @throws IOException If persistence fails or the directory is invalid.
=======
     * @param budgets budgets to persist, ignored if {@code null}
>>>>>>> 61e85fc Implement JPA persistence for budgets
=======
     * @param budgets The list of budgets to save.
     * @param companyDirectory Unused but kept for API compatibility.
>>>>>>> b1f07f2 Extend SQL support
     */
    public void saveBudgets(List<Budget> budgets) {
        if (budgets == null) {
            LOGGER.warning("Budget list provided is null. Nothing to save.");
            return;
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        }
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            throw new IOException("Company directory is invalid or not provided.");
=======
>>>>>>> b1f07f2 Extend SQL support
        }

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
=======
        EntityManager em = DatabaseManager.getEntityManager();
>>>>>>> 61e85fc Implement JPA persistence for budgets
        try {
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
            this.budgetDao.clearBudgets(companyDirectory);
            for (Budget budget : budgets) {
                this.budgetDao.saveBudget(budget, companyDirectory);
            }
            LOGGER.info("Budgets saved successfully to database in " + companyDirectory.getAbsolutePath());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save budgets to database", e);
            throw new IOException("Database error while saving budgets", e);
=======
            em.getTransaction().begin();
            for (Budget b : budgets) {
                em.merge(b);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to persist budgets", e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
>>>>>>> 61e85fc Implement JPA persistence for budgets
=======
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
>>>>>>> b1f07f2 Extend SQL support
        }
    }

    /**
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
     * Loads all {@link Budget} objects from the database located in the
     * specified company directory. If the directory is invalid or no database
     * exists, an empty list is returned.
=======
     * Retrieves all budgets from the database.
>>>>>>> 61e85fc Implement JPA persistence for budgets
=======
     * Loads all budgets from the database.
>>>>>>> b1f07f2 Extend SQL support
     *
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
     * @param companyDirectory The {@link File} object representing the directory where the
     *                         company's database is located. Must not be null and
     *                         must be a valid directory.
     * @return A {@code List<Budget>} loaded from the database. Returns an empty
     *         list on error or if none exist.
     * @throws IOException If a database error occurs.
=======
     * @return list of persisted budgets
>>>>>>> 61e85fc Implement JPA persistence for budgets
=======
     * @param companyDirectory Unused but kept for API compatibility.
     * @return list of budgets from the database.
>>>>>>> b1f07f2 Extend SQL support
     */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
    public List<Budget> loadBudgets(File companyDirectory) throws IOException {
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            LOGGER.warning("Company directory is invalid or not provided for loading budgets.");
            return new ArrayList<>();
=======
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
>>>>>>> b1f07f2 Extend SQL support
        }
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file

=======
    public List<Budget> loadBudgets() {
        EntityManager em = DatabaseManager.getEntityManager();
>>>>>>> 61e85fc Implement JPA persistence for budgets
        try {
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
            return this.budgetDao.getAllBudgets(companyDirectory);
        } catch (SQLException | IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load budgets from database", e);
            return new ArrayList<>();
=======
            TypedQuery<Budget> q = em.createQuery("SELECT b FROM Budget b", Budget.class);
            return q.getResultList();
        } finally {
            em.close();
>>>>>>> 61e85fc Implement JPA persistence for budgets
        }
=======
        return list;
>>>>>>> b1f07f2 Extend SQL support
    }
>>>>>>> 734695e Add database persistence for budgets
}
