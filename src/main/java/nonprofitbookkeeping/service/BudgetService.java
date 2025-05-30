package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.core.util.*;

import nonprofitbookkeeping.model.budget.Budget;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BudgetService {

    private static final Logger LOGGER = Logger.getLogger(BudgetService.class.getName());
    private static final String BUDGETS_FILENAME = "budgets.json";

    /**
     * Saves a list of budgets to a JSON file within the specified company directory.
     *
     * @param budgets The list of Budget objects to save.
     * @param companyDirectory The directory where the company's data is stored.
     * @throws IOException If an error occurs during file writing or serialization.
     */
    public void saveBudgets(List<Budget> budgets, File companyDirectory) throws IOException {
        if (budgets == null) {
            LOGGER.warning("Budget list provided is null. Nothing to save.");
            // Optionally, save an empty list or throw IllegalArgumentException
            // For now, let's just not write the file if the list is null.
            // If an empty list is provided, an empty JSON array will be saved.
            return; 
        }
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            throw new IOException("Company directory is invalid or not provided.");
        }

        File budgetsFile = new File(companyDirectory, BUDGETS_FILENAME);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // For pretty printing
        objectMapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());


        try {
            // Ensure parent directory exists (though companyDirectory should already exist)
            // Files.createDirectories(budgetsFile.getParentFile().toPath());
            objectMapper.writeValue(budgetsFile, budgets);
            LOGGER.info("Budgets saved successfully to: " + budgetsFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save budgets to " + budgetsFile.getAbsolutePath(), e);
            throw e; // Re-throw to allow caller to handle
        }
    }

    /**
     * Loads a list of budgets from a JSON file within the specified company directory.
     *
     * @param companyDirectory The directory where the company's data is stored.
     * @return A List of Budget objects. Returns an empty list if the file doesn't exist or
     *         if there's an error during deserialization (after logging the error).
     * @throws IOException If a critical I/O error occurs (other than file not found or parse error,
     *                     which return empty list).
     */
    public List<Budget> loadBudgets(File companyDirectory) throws IOException {
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            LOGGER.warning("Company directory is invalid or not provided for loading budgets.");
            return new ArrayList<>(); // Or throw new IOException("Company directory is invalid.");
        }

        File budgetsFile = new File(companyDirectory, BUDGETS_FILENAME);

        if (!budgetsFile.exists() || !budgetsFile.isFile()) {
            LOGGER.info("Budgets file not found at: " + budgetsFile.getAbsolutePath() + ". Returning empty list.");
            return new ArrayList<>();
        }

        if (budgetsFile.length() == 0) {
            LOGGER.info("Budgets file is empty at: " + budgetsFile.getAbsolutePath() + ". Returning empty list.");
            return new ArrayList<>();
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, Budget.class);
            List<Budget> loadedBudgets = objectMapper.readValue(budgetsFile, listType);
            LOGGER.info("Budgets loaded successfully from: " + budgetsFile.getAbsolutePath());
            // The getBudgetId() method in Budget model ensures ID is generated if null after deserialization.
            return loadedBudgets != null ? loadedBudgets : new ArrayList<>();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load or parse budgets from " + budgetsFile.getAbsolutePath() + ". Returning empty list.", e);
            // Depending on policy, might re-throw for certain IOExceptions vs returning empty for parse errors
            return new ArrayList<>(); 
        }
    }
}
