package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {

    private BudgetService budgetService;
    private File companyDirectory;

    @TempDir
    Path tempDir; // JUnit 5 temporary directory

    @BeforeEach
    void setUp() {
        budgetService = new BudgetService();
        companyDirectory = tempDir.toFile(); // Use the temp directory as the company directory
    }

    @Test
    void testSaveAndLoad_EmptyList() throws IOException {
        List<Budget> emptyList = new ArrayList<>();
        budgetService.saveBudgets(emptyList, companyDirectory);

        File budgetsFile = new File(companyDirectory, "budgets.json");
        assertTrue(budgetsFile.exists(), "budgets.json file should be created.");

        List<Budget> loadedBudgets = budgetService.loadBudgets(companyDirectory);
        assertNotNull(loadedBudgets, "Loaded budgets should not be null.");
        assertTrue(loadedBudgets.isEmpty(), "Loaded budgets should be an empty list.");
    }

    @Test
    void testSaveAndLoad_SingleBudget() throws IOException {
        Budget budget = new Budget("Annual Budget", 2024);
        budget.setBudgetId(UUID.randomUUID().toString()); // Set ID for predictable comparison
        budget.setDescription("Main budget for the year");
        budget.setCurrency("USD");

        BudgetLine line1 = new BudgetLine("ACC101", "Donations", new BigDecimal("50000.00"), Periodicity.ANNUAL);
        BudgetLine line2 = new BudgetLine("ACC201", "Grants", new BigDecimal("25000.00"), Periodicity.QUARTERLY);
        line2.setPeriodicAmounts(List.of(new BigDecimal("6250"), new BigDecimal("6250"), new BigDecimal("6250"), new BigDecimal("6250")));
        budget.addBudgetLine(line1);
        budget.addBudgetLine(line2);

        List<Budget> budgetsToSave = List.of(budget);
        budgetService.saveBudgets(budgetsToSave, companyDirectory);

        List<Budget> loadedBudgets = budgetService.loadBudgets(companyDirectory);
        assertNotNull(loadedBudgets);
        assertEquals(1, loadedBudgets.size(), "Should load one budget.");

        Budget loadedBudget = loadedBudgets.get(0);
        assertEquals(budget.getBudgetId(), loadedBudget.getBudgetId());
        assertEquals(budget.getBudgetName(), loadedBudget.getBudgetName());
        assertEquals(budget.getFiscalYear(), loadedBudget.getFiscalYear());
        assertEquals(budget.getDescription(), loadedBudget.getDescription());
        assertEquals(budget.getCurrency(), loadedBudget.getCurrency());
        assertEquals(budget.getBudgetLines().size(), loadedBudget.getBudgetLines().size(), "Number of budget lines should match.");

        // Compare budget lines
        BudgetLine originalLine1 = budget.getBudgetLines().get(0);
        BudgetLine loadedLine1 = loadedBudget.getBudgetLines().get(0);
        assertEquals(originalLine1.getAccountId(), loadedLine1.getAccountId());
        assertEquals(originalLine1.getAccountName(), loadedLine1.getAccountName());
        assertTrue(originalLine1.getTotalBudgetedAmount().compareTo(loadedLine1.getTotalBudgetedAmount()) == 0);
        assertEquals(originalLine1.getPeriodicity(), loadedLine1.getPeriodicity());

        BudgetLine originalLine2 = budget.getBudgetLines().get(1);
        BudgetLine loadedLine2 = loadedBudget.getBudgetLines().get(1);
        assertEquals(originalLine2.getAccountId(), loadedLine2.getAccountId());
        assertTrue(originalLine2.getTotalBudgetedAmount().compareTo(loadedLine2.getTotalBudgetedAmount()) == 0);
        assertEquals(originalLine2.getPeriodicity(), loadedLine2.getPeriodicity());
        assertEquals(originalLine2.getPeriodicAmounts().size(), loadedLine2.getPeriodicAmounts().size());
        for(int i=0; i < originalLine2.getPeriodicAmounts().size(); i++) {
            assertTrue(originalLine2.getPeriodicAmounts().get(i).compareTo(loadedLine2.getPeriodicAmounts().get(i)) == 0);
        }
    }

    @Test
    void testSaveAndLoad_MultipleBudgets() throws IOException {
        Budget budget1 = new Budget("Q1 Operations", 2024);
        budget1.setBudgetId(UUID.randomUUID().toString());
        budget1.addBudgetLine(new BudgetLine("EXP100", "Office Supplies", new BigDecimal("5000"), Periodicity.QUARTERLY));

        Budget budget2 = new Budget("Annual Fundraising", 2024);
        budget2.setBudgetId(UUID.randomUUID().toString());
        budget2.addBudgetLine(new BudgetLine("INC200", "Gala Event", new BigDecimal("150000"), Periodicity.ANNUAL));

        List<Budget> budgetsToSave = List.of(budget1, budget2);
        budgetService.saveBudgets(budgetsToSave, companyDirectory);

        List<Budget> loadedBudgets = budgetService.loadBudgets(companyDirectory);
        assertNotNull(loadedBudgets);
        assertEquals(2, loadedBudgets.size(), "Should load two budgets.");

        // Verify budget1 (simplified check, more thorough like above if needed)
        Budget loadedBudget1 = loadedBudgets.stream().filter(b -> b.getBudgetId().equals(budget1.getBudgetId())).findFirst().orElse(null);
        assertNotNull(loadedBudget1);
        assertEquals(budget1.getBudgetName(), loadedBudget1.getBudgetName());
        assertEquals(1, loadedBudget1.getBudgetLines().size());

        // Verify budget2
        Budget loadedBudget2 = loadedBudgets.stream().filter(b -> b.getBudgetId().equals(budget2.getBudgetId())).findFirst().orElse(null);
        assertNotNull(loadedBudget2);
        assertEquals(budget2.getBudgetName(), loadedBudget2.getBudgetName());
        assertEquals(1, loadedBudget2.getBudgetLines().size());
    }

    @Test
    void testLoadBudgets_FileNotFound() throws IOException {
        // Ensure budgets.json does not exist (it shouldn't in a fresh tempDir)
        List<Budget> loadedBudgets = budgetService.loadBudgets(companyDirectory);
        assertNotNull(loadedBudgets);
        assertTrue(loadedBudgets.isEmpty(), "Should return an empty list if file not found.");
    }

    @Test
    void testLoadBudgets_CorruptJsonFile() throws IOException {
        File budgetsFile = new File(companyDirectory, "budgets.json");
        Files.writeString(budgetsFile.toPath(), "{invalid json content,,}");

        List<Budget> loadedBudgets = budgetService.loadBudgets(companyDirectory);
        assertNotNull(loadedBudgets);
        assertTrue(loadedBudgets.isEmpty(), "Should return an empty list for corrupt JSON.");
        // Verification of logging would require a log capture mechanism,
        // but the requirement is to ensure it returns empty and doesn't crash.
    }
    
    @Test
    void testLoadBudgets_EmptyJsonFile() throws IOException {
        File budgetsFile = new File(companyDirectory, "budgets.json");
        Files.writeString(budgetsFile.toPath(), ""); // Empty file

        List<Budget> loadedBudgets = budgetService.loadBudgets(companyDirectory);
        assertNotNull(loadedBudgets);
        assertTrue(loadedBudgets.isEmpty(), "Should return an empty list for an empty JSON file.");
    }


    @Test
    void testSaveBudgets_NullCompanyDirectory() {
        List<Budget> budgets = List.of(new Budget("Test Budget", 2024));
        Exception exception = assertThrows(IOException.class, () -> {
            budgetService.saveBudgets(budgets, null);
        });
        assertEquals("Company directory is invalid or not provided.", exception.getMessage());
    }

    @Test
    void testSaveBudgets_InvalidCompanyDirectory() throws IOException {
        List<Budget> budgets = List.of(new Budget("Test Budget", 2024));
        File testFileAsDirectory = new File(companyDirectory, "not_a_directory.txt");
        assertTrue(testFileAsDirectory.createNewFile(), "Failed to create test file.");

        Exception exception = assertThrows(IOException.class, () -> {
            budgetService.saveBudgets(budgets, testFileAsDirectory);
        });
        assertEquals("Company directory is invalid or not provided.", exception.getMessage());
        
        testFileAsDirectory.delete(); // Clean up
    }
    
    @Test
    void testSaveBudgets_NullBudgetsList() throws IOException {
        // As per BudgetService implementation, this logs a warning and does not create the file.
        budgetService.saveBudgets(null, companyDirectory);
        File budgetsFile = new File(companyDirectory, "budgets.json");
        assertFalse(budgetsFile.exists(), "budgets.json should not be created for null list.");
    }


    @Test
    void testLoadBudgets_NullCompanyDirectory() throws IOException {
        // Current implementation logs a warning and returns an empty list.
        List<Budget> loadedBudgets = budgetService.loadBudgets(null);
        assertNotNull(loadedBudgets);
        assertTrue(loadedBudgets.isEmpty(), "Should return an empty list if company directory is null.");
    }
    
    @Test
    void testLoadBudgets_InvalidCompanyDirectory() throws IOException {
        File testFileAsDirectory = new File(companyDirectory, "not_a_directory_for_load.txt");
        assertTrue(testFileAsDirectory.createNewFile(), "Failed to create test file for loading.");

        // Current implementation logs a warning and returns an empty list.
        List<Budget> loadedBudgets = budgetService.loadBudgets(testFileAsDirectory);
        assertNotNull(loadedBudgets);
        assertTrue(loadedBudgets.isEmpty(), "Should return an empty list if company directory is not a directory.");

        testFileAsDirectory.delete(); // Clean up
    }
}
