
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

/**
 * Service class for managing {@link Budget} data.
 * This class provides functionalities to save a list of budgets to a JSON file
 * and load them back. It uses Jackson for JSON serialization and deserialization.
 * Budgets are stored in a file named "budgets.json" within a specified company directory.
 */
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
	public void saveBudgets(List<Budget> budgets, File companyDirectory) throws IOException
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
	
}
