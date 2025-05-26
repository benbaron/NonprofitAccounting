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
 * 
 */
public class CloseCompanyFileAction
{
	/**
	 * 
	 * Constructor CloseCompanyFileAction
	 * @param primaryStage 
	 */
	public CloseCompanyFileAction(Stage primaryStage)
	{
		// Store thyself
		try
		{
			CurrentCompany.persist();
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e)
		{
			e.printStackTrace();
		}
		CurrentCompany.close();
	}

	
}
