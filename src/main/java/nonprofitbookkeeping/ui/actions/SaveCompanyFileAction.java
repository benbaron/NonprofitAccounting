/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * StoreCompanyFileAction.java
 * StoreCompanyFileAction
 */
package nonprofitbookkeeping.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javafx.stage.Stage;
import nonprofitbookkeeping.core.JacksonDataStorer;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.Company;

/**
 * This saves the file to persistent memory without closing the company file
 */
public class SaveCompanyFileAction
{

	private Company cdf;
	private JacksonDataStorer dataStorer;
	private File currentInputFile;

	/**
	 * 
	 * Constructor SaveCompanyFileAction
	 * @param primaryStage 
	 */
	public SaveCompanyFileAction(Stage primaryStage)
	{

	}

	/**
	 * @param e
	 */
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			this.dataStorer.saveData(this.cdf, this.currentInputFile);
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e1)
		{
			e1.printStackTrace();
		}
		
	}
	
}
