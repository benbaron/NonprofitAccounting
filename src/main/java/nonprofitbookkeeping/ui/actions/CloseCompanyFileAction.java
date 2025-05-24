/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * StoreCompanyFileAction.java
 * StoreCompanyFileAction
 */
package nonprofitbookkeeping.ui.actions;

import java.awt.event.ActionEvent;

import javafx.stage.Stage;
import nonprofitbookkeeping.core.JacksonDataStorer;
import nonprofitbookkeeping.model.Company;
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
		Company.store();
		
		// Indicate that the company file is now closed
		// This is not the best way to do this since it
		// strongly couples the method to the data model.
		
		Company.setCompany(null);
		Company.setCurrentFile(null);
		JacksonDataStorer.setDataStorer(null);
	}
	
}
