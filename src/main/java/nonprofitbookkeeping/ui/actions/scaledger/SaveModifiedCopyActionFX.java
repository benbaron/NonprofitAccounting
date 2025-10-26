package nonprofitbookkeeping.ui.actions.scaledger;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.plugins.scaledger.SCALedgerPlugin;
import nonprofitbookkeeping.plugins.scaledger.ui.PageViewerPanel;
import nonprofitbookkeeping.preferences.PreferencesManager;

/**
 * JavaFX equivalent of the Swing {@code SaveModifiedCopyAction}. 
 * Saves the edited XLSM workbook to a user-chosen location.  Relies on
 * {@link ExcelDataWriter#writeModifiedCopy(File, File, String, javax.swing.table.DefaultTableModel)}
 * for the actual export, using data from the plugin's {@link PageViewerPanel}.
 */
public class SaveModifiedCopyActionFX implements EventHandler<ActionEvent>
{

        private static final String DEFAULT_SHEET_NAME = "Sheet1";

        /** The owner {@link Stage} for displaying dialogs like the FileChooser. */
        private final Stage owner;
        /**
         * The {@link SCALedgerPlugin} instance from which to get the current SCA file (input)
         * and the {@link PageViewerPanel} containing the data model to save.
         */
        private final SCALedgerPlugin plugin;

        public SaveModifiedCopyActionFX(Stage owner, SCALedgerPlugin plugin)
        {
                this.owner = owner;
                this.plugin = plugin;
        }

        @Override public void handle(ActionEvent e)
        {
                if (this.plugin == null)
                {
                        showAlert(Alert.AlertType.ERROR, "Plugin not initialized for SaveModifiedCopyActionFX.");
                        return;
                }

                File input = this.plugin.getCurrentScaFile();
                if (input == null)
                {
                        showAlert(Alert.AlertType.ERROR, "No input file loaded or ledger selected.");
                        return;
                }

                File output = chooseDestinationFile(input);
                if (output == null)
                {
                        return;
                }

                if (!validateOutputPath(output))
                {
                        return;
                }

                try
                {
                        PageViewerPanel panel = this.plugin.getPageViewerPanel();
                        if (panel == null)
                        {
                                showAlert(Alert.AlertType.ERROR, "Page viewer not initialized.");
                                return;
                        }

                        javax.swing.table.DefaultTableModel model = panel.getTableModel();
                        if (model == null)
                        {
                                showAlert(Alert.AlertType.ERROR, "No table data available to save.");
                                return;
                        }

                        ExcelDataWriter.writeModifiedCopy(input, output, DEFAULT_SHEET_NAME, model);
                        PreferencesManager.setLastDirectory(output.getParent());
                        showAlert(Alert.AlertType.INFORMATION, "Workbook saved successfully.");
                }
                catch (IllegalArgumentException iae)
                {
                        showAlert(Alert.AlertType.ERROR, iae.getMessage());
                }
                catch (IOException io)
                {
                        showAlert(Alert.AlertType.ERROR, "I/O error saving workbook:\n" + io.getMessage());
                }
                catch (Exception ex)
                {
                        ex.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Failed to save workbook.\n" + ex.getMessage());
                }
        }

        private File chooseDestinationFile(File input)
        {
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

                return chooser.showSaveDialog(this.owner);
        }

        private boolean validateOutputPath(File output)
        {
                if (output.isDirectory())
                {
                        showAlert(Alert.AlertType.ERROR, "Please specify a file name, not a directory.");
                        return false;
                }

                File parent = output.getParentFile();
                if (parent != null && (!parent.exists() || !parent.canWrite()))
                {
                        showAlert(Alert.AlertType.ERROR, "Cannot write to the selected location.");
                        return false;
                }
                return true;
        }

        private static void showAlert(Alert.AlertType type, String message)
        {
                new Alert(type, message).showAndWait();
        }
}
