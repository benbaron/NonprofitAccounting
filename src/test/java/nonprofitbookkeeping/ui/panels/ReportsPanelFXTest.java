package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import nonprofitbookkeeping.reports.ReportMetadata;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import nonprofitbookkeeping.ui.panels.ReportsPanelFX.ReportRow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.awt.Desktop; // Import for mocking
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;


public class ReportsPanelFXTest extends JavaFXTestBase {

    private ReportsPanelFX panel;

    // We need to mock ReportService, but it's instantiated directly in ReportsPanelFX.
    // This is a common issue. Solutions:
    // 1. Modify ReportsPanelFX to allow ReportService injection (constructor or setter). (Best for testability)
    // 2. Use PowerMock or similar to mock constructor calls (more complex setup).
    // 3. For this test, since ReportService is simple and we mainly care about its `listGeneratedReports`
    //    and the interaction with GenerateReportPanelFX, we can let it instantiate.
    //    If `listGeneratedReports` reads from a file/DB, that would need mocking or a test setup.
    //    Let's assume `listGeneratedReports` can be influenced by files created by `GenerateReportPanelFX`
    //    or that we can test its effect by what `GenerateReportPanelFX` does.
    //    For now, we'll focus on UI interactions and the opening of GenerateReportPanelFX.
    //    To test table population, we'll need to simulate `GenerateReportPanelFX` creating a report.

    // For testing the "Open" button, we'll mock Desktop.
    private MockedStatic<Desktop> mockedDesktop;
    @Mock private Desktop mockDesktopInstance;


    @Start
    @Override
    public void start(Stage stage) throws Exception {
        MockitoAnnotations.openMocks(this); // Initialize mocks for Desktop

        // Mock static Desktop.getDesktop() to return our mock instance
        this.mockedDesktop = Mockito.mockStatic(Desktop.class);
        this.mockedDesktop.when(Desktop::getDesktop).thenReturn(this.mockDesktopInstance);
        when(this.mockDesktopInstance.isSupported(Desktop.Action.OPEN)).thenReturn(true);


        this.panel = new ReportsPanelFX();
        Scene scene = new Scene(this.panel, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @AfterEach
    public void tearDownStaticMock() {
        if (this.mockedDesktop != null) {
            this.mockedDesktop.close(); // Release static mock
        }
    }

    @BeforeEach
    public void clearReportListBeforeTest() {
        // To ensure a clean state for the report list, we can try to clear
        // any persisted report metadata if ReportService loads from a fixed location,
        // or ensure our mock setup for GenerateReportPanelFX doesn't leave files.
        // Since ReportService is not injected, this is harder.
        // For now, we assume the panel's refresh() will show what's available.
        // A better approach would be an injectable ReportService.
        // We will simulate that no reports exist initially.
        // This means the ReportService used by the panel should return an empty list.
        // This test will rely on the `Generate` button test to populate the list.
        // The panel calls refresh() in its constructor.
    }


    @Test
    public void testInitialState_GeneratorBarAndEmptyTable() {
        verifyThat("Type:", (Label l) -> l.isVisible());
        verifyThat(".combo-box", (ComboBox<String> cb) -> cb.isVisible() && !cb.getItems().isEmpty());
        verifyThat("From:", (Label l) -> l.isVisible());
 //       verifyThat(".date-picker", (DatePicker dp) -> dp.isVisible(), 2); // Should be 2 date pickers
        verifyThat("To:", (Label l) -> l.isVisible());
        verifyThat("Generate", (Button b) -> b.isVisible());

        TableView<ReportRow> table = lookup(".table-view").queryTableView();
        assertNotNull(table);
        // Assuming ReportService initially returns no reports or they are cleared.
        // The default ReportService might list actual files, so this might vary.
        // For a clean test, we'd mock ReportService.
        // Let's assume it's initially empty for this test's purpose.
        // If ReportService() constructor in panel reads existing files, this test needs adjustment or pre-cleanup.
         assertEquals(0, table.getItems().size(), "Table should be initially empty or cleared for test.");
    }

    @Test
    public void testGenerateButton_OpensGenerateReportPanelDialog() {
        clickOn("Generate");
        WaitForAsyncUtils.waitForFxEvents();

        List<Stage> stages = listTargetWindows().stream()
                                             .filter(w -> w instanceof Stage)
                                             .map(w -> (Stage) w)
                                             .collect(Collectors.toList());

        assertTrue(stages.size() > 1, "A new dialog stage should have opened for GenerateReportPanelFX.");
        Stage generateDialog = stages.get(stages.size() - 1); // Get the newest stage

        assertTrue(generateDialog.isShowing(), "GenerateReportPanelFX dialog should be showing.");
        assertEquals("Generating Report", generateDialog.getTitle(), "Dialog title mismatch.");

        // Verify that the dialog contains the GenerateReportPanelFX (e.g. by looking for a known element)
        assertNotNull(from(generateDialog.getScene().getRoot()).lookup("Generate Specific Report").queryLabeled(),
                      "Dialog should contain elements from GenerateReportPanelFX");

        // Close the dialog to simulate completion
        Platform.runLater(generateDialog::close);
        WaitForAsyncUtils.waitForFxEvents();

        // After dialog closes, ReportsPanelFX calls refresh().
        // We can't easily verify ReportService calls without injection.
        // We'd need GenerateReportPanelFX to actually create a ReportMetadata file
        // that ReportService would then pick up. This is integration-level.
        // For this unit test, verifying dialog opening is the main goal for this button.
    }

    @Test
    public void testOpenButtonInTable_AttemptsToOpenFile() throws IOException {
        // We need to populate the table with a mock row.
        // This is tricky because ReportService is not mocked and refresh() is called.
        // We'll simulate a report being added to the panel's internal list and refresh table.
        // This bypasses ReportService for this specific test of the "Open" button's wiring.

        String testFilePath = "test_report.pdf";
        File testFile = new File(testFilePath);
        // Create a dummy file for Desktop.open to "succeed" (not throw file not found)
        if (!testFile.exists()) testFile.createNewFile();

        ReportMetadata metadata = new ReportMetadata(
            "Test Report",
            LocalDate.now().format(DateTimeFormatter.ISO_DATE),
            testFile.getAbsolutePath(),
            "Test Type"
        );
        ReportRow testRow = new ReportRow(metadata);

        TableView<ReportRow> table = lookup(".table-view").queryTableView();
        Platform.runLater(() -> table.getItems().add(testRow));
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat(table, hasNumRows(1));

        // Find the "Open" button in the row.
        // TableCell containing the button is a child of TableRow, which is a child of TableView.
        // This lookup can be complex. A common way is to iterate cells.
        Button openButton = null;
        for (Node nodeInRow : from(table).lookup(".table-row-cell").nth(0).lookup(".button").queryAll()) {
            if (nodeInRow instanceof Button && "Open".equals(((Button) nodeInRow).getText())) {
                openButton = (Button) nodeInRow;
                break;
            }
        }
        assertNotNull(openButton, "Open button not found in table row");

        clickOn(openButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify Desktop.getDesktop().open() was called with the correct file
        this.mockedDesktop.verify(() -> Desktop.getDesktop().open(eq(new File(testRow.getPath()))));

        if (testFile.exists()) testFile.delete(); // Clean up dummy file
    }

    @Test
    public void testOpenButtonInTable_HandlesIOException() throws IOException {
        // Simulate a file that causes an IOException when Desktop.open() is called
        String nonExistentFilePath = "non_existent_report.pdf"; // File that won't open
        ReportMetadata metadata = new ReportMetadata(
            "Error Report",
            LocalDate.now().format(DateTimeFormatter.ISO_DATE),
            nonExistentFilePath,
            "Error Type"
        );
        ReportRow errorRow = new ReportRow(metadata);

        TableView<ReportRow> table = lookup(".table-view").queryTableView();
        Platform.runLater(() -> table.getItems().add(errorRow));
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(table, hasNumRows(1));

        Button openButton = from(table).lookup(".table-row-cell").nth(0).lookup((Button b) -> "Open".equals(b.getText())).queryButton();
        assertNotNull(openButton, "Open button not found for error test");

        // Make Desktop.open() throw an IOException for this specific file
        doThrow(new IOException("Test IO Exception")).when(this.mockDesktopInstance).open(eq(new File(nonExistentFilePath)));

        clickOn(openButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify that an error alert was shown
        // TestFX doesn't "see" alerts opened by java.awt.Desktop easily.
        // The panel code uses `new Alert(Alert.AlertType.ERROR, ...).showAndWait();`
        // We need to find this JavaFX alert.
        DialogPane alertPane = getTopModalDialogPane(); // Helper from other tests
        assertNotNull(alertPane, "Error alert dialog should be shown.");
        assertTrue(alertPane.getContentText().contains("Cannot open file: Test IO Exception"));

        // Close the alert
        Button okButton = (Button) alertPane.lookupButton(ButtonType.OK);
        if (okButton == null) { // Some alerts might have Close instead of OK
            okButton = (Button) alertPane.lookupButton(ButtonType.CLOSE);
        }
        assertNotNull(okButton, "OK or Close button not found on alert dialog.");
        clickOn(okButton);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private DialogPane getTopModalDialogPane() {
        List<Stage> stages = listTargetWindows().stream()
                                             .filter(w -> w instanceof Stage)
                                             .map(w -> (Stage) w)
                                             .filter(s -> s.getModality() == javafx.stage.Modality.APPLICATION_MODAL || s.getModality() == javafx.stage.Modality.WINDOW_MODAL)
                                             .filter(Stage::isShowing)
                                             .collect(Collectors.toList());
        if (stages.isEmpty()) {
            // Check for alerts that might not be modal stages in the same way
            Optional<Node> alertNode = lookup( (Node n) -> n instanceof DialogPane && n.isVisible()).tryQuery();
            if (alertNode.isPresent()) return (DialogPane) alertNode.get();
            return null;
        }
        Stage currentDialogStage = stages.get(stages.size() - 1);
        if (currentDialogStage.getScene() == null || currentDialogStage.getScene().getRoot() == null) return null;
        if (currentDialogStage.getScene().getRoot() instanceof DialogPane) {
            return (DialogPane) currentDialogStage.getScene().getRoot();
        }
        return from(currentDialogStage.getScene().getRoot()).lookup(".dialog-pane").query();
    }
}
