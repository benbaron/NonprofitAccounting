/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * StoreCompanyFileAction.java
 * StoreCompanyFileAction
 */
package nonprofitbookkeeping.ui.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;

import nonprofitbookkeeping.core.JacksonDataStore;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.CompanyDataFile;
import nonprofitbookkeeping.model.CurrentInputFile;

/**
 * 
 */
public class CloseCompanyFileAction
{
	/**
	 * 
	 * Constructor CloseCompanyFileAction
	 */
	public CloseCompanyFileAction()
	{
	}

	/**
	 * @param e
	 */
	public void actionPerformed(ActionEvent e)
	{
		CompanyDataFile.store();
		
		// Indicate that the company file is now closed
		// This is not the best way to do this since it
		// strongly couples the method to the data model.
		CompanyDataFile.setCompanyDataFile(null);
		CurrentInputFile.setCurrentInputFile(null);
		JacksonDataStore.setDataStore(null);
	}
	
}
