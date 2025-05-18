/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * StoreCompanyFileAction.java
 * StoreCompanyFileAction
 */
package nonprofitbookkeeping.ui.actions;

import java.awt.event.ActionEvent;

import javafx.stage.Stage;
import nonprofitbookkeeping.core.JacksonDataStore;
import nonprofitbookkeeping.model.CompanyDataFile;
import nonprofitbookkeeping.model.NonCompanyFile;

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
	}

	

	/**
	 * @param e
	 */
	public static void actionPerformed(ActionEvent e)
	{
		CompanyDataFile.store();
		
		// Indicate that the company file is now closed
		// This is not the best way to do this since it
		// strongly couples the method to the data model.
		
		CompanyDataFile.setCompanyDataFile(null);
		CompanyDataFile.setCurrentFile(null);
		JacksonDataStore.setDataStore(null);
	}
	
}
