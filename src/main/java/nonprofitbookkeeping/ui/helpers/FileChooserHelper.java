
package nonprofitbookkeeping.ui.helpers;

import nonprofitbookkeeping.preferences.PreferencesManager;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * FileChooserHelper class for handling file selection and creation, remembering the last selected directory.
 */
public class FileChooserHelper
{
	
	/**
	 * Opens a file chooser dialog to allow the user to select a file.
	 * If the file does not exist, prompts the user to create it.
	 * Ensures the file extension is as desired.
	 * 
	 * @param extension The desired file extension (including dot), or null for no specific extension.
	 * @return The selected file, or throws an exception if no file is selected or created.
	 * @throws ActionCancelledException if the user cancels the file selection.
	 * @throws NoFileCreatedException if the file does not exist and the user opts not to create it or if file creation fails.
	 */
	public static File choose(String extension) throws ActionCancelledException,
												NoFileCreatedException
	{
		JFileChooser fileChooser = new JFileChooser();
		
		// Load the last directory from the preferences manager
		File lastDirectory = new File(PreferencesManager.getLastDirectory());
		
		if (lastDirectory.exists())
		{
			fileChooser.setCurrentDirectory(lastDirectory); // Set the last used directory
		}
		
		// If an extension is provided, set the filter to that extension
		if (extension != null)
		{
//			fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
//				"Files (*" + extension + ")", extension));
		}
		
		int returnValue = fileChooser.showDialog(null, "Select");
		
		if (returnValue == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = fileChooser.getSelectedFile();
			
			// Save the directory of the selected file for future use
			PreferencesManager.setLastDirectory(selectedFile.getParent());
			
			// If the file does not have the desired extension, add it
			if (extension != null && !selectedFile.getName().endsWith(extension))
			{
				selectedFile = new File(selectedFile.getAbsolutePath() + extension);
			}
			
			// Check if the selected file exists
			if (!selectedFile.exists())
			{
				// Prompt the user if they want to create the file
				int createFileOption = JOptionPane.showConfirmDialog(
					null,
					"The selected file does not exist. Would you like to create it?",
					"File Does Not Exist",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
				
				if (createFileOption == JOptionPane.YES_OPTION)
				{					
					try
					{						
						// Create the file
						if (selectedFile.createNewFile())
						{
							return selectedFile; // Return the created file
						}
						else
						{
							// If file creation fails, throw NoFileCreatedException
							throw new NoFileCreatedException("Failed to create the file.");
						}
						
					}
					catch (IOException e)
					{
						// Handle any IOExceptions during file creation
						throw new NoFileCreatedException(
							"Error creating the file: " + e.getMessage());
					}
					
				}
				else
				{
					// User declined to create the file, throw NoFileCreatedException
					throw new NoFileCreatedException(
						"File creation was cancelled by the user.");
				}
				
			}
			
			return selectedFile; // Return the file if it already exists
		}
		
		// User cancelled the action, throw ActionCancelledException
		throw new ActionCancelledException(
			"File selection was cancelled by the user.");
	}
	
}
