package nonprofitbookkeeping.ui.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.File;

/**
 * Handles the action of importing a file in a JavaFX application.
 * This class implements {@link EventHandler} for {@link ActionEvent} to trigger
 * the file import process. It opens a file dialog for the user to select a file
 * and currently acts as a placeholder, showing an alert with the selected file's details.
 * It requires an owner {@link Stage} to properly manage dialogs.
 */
public class ImportFileActionFX implements EventHandler<ActionEvent> {

    private final Stage ownerStage;

    /**
     * Constructs a new ImportFileActionFX.
     *
     * @param ownerStage The primary stage of the JavaFX application, which will own
     *                   any dialogs opened by this action. Must not be null.
     * @throws IllegalArgumentException if ownerStage is null.
     */
    public ImportFileActionFX(Stage ownerStage) {
        if (ownerStage == null) {
            throw new IllegalArgumentException("Owner stage cannot be null.");
        }
        this.ownerStage = ownerStage;
    }

    /**
     * Handles the action event to trigger the file import process.
     * This method opens a {@link FileChooser} to allow the user to select a file
     * for import, supporting OFX, QFX, and QIF formats. If a file is selected,
     * an informational alert is displayed acknowledging the selection and the
     * determined format. Actual file parsing and import logic is pending.
     *
     * @param event The {@link ActionEvent} that triggered this handler.
     */
    @Override
    public void handle(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import File");

        ExtensionFilter ofxFilter = new ExtensionFilter("OFX files (*.ofx)", "*.ofx");
        ExtensionFilter qfxFilter = new ExtensionFilter("QFX files (*.qfx)", "*.qfx");
        ExtensionFilter qifFilter = new ExtensionFilter("QIF files (*.qif)", "*.qif");
        ExtensionFilter allFilter = new ExtensionFilter("All files (*.*)", "*.*");

        fileChooser.getExtensionFilters().addAll(ofxFilter, qfxFilter, qifFilter, allFilter);
        fileChooser.setSelectedExtensionFilter(ofxFilter); // Default to OFX

        File selectedFile = fileChooser.showOpenDialog(this.ownerStage);

        if (selectedFile != null) {
            ExtensionFilter selectedFilter = fileChooser.getSelectedExtensionFilter();
            String chosenFormatString = "Unknown format";
            String fileName = selectedFile.getName().toLowerCase();

            if (selectedFilter == ofxFilter || fileName.endsWith(".ofx")) {
                chosenFormatString = "OFX";
            } else if (selectedFilter == qfxFilter || fileName.endsWith(".qfx")) {
                chosenFormatString = "QFX";
            } else if (selectedFilter == qifFilter || fileName.endsWith(".qif")) {
                chosenFormatString = "QIF";
            } else if (selectedFilter == allFilter) {
                 // Try to infer from extension if "All files" was chosen
                if (fileName.endsWith(".ofx")) chosenFormatString = "OFX";
                else if (fileName.endsWith(".qfx")) chosenFormatString = "QFX";
                else if (fileName.endsWith(".qif")) chosenFormatString = "QIF";
                else chosenFormatString = "selected file type"; // Generic if extension not matched
            }


            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Import File");
            alert.setHeaderText("File Selected for Import");
            alert.setContentText("File '" + selectedFile.getName() + "' selected for import as " +
                                 chosenFormatString + ". Actual import logic is pending implementation.");
            alert.initOwner(this.ownerStage);
            alert.showAndWait();
        } else {
            // User cancelled the dialog, do nothing.
        }
    }
}
