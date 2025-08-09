
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.ui.panels.CreateOrEditCompanyPanelFX;
import static com.google.common.base.Preconditions.checkNotNull;

import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;

/**
 * Action class responsible for initiating the process to create a new company profile
 * or edit an existing one. It displays a dialog (`CreateOrEditCompanyPanelFX`)
 * to gather company information from the user.
 */
public class CreateOrEditCompanyActionFX
{
	
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
		Company existingCompany = null;
		
		if (!CurrentCompany.isOpen())
		{
			// If no company is open, we might be creating a new one.
			// A new Company object might be instantiated here, or
			// CurrentCompany.getCompany() might provide one.
			// The original code had: existingCompany = new Company();
			// Then immediately overwrote it. Let's clarify the intent.
			// For "create" mode, the panel likely needs a fresh or default
			// CompanyProfileModel.
			// If CurrentCompany.getCompany() returns a valid new/empty company, that's
			// fine.
			// If CurrentCompany.getCompany() could be null, then `new Company()` would be
			// appropriate.
			// The current logic relies on CurrentCompany.getCompany() to provide the
			// instance to be edited or filled.
		}
		
		// 1. Existing profile if you’re in "edit" mode, or a new one if creating.
		existingCompany = CurrentCompany.getCompany();
		// It's assumed CurrentCompany.getCompany() provides an appropriate instance
		// (new or existing).
		// If existingCompany is null here (e.g. first run, no default company in
		// CurrentCompany),
		// the CreateOrEditCompanyPanelFX constructor might need to handle it or a new
		// Company should be explicitly made.
		// For this Javadoc, we assume `existingCompany` will be non-null or handled by
		// the panel.
		
		// 2. Dialog-style stage
		Stage dialog = new Stage();
		dialog.initOwner(primaryStage);
		dialog.setTitle(CurrentCompany.isOpen() ? "Edit Company" : "Create Company");
		
		// 3. Build the FX panel
		CreateOrEditCompanyPanelFX panel = new CreateOrEditCompanyPanelFX(existingCompany,
			// void onCreatedProfileModel(CompanyProfileModel created)
			created ->
			{
				// callback fires when the wizard saves
				File out = new File(PreferencesService.getDefaultCompanyDir(),
					created.getCompanyName() + ".npbk");
				
				// Set the created file to the one just created.
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
				CurrentCompany.markCompanyOpen();
				
				dialog.close();
			});
			
		// 4. Show it
		dialog.setScene(new Scene(panel, 800, 600));
		dialog.show();
	}
	
	
}
