
package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.exception.NoFileException;
import nonprofitbookkeeping.model.CompanyDataFile;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.service.CompanyLoader;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.ui.panels.CreateCompanyPanelFX;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Action to create a new company or edit an existing company profile.
 */
public class CreateOrEditCompanyAction extends AbstractAction
{
	private static final long serialVersionUID = -3709294644528345190L;
	/**
	 * Constructor CreateOrEditCompanyAction
	 * @param parentFrame the parent frame
	 */
	public CreateOrEditCompanyAction(JFrame parentFrame)
	{
		super("Create or Edit Company...");
	}
	

	
	/**
	 * ActionListener override to handle the action of creating or editing a company.
	 */
	@Override public void actionPerformed(ActionEvent e)
	{
		// Retrieve the existing company profile if available
		CompanyDataFile existingProfile = CompanyDataFile.getCompanyDataFile();
			
		
		// Create a new JFrame to host the company creation/editing form
		JFrame createFrame =
			new JFrame(existingProfile != null ? "Edit Company" : "Create Company");
		
		// Create the panel, passing the existing profile if editing
		CreateCompanyPanelFX panel =
			new CreateCompanyPanelFX(existingProfile, (CompanyProfileModel created) ->
			{
				// Save or update the company profile upon finishing
				File out = new File(PreferencesService.getDefaultCompanyDir(),
					created.getCompanyName() + ".npbk");
				
				// Save the company profile (update if editing, save if new)
				CompanyLoader.saveCompanyProfile(out, created);
				
				// Set the last used company file path in preferences
				PreferencesService.setLastUsedCompanyFile(out.getAbsolutePath());
				
				// Close the create frame after saving
				createFrame.dispose();
			});
			

	}
	
	/**
	 * Helper method to retrieve the existing company profile if one exists.
	 * @return the existing company profile or null if none exists
	 */
	private static CompanyDataFile getExistingCompanyProfile()
	{
		// Check the preferences for the last used company file
		String lastCompanyPath = PreferencesService.getLastUsedCompanyFile();
		
		if (lastCompanyPath != null)
		{
			File companyFile = new File(lastCompanyPath);
			
			if (companyFile.exists())
			{
				
				// If the file exists, load and return the existing company profile
				try
				{
					return CompanyLoader.loadCompanyProfile(companyFile);
				}
				catch (NoFileException e)
				{
					e.printStackTrace();
				}
				
			}
			
		}
		
		return null; // Return null if no existing company is found (creating new)
	}
	
}
