/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * StoreCompanyFileAction.java
 * StoreCompanyFileAction
 */
package nonprofitbookkeeping.ui.actions;

import java.io.IOException;

import javafx.stage.Stage;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.CurrentCompany;

/**
 * This saves the file to persistent memory without closing the company file
 */
public class SaveCompanyFileAction
{

	/**
	 * 
	 * Constructor SaveCompanyFileAction
	 * @param primaryStage 
	 */
	public SaveCompanyFileAction(Stage primaryStage)
	{
		// Store thyself to the file system
		try
		{
			CurrentCompany.persist();
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e)
		{
			e.printStackTrace();
		}
	}

	
}
