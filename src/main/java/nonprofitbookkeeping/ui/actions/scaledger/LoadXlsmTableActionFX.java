
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
import nonprofitbookkeeping.plugins.scaledger.SCALedgerPlugin; // Added
import nonprofitbookkeeping.preferences.PreferencesManager;
import nonprofitbookkeeping.ui.helpers.NpbkFileChooserFX;

/**
 * JavaFX action handler for loading data from an Excel XLSM file.
 * This action prompts the user to select an .xlsm file using a {@link FileChooser}.
 * Upon selection, it reads the first sheet of the workbook into a Swing {@link javax.swing.table.DefaultTableModel}
 * (via {@link XlsmTableViewerFX#readXlsmToTableModel(File, int)}), then converts this model
 * into a JavaFX {@link TableView} which is displayed in a new window.
 * <p>
 * The selected file's path is stored via the associated {@link SCALedgerPlugin},
 * preferences are updated, and data is loaded using {@link SCALedgerDataLoader}
 * into the plugin's bean map (typically using "jxlsMapping.xml").
 * </p>
 */
public class LoadXlsmTableActionFX implements EventHandler<ActionEvent>
{
	
	/** The owner {@link Stage} for displaying dialogs like the FileChooser and new windows. */
	private final Stage owner;
	/** The {@link SCALedgerPlugin} instance used for managing state related to the loaded SCA ledger file and its data. */
	private final SCALedgerPlugin plugin;
	
	/**
     * Constructs a new {@code LoadXlsmTableActionFX}.
     *
     * @param owner The owner {@link Stage} for any dialogs or new windows created by this action.
     *              Must not be null.
     * @param plugin The {@link SCALedgerPlugin} instance that will manage the state of the
     *               loaded XLSM file and its processed data. Must not be null.
     * @throws NullPointerException if {@code owner} or {@code plugin} is null (though not explicitly checked here,
     *                              they are later used and would cause NPE if null).
     */
	public LoadXlsmTableActionFX(Stage owner, SCALedgerPlugin plugin)
	{
		this.owner = owner;
		this.plugin = plugin;
	}
	
	/**
     * {@inheritDoc}
     * <p>
     * Handles the action event, typically from a menu item, to load and display an XLSM file's first sheet.
     * The process includes:
     * <ol>
     *   <li>Presenting a {@link FileChooser} to the user to select an ".xlsm" file.
     *       The initial directory is based on preferences.</li>
     *   <li>If a file is selected:
     *     <ul>
     *       <li>The first sheet of the XLSM file is read into a Swing {@code DefaultTableModel}.</li>
     *       <li>This Swing model is converted to a JavaFX {@code TableView<Map<String, Object>>}.</li>
     *       <li>The selected file is set as the current SCA file in the associated {@link SCALedgerPlugin}.</li>
     *       <li>The last directory preference is updated.</li>
     *       <li>Data is loaded from the XLSM file into the plugin's beans using {@link SCALedgerDataLoader}
     *           (with a hardcoded "jxlsMapping.xml" file).</li>
     *       <li>The JavaFX {@code TableView} is displayed in a new {@link Stage}.</li>
     *     </ul>
     *   </li>
     *   <li>If file selection is cancelled, or if any {@link Exception} occurs during processing
     *       (e.g., file reading, data loading), an error alert is shown and the stack trace is printed.</li>
     * </ol>
     * </p>
     * @param e The {@link ActionEvent} that triggered this handler.
     */
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
	 * Converts a Swing {@link javax.swing.table.DefaultTableModel} into a JavaFX {@link TableView}.
	 * Each row in the resulting {@code TableView} is represented as a {@code Map<String, Object>},
	 * where keys are column names (obtained from the {@code DefaultTableModel}) and values are the
	 * corresponding cell data.
	 * <p>
	 * Columns for the {@code TableView} are created dynamically based on the column names from the input model.
	 * Cell values are populated using a lambda expression for the cell value factory, which looks up
	 * values in the row map by column name. The table view is configured with a constrained column resize policy.
	 * </p>
	 *
	 * @param model The Swing {@link javax.swing.table.DefaultTableModel} to convert. Must not be null.
	 * @return A JavaFX {@link TableView} populated with data from the input model.
	 * @deprecated The {@code @SuppressWarnings("deprecation")} annotation in the original code suggests
	 *             potential use of deprecated APIs, though not immediately obvious from the current implementation.
	 *             Consider reviewing if any underlying Swing/JavaFX interop parts are subject to deprecation.
	 *             The method itself is functional for converting table data.
	 */
	@Deprecated
	private static TableView<Map<String, Object>> createFxTable(javax.swing.table.DefaultTableModel model)
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
