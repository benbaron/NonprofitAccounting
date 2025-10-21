package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;
import nonprofitbookkeeping.persistence.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {

    private BudgetService budgetService;
    private File companyDirectory;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        Path dbFile = this.tempDir.resolve("budgets-db");
        Database.init(dbFile);
        Database.get().ensureSchema();
        this.budgetService = new BudgetService();
        this.companyDirectory = this.tempDir.toFile();
    }

    @Test
    void testSaveAndLoad_EmptyList() throws IOException {
        List<Budget> emptyList = new ArrayList<>();
        BudgetService.saveBudgets(emptyList, this.companyDirectory);

        List<Budget> loadedBudgets = this.budgetService.loadBudgets(this.companyDirectory);
        assertNotNull(loadedBudgets, "Loaded budgets should not be null.");
        assertTrue(loadedBudgets.isEmpty(), "Loaded budgets should be an empty list.");
    }

    @Test
    void testSaveAndLoad_SingleBudget() throws IOException {
        Budget budget = new Budget("Annual Budget", 2024);
        budget.setBudgetId(UUID.randomUUID().toString());
        budget.setDescription("Main budget for the year");
        budget.setCurrency("USD");

        BudgetLine line1 = new BudgetLine("ACC101", "Donations", new BigDecimal("50000.00"),
                Periodicity.ANNUAL, new ArrayList<>(), null);
        BudgetLine line2 = new BudgetLine("ACC201", "Grants", new BigDecimal("25000.00"),
                Periodicity.QUARTERLY, new ArrayList<>(), null);
        line2.setPeriodicAmounts(List.of(new BigDecimal("6250"), new BigDecimal("6250"),
                new BigDecimal("6250"), new BigDecimal("6250")));
        budget.addBudgetLine(line1);
        budget.addBudgetLine(line2);

        List<Budget> budgetsToSave = List.of(budget);
        BudgetService.saveBudgets(budgetsToSave, this.companyDirectory);

        List<Budget> loadedBudgets = this.budgetService.loadBudgets(this.companyDirectory);
        assertNotNull(loadedBudgets);
        assertEquals(1, loadedBudgets.size(), "Should load one budget.");

        Budget loadedBudget = loadedBudgets.get(0);
        assertEquals(budget.getBudgetId(), loadedBudget.getBudgetId());
        assertEquals(budget.getBudgetName(), loadedBudget.getBudgetName());
        assertEquals(budget.getFiscalYear(), loadedBudget.getFiscalYear());
        assertEquals(budget.getDescription(), loadedBudget.getDescription());
        assertEquals(budget.getCurrency(), loadedBudget.getCurrency());
        assertEquals(budget.getBudgetLines().size(), loadedBudget.getBudgetLines().size());

        BudgetLine originalLine1 = budget.getBudgetLines().get(0);
        BudgetLine loadedLine1 = loadedBudget.getBudgetLines().get(0);
        assertEquals(originalLine1.getAccountId(), loadedLine1.getAccountId());
        assertEquals(originalLine1.getAccountName(), loadedLine1.getAccountName());
        assertTrue(originalLine1.getTotalBudgetedAmount()
                .compareTo(loadedLine1.getTotalBudgetedAmount()) == 0);
        assertEquals(originalLine1.getPeriodicity(), loadedLine1.getPeriodicity());

        BudgetLine originalLine2 = budget.getBudgetLines().get(1);
        BudgetLine loadedLine2 = loadedBudget.getBudgetLines().get(1);
        assertEquals(originalLine2.getAccountId(), loadedLine2.getAccountId());
        assertTrue(originalLine2.getTotalBudgetedAmount()
                .compareTo(loadedLine2.getTotalBudgetedAmount()) == 0);
        assertEquals(originalLine2.getPeriodicity(), loadedLine2.getPeriodicity());
        assertEquals(originalLine2.getPeriodicAmounts().size(),
                loadedLine2.getPeriodicAmounts().size());

        for (int i = 0; i < originalLine2.getPeriodicAmounts().size(); i++) {
            assertTrue(originalLine2.getPeriodicAmounts().get(i)
                    .compareTo(loadedLine2.getPeriodicAmounts().get(i)) == 0);
        }
    }

    @Test
    void testSaveAndLoad_MultipleBudgets() throws IOException {
        Budget budget1 = new Budget("Q1 Operations", 2024);
        budget1.setBudgetId(UUID.randomUUID().toString());
        budget1.addBudgetLine(new BudgetLine("EXP100", "Office Supplies", new BigDecimal("5000"),
                Periodicity.QUARTERLY, new ArrayList<>(), null));

        Budget budget2 = new Budget("Annual Fundraising", 2024);
        budget2.setBudgetId(UUID.randomUUID().toString());
        budget2.addBudgetLine(new BudgetLine("INC200", "Gala Event", new BigDecimal("150000"),
                Periodicity.ANNUAL, new ArrayList<>(), null));

        List<Budget> budgetsToSave = List.of(budget1, budget2);
        BudgetService.saveBudgets(budgetsToSave, this.companyDirectory);

        List<Budget> loadedBudgets = this.budgetService.loadBudgets(this.companyDirectory);
        assertNotNull(loadedBudgets);
        assertEquals(2, loadedBudgets.size(), "Should load two budgets.");

        Budget loadedBudget1 = loadedBudgets.stream()
                .filter(b -> b.getBudgetId().equals(budget1.getBudgetId())).findFirst().orElse(null);
        assertNotNull(loadedBudget1);
        assertEquals(budget1.getBudgetName(), loadedBudget1.getBudgetName());

        Budget loadedBudget2 = loadedBudgets.stream()
                .filter(b -> b.getBudgetId().equals(budget2.getBudgetId())).findFirst().orElse(null);
        assertNotNull(loadedBudget2);
        assertEquals(budget2.getBudgetName(), loadedBudget2.getBudgetName());
    }

    @Test
    void testLoadBudgets_NoData() throws IOException {
        List<Budget> loadedBudgets = this.budgetService.loadBudgets(this.companyDirectory);
        assertNotNull(loadedBudgets);
        assertTrue(loadedBudgets.isEmpty(), "Should return an empty list when nothing was saved.");
    }

    @Test
    void testLoadBudgets_CorruptJsonInDatabase() throws IOException {
        try {
            new DocumentRepository().upsert("budgets", "{invalid json content,,}");
        } catch (Exception e) {
            throw new IOException(e);
        }

        List<Budget> loadedBudgets = this.budgetService.loadBudgets(this.companyDirectory);
        assertNotNull(loadedBudgets);
        assertTrue(loadedBudgets.isEmpty(), "Should return an empty list for corrupt JSON.");
    }

    @Test
    void testLoadBudgets_EmptyJsonInDatabase() throws IOException {
        try {
            new DocumentRepository().upsert("budgets", "");
        } catch (Exception e) {
            throw new IOException(e);
        }

        List<Budget> loadedBudgets = this.budgetService.loadBudgets(this.companyDirectory);
        assertNotNull(loadedBudgets);
        assertTrue(loadedBudgets.isEmpty(), "Should return an empty list for an empty JSON payload.");
    }

    @Test
    void testSaveBudgets_NullCompanyDirectory() {
        List<Budget> budgets = List.of(new Budget("Test Budget", 2024));
        assertDoesNotThrow(() -> BudgetService.saveBudgets(budgets, null));
    }

    @Test
    void testSaveBudgets_InvalidCompanyDirectory() throws IOException {
        List<Budget> budgets = List.of(new Budget("Test Budget", 2024));
        File testFileAsDirectory = new File(this.companyDirectory, "not_a_directory.txt");
        assertTrue(testFileAsDirectory.createNewFile(), "Failed to create test file.");

        assertDoesNotThrow(() -> BudgetService.saveBudgets(budgets, testFileAsDirectory));
    }
}
