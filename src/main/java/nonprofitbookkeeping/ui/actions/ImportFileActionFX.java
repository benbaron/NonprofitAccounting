/**
 * nonprofit-scaledger-ribbon.zip_expanded ImportFileActionFX.java
 * ImportFileActionFX
 */

package nonprofitbookkeeping.ui.actions;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.service.FileImportService;

/**
 * JavaFX replacement for Swing {@code ImportFileAction}. Opens a native file
 * chooser, sends the selection to {@link FileImportService#importFile(File)},
 * and shows a success/failure alert.
 */
public class ImportFileActionFX implements EventHandler<ActionEvent>
{
	
	private final Stage owner; // may be null
	
	public ImportFileActionFX(Stage owner)
	{
		this.owner = owner;
	}
	
	@Override public void handle(ActionEvent event)
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Import File");
		File file = chooser.showOpenDialog(owner);
		if (file == null)
			return; // user cancelled
			
		boolean success = FileImportService.importFile(file);
		Alert alert = new Alert(success ? AlertType.INFORMATION : AlertType.ERROR);
		alert.initOwner(owner);
		alert.setHeaderText(null);
		alert
			.setContentText(success ? "File imported successfully." : "Failed to import the file.");
		alert.showAndWait();
	}
	
}
