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
 * {@link ExcelDataWriter#writeModifiedCopy(File, File, String, javax.swing.table.DefaultTableModel)}
 * for the actual export, using data from the plugin's {@link PageViewerPanel}.
 */
public class SaveModifiedCopyActionFX implements EventHandler<ActionEvent>
{
	
	/** The owner {@link Stage} for displaying dialogs like the FileChooser. */
	private final Stage owner;
	/**
	 * The {@link SCALedgerPlugin} instance from which to get the current SCA file (input)
	 * and the {@link PageViewerPanel} containing the data model to save.
	 */
	private final SCALedgerPlugin plugin; 
	
	/**
     * Constructs a new {@code SaveModifiedCopyActionFX}.
     *
     * @param owner The owner {@link Stage} for the {@link FileChooser} dialog. Must not be null.
     * @param plugin The {@link SCALedgerPlugin} instance providing context, such as the input file
     *               and the data model to be saved. Must not be null.
     * @throws NullPointerException if {@code owner} or {@code plugin} is null (though not explicitly checked here,
     *                              they are later used and would cause NPE if null).
     */
	public SaveModifiedCopyActionFX(Stage owner, SCALedgerPlugin plugin)
	{
		this.owner = owner;
		this.plugin = plugin; 
	}
	
	/**
     * {@inheritDoc}
     * <p>
     * Handles the action event, typically from a menu item, to save a modified copy of an XLSM workbook.
     * The process involves:
     * <ol>
     *   <li>Checking if the associated {@link SCALedgerPlugin} and its current input file are available.
     *       Shows an error alert if not.</li>
     *   <li>Displaying a {@link FileChooser} to the user to select a destination file path for the modified copy.
     *       The initial filename suggests "modified-[original_filename].xlsm".
     *       The initial directory is based on preferences.</li>
     *   <li>If a destination file is selected:
     *     <ul>
     *       <li>Retrieves the {@link PageViewerPanel} and its {@link javax.swing.table.DefaultTableModel} from the plugin.
     *           Shows an error if the panel or model is unavailable.</li>
     *       <li>Calls {@link ExcelDataWriter#writeModifiedCopy(File, File, String, javax.swing.table.DefaultTableModel)}
     *           to write the data from the table model to the selected output file, using the original input file as a template.
     *           (Note: The {@code sheetName} parameter is currently passed as {@code null} to {@code writeModifiedCopy},
     *           which might be an issue depending on the writer's implementation).</li>
     *       <li>Shows an information alert upon successful save, or an error alert if an exception occurs.</li>
     *       <li>Updates the last used directory preference.</li>
     *     </ul>
     *   </li>
     *   <li>If file selection is cancelled, the method returns without further action.</li>
     * </ol>
     * </p>
     * @param e The {@link ActionEvent} that triggered this handler.
     */
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
				"Sheet1", // sheetName - Using "Sheet1" as a default
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
