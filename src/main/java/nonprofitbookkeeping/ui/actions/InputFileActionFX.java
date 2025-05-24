/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * InputFileActionFX.java
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
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.preferences.PreferencesManager;
import nonprofitbookkeeping.ui.NonprofitBookkeepingFX;
import nonprofitbookkeeping.ui.actions.scaledger.SCALedgerDataLoader;

/**
 * JavaFX replacement for the Swing {@code InputFileAction}. Opens a native file
 * chooser, remembers the last directory via {@link PreferencesManager}, stores
 * the selected file in {@link NonprofitBookkeepingFX#currentFile}, and
 * feeds it to {@link SCALedgerDataLoader} to populate the global
 * {@link BeanShell#beans} map.
 */
public class InputFileActionFX implements EventHandler<ActionEvent>
{
	
	private final Stage owner; // needed for FileChooser modality
	
	public InputFileActionFX(Stage owner)
	{
		this.owner = owner;
	}
	
	@Override public void handle(ActionEvent e)
	{
		chooseFile();
	}
	
	/* --------------------------------------------------------------------- */
	private void chooseFile()
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Input File");
		
		// default dir → last-used or user's home
		String lastDir = PreferencesManager.getLastDirectory();
		File initialDir =
			lastDir != null ? new File(lastDir) : new File(System.getProperty("user.home"));
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
			Map<String, Object> beans = SCALedgerDataLoader.loadData(mappingFile, selected);
			BeanShell.setBeans(beans);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	private static void printCurrentWorkingDirectory()
	{
		System.out.println("Current working directory: " + System.getProperty("user.dir"));
	}
	
}
