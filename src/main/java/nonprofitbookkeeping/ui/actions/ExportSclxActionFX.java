package nonprofitbookkeeping.ui.actions;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.importer.sclx.NonprofitBookkeepingSclxExportService;
import nonprofitbookkeeping.model.CurrentCompany;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** JavaFX action that exports the active company to SCLX JSON. */
public class ExportSclxActionFX implements EventHandler<ActionEvent>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportSclxActionFX.class);

    private final Stage ownerStage;
    private final NonprofitBookkeepingSclxExportService exportService;

    public ExportSclxActionFX(Stage ownerStage)
    {
        this(ownerStage, new NonprofitBookkeepingSclxExportService());
    }

    ExportSclxActionFX(Stage ownerStage, NonprofitBookkeepingSclxExportService exportService)
    {
        if (ownerStage == null)
        {
            throw new IllegalArgumentException("Owner stage cannot be null.");
        }
        this.ownerStage = ownerStage;
        this.exportService = exportService;
    }

    @Override
    public void handle(ActionEvent event)
    {
        if (!isFxReady())
        {
            return;
        }
        if (!CurrentCompany.isOpen())
        {
            showAlert(AlertType.ERROR, "Export Unavailable", "Open a company before exporting SCLX.");
            return;
        }

        TextInputDialog runIdDialog = new TextInputDialog();
        runIdDialog.initOwner(this.ownerStage);
        runIdDialog.setTitle("SCLX Export");
        runIdDialog.setHeaderText("Optional import run id");
        runIdDialog.setContentText("Import run id (leave blank for active company export):");
        Optional<String> runId = runIdDialog.showAndWait();
        if (runId.isEmpty())
        {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export SCLX");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SCLX JSON files (*.sclx.json)", "*.sclx.json", "*.json"));
        fileChooser.setInitialFileName("company.sclx.json");
        File chosen = fileChooser.showSaveDialog(this.ownerStage);
        if (chosen == null)
        {
            return;
        }

        try
        {
            String json = this.exportService.exportJson(blankToNull(runId.get()));
            Files.writeString(chosen.toPath(), json, StandardCharsets.UTF_8);
            showAlert(AlertType.INFORMATION, "Export Complete", "Exported SCLX to:\n" + chosen.toPath().toAbsolutePath());
        }
        catch (Exception ex)
        {
            LOGGER.error("SCLX export failed", ex);
            showAlert(AlertType.ERROR, "Export Failed", ex.getMessage());
        }
    }

    private boolean isFxReady()
    {
        try
        {
            return Platform.isFxApplicationThread();
        }
        catch (IllegalStateException ex)
        {
            LOGGER.debug("JavaFX runtime not initialised. Skipping SCLX export dialog.", ex);
            return false;
        }
    }

    private void showAlert(AlertType type, String title, String message)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(this.ownerStage);
        alert.showAndWait();
    }

    private static String blankToNull(String value)
    {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
