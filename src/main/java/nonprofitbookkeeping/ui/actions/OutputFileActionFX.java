/**
 * nonprofit-scaledger-ribbon.zip_expanded OutputFileActionFX.java
 * OutputFileActionFX
 */

package nonprofitbookkeeping.ui.actions;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.preferences.PreferencesManager;

/**
 * JavaFX counterpart to the Swing {@code OutputFileAction}. Opens a native
 * save‑dialog, remembers the last directory via {@link PreferencesManager}, and
 * shows the chosen path in an information alert.
 */
public class OutputFileActionFX implements EventHandler<ActionEvent>
{
	
	private final Stage owner; // may be null
	
	public OutputFileActionFX(Stage owner)
	{
		this.owner = owner;
	}
	
	@Override public void handle(ActionEvent e)
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Output File");
		
		// default directory
		String lastDir = PreferencesManager.getLastWriteDirectory();
		if (lastDir == null)
			lastDir = System.getProperty("user.home");
		File dir = new File(lastDir);
		if (dir.exists())
			chooser.setInitialDirectory(dir);
		
		File selected = chooser.showSaveDialog(this.owner);
		if (selected == null)
			return; // user cancelled
			
		// remember for next time
		PreferencesManager.setLastDirectory(selected.getParent());
		
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initOwner(this.owner);
		alert.setHeaderText(null);
		alert.setContentText("Selected Output File:\n" + selected.getAbsolutePath());
		alert.showAndWait();
	}
	
}
