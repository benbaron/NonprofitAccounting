
package nonprofitbookkeeping.ui.actions;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.core.JacksonDataStore;
import nonprofitbookkeeping.model.CompanyDataFile;
import nonprofitbookkeeping.model.CurrentInputFile;
import nonprofitbookkeeping.ui.helpers.ActionCancelledException;
import nonprofitbookkeeping.ui.helpers.NoFileCreatedException;

public class OpenCompanyFileActionFX implements EventHandler<ActionEvent>
{
	
	private final Stage owner; // main window
	private final JacksonDataStore dataStore; // injected service
	
	public OpenCompanyFileActionFX(Stage owner, JacksonDataStore dataStore)
	{
		this.owner = owner;
		this.dataStore = dataStore;
	}
	
	/**
	 * 
	 * Override @see javafx.event.EventHandler#handle(javafx.event.Event)
	 */
	@Override public void handle(ActionEvent e)
	{
		
		try
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
			
			CompanyDataFile cdf = this.dataStore.load(CompanyDataFile.class, file);
			CompanyDataFile.setCompanyDataFile(cdf);
			showInfo("Loaded " + file.getAbsolutePath());
			
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			showError("Error loading file:\n" + ex.getMessage());
		}
		catch (ActionCancelledException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (NoFileCreatedException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
	private void showInfo(String msg)
	{
		new Alert(AlertType.INFORMATION, msg).showAndWait();
	}
	
	/**
	 * 
	 * @param msg
	 */
	private void showError(String msg)
	{
		new Alert(AlertType.ERROR, msg).showAndWait();
	}
	
}
