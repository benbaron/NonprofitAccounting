
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
 */
public final class NpbkFileChooserFX
{
	
	/* --------------------------------------------------------------------- */
	/** Original behaviour: just pick a file; must exist. */
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
		File f = ch.showOpenDialog(owner);
		if (f == null)
		{
			throw new NoFileException("User cancelled.");
		}
		PreferencesManager.setLastDirectory(f.getParent());
		return f;
	}
	
	/* --------------------------------------------------------------------- */
	/**
	 * Enhanced chooser: remembers last dir, enforces {@code extension},
	 * optionally creates the file.  Throws:
	 * <ul>
	 *   <li>{@link ActionCancelledException} if the dialog is cancelled</li>
	 *   <li>{@link NoFileCreatedException}  if user declines or creation fails</li>
	 * </ul>
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
	
	/* --------------------------------------------------------------------- */
	/** Utility — set chooser’s initial directory from PreferencesManager. */
	private static void setInitialDir(FileChooser ch)
	{
		String lastDir = PreferencesManager.getLastDirectory();
		if (lastDir == null)
		{
			lastDir = System.getProperty("user.home");
		}
		File dir = new File(lastDir);
		if (dir.exists())
		{
			ch.setInitialDirectory(dir);
		}
	}
	
	private NpbkFileChooserFX()
	{
	} // utility class
	
}
