package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.db.DatabaseManager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for managing {@link Budget} data.
 * This class persists {@link Budget} instances using JPA via {@link DatabaseManager}.
 * Legacy references to JSON files remain for backward compatibility but are no longer used.
 */
public class BudgetService {

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(BudgetService.class.getName());
    /** No-op filename constant retained for backward compatibility. */
    private static final String BUDGETS_FILENAME = "budgets.json";

    /**
     * Persists the provided budgets using JPA. Existing records are updated and new
     * ones are inserted.
     *
     * @param budgets budgets to persist, ignored if {@code null}
     */
    public void saveBudgets(List<Budget> budgets) {
        if (budgets == null) {
            LOGGER.warning("Budget list provided is null. Nothing to save.");
            return;
        }
        EntityManager em = DatabaseManager.getEntityManager();
        try {
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
        }
    }

    /**
     * Retrieves all budgets from the database.
     *
     * @return list of persisted budgets
     */
    public List<Budget> loadBudgets() {
        EntityManager em = DatabaseManager.getEntityManager();
        try {
            TypedQuery<Budget> q = em.createQuery("SELECT b FROM Budget b", Budget.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
