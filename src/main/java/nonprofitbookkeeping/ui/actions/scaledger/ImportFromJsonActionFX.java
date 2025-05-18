
package nonprofitbookkeeping.ui.actions.scaledger;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import nonprofitbookkeeping.preferences.PreferencesManager;

/**
 * JavaFX version of the Swing {@code ImportFromJsonAction}. Opens a JSON file, converts it to a
 * Swing {@link DefaultTableModel}, then drops that data into the current {@link PageViewer}
 * table model so the ledger grid updates live.
 */
public class ImportFromJsonActionFX implements EventHandler<ActionEvent>
{
	
	private final Stage owner;
	private final ObjectMapper mapper = new ObjectMapper();
	
	public ImportFromJsonActionFX(Stage owner)
	{
		this.owner = owner;
	}
	
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
				chooser.setInitialDirectory(dir);
		}
		
		File file = chooser.showOpenDialog(this.owner);
		if (file == null)
			return; // cancelled
			
		try
		{
			DefaultTableModel imported = importJsonToTableModel(file);
			
			// Push data into the existing PageViewer model
			Vector<String> cols = new Vector<>();
			for (int i = 0; i < imported.getColumnCount(); i++)
				cols.add(imported.getColumnName(i));
			PageViewer.getTableModel().setDataVector(imported.getDataVector(), cols);
			
			new Alert(Alert.AlertType.INFORMATION, "Ledger imported from JSON.")
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
			columns.addAll(row.keySet());
		
		// 3. Build DefaultTableModel with those columns
		Vector<String> columnVector = new Vector<>(columns);
		DefaultTableModel model = new DefaultTableModel(columnVector, 0);
		
		// 4. Populate rows
		for (Map<String, Object> row : rows)
		{
			Vector<Object> vec = new Vector<>(columns.size());
			for (String col : columns)
				vec.add(row.get(col));
			model.addRow(vec);
		}
		
		return model;
	}
	
}
