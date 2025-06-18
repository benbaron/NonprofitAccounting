package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import nonprofitbookkeeping.ui.panels.NewTransactionPanelFX.Line;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isDisabled;
import static org.testfx.matcher.base.NodeMatchers.isEnabled;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;


public class NewTransactionPanelFXTest extends JavaFXTestBase {

    private NewTransactionPanelFX panel;
    private Company testCompany;
    private ChartOfAccounts testCoa;

    @Mock
    private Consumer<AccountingTransaction> mockOnSave;

    private TableView<Line> table;


    @Start
    @Override
    public void start(Stage stage) throws Exception {
        MockitoAnnotations.openMocks(this);

        this.testCoa = new ChartOfAccounts();
        Account assetAcc = new Account("1010", "Bank", AccountType.ASSET, BigDecimal.ZERO);
        assetAcc.setIncreaseSide(AccountSide.DEBIT); // Explicitly set for test
        Account expenseAcc = new Account("6010", "Office Supplies", AccountType.EXPENSE, BigDecimal.ZERO);
        expenseAcc.setIncreaseSide(AccountSide.DEBIT);
        Account incomeAcc = new Account("4010", "Donations", AccountType.INCOME, BigDecimal.ZERO);
        incomeAcc.setIncreaseSide(AccountSide.CREDIT);

        this.testCoa.addAccount(assetAcc);
        this.testCoa.addAccount(expenseAcc);
        this.testCoa.addAccount(incomeAcc);

        this.testCompany = new Company();
        CompanyProfileModel profile = new CompanyProfileModel();
        profile.setCompanyName("Test NewTransaction Co");
        this.testCompany.setCompanyProfile(profile);
        this.testCompany.setChartOfAccounts(this.testCoa);

        CurrentCompany.forceCompanyLoad(this.testCompany);

        // For creating a new transaction
        this.panel = new NewTransactionPanelFX(this.mockOnSave);
        Scene scene = new Scene(this.panel, 800, 600);
        stage.setScene(scene);
        stage.show();

        this.table = lookup(".table-view").queryTableView();
    }

    @AfterEach
    public void tearDown() {
        CurrentCompany.close();
    }

    @Test
    public void testInitialState_SaveDisabled_OneEmptyLine() {
        verifyThat("#datePicker", (DatePicker dp) -> dp.getValue().equals(LocalDate.now()));
        verifyThat("#memoArea", (TextArea ta) -> ta.getText().isEmpty());
        verifyThat(this.table, hasNumRows(0)); // Panel starts with 0 lines, user clicks "+ Entry"

        clickOn("+ Entry"); // Add first line
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(this.table, hasNumRows(1));

        Line firstLine = this.table.getItems().get(0);
        assertEquals("", firstLine.account.get());
        assertEquals(AccountSide.DEBIT, firstLine.side.get());
        assertEquals(BigDecimal.ZERO, firstLine.amount.get());

        verifyThat("#saveBtn", isDisabled());
    }

    @Test
    public void testAddAndRemoveLines() {
        clickOn("+ Entry");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(this.table, hasNumRows(1));

        clickOn("+ Entry");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(this.table, hasNumRows(2));

        Platform.runLater(() -> this.table.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("Remove");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(this.table, hasNumRows(1));

        Platform.runLater(() -> this.table.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("Remove");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(this.table, hasNumRows(0));
        verifyThat("#saveBtn", isDisabled());
    }

    @Test
    public void testEditLine_AccountSideAmount_SaveEnablesWhenBalanced() {
        clickOn("+ Entry"); // Line 1
        clickOn("+ Entry"); // Line 2
        WaitForAsyncUtils.waitForFxEvents();

        // Edit Line 1: Bank DEBIT 100
        editCell(0, 0, "Bank"); // Account column
        editCell(0, 1, AccountSide.DEBIT); // Side column
        editCell(0, 2, "100.00"); // Amount column

        Line line1 = this.table.getItems().get(0);
        assertEquals("Bank", line1.account.get());
        assertEquals(AccountSide.DEBIT, line1.side.get());
        assertEquals(new BigDecimal("100.00"), line1.amount.get());
        verifyThat("#saveBtn", isDisabled()); // Not balanced yet

        // Edit Line 2: Donations CREDIT 100
        editCell(1, 0, "Donations");
        editCell(1, 1, AccountSide.CREDIT);
        editCell(1, 2, "100.00");

        Line line2 = this.table.getItems().get(1);
        assertEquals("Donations", line2.account.get());
        assertEquals(AccountSide.CREDIT, line2.side.get());
        assertEquals(new BigDecimal("100.00"), line2.amount.get());

        verifyThat("#saveBtn", isEnabled()); // Should be balanced

        // Make it unbalanced again
        editCell(1, 2, "50.00");
        verifyThat("#saveBtn", isDisabled());
    }

    @Test
    public void testSaveButton_InvokesCallbackWithCorrectData() {
        // Set Date and Memo
        LocalDate testDate = LocalDate.of(2024, 7, 31);
        String testMemo = "Test transaction memo";
        lookup("#datePicker").queryAs(DatePicker.class).setValue(testDate);
        lookup("#memoArea").queryAs(TextArea.class).setText(testMemo);

        // Add lines
        clickOn("+ Entry");
        clickOn("+ Entry");
        WaitForAsyncUtils.waitForFxEvents();

        editCell(0, 0, "Bank");
        editCell(0, 1, AccountSide.DEBIT);
        editCell(0, 2, "150.00");

        editCell(1, 0, "Donations");
        editCell(1, 1, AccountSide.CREDIT);
        editCell(1, 2, "150.00");

        verifyThat("#saveBtn", isEnabled());
        clickOn("#saveBtn");
        WaitForAsyncUtils.waitForFxEvents();

        ArgumentCaptor<AccountingTransaction> captor = ArgumentCaptor.forClass(AccountingTransaction.class);
        verify(this.mockOnSave, times(1)).accept(captor.capture());

        AccountingTransaction savedTx = captor.getValue();
        assertNotNull(savedTx);
        assertEquals(testDate.toString(), savedTx.getDate());
        assertEquals(testMemo, savedTx.getDescription());

        Set<AccountingEntry> entries = savedTx.getEntries();
        assertEquals(2, entries.size());

        assertTrue(entries.contains(new AccountingEntry(new BigDecimal("150.00"), "Bank", AccountSide.DEBIT)));
        assertTrue(entries.contains(new AccountingEntry(new BigDecimal("150.00"), "Donations", AccountSide.CREDIT)));
    }

    @Test
    public void testLoadExistingTransaction_PopulatesFieldsAndLines() {
        AccountingTransaction existingTx = new AccountingTransaction();
        existingTx.setDate("2023-11-01");
        existingTx.setDescription("Existing Memo");
        existingTx.addEntry(new AccountingEntry(new BigDecimal("75.00"), "Office Supplies", AccountSide.DEBIT));
        existingTx.addEntry(new AccountingEntry(new BigDecimal("75.00"), "Bank", AccountSide.CREDIT));

        // Create a new panel instance for editing
        Platform.runLater(() -> {
            NewTransactionPanelFX editPanel = new NewTransactionPanelFX(existingTx, this.mockOnSave);
            Stage stage = (Stage) this.panel.getScene().getWindow(); // Reuse stage for simplicity
            stage.setScene(new Scene(editPanel, 800, 600));
            // Update table reference for this test
            this.table = from(editPanel).lookup(".table-view").queryTableView();
        });
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat("#datePicker", (DatePicker dp) -> dp.getValue().equals(LocalDate.of(2023,11,1)));
        verifyThat("#memoArea", (TextArea ta) -> ta.getText().equals("Existing Memo"));

        verifyThat(this.table, hasNumRows(2));
        Line line1 = this.table.getItems().get(0); // Order might not be guaranteed from Set to List
        Line line2 = this.table.getItems().get(1);

        boolean foundSupplies = false;
        boolean foundBank = false;

        if ("Office Supplies".equals(line1.account.get())) {
            foundSupplies = true;
            assertEquals(AccountSide.DEBIT, line1.side.get());
            assertEquals(new BigDecimal("75.00"), line1.amount.get());
        } else if ("Bank".equals(line1.account.get())) {
            foundBank = true;
            assertEquals(AccountSide.CREDIT, line1.side.get());
            assertEquals(new BigDecimal("75.00"), line1.amount.get());
        }

        if ("Office Supplies".equals(line2.account.get())) {
            foundSupplies = true;
            assertEquals(AccountSide.DEBIT, line2.side.get());
            assertEquals(new BigDecimal("75.00"), line2.amount.get());
        } else if ("Bank".equals(line2.account.get())) {
            foundBank = true;
            assertEquals(AccountSide.CREDIT, line2.side.get());
            assertEquals(new BigDecimal("75.00"), line2.amount.get());
        }
        assertTrue(foundBank && foundSupplies, "Both entries from existing transaction should be in table");
        verifyThat("#saveBtn", isEnabled()); // Should be balanced
    }


    // More robust editCell, particularly for ComboBoxTableCell used for "Account"
    private void editCell(int rowIndex, int colIndex, String textValueForComboBox) {
        Platform.runLater(() -> {
            this.table.edit(rowIndex, this.table.getColumns().get(colIndex));
        });
        WaitForAsyncUtils.waitForFxEvents();

        // The editor for ComboBoxTableCell is a ComboBox itself.
        ComboBox<String> comboBoxEditor = lookup(".combo-box-table-cell .combo-box").nth(0).queryComboBox();
        Platform.runLater(() -> comboBoxEditor.getEditor().setText(textValueForComboBox));
        WaitForAsyncUtils.waitForFxEvents();
        press(javafx.scene.input.KeyCode.ENTER).release(javafx.scene.input.KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
         clickOn(lookup(".table-view").queryTableView()); // Click outside to try to commit
        WaitForAsyncUtils.waitForFxEvents();
    }

    // For setting AccountSide which uses ChoiceBoxTableCell
    private void editCell(int rowIndex, int colIndex, AccountSide sideValue) {
         Platform.runLater(() -> {
            this.table.edit(rowIndex, this.table.getColumns().get(colIndex));
        });
        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<AccountSide> choiceBoxEditor = lookup(".choice-box-table-cell .choice-box").nth(0).queryComboBox();
        Platform.runLater(() -> choiceBoxEditor.setValue(sideValue));
        WaitForAsyncUtils.waitForFxEvents();
        press(javafx.scene.input.KeyCode.ENTER).release(javafx.scene.input.KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
         clickOn(lookup(".table-view").queryTableView()); // Click outside to try to commit
        WaitForAsyncUtils.waitForFxEvents();
    }


    // Assign fx:id to nodes for easier lookup if not set in source
    @BeforeEach
    public void assignFxIds() {
        Platform.runLater(() -> {
            if (this.panel == null) return;
            lookup( (Node n) -> n instanceof DatePicker).queryAs(DatePicker.class).setId("datePicker");
            lookup( (Node n) -> n instanceof TextArea).queryAs(TextArea.class).setId("memoArea");

            // Buttons are in a ToolBar. Toolbar buttons don't get IDs easily unless set.
            // We'll rely on text lookup for buttons like "+ Entry", "Remove", "Save".
            // For saveBtn, panel field is used.
            Button saveButton = from(this.panel).lookup((Node node) -> node instanceof Button && "Save".equals(((Button)node).getText())).queryButton();
            if (saveButton != null) saveButton.setId("saveBtn");
        });
        WaitForAsyncUtils.waitForFxEvents();
    }
}
