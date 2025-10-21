package nonprofitbookkeeping.ui.actions.scaledger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.DefaultTableModel;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.impex.ExcelLedgerRow;
import nonprofitbookkeeping.plugins.scaledger.SCALedgerPlugin;
import nonprofitbookkeeping.plugins.scaledger.ui.PageViewerPanel;
import nonprofitbookkeeping.preferences.PreferencesManager;
import nonprofitbookkeeping.service.ExcelLedgerImportService;

/**
 * JavaFX action that imports an SCA ledger stored in an Excel workbook
 * (typically <code>.xlsx</code>) and displays the parsed rows inside the
 * plugin's {@link PageViewerPanel}.
 */
public class ImportFromExcelActionFX implements EventHandler<ActionEvent>
{
        private static final Logger LOGGER = Logger.getLogger(ImportFromExcelActionFX.class.getName());

        private final Stage owner;
        private final SCALedgerPlugin plugin;

        /**
         * Creates a new importer bound to the provided JavaFX stage and plugin.
         *
         * @param owner  the owner window used for file pickers and alerts
         * @param plugin the SCA plugin that exposes the shared {@link PageViewerPanel}
         */
        public ImportFromExcelActionFX(Stage owner, SCALedgerPlugin plugin)
        {
                this.owner = owner;
                this.plugin = Objects.requireNonNull(plugin, "plugin");
        }

        @Override public void handle(ActionEvent event)
        {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Import SCA Excel Ledger");
                FileChooser.ExtensionFilter xlsxFilter =
                        new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx");
                chooser.getExtensionFilters().add(xlsxFilter);
                chooser.setSelectedExtensionFilter(xlsxFilter);

                String lastDir = PreferencesManager.getLastDirectory();

                if (lastDir != null)
                {
                        File dir = new File(lastDir);
                        if (dir.isDirectory())
                        {
                                chooser.setInitialDirectory(dir);
                        }
                }

                File selected = chooser.showOpenDialog(this.owner);

                if (selected == null)
                {
                        return; // cancelled
                }

                try
                {
                        List<ExcelLedgerRow> rows = ExcelLedgerImportService.importSpreadsheet(selected);
                        DefaultTableModel model = buildTableModel(rows);

                        PageViewerPanel viewerPanel = this.plugin.getPageViewerPanel();

                        if (viewerPanel == null)
                        {
                                new Alert(AlertType.ERROR, "Page viewer is not available.").showAndWait();
                                return;
                        }

                        viewerPanel.loadData(model);
                        viewerPanel.displayInWindow(null, "Excel Import Viewer - " + selected.getName());

                        this.plugin.setCurrentScaFile(selected);
                        PreferencesManager.setLastDirectory(selected.getParent());

                        new Alert(AlertType.INFORMATION, "Data loaded into viewer from Excel.").showAndWait();
                }
                catch (IOException ex)
                {
                        LOGGER.log(Level.WARNING, "Failed to import Excel ledger", ex);
                        new Alert(AlertType.ERROR,
                                "Failed to import from Excel.\n" + ex.getMessage()).showAndWait();
                }
        }

        /**
         * Converts the parsed ledger rows into a {@link DefaultTableModel} that mirrors the
         * spreadsheet's structure. Allocation columns are generated dynamically depending on the
         * maximum number of allocations encountered across all rows.
         *
         * @param rows parsed ledger rows; can be {@code null}
         * @return a non-editable table model ready for {@link PageViewerPanel#loadData(DefaultTableModel)}
         */
        static DefaultTableModel buildTableModel(List<ExcelLedgerRow> rows)
        {
                Vector<String> columns = new Vector<>(Arrays.asList(
                        "Balance",
                        "Date",
                        "Check Number",
                        "Cleared Bank",
                        "To/From",
                        "Memo/Notes",
                        "Budget Tracking",
                        "Net Total"));

                int maxAllocations = 0;

                if (rows != null)
                {
                        for (ExcelLedgerRow row : rows)
                        {
                                if (row == null || row.getAllocations() == null)
                                {
                                        continue;
                                }
                                maxAllocations = Math.max(maxAllocations, row.getAllocations().size());
                        }
                }

                for (int i = 0; i < maxAllocations; i++)
                {
                        int displayIndex = i + 1;
                        columns.add("Allocation " + displayIndex + " Amount");
                        columns.add("Allocation " + displayIndex + " Asset/Liability");
                        columns.add("Allocation " + displayIndex + " Income");
                        columns.add("Allocation " + displayIndex + " Expense");
                        columns.add("Allocation " + displayIndex + " Fund");
                }

                Vector<Vector<Object>> data = new Vector<>();

                if (rows != null)
                {
                        for (ExcelLedgerRow row : rows)
                        {
                                if (row == null)
                                {
                                        continue;
                                }

                                Vector<Object> values = new Vector<>();
                                values.add(row.getBalance());
                                values.add(row.getDate());
                                values.add(row.getCheckNumber());
                                values.add(row.getClearBank());
                                values.add(row.getToFrom());
                                values.add(row.getMemoNotes());
                                values.add(row.getBudgetTracking());
                                values.add(row.getNetTotal());

                                List<ExcelLedgerRow.Allocation> allocations =
                                        (row.getAllocations() == null) ? Collections.emptyList() : row.getAllocations();

                                for (int i = 0; i < maxAllocations; i++)
                                {
                                        if (i < allocations.size() && allocations.get(i) != null)
                                        {
                                                ExcelLedgerRow.Allocation alloc = allocations.get(i);
                                                values.add(alloc.getAmount());
                                                values.add(alloc.getAssetLiabilityAccount());
                                                values.add(alloc.getIncomeCategory());
                                                values.add(alloc.getExpenseCategory());
                                                values.add(alloc.getFund());
                                        }
                                        else
                                        {
                                                values.add(null);
                                                values.add(null);
                                                values.add(null);
                                                values.add(null);
                                                values.add(null);
                                        }
                                }

                                data.add(values);
                        }
                }

                return new DefaultTableModel(data, columns)
                {
                        @Override public boolean isCellEditable(int row, int column)
                        {
                                return false;
                        }
                };
        }
}
