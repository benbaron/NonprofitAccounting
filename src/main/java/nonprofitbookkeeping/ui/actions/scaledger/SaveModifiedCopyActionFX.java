package nonprofitbookkeeping.ui.actions.scaledger;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.plugins.scaledger.SCALedgerPlugin;
import nonprofitbookkeeping.plugins.scaledger.ui.PageViewerPanel; // Added
import nonprofitbookkeeping.preferences.PreferencesManager;
// Removed: import nonprofitbookkeeping.model.NonCompanyFile; 
// Removed: import nonprofitbookkeeping.ui.actions.scaledger.PageViewer; // Assuming it was imported before

/**
 * JavaFX equivalent of the Swing {@code SaveModifiedCopyAction}. 
 * Saves the edited XLSM workbook to a user-chosen location.  Relies on
 * {@link ExcelDataWriter#writeModifiedCopy(File, File, Object,
 * javax.swing.table.TableModel)} for the actual export.
 */
public class SaveModifiedCopyActionFX implements EventHandler<ActionEvent>
{
	
	private final Stage owner;
	private final SCALedgerPlugin plugin; 
	
	public SaveModifiedCopyActionFX(Stage owner, SCALedgerPlugin plugin)
	{
		this.owner = owner;
		this.plugin = plugin; 
	}
	
	@Override public void handle(ActionEvent e)
	{
        if (this.plugin == null) {
            new Alert(Alert.AlertType.ERROR, "Plugin not initialized for SaveModifiedCopyActionFX.").showAndWait();
            return;
        }
		File input = this.plugin.getCurrentScaFile(); 
		
		if (input == null)
		{
			new Alert(Alert.AlertType.ERROR, "No input file loaded or ledger selected.").showAndWait();
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
		{
			return; // cancelled
		}
			
		try
		{
            PageViewerPanel pvp = this.plugin.getPageViewerPanel();
            if (pvp == null) {
                new Alert(Alert.AlertType.ERROR, "PageViewerPanel not initialized in plugin.").showAndWait();
                return;
            }
            javax.swing.table.DefaultTableModel modelToSave = pvp.getTableModel();
            if (modelToSave == null) { 
                 new Alert(Alert.AlertType.ERROR, "Data model not available from PageViewerPanel.").showAndWait();
                 return;
            }

			ExcelDataWriter.writeModifiedCopy(
				input,
				output,
				null, // sheetName - still null, potential issue in ExcelDataWriter
				modelToSave);
			new Alert(Alert.AlertType.INFORMATION, "Workbook saved successfully.").showAndWait();
			PreferencesManager.setLastDirectory(output.getParent());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			new Alert(Alert.AlertType.ERROR, "Failed to save workbook.\n" + ex.getMessage()).showAndWait();
		}
	}
}
