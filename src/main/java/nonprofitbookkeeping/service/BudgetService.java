
package nonprofitbookkeeping.service;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;

import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.persistence.DocumentRepository;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for managing {@link Budget} data.
 * Budget collections are persisted as JSON payloads inside the H2 database rather than in
 * ad-hoc {@code budgets.json} files on disk.
 */
public class BudgetService
{
	
	/** Logger for this class. */
	private static final Logger LOGGER = Logger.getLogger(BudgetService.class.getName());
	/** The standard filename used for storing budget data in JSON format. */
        private static final String DOCUMENT_NAME = "budgets";
        private static final ObjectMapper MAPPER = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setDefaultPrettyPrinter(new DefaultPrettyPrinter());
        private static final CollectionType LIST_TYPE =
                MAPPER.getTypeFactory().constructCollectionType(List.class, Budget.class);
	
        /**
         * Saves a list of {@link Budget} objects to the persistent document store.
         * If the provided list of budgets is null, nothing is persisted.
         *
         * @param budgets The list of {@link Budget} objects to save. Can be null or empty.
         * @param companyDirectory retained for backwards compatibility but ignored by the method.
         * @throws IOException if the underlying database write fails.
         */
        public static void saveBudgets(List<Budget> budgets, File companyDirectory) throws IOException
        {

                if (budgets == null)
                {
                        LOGGER.warning("Budget list provided is null. Nothing to save.");
                        return;
                }

                try
                {
                        String payload = MAPPER.writeValueAsString(budgets);
                        new DocumentRepository().upsert(DOCUMENT_NAME, payload);
                        LOGGER.info("Budgets saved successfully to database document '" + DOCUMENT_NAME + "'.");
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to save budgets to database", e);
                }

        }
	
        /**
         * Loads a list of {@link Budget} objects from the persistent document store.
         *
         * @param companyDirectory retained for backwards compatibility but ignored by the method.
         * @return a list of budgets stored in the database; an empty list is returned when no data exists
         *         or if deserialization fails.
         * @throws IOException if the underlying database access fails.
         */
        public List<Budget> loadBudgets(File companyDirectory) throws IOException
        {

                try
                {
                        return new DocumentRepository().find(DOCUMENT_NAME)
                                .map(payload -> {
                                        try
                                        {
                                                List<Budget> loaded = MAPPER.readValue(payload, LIST_TYPE);
                                                LOGGER.info("Budgets loaded successfully from database document '"
                                                        + DOCUMENT_NAME + "'.");
                                                return loaded;
                                        }
                                        catch (IOException ex)
                                        {
                                                LOGGER.log(Level.SEVERE,
                                                        "Failed to deserialize budgets JSON from database. Returning empty list.",
                                                        ex);
                                                return new ArrayList<Budget>();
                                        }
                                })
                                .orElseGet(ArrayList::new);
                }
                catch (SQLException e)
                {
                        throw new IOException("Failed to load budgets from database", e);
                }

        }

}
