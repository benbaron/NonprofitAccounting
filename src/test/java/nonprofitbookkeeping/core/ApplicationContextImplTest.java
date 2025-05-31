package nonprofitbookkeeping.core;

import javafx.stage.Stage;
import javafx.scene.control.MenuBar;
import nonprofitbookkeeping.model.Company; // For getCurrentCompany, though not directly tested via constructor
import nonprofitbookkeeping.service.*; // Import all services

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
// It's okay if CurrentCompany.getCompany() returns null in tests if not set up,
// as getCurrentCompany() isn't the focus of these constructor tests.

@ExtendWith(MockitoExtension.class)
class ApplicationContextImplTest {

    @Mock
    private Stage mockPrimaryStage;
    @Mock
    private MenuBar mockMenuBar;
    @Mock
    private ReportService mockReportService;
    @Mock
    private BudgetService mockBudgetService;
    @Mock
    private ReportConfigurationService mockReportConfigurationService;
    @Mock
    private InventoryService mockInventoryService;
    @Mock
    private DocumentStorageService mockDocumentStorageService;
    @Mock
    private FundAccountingService mockFundAccountingService;

    @Test
    @DisplayName("Constructor: Valid inputs should initialize all fields correctly")
    void testConstructor_validInputs_gettersReturnCorrectInstances() {
        ApplicationContextImpl context = new ApplicationContextImpl(
                this.mockPrimaryStage,
                this.mockMenuBar,
                this.mockReportService,
                this.mockBudgetService,
                this.mockReportConfigurationService,
                this.mockInventoryService,
                this.mockDocumentStorageService,
                this.mockFundAccountingService
        );

        assertNotNull(context, "ApplicationContextImpl instance should not be null.");
        assertSame(this.mockPrimaryStage, context.getPrimaryStage(), "getPrimaryStage() should return the mocked Stage.");
        assertSame(this.mockMenuBar, context.getMenuBar(), "getMenuBar() should return the mocked MenuBar.");
        assertSame(this.mockReportService, context.getReportService(), "getReportService() should return the mocked ReportService.");
        assertSame(this.mockBudgetService, context.getBudgetService(), "getBudgetService() should return the mocked BudgetService.");
        assertSame(this.mockReportConfigurationService, context.getReportConfigurationService(), "getReportConfigurationService() should return the mocked ReportConfigurationService.");
        assertSame(this.mockInventoryService, context.getInventoryService(), "getInventoryService() should return the mocked InventoryService.");
        assertSame(this.mockDocumentStorageService, context.getDocumentStorageService(), "getDocumentStorageService() should return the mocked DocumentStorageService.");
        assertSame(this.mockFundAccountingService, context.getFundAccountingService(), "getFundAccountingService() should return the mocked FundAccountingService.");
    }

    @Test
    @DisplayName("Constructor: Null PrimaryStage should throw IllegalArgumentException")
    void testConstructor_nullPrimaryStage_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new ApplicationContextImpl(
                    null, // Null PrimaryStage
                    this.mockMenuBar,
                    this.mockReportService,
                    this.mockBudgetService,
                    this.mockReportConfigurationService,
                    this.mockInventoryService,
                    this.mockDocumentStorageService,
                    this.mockFundAccountingService
            );
        });
        assertEquals("PrimaryStage cannot be null.", exception.getMessage(),
                     "Exception message for null PrimaryStage is not as expected.");
    }

    @Test
    @DisplayName("Constructor: Null MenuBar should throw IllegalArgumentException")
    void testConstructor_nullMenuBar_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new ApplicationContextImpl(
                    this.mockPrimaryStage,
                    null, // Null MenuBar
                    this.mockReportService,
                    this.mockBudgetService,
                    this.mockReportConfigurationService,
                    this.mockInventoryService,
                    this.mockDocumentStorageService,
                    this.mockFundAccountingService
            );
        });
        assertEquals("MenuBar cannot be null.", exception.getMessage(),
                     "Exception message for null MenuBar is not as expected.");
    }

    // Note: Null checks for other service dependencies could be added here for completeness
    // but are not strictly required by the current subtask focusing on MenuBar.
}
