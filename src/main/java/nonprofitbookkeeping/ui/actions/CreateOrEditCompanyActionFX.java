
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.ui.panels.CreateCompanyPanelFX;
import static com.google.common.base.Preconditions.checkNotNull;

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
		Company existingCompany = null;
		
		if (!CurrentCompany.isOpen())
		{
			existingCompany = new Company();
		}
		
		// 1. Existing profile if you’re in “edit” mode
		existingCompany = CurrentCompany.getCompany();
		
		// 2. Dialog-style stage
		Stage dialog = new Stage();
		dialog.initOwner(primaryStage);
		dialog.setTitle(CurrentCompany.isOpen() ?
			"Edit Company" : "Create Company");
		
		// 3. Build the FX panel
		CreateCompanyPanelFX panel = new CreateCompanyPanelFX(
			existingCompany,
			// void onCreatedProfileModel(CompanyProfileModel created)
			created ->
			{				
				// callback fires when the wizard saves
				File out = new File(
					PreferencesService.getDefaultCompanyDir(),
					created.getCompanyName() + ".npbk");
				
				// Set the current filename.
				CurrentCompany.setCurrentFile(out);
				CurrentCompany.getCompany().setCompanyProfileModel(checkNotNull(created));
				
				try
				{
					CurrentCompany.persist();
				}
				catch (IOException | ActionCancelledException | NoFileCreatedException e)
				{
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
