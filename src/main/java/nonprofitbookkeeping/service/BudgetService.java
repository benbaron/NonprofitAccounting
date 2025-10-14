
package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import nonprofitbookkeeping.model.budget.Budget;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
	
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
                        .enable(SerializationFeature.INDENT_OUTPUT);

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
                        return;
                }

                if (companyDirectory == null)
                {
                        throw new IOException("Company directory is invalid or not provided.");
                }

                Path companyPath = companyDirectory.toPath();
                try
                {
                        Files.createDirectories(companyPath);
                }
                catch (IOException e)
                {
                        LOGGER.log(Level.SEVERE,
                                "Unable to create company directory at: " + companyPath.toAbsolutePath(), e);
                        throw new IOException("Company directory is invalid or not provided.", e);
                }

                if (!Files.isDirectory(companyPath))
                {
                        throw new IOException("Company directory is invalid or not provided.");
                }

                Path budgetsPath = companyPath.resolve(BUDGETS_FILENAME);
                ObjectWriter writer = OBJECT_MAPPER.writer(new DefaultPrettyPrinter());

                try
                {
                        Path tempFile = Files.createTempFile(companyPath, BUDGETS_FILENAME, ".tmp");
                        try
                        {
                                writer.writeValue(tempFile.toFile(), budgets);
                                moveAtomically(tempFile, budgetsPath);
                                LOGGER.info("Budgets saved successfully to: " + budgetsPath.toAbsolutePath());
                        }
                        finally
                        {
                                try
                                {
                                        Files.deleteIfExists(tempFile);
                                }
                                catch (IOException cleanupException)
                                {
                                        LOGGER.log(Level.WARNING,
                                                "Unable to delete temporary budgets file at: "
                                                        + tempFile.toAbsolutePath(), cleanupException);
                                }
                        }
                }
                catch (IOException e)
                {
                        LOGGER.log(Level.SEVERE, "Failed to save budgets to " + budgetsPath.toAbsolutePath(), e);
                        throw e; // Re-throw to allow caller to handle
                }

        }

        private static void moveAtomically(Path source, Path target) throws IOException
        {
                try
                {
                        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING,
                                StandardCopyOption.ATOMIC_MOVE);
                }
                catch (IOException e)
                {
                        if (!(e instanceof java.nio.file.AtomicMoveNotSupportedException))
                        {
                                throw e;
                        }
                        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
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
		
                if (companyDirectory == null)
                {
                        LOGGER.warning("Company directory is invalid or not provided for loading budgets.");
                        return new ArrayList<>();
                }

                Path companyPath = companyDirectory.toPath();

                if (!Files.isDirectory(companyPath))
                {
                        LOGGER.warning("Company directory is invalid or not provided for loading budgets.");
                        return new ArrayList<>();
                }

                Path budgetsPath = companyPath.resolve(BUDGETS_FILENAME);

                if (!Files.exists(budgetsPath) || !Files.isRegularFile(budgetsPath))
                {
                        LOGGER.info("Budgets file not found at: " + budgetsPath.toAbsolutePath() +
                                ". Returning empty list.");
                        return new ArrayList<>();
                }

                long size;
                try
                {
                        size = Files.size(budgetsPath);
                }
                catch (IOException e)
                {
                        LOGGER.log(Level.WARNING,
                                "Unable to determine size of budgets file at: " + budgetsPath.toAbsolutePath()
                                        + ". Returning empty list.", e);
                        return new ArrayList<>();
                }

                if (size == 0)
                {
                        LOGGER.info("Budgets file is empty at: " + budgetsPath.toAbsolutePath() +
                                ". Returning empty list.");
                        return new ArrayList<>();
                }

                try
                {
                        CollectionType listType = OBJECT_MAPPER.getTypeFactory()
                                .constructCollectionType(List.class, Budget.class);
                        List<Budget> loadedBudgets = OBJECT_MAPPER.readValue(budgetsPath.toFile(), listType);
                        LOGGER.info("Budgets loaded successfully from: " + budgetsPath.toAbsolutePath());
                        return loadedBudgets != null ? loadedBudgets : new ArrayList<>();
                }
                catch (IOException e)
                {
                        LOGGER.log(Level.SEVERE, "Failed to load or parse budgets from " +
                                budgetsPath.toAbsolutePath() + ". Returning empty list.", e);
                        return new ArrayList<>();
                }

        }

}
