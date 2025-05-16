
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.BeanShell;
import nonprofitbookkeeping.model.CurrentInputFile;
import nonprofitbookkeeping.preferences.PreferencesManager;
import nonprofitbookkeeping.ui.NonprofitBookkeepingFX;

/**
 * JavaFX replacement for the Swing {@code LoadXlsmTableAction}. Opens an .xlsm file, converts the
 * first sheet into a {@link TableView}, and shows that table in a new window.  The selected file is
 * stored in {@link NonprofitBookkeepingFX#currentInputFile} and beans are loaded through
 * {@link SCALedgerDataLoader} just like the original code.
 */
public class LoadXlsmTableActionFX implements EventHandler<ActionEvent>
{
	
	private final Stage owner;
	
	public LoadXlsmTableActionFX(Stage owner)
	{
		this.owner = owner;
	}
	
	@Override public void handle(ActionEvent e)
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select XLSM File");
		chooser.getExtensionFilters().add(
			new FileChooser.ExtensionFilter("Excel Macro-Enabled", "*.xlsm"));
		
		// Default directory
		String lastDir = PreferencesManager.getLastDirectory();
		if (lastDir == null)
			lastDir = System.getProperty("user.home");
		File dir = new File(lastDir);
		if (dir.exists())
			chooser.setInitialDirectory(dir);
		
		File file = chooser.showOpenDialog(this.owner);
		if (file == null)
			return; // user cancelled
			
		try
		{
			// Existing helper converts Excel sheet to Swing TableModel
			javax.swing.table.DefaultTableModel model =
				XlsmTableViewer.readXlsmToTableModel(file, 0);
			
			TableView<Map<String, Object>> table = createFxTable(model);
			
			// Persist globals & prefs
			CurrentInputFile.setCurrentInputFile(file);
			PreferencesManager.setLastDirectory(file.getParent());
			BeanShell.setBeans(SCALedgerDataLoader.loadData(new File("jxlsMapping.xml"), file));
			
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
