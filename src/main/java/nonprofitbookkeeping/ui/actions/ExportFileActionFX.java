/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * ExportFileActionFX.java
 * ExportFileActionFX
 */
package nonprofitbookkeeping.ui.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.FileChooser; // Moved import
import javafx.scene.control.Alert; // Moved import
import javafx.scene.control.Alert.AlertType; // Moved import
import java.io.File; // Moved import
import java.io.FileWriter; // Moved import
import java.io.IOException; // Moved import

/**
 * Handles the action of exporting a file in a JavaFX application.
 * This class implements {@link EventHandler} for {@link ActionEvent} to trigger
 * the file export process, typically by opening a file dialog.
 * It requires an owner {@link Stage} to properly manage dialogs.
 */
public class ExportFileActionFX  implements EventHandler<ActionEvent>
{
	private final Stage ownerStage;

	/**  
	 * Constructs a new ExportFileActionFX.
	 *
	 * @param primaryStage The primary stage of the JavaFX application, which will own
	 *                     any dialogs opened by this action. Must not be null.
	 * @throws IllegalArgumentException if primaryStage is null.
	 */
	public ExportFileActionFX(Stage primaryStage)
	{
		if (primaryStage == null) {
            throw new IllegalArgumentException("Primary stage (owner stage) cannot be null.");
        }
		this.ownerStage = primaryStage;
	}

	// Imports moved to the top

	/**
	 * Handles the action event to trigger the file export process.
	 * This method opens a {@link FileChooser} to allow the user to select a destination
	 * file. If a file is selected, placeholder content is written to it.
	 * Success or error messages are displayed to the user via {@link Alert} dialogs.
	 *
	 * @param event The {@link ActionEvent} that triggered this handler.
	 */
	@Override
	public void handle(ActionEvent event)
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export File As");

        // Define extension filters
        FileChooser.ExtensionFilter ofxFilter = new FileChooser.ExtensionFilter("OFX files (*.ofx)", "*.ofx");
        FileChooser.ExtensionFilter qfxFilter = new FileChooser.ExtensionFilter("QFX files (*.qfx)", "*.qfx");
        FileChooser.ExtensionFilter qifFilter = new FileChooser.ExtensionFilter("QIF files (*.qif)", "*.qif");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");

        fileChooser.getExtensionFilters().addAll(ofxFilter, qfxFilter, qifFilter, allFilter);
        fileChooser.setSelectedExtensionFilter(ofxFilter); // Default to OFX

        File selectedFile = fileChooser.showSaveDialog(this.ownerStage);

        if (selectedFile != null) {
            FileChooser.ExtensionFilter selectedFilter = fileChooser.getSelectedExtensionFilter();
            String chosenFormatString = "selected format"; // Default
            String fileExtension = "";

            if (selectedFilter == ofxFilter) {
                chosenFormatString = "OFX";
                fileExtension = ".ofx";
            } else if (selectedFilter == qfxFilter) {
                chosenFormatString = "QFX";
                fileExtension = ".qfx";
            } else if (selectedFilter == qifFilter) {
                chosenFormatString = "QIF";
                fileExtension = ".qif";
            }
            // If "All files" was selected, we might try to infer from filename or use a default.
            // For this placeholder, we'll rely on the filter selection primarily.

            String finalFileName = selectedFile.getAbsolutePath();
            // Ensure the file has the correct extension based on the filter, if not "All Files"
            if (!fileExtension.isEmpty() && !finalFileName.toLowerCase().endsWith(fileExtension)) {
                finalFileName += fileExtension;
            }
            File finalFileToWrite = new File(finalFileName);


            try (FileWriter writer = new FileWriter(finalFileToWrite)) {
                String content = "Placeholder for " + chosenFormatString + " data export from Nonprofit Bookkeeping application.";
                writer.write(content);

                Alert successAlert = new Alert(AlertType.INFORMATION);
                successAlert.setTitle("Export Successful");
                successAlert.setHeaderText(null);
                successAlert.setContentText("File exported successfully as " + chosenFormatString + " to " + finalFileToWrite.getAbsolutePath());
                successAlert.initOwner(this.ownerStage);
                successAlert.showAndWait();
            } catch (IOException ex) {
                Alert errorAlert = new Alert(AlertType.ERROR);
                errorAlert.setTitle("Export Error");
                errorAlert.setHeaderText("Could not save file.");
                errorAlert.setContentText("An error occurred while exporting the file: " + ex.getMessage());
                errorAlert.initOwner(this.ownerStage);
                errorAlert.showAndWait();
            }
        } else {
            // User cancelled the dialog, do nothing as per plan.
            // Optionally, show an info alert:
            // Alert cancelledAlert = new Alert(AlertType.INFORMATION);
            // cancelledAlert.setTitle("Export Cancelled");
            // cancelledAlert.setHeaderText(null);
            // cancelledAlert.setContentText("File export was cancelled by the user.");
            // cancelledAlert.initOwner(this.ownerStage);
            // cancelledAlert.showAndWait();
        }
	}
	
}
