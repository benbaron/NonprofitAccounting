package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.JavaFXTestBase;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.ComboBoxMatchers.hasItems;


public class GenerateReportPanelFXTest extends JavaFXTestBase {

    private GenerateReportPanelFX panel;

    @Mock
    private ReportService mockReportService;

    private ComboBox<String> reportSelector;
    private Button generateButton;
    private TextArea outputArea;

    @Start
    @Override
    public void start(Stage stage) throws Exception {
        MockitoAnnotations.openMocks(this);

        this.panel = new GenerateReportPanelFX(this.mockReportService);
        Scene scene = new Scene(this.panel, 700, 500); // Increased size slightly
        stage.setScene(scene);
        stage.show();

        // Assign nodes for easier access in tests
        this.reportSelector = lookup(".combo-box").queryComboBox();
        this.generateButton = lookup("Generate Report").queryButton(); // Lookup by text
        this.outputArea = lookup(".text-area").queryAs(TextArea.class);
    }

    @Test
    public void testInitialState_ComponentsArePresent_SelectorHasItems() {
        assertNotNull(this.reportSelector);
        assertNotNull(this.generateButton);
        assertNotNull(this.outputArea);

        // Check number of items. If this changes often, could make test brittle.
        // Or check for specific key items.
        verifyThat(this.reportSelector, hasItems(6));
        assertTrue(this.reportSelector.getItems().contains("Income Statement"));
        assertTrue(this.reportSelector.getItems().contains("Trial Balance (Jasper)"));

        assertEquals("Income Statement", this.reportSelector.getSelectionModel().getSelectedItem());
        assertEquals("", this.outputArea.getText()); // Should be initially empty
    }

    @Test
    public void testGenerateJasperReport_TrialBalance_Success() throws Exception {
        File mockFile = new File("mock_trial_balance.pdf");
        when(this.mockReportService.generateJasperReport(any(ReportContext.class), eq("pdf")))
            .thenReturn(mockFile);

        Platform.runLater(() -> this.reportSelector.setValue("Trial Balance (Jasper)"));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn(this.generateButton);
        WaitForAsyncUtils.waitForFxEvents();

        ArgumentCaptor<ReportContext> contextCaptor = ArgumentCaptor.forClass(ReportContext.class);
        verify(this.mockReportService).generateJasperReport(contextCaptor.capture(), eq("pdf"));
        assertEquals("trial_balance_jasper", contextCaptor.getValue().getReportType());
        // Dates are LocalDate.now() based, so exact match is hard. Check they are not null.
        assertNotNull(contextCaptor.getValue().getStartDate());
        assertNotNull(contextCaptor.getValue().getEndDate());

        String outputText = this.outputArea.getText();
        assertTrue(outputText.contains("Generating Trial Balance (Jasper) report..."));
        assertTrue(outputText.contains("Report generated successfully: " + mockFile.getAbsolutePath()));
    }

    @Test
    public void testGenerateJasperReport_TrialBalance_ServiceReturnsNull() throws Exception {
        when(this.mockReportService.generateJasperReport(any(ReportContext.class), eq("pdf")))
            .thenReturn(null);

        Platform.runLater(() -> this.reportSelector.setValue("Trial Balance (Jasper)"));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn(this.generateButton);
        WaitForAsyncUtils.waitForFxEvents();

        verify(this.mockReportService).generateJasperReport(any(ReportContext.class), eq("pdf"));
        assertTrue(this.outputArea.getText().contains("Report generation failed to produce a file."));
    }

    @Test
    public void testGenerateJasperReport_TrialBalance_ServiceThrowsException() throws Exception {
        when(this.mockReportService.generateJasperReport(any(ReportContext.class), eq("pdf")))
            .thenThrow(new IOException("Test Jasper Exception"));

        Platform.runLater(() -> this.reportSelector.setValue("Trial Balance (Jasper)"));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn(this.generateButton);
        WaitForAsyncUtils.waitForFxEvents();

        verify(this.mockReportService).generateJasperReport(any(ReportContext.class), eq("pdf"));
        assertTrue(this.outputArea.getText().contains("Error generating report: Test Jasper Exception"));
    }

    @Test
    public void testGenerateOtherReportType_IncomeStatement() {
        // For non-Jasper reports, it uses GenerateReportsAction.
        // We can't easily mock `new GenerateReportsAction(...)` without PowerMock or refactoring panel.
        // So, we'll verify the UI output which is currently placeholder text.

        Platform.runLater(() -> this.reportSelector.setValue("Income Statement"));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn(this.generateButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify no call to generateJasperReport for this type
        try
		{
			verify(this.mockReportService, never()).generateJasperReport(any(ReportContext.class), anyString());
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        String outputText = this.outputArea.getText();
        assertTrue(outputText.contains("Generating report: Income Statement..."));
        assertTrue(outputText.contains("Done. (This is a placeholder for the actual report)"));
    }
}
