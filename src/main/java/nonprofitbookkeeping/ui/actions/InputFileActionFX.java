/**
 * nonprofit-scaledger-ribbon.zip_expanded InputFileActionFX.java
 * InputFileActionFX
 */

package nonprofitbookkeeping.ui.actions;


import java.io.File;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.BeanShell;
import nonprofitbookkeeping.preferences.PreferencesManager;
import nonprofitbookkeeping.ui.actions.scaledger.SCALedgerDataLoader;
import nonprofitbookkeeping.ui.helpers.AlertBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX replacement for the Swing {@code InputFileAction}. Opens a native file
 * chooser, remembers the last directory via {@link PreferencesManager}.
 * The selected file is then processed by {@link SCALedgerDataLoader} using a
 * mapping file ("jxlsMapping.xml") to populate a global bean map via {@link BeanShell#setBeans(Map)}.
 * Note: The reference to storing the selected file in {@code NonprofitBookkeepingFX#currentFile}
 * in the original class Javadoc does not seem to be implemented in the provided code.
 */
public class InputFileActionFX implements EventHandler<ActionEvent>
{
	
	/** The owner Stage for the FileChooser dialog, ensuring proper modality. */
	private final Stage owner;
	
	private static final Logger LOGGER =
		LoggerFactory.getLogger(InputFileActionFX.class);
	
	/**
	 * Constructs a new {@code InputFileActionFX}.
	 *
	 * @param owner The owner {@link Stage} for the {@link FileChooser} dialog that will be displayed.
	 *              This is necessary for correct dialog behavior and modality. Must not be null.
	 * @throws NullPointerException if {@code owner} is null (though not explicitly checked in constructor).
	 */
	public InputFileActionFX(Stage owner)
	{
		this.owner = owner;
		
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Handles the action event, typically from a menu item or button, by calling
	 * the {@link #chooseFile()} method to initiate the file selection and processing workflow.
	 * </p>
	 * @param e The {@link ActionEvent} that triggered this handler.
	 */
	@Override
	public void handle(ActionEvent e)
	{
		chooseFile();
		
	}
	
	/**
	 * Opens a {@link FileChooser} dialog to allow the user to select an input file.
	 * The dialog's initial directory is set based on the last used directory preference
	 * (via {@link PreferencesManager#getLastDirectory()}), defaulting to the user's home directory.
	 * <p>
	 * If a file is selected:
	 * <ul>
	 *   <li>The parent directory of the selected file is saved as the new last used directory preference.</li>
	 *   <li>The current working directory is printed to standard output.</li>
	 *   <li>The selected file is processed by {@link SCALedgerDataLoader#loadData(File, File)}
	 *       using a hardcoded mapping file named "jxlsMapping.xml" (expected in the current working directory).</li>
	 *   <li>The data loaded by {@code SCALedgerDataLoader} (a {@code Map<String, Object>}) is then set
	 *       globally using {@link BeanShell#setBeans(Map)}.</li>
	 * </ul>
	 * Exceptions during data loading are caught and their stack traces printed to standard error.
	 * If the user cancels the file selection, the method returns without further action.
	 * </p>
	 */
	private void chooseFile()
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Input File");
		
		// default dir → last-used or user's home
		String lastDir = PreferencesManager.getLastDirectory();
		File initialDir =
			lastDir != null ? new File(lastDir) :
				new File(System.getProperty("user.home"));
		
		if (initialDir.exists())
		{
			chooser.setInitialDirectory(initialDir);
		}
		
		File selected = chooser.showOpenDialog(this.owner);
		
		if (selected == null)
		{
			return; // user cancelled
		}
		
		PreferencesManager.setLastDirectory(selected.getParent());
		
		printCurrentWorkingDirectory();
		
		File mappingFile = new File("jxlsMapping.xml");
		
		try
		{
			Map<String, Object> beans =
				SCALedgerDataLoader.loadData(mappingFile, selected);
			BeanShell.setBeans(beans);
		}
		catch (Exception ex)
		{
			LOGGER.error("Failed to load data from {}", selected, ex);
			AlertBox.showError(this.owner,
				"Failed to load data: " + ex.getMessage());
		}
		
	}
	
	/**
	 * Prints the current working directory (obtained from {@code System.getProperty("user.dir")})
	 * to standard output. This method is static and can be called without an instance.
	 */
	private static void printCurrentWorkingDirectory()
	{
		LoggerFactory.getLogger(InputFileActionFX.class)
			.debug("Current working directory: {}",
				System.getProperty("user.dir"));
		
	}
	
}
