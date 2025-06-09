
package nonprofitbookkeeping.reports.writer;

import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.service.TrialBalanceService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class) class LedgerReportWriterTest
{
	
	@Mock private TrialBalanceService mockTrialBalanceService;
	
	@Mock private Ledger mockLedger;
	
	@Test
	@DisplayName("Constructor: Valid inputs should create instance successfully")
		void testConstructor_validInputs_succeeds()
	{
		LedgerReportWriter writer =
			new LedgerReportWriter(this.mockTrialBalanceService, this.mockLedger);
		assertNotNull(writer, "Writer instance should be created successfully with valid inputs.");
		// In a more comprehensive test, one might check if these dependencies are used
		// by other methods.
		// For now, successful instantiation is the key check.
	}
	
	@Test
	@DisplayName("Constructor: Null TrialBalanceService should throw IllegalArgumentException")
		void testConstructor_nullTrialBalanceService_throwsIllegalArgumentException()
	{
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			new LedgerReportWriter(null, this.mockLedger);
		});
		assertEquals("TrialBalanceService cannot be null.", exception.getMessage(),
			"Exception message should indicate TrialBalanceService is null.");
	}
	
	@Test
	@DisplayName("Constructor: Null Ledger should throw IllegalArgumentException")
		void testConstructor_nullLedger_throwsIllegalArgumentException()
	{
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			new LedgerReportWriter(this.mockTrialBalanceService, null);
		});
		assertEquals("Ledger cannot be null.", exception.getMessage(),
			"Exception message should indicate Ledger is null.");
	}
	
	@Test
	@DisplayName("Constructor: Both inputs null should throw IllegalArgumentException")
		void testConstructor_bothInputsNull_throwsIllegalArgumentException()
	{
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			new LedgerReportWriter(null, null);
		});
		// The message will depend on the order of null checks in the constructor.
		// Assuming TrialBalanceService is checked first based on previous writer test.
		assertTrue(exception.getMessage().contains("TrialBalanceService cannot be null."),
			"Exception message should indicate a null parameter, likely TrialBalanceService first.");
	}
	
}
