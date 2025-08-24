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
import nonprofitbookkeeping.plugins.scaledger.SCALedgerPlugin;
import nonprofitbookkeeping.preferences.PreferencesManager;

/**
 * JavaFX action handler for loading data from an Excel XLSM file.
 * Prompts for an .xlsm, previews first sheet in a TableView, and loads data into the plugin.
 */
public class LoadXlsmTableActionFX implements EventHandler<ActionEvent>
{
    /** Owner stage for dialogs/windows. */
    private final Stage owner;
    /** Plugin instance to manage SCA ledger state. */
    private final SCALedgerPlugin plugin;

    public LoadXlsmTableActionFX(Stage owner, SCALedgerPlugin plugin)
    {
        this.owner = owner;
        this.plugin = plugin;
    }

    @Override
    public void handle(ActionEvent e)
    {
        // 1) Let user pick an .xlsm (All files available as fallback)
        File file = showOpenXlsmDialog(owner);
        if (file == null) {
            // user cancelled
            return;
        }

        try
        {
            // 2) Convert first sheet to Swing model, then to JavaFX TableView
            javax.swing.table.DefaultTableModel model =
                XlsmTableViewerFX.readXlsmToTableModel(file, 0);

            TableView<Map<String, Object>> table = createFxTable(model);

            // 3) Persist globals & prefs, then load data via plugin
            if (this.plugin == null) {
                new Alert(AlertType.ERROR, "Plugin not initialized for LoadXlsmTableActionFX.").showAndWait();
                return;
            }
            this.plugin.setCurrentScaFile(file);
            PreferencesManager.setLastDirectory(file.getParent());
            this.plugin.setScaBeans(SCALedgerDataLoader.loadData(new File("jxlsMapping.xml"), file));

            // 4) Show viewer window
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
     * Presents a JavaFX FileChooser for .xlsm files,
     * restores initial directory from preferences, and stores it after selection.
     * @return selected file, or null if user cancelled.
     */
    private static File showOpenXlsmDialog(Stage owner)
    {
        FileChooser ch = new FileChooser();
        ch.getExtensionFilters().setAll(
            new FileChooser.ExtensionFilter("Excel Macro-Enabled (*.xlsm)", "*.xlsm"),
            new FileChooser.ExtensionFilter("All files", "*.*")
        );

        // Initial directory from preferences or user.home
        String lastDir = PreferencesManager.getLastDirectory();
        if (lastDir == null || lastDir.isBlank()) {
            lastDir = System.getProperty("user.home");
        }
        File initial = new File(lastDir);
        if (initial.exists() && initial.isDirectory()) {
            ch.setInitialDirectory(initial);
        }

        File f = ch.showOpenDialog(owner);
        if (f != null) {
            PreferencesManager.setLastDirectory(f.getParent());
        }
        return f;
    }

    /**
     * Converts a Swing DefaultTableModel into a JavaFX TableView.
     */
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
