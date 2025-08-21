
package nonprofitbookkeeping.ui.actions;

import java.io.File;
import java.io.IOException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.ui.helpers.NpbkFileChooserFX;

/**
 * Action to "Save As" the current company, supporting .npbk and .sql.
 * Appends the correct extension automatically and confirms overwrite inline.
 */
public class SaveCompanyFileAction
{
	
	private final Stage ownerStage;
	
	public SaveCompanyFileAction(Stage ownerStage)
	{
		this.ownerStage = ownerStage;
		
	}
	
	public void run()
	{
		
		try
		{
			// Suggest a base name; chooser appends .npbk or .sql automatically
			File target =
				NpbkFileChooserFX.showSaveCompanyDialog(ownerStage, "company");
			
			// Inline overwrite confirmation (no dependency on helper)
			if (target.exists())
			{
				Alert alert = new Alert(AlertType.CONFIRMATION,
					"The file \"" + target.getName() +
						"\" already exists. Overwrite?");
				alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
				alert.initOwner(ownerStage);
				var result = alert.showAndWait();
				
				if (result.isEmpty() || result.get() != ButtonType.OK)
				{
					// Treat as user cancel
					return;
				}
				
			}
			
			CurrentCompany.setCurrentFile(target);
			CurrentCompany.persist();
			showInfo("Saved: " + target.getName());
		}
		catch (ActionCancelledException e)
		{
			// user cancelled; no-op
		}
		catch (NoFileCreatedException | IOException e)
		{
			showError("Unable to save file:\n" + e.getMessage());
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
		Alert a = new Alert(AlertType.ERROR, msg);
		a.initOwner(ownerStage);
		a.showAndWait();
		
	}
	
}
