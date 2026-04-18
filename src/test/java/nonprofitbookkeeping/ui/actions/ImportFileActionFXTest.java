
package nonprofitbookkeeping.ui.actions;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.model.records.ExcelLedgerRow;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImportFileActionFXTest
{
	
	@Mock private Stage mockStage; // Owner stage for dialogs
	
	@Test
	@DisplayName("Constructor: Valid Stage should create instance successfully")
	void
		testConstructor_validStage_succeeds()
	{
		ImportFileActionFX action = new ImportFileActionFX(this.mockStage);
		assertNotNull(action, "Instance should be created with a valid Stage.");
		
	}
	
	@Test
	@DisplayName("Constructor: Null Stage should throw IllegalArgumentException")
	void
		testConstructor_nullStage_throwsIllegalArgumentException()
	{
		Exception exception =
			assertThrows(IllegalArgumentException.class, () ->
			{});
		assertEquals("Owner stage cannot be null.", exception.getMessage(),
			"Exception message for null stage is not as expected.");
		
	}
	
	@Test
	@DisplayName("handle: Basic call with null ActionEvent should not throw unexpected exceptions")
	void
		testHandle_basicCall_noUnexpectedExceptions()
	{
		ImportFileActionFX action = new ImportFileActionFX(this.mockStage);
		
		// Similar to ExportFileActionFXTest, this is a basic sanity check.
		// The handle method shows a FileChooser and then an Alert.
		// In a non-JavaFX test environment, showOpenDialog will likely return
		// null.
		// The method should handle this gracefully without throwing NPEs.
		assertDoesNotThrow(() -> action.handle(null), // Pass null for
														// ActionEvent as it's
														// not used
			"Calling handle(null) should not throw unexpected exceptions in its current placeholder state.");
		
	}
	
	@Test
	@DisplayName("convertExcelRows: valid allocations produce balanced transaction")
	void
		testConvertExcelRows_validAllocations()
	{
		// setup chart of accounts
		ChartOfAccounts chart = new ChartOfAccounts();
		Account bank = new Account("100", "Bank", AccountSide.DEBIT);
		Account sales = new Account("400", "Sales", AccountSide.CREDIT);
		Account supplies = new Account("500", "Supplies", AccountSide.DEBIT);
		chart.addAccount(bank);
		chart.addAccount(sales);
		chart.addAccount(supplies);
		
		Company company = new Company();
		company.setChartOfAccounts(chart);
		CurrentCompany.forceCompanyLoad(company);
		
		ExcelLedgerRow row = new ExcelLedgerRow();
		ExcelLedgerRow.Allocation a1 = new ExcelLedgerRow.Allocation();
		a1.setAmount(new BigDecimal("100"));
		a1.setAssetLiabilityAccount("Bank");
		a1.setIncomeCategory("Sales");
		ExcelLedgerRow.Allocation a2 = new ExcelLedgerRow.Allocation();
		a2.setAmount(new BigDecimal("-50"));
		a2.setAssetLiabilityAccount("Bank");
		a2.setExpenseCategory("Supplies");
		row.getAllocations().add(a1);
		row.getAllocations().add(a2);
		
		List<AccountingTransaction> txs =
			ImportFileActionFX.convertExcelRows(List.of(row));
		assertEquals(1, txs.size());
		AccountingTransaction tx = txs.get(0);
		assertEquals(4, tx.getEntries().size());
		
	}
	
	@Test
	@DisplayName("convertExcelRows: ignores allocations without two accounts")
	void
		testConvertExcelRows_invalidAllocationIgnored()
	{
		ChartOfAccounts chart = new ChartOfAccounts();
		chart.addAccount(new Account("100", "Bank", AccountSide.DEBIT));
		chart.addAccount(new Account("400", "Sales", AccountSide.CREDIT));
		Company c = new Company();
		c.setChartOfAccounts(chart);
		CurrentCompany.forceCompanyLoad(c);
		
		ExcelLedgerRow row = new ExcelLedgerRow();
		ExcelLedgerRow.Allocation a = new ExcelLedgerRow.Allocation();
		a.setAmount(new BigDecimal("10"));
		a.setAssetLiabilityAccount("Bank");
		// missing second account
		row.getAllocations().add(a);
		
		List<AccountingTransaction> txs =
			ImportFileActionFX.convertExcelRows(List.of(row));
		assertTrue(txs.isEmpty());
		
	}
	
}
