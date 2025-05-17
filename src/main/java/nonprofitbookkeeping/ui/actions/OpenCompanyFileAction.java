/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * LoadCompanyFileAction.java
 * LoadCompanyFileAction
 */
package nonprofitbookkeeping.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.exception.NoFileException;
import nonprofitbookkeeping.model.CompanyDataFile;
import nonprofitbookkeeping.model.CurrentInputFile;
import nonprofitbookkeeping.ui.helpers.FileChooserHelper;
import nonprofitbookkeeping.ui.panels.AlertBox;

/**
 * 
 */
public class OpenCompanyFileAction
{
	public File currentInputFile;

	/**  
	 * Constructor LoadCompanyFileAction
	 * @param currentInputFile
	 */
	public OpenCompanyFileAction(File currentInputFile)
	{
		this.currentInputFile = currentInputFile;
	}

	/**
	 * @param e
	 * @throws NoFileException 
	 */
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			if (this.currentInputFile == null || 
				!this.currentInputFile.exists() || 
				!this.currentInputFile.canRead())
			{
				this.currentInputFile = FileChooserHelper.choose(".npbk"); // file extension desired or null
				if (this.currentInputFile == null)
				{
					AlertBox.showError(null, "Error loading file");
					return;
				}
				CurrentInputFile.setCurrentInputFile(this.currentInputFile);
			}
			
			// Load company from the data store
			CompanyDataFile.getCompanyDataFile().load(this.currentInputFile);
			AlertBox.showInfo(null, "Loaded "+ this.currentInputFile.toString());
		}
		catch (ActionCancelledException | NoFileCreatedException e1)
		{
			AlertBox.showError(null, "Error loading file:" + e1.getMessage());
			e1.printStackTrace();
		}
		
	}
	
}
