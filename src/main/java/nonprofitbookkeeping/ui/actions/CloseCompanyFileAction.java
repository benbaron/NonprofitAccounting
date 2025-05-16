/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * StoreCompanyFileAction.java
 * StoreCompanyFileAction
 */
package nonprofitbookkeeping.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import nonprofitbookkeeping.core.JacksonDataStore;
import nonprofitbookkeeping.model.CompanyDataFile;
import nonprofitbookkeeping.model.CurrentInputFile;
import nonprofitbookkeeping.ui.helpers.ActionCancelledException;
import nonprofitbookkeeping.ui.helpers.NoFileCreatedException;

/**
 * 
 */
public class CloseCompanyFileAction
{

	private CompanyDataFile cdf;
	private JacksonDataStore dataStore;
	private File currentInputFile;

	/**
	 * 
	 * Constructor CloseCompanyFileAction
	 * @param currentInputFile
	 * @param dataStore2
	 * @param cdf
	 */
	public CloseCompanyFileAction(File currentInputFile, JacksonDataStore dataStore2, CompanyDataFile cdf)
	{
		this.cdf = cdf;
		this.currentInputFile = currentInputFile;
		this.dataStore = dataStore2;
	}

	/**
	 * @param e
	 */
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			this.dataStore.save(this.cdf, this.currentInputFile);
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e1)
		{
			e1.printStackTrace();
		}
		
		// Indicate that the company file is now closed
		// This is not the best way to do this since it
		// strongly couples the method to the data model.
		CompanyDataFile.setCompanyDataFile(null);
		CurrentInputFile.setCurrentInputFile(null);
		JacksonDataStore.setDataStore(null);
	}
	
}
