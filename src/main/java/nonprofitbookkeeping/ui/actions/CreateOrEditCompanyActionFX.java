
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.DemoCompanySeeder;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.ui.panels.CreateOrEditCompanyPanelFX;
import static com.google.common.base.Preconditions.checkNotNull;

import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Action class responsible for initiating the process to create a new company profile
 * or edit an existing one. It displays a dialog (`CreateOrEditCompanyPanelFX`)
 * to gather company information from the user.
 */
public class CreateOrEditCompanyActionFX
{
	private final DemoCompanySeeder demoCompanySeeder = new DemoCompanySeeder();
	
	
	/**  
	 * Constructs and executes the action to create or edit a company profile.
	 * This constructor immediately sets up and displays a dialog window.
	 * <p>
	 * If a company is already open (determined by {@link CurrentCompany#isOpen()}),
	 * the action assumes "edit" mode and pre-populates the dialog with the
	 * existing company's profile (from {@link CurrentCompany#getCompany()}).
	 * If no company is open, it assumes "create" mode, initializing with a new, empty
	 * {@link Company} object if {@code CurrentCompany.getCompany()} were null (though current logic
	 * might re-fetch or overwrite this).
	 * </p>
	 * <p>
	 * The dialog uses a {@link CreateOrEditCompanyPanelFX}. Upon successful completion
	 * (e.g., user clicks "Save" in the panel), a callback is triggered which:
	 * <ul>
	 *   <li>Constructs a filename based on the company name and saves it to the
	 *       default company directory (obtained from {@link PreferencesService}).</li>
	 *   <li>Updates {@link CurrentCompany} with the new/edited profile and the file path.</li>
	 *   <li>Persists the company data via {@link CurrentCompany#persist()}.</li>
	 *   <li>Updates the "last used company file" preference.</li>
	 *   <li>Closes the dialog.</li>
	 * </ul>
	 * Any exceptions during persistence are caught and their stack traces printed.
	 * </p>
	 *
	 * @param primaryStage The primary {@link Stage} of the JavaFX application, used as the
	 *                     owner for the dialog window.
	 */
	public CreateOrEditCompanyActionFX(Stage primaryStage)
	{
		Company existingCompany = CurrentCompany.getCompany();
		
		if (existingCompany == null)
		{
			existingCompany = new Company();
		}
		
		Stage dialog = new Stage();
		dialog.initOwner(primaryStage);
		dialog.setTitle(
			CurrentCompany.isOpen() ? "Edit Company" : "Create Company");
		
		CreateOrEditCompanyPanelFX panel =
			new CreateOrEditCompanyPanelFX(existingCompany,
				(created, seedDemoData) ->
				{
					Company target = CurrentCompany.getCompany();
					
					if (target == null)
					{
						target = new Company();
						CurrentCompany.forceCompanyLoad(target);
					}
					
					boolean isNewCompany =
						CurrentCompany.getCurrentCompanyId() == null;
					
					target.setCompanyProfileModel(checkNotNull(created));
					
					if (seedDemoData && isNewCompany)
					{
						this.demoCompanySeeder.seed(target);
					}
					
					try
					{
						CurrentCompany.persist();
						PreferencesService.setLastUsedCompanyId(
							CurrentCompany.getCurrentCompanyId());
						CurrentCompany.markCompanyOpen();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					
					dialog.close();
				});
				
		panel.setDemoSeedingAvailable(!CurrentCompany.isOpen());
		
		dialog.setScene(new Scene(panel, 800, 600));
		dialog.show();
		
	}
	
	
}
