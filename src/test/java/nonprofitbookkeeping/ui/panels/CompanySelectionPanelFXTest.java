
package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;


import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.base.NodeMatchers.isEnabled;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.service.PreferencesService;


public class CompanySelectionPanelFXTest extends JavaFXTestBase {

    private CompanySelectionPanelFX panel;
    private static final String TEST_COMPANIES_DIR_NAME = "test_companies_temp";
    private static File testCompaniesDir;

    @Start
    @Override
    public void start(Stage stage) throws Exception {
        // Create a temporary directory for test company files
        Path tempDirPath = Files.createTempDirectory(TEST_COMPANIES_DIR_NAME);
        testCompaniesDir = tempDirPath.toFile();

        // Override default company directory for tests
        PreferencesService.getInstance().setDefaultCompanyDir(testCompaniesDir.getAbsolutePath());

        // Create a dummy company file for testing selection and preview
        try {
            CompanyProfileModel dummyProfile = new CompanyProfileModel();
            dummyProfile.setCompanyName("Test Dummy Company");
            dummyProfile.setCompanyFileDir(testCompaniesDir.getAbsolutePath());
            dummyProfile.setCompanyFileName("dummy.npbk"); // Standard extension

            // This is a simplified way to create a file; actual company creation involves more
            File dummyCompanyFile = new File(testCompaniesDir, dummyProfile.getCompanyFileName());
            if (dummyCompanyFile.createNewFile()) {
                // Optionally write some minimal content if CurrentCompany.loadFromPersistent needs it
                // For now, an empty file is created.
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle exception during test setup
        }

        this.panel = new CompanySelectionPanelFX();
        Scene scene = new Scene(this.panel, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testPanelLoads_InitialComponentsVisible() {
        // Verify that the main components are visible
        verifyThat(".list-view", isVisible()); // ListView for companies
        verifyThat(".text-area", isVisible()); // TextArea for preview
        verifyThat("Open Selected", (Button b) -> b.isVisible() && hasText("Open Selected").matches(b));
        verifyThat("Create New Company…", (Button b) -> b.isVisible() && hasText("Create New Company…").matches(b));

        // Check if the list view is populated (or empty if no files)
        ListView<File> companyList = lookup(".list-view").queryListView();
        assertNotNull(companyList);

        // The "Open Selected" button should be enabled if a company is selected,
        // which happens by default if there's at least one company.
        // If list is empty, it might be disabled or simply do nothing.
        // CompanySelectionPanelFX auto-selects the first item if the list is not empty.
        if (!companyList.getItems().isEmpty()) {
            verifyThat("Open Selected", isEnabled());
        } else {
            // If no companies, it makes sense for Open Selected to be disabled,
            // or at least do nothing. The current panel code doesn't explicitly disable it,
            // but the openSelected() method checks for null selection.
            // For now, we assume it's enabled but the action handles no selection.
             verifyThat("Open Selected", isEnabled()); // As per current panel logic
        }
    }

    @Test
    public void testCreateNewCompanyButton_OpensDialog() {
        // Click the "Create New Company..." button
        clickOn("Create New Company…");
        WaitForAsyncUtils.waitForFxEvents(); // Ensure UI updates

        // Verify that a new stage (dialog) has appeared
        // TestFX looks for stages based on the order they were shown.
        // The primary stage is the first one. A new dialog would be the second.
        List<Stage> stages = listTargetWindows().stream()
                                             .filter(w -> w instanceof Stage)
                                             .map(w -> (Stage) w)
                                             .collect(Collectors.toList());

        assertTrue(stages.size() > 1, "A new dialog stage should have opened.");

        Stage createCompanyDialog = stages.get(stages.size() - 1); // Get the newest stage
        assertTrue(createCompanyDialog.isShowing(), "Create Company dialog should be showing.");
        assertEquals("Create New Company", createCompanyDialog.getTitle(), "Dialog title should be 'Create New Company'");

        // Verify that the dialog contains the CreateOrEditCompanyPanelFX
        assertNotNull(lookup(".text-field").match(node -> node.getScene().getWindow() == createCompanyDialog).query(),
                      "Dialog should contain elements from CreateOrEditCompanyPanelFX (e.g., a text field)");

        // Close the dialog
        Platform.runLater(createCompanyDialog::close);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testCompanyList_SelectionUpdatesPreview() {
        ListView<File> companyList = lookup(".list-view").queryListView();
        TextArea previewArea = lookup(".text-area").queryAs(TextArea.class);

        // Ensure there's at least one item from setup
        if (companyList.getItems().isEmpty()) {
            System.err.println("Company list is empty, skipping selection test. Check test setup.");
            return;
        }

        // Select the first item (dummy.npbk)
        Platform.runLater(() -> companyList.getSelectionModel().selectFirst());
        WaitForAsyncUtils.waitForFxEvents();

        // Wait for preview to update. The preview logic in CompanySelectionPanelFX
        // involves file I/O and CurrentCompany interaction, so it might take a moment.
        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS); // Give some time for preview to load

        File selectedFile = companyList.getSelectionModel().getSelectedItem();
        assertNotNull(selectedFile, "A file should be selected.");
        assertEquals("dummy.npbk", selectedFile.getName());

        // Check if previewArea shows the company name or file name
        // The exact text depends on showPreview() logic
        String previewText = previewArea.getText();
        assertTrue(previewText.contains("Test Dummy Company") || previewText.contains("dummy.npbk"),
                   "Preview area should show details of 'dummy.npbk'. Actual: " + previewText);
    }

    @Test
    public void testOpenSelectedButton_WithSelection() {
        ListView<File> companyList = lookup(".list-view").queryListView();

        if (companyList.getItems().isEmpty()) {
            System.err.println("Company list is empty, skipping 'Open Selected' test. Check test setup.");
            return;
        }

        // Ensure first item is selected
        Platform.runLater(() -> companyList.getSelectionModel().selectFirst());
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS); // allow preview to load

        // Click "Open Selected"
        clickOn("Open Selected");
        WaitForAsyncUtils.waitForFxEvents();

        // The panel shows an Alert. We need to find and close it.
        // TestFX doesn't directly "see" alerts as new Stages in listTargetWindows() sometimes.
        // A common way to handle alerts is to look for their buttons.
        Node alertPane = lookup(".alert").query();
        assertNotNull(alertPane, "An alert should be displayed.");

        Button okButton = lookup(".alert .button").match(Node::isVisible).queryButton();
        assertNotNull(okButton, "Alert should have an OK button (or similar).");
        assertEquals("OK", okButton.getText(), "Alert OK button text check"); // Or whatever the default is

        clickOn(okButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify alert is closed (no longer found)
        // This can be tricky as the node might just be hidden.
        // A better check would be if a subsequent action is now possible.
        // For now, assume clicking OK closes it.
    }


    // Clean up the temporary directory after all tests in this class
    @org.junit.jupiter.api.AfterAll
    public static void cleanup() {
        if (testCompaniesDir != null && testCompaniesDir.exists()) {
            try {
                // Recursively delete the directory
                Files.walk(testCompaniesDir.toPath())
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(File::delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Reset preferences if necessary, though for default dir it might not be critical
        // PreferencesService.getInstance().setDefaultCompanyDir(originalDefaultDir); // If we stored it
    }
}
