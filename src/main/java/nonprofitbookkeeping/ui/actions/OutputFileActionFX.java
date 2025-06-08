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
 * chooser, remembers the last directory used for writing via {@link PreferencesManager},
 * and then displays an informational alert with the path of the file chosen by the user.
 * This action does not perform any actual file writing; it only handles the file selection part.
 */
public class OutputFileActionFX implements EventHandler<ActionEvent>
{
	
	/** The owner Stage for the FileChooser dialog. Can be null, but dialogs are better with an owner. */
	private final Stage owner;
	
	/**
     * Constructs a new {@code OutputFileActionFX}.
     *
     * @param owner The owner {@link Stage} for the {@link FileChooser} dialog that will be displayed.
     *              This is used for proper dialog modality. It can be null, in which case the
     *              dialog might not behave as a modal window tied to a specific parent.
     */
	public OutputFileActionFX(Stage owner)
	{
		this.owner = owner;
	}
	
	/**
     * {@inheritDoc}
     * <p>
     * Handles the action event, typically from a menu item or button, by prompting the
     * user to select a destination file for an output/save operation.
     * The process involves:
     * <ol>
     *   <li>Creating and configuring a {@link FileChooser} with the title "Select Output File".</li>
     *   <li>Setting the initial directory of the chooser based on the last write directory
     *       preference (from {@link PreferencesManager#getLastWriteDirectory()}), defaulting to the user's home directory.</li>
     *   <li>Displaying a "save" dialog.</li>
     *   <li>If a file is selected by the user:
     *     <ul>
     *       <li>The parent directory of the selected file is saved as the new "last directory" preference
     *           (using {@link PreferencesManager#setLastDirectory(String)} - note: this might intend to use a "last write directory" specific setter if available).</li>
     *       <li>An informational {@link Alert} is shown, displaying the absolute path of the selected file.</li>
     *     </ul>
     *   </li>
     *   <li>If the user cancels the dialog, no further action is taken.</li>
     * </ol>
     * </p>
     * @param e The {@link ActionEvent} that triggered this handler.
     */
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
