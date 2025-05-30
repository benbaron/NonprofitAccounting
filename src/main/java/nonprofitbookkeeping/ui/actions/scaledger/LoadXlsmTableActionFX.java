
package nonprofitbookkeeping.ui.actions.scaledger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import nonprofitbookkeeping.exception.NoFileException;
// import nonprofitbookkeeping.model.BeanShell; // Replaced by plugin
// import nonprofitbookkeeping.model.NonCompanyFile; // Replaced by plugin
import nonprofitbookkeeping.plugins.scaledger.SCALedgerPlugin; // Added
import nonprofitbookkeeping.preferences.PreferencesManager;
// import nonprofitbookkeeping.ui.NonprofitBookkeepingFX; // NonprofitBookkeepingFX.currentFile was an error
import nonprofitbookkeeping.ui.helpers.NpbkFileChooserFX;

/**
 * JavaFX replacement for the Swing {@code LoadXlsmTableAction}. Opens an .xlsm file, converts the
 * first sheet into a {@link TableView}, and shows that table in a new window.  The selected file is
 * stored in {@link NonprofitBookkeepingFX#currentFile} and beans are loaded through
 * {@link SCALedgerDataLoader} just like the original code.
 */
public class LoadXlsmTableActionFX implements EventHandler<ActionEvent>
{
	
	private final Stage owner;
	private final SCALedgerPlugin plugin; // Added
	
	public LoadXlsmTableActionFX(Stage owner, SCALedgerPlugin plugin) // Updated constructor
	{
		this.owner = owner;
		this.plugin = plugin; // Added
	}
	
	@Override public void handle(ActionEvent e)
	{
		
		String title = "Select XLSM File";
		String description = "Excel Macro-Enabled";
		String fileQualifier = "*.xlsm";
		
		File file = null;
		
		try
		{
			file = NpbkFileChooserFX.chooseExisting(title, description, fileQualifier, this.owner);
		}
		catch (NoFileException e1)
		{
			e1.printStackTrace();
		}
			
		try
		{
			// Existing helper converts Excel sheet to Swing TableModel
			javax.swing.table.DefaultTableModel model =
				XlsmTableViewerFX.readXlsmToTableModel(file, 0);
			
			TableView<Map<String, Object>> table = createFxTable(model);
			
			// Persist globals & prefs
            if (this.plugin == null) {
                new Alert(AlertType.ERROR, "Plugin not initialized for LoadXlsmTableActionFX.").showAndWait();
                return;
            }
			this.plugin.setCurrentScaFile(file); // Use plugin
			PreferencesManager.setLastDirectory(file.getParent());
			this.plugin.setScaBeans(SCALedgerDataLoader.loadData(new File("jxlsMapping.xml"), file)); // Use plugin
			
			// Show viewer window
			Stage viewer = new Stage();
			viewer.initOwner(this.owner);
			viewer.setTitle("XLSM Table Viewer – " + file.getName());
			viewer.setScene(new Scene(table, 800, 600));
			viewer.show();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			new Alert(AlertType.ERROR,
				"Error reading XLSM file:\n" + ex.getMessage()).showAndWait();
		}
		
	}

	/**
	 * Convert a Swing {@link javax.swing.table.DefaultTableModel} to a JavaFX {@link TableView}
	 * without using {@code MapValueFactory}.  Each row is a {@code Map<String,Object>} and each
	 * column pulls its value with a simple lambda.
	 */
	@SuppressWarnings("deprecation") private static TableView<Map<String, Object>> createFxTable(javax.swing.table.DefaultTableModel model)
	{
		TableView<Map<String, Object>> tv = new TableView<>();
		
		// Build columns first
		for (int c = 0; c < model.getColumnCount(); c++)
		{
			final String colName = model.getColumnName(c);
			TableColumn<Map<String, Object>, Object> col = new TableColumn<>(colName);
			col.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().get(colName)));
			tv.getColumns().add(col);
		}
		
		// Convert rows → ObservableList<Map>
		ObservableList<Map<String, Object>> rows = FXCollections.observableArrayList();
		
		for (int r = 0; r < model.getRowCount(); r++)
		{
			Map<String, Object> row = new HashMap<>();
			
			for (int c = 0; c < model.getColumnCount(); c++)
			{
				row.put(model.getColumnName(c), model.getValueAt(r, c));
			}
			
			rows.add(row);
		}
		
		tv.setItems(rows);
		tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		return tv;
	}
	
}
