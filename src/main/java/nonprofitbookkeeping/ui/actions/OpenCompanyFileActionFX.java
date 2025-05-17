
package nonprofitbookkeeping.ui.actions;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.core.JacksonDataStore;
import nonprofitbookkeeping.model.CompanyDataFile;
import nonprofitbookkeeping.model.CurrentInputFile;

public class OpenCompanyFileActionFX implements EventHandler<ActionEvent>
{
	
	private final Stage owner; // main window
	public OpenCompanyFileActionFX(Stage owner, JacksonDataStore dataStore)
	{
		this.owner = owner;
	}
	
	/**
	 * 
	 * Override @see javafx.event.EventHandler#handle(javafx.event.Event)
	 */
	@Override public void handle(ActionEvent e)
	{
		
		File file = CurrentInputFile.getCurrentInputFile();
		
		if (file == null || !file.exists() || !file.canRead())
		{
			file = chooseCompanyFile(); // JavaFX FileChooser
			
			if (file == null)
			{ // cancelled
				showError("Error loading file.");
				return;
			}
			
			CurrentInputFile.setCurrentInputFile(file);
		}
		
		// Load file from the data store
		CompanyDataFile.getCompanyDataFile().load(file);
		showInfo("Loaded " + file.getAbsolutePath());
		
	}
	
	/* ───────────────── helpers ───────────────── */
	private File chooseCompanyFile()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Open Company File");
		fc.getExtensionFilters().add(
			new FileChooser.ExtensionFilter("Nonprofit Bookkeeping (*.npbk)", "*.npbk"));
		return fc.showOpenDialog(this.owner);
	}
	
	/**
	 * 
	 * @param msg
	 */
	private static void showInfo(String msg)
	{
		new Alert(AlertType.INFORMATION, msg).showAndWait();
	}
	
	/**
	 * 
	 * @param msg
	 */
	private static void showError(String msg)
	{
		new Alert(AlertType.ERROR, msg).showAndWait();
	}
	
}
