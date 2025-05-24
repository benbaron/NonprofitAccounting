
package nonprofitbookkeeping.ui.actions;

import java.io.File;
import java.io.IOException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.exception.NoFileException;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.helpers.NpbkFileChooserFX;

/**
 * 
 */
public class OpenCompanyFileActionFX
{
	
	/**
	 * 
	 * Constructor OpenCompanyFileActionFX
	 * @param owner
	 */
	public OpenCompanyFileActionFX(Stage owner)
	{
	
		try
		{
			File file = NpbkFileChooserFX.chooseExisting("Open File",
				"Company file",
				"*.npbk",
				owner);
			
			// Load file from the file system
			Company.getCompany().loadFromPersistent(file);
			Company.getCompany().open();
			
			showInfo("Loaded " + file.getAbsolutePath());
		}
		catch (NoFileException | IOException | ActionCancelledException | NoFileCreatedException e1)
		{
			AlertBox.showError(owner, "File Load Failed");
		}

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
