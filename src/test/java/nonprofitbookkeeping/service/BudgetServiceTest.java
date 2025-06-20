package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {
    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        this.budgetService = new BudgetService();
    }

    @Test
    void testSaveAndLoad_EmptyList() {
        this.budgetService.saveBudgets(new ArrayList<>());
        List<Budget> loaded = this.budgetService.loadBudgets();
        assertTrue(loaded.isEmpty());
    }

    @Test
    void testSaveAndLoad_SingleBudget() {
        Budget budget = new Budget("Annual Budget", 2024);
        budget.setBudgetId(UUID.randomUUID().toString());
        budget.setDescription("Main budget for the year");
        budget.setCurrency("USD");
        BudgetLine line = new BudgetLine("A1","Donations",new BigDecimal("10"), Periodicity.ANNUAL,new ArrayList<>(),null);
        budget.addBudgetLine(line);
        this.budgetService.saveBudgets(List.of(budget));

        List<Budget> loaded = this.budgetService.loadBudgets();
        assertEquals(1, loaded.size());
        assertEquals(budget.getBudgetName(), loaded.get(0).getBudgetName());
        assertEquals(1, loaded.get(0).getBudgetLines().size());
    }

    @Test
    void testSaveAndLoad_MultipleBudgets() {
        Budget b1 = new Budget("B1",2024);
        b1.setBudgetId(UUID.randomUUID().toString());
        b1.addBudgetLine(new BudgetLine("A1","Donations",BigDecimal.ONE,Periodicity.ANNUAL,new ArrayList<>(),null));
        Budget b2 = new Budget("B2",2025);
        b2.setBudgetId(UUID.randomUUID().toString());
        this.budgetService.saveBudgets(List.of(b1,b2));

        List<Budget> loaded = this.budgetService.loadBudgets();
        assertEquals(2, loaded.size());
    }

    @Test
    void testSaveBudgets_NullList() {
        this.budgetService.saveBudgets(null); // should not throw
        assertTrue(this.budgetService.loadBudgets().isEmpty());
    }
}
