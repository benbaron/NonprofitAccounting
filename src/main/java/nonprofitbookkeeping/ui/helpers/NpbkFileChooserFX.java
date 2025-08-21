
package nonprofitbookkeeping.ui.helpers;

import java.io.File;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.exception.NoFileException;
import nonprofitbookkeeping.preferences.PreferencesManager;

/**
 * Centralized JavaFX FileChooser helpers for company files.
 * Ensures .npbk (zip) and .sql (H2 script) are both offered everywhere.
 */
public final class NpbkFileChooserFX
{
	
	private static final FileChooser.ExtensionFilter NPBK_FILTER =
		new FileChooser.ExtensionFilter("Nonprofit company (*.npbk)", "*.npbk");
	private static final FileChooser.ExtensionFilter SQL_FILTER =
		new FileChooser.ExtensionFilter("H2 SQL script (*.sql)", "*.sql");
	private static final FileChooser.ExtensionFilter ALL_FILTER =
		new FileChooser.ExtensionFilter("All files", "*.*");
	
	/** Configure standard filters and default selection (.npbk). */
	private static void configureCompanyFilters(FileChooser ch)
	{
		ch.getExtensionFilters().setAll(NPBK_FILTER, SQL_FILTER, ALL_FILTER);
		ch.setSelectedExtensionFilter(NPBK_FILTER);
		
	}
	
	/** If saving and user omitted the extension, append the one from the selected filter. */
	private static File ensureSelectedExtension(File chosen, FileChooser ch)
	{
		if (chosen == null)
			return null;
		final FileChooser.ExtensionFilter sel = ch.getSelectedExtensionFilter();
		if (sel == null)
			return chosen;
		final String name = chosen.getName().toLowerCase();
		
		if (sel == NPBK_FILTER)
		{
			return name.endsWith(".npbk") ? chosen :
				new File(chosen.getParentFile(), chosen.getName() + ".npbk");
		}
		else if (sel == SQL_FILTER)
		{
			return name.endsWith(".sql") ? chosen :
				new File(chosen.getParentFile(), chosen.getName() + ".sql");
		}
		
		return chosen;
		
	}
	
	/** Open: allow .npbk and .sql */
	public static File showOpenCompanyDialog(Stage owner)
		throws NoFileException, ActionCancelledException
	{
		FileChooser ch = new FileChooser();
		configureCompanyFilters(ch);
		restoreInitialDirectory(ch);
		File f = ch.showOpenDialog(owner);
		
		if (f == null)
		{
			throw new ActionCancelledException("Open cancelled");
		}
		
		PreferencesManager.setLastDirectory(f.getParent());
		return f;
		
	}
	
	/** Save As: allow .npbk and .sql, append extension automatically. */
	public static File showSaveCompanyDialog(Stage owner, String suggestedName)
		throws NoFileCreatedException, ActionCancelledException
	{
		FileChooser ch = new FileChooser();
		configureCompanyFilters(ch);
		
		if (suggestedName != null && !suggestedName.isBlank())
		{
			ch.setInitialFileName(suggestedName);
		}
		
		restoreInitialDirectory(ch);
		File chosen = ch.showSaveDialog(owner);
		
		if (chosen == null)
		{
			throw new ActionCancelledException("Save cancelled");
		}
		
		chosen = ensureSelectedExtension(chosen, ch);
		
		try
		{
			File parent = chosen.getParentFile();
			if (parent != null && !parent.exists())
				parent.mkdirs();
			PreferencesManager.setLastDirectory(parent.getAbsolutePath());
			return chosen;
		}
		catch (Exception e)
		{
			throw new NoFileCreatedException(
				"Cannot use selected file: " + e.getMessage(), e);
		}
		
	}
	
	/** Restore last-used folder or fall back to user.home. */
	private static void restoreInitialDirectory(FileChooser ch)
	{
		String lastDir = PreferencesManager.getLastDirectory();
		
		if (lastDir == null || lastDir.trim().isEmpty())
		{
			lastDir = System.getProperty("user.home");
		}
		
		File dir = new File(lastDir);
		
		if (dir.exists() && dir.isDirectory())
		{
			ch.setInitialDirectory(dir);
		}
		
	}
	
	private NpbkFileChooserFX()
	{
	
	}
	
}
