
package nonprofitbookkeeping.ui.helpers;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.exception.NoFileException;
import nonprofitbookkeeping.preferences.PreferencesManager;

/**
 * Unified Java FX file-chooser helper.
 *
 * <ul>
 *   <li>{@link #chooseExisting} replicates the original npbkFileChooser behaviour:
 *       pick an existing file or throw {@link NoFileException} on cancel.</li>
 *   <li>{@link #chooseAndCreate} adds extension enforcement, remembers the last
 *       directory, and (optionally) creates the file if it does not exist.</li>
 * </ul>
 * This class is final and cannot be instantiated.
 */
public final class NpbkFileChooserFX
{
	
	/**
     * Displays a JavaFX {@link FileChooser} configured to select an existing file.
     * The dialog uses the provided title, description, and file pattern for its filter.
     * It sets the initial directory based on {@link PreferencesManager#getLastDirectory()}.
     * If a file is selected, its parent directory is saved back to preferences.
     *
     * @param title The title for the file chooser dialog window.
     * @param description The description for the file extension filter (e.g., "Company file").
     * @param filePattern The file extension pattern (e.g., "*.npbk").
     * @param owner The owner {@link Stage} for the dialog. This helps with proper modality.
     * @return The selected {@link File}.
     * @throws NoFileException if the user cancels the dialog or no file is selected.
     */
	public static File chooseExisting(	String title,
										String description,
										String filePattern,
										Stage owner) throws NoFileException
	{
		
		FileChooser ch = new FileChooser();
		ch.setTitle(title);
		ch.getExtensionFilters()
			.add(new FileChooser.ExtensionFilter(description, filePattern));
		
		setInitialDir(ch);
		File f = ch.showOpenDialog(owner); // Shows an "Open" dialog
		if (f == null)
		{
			throw new NoFileException("User cancelled file selection.");
		}
		PreferencesManager.setLastDirectory(f.getParent());
		return f;
	}
	
	/**
     * Displays a JavaFX {@link FileChooser} that allows the user to select an existing file
     * or specify a new filename. It can enforce a specific file {@code extension} and
     * optionally prompt to create the file if it doesn't exist.
     * <p>
     * The dialog's initial directory is set from {@link PreferencesManager#getLastDirectory()}.
     * If a file is chosen (either existing or a new name):
     * <ul>
     *   <li>The parent directory is saved to preferences.</li>
     *   <li>If an {@code extension} is provided and the selected filename doesn't have it,
     *       the extension is appended.</li>
     *   <li>If the (potentially extension-modified) file does not exist, the user is prompted
     *       with a confirmation dialog to create it. If declined, {@link NoFileCreatedException} is thrown.
     *       If creation fails due to an {@link IOException}, {@link NoFileCreatedException} is also thrown,
     *       wrapping the original IO error.</li>
     * </ul>
     * </p>
     *
     * @param owner The owner {@link Stage} for the dialogs.
     * @param title The title for the file chooser dialog window.
     * @param extension The desired file extension (e.g., ".npbk" or "npbk"). If null, no specific
     *                  extension is enforced or added, though a generic filter might still be applied.
     * @return The selected or created {@link File}.
     * @throws ActionCancelledException if the user cancels the initial file chooser dialog.
     * @throws NoFileCreatedException if the user declines to create a new file when prompted,
     *                                or if file creation fails.
     */
	public static File chooseAndCreate(	Stage owner,
										String title,
										String extension)
															throws ActionCancelledException,
															NoFileCreatedException
	{
		
		FileChooser ch = new FileChooser();
		ch.setTitle(title);
		
		if (extension != null)
		{
			String patt = "*" + (extension.startsWith(".") ? extension : "." + extension);
			ch.getExtensionFilters()
				.add(new FileChooser.ExtensionFilter("Files (" + patt + ")", patt));
		}
		
		setInitialDir(ch);
		
		File selected = ch.showOpenDialog(owner);
		if (selected == null)
		{
			throw new ActionCancelledException("Chooser cancelled.");
		}
		
		PreferencesManager.setLastDirectory(selected.getParent());
		
		if (extension != null && !selected.getName().endsWith(extension))
		{
			selected = new File(selected.getAbsolutePath() + extension);
		}
		
		if (!selected.exists())
		{
			Optional<ButtonType> res = new Alert(Alert.AlertType.CONFIRMATION,
				"File does not exist.\nCreate it now?")
				.showAndWait();
			if (res.isEmpty() || res.get() != ButtonType.OK)
			{
				throw new NoFileCreatedException("User declined to create file.");
			}
				
			try
			{
				if (!selected.createNewFile())
					throw new NoFileCreatedException("Could not create file.");
			}
			catch (IOException io)
			{
				throw new NoFileCreatedException("IO error: " + io.getMessage());
			}
			
		}
		
		return selected;
	}
	
	/**
     * Sets the initial directory for the given {@link FileChooser} based on the
     * last used directory stored in {@link PreferencesManager}.
     * If no last directory is found in preferences, it defaults to the user's home directory.
     * The chooser's initial directory is only set if the determined path exists.
     *
     * @param ch The {@link FileChooser} whose initial directory is to be set.
     */
	private static void setInitialDir(FileChooser ch)
	{
		String lastDir = PreferencesManager.getLastDirectory();
		if (lastDir == null || lastDir.trim().isEmpty()) // Check for empty string too
		{
			lastDir = System.getProperty("user.home");
		}
		File dir = new File(lastDir);
		if (dir.exists() && dir.isDirectory()) // Ensure it's a directory
		{
			ch.setInitialDirectory(dir);
		} else {
            // Fallback if preferred directory doesn't exist or isn't a directory
            File homeDir = new File(System.getProperty("user.home"));
            if (homeDir.exists() && homeDir.isDirectory()) {
                ch.setInitialDirectory(homeDir);
            }
            // If home also fails, FileChooser will use its own default.
        }
	}
	
	/**
     * Private constructor to prevent instantiation of this utility class.
     */
	private NpbkFileChooserFX()
	{
	} // utility class
	
}
