package nonprofitbookkeeping.service;

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
 * Service class for managing budgets persisted in the shared H2 database.
 */
public class BudgetService {
    private static final Logger LOGGER = Logger.getLogger(BudgetService.class.getName());
    private static final String DOCUMENT_NAME = "budgets";
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private static final CollectionType LIST_TYPE =
            MAPPER.getTypeFactory().constructCollectionType(List.class, Budget.class);

    /**
     * Persists the provided list of budgets. The {@code companyDirectory} parameter is retained for
     * backwards compatibility but ignored – data is always written to the database.
     *
     * @param budgets           budgets to persist; when {@code null} the call is logged and ignored
     * @param companyDirectory  unused legacy parameter maintained for API stability
     * @throws IOException if the budgets cannot be serialized or the database update fails
     */
    public static void saveBudgets(List<Budget> budgets, File companyDirectory) throws IOException {
        if (budgets == null) {
            LOGGER.warning("Budget list provided is null. Nothing to save.");
            return;
        }

        try {
            String payload = MAPPER.writeValueAsString(budgets);
            new DocumentRepository().upsert(DOCUMENT_NAME, payload);
            LOGGER.info("Budgets saved successfully to database document '" + DOCUMENT_NAME + "'.");
        } catch (SQLException e) {
            throw new IOException("Failed to save budgets to database", e);
        }
    }

    /**
     * Loads all stored budgets from the database. The {@code companyDirectory} parameter is ignored.
     *
     * @param companyDirectory unused legacy parameter maintained for API stability
     * @return list of budgets read from storage; never {@code null}
     * @throws IOException if the document cannot be fetched or deserialized
     */
    public List<Budget> loadBudgets(File companyDirectory) throws IOException {
        try {
            return new DocumentRepository().find(DOCUMENT_NAME)
                    .map(payload -> {
                        try {
                            List<Budget> loaded = MAPPER.readValue(payload, LIST_TYPE);
                            LOGGER.info("Budgets loaded successfully from database document '" + DOCUMENT_NAME + "'.");
                            return loaded;
                        } catch (IOException ex) {
                            LOGGER.log(Level.SEVERE,
                                    "Failed to deserialize budgets JSON from database. Returning empty list.", ex);
                            return new ArrayList<Budget>();
                        }
                    })
                    .orElseGet(ArrayList::new);
        } catch (SQLException e) {
            throw new IOException("Failed to load budgets from database", e);
        }
    }
}
