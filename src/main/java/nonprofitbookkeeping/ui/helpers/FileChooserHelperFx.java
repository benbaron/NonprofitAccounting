
package nonprofitbookkeeping.ui.helpers;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.preferences.PreferencesManager;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * JavaFX version of {@code FileChooserHelper}.
 * Opens a {@link FileChooser}, remembers the last directory, enforces an
 * optional extension, and (optionally) creates a missing file.
 */
public final class FileChooserHelperFx
{
	
	/** 
	 * choose
	 *  @param owner     JavaFX window that owns the dialog (may be {@code null}). 
	 *  @param extension Desired extension including the dot (e.g. ".npbk"); 
	 *                   pass {@code null} for no enforcement.
	 *  @throws ActionCancelledException if the user cancels.
	 *  @throws NoFileCreatedException   if the user declines or file creation fails. */
	public static File choose(Stage owner, String extension)
																throws ActionCancelledException,
																NoFileCreatedException
	{
		
		FileChooser chooser = new FileChooser();
		
		/* restore last directory */
		String lastDir = PreferencesManager.getLastDirectory();
		
		if (lastDir != null && new File(lastDir).exists())
		{
			chooser.setInitialDirectory(new File(lastDir));
		}
		
		/* optional filter */
		if (extension != null)
		{
			String extNoDot = extension.startsWith(".") ? extension.substring(1) : extension;
			chooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("*" + extension, "*" + extension));
		}
		
		File selected = chooser.showOpenDialog(owner);
		
		if (selected == null)
		{
			throw new ActionCancelledException("File selection cancelled.");
		}
		
		/* remember directory */
		PreferencesManager.setLastDirectory(selected.getParent());
		
		/* ensure extension */
		if (extension != null && !selected.getName().endsWith(extension))
		{
			selected = new File(selected.getAbsolutePath() + extension);
		}
		
		/* create if missing */
		if (!selected.exists())
		{
			Optional<ButtonType> res = new Alert(Alert.AlertType.CONFIRMATION,
				"The selected file does not exist.\nCreate it now?")
				.showAndWait();
			
			if (res.isEmpty() || res.get() != ButtonType.OK)
			{
				throw new NoFileCreatedException("User declined to create the file.");
			}
			
			try
			{
				
				if (!selected.createNewFile())
				{
					throw new NoFileCreatedException("Could not create the file.");
				}
				
			}
			catch (IOException io)
			{
				throw new NoFileCreatedException("IO error creating file: " + io.getMessage());
			}
			
		}
		
		return selected;
	}
	
	private FileChooserHelperFx()
	{
	} // utility class, no instances
	
}
