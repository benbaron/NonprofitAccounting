package org.nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.importer.sclx.AccountImportMode;
import nonprofitbookkeeping.importer.sclx.SclxImportOptions;
import nonprofitbookkeeping.model.CurrentCompany;

import java.nio.file.Path;
import java.util.Map;

/** Native alternate workspace for import/export operations. */
public class AlternateImportExportPanel implements AppPanel
{
    private final VBox root = new VBox(12);
    private final AlternateImportExportService service;
    private final TextArea status = new TextArea();

    public AlternateImportExportPanel()
    {
        this(new AlternateImportExportService());
    }

    AlternateImportExportPanel(AlternateImportExportService service)
    {
        this.service = service;
        build();
    }

    @Override
    public String title()
    {
        return "Import/Export";
    }

    @Override
    public Node root()
    {
        return this.root;
    }

    private void build()
    {
        Label title = new Label("Import/Export");
        title.getStyleClass().add("alternate-panel-title");
        Label subtitle = new Label("Company-level import/export is separated from database-level import/export and backup operations.");
        subtitle.setWrapText(true);
        subtitle.getStyleClass().add("alternate-panel-subtitle");
        this.status.setEditable(false);
        this.status.setPrefRowCount(8);
        VBox.setVgrow(this.status, Priority.ALWAYS);
        this.root.setPadding(new Insets(12));
        this.root.getStyleClass().add("alternate-content-card");
        this.root.getChildren().setAll(title, subtitle,
            databaseSection(), coaSection(), sclxSection(), supportedFormatsSection(), new Separator(), new Label("Result"), this.status);
    }

    private GridPane databaseSection()
    {
        GridPane grid = section("Database import/export (database-level)",
            "Use Database Administration for full database import, export/backup, validate, repair, and schema migration. These operations are not mixed with company-level imports.");
        addReadOnlyRow(grid, 2, "Import modes", "Create/import into a new database/company; validate database before opening where supported.");
        addReadOnlyRow(grid, 3, "Export modes", "Full database export/backup; active database backup with explicit source and target paths.");
        Button open = new Button("Open Database Administration");
        open.setOnAction(e -> this.status.setText("Open the Database Administration workspace for database-level import/export and backup."));
        grid.add(open, 1, 4);
        return grid;
    }

    private GridPane coaSection()
    {
        GridPane grid = section("Chart of Accounts import/export (company-level)",
            "COA import/export is a company-level workflow for supported XLSX chart-of-accounts files.");
        addReadOnlyRow(grid, 2, "Import modes", "Preview only; validate only; commit to active company after blocking errors are resolved.");
        addReadOnlyRow(grid, 3, "Duplicate-code policy", "Duplicate account codes are blocking errors unless an explicit update mapping is selected by a future COA service.");
        addReadOnlyRow(grid, 4, "Deactivation policy", "Missing import rows do not deactivate existing accounts unless the user explicitly selects a deactivation policy.");
        addReadOnlyRow(grid, 5, "Transaction history policy", "Existing accounts with transaction history may not have code/type changes committed without service validation.");
        addReadOnlyRow(grid, 6, "Export modes", "Chart of accounts export for the active company.");
        return grid;
    }

    private GridPane sclxSection()
    {
        GridPane grid = section("SCLX import (company-level)",
            "Uses the existing SclxImportService, SclxImportOptions, SclxImportResult, and NonprofitBookkeepingSclxImportTarget.");
        TextField file = new TextField();
        file.setPromptText("/path/to/file.sclx.json");
        TextField cash = new TextField();
        cash.setPromptText("Cash account reference for single-sided transactions");
        ComboBox<String> mode = new ComboBox<>();
        mode.getItems().addAll("Preview only", "Validate only", "Commit to active company", "Create/import into new database/company");
        mode.setValue("Preview only");
        TextField exportFile = new TextField();
        exportFile.setPromptText("/path/to/export.sclx.json");
        TextField exportRunId = new TextField();
        exportRunId.setPromptText("Optional import run id for raw round-trip fidelity");
        Button run = new Button("Run SCLX import");
        run.setOnAction(e -> runSclx(file.getText(), cash.getText(), mode.getValue()));
        Button export = new Button("Export active company to SCLX");
        export.setOnAction(e -> runSclxExport(exportFile.getText(), exportRunId.getText()));
        grid.addRow(2, new Label("SCLX file"), file);
        grid.addRow(3, new Label("Import mode"), mode);
        grid.addRow(4, new Label("Cash account"), cash);
        grid.add(run, 1, 5);
        grid.addRow(6, new Label("Export file"), exportFile);
        grid.addRow(7, new Label("Import run id"), exportRunId);
        grid.add(export, 1, 8);
        addReadOnlyRow(grid, 9, "Result counts", "Every run reports created, updated, skipped, warnings, and errors. Errors block commit.");
        addReadOnlyRow(grid, 10, "Export mode", "Exports the active company through NonprofitBookkeepingSclxExportService; an optional import run id reuses preserved raw SCLX when available.");
        return grid;
    }

    private GridPane supportedFormatsSection()
    {
        GridPane grid = section("Other supported formats", "Existing spreadsheet, XLSM, JSON, OFX/QFX, and database formats remain routed to their existing services/actions until native service boundaries are added here.");
        addReadOnlyRow(grid, 2, "Spreadsheet/XLSM/JSON", "Supported where existing import/export services already exist; preview/validate/commit/result-summary rules apply.");
        addReadOnlyRow(grid, 3, "Banking imports", "Financial file review queues stay separate from database-level import/export.");
        return grid;
    }

    private void runSclx(String file, String cashAccount, String mode)
    {
        if (file == null || file.isBlank())
        {
            showResult(this.service.blockingError("Select an SCLX file before import."));
            return;
        }
        if (mode != null && mode.startsWith("Commit") && (!Database.isInitialized() || !CurrentCompany.isOpen()))
        {
            showResult(this.service.blockingError("Open a database and active company before committing an SCLX import."));
            return;
        }
        if (mode != null && (mode.startsWith("Preview") || mode.startsWith("Validate") || mode.startsWith("Create")))
        {
            showResult(new ImportExportOperationResult(0, 0, 0,
                java.util.List.of(mode + " is defined but awaits a non-mutating SCLX target/new-company service boundary."), java.util.List.of()));
            return;
        }
        SclxImportOptions options = new SclxImportOptions(true, true, true, true,
            blankToNull(cashAccount), null, AccountImportMode.AS_IS, Map.of());
        try
        {
            showResult(this.service.importSclx(Path.of(file.trim()), options));
        }
        catch (RuntimeException ex)
        {
            showResult(this.service.blockingError(ex.getMessage()));
        }
    }


    private void runSclxExport(String file, String importRunId)
    {
        if (!Database.isInitialized() || !CurrentCompany.isOpen())
        {
            showResult(this.service.blockingError("Open a database and active company before exporting SCLX."));
            return;
        }
        if (file == null || file.isBlank())
        {
            showResult(this.service.blockingError("Select a destination file before exporting SCLX."));
            return;
        }
        showResult(this.service.exportSclx(Path.of(file.trim()), blankToNull(importRunId)));
    }

    private void showResult(ImportExportOperationResult result)
    {
        String details = result.describeCounts();
        if (result.hasBlockingErrors()) details += "\nBlocking errors:\n- " + String.join("\n- ", result.errors());
        if (!result.warnings().isEmpty()) details += "\nWarnings:\n- " + String.join("\n- ", result.warnings());
        this.status.setText(details);
    }

    private GridPane section(String headingText, String description)
    {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        grid.getStyleClass().add("alternate-section");
        Label heading = new Label(headingText);
        heading.getStyleClass().add("alternate-section-title");
        Label desc = new Label(description);
        desc.setWrapText(true);
        grid.add(heading, 0, 0, 2, 1);
        grid.add(desc, 0, 1, 2, 1);
        return grid;
    }

    private static void addReadOnlyRow(GridPane grid, int row, String label, String value)
    {
        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);
        grid.addRow(row, new Label(label), valueLabel);
    }

    private static String blankToNull(String value)
    {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
