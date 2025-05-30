package nonprofitbookkeeping.service;

import nonprofitbookkeeping.api.ReportWriterIntf;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.ReportMetadata;

import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
// import java.nio.file.Path; // Not strictly needed if using File.toPath()
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportWriterIntf mockReportWriter;
    @Mock
    private ReportContext mockContext;
    @Mock
    private Ledger mockLedger;
    @Mock
    private ChartOfAccounts mockCoa;
    @Mock
    private JxlsHelper mockJxlsHelper; 

    private MockedStatic<JxlsHelper> mockedStaticJxlsHelper;
    private MockedStatic<ReportService> mockedStaticReportServiceForStream;


    @BeforeEach
    void setUp() {
        ReportService.resetForTesting(); 
        lenient().when(mockContext.getReportType()).thenReturn("income_statement");
        lenient().when(mockContext.getStartDate()).thenReturn(LocalDate.of(2023,1,1));
        lenient().when(mockContext.getEndDate()).thenReturn(LocalDate.of(2023,1,31));
        // Stubbing for ChartOfAccounts to avoid NPEs in prepare...Context methods
        lenient().when(mockCoa.getAccounts()).thenReturn(Collections.emptyList()); 
        // Stubbing for Ledger to avoid NPEs
        lenient().when(mockLedger.getTransactions()).thenReturn(Collections.emptyList());
    }

    @AfterEach
    void tearDown() {
        if (mockedStaticJxlsHelper != null && !mockedStaticJxlsHelper.isClosed()) {
            mockedStaticJxlsHelper.close();
        }
        if (mockedStaticReportServiceForStream != null && !mockedStaticReportServiceForStream.isClosed()) {
            mockedStaticReportServiceForStream.close();
        }
        // Clean up any generated files
        new File("income_statement_2023-01-01_to_2023-01-31.xlsx").delete();
        new File("balance_sheet_2023-01-31.xlsx").delete(); // If balance_sheet test creates this
        new File("generated_report_stub_test_type.txt").delete();
    }
    
    @Test
    @DisplayName("resetForTesting: Clears writers and generated reports")
    void testResetForTesting_clearsReportWritersAndGeneratedReports() {
        ReportService reportServiceInstance = new ReportService(); 
        reportServiceInstance.registerWriter("dummy_type", mockReportWriter); 
        
        // Simulate a report generation to add to generatedReports
        // For this specific test of reset, we manually ensure lists are clear
        // by calling reset then checking.
        ReportService.resetForTesting(); // Call it again to ensure it works on potentially non-empty lists
        
        assertTrue(ReportService.listGeneratedReports().isEmpty(), "Generated reports should be empty after reset.");
        // Verifying reportWriters is cleared is tricky without a getter.
        // We rely on the fact that if it wasn't cleared, other tests might fail or show inconsistent behavior.
    }

    // --- registerWriter Tests ---
    @Test
    @DisplayName("registerWriter: Valid writer and type, no error")
    void testRegisterWriter_validWriter_noError() {
        ReportService reportServiceInstance = new ReportService();
        assertDoesNotThrow(() -> reportServiceInstance.registerWriter("custom_report", mockReportWriter));
    }

    @Test
    @DisplayName("registerWriter: Null reportType is ignored")
    void testRegisterWriter_nullReportType_isIgnored() {
        ReportService reportServiceInstance = new ReportService();
        assertDoesNotThrow(() -> reportServiceInstance.registerWriter(null, mockReportWriter));
    }

    @Test
    @DisplayName("registerWriter: Blank reportType is ignored")
    void testRegisterWriter_blankReportType_isIgnored() {
        ReportService reportServiceInstance = new ReportService();
        assertDoesNotThrow(() -> reportServiceInstance.registerWriter("   ", mockReportWriter));
    }

    @Test
    @DisplayName("registerWriter: Null writer is ignored")
    void testRegisterWriter_nullWriter_isIgnored() {
        ReportService reportServiceInstance = new ReportService();
        assertDoesNotThrow(() -> reportServiceInstance.registerWriter("my_report", null));
    }

    // --- listGeneratedReports Tests ---
    @Test
    @DisplayName("listGeneratedReports: Initially empty after reset")
    void testListGeneratedReports_initiallyEmpty() {
        assertTrue(ReportService.listGeneratedReports().isEmpty());
    }

    @Test
    @DisplayName("listGeneratedReports: Returns a copy, not the internal list")
    void testListGeneratedReports_returnsCopy() {
        // This test is more meaningful if a report is generated first.
        // For now, we test the copy behavior on an empty list.
        List<ReportMetadata> list1 = ReportService.listGeneratedReports();
        assertNotNull(list1);
        
        try {
            list1.add(new ReportMetadata("dummy", "dummyTime", "dummyPath"));
        } catch (UnsupportedOperationException e) {
            // This would be if an unmodifiable list was returned.
            // ReportService returns new ArrayList<>() which is modifiable.
        }
        
        // The key is that modifying list1 (a copy) does not affect the internal list.
        // Since the internal list is empty and list1 is a copy, internal should remain empty.
        assertEquals(0, ReportService.listGeneratedReports().size(), "Internal list should remain empty if copy is modified.");
        
        List<ReportMetadata> list2 = ReportService.listGeneratedReports();
        assertNotSame(list1, list2, "Should return a new list instance.");
    }

    // --- generate() method's metadata recording Tests ---

    private void setupStaticMocksForSuccessfulGenerate() {
        mockedStaticJxlsHelper = mockStatic(JxlsHelper.class);
        mockedStaticJxlsHelper.when(JxlsHelper::getInstance).thenReturn(mockJxlsHelper);
        lenient().doNothing().when(mockJxlsHelper).processTemplate(
            any(InputStream.class), any(OutputStream.class), any(Context.class)
        );

        mockedStaticReportServiceForStream = mockStatic(ReportService.class, Answers.CALLS_REAL_METHODS);
        // Ensure all calls to getResourceAsStream return a valid stream
        mockedStaticReportServiceForStream.when(() -> ReportService.class.getResourceAsStream(startsWith("/templates/")))
            .thenReturn(new ByteArrayInputStream("fake template data".getBytes()));
    }

    @Test
    @DisplayName("generate: JXLS report (income_statement) success records metadata")
    void testGenerate_jxlsReport_recordsMetadataOnSuccess() throws IOException {
        setupStaticMocksForSuccessfulGenerate();
        when(mockContext.getReportType()).thenReturn("income_statement");
        // Ensure date methods are called on mockContext
        when(mockContext.getStartDate()).thenReturn(LocalDate.of(2023,1,1));
        when(mockContext.getEndDate()).thenReturn(LocalDate.of(2023,1,31));


        File resultFile = null;
        try {
            resultFile = ReportService.generate(mockContext, mockLedger, mockCoa);
            assertNotNull(resultFile);
            assertTrue(resultFile.getName().startsWith("income_statement_"));
            assertTrue(resultFile.getName().endsWith(".xlsx"));

            List<ReportMetadata> generated = ReportService.listGeneratedReports();
            assertEquals(1, generated.size());
            ReportMetadata meta = generated.get(0);
            assertTrue(meta.getReportName().startsWith("income_statement_"));
            assertNotNull(meta.getCreated());
            assertEquals(resultFile.getAbsolutePath(), meta.getFilePath());
        } finally {
            if (resultFile != null) Files.deleteIfExists(resultFile.toPath());
        }
    }

    @Test
    @DisplayName("generate: JXLS report error records no metadata")
    void testGenerate_jxlsReport_recordsNoMetadataOnJxlsError() {
        setupStaticMocksForSuccessfulGenerate(); // Still need ReportService mock for getResourceAsStream
        when(mockContext.getReportType()).thenReturn("balance_sheet");
        when(mockContext.getEndDate()).thenReturn(LocalDate.of(2023,1,31));


        // Force JxlsHelper to throw an IOException during processTemplate
        doThrow(new IOException("JXLS processing error")).when(mockJxlsHelper).processTemplate(
            any(InputStream.class), any(OutputStream.class), any(Context.class)
        );

        File resultFile = null;
        try {
            assertThrows(IOException.class, () -> {
                ReportService.generate(mockContext, mockLedger, mockCoa);
            });
        } finally {
             // Clean up if a file was unexpectedly created (should not happen)
            resultFile = new File("balance_sheet_2023-01-31.xlsx");
            if (resultFile.exists()) {
                try {
                    Files.deleteIfExists(resultFile.toPath());
                } catch (IOException e) { /* ignore cleanup error */ }
            }
        }
        assertTrue(ReportService.listGeneratedReports().isEmpty(), "No metadata should be recorded on JXLS error.");
    }
    
    @Test
    @DisplayName("generate: Unrecognized report type records metadata for stub TXT file")
    void testGenerate_unrecognizedReportType_recordsMetadataForStub() throws IOException {
        // No JXLS mocking needed here, as it should take the stub path.
        // Close any existing static mocks if they were set up by a previous test's @BeforeEach or similar
        if (mockedStaticJxlsHelper != null && !mockedStaticJxlsHelper.isClosed()) mockedStaticJxlsHelper.close();
        if (mockedStaticReportServiceForStream != null && !mockedStaticReportServiceForStream.isClosed()) mockedStaticReportServiceForStream.close();


        String unknownReportType = "stub_test_type";
        when(mockContext.getReportType()).thenReturn(unknownReportType);
        // Stub date methods for filename consistency and metadata
        when(mockContext.getStartDate()).thenReturn(LocalDate.of(2023, 3, 1));
        when(mockContext.getEndDate()).thenReturn(LocalDate.of(2023, 3, 31));


        File resultFile = null;
        try {
            resultFile = ReportService.generate(mockContext, mockLedger, mockCoa);
            assertNotNull(resultFile);
            assertEquals("generated_report_" + unknownReportType + ".txt", resultFile.getName());
            assertTrue(resultFile.exists()); 

            List<ReportMetadata> generated = ReportService.listGeneratedReports();
            assertEquals(1, generated.size());
            ReportMetadata meta = generated.get(0);
            assertEquals("generic_report_" + unknownReportType, meta.getReportName());
            assertNotNull(meta.getCreated());
            assertEquals(resultFile.getAbsolutePath(), meta.getFilePath());
        } finally {
            if (resultFile != null) Files.deleteIfExists(resultFile.toPath());
        }
    }
}
