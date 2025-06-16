
package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;


public class CoaEditorPanelFXTest extends JavaFXTestBase
{
	
	private CoaEditorPanelFX panel;
	private ChartOfAccounts testCoa;
	private Consumer<ChartOfAccounts> mockOnSave;
	private Runnable mockOnClose;
	
	private TreeTableView<Account> tree;
	
	@Start
	@Override
	@SuppressWarnings("unchecked") public
		void start(Stage stage) throws Exception
	{
		MockitoAnnotations.openMocks(this); // Initialize mocks
		
		this.testCoa = new ChartOfAccounts();
		// Add some initial accounts for testing
		Account rootAsset = new Account("1000", "Assets", AccountType.ASSET, BigDecimal.ZERO);
		this.testCoa.addAccount(rootAsset);
		
		Account rootEquity = new Account("3000", "Equity", AccountType.EQUITY, BigDecimal.ZERO);
		this.testCoa.addAccount(rootEquity);
		
		this.mockOnSave = mock(Consumer.class);
		this.mockOnClose = mock(Runnable.class);
		
		this.panel = new CoaEditorPanelFX(this.testCoa, this.mockOnSave, this.mockOnClose);
		Scene scene = new Scene(this.panel, 800, 600);
		stage.setScene(scene);
		stage.show();
		
		this.tree = lookup(".tree-table-view").query();
	}
	
	@BeforeEach public void resetSelection()
	{
		Platform.runLater(() -> this.tree.getSelectionModel().clearSelection());
		WaitForAsyncUtils.waitForFxEvents();
	}
	
	@Test public void testInitialDisplay_ShowsAccounts()
	{
		assertNotNull(this.tree);
		// Expecting 2 root accounts (Assets, Equity)
		// The actual TreeTableView rows = root items + their children if expanded
		// The CoaEditorPanelFX expands nodes by default.
		// Root "Assets" (1) + child "Bank" (1) + Root "Equity" (1) = 3 rows
		assertEquals(3, this.tree.getRoot().getChildren().stream()
			.mapToInt(ti -> 1 + ti.getChildren().size()).sum());
		
		TreeItem<Account> assetsItem = findTreeItem(this.tree.getRoot(), "Assets");
		assertNotNull(assetsItem);
		assertEquals(1, assetsItem.getChildren().size()); // Bank
		assertEquals("Bank", assetsItem.getChildren().get(0).getValue().getName());
	}
	
	@Test public void testAddRootAccount_Successful()
	{
		clickOn("Add Root");
		WaitForAsyncUtils.waitForFxEvents();
		
		// Dialog interaction
		fillAccountDialog("4000", "Liabilities", AccountType.LIABILITY, "500.00");
		clickOkInDialog();
		
		// Verify new root account in tree
		TreeItem<Account> liabilitiesItem = findTreeItem(this.tree.getRoot(), "Liabilities");
		assertNotNull(liabilitiesItem);
		assertEquals("4000", liabilitiesItem.getValue().getAccountNumber());
		assertEquals(BigDecimal.valueOf(500.00).stripTrailingZeros(),
			liabilitiesItem.getValue().getOpeningBalance().stripTrailingZeros());
	}
	
	@Test public void testAddSubAccount_Successful()
	{
		TreeItem<Account> assetsItem = selectTreeItem(this.tree, "Assets"); // Select "Assets"
		assertNotNull(assetsItem, "Assets account not found or could not be selected");
		
		clickOn("Add Sub-account");
		WaitForAsyncUtils.waitForFxEvents();
		
		fillAccountDialog("1020", "Petty Cash", AccountType.ASSET, "50.00");
		clickOkInDialog();
		
		WaitForAsyncUtils.waitForFxEvents(); // Ensure tree updates
		
		TreeItem<Account> pettyCashItem = findTreeItem(assetsItem, "Petty Cash");
		assertNotNull(pettyCashItem, "Petty Cash sub-account not found under Assets");
		assertEquals("1020", pettyCashItem.getValue().getAccountNumber());
	}
	
	@Test public void testEditAccount_Successful()
	{
		TreeItem<Account> bankItem = selectTreeItem(this.tree, "Bank");
		assertNotNull(bankItem, "Bank account not found or could not be selected");
		
		clickOn("Edit");
		WaitForAsyncUtils.waitForFxEvents();
		
		fillAccountDialog(null, "Main Bank Account", null, "1200.00"); // Edit name and balance
		clickOkInDialog();
		WaitForAsyncUtils.waitForFxEvents();
		
		assertEquals("Main Bank Account", bankItem.getValue().getName());
		assertEquals(BigDecimal.valueOf(1200.00).stripTrailingZeros(),
			bankItem.getValue().getOpeningBalance().stripTrailingZeros());
		this.tree.refresh(); // Ensure UI reflects change
		assertEquals("Main Bank Account", bankItem.getValue().getName()); // Double check after
																			// refresh
	}
	
	@Test public void testDeleteAccount_Successful()
	{
		selectTreeItem(this.tree, "Bank"); // Select "Bank"
		clickOn("Delete");
		WaitForAsyncUtils.waitForFxEvents(); // Deletion happens directly
		
		TreeItem<Account> assetsItem = findTreeItem(this.tree.getRoot(), "Assets");
		assertNotNull(assetsItem);
		assertTrue(findTreeItem(assetsItem, "Bank") == null, "Bank account should be deleted");
		assertEquals(0, assetsItem.getChildren().size()); // Assets should have no children now
	}
	
	@Test public void testSaveButton_InvokesCallback()
	{
		clickOn("Save");
		WaitForAsyncUtils.waitForFxEvents();
		
		ArgumentCaptor<ChartOfAccounts> captor = ArgumentCaptor.forClass(ChartOfAccounts.class);
		verify(this.mockOnSave, times(1)).accept(captor.capture());
		verify(this.mockOnClose, times(1)).run(); // onClose is also called
		
		ChartOfAccounts savedCoa = captor.getValue();
		assertNotNull(savedCoa);
		// Could add more assertions about the state of savedCoa if specific changes
		// were made
	}
	
        @Test public void testCancelButton_InvokesCallback()
        {
                clickOn("Cancel");
                WaitForAsyncUtils.waitForFxEvents();

                verify(this.mockOnSave, never()).accept(any());
                verify(this.mockOnClose, times(1)).run();
        }

        @Test public void testClosePanel_FiresCompanyOpenEvent()
        {
                try (MockedStatic<CurrentCompany> mocked = mockStatic(CurrentCompany.class))
                {
                        clickOn("Cancel");
                        WaitForAsyncUtils.waitForFxEvents();

                        mocked.verify(CurrentCompany::markCompanyOpen, times(1));
                }
        }
	
	// --- Helper methods for dialogs and tree ---
	
	private void fillAccountDialog(String number, String name, AccountType type, String balance)
	{
		DialogPane dialogPane = getTopModalDialogPane();
		assertNotNull(dialogPane, "Dialog not found");
		
		if (number != null)
		{
			TextField numField = from(dialogPane).lookup(".text-field").nth(0).query(); // Assumes
																						// order
			numField.setText(number);
		}
		
		if (name != null)
		{
			TextField nameField =
				from(dialogPane).lookup(".text-field").nth(number == null ? 0 : 1).query(); // Adjust
																							// index
																							// if
																							// number
																							// field
																							// was
																							// skipped
			nameField.setText(name);
		}
		
		if (type != null)
		{
			ComboBox<AccountType> typeCombo =
				from(dialogPane).lookup(".combo-box").queryComboBox();
			Platform.runLater(() -> typeCombo.setValue(type));
			WaitForAsyncUtils.waitForFxEvents();
		}
		
		if (balance != null)
		{
			List<Node> textFields = from(dialogPane).lookup(".text-field").queryAll().stream()
				.collect(Collectors.toList());
			// Balance field is the last text field usually
			TextField balField = (TextField) textFields.get(textFields.size() - 1);
			balField.setText(balance);
		}
		
	}
	
	private void clickOkInDialog()
	{
		DialogPane dialogPane = getTopModalDialogPane();
		assertNotNull(dialogPane, "Dialog not found for OK click");
		Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
		clickOn(okButton);
		WaitForAsyncUtils.waitForFxEvents();
	}
	
	private DialogPane getTopModalDialogPane()
	{
		// Get the top-most dialog stage
		List<Stage> stages = listTargetWindows().stream()
			.filter(w -> w instanceof Stage)
			.map(w -> (Stage) w)
			.filter(s -> s.getModality() == javafx.stage.Modality.APPLICATION_MODAL ||
				s.getModality() == javafx.stage.Modality.WINDOW_MODAL)
			.filter(Stage::isShowing)
			.collect(Collectors.toList());
		if (stages.isEmpty())
			return null;
		Stage currentDialogStage = stages.get(stages.size() - 1); // Get the newest stage
		return (DialogPane) currentDialogStage.getScene().getRoot();
	}
	
	private TreeItem<Account> selectTreeItem(TreeTableView<Account> ttv, String accountName)
	{
		TreeItem<Account> itemToSelect = findTreeItem(ttv.getRoot(), accountName);
		
		if (itemToSelect != null)
		{
			final TreeItem<Account> finalItem = itemToSelect; // Effectively final for lambda
			Platform.runLater(() -> {
				ttv.getSelectionModel().select(finalItem);
				// Scroll to the item to ensure it's visible for interaction, if necessary
				int rowIndex = ttv.getRow(finalItem);
				
				if (rowIndex != -1)
				{
					ttv.scrollTo(rowIndex);
				}
				
			});
			WaitForAsyncUtils.waitForFxEvents();
			return finalItem;
		}
		
		return null;
	}
	
	private TreeItem<Account> findTreeItem(TreeItem<Account> root, String accountName)
	{
		if (root == null)
			return null;
			
		if (root.getValue() != null && accountName.equals(root.getValue().getName()))
		{
			return root;
		}
		
		for (TreeItem<Account> child : root.getChildren())
		{
			TreeItem<Account> found = findTreeItem(child, accountName);
			
			if (found != null)
			{
				return found;
			}
			
		}
		
		return null;
	}
	
}
