package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class JournalEntryWorkspaceFXTest extends JavaFXTestBase
{
        private JournalEntryWorkspaceFX panel;
        private TableView<JournalEntryWorkspaceFX.Line> table;

        @Mock
        private Consumer<AccountingTransaction> onSave;

        @Start
        public void start(Stage stage)
        {
                MockitoAnnotations.openMocks(this);

                ChartOfAccounts coa = new ChartOfAccounts();
                Account bank = new Account("1010", "Bank", AccountType.ASSET, BigDecimal.ZERO);
                bank.setIncreaseSide(AccountSide.DEBIT);
                Account revenue = new Account("4010", "Donations", AccountType.INCOME, BigDecimal.ZERO);
                revenue.setIncreaseSide(AccountSide.CREDIT);
                Account expense = new Account("6010", "Supplies", AccountType.EXPENSE, BigDecimal.ZERO);
                expense.setIncreaseSide(AccountSide.DEBIT);
                coa.addAccount(bank);
                coa.addAccount(revenue);
                coa.addAccount(expense);

                Company company = new Company();
                company.setChartOfAccounts(coa);
                CurrentCompany.forceCompanyLoad(company);

                this.panel = new JournalEntryWorkspaceFX(this.onSave);
                Scene scene = new Scene(this.panel, 900, 600);
                stage.setScene(scene);
                stage.show();

                this.table = this.panel.getTable();
        }

        @BeforeEach
        public void resetFields()
        {
                WaitForAsyncUtils.waitForFxEvents();
        }

        @Test
        public void initialState_hasSingleBlankLine_andSaveDisabled()
        {
                assertEquals(1, this.panel.getLines().size());
                JournalEntryWorkspaceFX.Line line = this.panel.getLines().get(0);
                assertEquals("", line.account.get());
                assertEquals(BigDecimal.ZERO, line.debit.get());
                assertEquals(BigDecimal.ZERO, line.credit.get());
                assertTrue(this.panel.getSaveButton().isDisabled());
                verifyNoInteractions(this.onSave);
        }

        @Test
        public void addAndRemoveLinesMaintainsAtLeastOneLine()
        {
                Platform.runLater(() -> this.panel.getAddLineButton().fire());
                WaitForAsyncUtils.waitForFxEvents();
                assertEquals(2, this.panel.getLines().size());

                Platform.runLater(() -> {
                        this.table.getSelectionModel().select(0);
                        this.panel.getRemoveLineButton().fire();
                });
                WaitForAsyncUtils.waitForFxEvents();
                assertEquals(1, this.panel.getLines().size());

                Platform.runLater(() -> {
                        this.table.getSelectionModel().select(0);
                        this.panel.getRemoveLineButton().fire();
                });
                WaitForAsyncUtils.waitForFxEvents();
                assertEquals(1, this.panel.getLines().size(),
                                "Workspace should always leave a blank line");
        }

        @Test
        public void balancedEntriesEnableSave()
        {
                Platform.runLater(() -> this.panel.getAddLineButton().fire());
                WaitForAsyncUtils.waitForFxEvents();

                Platform.runLater(() -> {
                        JournalEntryWorkspaceFX.Line debit = this.panel.getLines().get(0);
                        debit.account.set("Bank");
                        debit.debit.set(new BigDecimal("150.00"));

                        JournalEntryWorkspaceFX.Line credit = this.panel.getLines().get(1);
                        credit.account.set("Donations");
                        credit.credit.set(new BigDecimal("150.00"));
                });
                WaitForAsyncUtils.waitForFxEvents();

                assertFalse(this.panel.getSaveButton().isDisabled());

                Platform.runLater(() -> this.panel.getLines().get(1).credit.set(new BigDecimal("50.00")));
                WaitForAsyncUtils.waitForFxEvents();
                assertTrue(this.panel.getSaveButton().isDisabled());
        }

        @Test
        public void saveEmitsBalancedTransaction()
        {
                Platform.runLater(() -> this.panel.getAddLineButton().fire());
                WaitForAsyncUtils.waitForFxEvents();

                Platform.runLater(() -> {
                        this.panel.getLines().get(0).account.set("Bank");
                        this.panel.getLines().get(0).debit.set(new BigDecimal("200.00"));
                        this.panel.getLines().get(1).account.set("Donations");
                        this.panel.getLines().get(1).credit.set(new BigDecimal("200.00"));
                        this.panel.getSaveButton().fire();
                });
                WaitForAsyncUtils.waitForFxEvents();

                ArgumentCaptor<AccountingTransaction> txCaptor =
                                ArgumentCaptor.forClass(AccountingTransaction.class);
                verify(this.onSave).accept(txCaptor.capture());
                AccountingTransaction tx = txCaptor.getValue();
                assertEquals(LocalDate.now().toString(), tx.getDate());
                assertEquals(2, tx.getEntries().size());

                Set<AccountingEntry> expected = new LinkedHashSet<>();
                expected.add(new AccountingEntry(new BigDecimal("200.00"), "1010",
                                AccountSide.DEBIT, "Bank"));
                expected.add(new AccountingEntry(new BigDecimal("200.00"), "4010",
                                AccountSide.CREDIT, "Donations"));
                assertEquals(expected, tx.getEntries());
        }

        @Test
        public void existingTransactionPopulatesFieldsAndEnablesSave()
        {
                AccountingTransaction existing = new AccountingTransaction();
                existing.setDate("2023-11-01");
                existing.setDescription("Existing Memo");
                existing.addEntry(new AccountingEntry(new BigDecimal("75.00"), "1010",
                                AccountSide.DEBIT, "Bank"));
                existing.addEntry(new AccountingEntry(new BigDecimal("75.00"), "4010",
                                AccountSide.CREDIT, "Donations"));

                Platform.runLater(() -> {
                        Stage stage = (Stage) this.panel.getScene().getWindow();
                        JournalEntryWorkspaceFX editPanel = new JournalEntryWorkspaceFX(existing, this.onSave);
                        stage.setScene(new Scene(editPanel, 900, 600));
                        this.panel = editPanel;
                        this.table = editPanel.getTable();
                });
                WaitForAsyncUtils.waitForFxEvents();

                DatePicker datePicker = fetchDatePicker();
                String memoText = fetchMemoText();

                assertEquals(LocalDate.of(2023, 11, 1), datePicker.getValue());
                assertEquals("Existing Memo", memoText);
                assertFalse(this.panel.getSaveButton().isDisabled());
                assertEquals(2, this.panel.getLines().size());

                assertEquals("Bank", this.panel.getLines().get(0).account.get());
                assertEquals(new BigDecimal("75.00"), this.panel.getLines().get(0).debit.get());
                assertEquals("Donations", this.panel.getLines().get(1).account.get());
                assertEquals(new BigDecimal("75.00"), this.panel.getLines().get(1).credit.get());

                verifyNoMoreInteractions(this.onSave);
        }


        @Test
        public void detailsSectionUsesSimpleTwoColumnGrid_toAvoidLayoutRegression()
        {
                GridPane detailsGrid = findDetailsGrid(this.panel);
                assertNotNull(detailsGrid, "Details grid should be present");
                assertEquals(2, detailsGrid.getColumnConstraints().size(),
                                "Details grid should keep exactly 2 columns (label + field) to avoid GridPane resize-loop regressions");
        }


        private GridPane findDetailsGrid(Parent root)
        {
                for (javafx.scene.Node node : root.getChildrenUnmodifiable())
                {
                        if (node instanceof GridPane grid && isDetailsGrid(grid))
                        {
                                return grid;
                        }
                        if (node instanceof Parent child)
                        {
                                GridPane nested = findDetailsGrid(child);
                                if (nested != null)
                                {
                                        return nested;
                                }
                        }
                }
                return null;
        }

        private boolean isDetailsGrid(GridPane grid)
        {
                return grid.getChildren().stream()
                                .filter(Label.class::isInstance)
                                .map(Label.class::cast)
                                .map(Label::getText)
                                .anyMatch("Date"::equals);
        }

        private DatePicker fetchDatePicker()
        {
                final DatePicker[] dpRef = new DatePicker[1];
                Platform.runLater(() -> dpRef[0] = (DatePicker) this.panel.lookup("#datePicker"));
                WaitForAsyncUtils.waitForFxEvents();
                return dpRef[0];
        }

        private String fetchMemoText()
        {
                final String[] memo = new String[1];
                Platform.runLater(() -> memo[0] = ((TextArea) this.panel.lookup("#memoArea")).getText());
                WaitForAsyncUtils.waitForFxEvents();
                return memo[0];
        }
}

