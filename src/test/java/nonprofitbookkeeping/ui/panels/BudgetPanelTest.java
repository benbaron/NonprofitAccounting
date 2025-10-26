package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.model.budget.*;
import nonprofitbookkeeping.service.BudgetService;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class BudgetPanelTest {

    /** Simple stub dialog that bypasses UI. */
    static class StubBudgetLineDialog extends BudgetLineDialog {
        private final boolean saved;
        private final BudgetLine line;
        private final Consumer<BudgetLine> onShow;

        StubBudgetLineDialog(ChartOfAccounts coa, List<Fund> funds, BudgetLine seed,
                             boolean saved, Consumer<BudgetLine> onShow) {
            super((Dialog) null, "Stub", coa, funds, seed != null ? seed : new BudgetLine());
            this.line = seed != null ? seed : super.getBudgetLine();
            this.saved = saved;
            this.onShow = onShow;
        }

        @Override public void setVisible(boolean b) {
            if (b && this.onShow != null) {
                this.onShow.accept(this.line);
            }
        }

        @Override public boolean isSaved() { return this.saved; }

        @Override public BudgetLine getBudgetLine() { return this.line; }
    }

    /** Panel subclass that injects a stub dialog. */
    static class TestBudgetPanel extends BudgetPanel {
        private Function<BudgetLine, BudgetLineDialog> factory;

        TestBudgetPanel(ChartOfAccounts coa, List<Fund> funds,
                        BudgetService svc, File dir, Budget budget) {
            super(null, coa, funds, svc, dir, budget);
        }

        void setDialogFactory(Function<BudgetLine, BudgetLineDialog> factory) {
            this.factory = factory;
        }

        @Override
        protected BudgetLineDialog createBudgetLineDialog(String title, BudgetLine line) {
            if (this.factory != null) {
                return this.factory.apply(line);
            }
            return super.createBudgetLineDialog(title, line);
        }
    }

    @Test
    public void testAddBudgetLine() throws Exception {
        ChartOfAccounts coa = new ChartOfAccounts();
        Account acc = new Account();
        acc.setAccountNumber("A1");
        acc.setName("Cash");
        coa.addAccount(acc);

        Fund fund = new Fund("General");
        BudgetService svc = new BudgetService();
        File dir = Files.createTempDirectory("budtest").toFile();

        TestBudgetPanel panel = new TestBudgetPanel(coa, List.of(fund), svc, dir, null);

        BudgetLine line = new BudgetLine();
        line.setAccountId("A1");
        line.setAccountName("Cash");
        line.setTotalBudgetedAmount(new BigDecimal("100"));
        line.setPeriodicity(Periodicity.ANNUAL);

        panel.setDialogFactory(existing -> new StubBudgetLineDialog(coa, List.of(fund), line, true, null));

        Method add = BudgetPanel.class.getDeclaredMethod("actionAddLine", ActionEvent.class);
        add.setAccessible(true);
        add.invoke(panel, new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, "add"));

        Field modelField = BudgetPanel.class.getDeclaredField("budgetLineTableModel");
        modelField.setAccessible(true);
        BudgetLineTableModel model = (BudgetLineTableModel) modelField.get(panel);

        assertEquals(1, model.getBudgetLines().size());
        assertEquals(new BigDecimal("100"), model.getBudgetLines().get(0).getTotalBudgetedAmount());

        Field tableField = BudgetPanel.class.getDeclaredField("tblBudgetLines");
        tableField.setAccessible(true);
        JTable table = (JTable) tableField.get(panel);
        assertEquals(0, table.getSelectedRow());
    }

    @Test
    public void testEditBudgetLine() throws Exception {
        ChartOfAccounts coa = new ChartOfAccounts();
        Account acc = new Account();
        acc.setAccountNumber("A1");
        acc.setName("Cash");
        coa.addAccount(acc);

        Fund fund = new Fund("General");
        BudgetService svc = new BudgetService();
        File dir = Files.createTempDirectory("budtest2").toFile();

        Budget initialBudget = new Budget("B", 2025);
        BudgetLine line = new BudgetLine();
        line.setAccountId("A1");
        line.setAccountName("Cash");
        line.setTotalBudgetedAmount(new BigDecimal("50"));
        line.setPeriodicity(Periodicity.ANNUAL);
        initialBudget.setBudgetLines(new java.util.ArrayList<>(List.of(line)));

        TestBudgetPanel panel = new TestBudgetPanel(coa, List.of(fund), svc, dir, initialBudget);

        Field tableField = BudgetPanel.class.getDeclaredField("tblBudgetLines");
        tableField.setAccessible(true);
        JTable table = (JTable) tableField.get(panel);
        table.getSelectionModel().setSelectionInterval(0,0);

        panel.setDialogFactory(existing -> new StubBudgetLineDialog(coa, List.of(fund), existing, true,
                lineRef -> lineRef.setTotalBudgetedAmount(new BigDecimal("75"))));

        Method edit = BudgetPanel.class.getDeclaredMethod("actionEditLine", ActionEvent.class);
        edit.setAccessible(true);
        edit.invoke(panel, new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, "edit"));

        Field modelField = BudgetPanel.class.getDeclaredField("budgetLineTableModel");
        modelField.setAccessible(true);
        BudgetLineTableModel model = (BudgetLineTableModel) modelField.get(panel);

        assertEquals(1, model.getBudgetLines().size());
        assertEquals(new BigDecimal("75"), model.getBudgetLines().get(0).getTotalBudgetedAmount());
        assertEquals(0, table.getSelectedRow());
    }
}
