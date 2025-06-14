
package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import nonprofitbookkeeping.ui.panels.AccountsActivityPanelFX.TransactionRow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;
import static org.testfx.matcher.control.LabeledMatchers.hasText; // For buttons/labels


public class AccountsActivityPanelFXTest extends JavaFXTestBase
{
	
	private AccountsActivityPanelFX panel;
	private TableView<TransactionRow> table;
	private ComboBox<String> accountSelector;
	private TextField filterDateField;
	private TextField filterMemoField;
	private TextField filterAmountField;
	private Button applyFiltersButton;
	
	@Mock private Ledger mockLedger;
	@Mock private ChartOfAccounts mockCoa;
	
	private Company testCompany;
	private List<AccountingTransaction> allTransactions;
	
	// Account Names
	private static final String BANK_ACCOUNT_NAME = "Bank Checking";
	private static final String INCOME_ACCOUNT_NAME = "Donation Income";
	private static final String EXPENSE_ACCOUNT_NAME = "Office Supplies";
	
	
	@Start
	@Override
	@SuppressWarnings("unchecked") public
		void start(Stage stage) throws Exception
	{
		MockitoAnnotations.openMocks(this);
		
		// Setup Chart of Accounts
		Account bankAccount =
			new Account("1010", BANK_ACCOUNT_NAME, AccountType.ASSET, BigDecimal.ZERO);
		Account incomeAccount =
			new Account("4010", INCOME_ACCOUNT_NAME, AccountType.INCOME, BigDecimal.ZERO);
		Account expenseAccount =
			new Account("6010", EXPENSE_ACCOUNT_NAME, AccountType.EXPENSE, BigDecimal.ZERO);
		when(mockCoa.getAccounts())
			.thenReturn(Arrays.asList(bankAccount, incomeAccount, expenseAccount));
		
		// Setup Company and CurrentCompany
		testCompany = new Company();
		CompanyProfileModel profile = new CompanyProfileModel();
		profile.setCompanyName("Activity Test Co");
		testCompany.setCompanyProfile(profile);
		testCompany.setChartOfAccounts(mockCoa);
		CurrentCompany.forceCompanyLoad(testCompany); // Make sure COA is available for
														// accountSelector
		
		// Setup Ledger and Transactions
		allTransactions = new ArrayList<>();
		// Bank Transactions
		allTransactions.add(
			createTx("2024-01-05", "Deposit", BANK_ACCOUNT_NAME, "1000.00", "Initial Deposit"));
		allTransactions.add(
			createTx("2024-01-10", "Check #101", BANK_ACCOUNT_NAME, "-50.00", "Supplies Purchase"));
		// Income Transactions
		allTransactions.add(createTx("2024-01-15", "Donation Received", INCOME_ACCOUNT_NAME,
			"200.00", "Gala Event"));
		// Expense Transactions
		allTransactions.add(createTx("2024-01-10", "Office Supplies", EXPENSE_ACCOUNT_NAME, "50.00",
			"Supplies Purchase Memo"));
		allTransactions.add(
			createTx("2024-01-20", "Rent Payment", EXPENSE_ACCOUNT_NAME, "300.00", "Monthly Rent"));
		
		when(mockLedger.getTransactions()).thenReturn(allTransactions);
		
		panel = new AccountsActivityPanelFX(mockLedger);
		Scene scene = new Scene(panel, 1024, 768); // Wider for more columns
		stage.setScene(scene);
		stage.show();
		
		// Assign UI elements (assuming fx:id or robust lookup)
		// For this panel, direct field access is not possible, so we use lookups.
		// It's better if panel sets fx:id for these.
		accountSelector = lookup("#accountSelector").queryComboBox();
		filterDateField = lookup("#filterDateField").queryAs(TextField.class);
		filterMemoField = lookup("#filterMemoField").queryAs(TextField.class);
		filterAmountField = lookup("#filterAmountField").queryAs(TextField.class);
		applyFiltersButton = lookup("Apply Filters").queryButton(); // by text
		table = lookup(".table-view").queryTableView();
	}
	
	private AccountingTransaction createTx(	String date, String desc, String accName, String amount,
											String memo)
	{
		AccountingTransaction tx = new AccountingTransaction();
		tx.setDate(date);
		tx.setDescription(desc);
		tx.setAccountName(accName); // This is important for filtering by account
		tx.setTotalAmount(new BigDecimal(amount)); // Assuming positive for income/asset increase,
													// negative for decrease
		tx.setMemo(memo);
		// For simplicity, TransactionRow uses these fields.
		// If TransactionRow used entries, we'd need to add AccountingEntry objects.
		return tx;
	}
	
	@BeforeEach public void assignFxIds()
	{
		// Helper to assign fx:id to nodes for easier lookup if not set in source
		// This is a workaround for testing. Ideally, panel code sets fx:id.
		Platform.runLater(() -> {
			if (panel == null || panel.getTop() == null)
				return;
				
			// AccountSelector is in a HBox, first ComboBox in that HBox
			// The panel structure is VBox -> HBox (for selector) -> HBox (for filters) ->
			// TableView
			VBox topVBox = (VBox) panel.getTop();
			HBox selectorPane = (HBox) topVBox.getChildren().get(0);
			
			if (selectorPane != null && selectorPane.getChildren().size() > 1 &&
				selectorPane.getChildren().get(1) instanceof ComboBox)
			{
				((ComboBox<?>) selectorPane.getChildren().get(1)).setId("accountSelector");
			}
			
			// Filter fields are in another HBox
			HBox filterPane = (HBox) topVBox.getChildren().get(1);
			
			if (filterPane != null && filterPane.getChildren().size() > 5)
			{ // Check for enough children
				// Order: Label, TextField, Label, TextField, Label, TextField, Button
				if (filterPane.getChildren().get(1) instanceof TextField)
					((TextField) filterPane.getChildren().get(1)).setId("filterDateField");
				if (filterPane.getChildren().get(3) instanceof TextField)
					((TextField) filterPane.getChildren().get(3)).setId("filterMemoField");
				if (filterPane.getChildren().get(5) instanceof TextField)
					((TextField) filterPane.getChildren().get(5)).setId("filterAmountField");
			}
			
		});
		WaitForAsyncUtils.waitForFxEvents();
	}
	
	
	@AfterEach public void tearDown()
	{
		CurrentCompany.close();
	}
	
	@Test public void testInitialState_AccountSelectorPopulated_TableShowsFirstAccountActivity()
	{
		assertNotNull(accountSelector);
		assertNotNull(filterDateField);
		assertNotNull(filterMemoField);
		assertNotNull(filterAmountField);
		assertNotNull(applyFiltersButton);
		assertNotNull(table);
		
		// Account selector should be populated from mockCoa
		assertEquals(3, accountSelector.getItems().size());
		assertTrue(accountSelector.getItems().contains(BANK_ACCOUNT_NAME));
		assertEquals(BANK_ACCOUNT_NAME, accountSelector.getValue(),
			"First account should be selected by default.");
		
		// Table should show transactions for the initially selected account (Bank
		// Checking)
		// The panel calls applyFilters() in constructor -> which calls
		// filterAndDisplayTransactions
		verifyThat(table, hasNumRows(2)); // 2 bank transactions
	}
	
	@Test public void testAccountSelection_UpdatesTable()
	{
		Platform.runLater(() -> accountSelector.setValue(EXPENSE_ACCOUNT_NAME));
		WaitForAsyncUtils.waitForFxEvents(); // applyFilters is called on accountSelector action
		
		verifyThat(table, hasNumRows(2)); // 2 expense transactions
		// Verify content if necessary, e.g., check one row's description
		assertTrue(table.getItems().stream()
			.anyMatch(r -> "Office Supplies".equals(r.descriptionProperty().get())));
	}
	
	@Test public void testFilterByDate()
	{
		Platform.runLater(() -> accountSelector.setValue(BANK_ACCOUNT_NAME)); // 2 bank tx initially
		WaitForAsyncUtils.waitForFxEvents();
		
		filterDateField.setText("2024-01-10");
		clickOn(applyFiltersButton);
		WaitForAsyncUtils.waitForFxEvents();
		
		verifyThat(table, hasNumRows(1)); // Only "Check #101"
		assertEquals("Check #101", table.getItems().get(0).descriptionProperty().get());
	}
	
	@Test public void testFilterByMemo()
	{
		Platform.runLater(() -> accountSelector.setValue(BANK_ACCOUNT_NAME));
		WaitForAsyncUtils.waitForFxEvents();
		
		filterMemoField.setText("Initial"); // Case insensitive partial match
		clickOn(applyFiltersButton);
		WaitForAsyncUtils.waitForFxEvents();
		
		verifyThat(table, hasNumRows(1));
		assertEquals("Initial Deposit", table.getItems().get(0).memoProperty().get());
	}
	
	@Test public void testFilterByAmount_ExactMatch()
	{
		Platform.runLater(() -> accountSelector.setValue(EXPENSE_ACCOUNT_NAME)); // Rent is 300.00
		WaitForAsyncUtils.waitForFxEvents();
		
		filterAmountField.setText("300.00");
		clickOn(applyFiltersButton);
		WaitForAsyncUtils.waitForFxEvents();
		
		verifyThat(table, hasNumRows(1));
		assertEquals("Rent Payment", table.getItems().get(0).descriptionProperty().get());
	}
	
	@Test public void testFilterByAmount_InvalidFormat_ShowsAllForAccount()
	{
		Platform.runLater(() -> accountSelector.setValue(BANK_ACCOUNT_NAME));
		WaitForAsyncUtils.waitForFxEvents();
		int initialRows = table.getItems().size(); // Should be 2 for Bank
		
		filterAmountField.setText("abc"); // Invalid amount
		clickOn(applyFiltersButton);
		WaitForAsyncUtils.waitForFxEvents();
		
		// Panel logs error, amountFilter internal field becomes null, so no amount
		// filtering applied
		verifyThat(table, hasNumRows(initialRows));
	}
	
	@Test public void testCombinedFilters()
	{
		Platform.runLater(() -> accountSelector.setValue(BANK_ACCOUNT_NAME));
		WaitForAsyncUtils.waitForFxEvents();
		
		filterDateField.setText("2024-01-10");
		filterMemoField.setText("Supplies");
		// filterAmountField.setText("-50.00"); // TotalAmount is used, which might be
		// positive for expense in some conventions
		// Let's use the amount from createTx for "Check #101" which is -50.00
		filterAmountField.setText("-50.00");
		
		clickOn(applyFiltersButton);
		WaitForAsyncUtils.waitForFxEvents();
		
		verifyThat(table, hasNumRows(1));
		TransactionRow row = table.getItems().get(0);
		assertEquals("Check #101", row.descriptionProperty().get());
		assertEquals("2024-01-10", row.dateProperty().get());
		assertEquals("Supplies Purchase", row.memoProperty().get());
		assertEquals(new BigDecimal("-50.00"), row.amountProperty().get());
	}
	
	@Test public void testBottomButtons_ShowAlerts()
	{
		// Test Reconcile button
		clickOn("Reconcile");
		WaitForAsyncUtils.waitForFxEvents();
		DialogPane reconcileAlert = getTopModalDialogPane();
		assertNotNull(reconcileAlert, "Reconcile alert not shown.");
		assertTrue(
			reconcileAlert.getContentText().contains("Reconciliation process would start here."));
		clickOn((Button) reconcileAlert.lookupButton(ButtonType.OK));
		WaitForAsyncUtils.waitForFxEvents();
		
		// Test Import Statement button
		clickOn("Import Statement (CSV/QIF/OFX)");
		WaitForAsyncUtils.waitForFxEvents();
		DialogPane importAlert = getTopModalDialogPane();
		assertNotNull(importAlert, "Import alert not shown.");
		assertTrue(importAlert.getContentText().contains("Import dialog not implemented."));
		clickOn((Button) importAlert.lookupButton(ButtonType.OK));
		WaitForAsyncUtils.waitForFxEvents();
	}
	
	private DialogPane getTopModalDialogPane()
	{
		Optional<Node> dialogPaneOpt = lookup((Node n) -> n instanceof DialogPane &&
			n.getScene() != null && n.getScene().getWindow() instanceof Stage &&
			((Stage) n.getScene().getWindow()).isShowing()).tryQuery();
		return (DialogPane) dialogPaneOpt.orElse(null);
	}
	
}
