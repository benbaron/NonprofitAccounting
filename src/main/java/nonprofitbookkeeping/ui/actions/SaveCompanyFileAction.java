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
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.CompanyDataFile;

/**
 * This saves the file to persistent memory without closing the company file
 */
public class SaveCompanyFileAction
{

	private CompanyDataFile cdf;
	private JacksonDataStore dataStore;
	private File currentInputFile;

	/**
	 * 
	 * Constructor SaveCompanyFileAction
	 * @param currentInputFile
	 * @param dataStore2
	 * @param cdf
	 */
	public SaveCompanyFileAction(File currentInputFile, JacksonDataStore dataStore2, CompanyDataFile cdf)
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
		
	}
	
}
