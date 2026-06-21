package org.nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.nio.file.Path;

/** Native alternate workspace for database-level administration. */
public class AlternateDatabaseAdminPanel implements AppPanel
{
    private final VBox root = new VBox(12);
    private final AlternateDatabaseAdminService service;
    private final TextArea status = new TextArea();
    private final ProgressIndicator progress = new ProgressIndicator();

    public AlternateDatabaseAdminPanel(UiServiceProvider services)
    {
        this(new AlternateDatabaseAdminService(services.databaseAdministration(), services.sessionContext()));
    }

    AlternateDatabaseAdminPanel(AlternateDatabaseAdminService service)
    {
        this.service = service;
        build();
    }

    @Override
    public String title()
    {
        return "Database Administration";
    }

    @Override
    public Node root()
    {
        return root;
    }

    private void build()
    {
        Label title = new Label("Database Administration");
        title.getStyleClass().add("alternate-panel-title");
        Label subtitle = new Label("Open, import, export/backup, validate, repair/recover, and migrate H2 database files.");
        subtitle.getStyleClass().add("alternate-panel-subtitle");
        progress.setVisible(false);
        progress.setManaged(false);
        status.setEditable(false);
        status.setPrefRowCount(8);
        root.setPadding(new Insets(12));
        root.getStyleClass().add("alternate-content-card");
        root.getChildren().setAll(title, subtitle,
            operationRow("Open Database", "Source database", "", false, "Open", (source, target, backup) -> {
                DatabaseOpenService.OpenResult opened = service.openDatabase(path(source));
                return new AlternateDatabaseAdminService.AdminResult(path(source), null, null, opened.basePath(), opened.successMessage(), opened, java.util.List.of());
            }),
            operationRow("Close Database", "", "", false, "Close", (source, target, backup) -> {
                service.closeDatabase();
                return new AlternateDatabaseAdminService.AdminResult(null, null, null, null, "Database context closed.", null, java.util.List.of());
            }),
            operationRow("Import Database", "Source database", "Target database", false, "Import and Open", (source, target, backup) ->
                service.importDatabase(path(source), path(target), true)),
            operationRow("Export / Backup Database", "Source database (blank = active)", "Backup target", false, "Export", (source, target, backup) ->
                service.exportDatabase(blankPath(source), path(target), false)),
            operationRow("Validate Database", "Source database", "", false, "Validate", (source, target, backup) ->
                service.validateDatabase(path(source))),
            operationRow("Repair / Recover H2 Database", "Source database", "", true, "Repair", (source, target, backup) ->
                service.repairDatabase(path(source), backup, true)),
            operationRow("Migrate Schema", "Source database", "Optional SQL result path", true, "Migrate", (source, target, backup) ->
                service.migrateSchema(path(source), blankPath(target), backup)),
            new HBox(8, progress), status);
    }

    private GridPane operationRow(String label, String sourcePrompt, String targetPrompt, boolean backupConfirmation,
        String actionLabel, ThrowingOperation operation)
    {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        grid.getStyleClass().add("alternate-section");
        Label heading = new Label(label);
        heading.getStyleClass().add("alternate-section-title");
        TextField source = new TextField();
        source.setPromptText(sourcePrompt);
        TextField target = new TextField();
        target.setPromptText(targetPrompt);
        CheckBox confirmBackup = new CheckBox("I have created or selected an explicit backup before committing this exclusive operation.");
        confirmBackup.setVisible(backupConfirmation);
        confirmBackup.setManaged(backupConfirmation);
        Button run = new Button(actionLabel);
        run.setOnAction(e -> runAsync(label, () -> operation.run(source.getText(), target.getText(), confirmBackup.isSelected())));
        grid.add(heading, 0, 0, 3, 1);
        grid.add(new Label(sourcePrompt.isBlank() ? "Operation" : sourcePrompt), 0, 1);
        grid.add(source, 1, 1);
        grid.add(new Label(targetPrompt), 0, 2);
        grid.add(target, 1, 2);
        grid.add(confirmBackup, 1, 3);
        grid.add(run, 2, 1);
        return grid;
    }

    private void runAsync(String label, ThrowingSupplier supplier)
    {
        setBusy(label + " started...");
        UiAsync.run("alternate-db-admin", () -> {
            try { return supplier.get(); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        }, result -> {
            setIdle(describe(result));
        }, ex -> setIdle(label + " failed: " + rootCause(ex).getMessage()));
    }

    private void setBusy(String message)
    {
        progress.setVisible(true);
        progress.setManaged(true);
        status.setText(message);
    }

    private void setIdle(String message)
    {
        progress.setVisible(false);
        progress.setManaged(false);
        status.setText(message);
    }

    private String describe(AlternateDatabaseAdminService.AdminResult result)
    {
        return result.message() + "\nSource: " + value(result.sourcePath()) + "\nTarget: " + value(result.targetPath())
            + "\nBackup: " + value(result.backupPath()) + "\nResult: " + value(result.resultPath());
    }

    private static String value(Path path) { return path == null ? "(none)" : path.toAbsolutePath().toString(); }
    private static Path path(String value) { if (value == null || value.isBlank()) throw new IllegalArgumentException("Path is required."); return Path.of(value.trim()); }
    private static Path blankPath(String value) { return value == null || value.isBlank() ? null : Path.of(value.trim()); }
    private static Throwable rootCause(Throwable throwable) { return throwable.getCause() == null ? throwable : rootCause(throwable.getCause()); }

    @FunctionalInterface private interface ThrowingOperation { AlternateDatabaseAdminService.AdminResult run(String source, String target, boolean backup) throws Exception; }
    @FunctionalInterface private interface ThrowingSupplier { AlternateDatabaseAdminService.AdminResult get() throws Exception; }
}
