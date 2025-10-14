
package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import nonprofitbookkeeping.api.CompanyCreatedCallback;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.base.NodeMatchers.isEnabled;
import static org.testfx.matcher.base.NodeMatchers.isDisabled;


public class CreateOrEditCompanyPanelFXTest extends JavaFXTestBase
{
	
	private CreateOrEditCompanyPanelFX panel;
	private CompanyCreatedCallback mockCallback;
	private Company newCompany;
	
	// Field values for testing
	private static final String COMPANY_NAME = "Test Co";
	private static final String LEGAL_STRUCTURE = "501(c)(3)";
	private static final String TAX_ID = "12-3456789";
	private static final String ADDRESS = "123 Main St";
	private static final String PHONE = "555-1234";
	private static final String EMAIL = "test@example.com";
	
	private static final String FISCAL_START = "2024-01-01";
	private static final String CURRENCY = "USD";
	private static final String START_BALANCE_DATE = "2024-01-01";
	private static final String CHART_OF_ACCOUNTS = "Standard Nonprofit";
	
	private static final String ADMIN_USER = "test_admin";
	private static final String ADMIN_PASS = "password123";
	private static final String DEFAULT_BANK = "Test Checking";
	
	
	@Start
	@Override public
		void start(Stage stage) throws Exception
	{
		this.newCompany = new Company(); // For creating a new company
		this.mockCallback = mock(CompanyCreatedCallback.class);
		this.panel = new CreateOrEditCompanyPanelFX(this.newCompany, this.mockCallback);
		Scene scene = new Scene(this.panel, 800, 600);
		stage.setScene(scene);
		stage.show();
	}
	
	@Test 
	public void testInitialState_Step1Visible_NavigationButtonsCorrect()
	{
		// Step 1: Company Information
		verifyThat("Company Information",
			(TitledPane pane) -> pane.isVisible() && pane.isExpanded());
		
		// Check fields for step 1 (presence)
		lookup((Node node) -> node instanceof TextField &&
			"Company Name:".equals(findLabelForNode(node))).queryAs(TextField.class);
		
		// Navigation buttons
		verifyThat("Back", (Button b) -> isDisabled().matches(b) && b.isVisible());
		verifyThat("Next", (Button b) -> isEnabled().matches(b) && b.isVisible());
		verifyThat("Save Company", (Button b) -> !b.isVisible());
	}
	
	@Test public void testNavigation_NextAndBackButtons()
	{
		// Step 1 to Step 2
		clickOn("Next");
		WaitForAsyncUtils.waitForFxEvents();
		verifyThat("Fiscal Settings", (TitledPane pane) -> pane.isVisible() && pane.isExpanded());
		verifyThat("Back", isEnabled());
		verifyThat("Next", isEnabled());
		verifyThat("Save Company", (Button b) -> !b.isVisible());
		
		// Step 2 to Step 3
		clickOn("Next");
		WaitForAsyncUtils.waitForFxEvents();
		verifyThat("Admin & Features", (TitledPane pane) -> pane.isVisible() && pane.isExpanded());
		verifyThat("Back", isEnabled());
		verifyThat("Next", isDisabled()); // Next should be disabled on the last step (replaced by
											// Save)
		verifyThat("Save Company", (Button b) -> isVisible().matches(b) && isEnabled().matches(b));
		
		
		// Step 3 to Step 2
		clickOn("Back");
		WaitForAsyncUtils.waitForFxEvents();
		verifyThat("Fiscal Settings", (TitledPane pane) -> pane.isVisible() && pane.isExpanded());
		verifyThat("Back", isEnabled());
		verifyThat("Next", isEnabled());
		verifyThat("Save Company", (Button b) -> !b.isVisible());
		
		// Step 2 to Step 1
		clickOn("Back");
		WaitForAsyncUtils.waitForFxEvents();
		verifyThat("Company Information",
			(TitledPane pane) -> pane.isVisible() && pane.isExpanded());
		verifyThat("Back", isDisabled());
		verifyThat("Next", isEnabled());
		verifyThat("Save Company", (Button b) -> !b.isVisible());
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	private static String findLabelForNode(Node node)
	{
		
		// Simplistic way to find a label associated with a control in a GridPane
		// Assumes Label is in the column before the control.
		if (node.getParent() instanceof GridPane)
		{
			GridPane grid = (GridPane) node.getParent();
			Integer colIndex = GridPane.getColumnIndex(node);
			Integer rowIndex = GridPane.getRowIndex(node);
			
			if (colIndex != null && rowIndex != null && colIndex > 0)
			{
				
				for (Node child : grid.getChildren())
				{
					
					if (GridPane.getRowIndex(child) != null &&
						GridPane.getRowIndex(child).equals(rowIndex) &&
						GridPane.getColumnIndex(child) != null &&
						GridPane.getColumnIndex(child).equals(colIndex - 1) &&
						child instanceof Label)
					{
						return ((Label) child).getText();
					}
					
				}
				
			}
			
		}
		
		return null;
	}
	
	
	/**
	 * 
	 */
	@Test public void testFillAndSaveForm_VerifyCallbackData()
	{
		// Step 1: Company Information
		// Need to be careful with lookups if fx:id is not set.
		// Assuming TextFields can be uniquely identified or by order/label.
		// For robust tests, fx:id would be better.
		// Let's try to find by associated label text if possible, or by type + order.
		
		// Fill Step 1
		((TextField) lookup((Node node) -> node instanceof TextField &&
			"Company Name:".equals(findLabelForNode(node))).query()).setText(COMPANY_NAME);
		selectInComboBox(findComboBoxByLabel("Legal Structure:"), LEGAL_STRUCTURE);
		((TextField) lookup(
			(Node node) -> node instanceof TextField && "Tax ID:".equals(findLabelForNode(node)))
			.query()).setText(TAX_ID);
		((TextField) lookup(
			(Node node) -> node instanceof TextField && "Address:".equals(findLabelForNode(node)))
			.query()).setText(ADDRESS);
		((TextField) lookup(
			(Node node) -> node instanceof TextField && "Phone:".equals(findLabelForNode(node)))
			.query()).setText(PHONE);
		((TextField) lookup(
			(Node node) -> node instanceof TextField && "Email:".equals(findLabelForNode(node)))
			.query()).setText(EMAIL);
		clickOn("Next");
		WaitForAsyncUtils.waitForFxEvents();
		
		// Fill Step 2
		((TextField) lookup((Node node) -> node instanceof TextField &&
			"Fiscal Year Start:".equals(findLabelForNode(node))).query()).setText(FISCAL_START);
		selectInComboBox(findComboBoxByLabel("Base Currency:"), CURRENCY);
		((TextField) lookup((Node node) -> node instanceof TextField &&
			"Starting Balance Date:".equals(findLabelForNode(node))).query())
			.setText(START_BALANCE_DATE);
		selectInComboBox(findComboBoxByLabel("Chart of Accounts:"), CHART_OF_ACCOUNTS);
		clickOn("Next");
		WaitForAsyncUtils.waitForFxEvents();
		
		// Fill Step 3
		((TextField) lookup((Node node) -> node instanceof TextField &&
			"Admin Username:".equals(findLabelForNode(node))).query()).setText(ADMIN_USER);
		((PasswordField) lookup((Node node) -> node instanceof PasswordField &&
			"Admin Password:".equals(findLabelForNode(node))).query()).setText(ADMIN_PASS);
		((TextField) lookup((Node node) -> node instanceof TextField &&
			"Default Bank Account:".equals(findLabelForNode(node))).query()).setText(DEFAULT_BANK);
		
		// Checkboxes - assuming fx:id or direct lookup possible if IDs were set.
		// If not, lookup by text.
		clickOn((CheckBox) lookup(".check-box")
			.match(cb -> ((CheckBox) cb).getText().equals("Enable Fund Accounting")).query()); // Example:
																								// click
																								// to
																								// change
																								// state
		clickOn((CheckBox) lookup(".check-box")
			.match(cb -> ((CheckBox) cb).getText().equals("Enable Inventory Tracking")).query());
		
		
		// Save
		clickOn("Save Company");
		WaitForAsyncUtils.waitForFxEvents();
		
		// Verify callback
                ArgumentCaptor<CompanyProfileModel> profileCaptor =
                        ArgumentCaptor.forClass(CompanyProfileModel.class);
                ArgumentCaptor<Boolean> seedCaptor = ArgumentCaptor.forClass(Boolean.class);
                verify(this.mockCallback, times(1)).onCreatedProfileModel(profileCaptor.capture(),
                        seedCaptor.capture());

                CompanyProfileModel savedModel = profileCaptor.getValue();
                assertEquals(COMPANY_NAME, savedModel.getCompanyName());
		assertEquals(LEGAL_STRUCTURE, savedModel.getLegalStructure());
		assertEquals(TAX_ID, savedModel.getTaxId());
		assertEquals(ADDRESS, savedModel.getAddress());
		assertEquals(PHONE, savedModel.getPhone());
		assertEquals(EMAIL, savedModel.getEmail());
		
		assertEquals(FISCAL_START, savedModel.getFiscalYearStart());
		assertEquals(CURRENCY, savedModel.getBaseCurrency());
		assertEquals(START_BALANCE_DATE, savedModel.getStartingBalanceDate());
		assertEquals(CHART_OF_ACCOUNTS, savedModel.getChartOfAccountsType());
		
		assertEquals(ADMIN_USER, savedModel.getAdminUsername());
		assertEquals(ADMIN_PASS, savedModel.getAdminPassword());
		assertEquals(DEFAULT_BANK, savedModel.getDefaultBankAccount());
		
		// Checkboxes: depends on initial state and if we clicked them
		// Assuming they were initially false and we clicked them to true
		assertTrue(savedModel.isEnableFundAccounting());
                assertTrue(savedModel.isEnableInventory());
                assertTrue(seedCaptor.getValue(), "Demo data seeding should default to enabled");
		// Assuming multiCurBox was not clicked and defaults to false or its initial
		// state
		// boolean expectedMultiCurrency = ((CheckBox) lookup(".check-box").match(cb ->
		// ((CheckBox)cb).getText().equals("Enable
		// Multi-Currency")).query()).isSelected();
		// assertEquals(expectedMultiCurrency, savedModel.isEnableMultiCurrency());
	}
	
	private ComboBox<String> findComboBoxByLabel(String labelText)
	{
		return lookup(
			(Node node) -> node instanceof ComboBox && labelText.equals(findLabelForNode(node)))
			.queryComboBox();
	}
	
	private static void selectInComboBox(ComboBox<String> comboBox, String valueToSelect)
	{
		Platform.runLater(() -> comboBox.setValue(valueToSelect));
		WaitForAsyncUtils.waitForFxEvents();
	}
	
	// TODO: Add tests for input validation if any specific validation rules are
	// implemented in the panel.
	// For example, checking if required fields are empty, date formats, etc.
	// The current panel code doesn't show explicit validation logic before saving,
	// so such tests would depend on future enhancements to the panel.
}
