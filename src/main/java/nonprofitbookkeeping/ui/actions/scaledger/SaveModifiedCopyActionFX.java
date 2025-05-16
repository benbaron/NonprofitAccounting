
package nonprofitbookkeeping.ui.actions.scaledger;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.CurrentInputFile;
import nonprofitbookkeeping.preferences.PreferencesManager;

/**
 * JavaFX equivalent of the Swing {@code SaveModifiedCopyAction}. 
 * Saves the edited XLSM workbook to
 * a user-chosen location.  Relies on
 * {@link ExcelDataWriter#writeModifiedCopy(File, File, Object,
 * javax.swing.table.TableModel)} for the actual export.
 */
public class SaveModifiedCopyActionFX implements EventHandler<ActionEvent>
{
	
	private final Stage owner;
	
	public SaveModifiedCopyActionFX(Stage owner)
	{
		this.owner = owner;
	}
	
	@Override public void handle(ActionEvent e)
	{
		// Ensure an input workbook is loaded
		File input = CurrentInputFile.getCurrentInputFile();
		
		if (input == null)
		{
			new Alert(Alert.AlertType.ERROR, "No input file loaded or ledger selected.")
				.showAndWait();
			return;
		}
		
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Save Modified Workbook");
		chooser.getExtensionFilters()
			.add(new FileChooser.ExtensionFilter("Excel Macro-Enabled", "*.xlsm"));
		chooser.setInitialFileName("modified-" + input.getName());
		
		String lastDir = PreferencesManager.getLastDirectory();
		
		if (lastDir != null)
		{
			File dir = new File(lastDir);
			if (dir.exists())
				chooser.setInitialDirectory(dir);
		}
		
		File output = chooser.showSaveDialog(this.owner);
		if (output == null)
			return; // cancelled
			
		try
		{
			ExcelDataWriter.writeModifiedCopy(
				input,
				output,
				null,
				nonprofitbookkeeping.ui.panels.PageViewer.getTableModel());
			new Alert(Alert.AlertType.INFORMATION, "Workbook saved successfully.").showAndWait();
			PreferencesManager.setLastDirectory(output.getParent());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			new Alert(Alert.AlertType.ERROR, "Failed to save workbook.\n" + ex.getMessage())
				.showAndWait();
		}
		
	}
	
}
