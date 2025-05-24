
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.ui.panels.CreateCompanyPanelFX;

import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;

/**
 * Action to create a new company or edit an existing company profile.
 */
public class CreateOrEditCompanyActionFX
{

	/**  
	 * Constructor CreateOrEditCompanyAction
	 * @param primaryStage
	 */
	public CreateOrEditCompanyActionFX(Stage primaryStage)
	{
	    // 1. Existing profile if you’re in “edit” mode
	    Company existingCompany = Company.getCompany();

	    // 2. Dialog-style stage
	    Stage dialog = new Stage();
	    dialog.initOwner(primaryStage);
	    dialog.setTitle(existingCompany.isOpen() ? 
	                    "Edit Company" : "Create Company");

	    // 3. Build the FX panel
	    CreateCompanyPanelFX panel = new CreateCompanyPanelFX(
	        existingCompany,
	        /* CompanyProfileModel */ created -> {
	        	// callback fires when the wizard saves
	            File out = new File(
	                    PreferencesService.getDefaultCompanyDir(),
	                    created.getCompanyName() + ".npbk");

	            // get the company singleton, 
	            // set its filename,
	            // store it
	            Company c = Company.getCompany();
	            c.setCurrentFile(out);
	            try
				{
					c.persist();
				}
				catch (IOException | ActionCancelledException | NoFileCreatedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
	            PreferencesService.setLastUsedCompanyFile(out.getAbsolutePath());

	            dialog.close();
	        });

	    // 4. Show it
	    dialog.setScene(new Scene(panel, 800, 600));
	    dialog.show();
	}


}
