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
 * It requires an owner {@link Stage} to properly manage dialogs such as the {@link FileChooser}.
 * The current implementation identifies the selected file and its likely format but
 * does not perform the actual data import; that logic is noted as pending.
 */
public class ImportFileActionFX implements EventHandler<ActionEvent> {

    /** The owner Stage for any dialogs created by this action. */
    private final Stage ownerStage;

    /**
     * Constructs a new {@code ImportFileActionFX}.
     *
     * @param ownerStage The primary {@link Stage} of the JavaFX application. This stage will act
     *                   as the owner for any dialogs (e.g., FileChooser, Alert) displayed by this action.
     *                   Must not be null.
     * @throws IllegalArgumentException if {@code ownerStage} is null.
     */
    public ImportFileActionFX(Stage ownerStage) {
        if (ownerStage == null) {
            throw new IllegalArgumentException("Owner stage cannot be null.");
        }
        this.ownerStage = ownerStage;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Handles the action event, typically triggered by a menu item or button click,
     * to initiate the file import process. This method performs the following steps:
     * <ol>
     *   <li>Creates and configures a {@link FileChooser} with the title "Import File" and
     *       relevant file extension filters (OFX, QFX, QIF, All Files).</li>
     *   <li>Displays an "open" dialog to the user, owned by the {@code ownerStage}.</li>
     *   <li>If the user selects a file:
     *     <ul>
     *       <li>Determines the likely file format based on the selected extension filter or filename extension.</li>
     *       <li>Displays an {@link Alert} dialog confirming the selected file and its inferred format,
     *           noting that the actual import logic is pending implementation.</li>
     *     </ul>
     *   </li>
     *   <li>If the user cancels the dialog, no further action is taken.</li>
     * </ol>
     * </p>
     * @param event The {@link ActionEvent} that triggered this handler (e.g., a menu item click).
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
