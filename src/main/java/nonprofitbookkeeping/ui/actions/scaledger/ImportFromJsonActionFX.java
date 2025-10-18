
package nonprofitbookkeeping.ui.actions.scaledger;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.plugins.scaledger.SCALedgerPlugin; // Added
import nonprofitbookkeeping.plugins.scaledger.ui.PageViewerPanel; // Added
import nonprofitbookkeeping.preferences.PreferencesManager;

/**
 * JavaFX version of the Swing {@code ImportFromJsonAction}. Opens a JSON file, converts it to a
 * Swing {@link DefaultTableModel}, then loads this data into the {@link PageViewerPanel}
 * associated with the provided {@link SCALedgerPlugin}, and displays the panel in a new window.
 * It handles file selection using a JavaFX {@link FileChooser} and remembers the last directory.
 */
public class ImportFromJsonActionFX implements EventHandler<ActionEvent>
{
	
	/** The owner Stage for dialogs (FileChooser, Alerts). */
	private final Stage owner;
	/** Jackson ObjectMapper for reading JSON data. */
	private final ObjectMapper mapper = new ObjectMapper();
	/** The {@link SCALedgerPlugin} instance this action interacts with, particularly for accessing its {@link PageViewerPanel}. */
	private final SCALedgerPlugin plugin;
	
	/**
     * Constructs a new {@code ImportFromJsonActionFX}.
     *
     * @param owner The owner {@link Stage} for displaying file choosers and alerts.
     *              This is necessary for proper dialog modality and context.
     * @param plugin The {@link SCALedgerPlugin} instance that owns the {@link PageViewerPanel}
     *               where the imported data will be displayed. This provides the context for UI updates.
     * @throws NullPointerException if {@code owner} or {@code plugin} is null (though not explicitly checked currently).
     */
        public ImportFromJsonActionFX(Stage owner, SCALedgerPlugin plugin)
        {
                this.owner = owner;
                this.plugin = Objects.requireNonNull(plugin, "plugin");
        }
	
	/**
	 * {@inheritDoc}
     * <p>
     * Handles the action event triggered to import ledger data from a JSON file.
     * This method performs the following steps:
     * <ol>
     *   <li>Displays a {@link FileChooser} to allow the user to select a ".json" file.
     *       The initial directory is based on the last used directory preference.</li>
     *   <li>If a file is selected:
     *     <ul>
     *       <li>Calls {@link #importJsonToTableModel(File)} to parse the JSON file into a {@link DefaultTableModel}.</li>
     *       <li>Retrieves the {@link PageViewerPanel} from the associated {@link SCALedgerPlugin}.</li>
     *       <li>If the panel is available, loads the imported data into it using {@link PageViewerPanel#loadData(DefaultTableModel)}.</li>
     *       <li>Displays the {@code PageViewerPanel} in a new window.</li>
     *       <li>Shows an information alert indicating successful data loading.</li>
     *       <li>Updates the last used directory preference.</li>
     *     </ul>
     *   </li>
     *   <li>If file selection is cancelled, or if any error occurs during import or display,
     *       an error alert is shown (exceptions are printed to standard error).</li>
     * </ol>
     * </p>
     * @param e The {@link ActionEvent} that triggered this handler.
	 */
	@Override public void handle(ActionEvent e)
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Import JSON Ledger");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
		
		String lastDir = PreferencesManager.getLastDirectory();
		
		if (lastDir != null)
		{
			File dir = new File(lastDir);
			if (dir.exists())
			{
				chooser.setInitialDirectory(dir);
			}
		}
		
		File file = chooser.showOpenDialog(this.owner);
		if (file == null)
		{
			return; // cancelled
		}
			
		try
		{
			DefaultTableModel imported = importJsonToTableModel(file);
			
            PageViewerPanel pvp = this.plugin.getPageViewerPanel();
            if (pvp == null) {
                new Alert(Alert.AlertType.ERROR, "PageViewerPanel not initialized in plugin.").showAndWait();
                return;
            }
            
            pvp.loadData(imported);
            pvp.displayInWindow(null, "JSON Import Viewer - " + file.getName());
			
			new Alert(Alert.AlertType.INFORMATION, "Data loaded into viewer from JSON.") // Updated message
				.showAndWait();
			PreferencesManager.setLastDirectory(file.getParent());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			new Alert(Alert.AlertType.ERROR,
				"Failed to import from JSON.\n" + ex.getMessage()).showAndWait();
		}
		
	}
	
	/**
	 * Converts a JSON array of objects into a {@link DefaultTableModel}. The JSON file must look
	 * like:
	 * <pre>
	 * [
	 *   { "date": "2025-01-01", "account": "Donations", "amount": 150 },
	 *   { "date": "2025-01-02", "account": "Supplies Expense", "amount": -45.32 }
	 * ]
	 * </pre>
	 * The union of all object keys forms the column set (in encounter order). Missing values are
	 * filled with {@code null}.
	 */
	private DefaultTableModel importJsonToTableModel(File jsonFile) throws Exception
	{
		// 1. Load JSON as a list of maps (preserves insertion order per row)
		List<Map<String, Object>> rows = this.mapper.readValue(jsonFile,
			new TypeReference<List<Map<String, Object>>>()
			{
			});
		
		// 2. Collect column names in a LinkedHashSet to preserve insertion order across
		// rows
		Set<String> columns = new LinkedHashSet<>();
		for (Map<String, Object> row : rows)
		{
			columns.addAll(row.keySet());
		}
		
		// 3. Build DefaultTableModel with those columns
		Vector<String> columnVector = new Vector<>(columns);
		DefaultTableModel model = new DefaultTableModel(columnVector, 0);
		
		// 4. Populate rows
		for (Map<String, Object> row : rows)
		{
			Vector<Object> vec = new Vector<>(columns.size());
			for (String col : columns)
			{
				vec.add(row.get(col));
			}
			model.addRow(vec);
		}
		
		return model;
	}
	
}
