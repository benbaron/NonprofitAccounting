package nonprofitbookkeeping.reports.writer;

import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.service.TrialBalanceService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TrialBalanceReportWriterTest {

    @Mock
    private TrialBalanceService mockTrialBalanceService;

    @Mock
    private Ledger mockLedger;

    @Test
    @DisplayName("Constructor: Valid inputs should create instance successfully")
    void testConstructor_validInputs_succeeds() {
        TrialBalanceReportWriter writer = new TrialBalanceReportWriter(mockTrialBalanceService, mockLedger);
        assertNotNull(writer, "Writer instance should be created successfully with valid inputs.");
        // Further tests would involve calling methods on 'writer' to verify field assignment,
        // but for a constructor-only test, notNull is the primary check.
    }

    @Test
    @DisplayName("Constructor: Null TrialBalanceService should throw IllegalArgumentException")
    void testConstructor_nullTrialBalanceService_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new TrialBalanceReportWriter(null, mockLedger);
        });
        assertEquals("TrialBalanceService cannot be null.", exception.getMessage(),
                     "Exception message should indicate TrialBalanceService is null.");
    }

    @Test
    @DisplayName("Constructor: Null Ledger should throw IllegalArgumentException")
    void testConstructor_nullLedger_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new TrialBalanceReportWriter(mockTrialBalanceService, null);
        });
        assertEquals("Ledger cannot be null.", exception.getMessage(),
                     "Exception message should indicate Ledger is null.");
    }

    @Test
    @DisplayName("Constructor: Both inputs null should throw IllegalArgumentException")
    void testConstructor_bothInputsNull_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new TrialBalanceReportWriter(null, null);
        });
        // The message will depend on which check comes first in the constructor
        assertTrue(exception.getMessage().contains("cannot be null"),
                   "Exception message should indicate a null parameter.");
    }
}
