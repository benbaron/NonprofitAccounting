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

import static org.junit.jupiter.api.Assertions.*;

public class BudgetPanelTest {

    /** Simple stub dialog that bypasses UI. */
    static class StubBudgetLineDialog extends BudgetLineDialog {
        private final boolean saved;
        private final BudgetLine line;
        StubBudgetLineDialog(boolean saved, BudgetLine line) {
            this(saved, line, new ChartOfAccounts());
        }
        StubBudgetLineDialog(boolean saved, BudgetLine line, ChartOfAccounts coa) {
            super((Dialog) null, "Stub", coa, List.of(), line);
            this.saved = saved;
            this.line = line;
        }
        @Override public void setVisible(boolean b) { /* no UI */ }
        @Override public boolean isSaved() { return this.saved; }
        @Override public BudgetLine getBudgetLine() { return this.line; }
    }

    /** Stub dialog that mutates an existing budget line before closing. */
    static class MutatingStubBudgetLineDialog extends BudgetLineDialog {
        private final BudgetLine target;
        private final Runnable mutation;
        MutatingStubBudgetLineDialog(BudgetLine target, ChartOfAccounts coa, Runnable mutation) {
            super((Dialog) null, "Stub", coa, List.of(), target);
            this.target = target;
            this.mutation = mutation;
        }
        @Override public void setVisible(boolean b) {
            if (b && this.mutation != null) {
                this.mutation.run();
            }
        }
        @Override public boolean isSaved() { return true; }
        @Override public BudgetLine getBudgetLine() { return this.target; }
    }

    /** Panel subclass that injects a stub dialog. */
    static class TestBudgetPanel extends BudgetPanel {
        BudgetLineDialog stub;
        TestBudgetPanel(ChartOfAccounts coa, List<Fund> funds,
                        BudgetService svc, File dir, Budget budget) {
            super(null, coa, funds, svc, dir, budget);
        }
        void setStub(BudgetLineDialog d) { this.stub = d; }
        @Override
        protected BudgetLineDialog createBudgetLineDialog(String title, BudgetLine line) {
            return this.stub;
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

        StubBudgetLineDialog dlg = new StubBudgetLineDialog(true, line, coa);
        panel.setStub(dlg);

        Method add = BudgetPanel.class.getDeclaredMethod("actionAddLine", ActionEvent.class);
        add.setAccessible(true);
        add.invoke(panel, new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, "add"));

        Field modelField = BudgetPanel.class.getDeclaredField("budgetLineTableModel");
        modelField.setAccessible(true);
        BudgetLineTableModel model = (BudgetLineTableModel) modelField.get(panel);

        assertEquals(1, model.getBudgetLines().size());
        assertEquals(new BigDecimal("100"), model.getBudgetLines().get(0).getTotalBudgetedAmount());
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

        BudgetLine edited = new BudgetLine();
        edited.setAccountId("A1");
        edited.setAccountName("Cash");
        edited.setTotalBudgetedAmount(new BigDecimal("75"));
        edited.setPeriodicity(Periodicity.ANNUAL);
        StubBudgetLineDialog dlg = new StubBudgetLineDialog(true, edited, coa);
        panel.setStub(dlg);

        Method edit = BudgetPanel.class.getDeclaredMethod("actionEditLine", ActionEvent.class);
        edit.setAccessible(true);
        edit.invoke(panel, new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, "edit"));

        Field modelField = BudgetPanel.class.getDeclaredField("budgetLineTableModel");
        modelField.setAccessible(true);
        BudgetLineTableModel model = (BudgetLineTableModel) modelField.get(panel);

        assertEquals(1, model.getBudgetLines().size());
        assertEquals(new BigDecimal("75"), model.getBudgetLines().get(0).getTotalBudgetedAmount());
    }

    @Test
    public void testEditBudgetLineInPlaceInstance() throws Exception {
        ChartOfAccounts coa = new ChartOfAccounts();
        Account acc = new Account();
        acc.setAccountNumber("A1");
        acc.setName("Cash");
        coa.addAccount(acc);

        Fund fund = new Fund("General");
        BudgetService svc = new BudgetService();
        File dir = Files.createTempDirectory("budtest3").toFile();

        Budget initialBudget = new Budget("B", 2026);
        BudgetLine line = new BudgetLine();
        line.setAccountId("A1");
        line.setAccountName("Cash");
        line.setTotalBudgetedAmount(new BigDecimal("40"));
        line.setPeriodicity(Periodicity.ANNUAL);
        initialBudget.setBudgetLines(new java.util.ArrayList<>(List.of(line)));

        TestBudgetPanel panel = new TestBudgetPanel(coa, List.of(fund), svc, dir, initialBudget);

        Field tableField = BudgetPanel.class.getDeclaredField("tblBudgetLines");
        tableField.setAccessible(true);
        JTable table = (JTable) tableField.get(panel);
        table.getSelectionModel().setSelectionInterval(0,0);

        MutatingStubBudgetLineDialog dlg = new MutatingStubBudgetLineDialog(line, coa,
                () -> line.setTotalBudgetedAmount(new BigDecimal("95")));
        panel.setStub(dlg);

        Method edit = BudgetPanel.class.getDeclaredMethod("actionEditLine", ActionEvent.class);
        edit.setAccessible(true);
        edit.invoke(panel, new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, "edit"));

        Field modelField = BudgetPanel.class.getDeclaredField("budgetLineTableModel");
        modelField.setAccessible(true);
        BudgetLineTableModel model = (BudgetLineTableModel) modelField.get(panel);

        assertSame(line, model.getBudgetLines().get(0));
        assertEquals(new BigDecimal("95"), model.getBudgetLines().get(0).getTotalBudgetedAmount());
    }
}
