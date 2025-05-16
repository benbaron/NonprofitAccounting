/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * LoadCompanyFileAction.java
 * LoadCompanyFileAction
 */
package nonprofitbookkeeping.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import nonprofitbookkeeping.core.JacksonDataStore;
import nonprofitbookkeeping.model.CompanyDataFile;
import nonprofitbookkeeping.model.CurrentInputFile;
import nonprofitbookkeeping.service.NoFileException;
import nonprofitbookkeeping.ui.helpers.ActionCancelledException;
import nonprofitbookkeeping.ui.helpers.FileChooserHelper;
import nonprofitbookkeeping.ui.helpers.NoFileCreatedException;
import nonprofitbookkeeping.ui.panels.AlertBox;

/**
 * 
 */
public class OpenCompanyFileAction
{

	private JacksonDataStore dataStore;
	private File currentInputFile;

	/**  
	 * Constructor LoadCompanyFileAction
	 * @param currentInputFile
	 * @param dataStore 
	 */
	public OpenCompanyFileAction(File currentInputFile, 
	                             JacksonDataStore dataStore)
	{
		this.dataStore = dataStore;
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
			
			CompanyDataFile companyDataFile = this.dataStore.load(CompanyDataFile.class, 	
				this.currentInputFile);
			CompanyDataFile.setCompanyDataFile(companyDataFile);
			AlertBox.showInfo(null, "Loaded "+ this.currentInputFile.toString());
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e1)
		{
			AlertBox.showError(null, "Error loading file:" + e1.getMessage());
			e1.printStackTrace();
		}
		
	}
	
}
