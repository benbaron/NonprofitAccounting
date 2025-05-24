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
import nonprofitbookkeeping.model.Company;

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
			Company.getCompany().persist();
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Company.getCompany().close();
	}

	
}
