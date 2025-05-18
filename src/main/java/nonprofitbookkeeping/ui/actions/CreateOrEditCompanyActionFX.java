
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.model.CompanyDataFile;
import nonprofitbookkeeping.service.CompanyLoaderService;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.ui.panels.CreateCompanyPanelFX;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;

/**
 * Action to create a new company or edit an existing company profile.
 */
public class CreateOrEditCompanyActionFX implements EventHandler<Event>
{
	private Window ownerStage;

	/**  
	 * Constructor CreateOrEditCompanyAction
	 * @param primaryStage
	 */
	public CreateOrEditCompanyActionFX(Stage primaryStage)
	{
		this.ownerStage = primaryStage;	
	}
	
	/**
	 * Override @see javafx.event.EventHandler#handle(javafx.event.Event) 
	 */
	@Override public void handle(Event event)
	{

	    // 1. Existing profile if you’re in “edit” mode
	    CompanyDataFile existingProfile = CompanyDataFile.getCompanyDataFile();

	    // 2. Dialog-style stage
	    Stage dialog = new Stage();
	    dialog.initOwner(this.ownerStage);
	    dialog.setTitle(existingProfile != null ? 
	                    "Edit Company" : "Create Company");

	    // 3. Build the FX panel
	    CreateCompanyPanelFX panel = new CreateCompanyPanelFX(
	        existingProfile,
	        created -> {                     // callback fires when the wizard saves
	            File out = new File(
	                    PreferencesService.getDefaultCompanyDir(),
	                    created.getCompanyName() + ".npbk");

	            CompanyLoaderService.saveCompanyProfile(out, created);
	            
	            PreferencesService.setLastUsedCompanyFile(out.getAbsolutePath());

	            dialog.close();
	        });

	    // 4. Show it
	    dialog.setScene(new Scene(panel, 800, 600));
	    dialog.show();
	}


}
