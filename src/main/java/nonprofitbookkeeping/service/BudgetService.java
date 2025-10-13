package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.persistence.JsonStorageRepository;

/** Service class for managing budgets using the shared H2 database. */
public class BudgetService {
    private static final Logger LOGGER = Logger.getLogger(BudgetService.class.getName());
    private static final String STORAGE_KEY = "budgets";

    private final ObjectMapper objectMapper;

    public BudgetService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Persists the provided list of budgets into the json_storage table.
     *
     * @param budgets budgets to store; when {@code null} the existing payload is removed
     * @param companyDirectory unused legacy parameter retained for compatibility
     * @throws IOException when serialization fails or the database cannot be updated
     */
    public static void saveBudgets(List<Budget> budgets, File companyDirectory) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        JsonStorageRepository repository = new JsonStorageRepository();

        if (budgets == null) {
            LOGGER.warning("Budget list provided is null. Clearing stored budgets.");
            try {
                repository.delete(STORAGE_KEY);
            } catch (SQLException sqlException) {
                throw new IOException("Failed to clear budgets from H2 database", sqlException);
            }
            return;
        }

        try {
            String payload = mapper.writeValueAsString(budgets);
            repository.save(STORAGE_KEY, payload);
        } catch (SQLException sqlException) {
            throw new IOException("Failed to save budgets to H2 database", sqlException);
        }
    }

    /**
     * Loads the previously persisted budgets from the database.
     *
     * @param companyDirectory unused legacy parameter retained for compatibility
     * @return the stored budgets or an empty list when none are available
     * @throws IOException when the payload cannot be fetched from the database
     */
    public List<Budget> loadBudgets(File companyDirectory) throws IOException {
        CollectionType listType =
                this.objectMapper.getTypeFactory().constructCollectionType(List.class, Budget.class);

        try {
            JsonStorageRepository repository = new JsonStorageRepository();
            String payload = repository.load(STORAGE_KEY).orElse(null);

            if (payload == null || payload.isBlank()) {
                return new ArrayList<>();
            }

            List<Budget> loadedBudgets = this.objectMapper.readValue(payload, listType);
            LOGGER.info("Budgets loaded successfully from H2 database.");
            return loadedBudgets != null ? loadedBudgets : new ArrayList<>();
        } catch (SQLException sqlException) {
            throw new IOException("Failed to load budgets from H2 database", sqlException);
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE,
                    "Failed to parse budgets payload from H2 database. Returning empty list.",
                    ioException);
            return new ArrayList<>();
        }
    }
}
