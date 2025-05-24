
package nonprofitbookkeeping.ui.actions;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import nonprofitbookkeeping.exception.NoFileException;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.ui.helpers.NpbkFileChooserFX;

public class OpenCompanyFileActionFX implements EventHandler<ActionEvent>
{
	
	private final Stage owner; // main window
	
	public OpenCompanyFileActionFX(Stage owner)
	{
		this.owner = owner;
	}
	
	/**
	 * 
	 * Override @see javafx.event.EventHandler#handle(javafx.event.Event)
	 */
	@Override public void handle(ActionEvent e)
	{
		
		File file = Company.getCurrentFile();
		
		try
		{
			file = NpbkFileChooserFX.chooseExisting("Open File",
				"Company file",
				"*.npbk",
				this.owner);
		}
		catch (NoFileException e1)
		{
			e1.printStackTrace();
		}
		
		// Load file from the data store
		Company.load(file);
		showInfo("Loaded " + file.getAbsolutePath());
		
	}
	
	
	/**
	 * 
	 * @param msg
	 */
	private static void showInfo(String msg)
	{
		new Alert(AlertType.INFORMATION, msg).showAndWait();
	}
	

	
}
