package nonprofitbookkeeping.core;

import javafx.application.Platform;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;
import nonprofitbookkeeping.service.*; // Import all services

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
// It's okay if CurrentCompany.getCompany() returns null in tests if not set up,
// as getCurrentCompany() isn't the focus of these constructor tests.

@ExtendWith(MockitoExtension.class)
class ApplicationContextImplTest {

    @Mock
    private Stage mockPrimaryStage;
    private MenuBar menuBar;
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

    @BeforeAll
    static void initializeJavaFxToolkit() throws InterruptedException {
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("javafx.platform", "Monocle");

        CountDownLatch startupLatch = new CountDownLatch(1);
        try {
            Platform.startup(startupLatch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            startupLatch.countDown();
        }

        if (!startupLatch.await(5, TimeUnit.SECONDS)) {
            fail("JavaFX platform failed to start within 5 seconds.");
        }
    }

    @BeforeEach
    void setUpMenuBar() throws InterruptedException {
        CountDownLatch menuLatch = new CountDownLatch(1);
        AtomicReference<MenuBar> menuBarRef = new AtomicReference<>();

        Platform.runLater(() -> {
            menuBarRef.set(new MenuBar());
            menuLatch.countDown();
        });

        if (!menuLatch.await(5, TimeUnit.SECONDS)) {
            fail("MenuBar creation timed out.");
        }

        this.menuBar = menuBarRef.get();
    }

    @Test
    @DisplayName("Constructor: Valid inputs should initialize all fields correctly")
    void testConstructor_validInputs_gettersReturnCorrectInstances() {
        ApplicationContextImpl context = new ApplicationContextImpl(
                this.mockPrimaryStage,
                this.menuBar,
                this.mockReportService,
                this.mockBudgetService,
                this.mockReportConfigurationService,
                this.mockInventoryService,
                this.mockDocumentStorageService,
                this.mockFundAccountingService
        );

        assertNotNull(context, "ApplicationContextImpl instance should not be null.");
        assertSame(this.mockPrimaryStage, context.getPrimaryStage(), "getPrimaryStage() should return the mocked Stage.");
        assertSame(this.menuBar, context.getMenuBar(), "getMenuBar() should return the provided MenuBar instance.");
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
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new ApplicationContextImpl(
                        null,
                        this.menuBar,
                        this.mockReportService,
                        this.mockBudgetService,
                        this.mockReportConfigurationService,
                        this.mockInventoryService,
                        this.mockDocumentStorageService,
                        this.mockFundAccountingService
                )
        );
        assertEquals("PrimaryStage cannot be null.", exception.getMessage(),
                     "Exception message for null PrimaryStage is not as expected.");
    }

    @Test
    @DisplayName("Constructor: Null MenuBar should throw IllegalArgumentException")
    void testConstructor_nullMenuBar_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new ApplicationContextImpl(
                        this.mockPrimaryStage,
                        null,
                        this.mockReportService,
                        this.mockBudgetService,
                        this.mockReportConfigurationService,
                        this.mockInventoryService,
                        this.mockDocumentStorageService,
                        this.mockFundAccountingService
                )
        );
        assertEquals("MenuBar cannot be null.", exception.getMessage(),
                     "Exception message for null MenuBar is not as expected.");
    }

    // Note: Null checks for other service dependencies could be added here for completeness
    // but are not strictly required by the current subtask focusing on MenuBar.
}
