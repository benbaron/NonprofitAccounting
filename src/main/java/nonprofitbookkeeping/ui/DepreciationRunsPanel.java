package nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.service.DepreciationRunLifecycleService;
import nonprofitbookkeeping.service.DepreciationRunProcessingService;
import org.nonprofitbookkeeping.ui.AppPanel;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents the DepreciationRunsPanel component in the nonprofit bookkeeping application.
 */
public class DepreciationRunsPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    private final DepreciationRunLifecycleService lifecycleService;
    private final DepreciationRunProcessingService processingService;
    private final DatePicker periodStartPicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker periodEndPicker = new DatePicker(LocalDate.now());
    private final TextArea notesArea = new TextArea("Created from depreciation panel");
    private final TableView<RunRow> runsTable = new TableView<>();
    private final Label status = new Label("Ready");

    public DepreciationRunsPanel()
    {
        this(new DepreciationRunLifecycleService(), new DepreciationRunProcessingService());
    }

    DepreciationRunsPanel(DepreciationRunLifecycleService lifecycleService,
                          DepreciationRunProcessingService processingService)
    {
        this.lifecycleService = lifecycleService;
        this.processingService = processingService;
        this.root.setPadding(new Insets(16));
        Label title = new Label("Depreciation Runs");
        title.getStyleClass().add("journal-entry-heading");

        this.notesArea.setPrefRowCount(2);
        this.notesArea.setWrapText(true);

        GridPane inputs = new GridPane();
        inputs.setHgap(8);
        inputs.setVgap(6);
        inputs.addRow(0, new Label("Period Start"), this.periodStartPicker);
        inputs.addRow(1, new Label("Period End"), this.periodEndPicker);
        inputs.addRow(2, new Label("Notes"), this.notesArea);

        Button run = new Button("Create Draft Run");
        Button calculate = new Button("Calculate + Mark Calculated");
        Button preview = new Button("Preview Journal");
        Button lock = new Button("Lock Selected");
        Button unlock = new Button("Unlock Selected");
        Button delete = new Button("Delete Selected Run");
        run.setOnAction(evt -> createDraftRun());
        calculate.setOnAction(evt -> calculateSelectedRun());
        preview.setOnAction(evt -> showPreviewHint());
        lock.setOnAction(evt -> lockSelectedRun());
        unlock.setOnAction(evt -> unlockSelectedRun());
        delete.setOnAction(evt -> deleteSelectedRun());
        HBox actions = new HBox(8, run, calculate, lock, unlock, delete, preview);

        this.root.setTop(new VBox(6, title, actions, new Separator()));
        this.root.setCenter(new VBox(8, inputs, new Separator(), configureRunsTable(), this.status));
        refreshRuns();
    }

    @Override public String title() { return "Depreciation Runs"; }
    @Override public Node root() { return this.root; }

    private TableView<RunRow> configureRunsTable()
    {
        this.runsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        this.runsTable.getColumns().add(column("Run ID", "runId"));
        this.runsTable.getColumns().add(column("Start", "periodStart"));
        this.runsTable.getColumns().add(column("End", "periodEnd"));
        this.runsTable.getColumns().add(column("Status", "status"));
        this.runsTable.getColumns().add(column("Locked", "locked"));
        this.runsTable.getColumns().add(column("Last Updated", "lastUpdated"));
        return this.runsTable;
    }

    private TableColumn<RunRow, String> column(String title, String property)
    {
        TableColumn<RunRow, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    private void createDraftRun()
    {
        LocalDate periodStart = this.periodStartPicker.getValue();
        LocalDate periodEnd = this.periodEndPicker.getValue();
        if (periodStart == null || periodEnd == null || periodEnd.isBefore(periodStart))
        {
            showError("Invalid period", "Choose a valid start/end period for the run.");
            return;
        }
        String runId = "ui-run-" + UUID.randomUUID();
        String notes = this.notesArea.getText() == null ? "" : this.notesArea.getText().trim();
        try
        {
            this.lifecycleService.createDraftRun(runId, periodStart, periodEnd, notes.isEmpty() ? "Created from panel" : notes);
            this.runsTable.getItems().add(new RunRow(runId, periodStart.toString(), periodEnd.toString(),
                "DRAFT", "No", nowStamp()));
            this.status.setText("Draft run created: " + runId);
        }
        catch (SQLException | RuntimeException ex)
        {
            showError("Depreciation run failed", ex.getMessage());
        }
    }

    private void calculateSelectedRun()
    {
        RunRow row = this.runsTable.getSelectionModel().getSelectedItem();
        if (row == null)
        {
            showError("No run selected", "Select a run first.");
            return;
        }
        if (!"DRAFT".equals(row.getStatus()))
        {
            showError("Run status not allowed", "Only DRAFT runs can be calculated.");
            return;
        }
        try
        {
            List<DepreciationRunProcessingService.PreviewLine> lines =
                this.processingService.calculateAndMarkCalculated(row.getRunId(), "ui-user");
            row.setStatus("CALCULATED");
            row.setLastUpdated(nowStamp());
            this.runsTable.refresh();
            this.status.setText("Calculated " + lines.size() + " asset(s) for run " + row.getRunId());
        }
        catch (SQLException | RuntimeException ex)
        {
            showError("Could not update run", ex.getMessage());
        }
    }

    private void lockSelectedRun()
    {
        RunRow row = this.runsTable.getSelectionModel().getSelectedItem();
        if (row == null)
        {
            showError("No run selected", "Select a calculated run first.");
            return;
        }
        try
        {
            this.lifecycleService.lockRun(row.getRunId(), "ui-reviewer", "Locked from depreciation panel");
            row.setLocked("Yes");
            row.setStatus("LOCKED");
            row.setLastUpdated(nowStamp());
            this.runsTable.refresh();
            this.status.setText("Run locked: " + row.getRunId());
        }
        catch (SQLException | RuntimeException ex)
        {
            showError("Could not lock run", ex.getMessage());
        }
    }

    private void unlockSelectedRun()
    {
        RunRow row = this.runsTable.getSelectionModel().getSelectedItem();
        if (row == null)
        {
            showError("No run selected", "Select a run to unlock.");
            return;
        }
        try
        {
            this.processingService.unlockRun(row.getRunId(), "ui-reviewer", "Unlocked from depreciation panel");
            row.setLocked("No");
            row.setLastUpdated(nowStamp());
            this.runsTable.refresh();
            this.status.setText("Run unlocked: " + row.getRunId());
        }
        catch (SQLException | RuntimeException ex)
        {
            showError("Could not unlock run", ex.getMessage());
        }
    }

    private void deleteSelectedRun()
    {
        RunRow row = this.runsTable.getSelectionModel().getSelectedItem();
        if (row == null)
        {
            showError("No run selected", "Select a run to delete.");
            return;
        }

        Dialog<DepreciationRunProcessingService.DeleteMode> dialog = new Dialog<>();
        dialog.setTitle("Delete depreciation run");
        dialog.setHeaderText("Choose how linked journal entries should be handled");
        javafx.scene.control.ButtonType reverseBtn = new javafx.scene.control.ButtonType("Reverse linked journals");
        javafx.scene.control.ButtonType deleteBtn = new javafx.scene.control.ButtonType("Delete linked journals");
        dialog.getDialogPane().getButtonTypes().addAll(reverseBtn, deleteBtn, javafx.scene.control.ButtonType.CANCEL);
        dialog.setResultConverter(bt -> {
            if (bt == reverseBtn) return DepreciationRunProcessingService.DeleteMode.REVERSE_LINKED_JOURNALS;
            if (bt == deleteBtn) return DepreciationRunProcessingService.DeleteMode.DELETE_LINKED_JOURNALS;
            return null;
        });
        DepreciationRunProcessingService.DeleteMode mode = dialog.showAndWait().orElse(null);
        if (mode == null)
        {
            return;
        }

        try
        {
            this.processingService.deleteRun(row.getRunId(), mode, "ui-user");
            this.runsTable.getItems().remove(row);
            this.status.setText("Deleted run: " + row.getRunId());
        }
        catch (SQLException | RuntimeException ex)
        {
            showError("Delete failed", ex.getMessage());
        }
    }

    private void showPreviewHint()
    {
        RunRow row = this.runsTable.getSelectionModel().getSelectedItem();
        if (row == null)
        {
            showError("No run selected", "Select a run to preview generated journal details.");
            return;
        }
        try
        {
            List<DepreciationRunProcessingService.PreviewLine> lines = this.processingService.journalPreview(row.getRunId());
            if (lines.isEmpty())
            {
                showInfo("Preview Journal", "No depreciation lines are calculated for this run yet.");
                return;
            }
            List<String> preview = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;
            for (DepreciationRunProcessingService.PreviewLine line : lines)
            {
                total = total.add(line.amount());
                preview.add(line.assetId() + ": Dr " + line.debitAccount() + " / Cr " + line.creditAccount() +
                    " = " + line.amount());
            }
            showInfo("Preview Journal", String.join("\n", preview) + "\nTotal: " + total);
        }
        catch (SQLException ex)
        {
            showError("Preview failed", ex.getMessage());
        }
    }

    private String nowStamp()
    {
        return LocalDate.now().toString();
    }

    private void refreshRuns()
    {
        if (!Database.isInitialized())
        {
            this.status.setText("Database not initialized yet. Open/create a company to load runs.");
            this.runsTable.getItems().clear();
            return;
        }
        try (java.sql.Connection c = Database.get().getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement("""
                 SELECT depreciation_run_id, period_start, period_end, run_status, is_locked, created_at
                 FROM depreciation_run
                 ORDER BY created_at DESC
             """);
             java.sql.ResultSet rs = ps.executeQuery())
        {
            this.runsTable.getItems().clear();
            while (rs.next())
            {
                this.runsTable.getItems().add(new RunRow(
                    rs.getString(1),
                    rs.getDate(2) == null ? "" : rs.getDate(2).toLocalDate().toString(),
                    rs.getDate(3) == null ? "" : rs.getDate(3).toLocalDate().toString(),
                    rs.getString(4),
                    rs.getBoolean(5) ? "Yes" : "No",
                    rs.getTimestamp(6) == null ? "" : rs.getTimestamp(6).toLocalDateTime().toString()
                ));
            }
        }
        catch (SQLException ignored)
        {
            this.status.setText("Could not load historical runs.");
        }
    }

    private void showInfo(String title, String content)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content == null ? "Unknown error." : content);
        alert.showAndWait();
    }

    public static final class RunRow
    {
        private String runId;
        private String periodStart;
        private String periodEnd;
        private String status;
        private String locked;
        private String lastUpdated;

        public RunRow(String runId, String periodStart, String periodEnd, String status, String locked, String lastUpdated)
        {
            this.runId = runId;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            this.status = status;
            this.locked = locked;
            this.lastUpdated = lastUpdated;
        }

        public String getRunId() { return this.runId; }
        public String getPeriodStart() { return this.periodStart; }
        public String getPeriodEnd() { return this.periodEnd; }
        public String getStatus() { return this.status; }
        public String getLocked() { return this.locked; }
        public String getLastUpdated() { return this.lastUpdated; }
        public void setStatus(String status) { this.status = status; }
        public void setLocked(String locked) { this.locked = locked; }
        public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}
