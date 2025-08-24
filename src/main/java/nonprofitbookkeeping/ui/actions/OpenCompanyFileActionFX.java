
package nonprofitbookkeeping.ui.actions;

import java.io.File;
import java.io.IOException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.exception.NoFileException;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.ui.helpers.NpbkFileChooserFX;

/**
 * Action to open a company file (.npbk or .sql).
 */
public class OpenCompanyFileActionFX
{
	
	private final Stage ownerStage;
	
	public OpenCompanyFileActionFX(Stage ownerStage)
	{
		this.ownerStage = ownerStage;
		
	}
	
	public void run()
	{
		
		try
		{
			// Show chooser that allows .npbk and .sql
			File file = NpbkFileChooserFX.showOpenCompanyDialog(ownerStage);
			// Load (supports both formats)
			CurrentCompany.loadFromPersistent(file);
			showInfo("Opened: " + file.getName());
		}
		catch (ActionCancelledException e)
		{
			// user cancelled; no-op
		}
		catch (NoFileException | IOException | NoFileCreatedException e)
		{
			showError("Unable to open file:\n" + e.getMessage());
		}
		catch (Exception e)
		{
			showError("Unexpected error:\n" + e.toString());
		}
		
	}
	
	private void showInfo(String msg)
	{
		new Alert(AlertType.INFORMATION, msg).showAndWait();
		
	}
	
	private void showError(String msg)
	{
		Alert alert = new Alert(AlertType.ERROR, msg);
		alert.initOwner(ownerStage);
		alert.showAndWait();
		
	}
	
}
