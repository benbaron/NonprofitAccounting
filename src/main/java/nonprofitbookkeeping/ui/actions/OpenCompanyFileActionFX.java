
package nonprofitbookkeeping.ui.actions;

import java.io.File;
import java.io.IOException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.exception.NoFileException;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.ui.helpers.NpbkFileChooserFX;

/**
 * Action class responsible for handling the opening of an existing company data file.
 * This action uses a custom file chooser ({@link NpbkFileChooserFX}) to allow the user
 * to select a ".npbk" company file. Upon selection, it loads the company data
 * into the {@link CurrentCompany} context and marks the company as open.
 */
public class OpenCompanyFileActionFX
{
	private final Runnable onSuccessCallback;
	
	/**
	 * Constructs and executes the action to open an existing company file.
	 * This constructor immediately initiates the file selection process.
	 * <p>
	 * The process involves:
	 * <ol>
	 *   <li>Displaying a file chooser dialog (via {@link NpbkFileChooserFX#chooseExisting})
	 *       filtered for ".npbk" files.</li>
	 *   <li>If a file is selected, it loads the company data from this file using
	 *       {@link CurrentCompany#loadFromPersistent(File)}.</li>
         *   <li>Marks the company as open using {@link CurrentCompany#markCompanyOpen()}.</li>
	 *   <li>Displays an informational alert showing the path of the loaded file.</li>
	 * </ol>
	 * If the user cancels the file selection or if any error occurs during file loading
	 * (e.g., file not found, I/O error, parsing error), the respective exception is thrown.
	 * </p>
	 *
	 * @param owner The owner {@link Stage} for the file chooser dialog.
	 * @param onSuccessCallback A {@link Runnable} to be executed upon successful company file opening.
	 * @throws NoFileException If the user cancels the file selection or no file is chosen.
	 * @throws IOException If an I/O error occurs during file loading.
	 * @throws ActionCancelledException If the file loading action is explicitly cancelled by other means.
	 * @throws NoFileCreatedException If {@code CurrentCompany.loadFromPersistent} fails in a way that indicates no valid file was processed (though typically IOException or NoFileException might be more direct from chooser/loading).
	 * @throws Exception For any other unspecified errors during the process.
	 */
	public OpenCompanyFileActionFX(Stage owner, Runnable onSuccessCallback) throws Exception
	{
		this.onSuccessCallback = onSuccessCallback;
		try
		{
			File file = NpbkFileChooserFX.chooseExisting("Open File",
				"Company file",
				"*.npbk", // File extension filter
				owner);
			
			// Load file from the file system
                        CurrentCompany.loadFromPersistent(file); // This can throw various exceptions
                        CurrentCompany.markCompanyOpen(); // Mark company as open in the application context
			
			// If all above operations were successful, run the callback
			if (this.onSuccessCallback != null) {
				this.onSuccessCallback.run();
			}

			showInfo("Loaded " + file.getAbsolutePath());
		}
		catch (NoFileException | IOException | ActionCancelledException | NoFileCreatedException e1)
		{
			// Re-throw caught exceptions to be handled by the caller or a global exception handler.
			throw e1;
		}
		// Other RuntimeExceptions or Errors will propagate naturally.
	}
	
	
	/**
	 * Displays an informational alert dialog with the given message.
	 * The dialog is modal and waits for the user to close it.
	 * 
	 * @param msg The message to display in the alert dialog.
	 */
	private static void showInfo(String msg)
	{
		new Alert(AlertType.INFORMATION, msg).showAndWait();
	}
	
}
